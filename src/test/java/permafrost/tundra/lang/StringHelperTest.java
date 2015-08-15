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
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class StringHelperTest {

    @Test
    public void testSqueezeWithNull() throws Exception {
        assertEquals(null, StringHelper.squeeze(null));
    }

    @Test
    public void testSqueezeWithEmptyString() throws Exception {
        assertEquals(null, StringHelper.squeeze(""));
    }

    @Test
    public void testSqueezeWithWhitespace() throws Exception {
        assertEquals(null, StringHelper.squeeze("   "));
    }

    @Test
    public void testSqueezeWithLeadingTrailingAndInternalWhitespace() throws Exception {
        assertEquals("a b c", StringHelper.squeeze("  a   b  c  "));
        assertEquals("a   b  c", StringHelper.squeeze("  a   b  c  ", false));
    }

    @Test
    public void testPadWithNull() throws Exception {
        assertEquals("aaaaaaaaaa", StringHelper.pad(null, 10, 'a'));
        assertEquals("aaaaaaaaaa", StringHelper.pad(null, -10, 'a'));
        assertEquals("", StringHelper.pad(null, 0, 'a'));
    }

    @Test
    public void testPadWithStringLongerThanPadLength() throws Exception {
        assertEquals("abcdef", StringHelper.pad("abcdef", 2, 'a'));
    }

    @Test
    public void testPadWithStringShorterThanPadLength() throws Exception {
        assertEquals("ggggabcdef", StringHelper.pad("abcdef", 10, 'g'));
        assertEquals("abcdefgggg", StringHelper.pad("abcdef", -10, 'g'));
    }

    @Test
    public void testQuoteWithNull() throws Exception {
        assertEquals(null, StringHelper.quote((String)null));
        assertEquals(null, StringHelper.quote((String[])null));
    }

    @Test
    public void testQuoteWithReservedCharacters() throws Exception {
        assertTrue(StringHelper.match("$1.00", StringHelper.quote("$1.00")));
    }

    @Test
    public void testQuoteWithArray() throws Exception {
        String[] strings = { "$1.00", "$2.00" };
        assertTrue(StringHelper.match("$1.00", StringHelper.quote(strings)));
        assertTrue(StringHelper.match("$2.00", StringHelper.quote(strings)));
        assertTrue(!StringHelper.match("$3.00", StringHelper.quote(strings)));
    }

    @Test
    public void testLinesWithNull() throws Exception {
        assertArrayEquals(null, StringHelper.lines(null));
    }

    @Test
    public void testLinesWithSingleLine() throws Exception {
        assertArrayEquals(new String[] { "abc" }, StringHelper.lines("abc"));
    }

    @Test
    public void testLinesWithMultipleLines() throws Exception {
        assertArrayEquals(new String[] { "abc", "def" }, StringHelper.lines("abc\ndef"));
    }

    @Test
    public void testSplitWithNull() throws Exception {
        assertArrayEquals(null, StringHelper.split(null, null));
        assertArrayEquals(null, StringHelper.split(null, "a"));
        assertArrayEquals(new String[] { "a" }, StringHelper.split("a", null));
    }

    @Test
    public void testSplitWithLiteral() throws Exception {
        assertArrayEquals(new String[] { "a", "c", "d", "e" }, StringHelper.split("abcbdbe", "b", true));
    }

    @Test
    public void testSplitWithRegularExpression() throws Exception {
        assertArrayEquals(new String[] { "a", "", "be" }, StringHelper.split("abcbdbe", "(bc|bd)", false));
    }
}