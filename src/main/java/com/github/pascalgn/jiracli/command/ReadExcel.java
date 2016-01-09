package com.github.pascalgn.jiracli.command;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.NoneType;
import com.github.pascalgn.jiracli.util.ExcelHelper;
import com.github.pascalgn.jiracli.util.ExcelHelper.CellHandler;
import com.github.pascalgn.jiracli.util.ExcelHelperFactory;

class ReadExcel implements Command<NoneType, None, IssueList> {
	private final String filename;

	public ReadExcel(String filename) {
		this.filename = filename;
	}

	@Override
	public NoneType getInputType() {
		return NoneType.getInstance();
	}

	@Override
	public IssueList execute(Context context, None input) {
		return new IssueList(getSupplier(context));
	}

	private Supplier<Issue> getSupplier(Context context) {
		return new ExcelReader(filename);
	}

	private static class ExcelReader implements Supplier<Issue> {
		private final String filename;

		private transient List<Issue> issues;
		private transient int index;

		public ExcelReader(String filename) {
			this.filename = filename;
		}

		@Override
		public Issue get() {
			init();
			if (index < issues.size()) {
				return issues.get(index++);
			} else {
				return null;
			}
		}

		private synchronized void init() {
			if (issues == null) {
				issues = new ArrayList<Issue>();
				ExcelHelper excelHelper = ExcelHelperFactory.createExcelHelper();
				try (InputStream input = new FileInputStream(filename)) {
					excelHelper.parseWorkbook(input, new CellHandler() {
						@Override
						public void handleCell(int row, String column, String value) {
							issues.addAll(Issue.findAll(value));
						}
					});
				} catch (IOException e) {
					throw new IllegalStateException("Error reading from file: " + filename, e);
				}
			}
		}
	}
}
