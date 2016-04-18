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

import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.ProjectList;

@CommandDescription(names = "projects", description = "List all projects")
class Projects implements Command {
    @Argument(names = { "-p", "--project" }, parameters = Parameters.ONE, variable = "<project>",
            description = "only show projects matching the given key")
    private String project;

    @Override
    public ProjectList execute(Context context, Data input) {
        if (project == null) {
            List<Project> projects = context.getWebService().getProjects();
            return new ProjectList(projects.iterator());
        } else {
            Project p = context.getWebService().getProject(project);
            return new ProjectList(p);
        }
    }
}
