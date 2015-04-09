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

package permafrost.tundra.idata;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class IterableIDataTest {
    IData document;

    @Before
    public void setUp() throws Exception {
        document = IDataFactory.create();
        IDataCursor cursor = document.getCursor();
        IDataUtil.put(cursor, "a", "1");
        IDataUtil.put(cursor, "b", "2");
        IDataUtil.put(cursor, "c", "3");
        cursor.destroy();
    }

    @Test
    public void testHasNext() throws Exception {
        IDataIterator iterator = new IterableIData(document).iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        iterator.next();
        iterator.next();
        assertFalse(iterator.hasNext());
        iterator = new IterableIData(IDataFactory.create()).iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIteration() throws Exception {
        IDataIterator iterator = new IterableIData(document).iterator();
        assertTrue(iterator.hasNext());
        Map.Entry<String, Object> pair = iterator.next();
        assertEquals("a", pair.getKey());
        assertEquals("1", pair.getValue());
        assertTrue(iterator.hasNext());
        pair = iterator.next();
        assertEquals("b", pair.getKey());
        assertEquals("2", pair.getValue());
        assertTrue(iterator.hasNext());
        pair = iterator.next();
        assertEquals("c", pair.getKey());
        assertEquals("3", pair.getValue());
        assertFalse(iterator.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextAfterNoMoreElements() throws Exception {
        IDataIterator iterator = new IterableIData(document).iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        iterator.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveBeforeNext() throws Exception {
        IDataIterator iterator = new IterableIData(document).iterator();
        iterator.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAfterNext() throws Exception {
        IDataIterator iterator = new IterableIData(document).iterator();
        iterator.next();
        iterator.remove();
    }
}