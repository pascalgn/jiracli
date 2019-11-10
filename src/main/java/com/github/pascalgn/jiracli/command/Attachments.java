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

import java.util.Collection;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Attachment;
import com.github.pascalgn.jiracli.model.AttachmentList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "attachments", description = "List all attachments for the given issues")
class Attachments implements Command {
    @Override
    public AttachmentList execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        Set<Hint> hints = IssueHint.fields("attachment");
        return new AttachmentList(issueList.loadingSupplier(hints, new Function<Issue, Collection<Attachment>>() {
            @Override
            public Collection<Attachment> apply(Issue issue, Set<Hint> hints) {
                return context.getWebService().getAttachments(issue);
            }
        }));
    }
}
