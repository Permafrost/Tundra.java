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

package permafrost.tundra.bytes;

import permafrost.tundra.exception.BaseException;
import permafrost.tundra.exception.ExceptionHelper;
import permafrost.tundra.string.StringHelper;
import permafrost.tundra.io.StreamHelper;

public class BytesHelper {
    /**
     * Disallow instantiation of this class.
     */
    private BytesHelper() {}

    /**
     * Normalizes the given byte[].
     *
     * @param in A byte[] to be normalized.
     * @return   The given byte[] returned unchanged.
     */
    public static byte[] normalize(byte[] in) {
        return in;
    }
    /**
     * Converts the given String to an byte[].
     *
     * @param in A String to be converted to a byte[].
     * @return   A byte[] representation of the given String.
     * @throws BaseException
     */
    public static byte[] normalize(java.lang.String in) throws BaseException {
        return normalize(in, null);
    }

    /**
     * Converts the given String to an byte[] using the given character encoding set.
     * @param in    A string to be converted to a byte[].
     * @param encoding The character encoding set to use.
     * @return         A byte[] representation of the given String.
     * @throws BaseException
     */
    public static byte[] normalize(java.lang.String in, java.lang.String encoding) throws BaseException {
        byte[] out = null;
        try {
            out = in.getBytes(encoding == null ? StringHelper.DEFAULT_CHARACTER_ENCODING : encoding);
        } catch(java.io.UnsupportedEncodingException ex) {
            ExceptionHelper.raise(ex);
        }
        return out;
    }

    /**
     * Converts the given java.io.InputStream to a byte[] by reading all
     * data from the stream and then closing the stream.
     *
     * @param in A java.io.InputStream to be converted to a byte[]
     * @return   A byte[] representation of the given java.io.InputStream.
     * @throws BaseException
     */
    public static byte[] normalize(java.io.InputStream in) throws BaseException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        StreamHelper.copy(in, out);
        return out.toByteArray();
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     * @param object The object to be normalized to a byte[].
     * @return       A byte[] representation of the given object.
     * @throws BaseException
     */
    public static byte[] normalize(Object object) throws BaseException {
        return normalize(object, null);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     * @param object The object to be normalized to a string.
     * @param encoding  The character set to use.
     * @return       A byte[] representation of the given object.
     * @throws BaseException
     */
    public static byte[] normalize(Object object, String encoding) throws BaseException {
        if (object == null) return null;

        byte[] output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object);
        } else if (object instanceof String) {
            output = normalize((String)object, encoding);
        } else if (object instanceof java.io.InputStream) {
            output = normalize((java.io.InputStream)object);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Encodes binary data as a base64-encoded string.
     *
     * @param input Binary data to be base64-encoded.
     * @return The given data as a base64-encoded string.
     */
    public static String base64Encode(byte[] input) {
        return input == null ? null : javax.xml.bind.DatatypeConverter.printBase64Binary(input);
    }

    /**
     * Decodes a base64-encoded string to binary data.
     *
     * @param input A base64-encoded string.
     * @return The base64-encoded string decoded to binary data.
     */
    public static byte[] base64Decode(String input) {
        return input == null ? null : javax.xml.bind.DatatypeConverter.parseBase64Binary(input);
    }
}
