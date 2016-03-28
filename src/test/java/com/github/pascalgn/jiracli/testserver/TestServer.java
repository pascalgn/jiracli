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
package com.github.pascalgn.jiracli.testserver;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class TestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    private final Resources resources;

    public TestServer() throws IOException {
        resources = new Resources();
        new HttpServer();
    }

    private Response serve(IHTTPSession session) {
        String path = session.getUri();
        Map<String, String> parameters = session.getParms();
        LOGGER.info("Request received: {} {}", path, parameters);
        String resource = resources.getResource(path, parameters);
        if (resource == null) {
            resource = resources.getResource("404");
        }
        if (resource == null) {
            resource = "Not found.";
        }
        return NanoHTTPD.newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, resource);
    }

    private class HttpServer extends NanoHTTPD {
        private static final int HTTP_PORT = 8080;

        public HttpServer() throws IOException {
            super(HTTP_PORT);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            LOGGER.info("HTTP server running on localhost:{}", HTTP_PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            return TestServer.this.serve(session);
        }
    }

    public static void main(String[] args) throws IOException {
        new TestServer();
    }
}
