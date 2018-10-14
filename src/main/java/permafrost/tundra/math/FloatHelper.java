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

import java.text.MessageFormat;

/**
 * A collection of convenience methods for working with floats.
 */
public final class FloatHelper {
    /**
     * The default value used when parsing a null string.
     */
    private static float DEFAULT_FLOAT_VALUE = 0f;

    /**
     * Disallow instantiation of this class.
     */
    private FloatHelper() {}

    /**
     * Converts the given object to a Float.
     *
     * @param object    The object to be converted.
     * @return          The converted object.
     */
    public static Float normalize(Object object) {
        Float value = null;

        if (object instanceof Number) {
            value = ((Number)object).floatValue();
        } else if (object instanceof String) {
            value = parse((String)object);
        }

        return value;
    }

    /**
     * Parses the given string as a float.
     *
     * @param input A string to be parsed as float.
     * @return      Float representing the given string, or 0 if the given string was null.
     */
    public static float parse(String input) {
        return parse(input, DEFAULT_FLOAT_VALUE);
    }

    /**
     * Parses the given string as a float.
     *
     * @param input        A string to be parsed as float.
     * @param defaultValue The value returned if the given string is null.
     * @return             Float representing the given string, or defaultValue if the given string is null.
     */
    public static float parse(String input, float defaultValue) {
        if (input == null) return defaultValue;
        return Float.parseFloat(input);
    }

    /**
     * Parses the given strings as floats.
     *
     * @param input         A list of strings to be parsed as integers.
     * @return              A list of floats representing the given strings.
     */
    public static float[] parse(String[] input) {
        return parse(input, DEFAULT_FLOAT_VALUE);
    }

    /**
     * Parses the given strings as floats.
     *
     * @param input         A list of strings to be parsed as floats.
     * @param defaultValue  The value returned if a string in the list is null.
     * @return              A list of floats representing the given strings.
     */
    public static float[] parse(String[] input, float defaultValue) {
        if (input == null) return null;
        float[] output = new float[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = parse(input[i], defaultValue);
        }

        return output;
    }

    /**
     * Serializes the given float as a string.
     *
     * @param input The float to be serialized.
     * @return      A string representation of the given float.
     */
    public static String emit(float input) {
        return Float.toString(input);
    }

    /**
     * Serializes the given floats as strings.
     *
     * @param input A list of floats to be serialized.
     * @return      A list of string representations of the given floats.
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
