package com.github.pascalgn.jiracli.util;

import java.io.InputStream;
import java.util.List;

public interface ExcelHelper {
	void parseWorkbook(InputStream inputStream, CellHandler cellHandler);
	
	void parseWorkbook(InputStream inputStream, List<String> sheets, CellHandler cellHandler);

	interface CellHandler {
		void handleCell(int row, String column, String value);
	}
}
