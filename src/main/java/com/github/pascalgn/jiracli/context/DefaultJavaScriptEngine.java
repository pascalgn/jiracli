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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.WebService.CreateRequest;
import com.github.pascalgn.jiracli.context.WebService.Method;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.StringSupplierReader;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultJavaScriptEngine implements JavaScriptEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJavaScriptEngine.class);

    private static final String INIT_JS = "if (typeof forEach !== 'function') { forEach = Array.prototype.forEach; } "
            + "if (typeof println !== 'function') { println = function(obj) { print(obj); print('\\n'); }; }";

    private static final String ALL_FIELDS = "*";

    private final Console console;
    private final WebService webService;

    private final ScriptEngine engine;
    private final ScriptCtx scriptContext;

    private final Map<String, Object> references;

    public DefaultJavaScriptEngine(Console console, WebService webService) {
        this.console = console;
        this.webService = webService;
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByExtension("js");
        if (engine == null) {
            LOGGER.warn("No JavaScript engine available!");
            scriptContext = null;
            references = null;
        } else {
            scriptContext = new ScriptCtx(console);
            engine.setContext(scriptContext);
            try {
                engine.eval(INIT_JS);
            } catch (ScriptException e) {
                throw new IllegalStateException("Could not run initialization js: " + INIT_JS, e);
            }
            references = new HashMap<>();
            references.put("console", new JavaScriptConsole());
            references.put("webService", new JavaScriptWebService());
        }
    }

    @Override
    public Data evaluate(String js) {
        Object result = doEvaluate(js, "");
        return parseResult(result);
    }

    @Override
    public Data evaluate(String js, TextList input) {
        Objects.requireNonNull(input, "Input must not be null!");
        JSONArray arr = new JSONArray();
        Text text;
        while ((text = input.next()) != null) {
            arr.put(text.getText());
        }
        Object obj = toJsonObject(arr.toString());
        Object result = doEvaluate(js, obj);
        return parseResult(result);
    }

    @Override
    public Data evaluate(String js, IssueList input) {
        return doEvaluate(js, input, null);
    }

    @Override
    public Data evaluate(String js, IssueList input, List<String> fields) {
        Objects.requireNonNull(fields, "Fields cannot be null!");
        return doEvaluate(js, input, fields);
    }

    private Data doEvaluate(String js, IssueList input, List<String> fields) {
        Objects.requireNonNull(input, "Input must not be null!");
        List<Issue> issues = input.remaining();
        String inputStr = toJsonArray(issues, fields);
        Object inputObj = toJsonObject(inputStr);
        Object resultObj = doEvaluate(js, inputObj);
        return parseResult(resultObj);
    }

    private Object doEvaluate(String js, Object input) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(engine, "No JavaScript engine available!");
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("input", input);
        bindings.putAll(references);
        try {
            return engine.eval(js);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Could not evaluate JavaScript: " + e.getLocalizedMessage(), e);
        }
    }

    private Object toJsonObject(String json) {
        Objects.requireNonNull(engine, "No JavaScript engine available!");
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put("input", json);
        return eval("JSON.parse(input)");
    }

    private String toJsonString(Object json) {
        Objects.requireNonNull(engine, "No JavaScript engine available!");
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put("input", json);
        return Objects.toString(eval("JSON.stringify(input)"), "");
    }

    private Object eval(String js) {
        try {
            return engine.eval(js);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Could not evaluate JavaScript: " + e.getLocalizedMessage(), e);
        }
    }

    private static String toJsonArray(List<Issue> issues, List<String> fields) {
        StringBuilder str = new StringBuilder("[");
        boolean first = true;
        for (Issue issue : issues) {
            if (first) {
                first = false;
            } else {
                str.append(",");
            }
            str.append(toJson(issue, fields));
        }
        str.append("]");
        return str.toString();
    }

    private static String toJson(Issue issue, List<String> fields) {
        try (StringWriter stringWriter = new StringWriter()) {
            JSONWriter writer = new JSONWriter(stringWriter);
            writer.object();
            writer.key("key");
            writer.value(issue.getKey());
            writer.key("fields");
            writer.object();
            Collection<Field> fieldList;
            FieldMap fieldMap = issue.getFieldMap();
            if (fields == null) {
                if (fieldMap instanceof LoadingFieldMap) {
                    fieldList = ((LoadingFieldMap) fieldMap).getLoadedFields();
                    if (fieldList.isEmpty()) {
                        fieldList = fieldMap.getFields();
                    }
                } else {
                    fieldList = fieldMap.getFields();
                }
            } else if (fields.contains(ALL_FIELDS)) {
                fieldList = fieldMap.getFields();
            } else {
                fieldList = new ArrayList<Field>();
                for (String f : fields) {
                    Field field = fieldMap.getFieldById(f);
                    if (field != null) {
                        fieldList.add(field);
                    }
                }
            }
            for (Field field : fieldList) {
                writer.key(field.getId());
                writer.value(field.getValue().get());
            }
            writer.endObject();
            writer.endObject();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Data parseResult(Object result) {
        if (result == null) {
            return None.getInstance();
        }

        String str;
        if (result instanceof String) {
            str = (String) result;
        } else if (result.getClass().isArray()) {
            Object[] arr = (Object[]) result;
            str = StringUtils.join(Arrays.asList(arr), System.lineSeparator());
        } else {
            try {
                str = toJsonString(result);
            } catch (RuntimeException e) {
                LOGGER.trace("Could not parse result", e);
                str = null;
            }
            if (str == null) {
                return None.getInstance();
            }
        }

        str = str.trim();

        JSONArray array = JsonUtils.toJsonArray(str);
        if (array == null) {
            JSONObject obj = JsonUtils.toJsonObject(str);
            if (obj != null) {
                Issue issue = toIssue(obj);
                if (issue != null) {
                    return issue;
                }
            }
            return new Text(str);
        } else {
            boolean allNull = true;
            for (int i = 0; i < array.length(); i++) {
                if (!array.isNull(i)) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                return None.getInstance();
            } else {
                List<Issue> issueList = toIssueList(array);
                if (issueList == null || issueList.isEmpty()) {
                    List<Text> textList = toTextList(array);
                    return new TextList(textList.iterator());
                } else {
                    return new IssueList(issueList.iterator());
                }
            }
        }
    }

    private List<Issue> toIssueList(JSONArray array) {
        List<Issue> issues = new ArrayList<Issue>();
        for (Object obj : array) {
            Issue issue = toIssue((JSONObject) obj);
            if (issue != null) {
                issues.add(issue);
            }
        }
        return issues;
    }

    private Issue toIssue(JSONObject json) {
        String key = json.optString("key");
        if (key == null || key.isEmpty()) {
            return null;
        }
        Issue issue = webService.getIssue(key);
        JSONObject fields = json.optJSONObject("fields");
        if (fields != null) {
            FieldMap fieldMap = issue.getFieldMap();
            for (String id : fields.keySet()) {
                Object val = fields.get(id);
                Field field = fieldMap.getFieldById(id);
                if (field == null) {
                    field = new Field(issue, id, new Value(val));
                    fieldMap.addField(field);
                } else if (!Objects.equals(val, field.getValue().get())) {
                    field.getValue().set(val);
                }
            }
        }
        return issue;
    }

    private List<Text> toTextList(JSONArray array) {
        List<Text> list = new ArrayList<Text>();
        for (Object obj : array) {
            String str = Objects.toString(obj, "").trim();
            if (!str.isEmpty()) {
                list.add(new Text(str));
            }
        }
        return list;
    }

    public class JavaScriptConsole {
        public void print(String str) {
            console.print(str);;
        }

        public void println(String str) {
            console.println(str);
        }

        public String readLine() {
            return console.readLine();
        }

        public Object readLines() {
            return console.readLines().toArray();
        }

        public String edit(String content) {
            try {
                File tempFile = File.createTempFile("temp", ".txt");
                try {
                    IOUtils.write(tempFile, content);
                    if (console.editFile(tempFile)) {
                        return IOUtils.toString(tempFile);
                    } else {
                        return null;
                    }
                } finally {
                    if (!tempFile.delete() && tempFile.exists()) {
                        LOGGER.warn("Could not delete temporary file: {}", tempFile);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public class JavaScriptWebService {
        public String execute(String method, String path, String body) {
            return webService.execute(Method.valueOf(method.toUpperCase()), path, body);
        }

        public Object getIssue(String key) {
            return getIssue(key, null);
        }

        public Object getIssue(String key, List<String> fields) {
            Issue issue = webService.getIssue(key);
            String json = toJson(issue, fields);
            return toJsonObject(json);
        }

        public Object createIssue(final Map<String, String> fields) {
            CreateRequest request = new CreateRequest() {
                @Override
                public Map<String, String> getFields() {
                    return fields;
                }
            };
            List<Issue> issues = webService.createIssues(Collections.singletonList(request));
            if (issues.isEmpty()) {
                return null;
            } else if (issues.size() == 1) {
                String json = toJson(issues.get(0), null);
                return toJsonObject(json);
            } else {
                List<Object> result = new ArrayList<Object>();
                for (Issue issue : issues) {
                    String json = toJson(issue, null);
                    result.add(toJsonObject(json));
                }
                return result;
            }
        }
    }

    private static class ScriptCtx extends SimpleScriptContext {
        public ScriptCtx(final Console console) {
            Reader reader = new StringSupplierReader(new Supplier<String>() {
                @Override
                public String get() {
                    return console.readLine();
                }
            });
            setReader(reader);

            Writer writer = new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    console.print(new String(cbuf, off, len));
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
            };
            setWriter(writer);
            setErrorWriter(writer);
        }
    }
}
