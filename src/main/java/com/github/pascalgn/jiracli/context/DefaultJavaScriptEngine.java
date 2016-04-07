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

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.LoadingList;
import com.github.pascalgn.jiracli.util.StringSupplierReader;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultJavaScriptEngine implements JavaScriptEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJavaScriptEngine.class);

    private static final String INIT_JS = "if (typeof forEach !== 'function') { forEach = Array.prototype.forEach; } "
            + "if (typeof println !== 'function') { println = function(obj) { print(obj); print('\\n'); }; }";

    private final WebService webService;

    private final ScriptEngine engine;
    private final ScriptCtx scriptContext;

    public DefaultJavaScriptEngine(Console console, WebService webService) {
        this.webService = webService;
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByExtension("js");
        if (engine == null) {
            LOGGER.warn("No JavaScript engine available!");
            scriptContext = null;
        } else {
            scriptContext = new ScriptCtx(console);
            engine.setContext(scriptContext);
            try {
                engine.eval(INIT_JS);
            } catch (ScriptException e) {
                throw new IllegalStateException("Could not run initialization js: " + INIT_JS, e);
            }
        }
    }

    @Override
    public Text evaluate(String js, Text input) {
        Objects.requireNonNull(input, "Input must not be null!");
        Object result = evaluate0(js, input.getText());
        return new Text(Objects.toString(result, ""));
    }

    @Override
    public TextList evaluate(final String js, final TextList input) {
        Objects.requireNonNull(input, "Input must not be null!");
        List<Text> result = new LoadingList<Text>() {
            @Override
            protected List<Text> loadList() {
                JSONArray arr = new JSONArray();
                Text text;
                while ((text = input.next()) != null) {
                    arr.put(text.getText());
                }
                return null;
            }
        };
        return new TextList(result.iterator());
    }

    @Override
    public Issue evaluate(String js, Issue input) {
        Objects.requireNonNull(input, "Input must not be null!");
        String inputStr = toJson(input);
        Object inputObj = evaluate0("JSON.parse(input)", inputStr);
        Object resultObj = evaluate0(js, inputObj);
        String resultStr = (String) evaluate0("JSON.stringify(input)", resultObj);
        return fromJson(resultStr);
    }

    @Override
    public IssueList evaluate(final String js, final IssueList input) {
        Objects.requireNonNull(input, "Input must not be null!");
        List<Issue> result = new LoadingList<Issue>() {
            @Override
            protected List<Issue> loadList() {
                List<Issue> issues = input.remaining();
                String inputStr = toJsonArray(issues);
                Object inputObj = evaluate0("JSON.parse(input)", inputStr);
                Object resultObj = evaluate0(js, inputObj);
                if (resultObj == null) {
                    return Collections.emptyList();
                } else {
                    String resultStr = (String) evaluate0("JSON.stringify(input)", resultObj);
                    return fromJsonArray(issues, resultStr);
                }
            }
        };
        return new IssueList(result.iterator());
    }

    @Override
    public Object evaluate(String js) {
        return evaluate0(js, null);
    }

    @Override
    public Object evaluate(String js, Object input) {
        Objects.requireNonNull(input, "Input must not be null!");
        return evaluate0(js, input);
    }

    private Object evaluate0(String js, Object input) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(engine, "No JavaScript engine available!");
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put("input", input);
        try {
            return engine.eval(js);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Could not evaluate JavaScript: " + e.getLocalizedMessage(), e);
        }
    }

    private static String toJsonArray(List<Issue> issues) {
        StringBuilder str = new StringBuilder("[");
        boolean first = true;
        for (Issue issue : issues) {
            if (first) {
                first = false;
            } else {
                str.append(",");
            }
            str.append(toJson(issue));
        }
        str.append("]");
        return str.toString();
    }

    private static String toJson(Issue issue) {
        try (StringWriter stringWriter = new StringWriter()) {
            JSONWriter writer = new JSONWriter(stringWriter);
            writer.object();
            writer.key("key");
            writer.value(issue.getKey());
            writer.key("fields");
            writer.object();
            for (Field field : issue.getFieldMap().getFields()) {
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

    private List<Issue> fromJsonArray(List<Issue> issues2, String str) {
        JSONArray arr = new JSONArray(str);
        List<Issue> issues = new ArrayList<Issue>();
        for (Object obj : arr) {
            issues.add(fromJson((JSONObject) obj));
        }
        return issues;
    }

    private Issue fromJson(String str) {
        return fromJson(new JSONObject(str));
    }

    private Issue fromJson(JSONObject json) {
        String key = json.getString("key");
        Issue issue = webService.getIssue(key);
        JSONObject fields = json.optJSONObject("fields");
        if (fields != null) {
            FieldMap fieldMap = issue.getFieldMap();
            for (String id : fields.keySet()) {
                Object val = fields.get(key);
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
