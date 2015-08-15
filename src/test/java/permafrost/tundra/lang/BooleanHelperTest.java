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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BooleanHelperTest {

    @Test
    public void testParseWithNullArgument() throws Exception {
        assertEquals(false, BooleanHelper.parse(null));
        assertEquals(false, BooleanHelper.parse(null, false));
        assertEquals(true, BooleanHelper.parse(null, true));
        assertEquals(false, BooleanHelper.parse(null, "false"));
        assertEquals(true, BooleanHelper.parse(null, "true"));
    }

    @Test
    public void testParseWithZeroAndOne() throws Exception {
        assertEquals(false, BooleanHelper.parse("0"));
        assertEquals(true, BooleanHelper.parse("1"));
    }

    @Test
    public void testParseWithTrueAndFalse() throws Exception {
        assertEquals(false, BooleanHelper.parse("false"));
        assertEquals(true, BooleanHelper.parse("true"));
    }

    @Test
    public void testParseIgnoresCase() throws Exception {
        assertEquals(false, BooleanHelper.parse("FALSE"));
        assertEquals(true, BooleanHelper.parse("TrUe"));
    }

    @Test
    public void testParseIgnoresLeadingAndTrailingWhitespace() throws Exception {
        assertEquals(false, BooleanHelper.parse("  false  "));
        assertEquals(true, BooleanHelper.parse("  TrUe    \n"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithUnparseableArgument() throws Exception {
        assertEquals(true, BooleanHelper.parse("this is a test"));
    }

    @Test
    public void testParseWithCustomValues() throws Exception {
        assertEquals(true, BooleanHelper.parse("abc", "abc", "def"));
        assertEquals(false, BooleanHelper.parse("def", "abc", "def"));
        assertEquals(false, BooleanHelper.parse(null, "abc", "def"));
        assertEquals(true, BooleanHelper.parse(null, "abc", "def", true));
        assertEquals(false, BooleanHelper.parse(null, "abc", "def", false));
        assertEquals(true, BooleanHelper.parse(null, "abc", "def", "true"));
        assertEquals(false, BooleanHelper.parse(null, "abc", "def", "false"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithUnparseableStringAndCustomValues() throws Exception {
        BooleanHelper.parse("def", "abc", null);
    }

    @Test
    public void testParseWithCustomTrueValue() throws Exception {
        assertEquals(true, BooleanHelper.parse("abc", "abc", null));
    }

    @Test
    public void testNormalize() throws Exception {
        assertEquals("false", BooleanHelper.normalize(null));
        assertEquals("false", BooleanHelper.normalize("0"));
        assertEquals("false", BooleanHelper.normalize("false"));
        assertEquals("false", BooleanHelper.normalize("FaLSe"));
        assertEquals("false", BooleanHelper.normalize("  false  "));
        assertEquals("true", BooleanHelper.normalize("1"));
        assertEquals("true", BooleanHelper.normalize("true"));
        assertEquals("true", BooleanHelper.normalize("TrUe"));
        assertEquals("true", BooleanHelper.normalize("  true  "));
    }

    @Test
    public void testNormalizeWithDefaultValue() throws Exception {
        assertEquals("false", BooleanHelper.normalize(null, "0"));
        assertEquals("false", BooleanHelper.normalize(null, "false"));
        assertEquals("false", BooleanHelper.normalize(null, "FaLSe"));
        assertEquals("false", BooleanHelper.normalize(null, "  false  "));
        assertEquals("true", BooleanHelper.normalize(null, "1"));
        assertEquals("true", BooleanHelper.normalize(null, "true"));
        assertEquals("true", BooleanHelper.normalize(null, "TrUe"));
        assertEquals("true", BooleanHelper.normalize(null, "  true  "));
    }

    @Test
    public void testEmit() throws Exception {
        assertEquals("true", BooleanHelper.emit(true));
        assertEquals("false", BooleanHelper.emit(false));
    }

    @Test
    public void testEmitWithCustomValues() throws Exception {
        assertEquals("Y", BooleanHelper.emit(true, "Y", "N"));
        assertEquals("N", BooleanHelper.emit(false, "Y", "N"));
    }

    @Test
    public void testNegate() throws Exception {
        assertEquals(false, BooleanHelper.negate(true));
        assertEquals(true, BooleanHelper.negate(false));
    }

    @Test
    public void testNegateWithCustomValues() throws Exception {
        assertEquals("N", BooleanHelper.negate("true", "Y", "N"));
        assertEquals("Y", BooleanHelper.negate("false", "Y", "N"));
    }

    @Test
    public void testNegateWithString() throws Exception {
        assertEquals("false", BooleanHelper.negate("true"));
        assertEquals("true", BooleanHelper.negate("false"));
    }

    @Test
    public void testFormat() throws Exception {
        assertEquals("T", BooleanHelper.format("Y", "Y", "N", "T", "F"));
        assertEquals("F", BooleanHelper.format("N", "Y", "N", "T", "F"));
    }
}