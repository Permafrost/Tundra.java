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

/**
 * A collection of convenience methods for working with integers.
 */
public class IntegerHelper {
    /**
     * Disallow instantiation of this class.
     */
    private IntegerHelper() {}

    /**
     * Parses the given string as an integer.
     *
     * @param input A string to be parsed as integer.
     * @return      Integer representing the given string, or 0 if the given string was null.
     */
    public static int parse(String input) {
        return parse(input, 0);
    }

    /**
     * Parses the given string as an integer.
     *
     * @param input        A string to be parsed as integer.
     * @param defaultValue The value returned if the given string is null.
     * @return             Integer representing the given string, or defaultValue if the given string is null.
     */
    public static int parse(String input, int defaultValue) {
        if (input == null) return defaultValue;
        return Integer.parseInt(input);
    }
}
