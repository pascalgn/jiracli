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
package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.DefaultContext;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.testutil.MockConsole;
import com.github.pascalgn.jiracli.testutil.MockWebService;

public class JavaScriptTest {
    @Test
    public void test1() throws Exception {
        MockConsole console = new MockConsole();
        MockWebService webService = new MockWebService();
        JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console);
        Context context = new DefaultContext(console, webService, javaScriptEngine);

        Issue issue1 = Issue.valueOf("ISSUE-1");

        webService.setIssue(issue1.getKey(), new JSONObject("{key:'ISSUE-1',fields:{author:{name:'Author-Name'}}}"));

        JavaScript javaScript = new JavaScript("forEach.call(input, function(issue) { print(issue.key);"
                + " print(': '); print(issue.fields.author.name); });");
        javaScript.execute(context, new IssueList(issue1));

        assertEquals("ISSUE-1: Author-Name", console.getOutput().trim());
    }
}
