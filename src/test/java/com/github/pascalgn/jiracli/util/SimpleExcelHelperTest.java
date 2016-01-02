package com.github.pascalgn.jiracli.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.github.pascalgn.jiracli.util.ExcelHelper.CellHandler;

public class SimpleExcelHelperTest {
	@Test
	public void test1() throws Exception {
		byte[] buf;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (Workbook wb = new XSSFWorkbook()) {
				Sheet sheet1 = wb.createSheet("Sheet1");
				for (int row = 0; row < 100; row++) {
					for (int column = 0; column < 100; column++) {
						String cell = row + "/" + column;
						ExcelUtils.writeCell(sheet1, row, column, cell);
					}
				}
				wb.write(out);
			}
			buf = out.toByteArray();
		}
		
		final List<String> cellValues = new ArrayList<String>();
		
		try (InputStream inputStream = new ByteArrayInputStream(buf)) {
			ExcelHelper excelHelper = new SimpleExcelHelper();
			excelHelper.parseWorkbook(inputStream, new CellHandler() {
				@Override
				public void handleCell(int row, String column, String value) {
					cellValues.add(value);
				}
			});
		}
		
		assertEquals(10000, cellValues.size());
		assertEquals("12/34", cellValues.get(1234));
		assertEquals("43/21", cellValues.get(4321));
		assertEquals("99/99", cellValues.get(9999));
	}
}
