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
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

public class DefaultWebService implements WebService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

    static {
        SSLContext sslcontext;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        Unirest.setHttpClient(httpclient);
    }

    private final String rootURL;
    private final String username;
    private final char[] password;

    private transient Map<String, JSONObject> issueCache;
    private transient Map<String, List<JSONObject>> searchCache;
    private transient Map<String, String> fieldMapping;

    public DefaultWebService(String rootURL, String username, char[] password) {
        this.rootURL = stripEnd(rootURL, "/");
        this.username = username;
        this.password = password;
    }

    private static String stripEnd(String str, String end) {
        return (str.endsWith(end) ? str.substring(0, str.length() - end.length()) : str);
    }

    @Override
    public URI getURI(String issue) {
        return URI.create(rootURL + "/browse/" + issue);
    }

    @Override
    public synchronized JSONObject getIssue(String issue) {
        JSONObject result;
        if (issueCache == null) {
            issueCache = new HashMap<String, JSONObject>();
            result = null;
        } else {
            result = issueCache.get(issue);
        }
        if (result == null) {
            result = new JSONObject(call("/rest/api/latest/issue/{issue}", "issue", issue));
            issueCache.put(issue, result);
        }
        return result;
    }

    @Override
    public synchronized List<JSONObject> searchIssues(String jql) {
        List<JSONObject> result;
        if (searchCache == null) {
            searchCache = new HashMap<String, List<JSONObject>>();
            result = null;
        } else {
            result = searchCache.get(jql.trim());
        }
        if (result == null) {
            JSONObject response = new JSONObject(call("/rest/api/latest/search?jql={jql}", "jql", jql));
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

            searchCache.put(jql.trim(), result);
        }
        return result;
    }

    @Override
    public synchronized Map<String, String> getFieldMapping() {
        if (fieldMapping == null) {
            fieldMapping = new HashMap<String, String>();
            JSONArray array = new JSONArray(call("/rest/api/latest/field"));
            for (Object obj : array) {
                JSONObject field = (JSONObject) obj;
                String id = field.getString("id");
                String name = field.getString("name");
                boolean custom = field.getBoolean("custom");
                if (custom) {
                    fieldMapping.put(name, id);
                }
            }
        }
        return fieldMapping;
    }

    private String call(String path, String... routeParams) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        if (routeParams.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid route parameters: " + Arrays.toString(routeParams));
        }
        GetRequest request = Unirest.get(rootURL + path);
        if (username != null && password != null) {
            request = request.basicAuth(username, new String(password));
        }
        for (int i = 0; i < routeParams.length; i += 2) {
            request = request.routeParam(routeParams[i], routeParams[i + 1]);
        }
        LOGGER.debug("Fetching URL: {}", request.getUrl());
        HttpResponse<String> response;
        try {
            response = request.asString();
        } catch (UnirestException e) {
            throw new IllegalStateException("Error fetching URL: " + request.getUrl(), e);
        }
        if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new IllegalStateException("Unauthorized!");
        } else {
            return response.getBody();
        }
    }

    @Override
    public void close() {
        issueCache = null;
        fieldMapping = null;
        try {
            Unirest.shutdown();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to shutdown!", e);
        }
    }
}
