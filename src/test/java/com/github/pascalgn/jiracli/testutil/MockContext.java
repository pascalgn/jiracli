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
package com.github.pascalgn.jiracli.testutil;

import com.github.pascalgn.jiracli.context.AbstractContext;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;

public class MockContext extends AbstractContext {
    private MockConsole console;
    private MockWebService webService;
    private JavaScriptEngine javaScriptEngine;

    public MockContext() {
        console = new MockConsole();
        webService = new MockWebService();
        javaScriptEngine = new DefaultJavaScriptEngine(console);
    }

    @Override
    public MockConsole getConsole() {
        return console;
    }

    @Override
    public MockWebService getWebService() {
        return webService;
    }

    @Override
    public JavaScriptEngine getJavaScriptEngine() {
        return javaScriptEngine;
    }
}
