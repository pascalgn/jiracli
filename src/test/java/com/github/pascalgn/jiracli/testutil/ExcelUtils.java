package com.github.pascalgn.jiracli.testutil;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils {
	/**
	 * @param row 0-based index
	 * @param column 0-based index
	 */
	public static void writeCell(Sheet sheet, int row, int column, String value) {
		Row r = sheet.getRow(row);
		if (r == null) {
			r = sheet.createRow(row);
		}
		Cell cell = r.getCell(column);
		if (cell == null) {
			cell = r.createCell(column, Cell.CELL_TYPE_STRING);
		}
		cell.setCellValue(value);
	}
}
