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
package com.github.pascalgn.jiracli.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Issue extends Data<IssueType> {
    private static final Pattern KEY_PATTERN = Pattern.compile("[A-Z]+-[0-9]+");

    public static Pattern getKeyPattern() {
        return KEY_PATTERN;
    }

    public static boolean isKey(String str) {
        return KEY_PATTERN.matcher(str).matches();
    }

    public static Issue valueOf(String key) {
        if (!isKey(key)) {
            throw new IllegalArgumentException("Invalid issue key: " + key);
        }
        return new Issue(key);
    }

    public static Issue valueOfOrNull(String str) {
        if (isKey(str)) {
            return new Issue(str);
        } else {
            return null;
        }
    }

    public static List<Issue> findAll(String str) {
        List<Issue> result = null;
        Matcher m = KEY_PATTERN.matcher(str);
        if (m.find()) {
            result = new ArrayList<Issue>();
            result.add(new Issue(m.group()));
        } else {
            return Collections.emptyList();
        }
        while (m.find()) {
            result.add(new Issue(m.group()));
        }
        return result;
    }

    private final String key;

    private Issue(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public IssueType getType() {
        return IssueType.getInstance();
    }

    @Override
    public <S extends Type> Data<S> convertTo(S target) {
        return target.accept(new DataConverter() {
            @Override
            public Issue visit(IssueType issue) {
                return Issue.this;
            }

            @Override
            public IssueList visit(IssueListType issueList) {
                return new IssueList(Collections.singleton(Issue.this).iterator());
            }
        });
    }

    @Override
    public String toString() {
        return "Issue[" + key + "]";
    }
}
