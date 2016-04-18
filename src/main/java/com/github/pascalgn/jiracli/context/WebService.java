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
import com.github.pascalgn.jiracli.model.Change;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldDescription;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueType;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.Status;
import com.github.pascalgn.jiracli.model.Transition;
import com.github.pascalgn.jiracli.model.User;
import com.github.pascalgn.jiracli.model.Workflow;
import com.github.pascalgn.jiracli.util.Consumer;

public interface WebService extends AutoCloseable {
    enum Method {
        GET, POST, PUT, DELETE;
    }

    interface CreateRequest {
        Map<String, String> getFields();
    }

    interface Cache {
        void clear();
    }

    /**
     * Returns the current user, triggering an authentication request if the
     * user is not logged in yet
     */
    User authenticate();

    String execute(Method method, URI uri, String body);

    void download(URI uri, Consumer<InputStream> consumer);

    Schema getSchema();

    /**
     * Resolves the issues for the given keys, throws an exception if one or
     * more issues cannot be found.
     */
    List<Issue> getIssues(List<String> keys, Collection<String> fields);

    /**
     * @return The URL of this issue, for example <code>https://jira.example.com/browse/ISSUE-123</code>
     */
    URI getUrl(Issue issue);

    /**
     * @return All fields that may be edited by the user, never null
     */
    Collection<Field> getEditableFields(Issue issue);

    List<Issue> getIssues(Issue epic, Collection<String> fields);

    /**
     * Returns all issues that are linked to the given issue
     */
    List<Issue> getLinks(Issue issue, Collection<String> fields);

    List<Issue> searchIssues(String jql, Collection<String> fields);

    Workflow getWorkflow(Issue issue);

    List<Attachment> getAttachments(Issue issue);

    Status getStatus(Issue issue);

    List<Change> getChanges(Issue issue);

    void updateIssue(Issue issue, boolean notifyUsers);

    void transitionIssue(Issue issue, Transition transition);

    void rankIssues(List<Issue> issues);

    /**
     * Links the first issue to the second issue
     *
     * @param inward The inward issue
     * @param outward The outward issue
     * @param name The link type name
     */
    void linkIssues(Issue inward, Issue outward, String name);

    /**
     * Removes the link between the first and the second issue
     */
    void removeLink(Issue inward, Issue outward, String name);

    Status getStatus(String name);

    Project getProject(String key);

    List<Project> getProjects();

    List<FieldDescription> getFields(Project project, IssueType issueType);

    Board getBoard(int id);

    List<Board> getBoards();

    List<Board> getBoards(String name);

    List<Issue> getIssues(Board board, Collection<String> fields);

    List<Issue> getEpics(Board board);

    Sprint getSprint(int id);

    List<Sprint> getSprints(Board board);

    List<Issue> getIssues(Sprint sprint, Collection<String> fields);

    List<Issue> createIssues(Collection<CreateRequest> createRequests);

    Cache getCache();

    @Override
    void close();
}
