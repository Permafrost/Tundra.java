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
import org.junit.Test;
import permafrost.tundra.lang.StringHelper;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IDataCSVParserTest {

    @Test
    public void testEncodeToString() throws Exception {
        // rename column headings to ensure this feature works
        String[] columns = new String[] { "column1", "column2" };
        String expected = "column1,column2\nJohn,john@example.org\nJean,jean@example.org\nBill,bill@example.org\n";

        IData[] records = new IData[3];
        IDataCursor cursor;

        records[0] = IDataFactory.create();
        cursor = records[0].getCursor();
        IDataUtil.put(cursor, "name", "John");
        IDataUtil.put(cursor, "email", "john@example.org");
        cursor.destroy();

        records[1] = IDataFactory.create();
        cursor = records[1].getCursor();
        // reorder keys to ensure key ordering insensitivity
        IDataUtil.put(cursor, "email", "jean@example.org");
        IDataUtil.put(cursor, "name", "Jean");
        cursor.destroy();

        records[2] = IDataFactory.create();
        cursor = records[2].getCursor();
        IDataUtil.put(cursor, "name", "Bill");
        IDataUtil.put(cursor, "email", "bill@example.org");
        cursor.destroy();

        IDataMap document = new IDataMap();
        cursor = document.getCursor();
        IDataUtil.put(cursor, "recordWithNoID", records);
        cursor.destroy();

        String actual = new IDataCSVParser(',', "text/csv", true, columns).emit(document, String.class);

        assertArrayEquals(StringHelper.lines(expected), StringHelper.lines(actual));
    }

    @Test
    public void testEncodeToStringWithCustomDelimiter() throws Exception {
        IData[] records = new IData[3];
        IDataCursor cursor;

        records[0] = IDataFactory.create();
        cursor = records[0].getCursor();
        IDataUtil.put(cursor, "name", "John");
        IDataUtil.put(cursor, "email", "john@example.org");
        cursor.destroy();

        records[1] = IDataFactory.create();
        cursor = records[1].getCursor();
        IDataUtil.put(cursor, "name", "Jean");
        IDataUtil.put(cursor, "email", "jean@example.org");
        cursor.destroy();

        records[2] = IDataFactory.create();
        cursor = records[2].getCursor();
        IDataUtil.put(cursor, "name", "Bill");
        IDataUtil.put(cursor, "email", "bill@example.org");
        cursor.destroy();

        IDataMap document = new IDataMap();
        cursor = document.getCursor();
        IDataUtil.put(cursor, "recordWithNoID", records);
        cursor.destroy();

        IDataCSVParser parser = new IDataCSVParser('|');
        String csv = parser.emit(document, String.class);

        assertTrue(csv.contains("John|john@example.org"));
        assertTrue(csv.contains("Jean|jean@example.org"));
        assertTrue(csv.contains("Bill|bill@example.org"));
    }

    @Test
    public void testDecodeFromString() throws Exception {
        String csv = "name,email\nJohn,john@example.org\nJean,jean@example.org\nBill,bill@example.org";

        IData document = new IDataCSVParser().parse(csv);
        IDataCursor cursor = document.getCursor();
        IData[] records = IDataUtil.getIDataArray(cursor, "recordWithNoID");
        cursor.destroy();

        assertEquals(3, records.length);

        cursor = records[0].getCursor();
        assertEquals("John", IDataUtil.getString(cursor, "name"));
        assertEquals("john@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();

        cursor = records[1].getCursor();
        assertEquals("Jean", IDataUtil.getString(cursor, "name"));
        assertEquals("jean@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();

        cursor = records[2].getCursor();
        assertEquals("Bill", IDataUtil.getString(cursor, "name"));
        assertEquals("bill@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();
    }

    @Test
    public void testDecodeFromStringWithCustomDelimiter() throws Exception {
        String csv = "name|email\nJohn|john@example.org\nJean|jean@example.org\nBill|bill@example.org";

        IDataCSVParser parser = new IDataCSVParser("|");
        IData document = parser.parse(csv);
        IDataCursor cursor = document.getCursor();
        IData[] records = IDataUtil.getIDataArray(cursor, "recordWithNoID");
        cursor.destroy();

        assertEquals(3, records.length);

        cursor = records[0].getCursor();
        assertEquals("John", IDataUtil.getString(cursor, "name"));
        assertEquals("john@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();

        cursor = records[1].getCursor();
        assertEquals("Jean", IDataUtil.getString(cursor, "name"));
        assertEquals("jean@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();

        cursor = records[2].getCursor();
        assertEquals("Bill", IDataUtil.getString(cursor, "name"));
        assertEquals("bill@example.org", IDataUtil.getString(cursor, "email"));
        cursor.destroy();
    }
}