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

public class URIQueryHelperTest {
    @Test
    public void testParse() throws Exception {
        String[] c = { "3", "4", "5" };
        IData query = URIQueryHelper.parse("a=1&b=2=2&c=3&c=4&c=5", true);
        IDataCursor cursor = query.getCursor();
        assertEquals("1", IDataUtil.getString(cursor, "a"));
        assertEquals("2=2", IDataUtil.getString(cursor, "b"));
        assertArrayEquals(c, IDataUtil.getStringArray(cursor, "c"));
        cursor.destroy();
    }

    @Test
    public void testEmit() throws Exception {
        String[] c = { "3", "4", "5" };
        IData query = IDataFactory.create();
        IDataCursor cursor = query.getCursor();
        IDataUtil.put(cursor, "a", "1");
        IDataUtil.put(cursor, "b", "2");
        IDataUtil.put(cursor, "c", c);
        cursor.destroy();

        assertEquals("a=1&b=2&c=3&c=4&c=5", URIQueryHelper.emit(query, true));
    }
}
