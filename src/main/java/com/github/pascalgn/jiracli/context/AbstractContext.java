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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContext implements Context {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContext.class);

    private final List<Runnable> onClose;

    public AbstractContext() {
        onClose = new ArrayList<Runnable>();
    }

    @Override
    public void onClose(Runnable runnable) {
        Objects.requireNonNull(runnable);
        onClose.add(runnable);
    }

    @Override
    public void close() {
        for (Runnable runnable : onClose) {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                LOGGER.info("Exception while executing onClose action!", e);
            }
        }
    }
}
