/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Lachlan Dowding
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

import permafrost.tundra.lang.AuthenticationException;
import permafrost.tundra.lang.CharsetHelper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Convenience methods for hash-based message authentication codes.
 */
public class HMACHelper {
    /**
     * Disallow instantiation of this class.
     */
    private HMACHelper() {}

    /**
     * Throws an AuthenticationException if the given authentication code does not match the calculated authentication
     * code.
     *
     * @param authenticationCode        The authentication code provided by an authenticating party.
     * @param content                   The content or payload or data being authenticated.
     * @param key                       The secret key agreed between the parties.
     * @param algorithm                 The HMAC algorithm name used to calculate the authentication code.
     * @throws AuthenticationException  If the given authentication code does not match the calculated code.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws NoSuchAlgorithmException If the given algorithm is invalid.
     */
    public static void authenticate(byte[] authenticationCode, byte[] content, String key, String algorithm) throws AuthenticationException, InvalidKeyException, NoSuchAlgorithmException {
        if (!verify(authenticationCode, content, key, algorithm)) {
            throw new AuthenticationException("Unauthenticated: content authenticity and integrity failed verification and is not trusted");
        }
    }

    /**
     * Calculates a HMAC authentication code for a given HMAC algorithm, secret key, and content or data.
     *
     * @param content                   The content or payload or data being authenticated.
     * @param key                       The secret key used to calculate the authentication code.
     * @param algorithm                 The HMAC algorithm name used to calculate the authentication code.
     * @return                          The calculated authentication code.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws NoSuchAlgorithmException If the given algorithm is invalid.
     */
    public static byte[] calculate(byte[] content, String key, String algorithm) throws InvalidKeyException, NoSuchAlgorithmException {
        if (content == null) throw new NullPointerException("content must not be null");
        if (key == null) throw new NullPointerException("key must not be null");
        if (algorithm == null) throw new NullPointerException("algorithm must not be null");

        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(CharsetHelper.DEFAULT_CHARSET), algorithm);

        Mac calculator = Mac.getInstance(algorithm);
        calculator.init(secretKeySpec);

        return calculator.doFinal(content);
    }

    /**
     * Returns true if the given authentication code exactly matches the calculated authentication code.
     *
     * @param authenticationCode        The authentication code provided by an authenticating party.
     * @param content                   The content or payload or data being authenticated.
     * @param key                       The secret key agreed between the parties.
     * @param algorithm                 The HMAC algorithm name used to calculate the authentication code.
     * @return                          True if the given authentication code matches the calculated authentication code.
     * @throws InvalidKeyException      If the given key is invalid.
     * @throws NoSuchAlgorithmException If the given algorithm is invalid.
     */
    public static boolean verify(byte[] authenticationCode, byte[] content, String key, String algorithm) throws InvalidKeyException, NoSuchAlgorithmException {
        return authenticationCode != null && content != null && key != null && algorithm != null && Arrays.equals(calculate(content, key, algorithm), (authenticationCode));
    }
}
