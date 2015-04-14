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
import org.junit.Test;

import static org.junit.Assert.*;

public class IDataJSONCoderTest {

    @Test
    public void testEncodeToString() throws Exception {
        IDataMap document = new IDataMap();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "abc", "123");
        IDataUtil.put(cursor, "def", "456");
        IDataUtil.put(cursor, "ghi", "789");
        cursor.destroy();

        String json = IDataJSONCoder.getInstance().encodeToString(document);

        assertTrue(json.contains("abc"));
        assertTrue(json.contains("123"));
        assertTrue(json.contains("def"));
        assertTrue(json.contains("456"));
        assertTrue(json.contains("ghi"));
        assertTrue(json.contains("789"));
    }

    @Test
    public void testDecodeFromString() throws Exception {
        String json = "{ \"abc\": \"123\", \"def\": \"456\", \"ghi\": \"789\" }";

        IData document = IDataJSONCoder.getInstance().decodeFromString(json);
        IDataCursor cursor = document.getCursor();
        assertEquals("123", IDataUtil.getString(cursor, "abc"));
        assertEquals("456", IDataUtil.getString(cursor, "def"));
        assertEquals("789", IDataUtil.getString(cursor, "ghi"));
        cursor.destroy();
    }
}