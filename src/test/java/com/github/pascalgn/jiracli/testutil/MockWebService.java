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

import java.util.Collections;
import java.util.List;

import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Sprint;

public class MockWebService implements WebService {
    @Override
    public String execute(String path) {
        return "";
    }

    @Override
    public Issue getIssue(String key) {
        return IssueFactory.create(key);
    }

    @Override
    public List<Issue> getEpicIssues(Issue epic) {
        return Collections.emptyList();
    }

    @Override
    public List<Issue> searchIssues(String jql) {
        return Collections.emptyList();
    }

    @Override
    public void updateIssue(Issue issue) {
    }

    @Override
    public List<Board> getBoards() {
        return Collections.emptyList();
    }

    @Override
    public List<Sprint> getSprints(Board board) {
        return Collections.emptyList();
    }

    @Override
    public List<Issue> getIssues(Sprint sprint) {
        return Collections.emptyList();
    }

    @Override
    public void close() {
    }
}
