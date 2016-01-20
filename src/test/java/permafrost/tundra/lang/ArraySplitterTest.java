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

package permafrost.tundra.lang;

import static org.junit.Assert.*;
import org.junit.Test;

public class ArraySplitterTest {
    @Test
    public void testCountWithinBounds() throws Exception {
        String[] array = new String[]  { "a", "b", "c", "d", "e", "f", "g" };

        ArraySplitter<String> arraySplitter = new ArraySplitter<String>(array, 3);

        assertArrayEquals(new String[] { "a", "b", "c" }, arraySplitter.getHead());
        assertArrayEquals(new String[] { "d", "e", "f", "g" }, arraySplitter.getTail());
    }

    @Test
    public void testCountBeyondBounds() throws Exception {
        String[] array = new String[]  { "a", "b", "c", "d", "e", "f", "g" };

        ArraySplitter<String> arraySplitter = new ArraySplitter<String>(array, 10);

        assertArrayEquals(new String[] { "a", "b", "c", "d", "e", "f", "g" }, arraySplitter.getHead());
        assertArrayEquals(new String[] { }, arraySplitter.getTail());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCount() throws Exception {
        String[] array = new String[]  { "a", "b", "c", "d", "e", "f", "g" };
        ArraySplitter<String> arraySplitter = new ArraySplitter<String>(array, -1);
    }

    @Test
    public void testZeroCount() throws Exception {
        String[] array = new String[]  { "a", "b", "c", "d", "e", "f", "g" };
        ArraySplitter<String> arraySplitter = new ArraySplitter<String>(array, 0);

        assertArrayEquals(new String[] { }, arraySplitter.getHead());
        assertArrayEquals(new String[] { "a", "b", "c", "d", "e", "f", "g" }, arraySplitter.getTail());
    }

    @Test(expected = NullPointerException.class)
    public void testNullArray() throws Exception {
        ArraySplitter<String> arraySplitter = new ArraySplitter<String>(null, 1);
    }
}