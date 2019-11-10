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

import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.testutil.IssueFactory;
import com.github.pascalgn.jiracli.testutil.MockContext;
import com.github.pascalgn.jiracli.util.Hint;

public class FilterTest {
    @Test
    public void test1a() throws Exception {
        List<Issue> result = filterIssueList(new Filter("author.name", "Test1"));
        assertEquals(1, result.size());
        assertEquals("ISSUE-1", result.get(0).getKey());
    }

    @Test
    public void test1b() throws Exception {
        List<Issue> result = filterIssueList(new Filter("author.name", "test1"));
        assertEquals(0, result.size());
    }

    @Test
    public void test1c() throws Exception {
        List<Issue> result = filterIssueList(new Filter(false, true, "author.name", "test1"));
        assertEquals(1, result.size());
        assertEquals("ISSUE-1", result.get(0).getKey());
    }

    @Test
    public void test2a() throws Exception {
        List<Issue> result = filterIssueList(new Filter("author.name", "Test[0-9]"));
        assertEquals(0, result.size());
    }

    @Test
    public void test2b() throws Exception {
        List<Issue> result = filterIssueList(new Filter(true, false, "author.name", "Test[0-9]"));
        assertEquals(2, result.size());
        assertEquals("ISSUE-1", result.get(0).getKey());
        assertEquals("ISSUE-2", result.get(1).getKey());
    }

    private static List<Issue> filterIssueList(Filter filter) {
        Context context = new MockContext();

        Issue issue1 = IssueFactory.create("ISSUE-1", "author", new JSONObject("{name:'Test1'}"));
        Issue issue2 = IssueFactory.create("ISSUE-2", "author", new JSONObject("{name:'Test2'}"));
        IssueList issueList = new IssueList(issue1, issue2);

        Data result = filter.execute(context, issueList);

        return result.toIssueListOrFail().remaining(Hint.none());
    }
}
