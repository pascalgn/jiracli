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

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.Board.Type;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueType;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Cache;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.LoadingList;
import com.github.pascalgn.jiracli.util.MemoizingSupplier;
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

    private final Supplier<Map<String, JSONObject>> fieldDataCache;
    private final Cache<String, IssueData> issueCache;

    public DefaultWebService(String baseUrl, String username, char[] password) {
        this.httpClient = new HttpClient(baseUrl, username, password);
        this.fieldDataCache = new MemoizingSupplier<>(new Supplier<Map<String, JSONObject>>() {
            @Override
            public Map<String, JSONObject> get() {
                return loadFieldData();
            }
        });
        this.issueCache = new Cache<>(new Function<String, IssueData>() {
            @Override
            public IssueData apply(String key) {
                return new IssueData(key);
            }
        });
    }

    @Override
    public String execute(Method method, String path, String body) {
        if (method == Method.GET) {
            return httpClient.get(path, false);
        } else if (method == Method.POST) {
            return httpClient.post(path, body);
        } else if (method == Method.PUT) {
            return httpClient.put(path, body);
        } else {
            throw new UnsupportedOperationException(Objects.toString(method));
        }
    }

    @Override
    public Issue getIssue(String key) {
        URI uri = URI.create(httpClient.getBaseUrl() + "/browse/" + key);
        IssueData issueData = issueCache.get(key);
        LoadableFieldMap fieldMap = new LoadableFieldMap(issueData, fieldDataCache);
        Issue issue = new Issue(key, uri, fieldMap);
        fieldMap.setIssue(issue);
        return issue;
    }

    @Override
    public List<Issue> getEpicIssues(Issue epic) {
        return searchIssues("'Epic Link' = " + epic.getKey() + " ORDER BY Rank");
    }

    @Override
    public List<Issue> searchIssues(String jql) {
        String path = "/rest/api/latest/search?jql=" + urlEncode(jql.trim()) + "&fields=" + INITIAL_FIELDS;
        JSONObject response = httpClient.get(path, TO_OBJECT);
        JSONArray issueList = response.getJSONArray("issues");
        List<Issue> issues = new ArrayList<Issue>();
        for (Object obj : issueList) {
            JSONObject issue = (JSONObject) obj;
            String key = issue.getString("key");
            JSONObject fields = issue.getJSONObject("fields");
            IssueData issueData = new IssueData(key, fields);
            issueCache.putIfAbsent(key, issueData);
            issues.add(getIssue(key));
        }
        return issues;
    }

    @Override
    public void updateIssue(Issue issue) {
        JSONObject update = new JSONObject();
        Collection<Field> fields = issue.getFieldMap().getEditableFields();
        for (Field field : fields) {
            Value value = field.getValue();
            if (value.isModified()) {
                Object val = value.getValue();

                Object set;
                if (val instanceof JSONArray || val instanceof JSONObject || val instanceof String) {
                    set = val;
                } else if (val == null || val == JSONObject.NULL) {
                    set = JSONObject.NULL;
                } else {
                    set = Objects.toString(value.getValue(), "");
                }

                LOGGER.debug("Updating field: {}/{}: New value: {}", issue, field.getId(), set);

                update.put(field.getId(), new JSONArray().put(new JSONObject().put("set", set)));
            }
        }
        if (!update.keySet().isEmpty()) {
            JSONObject request = new JSONObject();
            request.put("update", update);
            String response = httpClient.put("/rest/api/latest/issue/" + issue.getKey(), request.toString());
            if (response != null) {
                LOGGER.warn("Unexpected response received: {}", response);
            }
        }
    }

    @Override
    public Project getProject(String key) {
        try {
            JSONObject response = httpClient.get("/rest/api/latest/project/" + key, TO_OBJECT);
            return toProject(response);
        } catch (NoSuchElementException e) {
            LOGGER.debug("Project not found: {}", key, e);
            return null;
        }
    }

    @Override
    public List<Project> getProjects() {
        JSONArray response = httpClient.get("/rest/api/latest/project", TO_ARRAY);
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
        JSONObject response = httpClient.get(path, TO_OBJECT);
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
        JSONObject response = httpClient.get("/rest/agile/latest/board", TO_OBJECT);
        JSONArray boardArray = response.getJSONArray("values");
        List<Board> boards = new ArrayList<Board>();
        for (Object obj : boardArray) {
            JSONObject json = (JSONObject) obj;
            int id = json.getInt("id");
            String name = json.getString("name");
            Type type = toType(json.optString("type"));
            boards.add(new Board(id, name, type));
        }
        return boards;
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
        JSONObject response = httpClient.get(path, TO_OBJECT);
        JSONArray array = response.getJSONArray("issues");
        List<Issue> result = new ArrayList<Issue>();
        for (Object obj : array) {
            JSONObject issue = (JSONObject) obj;
            result.add(getIssue(issue.getString("key")));
        }
        return result;
    }

    @Override
    public List<Sprint> getSprints(Board board) {
        JSONObject response = httpClient.get("/rest/agile/latest/board/" + board.getId() + "/sprint", TO_OBJECT);
        JSONArray array = response.getJSONArray("values");
        List<Sprint> sprints = new ArrayList<Sprint>();
        for (Object obj : array) {
            JSONObject json = (JSONObject) obj;
            int id = json.getInt("id");
            String name = json.getString("name");
            sprints.add(new Sprint(board, id, name));
        }
        return sprints;
    }

    @Override
    public List<Issue> getIssues(Sprint sprint) {
        String path = "/rest/agile/latest/sprint/" + sprint.getId() + "/issue?fields=" + INITIAL_FIELDS;
        JSONObject response = httpClient.get(path, TO_OBJECT);
        JSONArray array = response.getJSONArray("issues");
        List<Issue> result = new ArrayList<Issue>();
        for (Object obj : array) {
            JSONObject issue = (JSONObject) obj;
            result.add(getIssue(issue.getString("key")));
        }
        return result;
    }

    @Override
    public List<Issue> createIssues(Collection<CreateRequest> createRequests) {
        JSONArray issueUpdates = new JSONArray();
        for (CreateRequest createRequest : createRequests) {
            JSONObject fields = new JSONObject();
            for (Map.Entry<String, String> entry : createRequest.getFields().entrySet()) {
                String id = entry.getKey();
                String value = entry.getValue();
                if (id.equals("project")) {
                    fields.put(id, new JSONObject().put("key", value));
                } else if (id.equals("issuetype")) {
                    fields.put(id, new JSONObject().put("name", value));
                } else {
                    fields.put(id, value);
                }
            }
            issueUpdates.put(new JSONObject().put("fields", fields));
        }

        String request = new JSONObject().put("issueUpdates", issueUpdates).toString();
        String response = httpClient.post("/rest/api/latest/issue/bulk", request);

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

    private Map<String, JSONObject> loadFieldData() {
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();
        JSONArray array = httpClient.get("/rest/api/latest/field", TO_ARRAY);
        for (Object obj : array) {
            JSONObject field = (JSONObject) obj;
            String id = field.getString("id");
            map.put(id, field);
        }
        return map;
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    @Override
    public void close() {
        try {
            issueCache.clear();
        } finally {
            httpClient.close();
        }
    }

    class IssueData {
        private final String issue;
        private final JSONObject initialFields;

        private JSONObject allFields;
        private JSONObject editMeta;

        public IssueData(String issue) {
            this(issue, null);
        }

        public IssueData(String issue, JSONObject initialFields) {
            this.issue = issue;
            this.initialFields = initialFields;
        }

        public JSONObject getInitialFields() {
            return initialFields;
        }

        public synchronized JSONObject getAllFields() {
            if (allFields == null) {
                JSONObject response = httpClient.get("/rest/api/latest/issue/" + issue, TO_OBJECT);
                allFields = response.getJSONObject("fields");
            }
            return allFields;
        }

        public synchronized JSONObject getEditMeta() {
            if (editMeta == null) {
                JSONObject response = httpClient.get("/rest/api/latest/issue/" + issue + "/editmeta", TO_OBJECT);
                editMeta = response.getJSONObject("fields");
            }
            return editMeta;
        }
    }
}
