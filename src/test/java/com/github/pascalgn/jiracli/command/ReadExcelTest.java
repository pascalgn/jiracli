package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.testutil.DummyContext;
import com.github.pascalgn.jiracli.testutil.ExcelUtils;
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
			
			Context context = new DummyContext();
			
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
