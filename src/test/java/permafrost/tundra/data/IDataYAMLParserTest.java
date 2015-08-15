/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class IDataYAMLParserTest {

    @Test
    public void testDecodeFromString() throws Exception {
        String yaml = "abc: '123'\ndef: '456'\nghi: '789'";

        IData document = IDataYAMLParser.getInstance().decodeFromString(yaml);
        IDataCursor cursor = document.getCursor();
        assertEquals("123", IDataUtil.getString(cursor, "abc"));
        assertEquals("456", IDataUtil.getString(cursor, "def"));
        assertEquals("789", IDataUtil.getString(cursor, "ghi"));
        cursor.destroy();
    }

    @Test
    public void testEncodeToString() throws Exception {
        String content = "this is a test";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        IDataMap document = new IDataMap();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "abc", "123");
        IDataUtil.put(cursor, "def", "456");
        IDataUtil.put(cursor, "ghi", inputStream);
        IDataUtil.put(cursor, "jkl", new ObjectWithNoPublicMembers(1, 2));
        cursor.destroy();

        String yaml = IDataYAMLParser.getInstance().encodeToString(document);

        assertTrue(yaml.contains("abc"));
        assertTrue(yaml.contains("123"));
        assertTrue(yaml.contains("def"));
        assertTrue(yaml.contains("456"));
        assertTrue(yaml.contains("ghi"));
        assertTrue(yaml.contains("jkl"));
    }

    private class ObjectWithNoPublicMembers {
        private int foo;
        private int bar;

        public ObjectWithNoPublicMembers(int foo, int bar) {
            this.foo = foo;
            this.bar = bar;
        }
    }
}
