/**
 * Copyright 2016 Pascal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pascalgn.jiracli.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.DefaultRequest;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Attachment;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.Board.Type;
import com.github.pascalgn.jiracli.model.Change;
import com.github.pascalgn.jiracli.model.Change.Item;
import com.github.pascalgn.jiracli.model.Comment;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldDescription;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueType;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.Sprint.State;
import com.github.pascalgn.jiracli.model.Status;
import com.github.pascalgn.jiracli.model.Transition;
import com.github.pascalgn.jiracli.model.User;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.model.Workflow;
import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.ConversionUtils;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.LoadingList;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;
import com.github.pascalgn.jiracli.web.HttpClient.NotAuthenticatedException;

public class DefaultWebService implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

    private static final Request DEFAULT_REQUEST = new DefaultRequest("issuetype", "status", "summary");
    private static final Request ALL_FIELDS = new DefaultRequest(true);

    private static final Function<Reader, JSONObject> TO_OBJECT = new Function<Reader, JSONObject>() {
        @Override
        public JSONObject apply(Reader reader, Set<Hint> hints) {
            return new JSONObject(new JSONTokener(reader));
        }
    };

    private static final Function<Reader, JSONArray> TO_ARRAY = new Function<Reader, JSONArray>() {
        @Override
        public JSONArray apply(Reader reader, Set<Hint> hints) {
            return new JSONArray(new JSONTokener(reader));
        }
    };

    private final HttpClient httpClient;
    private final CacheImpl cache;
    private final Schema schema;

    public DefaultWebService(Console console) {
        this.httpClient = new HttpClient(console);
        this.cache = new CacheImpl();
        this.schema = new CachedSchema();
    }

    @Override
    public User authenticate() {
        JSONObject json = get("/rest/auth/latest/session", TO_OBJECT);
        String name = json.getString("name");
        return new User(name, name);
    }

    @Override
    public String execute(Method method, URI uri, String body) {
        if (method == Method.GET) {
            return httpClient.get(uri);
        } else if (method == Method.POST) {
            return httpClient.post(uri, body);
        } else if (method == Method.PUT) {
            return httpClient.put(uri, body);
        } else {
            throw new UnsupportedOperationException(Objects.toString(method));
        }
    }

    @Override
    public void download(URI uri, Consumer<InputStream> consumer) {
        httpClient.get(uri, consumer);
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    private Issue toIssue(String key, JSONObject fields) {
        LoadingFieldMap fieldMap = new LoadingFieldMap();
        final Issue issue = new Issue(key, fieldMap);
        if (fields != null) {
            cacheFields(key, fields);
            cacheLinkedIssues(fields);
            List<Field> fieldList = toFields(issue, fields);
            for (Field field : fieldList) {
                fieldMap.addField(field);
            }
        }
        fieldMap.setSupplier(new Supplier<List<Field>>() {
            @Override
            public List<Field> get(Set<Hint> hints) {
                // ignore hints, we will fetch all fields at once, even if only one field is requested,
                // because it's cheaper than doing multiple requests for multiple fields
                String path = "/rest/api/latest/issue/" + issue.getKey();
                JSONObject response = DefaultWebService.this.get(path, "", ALL_FIELDS);
                JSONObject all = response.getJSONObject("fields");
                cacheFields(issue.getKey(), all);
                cacheLinkedIssues(all);
                return toFields(issue, all);
            }
        });
        return issue;
    }

    private Function<JSONObject, Issue> toIssue() {
        return new Function<JSONObject, Issue>() {
            @Override
            public Issue apply(JSONObject obj, Set<Hint> hints) {
                String key = obj.getString("key");
                JSONObject fields = obj.optJSONObject("fields");
                return toIssue(key, fields);
            }
        };
    }

    private void cacheFields(String key, JSONObject fields) {
        JSONObject cached = cache.getFields(key);
        if (cached == null) {
            // at least note that it is a valid issue!
            cache.putFields(key, (fields == null ? new JSONObject() : fields));
        } else if (fields != null) {
            mergeEntries(fields, cached);
        }
    }

    private void cacheLinkedIssues(JSONObject fields) {
        JSONObject parent = fields.optJSONObject("parent");
        if (parent != null) {
            String k = parent.optString("key");
            if (k != null) {
                JSONObject f = parent.optJSONObject("fields");
                cacheFields(k, f);
            }
        }

        JSONArray issuelinks = fields.optJSONArray("issuelinks");
        if (issuelinks != null) {
            for (Object obj : issuelinks) {
                JSONObject json = (JSONObject) obj;
                JSONObject issue = json.optJSONObject("inwardIssue");
                if (issue == null) {
                    issue = json.optJSONObject("outwardIssue");
                }
                if (issue != null) {
                    String k = issue.optString("key");
                    if (k != null) {
                        JSONObject f = issue.optJSONObject("fields");
                        cacheFields(k, f);
                    }
                }
            }
        }
    }

    private static List<Field> toFields(Issue issue, JSONObject json) {
        List<Field> fields = new ArrayList<>();
        for (String id : json.keySet()) {
            Object val = json.get(id);
            if (val == JSONObject.NULL) {
                val = null;
            }
            fields.add(new Field(issue, id, new Value(val)));
        }
        return fields;
    }

    @Override
    public Issue getIssue(String key, Request request) {
        List<Issue> issues = getIssues(Collections.singletonList(key), request);
        if (issues.size() != 1) {
            throw new IllegalStateException("Invalid result for " + key + ": " + issues);
        }
        return issues.get(0);
    }

    @Override
    public List<Issue> getIssues(List<String> keys, Request request) {
        Collection<String> fields = request.getFields();
        if (keys.isEmpty()) {
            return Collections.emptyList();
        } else if (keys.size() == 1) {
            String key = keys.get(0);
            JSONObject fieldJson = cache.getFields(key);
            if (fieldJson == null) {
                // ignore initialFields, loadIssue will fetch all fields
                Issue issue = loadIssue(key);
                return Collections.singletonList(issue);
            } else {
                // don't check for loaded fields, we will fetch them later, if necessary
                Issue issue = toIssue(key, fieldJson);
                return Collections.singletonList(issue);
            }
        } else {
            // we will not request issues for which we already have cached fields:
            Map<String, Issue> resolved = new LinkedHashMap<>();

            Set<String> resolve = new LinkedHashSet<>(keys);

            Iterator<String> it = resolve.iterator();
            while (it.hasNext()) {
                String key = it.next();
                JSONObject fieldJson = cache.getFields(key);
                if (fieldJson != null) {
                    for (String field : fields) {
                        if (!fieldJson.has(field)) {
                            // we should fetch this issue again, to get the missing field(s)
                            fieldJson = null;
                            break;
                        }
                    }
                }
                if (fieldJson != null) {
                    Issue issue = toIssue(key, fieldJson);
                    resolved.put(key, issue);
                    it.remove();
                }
            }

            if (resolve.isEmpty()) {
                // we already have a cached instance of every issue
                return new ArrayList<>(resolved.values());
            }

            List<Issue> result = new ArrayList<>();

            // key order doesn't matter when searching but improves caching:
            List<Issue> searchResults = loadIssues(new TreeSet<>(resolve), request);

            // return the search results in the order the keys were given:
            Set<String> set = new LinkedHashSet<>(keys);
            for (String key : set) {
                Issue found = resolved.get(key);
                if (found == null) {
                    for (Issue issue : searchResults) {
                        if (issue.getKey().equals(key)) {
                            found = issue;
                            break;
                        }
                    }
                }
                if (found == null) {
                    throw new IllegalArgumentException("Issue not found: " + key);
                }
                result.add(found);
            }

            return result;
        }
    }

    private List<Issue> loadIssues(Collection<String> keys, Request request) {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        } else if (keys.size() == 1) {
            String key = keys.iterator().next();
            return Collections.singletonList(loadIssue(key));
        } else {
            return searchIssues("key IN (" + StringUtils.join(keys, ",") + ")", request);
        }
    }

    private Issue loadIssue(String key) {
        // fetch all fields when requesting a single issue
        JSONObject result = get("/rest/api/latest/issue/" + key, "", ALL_FIELDS);
        JSONObject fields = result.getJSONObject("fields");
        return toIssue(key, fields);
    }

    @Override
    public URI getUrl(Issue issue) {
        return URI.create(httpClient.getBaseUrl() + "/browse/" + issue.getKey());
    }

    @Override
    public Collection<Field> getEditableFields(Issue issue) {
        JSONObject response = get("/rest/api/latest/issue/" + issue.getKey() + "/editmeta", TO_OBJECT);
        JSONObject json = response.getJSONObject("fields");
        List<Field> editableFields = new ArrayList<>();
        for (String id : json.keySet()) {
            Field field = issue.getFieldMap().getFieldById(id);
            if (field == null) {
                LOGGER.debug("Unknown field in editmeta: {} (issue {})", id, issue);
            } else {
                boolean setAllowed = false;
                JSONObject editMeta = json.getJSONObject(id);
                JSONArray operations = editMeta.optJSONArray("operations");
                if (operations != null) {
                    for (Object obj : operations) {
                        if ("set".equals(obj)) {
                            setAllowed = true;
                            break;
                        }
                    }
                }
                if (setAllowed && !editableFields.contains(field)) {
                    editableFields.add(field);
                }
            }
        }
        Collections.sort(editableFields, new FieldComparator());
        return editableFields;
    }

    @Override
    public List<Issue> getIssues(Issue epic, Request request) {
        return searchIssues("'Epic Link' = " + epic.getKey() + " ORDER BY Rank", request);
    }

    @Override
    public List<Issue> getLinks(Issue issue, Request request) {
        Field field = issue.getFieldMap().getFieldById("issuelinks");
        if (field != null) {
            Object value = field.getValue().get();
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                List<String> links = new ArrayList<>();
                for (Object obj : array) {
                    JSONObject json = (JSONObject) obj;
                    JSONObject linked = json.optJSONObject("outwardIssue");
                    if (linked == null) {
                        linked = json.optJSONObject("inwardIssue");
                    }
                    if (linked != null) {
                        String key = linked.getString("key");
                        JSONObject fields = linked.optJSONObject("fields");
                        cacheFields(key, fields);
                        cacheLinkedIssues(fields);
                        links.add(key);
                    }
                }
                return getIssues(links, request);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Issue> searchIssues(String jql, Request request) {
        String path = "/rest/api/latest/search?jql=" + urlEncode(jql.trim());
        return new IssueList(path, "issues", request);
    }

    @Override
    public Issue getParent(Issue issue, Request request) {
        Field field = issue.getFieldMap().getFieldById("parent");
        if (field == null) {
            return null;
        }
        JSONObject json = (JSONObject) field.getValue().get();
        if (json == null) {
            return null;
        }
        String key = json.getString("key");
        return getIssue(key, request);
    }

    @Override
    public Workflow getWorkflow(final Issue issue) {
        Workflow workflow = cache.getWorkflow(issue.getKey());
        if (workflow == null) {
            // There is no REST API to get the workflow name, so we need to parse the HTML:
            String workflowName = httpClient.get("/browse/" + issue.getKey(), new Function<Reader, String>() {
                @Override
                public String apply(Reader reader, Set<Hint> hints) {
                    try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            String workflowName = WorkflowHelper.getWorkflowName(line);
                            if (workflowName != null) {
                                return workflowName;
                            }
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                    String message = "Could not parse workflow name for issue: " + issue;
                    throw new NotAuthenticatedException(new IllegalStateException(message));
                }
            });

            String path = "/rest/workflowDesigner/latest/workflows?name=" + urlEncode(workflowName);
            JSONObject response = get(path, TO_OBJECT);

            workflow = WorkflowHelper.parseWorkflow(workflowName, response);

            cache.putWorkflow(issue.getKey(), workflow);
        }
        return workflow;
    }

    @Override
    public List<Attachment> getAttachments(Issue issue) {
        Field field = issue.getFieldMap().getFieldById("attachment");
        if (field != null) {
            Object value = field.getValue().get();
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                List<Attachment> attachments = new ArrayList<>();
                for (Object obj : array) {
                    JSONObject json = (JSONObject) obj;
                    int id = json.getInt("id");
                    String filename = json.getString("filename");
                    String mimeType = json.getString("mimeType");
                    long size = json.getLong("size");
                    URI content = URI.create(json.getString("content"));
                    attachments.add(new Attachment(issue, id, filename, mimeType, size, content));
                }
                return attachments;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Status getStatus(Issue issue) {
        Field status = issue.getFieldMap().getFieldById("status");
        if (status != null) {
            Object value = status.getValue().get();
            if (value instanceof JSONObject) {
                return toStatus((JSONObject) value);
            }
        }
        throw new IllegalStateException("No status set for issue: " + issue);
    }

    @Override
    public List<Change> getChanges(Issue issue) {
        String path = "/rest/api/latest/issue/" + issue.getKey() + "?expand=changelog";
        JSONObject issueJson = get(path, "", new DefaultRequest("creator", "created"));
        cacheFields(issueJson.getString("key"), issueJson.getJSONObject("fields"));

        JSONObject changelog = issueJson.getJSONObject("changelog");
        JSONArray histories = changelog.getJSONArray("histories");

        List<Change> changes = new ArrayList<>();
        for (Object obj : histories) {
            JSONObject history = (JSONObject) obj;
            int id = history.getInt("id");
            User user = ConversionUtils.toUser(history.getJSONObject("author"));
            Date date = ConversionUtils.parseDate(history.getString("created"));
            List<Item> items = new ArrayList<>();
            JSONArray itemArray = history.getJSONArray("items");
            for (Object item : itemArray) {
                JSONObject itemJson = (JSONObject) item;
                String field = itemJson.getString("field");
                String from = toString(itemJson.get("fromString"));
                String to = toString(itemJson.get("toString"));
                items.add(new Item(field, from, to));
            }
            changes.add(new Change(id, user, date, items));
        }

        return changes;
    }

    @Override
    public List<Comment> getComments(Issue issue) {
        List<Comment> comments = new ArrayList<>();

        Field commentField = issue.getFieldMap().getFieldById("comment");
        JSONObject commentJson = (JSONObject) commentField.getValue().get();

        JSONArray commentArray = commentJson.getJSONArray("comments");
        for (Object obj : commentArray) {
            JSONObject comment = (JSONObject) obj;
            int id = comment.getInt("id");
            User author = ConversionUtils.toUser(comment.getJSONObject("author"));
            Date created = ConversionUtils.parseDate(comment.getString("created"));
            String body = comment.getString("body");
            comments.add(new Comment(id, author, body, created));
        }

        return comments;
    }

    private static String toString(Object obj) {
        return (obj == null || obj == JSONObject.NULL ? "" : obj.toString());
    }

    @Override
    public void updateIssue(Issue issue, boolean notifyUsers) {
        JSONObject update = new JSONObject();
        for (Field field : issue.getFieldMap().getLoadedFields()) {
            Value value = field.getValue();
            if (value.modified()) {
                Object val = value.get();

                Object set;
                if (val instanceof JSONArray || val instanceof JSONObject || val instanceof String) {
                    set = val;
                } else if (val == null || val == JSONObject.NULL) {
                    set = JSONObject.NULL;
                } else {
                    set = Objects.toString(value.get(), "");
                }

                LOGGER.debug("Updating field: {}/{}: New value: {}", issue, field.getId(), set);

                update.put(field.getId(), new JSONArray().put(new JSONObject().put("set", set)));
            }
        }
        if (!update.keySet().isEmpty()) {
            JSONObject request = new JSONObject();
            request.put("update", update);
            String path = "/rest/api/latest/issue/" + issue.getKey() + "?notifyUsers=" + notifyUsers;
            String response = put(path, request.toString());
            checkResponseEmpty(response);
        }
    }

    @Override
    public void addComment(Issue issue, String comment) {
        String path = "/rest/api/latest/issue/" + issue.getKey() + "/comment";
        JSONObject request = new JSONObject().put("body", comment);
        post(path, request.toString());
    }

    @Override
    public void transitionIssue(Issue issue, Transition transition) {
        JSONObject request = new JSONObject().put("transition", new JSONObject().put("id", transition.getId()));
        String response = post("/rest/api/latest/issue/" + issue.getKey() + "/transitions", request.toString());
        checkResponseEmpty(response);
    }

    @Override
    public void rankIssues(List<Issue> issues) {
        if (issues.isEmpty() || issues.size() == 1) {
            return;
        }
        Issue first = issues.get(0);
        JSONArray issueArr = new JSONArray();
        for (Issue issue : issues) {
            issueArr.put(issue.getKey());
        }
        JSONObject request = new JSONObject();
        request.put("issues", issueArr);
        request.put("rankBeforeIssue", first.getKey());
        String response = put("/rest/agile/latest/issue/rank", request.toString());
        checkResponseEmpty(response);
    }

    @Override
    public void linkIssues(Issue inward, Issue outward, String name) {
        JSONObject request = new JSONObject();
        request.put("type", new JSONObject().put("name", name));
        request.put("inwardIssue", new JSONObject().put("key", inward.getKey()));
        request.put("outwardIssue", new JSONObject().put("key", outward.getKey()));
        String response = post("/rest/api/latest/issueLink", request.toString());
        checkResponseEmpty(response);
    }

    @Override
    public void removeLink(Issue inward, Issue outward, String name) {
        Integer forward = findIssueLink(inward, "outwardIssue", outward.getKey(), name);
        if (forward != null) {
            removeLink(forward);
        }
        Integer backward = findIssueLink(outward, "inwardIssue", inward.getKey(), name);
        if (backward != null) {
            removeLink(backward);
        }
        if (forward == null && backward == null) {
            throw new IllegalArgumentException("No link between " + inward.getKey()
                    + " and " + outward.getKey() + ": " + name);
        }
    }

    private void removeLink(int id) {
        String response = delete("/rest/api/latest/issueLink/" + id);
        checkResponseEmpty(response);
    }

    private static Integer findIssueLink(Issue source, String targetField, String targetKey, String name) {
        Field links = source.getFieldMap().getFieldById("issuelinks");
        JSONArray linksJson = (JSONArray) links.getValue().get();
        for (Object linkObj : linksJson) {
            JSONObject linkJson = (JSONObject) linkObj;
            JSONObject typeJson = linkJson.getJSONObject("type");
            if (typeJson.getString("name").equals(name)) {
                JSONObject targetJson = linkJson.optJSONObject(targetField);
                if (targetJson != null) {
                    String key = targetJson.getString("key");
                    if (key.equals(targetKey)) {
                        return linkJson.getInt("id");
                    }
                }
            }
        }
        return null;
    }

    private static void checkResponseEmpty(String response) {
        if (response != null && !response.isEmpty() && !response.trim().isEmpty()) {
            LOGGER.warn("Unexpected response received: {}", response);
        }
    }

    @Override
    public Status getStatus(String name) {
        JSONObject response = get("/rest/api/latest/status/" + urlEncode(name), TO_OBJECT);
        return toStatus(response);
    }

    private static Status toStatus(JSONObject json) {
        int id = json.getInt("id");
        String name = json.getString("name");
        return new Status(id, name);
    }

    @Override
    public Project getProject(String key) {
        JSONObject response = get("/rest/api/latest/project/" + key, TO_OBJECT);
        return toProject(response);
    }

    @Override
    public List<Project> getProjects() {
        String path = "/rest/api/latest/project";
        String cached = cache.getResponse(path);

        JSONArray response;
        if (cached == null) {
            response = httpClient.get(path, new Function<Reader, JSONArray>() {
                @Override
                public JSONArray apply(Reader reader, Set<Hint> hints) {
                    JSONArray array = TO_ARRAY.apply(reader, hints);
                    if (array.length() == 0) {
                        throw new NotAuthenticatedException(new IllegalStateException("No projects found!"));
                    }
                    return array;
                }
            });
            cache.putResponse(path, response.toString());
        } else {
            response = new JSONArray(cached);
        }

        List<Project> projects = new ArrayList<>();
        for (Object obj : response) {
            JSONObject json = (JSONObject) obj;
            projects.add(toProject(json));
        }
        return projects;
    }

    private Project toProject(JSONObject json) {
        int id = json.getInt("id");
        final String key = json.getString("key");
        String name = json.getString("name");
        List<IssueType> issueTypes;
        JSONArray issueTypeArray = json.optJSONArray("issueTypes");
        if (issueTypeArray == null) {
            issueTypes = new LoadingList<IssueType>() {
                @Override
                protected List<IssueType> loadList() {
                    return loadIssueTypes(key);
                }
            };
        } else {
            issueTypes = toIssueTypes(issueTypeArray);
        }
        return new Project(id, key, name, issueTypes);
    }

    private List<IssueType> loadIssueTypes(String project) {
        JSONObject response = get("/rest/api/latest/project/" + project, TO_OBJECT);
        JSONArray issueTypes = response.getJSONArray("issueTypes");
        return toIssueTypes(issueTypes);
    }

    private static List<IssueType> toIssueTypes(JSONArray json) {
        List<IssueType> issueTypes = new ArrayList<>();
        for (Object issueTypeObj : json) {
            JSONObject issueTypeJson = (JSONObject) issueTypeObj;
            int id = issueTypeJson.getInt("id");
            String name = issueTypeJson.getString("name");
            boolean subtask = issueTypeJson.getBoolean("subtask");
            issueTypes.add(new IssueType(id, name, subtask));
        }
        return issueTypes;
    }

    @Override
    public List<FieldDescription> getFields(final Project project, IssueType issueType) {
        String path = "/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields";
        path += "&projectIds=" + project.getId();
        path += "&issuetypeIds=" + issueType.getId();

        JSONObject response = httpClient.get(path, new Function<Reader, JSONObject>() {
            @Override
            public JSONObject apply(Reader reader, Set<Hint> hints) {
                JSONObject json = TO_OBJECT.apply(reader, hints);
                JSONArray projects = json.getJSONArray("projects");
                if (projects.length() == 0) {
                    String msg = "Project not found: " + project.getKey();
                    throw new NotAuthenticatedException(new IllegalStateException(msg));
                }
                return json;
            }
        });

        cache.putResponse(path, response.toString());

        JSONArray projectArr = response.getJSONArray("projects");
        for (Object projectObj : projectArr) {
            JSONObject projectJson = (JSONObject) projectObj;
            int projectId = projectJson.getInt("id");
            if (projectId != project.getId()) {
                continue;
            }
            JSONArray issueTypeArr = projectJson.getJSONArray("issuetypes");
            for (Object issueTypeObj : issueTypeArr) {
                JSONObject issueTypeJson = (JSONObject) issueTypeObj;
                int id = issueTypeJson.getInt("id");
                if (id != issueType.getId()) {
                    continue;
                }
                JSONObject fieldObj = issueTypeJson.getJSONObject("fields");
                List<FieldDescription> fields = new ArrayList<>();
                for (String fieldId : fieldObj.keySet()) {
                    JSONObject fieldJson = fieldObj.getJSONObject(fieldId);
                    String fieldName = fieldJson.getString("name");
                    boolean required = fieldJson.getBoolean("required");
                    fields.add(new FieldDescription(fieldId, fieldName, required));
                }
                return fields;
            }
        }

        throw new IllegalStateException("No fields found: " + project + ", " + issueType);
    }

    @Override
    public Board getBoard(int id) {
        JSONObject json = get("/rest/agile/latest/board/" + id, TO_OBJECT);
        return toBoard(json);
    }

    @Override
    public List<Board> getBoards() {
        return getBoards(null);
    }

    @Override
    public List<Board> getBoards(String name) {
        String path = "/rest/agile/latest/board";
        if (name != null) {
            path += "?name=" + urlEncode(name);
        }
        return new PaginationList<>(path, "values", new Function<JSONObject, Board>() {
            @Override
            public Board apply(JSONObject json, Set<Hint> hints) {
                return toBoard(json);
            }
        });
    }

    private static Board toBoard(JSONObject json) {
        int id = json.getInt("id");
        String boardName = json.getString("name");
        Type type = toType(json.optString("type"));
        return new Board(id, boardName, type);
    }

    private static Type toType(String str) {
        String s = Objects.toString(str, "").trim().toLowerCase();
        switch (s) {
            case "scrum":
                return Type.SCRUM;

            case "kanban":
                return Type.KANBAN;

            default:
                return Type.UNKNOWN;
        }
    }

    @Override
    public List<Issue> getIssues(Board board, Request request) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/issue";
        return new IssueList(path, "issues", request);
    }

    @Override
    public List<Issue> getEpics(Board board, Request request) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/epic";
        return new IssueList(path, "values", request);
    }

    @Override
    public Sprint getSprint(int id) {
        JSONObject json = get("/rest/agile/latest/sprint/" + id, TO_OBJECT);
        return toSprint(json);
    }

    @Override
    public List<Sprint> getSprints(final Board board) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/sprint";
        return new PaginationList<>(path, "values", new Function<JSONObject, Sprint>() {
            @Override
            public Sprint apply(JSONObject json, Set<Hint> hints) {
                return toSprint(json);
            }
        });
    }

    private static Sprint toSprint(JSONObject json) {
        int id = json.getInt("id");
        String name = json.getString("name");
        State state = toState(json.optString("state"));
        return new Sprint(id, name, state);
    }

    private static State toState(String str) {
        String s = Objects.toString(str, "").trim().toLowerCase();
        switch (s) {
            case "closed":
                return State.CLOSED;

            case "active":
                return State.ACTIVE;

            case "future":
                return State.FUTURE;

            default:
                return State.UNKNOWN;
        }
    }

    @Override
    public List<Issue> getIssues(Sprint sprint, Request request) {
        String path = "/rest/agile/latest/sprint/" + sprint.getId() + "/issue";
        return new IssueList(path, "issues", request);
    }

    @Override
    public List<Issue> createIssues(Collection<CreateRequest> createRequests) {
        JSONArray issueUpdates = new JSONArray();
        for (CreateRequest createRequest : createRequests) {
            JSONObject fields = new JSONObject();
            for (Map.Entry<String, String> entry : createRequest.getFields().entrySet()) {
                String id = entry.getKey();
                String value = entry.getValue();
                Converter converter = schema.getConverter(id);
                Object val;
                try {
                    val = converter.fromString(value);
                } catch (RuntimeException e) {
                    throw new IllegalStateException("Error converting field " + id + ": " + value, e);
                }
                fields.put(id, val);
            }
            issueUpdates.put(new JSONObject().put("fields", fields));
        }

        String request = new JSONObject().put("issueUpdates", issueUpdates).toString();
        LOGGER.debug("Request: {}", request);

        String response = post("/rest/api/latest/issue/bulk", request);

        JSONObject responseObj = new JSONObject(response);
        JSONArray issueArr = responseObj.getJSONArray("issues");
        List<String> keys = new ArrayList<>();
        for (Object obj : issueArr) {
            JSONObject json = (JSONObject) obj;
            String key = json.getString("key");
            keys.add(key);
        }
        return getIssues(keys, DEFAULT_REQUEST);
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    private synchronized JSONObject get(String path, String issuesField, Request request) {
        Collection<String> fields = filterFields(request);
        String p = addParams(path, request, fields);
        String cached = cache.getResponse(p);
        if (cached == null) {
            // find a cached response for any fields:
            JSONObject cachedJson = cache.getIssues(path);
            if (cachedJson != null && !request.getAllFields()) {
                Object issues = (issuesField.isEmpty() ? cachedJson : cachedJson.get(issuesField));
                boolean fieldsCached = checkFieldsCached(issues, fields);
                if (fieldsCached) {
                    // cached response is valid, just use it
                    return cachedJson;
                }
            }
            JSONObject result = get(p, TO_OBJECT);
            cacheResults(path, issuesField, fields, cachedJson, result);
            return result;
        } else {
            JSONObject result;
            try (StringReader reader = new StringReader(cached)) {
                result = TO_OBJECT.apply(reader, Hint.none());
            }
            Object source = (issuesField.isEmpty() ? result : result.get(issuesField));
            addMissingFields(source, fields);
            return result;
        }
    }

    private void cacheResults(String path, String issuesField, Collection<String> fields, JSONObject cached,
            JSONObject result) {
        Object source = (issuesField.isEmpty() ? result : result.get(issuesField));
        // Mark fields that have been requested, but not returned, as null, because otherwise,
        // all issue fields would be fetched the next time to make sure the field isn't there
        addMissingFields(source, fields);
        if (cached == null) {
            cache.putIssues(path, result);
        } else {
            // merge the missing fields with the fields of the cache entry:
            Object target = (issuesField.isEmpty() ? cached : cached.get(issuesField));
            if (source instanceof JSONObject) {
                JSONObject json = (JSONObject) source;
                mergeFields(json, target);
            } else if (source instanceof JSONArray) {
                for (Object obj : (JSONArray) source) {
                    JSONObject json = (JSONObject) obj;
                    mergeFields(json, target);
                }
            } else {
                throw new IllegalStateException("Invalid response: " + path + ": " + source);
            }
        }
    }

    private Collection<String> filterFields(Request request) {
        Collection<String> result = new ArrayList<>();
        for (String field : request.getFields()) {
            String f = (field.contains(".") ? field.split("\\.", 2)[0] : field);
            String id;
            try {
                id = schema.getId(f);
            } catch (IllegalArgumentException e) {
                LOGGER.trace("Unknown field: {}", f, e);
                continue;
            }
            result.add(id);
        }
        return result;
    }

    private String addParams(String path, Request request, Collection<String> fields) {
        String fieldParam;
        if (request.getAllFields() || request.getFields().contains("*")) {
            fieldParam = "*all";
        } else {
            Set<String> sorted = new TreeSet<>(DEFAULT_REQUEST.getFields());
            if (fields.isEmpty()) {
                fieldParam = StringUtils.join(sorted, ",");
            } else {
                sorted.addAll(fields);
                StringBuilder str = new StringBuilder();
                for (String field : sorted) {
                    if (field.equals("key")) {
                        continue;
                    }
                    if (str.length() > 0) {
                        str.append(",");
                    }
                    str.append(field);
                }
                fieldParam = str.toString();
            }
        }
        return addParam(path, "fields", fieldParam);
    }

    private static boolean checkFieldsCached(Object issues, Collection<String> fields) {
        boolean allFieldsCached;
        if (issues instanceof JSONObject) {
            allFieldsCached = hasFields(((JSONObject) issues).getJSONObject("fields"), fields);
        } else if (issues instanceof JSONArray) {
            allFieldsCached = true;
            for (Object item : (JSONArray) issues) {
                JSONObject fieldsJson = ((JSONObject) item).getJSONObject("fields");
                if (!hasFields(fieldsJson, fields)) {
                    allFieldsCached = false;
                    break;
                }
            }
        } else {
            throw new IllegalStateException("Invalid cached value: " + issues);
        }
        return allFieldsCached;
    }

    private static boolean hasFields(JSONObject json, Collection<String> fields) {
        for (String field : fields) {
            if (!field.equals("key") && !json.has(field)) {
                return false;
            }
        }
        return true;
    }

    private static void addMissingFields(Object obj, Collection<String> fields) {
        if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            addFields(json, fields);
        } else if (obj instanceof JSONArray) {
            for (Object o : (JSONArray) obj) {
                JSONObject json = (JSONObject) o;
                addFields(json, fields);
            }
        }
    }

    private static void addFields(JSONObject json, Collection<String> fields) {
        JSONObject fieldJson = json.getJSONObject("fields");
        for (String field : fields) {
            if (!fieldJson.has(field)) {
                fieldJson.put(field, JSONObject.NULL);
            }
        }
    }

    private static void mergeFields(JSONObject source, Object target) {
        JSONObject sourceFields = source.getJSONObject("fields");
        if (target instanceof JSONObject) {
            JSONObject targetFields = ((JSONObject) target).getJSONObject("fields");
            mergeEntries(sourceFields, targetFields);
        } else if (target instanceof JSONArray) {
            String sourceKey = source.getString("key");
            for (Object targetIssueObj : (JSONArray) target) {
                JSONObject targetIssue = (JSONObject) targetIssueObj;
                if (sourceKey.equals(targetIssue.get("key"))) {
                    JSONObject targetFields = targetIssue.getJSONObject("fields");
                    mergeEntries(sourceFields, targetFields);
                }
            }
        } else {
            throw new IllegalStateException("Invalid object: " + target);
        }
    }

    private static void mergeEntries(JSONObject source, JSONObject target) {
        for (String key : source.keySet()) {
            if (!target.has(key)) {
                target.put(key, source.get(key));
            }
        }
    }

    private synchronized <T> T get(String path, Function<Reader, T> function) {
        String response = cache.getResponse(path);
        boolean cacheResponse = false;
        if (response == null) {
            response = httpClient.get(path);
            cacheResponse = true;
        }
        T result;
        try (StringReader reader = new StringReader(response)) {
            result = function.apply(reader, Hint.none());
        }
        if (cacheResponse) {
            // response could be converted, so it's probably safe to cache now:
            cache.putResponse(path, response);
        }
        return result;
    }

    private String post(String path, String body) {
        clearCache();
        return httpClient.post(path, body);
    }

    private String put(String path, String body) {
        clearCache();
        return httpClient.put(path, body);
    }

    private String delete(String path) {
        clearCache();
        return httpClient.delete(path);
    }

    private void clearCache() {
        cache.clear();
    }

    @Override
    public WebService.Cache getCache() {
        return cache;
    }

    @Override
    public void close() {
        try {
            clearCache();
        } finally {
            httpClient.close();
        }
    }

    private class IssueList extends PaginationList<Issue> {
        private final Request request;

        public IssueList(String path, String field, Request request) {
            super(path, field, toIssue());
            this.request = request;
        }

        @Override
        protected JSONObject fetch(String path) {
            return DefaultWebService.this.get(path, getField(), request);
        }
    }

    private class PaginationList<E> extends AbstractList<E> {
        private final List<E> fetched;

        private final String path;
        private final String field;
        private final Function<JSONObject, E> function;

        private boolean fetchedAll;

        private int size;

        public PaginationList(String path, String field, Function<JSONObject, E> function) {
            this.path = path;
            this.field = field;
            this.function = function;
            this.fetched = new ArrayList<>();
            this.size = -1;
        }

        protected String getField() {
            return field;
        }

        @Override
        public E get(int index) {
            while (!fetchedAll && index >= fetched.size()) {
                fetchMore();
            }
            return fetched.get(index);
        }

        @Override
        public int size() {
            while (!fetchedAll && size == -1) {
                fetchMore();
            }
            return size;
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    while (!fetchedAll && index >= fetched.size()) {
                        fetchMore();
                    }
                    return index < fetched.size();
                }

                @Override
                public E next() {
                    return get(index++);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        private void fetchMore() {
            if (fetchedAll) {
                throw new IllegalStateException();
            }
            String p = path;
            if (!fetched.isEmpty()) {
                p = addParam(p, "startAt", fetched.size());
            }
            JSONObject object = fetch(p);
            JSONArray values = object.getJSONArray(field);
            for (Object obj : values) {
                JSONObject json = (JSONObject) obj;
                E element = function.apply(json, Hint.none());
                if (element == null) {
                    throw new NullPointerException("Element cannot be null!");
                }
                fetched.add(element);
            }
            boolean isLast = object.optBoolean("isLast", false);
            if (isLast) {
                fetchedAll = true;
                if (size == -1) {
                    size = fetched.size();
                }
            } else {
                int total = object.optInt("total", -1);
                if (total != -1) {
                    size = total;
                    if (fetched.size() >= size) {
                        fetchedAll = true;
                    }
                }
            }
        }

        protected JSONObject fetch(String path) {
            return DefaultWebService.this.get(path, TO_OBJECT);
        }
    }

    private static String addParam(String path, String parameter, Object value) {
        return path + (path.contains("?") ? "&" : "?") + parameter + "=" + value;
    }

    private class CachedSchema implements Schema {
        @Override
        public Set<String> getFields() {
            return new HashSet<>(getFieldInfos().keySet());
        }

        @Override
        public String getId(String field) {
            Map<String, FieldInfo> fieldInfos = getFieldInfos();
            if (fieldInfos.containsKey(field)) {
                return field;
            }
            for (Map.Entry<String, FieldInfo> entry : fieldInfos.entrySet()) {
                if (entry.getValue().getName().equals(field)) {
                    return entry.getKey();
                }
            }
            throw new IllegalArgumentException("Unknown field: " + field);
        }

        @Override
        public String getName(String field) {
            FieldInfo fieldInfo = getFieldInfos().get(field);
            return (fieldInfo == null ? field : fieldInfo.getName());
        }

        @Override
        public Converter getConverter(String field) {
            FieldInfo fieldInfo = getFieldInfos().get(field);
            if (fieldInfo == null) {
                return ConverterProvider.getDefaultConverter();
            } else {
                return fieldInfo.getConverter();
            }
        }

        private synchronized Map<String, FieldInfo> getFieldInfos() {
            Map<String, FieldInfo> fieldInfos = cache.getFieldInfos();
            if (fieldInfos == null) {
                fieldInfos = new HashMap<>();
                JSONArray array = get("/rest/api/latest/field", TO_ARRAY);
                for (Object obj : array) {
                    JSONObject json = (JSONObject) obj;
                    String id = json.getString("id");

                    String name = json.optString("name");
                    if (name == null || name.isEmpty()) {
                        name = id;
                    }

                    JSONObject schema = json.optJSONObject("schema");
                    if (schema == null) {
                        LOGGER.trace("Schema is null: {}", json);
                    }

                    Converter converter = ConverterProvider.getConverter(schema);
                    fieldInfos.put(id, new FieldInfo(name, converter));
                }
                // Field 'parent' is not included in the array!
                fieldInfos.put("parent", new FieldInfo("Parent", ConverterProvider.getIssueConverter()));
                cache.setFieldInfos(fieldInfos);
            }
            return fieldInfos;
        }
    }

    private static class FieldComparator implements Comparator<Field> {
        @Override
        public int compare(Field f1, Field f2) {
            return f1.getId().compareTo(f2.getId());
        }
    }
}
