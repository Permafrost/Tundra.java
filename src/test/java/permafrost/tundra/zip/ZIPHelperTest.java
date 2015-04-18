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

import org.junit.Test;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.lang.StringHelper;

import static org.junit.Assert.*;

public class ZIPHelperTest {

    @Test
    public void testCompress() throws Exception {
        ZipEntryWithData[] contents = new ZipEntryWithData[2];
        contents[0] = new ZipEntryWithData("test1.txt", "this is the first zip entry");
        contents[1] = new ZipEntryWithData("test2.txt", "this is the second zip entry");
        byte[] compressedContent = BytesHelper.normalize(ZIPHelper.compress(contents));

        assertNotNull("compressed content not null", compressedContent);
        assertTrue("compressed length > 0", compressedContent.length > 0);
    }

    @Test
    public void testDecompress() throws Exception {
        byte[] compressedContent = BytesHelper.base64Decode("UEsDBBQACAgIAAizkkYAAAAAAAAAAAAAAAAJAAAAdGVzdDEudHh0K8nILFYAopKMVIW0zKLiEoWqzAKF1LySokoAUEsHCMvYnGgbAAAAGwAAAFBLAwQUAAgICAAIs5JGAAAAAAAAAAAAAAAACQAAAHRlc3QyLnR4dCvJyCxWAKKSjFSF4tTk/LwUharMAoXUvJKiSgBQSwcInFXy8BwAAAAcAAAAUEsBAhQAFAAICAgACLOSRsvYnGgbAAAAGwAAAAkAAAAAAAAAAAAAAAAAAAAAAHRlc3QxLnR4dFBLAQIUABQACAgIAAizkkacVfLwHAAAABwAAAAJAAAAAAAAAAAAAAAAAFIAAAB0ZXN0Mi50eHRQSwUGAAAAAAIAAgBuAAAApQAAAAAA");
        ZipEntryWithData[] contents = ZIPHelper.decompress(compressedContent);

        assertNotNull(contents);
        assertEquals("contents.length == 2", 2, contents.length);
        assertEquals("test1.txt", contents[0].getName());
        assertEquals("test2.txt", contents[1].getName());
        assertEquals("this is the first zip entry", StringHelper.normalize(contents[0].getData()));
        assertEquals("this is the second zip entry", StringHelper.normalize(contents[1].getData()));
    }
}