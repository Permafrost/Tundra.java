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

/**
 * List of supported message digest algorithms.
 */
public enum MessageDigestAlgorithm {
    MD2("MD2"), MD5("MD5"), SHA_1("SHA-1"), SHA_256("SHA-256"), SHA_384("SHA-384"), SHA_512("SHA-512");

    private String name;
    private static java.util.Map<String, MessageDigestAlgorithm> map = new java.util.HashMap<String, MessageDigestAlgorithm>();

    static {
        for (MessageDigestAlgorithm algorithm : MessageDigestAlgorithm.values()) {
            map.put(algorithm.name.toLowerCase(), algorithm);
        }
    }

    MessageDigestAlgorithm(String input) {
        name = input;
    }

    /**
     * Returns the MessageDigestAlgorithm for the given algorithm name.
     *
     * @param name The name of the algorithm to return.
     * @return The MessageDigestAlgorithm for the given algorithm name.
     */
    public static MessageDigestAlgorithm normalize(String name) {
        MessageDigestAlgorithm algorithm = null;
        if (name != null) {
            algorithm = map.get(name.toLowerCase());
        }
        return normalize(algorithm);
    }

    /**
     * Returns a normalized MessageDigestAlgorithm.
     *
     * @param algorithm The algorithm to normalize.
     * @return If the given algorithm is null then SHA_256 is returned, otherwise the given algorithm is returned.
     */
    public static MessageDigestAlgorithm normalize(MessageDigestAlgorithm algorithm) {
        return algorithm == null ? getDefault() : algorithm;
    }

    /**
     * Returns the MessageDigestAlgorithm's algorithm name.
     *
     * @return The MessageDigestAlgorithm's algorithm name.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the default MessageDigestAlgorithm.
     *
     * @return The default MessageDigestAlgorithm.
     */
    public static MessageDigestAlgorithm getDefault() {
        return SHA_256;
    }
}
