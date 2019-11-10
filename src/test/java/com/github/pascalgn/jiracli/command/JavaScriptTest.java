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
package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.testutil.IssueFactory;
import com.github.pascalgn.jiracli.testutil.MockContext;
import com.github.pascalgn.jiracli.util.Hint;

public class JavaScriptTest {
    @Test
    public void test1() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1");
        Issue issue2 = IssueFactory.create("ISSUE-2");

        JavaScript javaScript = new JavaScript("input.key", false);
        Data result = javaScript.execute(context, new IssueList(issue1, issue2));

        assertNotNull(result);
        assertNotNull(result.toTextList());
        List<Text> resultList = result.toTextList().remaining(Hint.none());
        assertEquals(Arrays.asList(new Text(issue1.getKey()), new Text(issue2.getKey())), resultList);
    }

    @Test
    public void test2() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1", "author", new JSONObject("{name:'Author-Name'}"));

        String js = "forEach.call(input, function(issue) { print(issue.key + ': ' + issue.fields.author.name); });";
        JavaScript javaScript = new JavaScript(js, true);
        Data result = javaScript.execute(context, new IssueList(issue1));

        assertNotNull(result);
        assertEquals("ISSUE-1: Author-Name", context.getConsole().getOutput().trim());
    }

    @Test
    public void test3() throws Exception {
        MockContext context = new MockContext();
        String js = "if (input == '123') { input += '456'; } println('Hello'); input;";
        JavaScript javaScript = new JavaScript(js, true);
        assertEquals("123456", javaScript.execute(context, new Text("123")).toTextOrFail().getText());
    }
}
