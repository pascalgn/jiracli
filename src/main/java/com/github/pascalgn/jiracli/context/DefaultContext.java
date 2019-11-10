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
package com.github.pascalgn.jiracli.context;

public class DefaultContext extends AbstractContext {
    private final Configuration configuration;
    private final Console console;
    private final WebService webService;
    private final JavaScriptEngine javaScriptEngine;

    public DefaultContext(Configuration configuration, Console console, WebService webService,
            JavaScriptEngine javaScriptEngine) {
        this.configuration = configuration;
        this.console = console;
        this.webService = webService;
        this.javaScriptEngine = javaScriptEngine;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public Console getConsole() {
        return console;
    }

    @Override
    public WebService getWebService() {
        return webService;
    }

    @Override
    public JavaScriptEngine getJavaScriptEngine() {
        return javaScriptEngine;
    }
}
