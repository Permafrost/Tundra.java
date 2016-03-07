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
 * A collection of convenience methods for working with doubles.
 */
public final class DoubleHelper {
    /**
     * The default value used when parsing a null string.
     */
    private static double DEFAULT_DOUBLE_VALUE = 0d;

    /**
     * Disallow instantiation of this class.
     */
    private DoubleHelper() {}

    /**
     * Parses the given string as a double.
     *
     * @param input A string to be parsed as double.
     * @return      Double representing the given string, or 0 if the given string was null.
     */
    public static double parse(String input) {
        return parse(input, DEFAULT_DOUBLE_VALUE);
    }

    /**
     * Parses the given string as a double.
     *
     * @param input        A string to be parsed as double.
     * @param defaultValue The value returned if the given string is null.
     * @return             Double representing the given string, or defaultValue if the given string is null.
     */
    public static double parse(String input, double defaultValue) {
        if (input == null) return defaultValue;
        return Double.parseDouble(input);
    }

    /**
     * Parses the given strings as doubles.
     *
     * @param input         A list of strings to be parsed as doubles.
     * @return              A list of doubles representing the given strings.
     */
    public static double[] parse(String[] input) {
        return parse(input, DEFAULT_DOUBLE_VALUE);
    }

    /**
     * Parses the given strings as double.
     *
     * @param input         A list of strings to be parsed as doubles.
     * @param defaultValue  The value returned if a string in the list is null.
     * @return              A list of doubles representing the given strings.
     */
    public static double[] parse(String[] input, double defaultValue) {
        if (input == null) return null;
        double[] output = new double[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = parse(input[i], defaultValue);
        }

        return output;
    }

    /**
     * Serializes the given double as a string.
     *
     * @param input The double to be serialized.
     * @return      A string representation of the given double.
     */
    public static String emit(double input) {
        return Double.toString(input);
    }

    /**
     * Serializes the given doubles as strings.
     *
     * @param input A list of doubles to be serialized.
     * @return      A list of string representations of the given doubles.
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
