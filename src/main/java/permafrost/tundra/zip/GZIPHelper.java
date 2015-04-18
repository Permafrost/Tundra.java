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

import permafrost.tundra.io.StreamHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A collection of convenience methods for working with GZIP compression.
 */
public class GZIPHelper {
    /**
     * Disallow instantiation of this class.
     */
    private GZIPHelper() {}

    /**
     * GZIP compresses the given data.
     * @param inputStream   The data to be compressed.
     * @return              The compressed data.
     * @throws IOException  If an I/O problem occurs when reading from the stream.
     */
    public static InputStream compress(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamHelper.copy(StreamHelper.normalize(inputStream), new GZIPOutputStream(byteArrayOutputStream));

        return StreamHelper.normalize(byteArrayOutputStream.toByteArray());
    }

    /**
     * GZIP decompresses the given data.
     * @param inputStream   The compressed data to be decompressed.
     * @return              The decompressed data.
     * @throws IOException  If an I/O problem occurs when reading from the stream.
     */
    public static InputStream decompress(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamHelper.copy(new GZIPInputStream(StreamHelper.normalize(inputStream)), byteArrayOutputStream);

        return StreamHelper.normalize(byteArrayOutputStream.toByteArray());
    }

}
