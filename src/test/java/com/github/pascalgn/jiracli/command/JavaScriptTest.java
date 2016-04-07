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
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.testutil.IssueFactory;
import com.github.pascalgn.jiracli.testutil.MockContext;

public class JavaScriptTest {
    @Test
    public void test1() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1");
        Issue issue2 = IssueFactory.create("ISSUE-2");

        JavaScript javaScript = new JavaScript(true, "input");
        Data result = javaScript.execute(context, new IssueList(issue1, issue2));

        assertNotNull(result);
        assertEquals(Arrays.asList(issue1, issue2), result.toIssueList().remaining());
    }

    @Test
    public void test2() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1", "author", new JSONObject("{name:'Author-Name'}"));

        String js = "forEach.call(input, function(issue) { print(issue.key + ': ' + issue.fields.author.name); });";
        JavaScript javaScript = new JavaScript(true, js);
        Data result = javaScript.execute(context, new IssueList(issue1));

        assertNotNull(result);
        assertEquals("", context.getConsole().getOutput().trim());

        // Lazy evaluation!
        result.toTextList().toText();

        assertEquals("ISSUE-1: Author-Name", context.getConsole().getOutput().trim());
    }

    @Test
    public void test3() throws Exception {
        MockContext context = new MockContext();
        JavaScript javaScript = new JavaScript("if (input == '123') { input += '456'; } println('Hello'); input;");
        assertEquals("123456", javaScript.execute(context, new Text("123")).toTextOrFail().getText());
    }
}
