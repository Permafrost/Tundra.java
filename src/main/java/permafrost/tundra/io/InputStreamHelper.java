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

package permafrost.tundra.io;

import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with InputStream objects.
 */
public final class InputStreamHelper {
    /**
     * Disallow instantiation of this class.
     */
    private InputStreamHelper() {}

    /**
     * Converts the given InputStream to a ByteArrayInputStream, unless it is already an instance of
     * ByteArrayInputStream in which case it is returned as is.
     *
     * @param inputStream   The stream to be memoized.
     * @return              The memoized stream.
     * @throws IOException  If an IO error occurs while reading the stream.
     */
    public static ByteArrayInputStream memoize(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;

        ByteArrayInputStream output;

        if (inputStream instanceof ByteArrayInputStream) {
            output = (ByteArrayInputStream)inputStream;
        } else {
            output = new ByteArrayInputStream(read(inputStream));
        }

        return output;
    }

    /**
     * Normalizes the given InputStream, by wrapping it in a BufferedInputStream where appropriate.
     *
     * @param inputStream   An InputStream to be normalized.
     * @return              The normalized InputStream.
     */
    public static InputStream normalize(InputStream inputStream) {
        if (inputStream == null) return null;

        if (!(inputStream instanceof FilterInputStream || inputStream instanceof ByteArrayInputStream)) {
            inputStream = new BufferedInputStream(inputStream, InputOutputHelper.DEFAULT_BUFFER_SIZE);
        }

        return inputStream;
    }

    /**
     * Converts the given string to an InputStream.
     *
     * @param string    A string to be converted.
     * @return          An InputStream representation of the given string.
     */
    public static InputStream normalize(String string) {
        return normalize(string, (Charset)null);
    }

    /**
     * Converts the given string to an InputStream.
     *
     * @param string    A string to be converted.
     * @param charset   The character set to use.
     * @return          An InputStream representation of the given string.
     */
    public static InputStream normalize(String string, Charset charset) {
        if (string == null) return null;
        return normalize(BytesHelper.normalize(string, CharsetHelper.normalize(charset)));
    }

    /**
     * Converts the given byte[] to an InputStream.
     *
     * @param bytes     A byte[] to be converted.
     * @return          An InputStream representation of the given byte[].
     */
    public static InputStream normalize(byte[] bytes) {
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Converts the given Object to an InputStream, if possible.
     *
     * @param object    A String, byte[], or InputStream object to be converted.
     * @return          An InputStream representation of the given object.
     */
    public static InputStream normalize(Object object) {
        return normalize(object, (Charset)null);
    }

    /**
     * Converts the given Object to an InputStream, if possible.
     *
     * @param object    A String, byte[], or InputStream object to be converted.
     * @param charset   The character to use.
     * @return          An InputStream representation of the given object.
     */
    public static InputStream normalize(Object object, Charset charset) {
        InputStream output = null;

        if (object instanceof InputStream) {
            output = normalize((InputStream) object);
        } else if (object instanceof byte[]) {
            output = normalize((byte[])object);
        } else if (object instanceof String) {
            output = normalize((String)object, charset);
        } else if (object != null){
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Reads all data from the given input stream, and then closes it when done.
     *
     * @param inputStream   An input stream containing data to be read.
     * @return              Returns a byte[] containing all the data read from the given inputStream.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public static byte[] read(InputStream inputStream) throws IOException {
        return read(inputStream, true);
    }

    /**
     * Reads all data from the given input stream, and optionally closes it when done.
     *
     * @param inputStream   An input stream containing data to be read.
     * @param close         When true the input stream will be closed when done.
     * @return              Returns a byte[] containing all the data read from the given inputStream.
     * @throws IOException  If there is a problem reading from the stream.
     */
    public static byte[] read(InputStream inputStream, boolean close) throws IOException {
        if (inputStream == null) return null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputOutputHelper.copy(inputStream, outputStream, close);
        return outputStream.toByteArray();
    }

    /**
     * Returns an InputStream which transcodes the character data in the given InputStream from one character set to
     * another.
     *
     * @param inputStream   The input stream to transcode.
     * @param sourceCharset The character set to decode the data from.
     * @param targetCharset The character set to transcode the data to.
     * @return              A new input stream which transcodes the given input stream.
     */
    public static InputStream transcode(InputStream inputStream, Charset sourceCharset, Charset targetCharset) {
        return new TranscodingInputStream(inputStream, sourceCharset, targetCharset);
    }
}
