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
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.testutil.MockConsole;

public class IntegrationTest {
    @Rule
    public TestServerRule testServerRule = new TestServerRule();

    @Test
    public void testSearch1() throws Exception {
        try (Context context = createContext()) {
            Data data = ShellHelper.execute(context, "search 'key=JRA-123' | print $summary");
            assertEquals("A simple Jira issue", toString(data.toTextListOrFail()));
        }
    }

    @Test
    public void testSearch2() throws Exception {
        try (Context context = createContext()) {
            Data data = ShellHelper.execute(context, "search 'key=JRA-123' | print ${issuetype.name}");
            assertEquals("Change request", toString(data.toTextListOrFail()));
        }
    }

    @Test
    public void testJavaScript1() throws Exception {
        try (Context context = createContext()) {
            ShellHelper.execute(context, "get JRA-123 | "
                    + "js \"forEach.call(input, function(issue) { println(issue.fields.issuetype.name); })\"");
            MockConsole console = (MockConsole) context.getConsole();
            assertEquals("Change request", console.getOutput().trim());
        }
    }

    @Test
    public void testBrowse1() throws Exception {
        try (Context context = createContext()) {
            Data data = ShellHelper.execute(context, "get JRA-123 | browse -n");
            assertEquals(testServerRule.getRootUrl() + "/browse/JRA-123", toString(data.toTextListOrFail()));
        }
    }

    private Context createContext() {
        Configuration configuration = Mockito.mock(Configuration.class);
        Console console = new MockConsole(testServerRule.getRootUrl());
        WebService webService = new DefaultWebService(console);
        JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console);
        return new DefaultContext(configuration, console, webService, javaScriptEngine);
    }

    private static String toString(TextList textList) {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        Text text;
        while ((text = textList.next()) != null) {
            if (first) {
                first = false;
            } else {
                str.append(", ");
            }
            str.append(text);
        }
        return str.toString();
    }
}
