/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

package permafrost.tundra.id;

/**
 * A Java implementation of ULID, as per https://github.com/alizain/ulid.
 */
public class ULID {
    /**
     * Table of characters used to base32-encode the ULID time and random components.
     */
    private static final char[] ENCODE_TABLE = {
        '0','1','2','3','4','5','6','7','8','9',
        'A','B','C','D','E','F','G','H','J','K',
        'M','N','P','Q','R','S','T','V','W','X',
        'Y','Z'
    };
    /**
     * The number of characters in the ULID devoted to the time component.
     */
    private static final int TIME_LENGTH = 10;
    /**
     * The number of characters in the ULID devoted ot the random component.
     */
    private static final int RANDOM_LENGTH = 16;

    /**
     * Returns a newly generated ULID.
     *
     * @return A newly generated ULID.
     */
    public static String generate() {
        StringBuilder buffer = new StringBuilder();
        encodeRandom(buffer);
        encodeTime(buffer);
        return buffer.reverse().toString();
    }

    /**
     * Encode the current time into this ULID's string buffer.
     */
    private static void encodeTime(StringBuilder buffer) {
        long currentTime = System.currentTimeMillis();

        for (int i = 0; i < TIME_LENGTH; i++) {
            int mod = (int)(currentTime % ENCODE_TABLE.length);
            currentTime = (currentTime - mod) / ENCODE_TABLE.length;
            buffer.append(ENCODE_TABLE[mod]);
        }
    }

    /**
     * Encodes a randomness component into this ULID's string buffer.
     */
    private static void encodeRandom(StringBuilder buffer) {
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int randomNumber = (int)(Math.floor(ENCODE_TABLE.length * Math.random()));
            buffer.append(ENCODE_TABLE[randomNumber]);
        }
    }
}
