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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.github.pascalgn.jiracli.testutil.ExcelUtils;
import com.github.pascalgn.jiracli.util.ExcelHelper.CellHandler;

public class SimpleExcelHelperTest {
    @Test
    public void test1() throws Exception {
        byte[] buf;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet1 = wb.createSheet("Sheet1");
                for (int row = 0; row < 10; row++) {
                    for (int column = 0; column < 10; column++) {
                        String cell = row + "/" + column;
                        ExcelUtils.writeCell(sheet1, row, column, cell);
                    }
                }
                wb.write(out);
            }
            buf = out.toByteArray();
        }

        final List<String> cellValues = new ArrayList<>();

        try (InputStream inputStream = new ByteArrayInputStream(buf)) {
            ExcelHelper excelHelper = new SimpleExcelHelper();
            excelHelper.parseWorkbook(inputStream, new CellHandler() {
                @Override
                public void handleCell(int row, String column, String value) {
                    cellValues.add(value);
                }
            });
        }

        assertEquals(100, cellValues.size());
        assertEquals("1/2", cellValues.get(12));
        assertEquals("4/3", cellValues.get(43));
        assertEquals("9/9", cellValues.get(99));
    }
}
