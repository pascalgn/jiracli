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
package com.github.pascalgn.jiracli.util;

import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Object getValue(Object instance, String property, Object defaultValue) {
        try {
            Object result = getValue(instance, property);
            return (result == null ? defaultValue : result);
        } catch (RuntimeException e) {
            if (defaultValue == null) {
                throw e;
            } else {
                return defaultValue;
            }
        }
    }

    public static Object getValue(Object instance, String property) {
        String getterName = "get" + StringUtils.capitalize(property);
        try {
            Method getter = instance.getClass().getMethod(getterName);
            return getter.invoke(instance);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No such property: " + property, e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot get property value: " + property, e);
        }
    }
}
