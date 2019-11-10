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
package com.github.pascalgn.jiracli.context;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.WebService.CreateRequest;
import com.github.pascalgn.jiracli.context.WebService.Method;
import com.github.pascalgn.jiracli.context.WebService.Request;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.ConversionUtils;
import com.github.pascalgn.jiracli.util.JsonUtils;
import com.github.pascalgn.jiracli.util.StringSupplierReader;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultJavaScriptEngine implements JavaScriptEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJavaScriptEngine.class);

    private static final String INIT_JS = "if (typeof forEach !== 'function') { forEach = Array.prototype.forEach; } "
            + "if (typeof println !== 'function') { println = function(obj) { print(obj); print('\\n'); }; }";

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
    public TextList evaluate(String js) {
        Object result = doEvaluate(js, "");
        return parseResult(result);
    }

    @Override
    public TextList evaluate(String js, Text input) {
        Object result = doEvaluate(js, input.getText());
        return parseResult(result);
    }

    @Override
    public TextList evaluate(String js, TextList input) {
        Objects.requireNonNull(input, "Input must not be null!");
        JSONArray arr = new JSONArray();
        Text text;
        while ((text = input.next(Hint.none())) != null) {
            arr.put(text.getText());
        }
        Object obj = toJsonObject(arr.toString());
        Object result = doEvaluate(js, obj);
        return parseResult(result);
    }

    @Override
    public TextList evaluate(String js, Issue input) {
        Object inputObj = toJsonObject(ConversionUtils.toJson(input).toString());
        Object resultObj = doEvaluate(js, inputObj);
        return parseResult(resultObj);
    }

    @Override
    public TextList evaluate(String js, IssueList input) {
        List<Issue> issues = input.remaining(Hint.none());
        String inputStr = toJsonArray(issues);
        Object inputObj = toJsonObject(inputStr);
        Object resultObj = doEvaluate(js, inputObj);
        return parseResult(resultObj);
    }

    @Override
    public boolean test(String js, Text input) {
        Object resultObj = doEvaluate(js, input.getText());
        return parseBooleanResult(resultObj);
    }

    @Override
    public boolean test(String js, Issue input) {
        Object inputObj = toJsonObject(ConversionUtils.toJson(input).toString());
        Object resultObj = doEvaluate(js, inputObj);
        return parseBooleanResult(resultObj);
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

    private static String toJsonArray(List<Issue> issues) {
        StringBuilder str = new StringBuilder("[");
        boolean first = true;
        for (Issue issue : issues) {
            if (first) {
                first = false;
            } else {
                str.append(",");
            }
            str.append(ConversionUtils.toJson(issue));
        }
        str.append("]");
        return str.toString();
    }

    private TextList parseResult(Object result) {
        TextList empty = new TextList();

        if (result == null) {
            return empty;
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
                return empty;
            }
        }

        str = str.trim();

        JSONArray array = JsonUtils.toJsonArray(str);
        if (array == null) {
            return new TextList(new Text(str));
        } else {
            boolean allNull = true;
            for (int i = 0; i < array.length(); i++) {
                if (!array.isNull(i)) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                return empty;
            } else {
                List<Text> texts = toTextList(array);
                return new TextList(texts.iterator());
            }
        }
    }

    private List<Text> toTextList(JSONArray array) {
        List<Text> list = new ArrayList<>();
        for (Object obj : array) {
            String str = Objects.toString(obj, "").trim();
            if (!str.isEmpty()) {
                list.add(new Text(str));
            }
        }
        return list;
    }

    private boolean parseBooleanResult(Object result) {
        Object bool = doEvaluate("(input ? true : false)", result);
        if (bool == Boolean.TRUE) {
            return true;
        } else if (bool == Boolean.FALSE) {
            return false;
        } else {
            throw new IllegalStateException("Invalid result: " + bool);
        }
    }

    public class JavaScriptConsole {
        public void print(String str) {
            console.print(str);
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
        public String execute(String url) {
            return execute("GET", url, null);
        }

        public String execute(String method, String url) {
            return execute(method, url, null);
        }

        public String execute(String method, String url, String body) {
            URI uri;
            if (url.startsWith("http:") || url.startsWith("https:")) {
                uri = URI.create(url);
            } else {
                uri = URI.create(console.getBaseUrl() + url);
            }
            return webService.execute(Method.valueOf(method.toUpperCase()), uri, body);
        }

        public Schema getSchema() {
            return webService.getSchema();
        }

        public Object getIssue(String key) {
            return getIssue(key, null);
        }

        public Object getIssue(String key, final List<String> fields) {
            Request request = new Request() {
                @Override
                public Collection<String> getFields() {
                    return fields;
                }

                @Override
                public Collection<String> getExpand() {
                    return Collections.emptyList();
                }

                @Override
                public boolean getAllFields() {
                    return false;
                }
            };
            Issue issue = webService.getIssue(key, request);
            String json = ConversionUtils.toJson(issue, fields).toString();
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
                String json = ConversionUtils.toJson(issues.get(0)).toString();
                return toJsonObject(json);
            } else {
                List<Object> result = new ArrayList<>();
                for (Issue issue : issues) {
                    String json = ConversionUtils.toJson(issue).toString();
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
                public String get(Set<Hint> hints) {
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
