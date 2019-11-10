/*
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.AccessControlException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.Credentials;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.InterruptedError;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;

class HttpClient implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    private static final int MAX_ERROR_LENGTH = 500;

    private static final Function<Reader, String> TO_STRING;
    private static final SSLConnectionSocketFactory SSL_SOCKET_FACTORY;

    static {
        TO_STRING = new Function<Reader, String>() {
            @Override
            public String apply(Reader reader, Set<Hint> hints) {
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

    private final Map<String, Credentials> credentials;
    private final AtomicReference<HttpUriRequest> request;
    private final Supplier<String> baseUrl;
    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpClientContext;

    public HttpClient(final Console console) {
        this.credentials = new HashMap<>();
        this.request = new AtomicReference<>();

        this.baseUrl = new Supplier<String>() {
            @Override
            public String get(Set<Hint> hints) {
                return console.getBaseUrl();
            }
        };

        this.httpClient = createHttpClient();
        this.httpClientContext = createHttpClientContext(console);

        console.onInterrupt(new Runnable() {
            @Override
            public void run() {
                HttpUriRequest req = request.get();
                if (req != null) {
                    req.abort();
                }
            }
        });
    }

    private static CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(SSL_SOCKET_FACTORY);
        return httpClientBuilder.build();
    }

    private HttpClientContext createHttpClientContext(final Console console) {
        HttpClientContext context = HttpClientContext.create();

        CredentialsProvider credentialsProvider = new CredentialsProvider() {
            @Override
            public void setCredentials(AuthScope authscope, org.apache.http.auth.Credentials credentials) {
            }

            @Override
            public org.apache.http.auth.Credentials getCredentials(AuthScope authscope) {
                String baseUrl = getBaseUrl();
                Credentials c = HttpClient.this.credentials.get(baseUrl);
                if (c == null) {
                    c = console.getCredentials(authscope.getOrigin().toURI());
                    if (c == null) {
                        throw new IllegalStateException("No credentials provided!");
                    }
                    HttpClient.this.credentials.put(baseUrl, c);
                }
                if (c == Credentials.getAnonymous()) {
                    return null;
                } else {
                    return new UsernamePasswordCredentials(c.getUsername(), new String(c.getPassword()));
                }
            }

            @Override
            public void clear() {
            }
        };
        context.setCredentialsProvider(credentialsProvider);
        return context;
    }

    public String getBaseUrl() {
        String url = baseUrl.get(Hint.none());
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("No base URL provided!");
        }
        if (url.endsWith("/") || !url.equals(url.trim())) {
            throw new IllegalStateException("Invalid base URL: " + url);
        }
        return url;
    }

    public String get(URI uri) {
        return execute(new HttpGet(uri), TO_STRING);
    }

    public String get(String path) {
        return get(path, TO_STRING);
    }

    public <T> T get(String path, Function<Reader, T> function) {
        return execute(new HttpGet(getUrl(path)), function);
    }

    public void get(final URI uri, final Consumer<InputStream> consumer) {
        execute(new HttpGet(uri), true, new Function<HttpEntity, Void>() {
            @Override
            public Void apply(HttpEntity entity, Set<Hint> hints) {
                if (entity == null) {
                    throw new IllegalStateException("No response!");
                } else {
                    try (InputStream input = entity.getContent()) {
                        consumer.accept(input);
                    } catch (IOException e) {
                        throw new IllegalStateException("Could not read response for URL: " + uri, e);
                    }
                }
                return null;
            }
        });
    }

    public String post(URI uri, String body) {
        return post(uri, body, TO_STRING);
    }

    public String post(String path, String body) {
        return post(path, body, TO_STRING);
    }

    public <T> T post(String path, String body, Function<Reader, T> function) {
        return post(getUrl(path), body, function);
    }

    public <T> T post(URI uri, String body, Function<Reader, T> function) {
        HttpPost request = new HttpPost(uri);
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, function);
    }

    public String put(URI uri, String body) {
        return put(uri, body, TO_STRING);
    }

    public String put(String path, String body) {
        return put(path, body, TO_STRING);
    }

    public <T> T put(String path, String body, Function<Reader, T> function) {
        return put(getUrl(path), body, function);
    }

    public <T> T put(URI uri, String body, Function<Reader, T> function) {
        HttpPut request = new HttpPut(uri);
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        return execute(request, function);
    }

    public String delete(String path) {
        return delete(path, TO_STRING);
    }

    public <T> T delete(String path, Function<Reader, T> function) {
        HttpDelete request = new HttpDelete(getUrl(path));
        return execute(request, function);
    }

    private URI getUrl(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return URI.create(getBaseUrl() + path);
    }

    private <T> T execute(final HttpUriRequest request, final Function<Reader, T> function) {
        return execute(request, true, new Function<HttpEntity, T>() {
            @Override
            public T apply(HttpEntity entity, Set<Hint> hints) {
                return (entity == null ? null : readResponse(request.getURI(), entity, function));
            }
        });
    }

    private <T> T execute(HttpUriRequest request, boolean retry, Function<HttpEntity, T> function) {
        this.request.set(request);
        try {
            return doExecute(request, retry, function);
        } finally {
            this.request.set(null);
        }
    }

    private <T> T doExecute(HttpUriRequest request, boolean retry, Function<HttpEntity, T> function) {
        LOGGER.debug("Calling URL: {} [{}]", request.getURI(), request.getMethod());

        // disable XSRF check:
        if (!request.containsHeader("X-Atlassian-Token")) {
            request.addHeader("X-Atlassian-Token", "nocheck");
        }

        HttpResponse response;
        try {
            response = httpClient.execute(request, httpClientContext);
        } catch (IOException e) {
            if (Thread.interrupted()) {
                LOGGER.trace("Could not call URL: {}", request.getURI(), e);
                throw new InterruptedError();
            } else {
                throw new IllegalStateException("Could not call URL: " + request.getURI(), e);
            }
        }

        LOGGER.debug("Response received ({})", response.getStatusLine().toString().trim());

        HttpEntity entity = response.getEntity();
        try {
            if (Thread.interrupted()) {
                throw new InterruptedError();
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (isSuccess(statusCode)) {
                T result;
                try {
                    result = function.apply(entity, Hint.none());
                } catch (NotAuthenticatedException e) {
                    if (retry) {
                        resetAuthentication();
                        setCredentials();
                        return doExecute(request, false, function);
                    } else {
                        throw e.getCause();
                    }
                } catch (RuntimeException e) {
                    if (Thread.interrupted()) {
                        LOGGER.trace("Could not call URL: {}", request.getURI(), e);
                        throw new InterruptedError();
                    } else {
                        throw e;
                    }
                }

                if (Thread.interrupted()) {
                    throw new InterruptedError();
                }

                return result;
            } else {
                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    resetAuthentication();
                    if (retry) {
                        setCredentials();
                        return doExecute(request, false, function);
                    } else {
                        String error = readErrorResponse(request.getURI(), entity);
                        LOGGER.debug("Unauthorized [401]: {}", error);
                        throw new AccessControlException("Unauthorized [401]: " + request.getURI());
                    }
                } else if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    resetAuthentication();
                    checkAccountLocked(response);
                    if (retry) {
                        setCredentials();
                        return doExecute(request, false, function);
                    } else {
                        throw new AccessControlException("Forbidden [403]: " + request.getURI());
                    }
                } else {
                    String status = response.getStatusLine().toString().trim();
                    String message;
                    if (entity == null) {
                        message = status;
                    } else {
                        String error = readErrorResponse(request.getURI(), entity);
                        message = status + (error.isEmpty() ? "" : ": " + error);
                    }

                    if (Thread.interrupted()) {
                        throw new InterruptedError();
                    }

                    if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        throw new NoSuchElementException(message);
                    } else {
                        throw new IllegalStateException(message);
                    }
                }
            }
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode <= 299;
    }

    private static <T> T readResponse(URI uri, HttpEntity entity, Function<Reader, T> function) {
        try (InputStream input = entity.getContent()) {
            try (Reader reader = new InputStreamReader(input, getEncoding(entity))) {
                return function.apply(reader, Hint.none());
            }
        } catch (NotAuthenticatedException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Could not read response for URL: " + uri, e);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read response for URL: " + uri, e);
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
        return StandardCharsets.UTF_8;
    }

    private void resetAuthentication() {
        String url = getBaseUrl();
        credentials.remove(url);
        AuthState authState = httpClientContext.getTargetAuthState();
        if (authState != null) {
            authState.reset();
        }
    }

    private void setCredentials() {
        AuthState authState = httpClientContext.getTargetAuthState();
        if (authState != null) {
            CredentialsProvider credentialsProvider = httpClientContext.getCredentialsProvider();
            AuthScope authScope = new AuthScope(HttpHost.create(getBaseUrl()));
            org.apache.http.auth.Credentials credentials = credentialsProvider.getCredentials(authScope);
            if (credentials != null) {
                authState.update(new BasicScheme(), credentials);
                authState.setState(AuthProtocolState.CHALLENGED);
            }
        }
    }

    private static void checkAccountLocked(HttpResponse response) {
        Header header = response.getLastHeader("X-Authentication-Denied-Reason");
        if (header != null) {
            String info = Objects.toString(header.getValue(), "").trim();
            throw new AccessControlException("Your account seems to be locked" + (info.isEmpty() ? "" : ": " + info));
        }
    }

    private static String readErrorResponse(URI uri, HttpEntity entity) {
        String error;
        try (InputStream input = entity.getContent()) {
            try (Reader reader = new InputStreamReader(maybeDecompress(input), getEncoding(entity))) {
                error = IOUtils.toString(reader, MAX_ERROR_LENGTH + 1);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        error = StringUtils.shorten(error, MAX_ERROR_LENGTH);
        return error.trim();
    }

    private static InputStream maybeDecompress(InputStream input) throws IOException {
        // Due to a bug, Jira sometimes returns double-compressed responses. See JRA-37608
        BufferedInputStream buffered = new BufferedInputStream(input, 2);
        buffered.mark(2);
        int[] buf = new int[2];
        buf[0] = buffered.read();
        buf[1] = buffered.read();
        buffered.reset();
        int header = (buf[1] << 8) | buf[0];
        if (header == GZIPInputStream.GZIP_MAGIC) {
            return new GZIPInputStream(buffered);
        } else {
            return buffered;
        }
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            LOGGER.warn("Failed to close HTTP client!", e);
        }
    }

    /**
     * Can be thrown to indicate that the error might have been caused by an invalid/insufficient authentication
     */
    public static class NotAuthenticatedException extends RuntimeException {
        private static final long serialVersionUID = 6132505794247992826L;

        public NotAuthenticatedException(RuntimeException cause) {
            super(cause);
        }

        @Override
        public synchronized RuntimeException getCause() {
            return (RuntimeException) super.getCause();
        }
    }
}
