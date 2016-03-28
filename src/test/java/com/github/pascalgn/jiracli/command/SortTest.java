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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.testutil.MockContext;

public class SortTest {
    @Test
    public void test1a() throws Exception {
        Sort sort = new Sort(Collections.<String> emptyList(), false, false, false);
        List<String> result = sort(sort, Arrays.asList("b", "a"));
        assertEquals(Arrays.asList("a", "b"), result);
    }

    @Test
    public void test1b() throws Exception {
        Sort sort = new Sort(Collections.<String> emptyList(), true, false, false);
        List<String> result = sort(sort, Arrays.asList("c", "1,2", "a", "b", "1.1", "1", "1x"));
        assertEquals(Arrays.asList("1", "1.1", "1,2", "1x", "a", "b", "c"), result);
    }

    @Test
    public void test1c() throws Exception {
        Sort sort = new Sort(Collections.<String> emptyList(), false, false, true);
        List<String> result = sort(sort, Arrays.asList("c", "c", "a", "b", "b", "b", "a"));
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void test1d() throws Exception {
        Sort sort = new Sort(Collections.<String> emptyList(), false, true, true);
        List<String> result = sort(sort, Arrays.asList("c", "c", "a", "b", "b", "b", "a"));
        assertEquals(Arrays.asList("c", "b", "a"), result);
    }

    private static List<String> sort(Sort sort, List<String> input) {
        List<Text> textList = new ArrayList<Text>();
        for (String str : input) {
            textList.add(new Text(str));
        }

        Context context = new MockContext();
        TextList result = (TextList) sort.execute(context, new TextList(textList.iterator()));

        List<String> resultList = new ArrayList<String>();
        for (Text text : result.remaining()) {
            resultList.add(text.getText());
        }

        return resultList;
    }

    @Test
    public void test2() throws Exception {
        MockContext context = new MockContext();

        Issue issue1 = Issue.valueOf("ISSUE-1");
        Issue issue2 = Issue.valueOf("ISSUE-2");
        context.getWebService().setIssue(issue1.getKey(), new JSONObject("{fields:{author:{name:'Test1'}}}"));
        context.getWebService().setIssue(issue2.getKey(), new JSONObject("{fields:{author:{name:'Test2'}}}"));

        Filter filter = new Filter("author.name", "Test1");

        assertTrue(filter.matches(context, issue1));
    }
}
