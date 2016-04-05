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
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.NoSuchElementException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.IOUtils;

class HttpClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private static final Function<Reader, String> TO_STRING;
    private static final SSLConnectionSocketFactory SSL_SOCKET_FACTORY;

    static {
        TO_STRING = new Function<Reader, String>() {
            @Override
            public String apply(Reader reader) {
                return IOUtils.toString(reader);
            }
        };

        SSLContext sslcontext;
        try {
            sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        SSL_SOCKET_FACTORY = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
    }

    private final String baseUrl;
    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpClientContext;

    public HttpClient(String baseUrl, String username, char[] password) {
        this.baseUrl = stripEnd(baseUrl, "/");
        this.httpClient = createHttpClient();
        this.httpClientContext = createHttpClientContext(username, password);
    }

    private static String stripEnd(String str, String end) {
        return (str.endsWith(end) ? str.substring(0, str.length() - end.length()) : str);
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public String get(String path) {
        return get(path, TO_STRING);
    }

    public <T> T get(String path, Function<Reader, T> function) {
        return execute(new HttpGet(getUrl(path)), function);
    }

    public String post(String path, String body) {
        return post(path, body, TO_STRING);
    }

    public <T> T post(String path, String body, Function<Reader, T> function) {
        HttpPost request = new HttpPost(getUrl(path));
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, function);
    }

    public String put(String path, String body) {
        return put(path, body, TO_STRING);
    }

    public <T> T put(String path, String body, Function<Reader, T> function) {
        HttpPut request = new HttpPut(getUrl(path));
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, function);
    }

    private String getUrl(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return baseUrl + path;
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
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close HTTP client!", e);
        }
    }
}
