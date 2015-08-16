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
import static org.junit.Assert.*;
import org.junit.Test;

public class ReadOnlyIDataMapTest {

    @Test
    public void testPutIgnored() throws Exception {
        IData document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", "1");
        cursor.destroy();

        ReadOnlyIDataMap map = ReadOnlyIDataMap.of(document);

        map.put("a", "2");
        assertEquals("1", map.get("a"));

        cursor = map.getCursor();
        IDataUtil.put(cursor, "a", "3");
        cursor.destroy();
        assertEquals("1", map.get("a"));
    }


    @Test
    public void testOfWithNullArgument() throws Exception {
        ReadOnlyIDataMap map = ReadOnlyIDataMap.of((IData)null);
        assertEquals(0, map.size());
    }
}