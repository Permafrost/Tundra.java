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

package permafrost.tundra.math;

/**
 * A collection of convenience methods for working with longs.
 */
public final class LongHelper {
    /**
     * The default value used when parsing a null string.
     */
    private static long DEFAULT_LONG_VALUE = 0L;

    /**
     * Disallow instantiation of this class.
     */
    private LongHelper() {}

    /**
     * Parses the given string as a long.
     *
     * @param input A string to be parsed as long.
     * @return      Long representing the given string, or 0 if the given string was null.
     */
    public static long parse(String input) {
        return parse(input, DEFAULT_LONG_VALUE);
    }

    /**
     * Parses the given string as a long.
     *
     * @param input        A string to be parsed as long.
     * @param defaultValue The value returned if the given string is null.
     * @return             Long representing the given string, or defaultValue if the given string is null.
     */
    public static long parse(String input, long defaultValue) {
        if (input == null) return defaultValue;
        return Long.parseLong(input);
    }

    /**
     * Parses the given strings as longs.
     *
     * @param input         A list of strings to be parsed as longs.
     * @return              A list of longs representing the given strings.
     */
    public static long[] parse(String[] input) {
        return parse(input, DEFAULT_LONG_VALUE);
    }

    /**
     * Parses the given strings as longs.
     *
     * @param input         A list of strings to be parsed as longs.
     * @param defaultValue  The value returned if a string in the list is null.
     * @return              A list of longs representing the given strings.
     */
    public static long[] parse(String[] input, long defaultValue) {
        if (input == null) return null;
        long[] output = new long[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = parse(input[i], defaultValue);
        }

        return output;
    }

    /**
     * Serializes the given long as a string.
     *
     * @param input The long to be serialized.
     * @return      A string representation of the given long.
     */
    public static String emit(long input) {
        return Long.toString(input);
    }

    /**
     * Serializes the given integers as strings.
     *
     * @param input A list of integers to be serialized.
     * @return      A list of string representations of the given integers.
     */
    public static String[] emit(int[] input) {
        if (input == null) return null;
        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = emit(input[i]);
        }

        return output;
    }
}
