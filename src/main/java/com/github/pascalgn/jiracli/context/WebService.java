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
package com.github.pascalgn.jiracli.context;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.pascalgn.jiracli.model.Attachment;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.util.Consumer;

public interface WebService extends AutoCloseable {
    enum Method {
        GET, POST, PUT, DELETE;
    }

    interface CreateRequest {
        Map<String, String> getFields();
    }

    String execute(Method method, String path, String body);

    void download(URI uri, Consumer<InputStream> consumer);

    Schema getSchema();

    Issue getIssue(String key);

    /**
     * @return The URL of this issue, for example <code>https://jira.example.com/browse/ISSUE-123</code>
     */
    URI getUrl(Issue issue);

    /**
     * @return All fields that may be edited by the user, never null
     */
    Collection<Field> getEditableFields(Issue issue);

    List<Issue> getIssues(Issue epic);

    List<Issue> getLinks(Issue issue);

    List<Issue> searchIssues(String jql);

    void updateIssue(Issue issue);

    void rankIssues(List<Issue> issues);

    List<Attachment> getAttachments(Issue issue);

    Project getProject(String key);

    List<Project> getProjects();

    List<Board> getBoards();

    List<Board> getBoards(String name);

    List<Issue> getIssues(Board board);

    List<Issue> getEpics(Board board);

    List<Sprint> getSprints(Board board);

    List<Issue> getIssues(Sprint sprint);

    List<Issue> createIssues(Collection<CreateRequest> createRequests);

    @Override
    void close();
}
