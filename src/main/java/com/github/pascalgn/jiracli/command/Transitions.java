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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Status;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Transition;
import com.github.pascalgn.jiracli.model.Workflow;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "transition", description = "Change the status of the given issues")
class Transitions implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transitions.class);

    private static final List<String> FIELDS = Collections.emptyList();

    @Argument(names = { "-n", "--dry" }, description = "only print the transition order")
    private boolean dry;

    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<status>", description = "The target status")
    private String status;

    @Override
    public Data execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        if (status == null) {
            return new TextList(issueList.loadingSupplier(Hint.none(), new Function<Issue, Collection<Text>>() {
                @Override
                public Collection<Text> apply(Issue issue, Set<Hint> hints) {
                    return listAllTransitions(context, issue);
                }
            }));
        } else {
            if (dry) {
                return new TextList(issueList.convertingSupplier(new Function<Issue, Text>() {
                    @Override
                    public Text apply(Issue issue, Set<Hint> hints) {
                        return listTransitions(context, issue);
                    }
                }));
            } else {
                return new IssueList(issueList.convertingSupplier(new Function<Issue, Issue>() {
                    @Override
                    public Issue apply(Issue issue, Set<Hint> hints) {
                        return doTransition(context, issue);
                    }
                }));
            }
        }
    }

    private static Collection<Text> listAllTransitions(Context context, Issue issue) {
        Workflow workflow = context.getWebService().getWorkflow(issue);
        Status status = context.getWebService().getStatus(issue);

        Collection<Text> texts = new ArrayList<Text>();
        texts.add(new Text(issue.getKey() + ": " + (status == null ? "(unknown)" : status.getName())));
        for (Transition transition : workflow.getTransitions()) {
            Status source = transition.getSource().getStatus();
            Status target = transition.getTarget().getStatus();
            StringBuilder str = new StringBuilder("    ");
            str.append(transition.getName());
            str.append(" (");
            if (transition.isGlobal()) {
                str.append("* ");
            } else if (source != null) {
                str.append(source.getName());
                str.append(" ");
            }
            str.append("-> ");
            str.append(target.getName());
            str.append(")");
            texts.add(new Text(str.toString()));
        }

        return texts;
    }

    private Text listTransitions(Context context, Issue issue) {
        List<Transition> transitions = getTransitions(context, issue);
        Status source = context.getWebService().getStatus(issue);
        StringBuilder str = new StringBuilder(source.getName());
        for (Transition transition : transitions) {
            str.append(" -> ");
            str.append(transition.getTarget().getStatus().getName());
        }
        return new Text(str.toString());
    }

    private Issue doTransition(Context context, Issue issue) {
        List<Transition> transitions = getTransitions(context, issue);
        for (Transition transition : transitions) {
            context.getWebService().transitionIssue(issue, transition);
        }
        List<Issue> issues = context.getWebService().getIssues(Collections.singletonList(issue.getKey()), FIELDS);
        return issues.get(0);
    }

    private List<Transition> getTransitions(Context context, Issue issue) {
        Status source = context.getWebService().getStatus(issue);
        Status target = context.getWebService().getStatus(this.status);
        Workflow workflow = context.getWebService().getWorkflow(issue);
        return getTransitions(workflow, source, target);
    }

    private static List<Transition> getTransitions(Workflow workflow, Status source, Status target) {
        if (source.equals(target)) {
            return Collections.emptyList();
        }

        List<List<Transition>> results = new ArrayList<>();

        List<Transition> result = new ArrayList<Transition>();
        List<Transition> valid = new ArrayList<Transition>(workflow.getTransitions());
        Status s = source;

        while (!valid.isEmpty()) {
            if (LOGGER.isTraceEnabled()) {
                logTrace(result, valid);
            }

            Transition next = null;
            // search for any transition that results in the target state:
            for (Transition t : valid) {
                if (s.equals(t.getSource().getStatus()) && target.equals(t.getTarget().getStatus())
                        || t.isGlobal() && target.equals(t.getTarget().getStatus())) {
                    next = t;
                    break;
                }
            }
            if (next == null && !result.isEmpty()) {
                // search for transitions in existing results:
                List<List<Transition>> addResults = new ArrayList<>();
                for (List<Transition> transitions : results) {
                    int index = -1;
                    for (int i = 0; i < transitions.size(); i++) {
                        Transition t = transitions.get(i);
                        if (!t.isGlobal() && s.equals(t.getSource().getStatus())) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        List<Transition> r = new ArrayList<>(result);
                        r.addAll(transitions.subList(index, transitions.size()));
                        if (!results.contains(r) && !addResults.contains(r)) {
                            addResults.add(r);
                        }
                    }
                }
                results.addAll(addResults);
            }
            if (next == null) {
                // use any valid non-global transition:
                for (Transition t : valid) {
                    if (!t.isGlobal() && s.equals(t.getSource().getStatus())) {
                        // check that we don't go back:
                        boolean back = false;
                        for (Transition rt : result) {
                            if (rt.getSource().getStatus() != null
                                    && rt.getSource().getStatus().equals(t.getTarget().getStatus())) {
                                back = true;
                                break;
                            }
                        }
                        if (!back) {
                            next = t;
                            break;
                        }
                    }
                }
            }
            if (next == null && result.isEmpty()) {
                // use any valid global transition:
                for (Transition t : valid) {
                    if (t.isGlobal() && !s.equals(t.getTarget().getStatus())) {
                        result.clear();
                        next = t;
                        break;
                    }
                }
            }
            if (next == null) {
                if (result.isEmpty()) {
                    break;
                } else {
                    // walk back and try other transitions
                    result.remove(result.size() - 1);
                    s = (result.isEmpty() ? source : result.get(result.size() - 1).getTarget().getStatus());
                }
            } else {
                valid.remove(next);
                result.add(next);
                s = next.getTarget().getStatus();

                if (s.equals(target)) {
                    results.add(new ArrayList<Transition>(result));

                    if (result.size() == 1) {
                        // we won't find a shorter transition
                        break;
                    } else {
                        result.remove(result.size() - 1);
                        s = (result.isEmpty() ? source : result.get(result.size() - 1).getTarget().getStatus());
                    }
                }
            }
        }

        result = null;

        LOGGER.trace("Found {} valid result(s): {}", results.size(), results);

        for (List<Transition> r : results) {
            if (result == null || r.size() < result.size()) {
                result = r;
            }
        }

        if (result == null) {
            throw new IllegalArgumentException(
                    "Cannot transition from '" + source.getName() + "' to '" + target.getName() + "'");
        }

        return result;
    }

    private static void logTrace(List<Transition> result, List<Transition> valid) {
        StringBuilder resultStr = new StringBuilder();
        for (Transition t : result) {
            if (resultStr.length() == 0) {
                Status s = t.getSource().getStatus();
                resultStr.append(s == null ? "*" : s.getName());
            }
            resultStr.append(" -> ");
            resultStr.append(t.getTarget().getStatus().getName());
        }

        StringBuilder validStr = new StringBuilder();
        for (Transition t : valid) {
            if (validStr.length() > 0) {
                validStr.append(", ");
            }
            validStr.append(t.getName());
        }

        LOGGER.trace("Intermediate result: {}", resultStr);
        LOGGER.trace("Valid transitions: {}", validStr);
    }
}
