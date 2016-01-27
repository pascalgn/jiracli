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

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.testutil.MockConsole;
import com.github.pascalgn.jiracli.testutil.MockWebService;

public class PrintTest {
    @Test
    public void test1() throws Exception {
        MockConsole console = new MockConsole();
        MockWebService webService = new MockWebService();
        Context context = new DefaultContext(console, webService);

        Issue issue1 = Issue.valueOf("ISSUE-1");
        webService.setIssue(issue1.getKey(), new JSONObject("{fields:{author:{name:'Author-Name'}}}"));

        Print print = new Print("${author.name}");
        print.execute(context, new IssueList(issue1));

        assertEquals("Author-Name", console.getOutput().trim());
    }
}
