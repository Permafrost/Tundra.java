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

package permafrost.tundra.string;

import permafrost.tundra.exception.BaseException;
import permafrost.tundra.io.EncodingException;
import permafrost.tundra.io.StreamHelper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class StringHelper {
    /**
     * The default character encoding used by Tundra.
     */
    public static final String DEFAULT_CHARACTER_ENCODING = Charset.forName("UTF-8").name();

    /**
     * Disallow instantiation of this class.
     */
    private StringHelper() {}

    /**
     * Normalizes the given String.
     *
     * @param in A string to be normalized.
     * @return   The given string returned unchanged.
     */
    public static String normalize(String in) {
        return in;
    }

    /**
     * Normalizes the given byte[] as a string.
     *
     * @param bytes A byte[] to be converted to a string.
     * @return      A string representation of the given byte[].
     * @throws BaseException
     */
    public static String normalize(byte[] bytes) throws BaseException {
        return normalize(bytes, null);
    }

    /**
     * Converts the given byte[] as a string.
     *
     * @param bytes     A byte[] to be converted to a string.
     * @param encoding  The character set to use.
     * @return          A string representation of the given byte[].
     * @throws BaseException
     */
    public static String normalize(byte[] bytes, String encoding) throws BaseException {
        String out = null;
        try {
            out = new String(bytes, encoding);
        } catch(UnsupportedEncodingException ex) {
            throw new EncodingException(ex);
        }
        return out;
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     *
     * @param in A java.io.InputStream to be converted to a string.
     * @return   A string representation of the given java.io.InputStream.
     * @throws BaseException
     */
    public static String normalize(java.io.InputStream in) throws BaseException {
        return normalize(in, null);
    }

    /**
     * Converts the given java.io.InputStream as a String, and closes the stream.
     *
     * @param in        A java.io.InputStream to be converted to a string.
     * @param encoding  The character set to use.
     * @return          A string representation of the given java.io.InputStream.
     * @throws BaseException
     */
    public static String normalize(java.io.InputStream in, String encoding) throws BaseException {
        java.io.Writer writer = new java.io.StringWriter();
        try {
            StreamHelper.copy(new java.io.InputStreamReader(StreamHelper.normalize(in), encoding), writer);
        } catch(UnsupportedEncodingException ex) {
            throw new EncodingException(ex);
        }
        return writer.toString();
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     * @param object The object to be normalized to a string.
     * @return       A string representation of the given object.
     * @throws BaseException
     */
    public static String normalize(Object object) throws BaseException {
        return normalize(object, null);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a String.
     * @param object The object to be normalized to a string.
     * @param encoding  The character set to use.
     * @return       A string representation of the given object.
     * @throws BaseException
     */
    public static String normalize(Object object, String encoding) throws BaseException {
        if (object == null) return null;

        String output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object, encoding);
        } else if (object instanceof String) {
            output = normalize((String)object);
        } else if (object instanceof java.io.InputStream) {
            output = normalize((java.io.InputStream)object, encoding);
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
