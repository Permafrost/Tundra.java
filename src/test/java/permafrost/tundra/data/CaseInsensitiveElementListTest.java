/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import static org.junit.Assert.*;
import org.junit.Test;

public class CaseInsensitiveElementListTest {
    @Test
    public void add() throws Exception {
        String bandName = "Joy Division";

        ElementList<String, Object> band = new CaseInsensitiveElementList<Object>();
        band.add(new Element<String, Object>("Name", bandName));

        IDataCursor cursor = band.getCursor();
        assertEquals("Keys should be case insensitive", bandName, IDataUtil.getString(cursor, "name"));
        assertEquals("Keys should be case insensitive", bandName, IDataUtil.getString(cursor, "NAME"));
        assertEquals("Keys should be case insensitive", bandName, IDataUtil.getString(cursor, "naME"));

        cursor.first();
        assertEquals("Keys should have their case preserved", "Name", cursor.getKey());
        assertEquals(bandName, cursor.getValue());
    }

}