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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.pascalgn.jiracli.ShellHelper;
import com.github.pascalgn.jiracli.context.Configuration;
import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.DefaultContext;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.DefaultWebService;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.testutil.MockConsole;

public class IntegrationTest {
    @Rule
    public TestServerRule testServerRule = new TestServerRule();

    @Test
    public void testSearch1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "search 'key=JRA-123' | print $summary");
            assertEquals("A simple Jira issue", getOutput(context));
        }
    }

    @Test
    public void testSearch2() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "search 'key=JRA-123' | print ${issuetype.name}");
            assertEquals("Change request", getOutput(context));
        }
    }

    @Test
    public void testJavaScript1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "get JRA-123 | "
                    + "js \"forEach.call(input, function(issue) { println(issue.fields.issuetype.name); })\"");
            assertEquals("Change request", getOutput(context));
        }
    }

    @Test
    public void testBrowse1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "get JRA-123 | browse -n");
            assertEquals(testServerRule.getRootUrl() + "/browse/JRA-123", getOutput(context));
        }
    }

    @Test
    public void testEdit1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "get JRA-123 | edit | p");
            assertEquals("JRA-123 - A simple Jira issue", getOutput(context));
        }
    }

    @Test
    public void testExecute1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "js \"webService.execute('GET', '/rest/api/latest/field', null);\"");
            String output = getOutput(context);
            assertFalse(output.isEmpty());
        }
    }

    private Context createContext() {
        Configuration configuration = Mockito.mock(Configuration.class);
        Console console = new MockConsole(testServerRule.getRootUrl());
        WebService webService = new DefaultWebService(console);
        JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console, webService);
        return new DefaultContext(configuration, console, webService, javaScriptEngine);
    }

    private static String getOutput(Context context) {
        return ((MockConsole) context.getConsole()).getOutput().trim();
    }
}
