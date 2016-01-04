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
public final class IntegerHelper {
    /**
     * The default value used when parsing a null string.
     */
    private static int DEFAULT_INT_VALUE = 0;

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
        return parse(input, DEFAULT_INT_VALUE);
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

    /**
     * Parses the given strings as integers.
     *
     * @param input         A list of strings to be parsed as integers.
     * @return              A list of integers representing the given strings.
     */
    public static int[] parse(String[] input) {
        return parse(input, DEFAULT_INT_VALUE);
    }

    /**
     * Parses the given strings as integers.
     *
     * @param input         A list of strings to be parsed as integers.
     * @param defaultValue  The value returned if a string in the list is null.
     * @return              A list of integers representing the given strings.
     */
    public static int[] parse(String[] input, int defaultValue) {
        if (input == null) return null;
        int[] output = new int[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = parse(input[i], defaultValue);
        }

        return output;
    }

    /**
     * Serializes the given integer as a string.
     *
     * @param input The integer to be serialized.
     * @return      A string representation of the given integer.
     */
    public static String emit(int input) {
        return Integer.toString(input);
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
