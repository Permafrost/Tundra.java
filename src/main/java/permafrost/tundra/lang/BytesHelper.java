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

import permafrost.tundra.io.InputStreamHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with byte[] objects.
 */
public final class BytesHelper {
    /**
     * Disallow instantiation of this class.
     */
    private BytesHelper() {}

    /**
     * Converts the given String to an byte[].
     *
     * @param string A String to be converted to a byte[].
     * @return A byte[] representation of the given String.
     */
    public static byte[] normalize(String string) {
        return normalize(string, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given String to an byte[] using the given character encoding set.
     *
     * @param string      A string to be converted to a byte[].
     * @param charsetName The character encoding set to use.
     * @return A byte[] representation of the given String.
     */
    public static byte[] normalize(String string, String charsetName) {
        return normalize(string, CharsetHelper.normalize(charsetName));
    }

    /**
     * Converts the given String to an byte[] using the given character encoding set.
     *
     * @param string  A string to be converted to a byte[].
     * @param charset The character encoding set to use.
     * @return A byte[] representation of the given String.
     */
    public static byte[] normalize(String string, Charset charset) {
        if (string == null) return null;
        return string.getBytes(CharsetHelper.normalize(charset));
    }

    /**
     * Converts the given java.io.InputStream to a byte[] by reading all data from the stream and then closing the
     * stream.
     *
     * @param inputStream A java.io.InputStream to be converted to a byte[]
     * @return A byte[] representation of the given java.io.InputStream.
     * @throws IOException If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(InputStream inputStream) throws IOException {
        return InputStreamHelper.read(inputStream);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     *
     * @param object The object to be normalized to a byte[].
     * @return A byte[] representation of the given object.
     * @throws IOException If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object) throws IOException {
        return normalize(object, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     *
     * @param object      The object to be normalized to a string.
     * @param charsetName The character set to use.
     * @return A byte[] representation of the given object.
     * @throws IOException If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object, String charsetName) throws IOException {
        return normalize(object, CharsetHelper.normalize(charsetName));
    }

    /**
     * Normalizes the given String, byte[], or java.io.InputStream object to a byte[].
     *
     * @param object  The object to be normalized to a string.
     * @param charset The character set to use.
     * @return A byte[] representation of the given object.
     * @throws IOException If there is a problem reading from the java.io.InputStream.
     */
    public static byte[] normalize(Object object, Charset charset) throws IOException {
        if (object == null) return null;

        charset = CharsetHelper.normalize(charset);

        byte[] output;

        if (object instanceof byte[]) {
            output = (byte[])object;
        } else if (object instanceof String) {
            output = normalize((String)object, charset);
        } else if (object instanceof InputStream) {
            output = normalize((InputStream)object);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Encodes binary data as a base64-encoded string.
     *
     * @param bytes Binary data to be base64-encoded.
     * @return The given data as a base64-encoded string.
     */
    public static String base64Encode(byte[] bytes) {
        if (bytes == null) return null;
        return javax.xml.bind.DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * Decodes a base64-encoded string to binary data.
     *
     * @param string A base64-encoded string.
     * @return The base64-encoded string decoded to binary data.
     */
    public static byte[] base64Decode(String string) {
        if (string == null) return null;
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(string);
    }

    /**
     * Encodes binary data as a hex-encoded string.
     *
     * @param bytes Binary data to be hex-encoded.
     * @return The given data as a hex-encoded string.
     */
    public static String hexEncode(byte[] bytes) {
        if (bytes == null) return null;
        return javax.xml.bind.DatatypeConverter.printHexBinary(bytes);
    }

    /**
     * Decodes a hex-encoded string to binary data.
     *
     * @param string A hex-encoded string.
     * @return The hex-encoded string decoded to binary data.
     */
    public static byte[] hexDecode(String string) {
        if (string == null) return null;
        return javax.xml.bind.DatatypeConverter.parseHexBinary(string);
    }
}
