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
package com.github.pascalgn.jiracli.gui;

import java.awt.Image;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.Shutdown;

class AppleHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppleHelper.class);

    public static void initialize() {
        try {
            initialize(AppleHelper.class.getClassLoader());
        } catch (ReflectiveOperationException e) {
            LOGGER.trace("Error initializing AppleHelper", e);
        }
    }

    private static void initialize(ClassLoader cl) throws ReflectiveOperationException {
        Class<?> applicationType = cl.loadClass("com.apple.eawt.Application");
        Object application = call(applicationType, "getApplication");

        Image icon = Images.getIconB();
        if (icon != null) {
            call(applicationType, application, "setDockIconImage", Image.class, icon);
        }

        final Class<?> quitResponseType = cl.loadClass("com.apple.eawt.QuitResponse");

        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("handleQuitRequestWith")) {
                    try {
                        Shutdown.runShutdownHooks();
                    } finally {
                        Object response = args[1];
                        call(quitResponseType, response, "performQuit");
                    }
                }
                return null;
            }
        };

        Class<?> quitHandlerType = cl.loadClass("com.apple.eawt.QuitHandler");
        Object quitHandler = Proxy.newProxyInstance(cl, new Class[] { quitHandlerType }, invocationHandler);
        call(applicationType, application, "setQuitHandler", quitHandlerType, quitHandler);
    }

    private static Object call(Class<?> type, String method) throws ReflectiveOperationException {
        Method m = type.getMethod(method);
        return m.invoke(null);
    }

    private static void call(Class<?> type, Object instance, String method) throws ReflectiveOperationException {
        Method m = type.getMethod(method);
        m.invoke(instance);
    }

    private static void call(Class<?> type, Object instance, String method, Class<?> argType, Object arg)
            throws ReflectiveOperationException {
        Method m = type.getMethod(method, argType);
        m.invoke(instance, arg);
    }
}
