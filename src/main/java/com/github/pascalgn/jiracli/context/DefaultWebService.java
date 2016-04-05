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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
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
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.LoadingList;
import com.github.pascalgn.jiracli.util.MemoizingSupplier;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultWebService implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

    private static final String INITIAL_FIELDS = "summary";

    private static final SSLConnectionSocketFactory SSL_SOCKET_FACTORY;

    static {
        SSLContext sslcontext;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        SSL_SOCKET_FACTORY = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
    }

    private static final Function<Reader, String> TO_STRING = new Function<Reader, String>() {
        @Override
        public String apply(Reader reader) {
            return IOUtils.toString(reader);
        }
    };

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

    private final String rootURL;
    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpClientContext;

    private final Supplier<Map<String, JSONObject>> fieldDataCache;
    private final Cache<String, IssueData> issueCache;
    private final Cache<String, JSONObject> searchCache;
    private final Supplier<List<JSONObject>> boardCache;
    private final Cache<Integer, List<JSONObject>> sprintCache;
    private final Cache<Integer, JSONArray> sprintIssuesCache;

    public DefaultWebService(String rootURL, String username, char[] password) {
        this.rootURL = stripEnd(rootURL, "/");
        this.httpClient = createHttpClient();
        this.httpClientContext = createHttpClientContext(username, password);
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
        this.searchCache = new Cache<>(new Function<String, JSONObject>() {
            @Override
            public JSONObject apply(String jql) {
                return loadIssueList(jql);
            }
        });
        this.boardCache = new MemoizingSupplier<>(new Supplier<List<JSONObject>>() {
            @Override
            public List<JSONObject> get() {
                return loadBoards();
            }
        });
        this.sprintCache = new Cache<>(new Function<Integer, List<JSONObject>>() {
            @Override
            public List<JSONObject> apply(Integer board) {
                return loadSprints(board);
            }
        });
        this.sprintIssuesCache = new Cache<>(new Function<Integer, JSONArray>() {
            @Override
            public JSONArray apply(Integer sprint) {
                return loadSprintIssues(sprint);
            }
        });
    }

    private static CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(SSL_SOCKET_FACTORY);
        return httpClientBuilder.build();
    }

    private static HttpClientContext createHttpClientContext(String username, char[] password) {
        HttpClientContext context = HttpClientContext.create();
        if (username != null && password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, new String(password)));

            final AuthScheme authScheme = new BasicScheme();
            AuthCache authCache = new AuthCache() {
                @Override
                public void remove(HttpHost host) {
                }

                @Override
                public void put(HttpHost host, AuthScheme authScheme) {
                }

                @Override
                public AuthScheme get(HttpHost host) {
                    return authScheme;
                }

                @Override
                public void clear() {
                }
            };

            context.setCredentialsProvider(credentialsProvider);
            context.setAuthCache(authCache);
        }
        return context;
    }

    private static String stripEnd(String str, String end) {
        return (str.endsWith(end) ? str.substring(0, str.length() - end.length()) : str);
    }

    @Override
    public String execute(Method method, String path, String body) {
        if (method == Method.GET) {
            return get(path, TO_STRING);
        } else if (method == Method.POST) {
            return post(path, body);
        } else if (method == Method.PUT) {
            return put(path, body);
        } else {
            throw new UnsupportedOperationException(Objects.toString(method));
        }
    }

    @Override
    public Issue getIssue(String key) {
        URI uri = URI.create(rootURL + "/browse/" + key);
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
        JSONObject response = searchCache.get(jql.trim());
        JSONArray issueList = response.getJSONArray("issues");
        List<Issue> issues = new ArrayList<Issue>();
        for (Object obj : issueList) {
            JSONObject issue = (JSONObject) obj;
            issues.add(getIssue(issue.getString("key")));
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
            String response = put("/rest/api/latest/issue/" + issue.getKey(), request.toString());
            if (response != null) {
                LOGGER.warn("Unexpected response received: {}", response);
            }
        }
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
        List<JSONObject> jsonList = boardCache.get();
        List<Board> boards = new ArrayList<Board>();
        for (JSONObject json : jsonList) {
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
    public List<Sprint> getSprints(Board board) {
        List<JSONObject> jsonList = sprintCache.get(board.getId());
        List<Sprint> sprints = new ArrayList<Sprint>();
        for (JSONObject json : jsonList) {
            int id = json.getInt("id");
            String name = json.getString("name");
            sprints.add(new Sprint(board, id, name));
        }
        return sprints;
    }

    @Override
    public List<Issue> getIssues(Sprint sprint) {
        JSONArray array = sprintIssuesCache.get(sprint.getId());
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

    private Map<String, JSONObject> loadFieldData() {
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();
        JSONArray array = get("/rest/api/latest/field", TO_ARRAY);
        for (Object obj : array) {
            JSONObject field = (JSONObject) obj;
            String id = field.getString("id");
            map.put(id, field);
        }
        return map;
    }

    private JSONObject loadAllFields(String issue) {
        JSONObject response = get("/rest/api/latest/issue/" + issue, TO_OBJECT);
        return response.getJSONObject("fields");
    }

    private JSONObject loadEditMeta(String issue) {
        JSONObject response = get("/rest/api/latest/issue/" + issue + "/editmeta", TO_OBJECT);
        return response.getJSONObject("fields");
    }

    private JSONObject loadIssueList(String jql) {
        String path = "/rest/api/latest/search?jql=" + urlEncode(jql) + "&fields=" + INITIAL_FIELDS;
        JSONObject response = get(path, TO_OBJECT);
        JSONArray issueList = response.getJSONArray("issues");
        for (Object issue : issueList) {
            JSONObject obj = (JSONObject) issue;
            String key = obj.getString("key");
            JSONObject fields = obj.getJSONObject("fields");
            IssueData issueData = new IssueData(key, fields);
            issueCache.putIfAbsent(key, issueData);
        }
        return response;
    }

    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    private List<JSONObject> loadBoards() {
        JSONObject response = get("/rest/agile/latest/board", TO_OBJECT);
        JSONArray boardArray = response.getJSONArray("values");
        List<JSONObject> result = new ArrayList<JSONObject>();
        for (Object obj : boardArray) {
            result.add((JSONObject) obj);
        }
        return result;
    }

    private List<JSONObject> loadSprints(Integer board) {
        JSONObject response = get("/rest/agile/latest/board/" + board + "/sprint", TO_OBJECT);
        JSONArray array = response.getJSONArray("values");
        List<JSONObject> result = new ArrayList<JSONObject>();
        for (Object obj : array) {
            result.add((JSONObject) obj);
        }
        return result;
    }

    private JSONArray loadSprintIssues(Integer sprint) {
        String path = "/rest/agile/latest/sprint/" + sprint + "/issue?fields=" + INITIAL_FIELDS;
        JSONObject response = get(path, TO_OBJECT);
        return response.getJSONArray("issues");
    }

    private <T> T get(String path, Function<Reader, T> function) {
        return execute(new HttpGet(getUrl(path)), function);
    }

    private String post(String path, String body) {
        HttpPost request = new HttpPost(getUrl(path));
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, TO_STRING);
    }

    private String put(String path, String body) {
        HttpPut request = new HttpPut(getUrl(path));
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, TO_STRING);
    }

    private String getUrl(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return rootURL + path;
    }

    private <T> T execute(HttpUriRequest request, Function<Reader, T> function) {
        LOGGER.debug("Calling URL: {} [{}]", request.getURI(), request.getMethod());

        HttpResponse response;
        try {
            response = httpClient.execute(request, httpClientContext);
        } catch (IOException e) {
            throw new IllegalStateException("Could not call URL: " + request.getURI());
        }

        LOGGER.debug("Response received ({})", response.getStatusLine().toString().trim());

        HttpEntity entity = response.getEntity();

        int statusCode = response.getStatusLine().getStatusCode();
        if (isSuccess(statusCode)) {
            if (entity == null) {
                return null;
            } else {
                return readResponse(request, entity, function);
            }
        } else {
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new IllegalStateException("Unauthorized!");
            } else {
                String message;
                String status = response.getStatusLine().toString().trim();
                if (entity == null) {
                    message = status;
                } else {
                    String error = readResponse(request, entity, TO_STRING);
                    message = status + (error.trim().isEmpty() ? "" : ": " + error);
                }
                if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    throw new NoSuchElementException(message);
                } else {
                    throw new IllegalStateException(message);
                }
            }
        }
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }

    private static <T> T readResponse(HttpUriRequest request, HttpEntity entity, Function<Reader, T> function) {
        try (InputStream input = entity.getContent()) {
            try (Reader reader = new InputStreamReader(input, getEncoding(entity))) {
                return function.apply(reader);
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not read response for URL: " + request.getURI(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read response for URL: " + request.getURI(), e);
        }
    }

    private static Charset getEncoding(HttpEntity entity) {
        if (entity.getContentEncoding() != null) {
            String value = entity.getContentEncoding().getValue();
            if (value != null) {
                try {
                    return Charset.forName(value);
                } catch (RuntimeException e) {
                    // use the default charset!
                    LOGGER.debug("Unsupported charset: {}", value, e);
                }
            }
        }
        return Charset.defaultCharset();
    }

    @Override
    public void close() {
        issueCache.clear();
        searchCache.clear();
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close HTTP client!", e);
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
                allFields = loadAllFields(issue);
            }
            return allFields;
        }

        public synchronized JSONObject getEditMeta() {
            if (editMeta == null) {
                editMeta = loadEditMeta(issue);
            }
            return editMeta;
        }
    }
}
