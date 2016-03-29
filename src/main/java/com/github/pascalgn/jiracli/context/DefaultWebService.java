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
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import com.github.pascalgn.jiracli.util.Function;

public class DefaultWebService implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

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

    private transient Map<String, JSONObject> issueCache;
    private transient Map<String, List<JSONObject>> issueListCache;
    private transient Map<String, String> fieldMapping;

    public DefaultWebService(String rootURL, String username, char[] password) {
        this.rootURL = stripEnd(rootURL, "/");
        this.httpClient = createHttpClient(username, password);
    }

    private static CloseableHttpClient createHttpClient(String username, char[] password) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(SSL_SOCKET_FACTORY);
        if (username != null && password != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, new String(password));
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        return httpClientBuilder.build();
    }

    private static String stripEnd(String str, String end) {
        return (str.endsWith(end) ? str.substring(0, str.length() - end.length()) : str);
    }

    @Override
    public Issue getIssue(String key) {
        URI uri = URI.create(rootURL + "/browse/" + key);
        LoadableFieldMap fieldMap = new LoadableFieldMap(this);
        Issue issue = new Issue(key, uri, fieldMap);
        fieldMap.setIssue(issue);
        return issue;
    }

    synchronized JSONObject getJson(String issue) {
        JSONObject result;
        if (issueCache == null) {
            issueCache = new HashMap<String, JSONObject>();
            result = null;
        } else {
            result = issueCache.get(issue);
        }
        if (result == null) {
            result = call("/rest/api/latest/issue/" + issue, TO_OBJECT);
            issueCache.put(issue, result);
        }
        return result;
    }

    @Override
    public List<Issue> getEpicIssues(Issue epic) {
        getIssueList("/rest/agile/latest/epic/" + epic + "/issue");
        return null;
    }

    @Override
    public List<Issue> searchIssues(String jql) {
        getIssueList("/rest/api/latest/search?jql=" + encode(jql.trim()));
        return null;
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    private synchronized List<JSONObject> getIssueList(String path) {
        List<JSONObject> result;
        if (issueListCache == null) {
            issueListCache = new HashMap<String, List<JSONObject>>();
            result = null;
        } else {
            result = issueListCache.get(path);
        }
        if (result == null) {
            JSONObject response = call(path, TO_OBJECT);
            JSONArray issues = response.getJSONArray("issues");

            if (issueCache == null) {
                issueCache = new HashMap<String, JSONObject>();
            }

            result = new ArrayList<JSONObject>();
            for (Object issue : issues) {
                JSONObject obj = (JSONObject) issue;
                String key = obj.getString("key");
                JSONObject cached = issueCache.get(key);
                if (cached == null) {
                    issueCache.put(key, obj);
                    result.add(obj);
                } else {
                    result.add(cached);
                }
            }

            issueListCache.put(path, result);
        } else {
            result = new ArrayList<JSONObject>(result);
        }
        return result;
    }

    synchronized Map<String, String> getFieldMapping() {
        if (fieldMapping == null) {
            fieldMapping = new HashMap<String, String>();
            JSONArray array = call("/rest/api/latest/field", TO_ARRAY);
            for (Object obj : array) {
                JSONObject field = (JSONObject) obj;
                String id = field.getString("id");
                String name = field.getString("name");
                fieldMapping.put(id, name);
            }
        }
        return fieldMapping;
    }

    private <T> T call(String path, Function<Reader, T> function) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }

        HttpUriRequest request = new HttpGet(rootURL + path);
        LOGGER.debug("Fetching URL: {}", request.getURI());

        HttpResponse response;
        try {
            response = httpClient.execute(request);
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
            } catch (IOException e) {
                throw new IllegalStateException("Could not read response for URL: " + request.getURI());
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
        issueCache = null;
        fieldMapping = null;
        try {
            httpClient.close();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to close HTTP client!", e);
        }
    }
}
