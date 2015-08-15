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

package permafrost.tundra.zip;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import permafrost.tundra.lang.BytesHelper;

public class GzipHelperTest {
    byte[] expectedCompressedContent, expectedDecompressedContent;

    @Before
    public void setUp() throws Exception {
        expectedCompressedContent = BytesHelper.base64Decode("H4sIAAAAAAAAAA3IwQkAIAwDwFWymmiwPqxisj8W7nWOJRSdTZgy+kkzDUczkhyq2vdRWjk/qJJnGjAAAAA=");
        expectedDecompressedContent = BytesHelper.base64Decode("dGhpcyBpcyBzb21lIHRlc3QgY29udGVudCB0aGF0IG5lZWRzIGNvbXByZXNzaW5n");
        ;
    }

    @Test
    public void testCompress() throws Exception {
        byte[] compressedContent = BytesHelper.normalize(GzipHelper.compress(expectedDecompressedContent));
        assertNotNull("compressed content not null", compressedContent);
        assertTrue("compressed length > 0", compressedContent.length > 0);
        assertNotEquals("decompressed content != compressed content", expectedDecompressedContent, BytesHelper.base64Encode(compressedContent));
        assertArrayEquals(expectedDecompressedContent, BytesHelper.normalize(GzipHelper.decompress(compressedContent)));
    }

    @Test
    public void testDecompress() throws Exception {
        byte[] decompressedContent = BytesHelper.normalize(GzipHelper.decompress(expectedCompressedContent));
        assertNotNull("decompressed content not null", decompressedContent);
        assertTrue("decompressed length > 0", decompressedContent.length > 0);
        assertArrayEquals(expectedDecompressedContent, decompressedContent);
    }
}