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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = "sort", description = "Sort the given input")
class Sort implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sort.class);

    private static final String FORMAT = "$summary";

    @Argument(names = { "-f", "--field" }, parameters = Parameters.ONE_OR_MORE, variable = "<field>",
            description = "issue fields to compare")
    private List<String> fields = Collections.singletonList("key");

    @Argument(names = { "-n", "--numeric" }, description = "compare input using numerical values")
    private boolean numeric;

    @Argument(names = { "-r", "--reverse" }, description = "reverse the sort order")
    private boolean reverse;

    @Argument(names = { "-u", "--unique" }, description = "remove duplicate entries")
    private boolean unique;

    @Argument(names = { "-e", "--edit" }, description = "open an editor to change the sort order")
    private boolean edit;

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
            return sort(context, issueList);
        }

        TextList textList = input.toTextList();
        if (textList != null) {
            return sort(textList);
        }

        throw new IllegalArgumentException("Invalid input: " + input);
    }

    private IssueList sort(final Context context, IssueList issueList) {
        List<Issue> issues = issueList.remaining();

        Schema schema = context.getWebService().getSchema();
        Collections.sort(issues, new IssueComparator(schema));

        if (unique) {
            Set<List<String>> set = new HashSet<>();
            Iterator<Issue> it = issues.iterator();
            while (it.hasNext()) {
                Issue issue = it.next();
                List<String> values = values(issue, schema);
                if (!set.add(values)) {
                    it.remove();
                }
            }
        }

        if (edit) {
            final List<Issue> issuesRef = issues;
            issues = CommandUtils.withTemporaryFile("sort", ".txt", new Function<File, List<Issue>>() {
                @Override
                public List<Issue> apply(File tempFile) {
                    try {
                        return edit(context, issuesRef, tempFile);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
        }

        return new IssueList(issues.iterator());
    }

    private List<Issue> edit(Context context, List<Issue> issues, File file) throws IOException {
        Schema schema = context.getWebService().getSchema();
        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            EditingUtils.writeSort(writer, issues, schema, FORMAT);
        }

        boolean success = context.getConsole().editFile(file);
        if (success) {
            try (BufferedReader reader = IOUtils.createBufferedReader(file)) {
                return EditingUtils.readSort(issues, reader);
            }
        } else {
            return issues;
        }
    }

    private TextList sort(TextList textList) {
        if (!fields.isEmpty()) {
            LOGGER.warn("Sorting text list, fields ignored: {}", fields);
        }

        List<Text> list = textList.remaining();
        Collections.sort(list, new TextComparator());

        if (unique) {
            return new TextList(new LinkedHashSet<Text>(list).iterator());
        } else {
            return new TextList(list.iterator());
        }
    }

    private class IssueComparator implements Comparator<Issue> {
        private final Schema schema;

        public IssueComparator(Schema schema) {
            this.schema = schema;
        }

        @Override
        public int compare(Issue issue1, Issue issue2) {
            List<String> values1 = values(issue1, schema);
            List<String> values2 = values(issue2, schema);
            for (int i = 0; i < fields.size(); i++) {
                String value1 = values1.get(i);
                String value2 = values2.get(i);
                int compare = Sort.this.compare(value1, value2);
                if (compare != 0) {
                    return compare;
                }
            }
            return 0;
        }
    }

    private List<String> values(Issue issue, Schema schema) {
        List<String> values = new ArrayList<String>();
        for (String field : fields) {
            Object value = CommandUtils.getFieldValue(issue, schema, field, "");
            values.add(Objects.toString(value, ""));
        }
        return values;
    }

    private class TextComparator implements Comparator<Text> {
        @Override
        public int compare(Text t1, Text t2) {
            String s1 = t1.getText();
            String s2 = t2.getText();
            return Sort.this.compare(s1, s2);
        }
    }

    private int compare(String s1, String s2) {
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
