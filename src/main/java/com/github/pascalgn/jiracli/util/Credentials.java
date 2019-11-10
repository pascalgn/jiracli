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
package com.github.pascalgn.jiracli.util;

import java.util.Objects;

public final class Credentials {
    private static final Credentials ANONYMOUS = new Credentials();

    public static Credentials getAnonymous() {
        return ANONYMOUS;
    }

    public static Credentials create(String username, char[] password) {
        return new Credentials(username, password);
    }

    private final String username;
    private final char[] password;

    private Credentials(String username, char[] password) {
        Objects.requireNonNull(username, "Username must not be null!");
        Objects.requireNonNull(password, "Password must not be null!");
        if (!username.equals(username.trim())) {
            throw new IllegalArgumentException("Invalid username!");
        }
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is empty!");
        }
        this.username = username;
        this.password = password;
    }

    private Credentials() {
        this.username = null;
        this.password = null;
    }

    public String getUsername() {
        return username;
    }

    public char[] getPassword() {
        return password;
    }

    public void clearPassword() {
        char[] pw = password;
        if (pw != null) {
            for (int i = 0; i < pw.length; i++) {
                pw[i] = '\0';
            }
        }
    }

    @Override
    public String toString() {
        return "Credentials[username=" + username + "]";
    }
}
