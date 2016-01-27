package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.testutil.MockConsole;
import com.github.pascalgn.jiracli.testutil.MockWebService;

public class Base64Test {
	@Test
	public void test1() throws Exception {
		MockConsole console = new MockConsole("hello");
		Context context = new DefaultContext(console, new MockWebService());
		Base64 base64 = new Base64();
		base64.execute(context, None.getInstance());
		assertEquals("aGVsbG8=", console.getOutput().trim());
	}
}
