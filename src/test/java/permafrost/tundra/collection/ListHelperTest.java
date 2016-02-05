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

package permafrost.tundra.collection;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class ListHelperTest {

    @Test
    public void testPrepend() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");

        ListHelper.prepend(list, "b", "c");

        assertEquals(3, list.size());
        assertEquals("b", list.get(0));
        assertEquals("c", list.get(1));
        assertEquals("a", list.get(2));
    }

    @Test
    public void testInsertIndexZero() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");


        ListHelper.insert(list, 0, "c", "d", "e");

        assertEquals(5, list.size());
        assertEquals("c", list.get(0));
        assertEquals("d", list.get(1));
        assertEquals("e", list.get(2));
        assertEquals("a", list.get(3));
        assertEquals("b", list.get(4));
    }

    @Test
    public void testInsertReverseIndexMinusOne() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");


        ListHelper.insert(list, -1, "c", "d", "e");

        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
        assertEquals("d", list.get(3));
        assertEquals("e", list.get(4));
    }

    @Test
    public void testInsertReverseIndexMinusTwo() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");


        ListHelper.insert(list, -2, "c", "d", "e");

        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals("c", list.get(1));
        assertEquals("d", list.get(2));
        assertEquals("e", list.get(3));
        assertEquals("b", list.get(4));
    }

    @Test
    public void testInsertOutsideRange() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");


        ListHelper.insert(list, 4, "b");

        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals(null, list.get(1));
        assertEquals(null, list.get(2));
        assertEquals(null, list.get(3));
        assertEquals("b", list.get(4));
    }

    @Test
    public void testInsertReverseFill() throws Exception {
        List<String> list = new ArrayList<String>();
        ListHelper.insert(list, -4, "a");

        assertEquals(5, list.size());
        assertEquals("a", list.get(0));
        assertEquals(null, list.get(1));
        assertEquals(null, list.get(2));
        assertEquals(null, list.get(3));
        assertEquals(null, list.get(4));
    }
}