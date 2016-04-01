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
package com.github.pascalgn.jiracli.it;

import java.io.IOException;

import org.junit.rules.ExternalResource;

import com.github.pascalgn.jiracli.testserver.TestServer;

public class TestServerRule extends ExternalResource {
    private static final int MIN_PORT = 8080;
    private static final int MAX_PORT = 8180;

    private TestServer testServer;

    public String getRootUrl() {
        return "http://localhost:" + testServer.getPort();
    }

    @Override
    protected void before() {
        for (int port = MIN_PORT; port <= MAX_PORT; port++) {
            try {
                testServer = new TestServer(port);
                return;
            } catch (IOException e) {
                continue;
            }
        }
        throw new IllegalStateException("No free port between " + MIN_PORT + " and " + MAX_PORT);
    }

    @Override
    protected void after() {
        testServer.stop();
    }
}
