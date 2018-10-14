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

package permafrost.tundra.net.uri;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;

public class URIQueryHelperTest {
    @Test
    public void testParse() throws Exception {
        String[] c = { "3", "4", "5" };
        IData query = URIQueryHelper.parse("a=1&b=2=2&c[0]=3&c[1]=4&c[2]=5&d=6&d=7&e/f=8", true);

        IDataCursor cursor = query.getCursor();
        assertEquals("1", IDataUtil.getString(cursor, "a"));
        assertEquals("2=2", IDataUtil.getString(cursor, "b"));
        assertArrayEquals(c, IDataUtil.getStringArray(cursor, "c"));
        cursor.destroy();
    }

    @Test
    public void testEmit() throws Exception {
        String[] c = { "3", "4", "5" };

        IDataMap e = new IDataMap();
        e.put("f", "8");

        IData query = IDataFactory.create();
        IDataCursor cursor = query.getCursor();
        cursor.insertAfter("a", "1");
        cursor.insertAfter("b", "2");
        cursor.insertAfter("c", c);
        cursor.insertAfter("d", "6");
        cursor.insertAfter("d", "7");
        cursor.insertAfter("e", e);
        cursor.destroy();

        assertEquals("a=1&b=2&c%5B0%5D=3&c%5B1%5D=4&c%5B2%5D=5&d=6&d=7&e%2Ff=8", URIQueryHelper.emit(query, true));
    }
}
