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
package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.testutil.MockContext;
import com.github.pascalgn.jiracli.util.Hint;

public class Base64Test {
    @Test
    public void test1() throws Exception {
        Context context = new MockContext();
        Base64 base64 = new Base64();
        TextList result = base64.execute(context, new TextList(new Text("hello")));
        List<Text> list = result.remaining(Hint.none());
        assertEquals(1, list.size());
        assertEquals("aGVsbG8=", list.get(0).getText());
    }
}
