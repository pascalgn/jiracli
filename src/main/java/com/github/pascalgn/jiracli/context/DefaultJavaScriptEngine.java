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
import java.io.Writer;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.util.StringSupplierReader;
import com.github.pascalgn.jiracli.util.Supplier;

public class DefaultJavaScriptEngine implements JavaScriptEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJavaScriptEngine.class);

    private static final String INIT_JS = "if (typeof forEach !== 'function') { forEach = Array.prototype.forEach; } "
            + "if (typeof println !== 'function') { println = function(obj) { print(obj); print('\\n'); }; }";

    private final ScriptEngine engine;
    private final ScriptCtx scriptContext;

    public DefaultJavaScriptEngine(Console console) {
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
    public Object evaluate(String js, Object input) {
        if (engine == null) {
            throw new IllegalStateException("No JavaScript engine available!");
        }
        scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put("input", input);
        try {
            return engine.eval(js);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Could not evaluate JavaScript: " + e.getLocalizedMessage(), e);
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
