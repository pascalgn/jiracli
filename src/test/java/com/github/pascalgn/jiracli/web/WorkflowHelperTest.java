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
package com.github.pascalgn.jiracli.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WorkflowHelperTest {
    @Test
    public void test1a() throws Exception {
        String name = "workflowName=Simplified+Workflow+for+Project+JIR";
        assertEquals("Simplified Workflow for Project JIR",
                WorkflowHelper.getWorkflowName("href=\"/browse/JIR-123?" + name + "&amp;stepId=1\""));
    }

    @Test
    public void test1b() throws Exception {
        String name = "workflowName=Simplified+Workflow+for+Project+JIR";
        assertEquals("Simplified Workflow for Project JIR",
                WorkflowHelper.getWorkflowName("href=\"/browse/JIR-123?stepId=1&amp;" + name + "\""));
    }

    @Test
    public void test1c() throws Exception {
        String name = "workflowName=Simplified+Workflow+for+Project+JIR";
        assertEquals("Simplified Workflow for Project JIR",
                WorkflowHelper.getWorkflowName("href=\"/browse/JIR-123?stepId=1&amp;" + name + "&amp;\""));
    }
}
