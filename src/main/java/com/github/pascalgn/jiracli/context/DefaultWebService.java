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
package com.github.pascalgn.jiracli.context;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Attachment;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.Board.Type;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueType;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.Credentials;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.LoadingList;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultWebService implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

    private static final String INITIAL_FIELDS = "summary";

    private static final Function<Reader, JSONObject> TO_OBJECT = new Function<Reader, JSONObject>() {
        @Override
        public JSONObject apply(Reader reader) {
            return new JSONObject(new JSONTokener(reader));
        }
    };

    private static final Function<Reader, JSONArray> TO_ARRAY = new Function<Reader, JSONArray>() {
        @Override
        public JSONArray apply(Reader reader) {
            return new JSONArray(new JSONTokener(reader));
        }
    };

    private final HttpClient httpClient;

    private final Map<String, String> cache;
    private final Map<String, JSONObject> fieldCache;

    private final LoadingSchema schema;

    public DefaultWebService(Console console) {
        this.httpClient = createHttpClient(console);
        this.cache = new HashMap<String, String>();
        this.fieldCache = new HashMap<String, JSONObject>();
        this.schema = new LoadingSchema() {
            @Override
            protected Map<String, FieldInfo> loadMap() {
                Map<String, FieldInfo> map = new HashMap<>();
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
                    map.put(id, new FieldInfo(name, converter));
                }
                return map;
            }
        };
    }

    private static HttpClient createHttpClient(final Console console) {
        Supplier<String> baseUrl = new Supplier<String>() {
            @Override
            public String get() {
                return console.getBaseUrl();
            }
        };
        Function<String, Credentials> credentials = new Function<String, Credentials>() {
            @Override
            public Credentials apply(String url) {
                return console.getCredentials(url);
            }
        };
        return new HttpClient(baseUrl, credentials);
    }

    @Override
    public String execute(Method method, String path, String body) {
        if (method == Method.GET) {
            return httpClient.get(path);
        } else if (method == Method.POST) {
            return httpClient.post(path, body);
        } else if (method == Method.PUT) {
            return httpClient.put(path, body);
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

    @Override
    public Issue getIssue(String key) {
        JSONObject fields = fieldCache.get(key);
        return getIssue(key, fields);
    }

    private Issue getIssue(String key, JSONObject fields) {
        LoadingFieldMap fieldMap = new LoadingFieldMap();
        final Issue issue = new Issue(key, fieldMap);
        if (fields != null) {
            cacheFields(key, fields);
            cacheIssueLinks(fields);
            List<Field> fieldList = toFields(issue, fields);
            for (Field field : fieldList) {
                fieldMap.addField(field);
            }
        }
        fieldMap.setSupplier(new Supplier<List<Field>>() {
            @Override
            public List<Field> get() {
                JSONObject response = DefaultWebService.this.get("/rest/api/latest/issue/" + issue, TO_OBJECT);
                JSONObject all = response.getJSONObject("fields");
                cacheIssueLinks(all);
                return toFields(issue, all);
            }
        });
        return issue;
    }

    private Function<JSONObject, Issue> toIssue() {
        return new Function<JSONObject, Issue>() {
            @Override
            public Issue apply(JSONObject obj) {
                String key = obj.getString("key");
                JSONObject fields = obj.optJSONObject("fields");
                return getIssue(key, fields);
            }
        };
    }

    private void cacheFields(String key, JSONObject fields) {
        JSONObject cached = fieldCache.get(key);
        if (cached == null) {
            fieldCache.put(key, fields);
        } else {
            for (String id : fields.keySet()) {
                if (!cached.has(id)) {
                    cached.put(id, fields.get(id));
                }
            }
        }
    }

    private void cacheIssueLinks(JSONObject fields) {
        JSONArray issuelinks = fields.optJSONArray("issuelinks");
        if (issuelinks != null) {
            for (Object obj : issuelinks) {
                JSONObject json = (JSONObject) obj;
                JSONObject issue = json.optJSONObject("inwardIssue");
                if (issue == null) {
                    issue = json.optJSONObject("outwardIssue");
                }
                if (issue != null) {
                    String k = json.optString("key");
                    JSONObject f = json.optJSONObject("fields");
                    if (k != null && f != null) {
                        cacheFields(k, f);
                    }
                }
            }
        }
    }

    private static List<Field> toFields(Issue issue, JSONObject json) {
        List<Field> fields = new ArrayList<Field>();
        for (String id : json.keySet()) {
            Object val = json.get(id);
            fields.add(new Field(issue, id, new Value(val)));
        }
        return fields;
    }

    @Override
    public URI getUrl(Issue issue) {
        return URI.create(httpClient.getBaseUrl() + "/browse/" + issue.getKey());
    }

    @Override
    public Collection<Field> getEditableFields(Issue issue) {
        JSONObject response = get("/rest/api/latest/issue/" + issue + "/editmeta", TO_OBJECT);
        JSONObject json = response.getJSONObject("fields");
        List<Field> editableFields = new ArrayList<Field>();
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
    public List<Issue> getIssues(Issue epic) {
        return searchIssues("'Epic Link' = " + epic.getKey() + " ORDER BY Rank");
    }

    @Override
    public List<Issue> getLinks(Issue issue) {
        Field field = issue.getFieldMap().getFieldById("issuelinks");
        if (field != null) {
            Object value = field.getValue().get();
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                List<Issue> links = new ArrayList<Issue>();
                for (Object obj : array) {
                    JSONObject json = (JSONObject) obj;
                    JSONObject linked = json.optJSONObject("outwardIssue");
                    if (linked == null) {
                        linked = json.optJSONObject("inwardIssue");
                    }
                    if (linked != null) {
                        String key = linked.getString("key");
                        JSONObject fields = linked.optJSONObject("fields");
                        links.add(getIssue(key, fields));
                    }
                }
                return links;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Issue> searchIssues(String jql) {
        String path = "/rest/api/latest/search?jql=" + urlEncode(jql.trim()) + "&fields=" + INITIAL_FIELDS;
        return new PaginationList<Issue>(path, "issues", toIssue());
    }

    @Override
    public void updateIssue(Issue issue) {
        JSONObject update = new JSONObject();
        Collection<Field> fields = getEditableFields(issue);
        for (Field field : fields) {
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
            String response = put("/rest/api/latest/issue/" + issue.getKey(), request.toString());
            if (response != null) {
                LOGGER.warn("Unexpected response received: {}", response);
            }
        }
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
        if (response != null) {
            LOGGER.warn("Unexpected response received: {}", response);
        }
    }

    @Override
    public List<Attachment> getAttachments(Issue issue) {
        Field field = issue.getFieldMap().getFieldById("attachment");
        if (field != null) {
            Object value = field.getValue().get();
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                List<Attachment> attachments = new ArrayList<Attachment>();
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
    public Project getProject(String key) {
        try {
            JSONObject response = get("/rest/api/latest/project/" + key, TO_OBJECT);
            return toProject(response);
        } catch (NoSuchElementException e) {
            LOGGER.debug("Project not found: {}", key, e);
            return null;
        }
    }

    @Override
    public List<Project> getProjects() {
        JSONArray response = get("/rest/api/latest/project", TO_ARRAY);
        List<Project> projects = new ArrayList<Project>();
        for (Object obj : response) {
            JSONObject json = (JSONObject) obj;
            projects.add(toProject(json));
        }
        return projects;
    }

    private Project toProject(JSONObject json) {
        final int id = json.getInt("id");
        String key = json.getString("key");
        String name = json.getString("name");
        List<IssueType> issueTypes = new LoadingList<IssueType>() {
            @Override
            protected List<IssueType> loadList() {
                return getIssueTypes(id);
            }
        };
        return new Project(id, key, name, issueTypes);
    }

    private List<IssueType> getIssueTypes(int project) {
        String path = "/rest/api/latest/issue/createmeta?expand=projects.issuetypes.fields&projectIds=" + project;
        JSONObject response = get(path, TO_OBJECT);
        JSONArray projectArr = response.getJSONArray("projects");
        List<IssueType> issueTypes = new ArrayList<IssueType>();
        for (Object projectObj : projectArr) {
            JSONObject projectJson = (JSONObject) projectObj;
            int projectId = projectJson.getInt("id");
            if (projectId != project) {
                continue;
            }
            JSONArray issueTypeArr = projectJson.getJSONArray("issuetypes");
            for (Object issueTypeObj : issueTypeArr) {
                JSONObject issueTypeJson = (JSONObject) issueTypeObj;
                int id = issueTypeJson.getInt("id");
                String name = issueTypeJson.getString("name");
                JSONObject fieldObj = issueTypeJson.getJSONObject("fields");
                List<IssueType.Field> fields = new ArrayList<>();
                for (String fieldId : fieldObj.keySet()) {
                    JSONObject fieldJson = fieldObj.getJSONObject(fieldId);
                    String fieldName = fieldJson.getString("name");
                    boolean required = fieldJson.getBoolean("required");
                    fields.add(new IssueType.Field(fieldId, fieldName, required));
                }
                issueTypes.add(new IssueType(id, name, fields));
            }
        }
        return issueTypes;
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
        return new PaginationList<Board>(path, "values", new Function<JSONObject, Board>() {
            @Override
            public Board apply(JSONObject json) {
                int id = json.getInt("id");
                String boardName = json.getString("name");
                Type type = toType(json.optString("type"));
                return new Board(id, boardName, type);
            }
        });
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
    public List<Issue> getIssues(Board board) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/issue?fields=" + INITIAL_FIELDS;
        return new PaginationList<Issue>(path, "issues", toIssue());
    }

    @Override
    public List<Issue> getEpics(Board board) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/epic";
        return new PaginationList<Issue>(path, "values", toIssue());
    }

    @Override
    public List<Sprint> getSprints(final Board board) {
        String path = "/rest/agile/latest/board/" + board.getId() + "/sprint";
        return new PaginationList<Sprint>(path, "values", new Function<JSONObject, Sprint>() {
            @Override
            public Sprint apply(JSONObject json) {
                int id = json.getInt("id");
                String name = json.getString("name");
                return new Sprint(board, id, name);
            }
        });
    }

    @Override
    public List<Issue> getIssues(Sprint sprint) {
        String path = "/rest/agile/latest/sprint/" + sprint.getId() + "/issue?fields=" + INITIAL_FIELDS;
        return new PaginationList<Issue>(path, "issues", toIssue());
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
        List<Issue> issues = new ArrayList<Issue>();
        for (Object issueObj : issueArr) {
            JSONObject issueJson = (JSONObject) issueObj;
            String key = issueJson.getString("key");
            issues.add(getIssue(key));
        }
        return issues;
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    private synchronized <T> T get(String path, Function<Reader, T> function) {
        String response = cache.get(path);
        boolean cacheResponse = false;
        if (response == null) {
            response = httpClient.get(path);
            cacheResponse = true;
        }
        T result;
        try (StringReader reader = new StringReader(response)) {
            result = function.apply(reader);
        }
        if (cacheResponse) {
            // response could be converted, so it's probably safe to cache now:
            cache.put(path, response);
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

    private void clearCache() {
        cache.clear();
        fieldCache.clear();
    }

    @Override
    public void close() {
        try {
            clearCache();
        } finally {
            httpClient.close();
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
            this.fetched = new ArrayList<E>();
            this.size = -1;
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
                p += (p.contains("?") ? "&" : "?") + "startAt=" + fetched.size();
            }
            JSONObject object = DefaultWebService.this.get(p, TO_OBJECT);
            JSONArray values = object.getJSONArray(field);
            for (Object obj : values) {
                JSONObject json = (JSONObject) obj;
                E element = function.apply(json);
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
                    if (size == fetched.size()) {
                        fetchedAll = true;
                    }
                }
            }
        }
    }

    private static class FieldComparator implements Comparator<Field> {
        @Override
        public int compare(Field f1, Field f2) {
            return f1.getId().compareTo(f2.getId());
        }
    }
}
