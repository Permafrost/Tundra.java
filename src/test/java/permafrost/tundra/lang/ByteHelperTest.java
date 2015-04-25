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

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteHelperTest {
    @Test(expected = IllegalArgumentException.class)
    public void testNormalizeWithObject() throws Exception {
        ByteHelper.normalize(new Object());
    }

    @Test
    public void testNormalizeWithString() throws Exception {
        String message = "test";
        assertArrayEquals(message.getBytes(CharsetHelper.DEFAULT_CHARSET_NAME), ByteHelper.normalize(message));
    }

    @Test
    public void testNormalizeWithStringAndEncoding() throws Exception {
        String message = "test";
        String encoding = "UTF-16";
        assertArrayEquals(message.getBytes(encoding), ByteHelper.normalize(message, encoding));
    }

    @Test
    public void testNormalizeWithInputStream() throws Exception {
        String message = "test";
        java.io.InputStream in = new java.io.ByteArrayInputStream(message.getBytes(CharsetHelper.DEFAULT_CHARSET_NAME));
        assertArrayEquals(message.getBytes(CharsetHelper.DEFAULT_CHARSET_NAME), ByteHelper.normalize(in));
    }

    @Test
    public void testNormalizeWithBytes() throws Exception {
        String message = "test";
        assertArrayEquals(message.getBytes(CharsetHelper.DEFAULT_CHARSET_NAME), ByteHelper.normalize(message.getBytes(CharsetHelper.DEFAULT_CHARSET_NAME)));
    }

    @Test
    public void testBase64Encode() throws Exception {
        assertEquals("test", new String(ByteHelper.base64Decode("dGVzdA=="), "UTF-8"));
    }

    @Test
    public void testBase64Decode() throws Exception {
        assertEquals("dGVzdA==", ByteHelper.base64Encode("test".getBytes("UTF-8")));
    }
}