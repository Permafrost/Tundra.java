/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Lachlan Dowding
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

import permafrost.tundra.io.StreamHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StringHelper {
    /**
     * The default character set used by Tundra.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    /**
     * The default character set name used by Tundra.
     */
    public static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name();

    /**
     * Disallow instantiation of this class.
     */
    private StringHelper() {}

    /**
     * Normalizes the given byte[] as a string.
     * @param bytes          A byte[] to be converted to a string.
     * @return               A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes) {
        return normalize(bytes, DEFAULT_CHARSET);
    }

    /**
     * Converts the given byte[] as a string.
     * @param bytes       A byte[] to be converted to a string.
     * @param charsetName The character set name to use.
     * @return            A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes, String charsetName) {
        return normalize(bytes, Charset.forName(charsetName));
    }

    /**
     * Converts the given byte[] as a string.
     * @param bytes     A byte[] to be converted to a string.
     * @param charset   The character set to use.
     * @return          A string representation of the given byte[].
     */
    public static String normalize(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     * @param inputStream       A java.io.InputStream to be converted to a string.
     * @return                  A string representation of the given java.io.InputStream.
     * @throws IOException      If the given encoding is unsupported, or if
     *                          there is an error reading from the java.io.InputStream.
     */
    public static String normalize(InputStream inputStream) throws IOException {
        return normalize(inputStream, DEFAULT_CHARSET);
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     * @param inputStream       A java.io.InputStream to be converted to a string.
     * @param charsetName       The character set to use.
     * @return                  A string representation of the given java.io.InputStream.
     * @throws IOException      If the given encoding is unsupported, or if
     *                          there is an error reading from the java.io.InputStream.
     */
    public static String normalize(InputStream inputStream, String charsetName) throws IOException {
        return normalize(inputStream, charsetName);
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     * @param inputStream       A java.io.InputStream to be converted to a string.
     * @param charset           The character set to use.
     * @return                  A string representation of the given java.io.InputStream.
     * @throws IOException      If there is an error reading from the java.io.InputStream.
     */
    public static String normalize(InputStream inputStream, Charset charset) throws IOException {
        java.io.Writer writer = new java.io.StringWriter();
        StreamHelper.copy(new java.io.InputStreamReader(StreamHelper.normalize(inputStream), charset), writer);
        return writer.toString();
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     * @param object            The object to be normalized to a string.
     * @return                  A string representation of the given object.
     * @throws IOException      If the given encoding is unsupported, or if
     *                          there is an error reading from the java.io.InputStream.
     */
    public static String normalize(Object object) throws IOException {
        return normalize(object, DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     * @param object            The object to be normalized to a string.
     * @param charsetName       The character set to use.
     * @return                  A string representation of the given object.
     * @throws IOException      If the given encoding is unsupported, or if
     *                          there is an error reading from the java.io.InputStream.
     */
    public static String normalize(Object object, String charsetName) throws IOException {
        return normalize(object, Charset.forName(charsetName));
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     * @param object            The object to be normalized to a string.
     * @param charset           The character set to use.
     * @return                  A string representation of the given object.
     * @throws IOException      If there is an error reading from the java.io.InputStream.
     */
    public static String normalize(Object object, Charset charset) throws IOException {
        if (object == null) return null;

        String output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object, charset);
        } else if (object instanceof String) {
            output = (String)object;
        } else if (object instanceof InputStream) {
            output = normalize((InputStream)object, charset);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Returns a substring starting at the given index for the given length.
     *
     * @param input     The string to be sliced.
     * @param index     The zero-based starting index of the slice.
     * @param length    The length in characters of the slice.
     * @return          The resulting substring.
     */
    public static String slice(String input, int index, int length) {
        if (input == null || input.equals("")) return input;

        String output = "";
        int inputLength = input.length(), endIndex = 0;

        // support reverse length
        if (length < 0) {
            // support reverse indexing
            if (index < 0) {
                endIndex = index + inputLength + 1;
            } else {
                if (index >= inputLength) index = inputLength - 1;
                endIndex = index + 1;
            }
            index = endIndex + length;
        } else {
            // support reverse indexing
            if (index < 0) index += inputLength;
            endIndex = index + length;
        }

        if (index < inputLength && endIndex > 0) {
            if (index < 0) index = 0;
            if (endIndex > inputLength) endIndex = inputLength;

            output = input.substring(index, endIndex);
        }

        return output;
    }
}
