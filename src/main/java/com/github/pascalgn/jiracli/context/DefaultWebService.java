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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.util.Cache;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.IOUtils;
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

    private final Supplier<Map<String, JSONObject>> fieldData;
    private final Cache<String, IssueData> issueCache;
    private final Cache<String, JSONObject> searchCache;

    public DefaultWebService(String rootURL, String username, char[] password) {
        this.rootURL = stripEnd(rootURL, "/");
        this.httpClient = createHttpClient();
        this.httpClientContext = createHttpClientContext(username, password);
        this.fieldData = new MemoizingSupplier<>(new Supplier<Map<String, JSONObject>>() {
            @Override
            public Map<String, JSONObject> get() {
                return loadFields();
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
    public String execute(String path) {
        return call(path, TO_STRING);
    }

    @Override
    public Issue getIssue(String key) {
        URI uri = URI.create(rootURL + "/browse/" + key);
        IssueData issueData = issueCache.get(key);
        LoadableFieldMap fieldMap = new LoadableFieldMap(issueData, fieldData);
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

    Map<String, JSONObject> getFields() {
        return fieldData.get();
    }

    private Map<String, JSONObject> loadFields() {
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();
        JSONArray array = call("/rest/api/latest/field", TO_ARRAY);
        for (Object obj : array) {
            JSONObject field = (JSONObject) obj;
            String id = field.getString("id");
            map.put(id, field);
        }
        return map;
    }

    private JSONObject loadAllFields(String issue) {
        JSONObject response = call("/rest/api/latest/issue/" + issue, TO_OBJECT);
        return response.getJSONObject("fields");
    }

    private JSONObject loadEditMeta(String issue) {
        JSONObject response = call("/rest/api/latest/issue/" + issue + "/editmeta", TO_OBJECT);
        return response.getJSONObject("fields");
    }

    private JSONObject loadIssueList(String jql) {
        String path = "/rest/api/latest/search?jql=" + encode(jql) + "&fields=" + INITIAL_FIELDS;
        JSONObject response = call(path, TO_OBJECT);
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

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    private <T> T call(String path, Function<Reader, T> function) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        HttpUriRequest request = new HttpGet(rootURL + path);
        LOGGER.debug("Fetching URL: {}", request.getURI());

        HttpResponse response;
        try {
            response = httpClient.execute(request, httpClientContext);
        } catch (IOException e) {
            throw new IllegalStateException("Could not fetch URL: " + request.getURI());
        }

        HttpEntity entity = response.getEntity();
        LOGGER.debug("Response received ({} bytes)", entity.getContentLength());

        if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new IllegalStateException("Unauthorized!");
        } else {
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
