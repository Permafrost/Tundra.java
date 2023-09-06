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

import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.io.NullOutputStream;
import permafrost.tundra.lang.BytesHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.Map;

/**
 * Convenience methods for cryptographic message digests.
 */
public final class MessageDigestHelper {
    /**
     * The default message digest algorithm name.
     */
    public static final String DEFAULT_ALGORITHM_NAME = "SHA-512";
    /**
     * The default message digest algorithm.
     */
    public static final MessageDigest DEFAULT_ALGORITHM = getDefault();

    /**
     * Disallow instantiation of this class.
     */
    private MessageDigestHelper() {}

    /**
     * Returns the default MessageDigest algorithm.
     *
     * @return The default MessageDigest algorithm.
     */
    private static MessageDigest getDefault() {
        MessageDigest messageDigest;

        try {
            messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM_NAME);
        } catch(NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }

        return messageDigest;
    }

    /**
     * Returns a MessageDigest object for the given named algorithm.
     *
     * @param algorithmName             The algorithm to use when calculating a message digest.
     * @return                          A MessageDigest that implements the given algorithm.
     * @throws NoSuchAlgorithmException If there is no provider for the named algorithm.
     */
    public static MessageDigest normalize(String algorithmName) throws NoSuchAlgorithmException {
        return algorithmName == null ? DEFAULT_ALGORITHM : MessageDigest.getInstance(algorithmName);
    }

    /**
     * Returns either the given algorithm if not null, or the default algorithm if given null.
     *
     * @param algorithm                 The algorithm to be normalized.
     * @return                          Either the given algorithm if not null, or the default algorithm.
     * @throws NoSuchAlgorithmException If there is no provider for the default algorithm.
     */
    public static MessageDigest normalize(MessageDigest algorithm) throws NoSuchAlgorithmException {
        return algorithm == null ? DEFAULT_ALGORITHM : algorithm;
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     *
     * @param algorithm                 The algorithm to use when calculating the message digest.
     * @param data                      The data to calculate the digest for.
     * @param charset                   The character set used to encode the text when data is provided as a string.
     * @return                          The message digest calculated for the given data using the given algorithm.
     * @throws IOException              If an I/O exception occurs reading from the stream.
     * @throws NoSuchAlgorithmException If there is no provider for the default algorithm.
     */
    @SuppressWarnings("unchecked")
    public static <T> Map.Entry<T, byte[]> digest(MessageDigest algorithm, T data, Charset charset) throws IOException, NoSuchAlgorithmException {
        Map.Entry<T, byte[]> output;

        if (data instanceof String) {
            byte[] digest = digest(algorithm, (String)data, charset);
            output = new AbstractMap.SimpleImmutableEntry<T, byte[]>(data, digest);
        } else if (data instanceof byte[]) {
            byte[] digest = digest(algorithm, (byte[])data);
            output = new AbstractMap.SimpleImmutableEntry<T, byte[]>(data, digest);
        } else if (data instanceof InputStream) {
            output = (Map.Entry<T, byte[]>)digest(algorithm, (InputStream)data);
        } else if (data == null) {
            throw new NullPointerException("data must not be null");
        } else {
            throw new IllegalArgumentException("data class not supported: " + data.getClass().getName() + " (only byte[], java.lang.String, or java.io.InputStream data is supported)");
        }

        return output;
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     *
     * @param algorithm                 The algorithm to use when calculating the message digest.
     * @param data                      The data to calculate the digest for.
     * @return                          The message digest calculated for the given data using the given algorithm.
     * @throws IOException              If an I/O exception occurs reading from the stream.
     * @throws NoSuchAlgorithmException If there is no provider for the default algorithm.
     */
    public static Map.Entry<InputStream, byte[]> digest(MessageDigest algorithm, InputStream data) throws IOException, NoSuchAlgorithmException {
        if (data == null) return null;

        // wrap the data in a digest input stream
        DigestInputStream digestInputStream = new DigestInputStream(data, normalize(algorithm));
        // generating the digest requires first reading the entire stream, so read in full then reset it
        data = InputStreamHelper.readFullyThenReset(digestInputStream, new NullOutputStream());
        // calculate the digest after reading the stream
        byte[] digest = digestInputStream.getMessageDigest().digest();
        // turn off the digest function now that its complete
        digestInputStream.on(false);

        return new AbstractMap.SimpleImmutableEntry<InputStream, byte[]>(data, digest);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     *
     * @param algorithm                 The algorithm to use when calculating the message digest.
     * @param data                      The data to calculate the digest for.
     * @return                          The message digest calculated for the given data using the given algorithm.
     * @throws NoSuchAlgorithmException If there is no provider for the default algorithm.
     */
    public static byte[] digest(MessageDigest algorithm, byte[] data) throws NoSuchAlgorithmException {
        return data == null ? null : normalize(algorithm).digest(data);
    }

    /**
     * Calculates a message digest for the given data using the given algorithm.
     *
     * @param algorithm                 The algorithm to use when calculating the message digest.
     * @param data                      The data to calculate the digest for.
     * @param charset                   The character set to use when encoding the text in data.
     * @return                          The message digest calculated for the given data using the given algorithm.
     * @throws NoSuchAlgorithmException If there is no provider for the default algorithm.
     */
    public static byte[] digest(MessageDigest algorithm, String data, Charset charset) throws NoSuchAlgorithmException {
        return digest(algorithm, BytesHelper.normalize(data, charset));
    }
}
