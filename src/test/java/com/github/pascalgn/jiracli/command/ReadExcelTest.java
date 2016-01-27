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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.testutil.ExcelUtils;
import com.github.pascalgn.jiracli.testutil.MockConsole;
import com.github.pascalgn.jiracli.testutil.MockWebService;
import com.github.pascalgn.jiracli.testutil.TemporaryFile;

public class ReadExcelTest {
    @Test
    public void test1() throws Exception {
        try (TemporaryFile f = new TemporaryFile("temp", ".xlsx")) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet1 = wb.createSheet("Sheet1");
                for (int row = 10; row <= 110; row++) {
                    ExcelUtils.writeCell(sheet1, row, 20, "ISSUE-" + row);
                }
                try (OutputStream out = f.createOutputStream()) {
                    wb.write(out);
                }
            }

            Context context = new DefaultContext(new MockConsole(), new MockWebService());

            ReadExcel re = new ReadExcel(f.getAbsolutePath());
            IssueList list = re.execute(context, None.getInstance());
            assertNotNull(list);

            List<Issue> issues = list.remaining();
            assertNotNull(issues);
            assertEquals(101, issues.size());
            assertEquals("ISSUE-10", issues.get(0).getKey());
            assertEquals("ISSUE-110", issues.get(100).getKey());
        }
    }
}
