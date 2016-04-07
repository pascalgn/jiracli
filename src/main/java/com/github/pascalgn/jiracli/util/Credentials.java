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

public final class Credentials {
    private final String username;
    private final char[] password;

    public Credentials(String username, char[] password) {
        this.username = username;
        this.password = password;
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
