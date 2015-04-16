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
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IDataHTMLCoderTest {
    IData document;

    @Before
    public void setUp() throws Exception {
        document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "abc", "123");
        IDataUtil.put(cursor, "def", "456");
        IDataUtil.put(cursor, "ghi", "789");
        cursor.destroy();
    }

    @Test
    public void testEncodeToString() throws Exception {
        String html = IDataHTMLCoder.getInstance().encodeToString(document);
        assertTrue(html.contains("abc"));
        assertTrue(html.contains("def"));
        assertTrue(html.contains("ghi"));
        assertTrue(html.contains("123"));
        assertTrue(html.contains("456"));
        assertTrue(html.contains("789"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDecodeToString() throws Exception {
        IDataHTMLCoder.getInstance().decodeFromString("test");
    }
}