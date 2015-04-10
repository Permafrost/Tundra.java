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

package permafrost.tundra.math;

import permafrost.tundra.lang.BaseException;
import permafrost.tundra.io.ParseException;

import java.math.BigInteger;

public class IntegerHelper {
    /**
     * Disallow instantiation of this class.
     */
    private IntegerHelper() {}

    /**
     * Returns a java.math.BigInteger object by parsing the given an integer string.
     * @param string A string to be parsed.
     * @return       A java.math.BigInteger representation of the given string.
     * @throws BaseException If the given string is unparseable.
     */
    public static BigInteger parse(String string) throws BaseException {
        return parse(string, 10);
    }

    /**
     * Returns a java.math.BigInteger object by parsing the given an integer string,
     * using the given radix.
     *
     * @param string A string to be parsed.
     * @param radix  The radix to use when interpreting the given string.
     * @return       A java.math.BigInteger representation of the given string.
     * @throws BaseException If the given string is unparseable.
     */
    public static BigInteger parse(String string, int radix) throws BaseException {
        if (string == null) return null;

        try {
            return new BigInteger(string, radix);
        } catch(IllegalArgumentException ex) {
            throw new ParseException(ex);
        }
    }
}
