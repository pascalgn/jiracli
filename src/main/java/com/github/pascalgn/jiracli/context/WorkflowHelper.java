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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.pascalgn.jiracli.model.Status;
import com.github.pascalgn.jiracli.model.Step;
import com.github.pascalgn.jiracli.model.Transition;
import com.github.pascalgn.jiracli.model.Workflow;

class WorkflowHelper {
    private static final Pattern LINK_PATTERN = Pattern.compile("href=\"/browse/[A-Z0-9-]+\\?([^\"]+)\"");
    private static final Pattern WORKFLOW_NAME_PATTERN = Pattern.compile("(?:^|&amp;)workflowName=([^\"&]+)($|&amp;)");

    public static String getWorkflowName(String line) {
        Matcher linkMatcher = LINK_PATTERN.matcher(line);
        while (linkMatcher.find()) {
            Matcher nameMatcher = WORKFLOW_NAME_PATTERN.matcher(linkMatcher.group(1));
            if (nameMatcher.find()) {
                return urlDecode(nameMatcher.group(1));
            }
        }
        return null;
    }

    private static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unsupported encoding!", e);
        }
    }

    public static Workflow parseWorkflow(String workflowName, JSONObject workflowData) {
        try {
            return doParseWorkflow(workflowName, workflowData);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid workflow data: " + workflowName + ": " + workflowData, e);
        }
    }

    private static Workflow doParseWorkflow(String workflowName, JSONObject workflowData) {
        JSONObject layout = workflowData.getJSONObject("layout");

        Map<Integer, Status> statuses = new HashMap<>();
        Map<String, Step> steps = new HashMap<>();

        JSONArray statusArray = layout.getJSONArray("statuses");
        for (Object obj : statusArray) {
            JSONObject json = (JSONObject) obj;
            String id = json.getString("id");
            boolean initial = json.getBoolean("initial");
            Step step;
            if (initial) {
                step = new Step(id);
            } else {
                int statusId = json.getInt("statusId");
                String name = json.getString("name");
                if (statuses.containsKey(statusId)) {
                    throw new IllegalArgumentException("Duplicate status: " + statusId);
                } else {
                    Status status = new Status(statusId, name);
                    step = new Step(id, status);
                    statuses.put(statusId, status);
                }
            }
            if (steps.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate step: " + id);
            } else {
                steps.put(id, step);
            }
        }

        List<Transition> transitions = new ArrayList<>();

        JSONArray transitionArray = layout.getJSONArray("transitions");
        for (Object obj : transitionArray) {
            JSONObject json = (JSONObject) obj;
            boolean looped = json.optBoolean("loopedTransition", false);
            if (looped) {
                continue;
            }
            int id = json.getInt("actionId");
            String name = json.getString("name");
            String sourceId = json.getString("sourceId");
            Step source = steps.get(sourceId);
            if (source == null) {
                throw new IllegalArgumentException("Unknown step: " + sourceId);
            }
            String targetId = json.getString("targetId");
            Step target = steps.get(targetId);
            if (target == null) {
                throw new IllegalArgumentException("Unknown step: " + targetId);
            }
            transitions.add(new Transition(id, name, source, target));
        }

        return new Workflow(workflowName, transitions);
    }
}
