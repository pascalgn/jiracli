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

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.pascalgn.jiracli.context.AbstractContext;
import com.github.pascalgn.jiracli.context.Configuration;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.context.WebService.Request;
import com.github.pascalgn.jiracli.model.Issue;

public class MockContext extends AbstractContext {
    private MockConsole console;
    private WebService webService;
    private JavaScriptEngine javaScriptEngine;

    public MockContext() {
        console = new MockConsole("http://localhost");
        webService = Mockito.mock(WebService.class);
        Mockito.when(webService.getIssues(Mockito.anyListOf(String.class), Mockito.any(Request.class)))
                .thenAnswer(new Answer<List<Issue>>() {
                    @Override
                    public List<Issue> answer(InvocationOnMock invocation) throws Throwable {
                        @SuppressWarnings("unchecked")
                        List<String> keys = invocation.getArgumentAt(0, List.class);
                        List<Issue> result = new ArrayList<Issue>();
                        for (String key : keys) {
                            result.add(IssueFactory.create(key));
                        }
                        return result;
                    }
                });
        javaScriptEngine = new DefaultJavaScriptEngine(console, webService);
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public MockConsole getConsole() {
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
