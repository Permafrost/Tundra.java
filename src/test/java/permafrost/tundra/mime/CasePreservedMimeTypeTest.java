/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lachlan Dowding
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

package permafrost.tundra.mime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CasePreservedMimeTypeTest {
    String contentType;
    CasePreservedMimeType mimeType;

    @Before
    public void setUp() throws Exception {
        contentType = "PRIMARY/Sub";
        mimeType = new CasePreservedMimeType(contentType);
    }

    @Test
    public void testGetPrimaryType() throws Exception {
        assertEquals("PRIMARY", mimeType.getPrimaryType());
    }

    @Test
    public void testGetSubType() throws Exception {
        assertEquals("Sub", mimeType.getSubType());
    }

    @Test
    public void testBaseType() throws Exception {
        assertEquals(contentType, mimeType.getBaseType());
    }

    @Test
    public void testToString() throws Exception {
        assertEquals(contentType, mimeType.toString());
    }

    @Test
    public void testMatch() throws Exception {
        assertTrue(mimeType.match("primary/sub"));
        assertTrue(mimeType.match("Primary/Sub"));
        assertTrue(mimeType.match("primary/*"));
        assertTrue(mimeType.match("Primary/*"));
    }

    @Test
    public void testParameters() throws Exception {
        mimeType.setParameter("PARAMETER", "VALUE");
        assertEquals(contentType + "; parameter=VALUE", mimeType.toString());
    }
}