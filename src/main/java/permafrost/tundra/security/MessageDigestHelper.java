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

import permafrost.tundra.io.MarkableInputStream;
import permafrost.tundra.lang.ByteHelper;
import permafrost.tundra.lang.CharsetHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Map;

public class MessageDigestHelper {
    /**
     * Disallow instantiation of this class.
     */
    private MessageDigestHelper() {}

    /**
     * Returns a MessageDigest object for the given algorithm.
     * @param algorithmName The algorithm to use when calculating a message digest.
     * @return              A MessageDigest that implements the given algorithm.
     */
    private static MessageDigest getInstance(String algorithmName) {
        return getInstance(MessageDigestAlgorithm.normalize(algorithmName));
    }

    /**
     * Returns a MessageDigest object for the given algorithm.
     * @param algorithm     The algorithm to use when calculating a message digest.
     * @return              A MessageDigest that implements the given algorithm.
     */
    private static MessageDigest getInstance(MessageDigestAlgorithm algorithm) {
        try {
            return MessageDigest.getInstance(algorithm.toString());
        } catch(NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithmName The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     * @throws IOException  If an I/O exception occurs reading from the stream.
     */
    public static Map.Entry<InputStream, byte[]> getDigest(String algorithmName, InputStream data) throws IOException {
        return getDigest(MessageDigestAlgorithm.normalize(algorithmName), data);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithm     The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     * @throws IOException  If an I/O exception occurs reading from the stream.
     */
    public static Map.Entry<InputStream, byte[]> getDigest(MessageDigestAlgorithm algorithm, InputStream data) throws IOException {
        DigestInputStream digestInputStream = new DigestInputStream(data, getInstance(algorithm));
        MarkableInputStream markableInputStream = new MarkableInputStream(digestInputStream);
        byte[] digest = digestInputStream.getMessageDigest().digest();
        digestInputStream.on(false);

        return new AbstractMap.SimpleImmutableEntry<InputStream, byte[]>(markableInputStream, digest);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithmName The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(String algorithmName, byte[] data) {
        return getDigest(MessageDigestAlgorithm.normalize(algorithmName), data);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithm     The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(MessageDigestAlgorithm algorithm, byte[] data) {
        return getInstance(algorithm).digest(data);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithmName The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(String algorithmName, String data) {
        return getDigest(algorithmName, data, (Charset) null);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithmName The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @param charsetName   The charset to use.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(String algorithmName, String data, String charsetName) {
        return getDigest(algorithmName, data, CharsetHelper.normalize(charsetName));
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithmName The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @param charset       The charset to use.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(String algorithmName, String data, Charset charset) {
        return getDigest(MessageDigestAlgorithm.normalize(algorithmName), data, charset);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithm     The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(MessageDigestAlgorithm algorithm, String data) {
        return getDigest(algorithm, data, (Charset)null);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithm     The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @param charsetName   The charset to use.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(MessageDigestAlgorithm algorithm, String data, String charsetName) {
        return getDigest(algorithm, data, CharsetHelper.normalize(charsetName));
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     * @param algorithm     The algorithm to use when calculating the message digest.
     * @param data          The data to calculate the digest for.
     * @param charset       The charset to use.
     * @return              The message digest calculated for the given data using the given algorithm.
     */
    public static byte[] getDigest(MessageDigestAlgorithm algorithm, String data, Charset charset) {
        return getDigest(algorithm, ByteHelper.normalize(data, charset));
    }
}
