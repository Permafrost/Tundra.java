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

package permafrost.tundra.security;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.Test;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

public class MessageDigestHelperTest {
    byte[] sha256;
    byte[] data;
    MessageDigest algorithm;

    @Before
    public void setUp() throws Exception {
        data = "this is a test".getBytes();
        sha256 = BytesHelper.hexDecode("2e99758548972a8e8822ad47fa1017ff72f06f3ff6a016851f45c398732bc50c");
        algorithm = MessageDigestHelper.normalize("SHA-256");
    }

    @Test
    public void testGetDigestWithByteArrayInputStream() throws Exception {
        Map.Entry<? extends InputStream, byte[]> result = MessageDigestHelper.digest(algorithm, new ByteArrayInputStream(data));
        assertArrayEquals(sha256, result.getValue());

        InputStream inputStream = result.getKey();

        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));
        inputStream.reset();
        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));
        inputStream.reset();
        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));

        inputStream.close();
    }

    @Test
    public void testGetDigestWithBufferedInputStream() throws Exception {
        File tempFile = FileHelper.create();
        FileHelper.writeFromBytes(tempFile, data, false);

        Map.Entry<? extends InputStream, byte[]> result = MessageDigestHelper.digest(algorithm, new FileInputStream(tempFile));
        assertArrayEquals(sha256, result.getValue());

        InputStream inputStream = result.getKey();

        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));
        inputStream.reset();
        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));
        inputStream.reset();
        assertArrayEquals(data, StreamHelper.readToBytes(inputStream, false));

        inputStream.close();
    }

    @Test
    public void testGetDigestWithBytes() throws Exception {
        assertArrayEquals(sha256, MessageDigestHelper.digest(algorithm, data));
    }

    @Test
    public void testGetDigestWithString() throws Exception {
        assertArrayEquals(sha256, MessageDigestHelper.digest(algorithm, new String(data), CharsetHelper.DEFAULT_CHARSET));
    }
}
