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

package permafrost.tundra.lang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ArrayHelperTest {
    @Test
    public void testFillWithIndexAndLengthInRange() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, item, item, null, null, null };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, 5, 2));
    }

    @Test
    public void testFillWithLengthOutOfRange() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, item, item, item, item, item };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, 5, 100));
    }

    @Test
    public void testFillWithIndexOutOfRange() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, null, null, null, null, null };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, 100, 100));
    }

    @Test
    public void testFillWithNegativeIndex() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, null, null, item, item, null };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, -3, 2));
    }

    @Test
    public void testFillWithNegativeLength() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, null, null, null, null, null };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, 5, -2));
    }

    @Test
    public void testFillWithZeroLength() throws Exception {
        String item = "test";
        String[] expected = { null, null, null, null, null, null, null, null, null, null };
        String[] actual = new String[10];

        assertArrayEquals(expected, ArrayHelper.fill(actual, item, 5, 0));
    }

    @Test
    public void testToStringArrayWithNull() throws Exception {
        assertArrayEquals(null, ArrayHelper.toStringArray(null));
    }

    @Test
    public void testToStringArrayWithIntegerArray() throws Exception {
        Integer[] array = new Integer[2];
        array[0] = new Integer(1);
        array[1] = new Integer(2);

        assertArrayEquals(new String[] { "1", "2" }, ArrayHelper.toStringArray(array));
    }

    @Test
    public void testToStringTableWithNull() throws Exception {
        assertArrayEquals(null, ArrayHelper.toStringTable(null));
    }

    @Test
    public void testToStringTableWithIntegerTable() throws Exception {
        Integer[][] table = new Integer[2][];

        table[0] = new Integer[2];
        table[1] = new Integer[2];

        table[0][0] = new Integer("1");
        table[0][1] = new Integer("2");
        table[1][0] = new Integer("3");
        table[1][1] = new Integer("4");

        assertArrayEquals(new String[][] { { "1", "2" }, { "3", "4" } }, ArrayHelper.toStringTable(table));
    }

    @Test
    public void testStringifyWithNull() throws Exception {
        assertEquals(null, ArrayHelper.stringify(null));
    }

    @Test
    public void testStringifyWithIntegerArray() throws Exception {
        Integer[] array = new Integer[2];
        array[0] = new Integer(1);
        array[1] = new Integer(2);

        assertEquals("[1, 2]", ArrayHelper.stringify(array));
    }

    @Test
    public void testStringifyWithIntegerTable() throws Exception {
        Integer[][] table = new Integer[2][];

        table[0] = new Integer[2];
        table[1] = new Integer[2];

        table[0][0] = new Integer("1");
        table[0][1] = new Integer("2");
        table[1][0] = new Integer("3");
        table[1][1] = new Integer("4");

        assertEquals("[[1, 2], [3, 4]]", ArrayHelper.stringify(table));
    }

    @Test
    public void testEqualReturnsTrue() throws Exception {
        String[] array1 = { "a", "b", "c" };
        String[] array2 = { "a", "b", "c" };

        assertEquals(true, ArrayHelper.equal(array1, array2));
    }

    @Test
    public void testEqualReturnsFalse() throws Exception {
        String[] array1 = { "a", "b", "c" };
        String[] array2 = { "a", "b", "d" };

        assertEquals(false, ArrayHelper.equal(array1, array2));
    }

    @Test
    public void testForwardSlice() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "b", "c" };

        String[] actual = ArrayHelper.slice(array1, 1, 2);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testForwardSliceLengthPastEnd() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "b", "c", "d", "e" };

        String[] actual = ArrayHelper.slice(array1, 1, 20);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testForwardSliceEntireArray() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "a", "b", "c", "d", "e" };

        String[] actual = ArrayHelper.slice(array1, 0, 5);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testForwardSliceIndexPastEnd() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { };

        String[] actual = ArrayHelper.slice(array1, 5, 10);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testReverseSlice() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "c", "d" };

        String[] actual = ArrayHelper.slice(array1, 3, -2);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testForwardSliceWithNegativeIndex() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "c", "d" };

        String[] actual = ArrayHelper.slice(array1, -3, 2);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testReverseSliceWithNegativeIndex() throws Exception {
        String[] array1 = { "a", "b", "c", "d", "e" };
        String[] expected = { "c", "d" };

        String[] actual = ArrayHelper.slice(array1, -2, -2);

        assertArrayEquals(expected, actual);
    }
}