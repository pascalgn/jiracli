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

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.testutil.IssueFactory;
import com.github.pascalgn.jiracli.testutil.MockContext;

public class PrintTest {
    @Test
    public void test1() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1", "author", new JSONObject("{name:'Author-Name'}"));

        Print print = new Print("${author.name}");
        TextList textList = print.execute(context, new IssueList(issue1));

        List<Text> list = textList.remaining();
        assertEquals(1, list.size());
        assertEquals("Author-Name", list.get(0).getText());
    }
}
