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
package com.github.pascalgn.jiracli.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;

@CommandDescription(names = "sort", description = "Sort the given input")
class Sort implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sort.class);

    @Argument(parameters = Parameters.ZERO_OR_MORE, variable = "<field>", description = "issue fields to compare")
    private List<String> fields;

    @Argument(names = { "-n", "--numeric" }, description = "compare input using numerical values")
    private boolean numeric;

    @Argument(names = { "-r", "--reverse" }, description = "reverse the sort order")
    private boolean reverse;

    @Argument(names = { "-u", "--unique" }, description = "remove duplicate entries")
    private boolean unique;

    public Sort() {
        // default constructor
    }

    Sort(List<String> fields, boolean numeric, boolean reverse, boolean unique) {
        this.fields = fields;
        this.numeric = numeric;
        this.reverse = reverse;
        this.unique = unique;
    }

    @Override
    public Data execute(Context context, Data input) {
        IssueList issueList = input.toIssueList();
        if (issueList != null) {
            return sort(issueList);
        }

        TextList textList = input.toTextList();
        if (textList != null) {
            return sort(textList);
        }

        throw new IllegalArgumentException("Invalid input: " + input);
    }

    private IssueList sort(IssueList issueList) {
        return new IssueList();
    }

    private TextList sort(TextList textList) {
        if (!fields.isEmpty()) {
            LOGGER.warn("Sorting text list, fields ignored: {}", fields);
        }

        List<Text> list = textList.remaining();
        Collections.sort(list, new TextComparator(numeric, reverse));

        if (unique) {
            return new TextList(new LinkedHashSet<Text>(list).iterator());
        } else {
            return new TextList(list.iterator());
        }
    }

    private static class TextComparator implements Comparator<Text> {
        private final boolean numeric;
        private final boolean reverse;

        public TextComparator(boolean numeric, boolean reverse) {
            this.numeric = numeric;
            this.reverse = reverse;
        }

        @Override
        public int compare(Text t1, Text t2) {
            String s1 = t1.getText();
            String s2 = t2.getText();
            if (s1.equals(s2)) {
                return 0;
            }
            int scale = (reverse ? -1 : 1);
            if (numeric) {
                Double d1 = toDouble(s1);
                Double d2 = toDouble(s2);
                if (d1 == null && d2 == null) {
                    return s1.compareTo(s2);
                } else if (d1 == null) {
                    return 1 * scale;
                } else if (d2 == null) {
                    return -1 * scale;
                } else {
                    return Double.compare(d1, d2) * scale;
                }
            } else {
                return s1.compareTo(s2) * scale;
            }
        }

        private static Double toDouble(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e0) {
                try {
                    return Double.parseDouble(str.replace(",", "."));
                } catch (NumberFormatException e1) {
                    try {
                        return Double.parseDouble(str.replace(".", ","));
                    } catch (NumberFormatException e2) {
                        return null;
                    }
                }
            }
        }
    }
}
