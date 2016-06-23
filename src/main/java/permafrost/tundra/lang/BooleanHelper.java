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

package permafrost.tundra.lang;

/**
 * A collection of convenience methods for working with booleans.
 */
public final class BooleanHelper {
    /**
     * Disallow instantiation of this class.
     */
    private BooleanHelper() {}

    /**
     * Normalizes a boolean string to either "true" or "false", substituting the given default if the string is null.
     *
     * @param string        The boolean string to normalize.
     * @param defaultValue  The value to use if string is null.
     * @return              The normalized boolean string.
     */
    public static String normalize(String string, String defaultValue) {
        return normalize(string == null ? defaultValue : string);
    }

    /**
     * Normalizes a boolean string to either "true" or "false".
     *
     * @param string    The boolean string to normalize.
     * @return          The normalized boolean string.
     */
    public static String normalize(String string) {
        return emit(parse(string));
    }

    /**
     * Parses the given object to a boolean value.
     *
     * @param object An object which is either a Boolean or a String representation of a boolean.
     * @return       The parsed boolean value.
     */
    public static boolean parse(Object object) {
        return parse(object, false);
    }

    /**
     * Parses the given object to a boolean value.
     *
     * @param object        An object which is either a Boolean or a String representation of a boolean.
     * @param defaultValue  The default boolean value returned when the given object is null.
     * @return              The parsed boolean value.
     */
    public static boolean parse(Object object, boolean defaultValue) {
        if (object instanceof Boolean) {
            return (Boolean)object;
        } else {
            return parse(object == null ? (String)null : object.toString(), defaultValue);
        }
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" to represent
     * true, or "false" or "0" to represent false.
     *
     * @param string    The boolean string to be parsed.
     * @return          The boolean value of the given string.
     */
    public static boolean parse(String string) {
        return parse(string, false);
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" to represent
     * true, or "false" or "0" to represent false.
     *
     * @param string        The boolean string to be parsed.
     * @param defaultValue  The boolean value returned if the given string is null.
     * @return              The boolean value of the given string.
     */
    public static boolean parse(String string, String defaultValue) {
        return parse(string, parse(defaultValue));
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" to represent
     * true, or "false" or "0" falseValue to represent false.
     *
     * @param string        The boolean string to be parsed.
     * @param defaultValue  The boolean value returned if the given string is null.
     * @return              The boolean value of the given string.
     */
    public static boolean parse(String string, boolean defaultValue) {
        return parse(string, null, null, defaultValue);
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" or the given
     * trueValue to represent true, or "false" or "0" or the given falseValue to represent false.
     *
     * @param string        The boolean string to be parsed.
     * @param trueValue     The value used to determine if the string represents the boolean value true.
     * @param falseValue    The value used to determine if the string represents the boolean value false.
     * @return              The boolean value of the given string.
     */
    public static boolean parse(String string, String trueValue, String falseValue) {
        return parse(string, trueValue, falseValue, false);
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" or the given
     * trueValue to represent true, or "false" or "0" or the given falseValue to represent false.
     *
     * @param string        The boolean string to be parsed.
     * @param trueValue     The value used to determine if the string represents the boolean value true.
     * @param falseValue    The value used to determine if the string represents the boolean value false.
     * @param defaultValue  The boolean value returned if the given string is null.
     * @return              The boolean value of the given string.
     */
    public static boolean parse(String string, String trueValue, String falseValue, String defaultValue) {
        return parse(string, trueValue, falseValue, parse(defaultValue));
    }

    /**
     * Parses a string that can contain (ignoring case and leading and trailing whitespace) "true" or "1" or the given
     * trueValue to represent true, or "false" or "0" or the given falseValue to represent false.
     *
     * @param string        The boolean string to be parsed.
     * @param trueValue     The value used to determine if the string represents the boolean value true.
     * @param falseValue    The value used to determine if the string represents the boolean value false.
     * @param defaultValue  The boolean value returned if the given string is null.
     * @return              The boolean value of the given string.
     */
    public static boolean parse(String string, String trueValue, String falseValue, boolean defaultValue) {
        if (string == null) return defaultValue;

        string = string.trim().toLowerCase();

        boolean result;

        if (string.equals("1") || string.equals("true") || (trueValue != null && string.equalsIgnoreCase(trueValue))) {
            result = true;
        } else if (string.equals("0") || string.equals("false") || (falseValue != null && string.equalsIgnoreCase(falseValue))) {
            result = false;
        } else {
            throw new IllegalArgumentException("Unparseable boolean value: " + string);
        }

        return result;
    }

    /**
     * Returns a string representation of the given boolean value.
     *
     * @param bool          The boolean to convert to a string.
     * @param trueValue     The value returned if the given boolean is true.
     * @param falseValue    The value returned if the given boolean is false.
     * @return              The string representation of the given boolean.
     */
    public static String emit(boolean bool, String trueValue, String falseValue) {
        return bool ? (trueValue == null ? Boolean.toString(bool) : trueValue) : (falseValue == null ? Boolean.toString(bool) : falseValue);
    }

    /**
     * Returns a boolean value in its canonical string form: "true" or "false".
     *
     * @param bool  The boolean value to serialize to a string.
     * @return      The canonical string representation of the given boolean value.
     */
    public static String emit(boolean bool) {
        return emit(bool, null, null);
    }

    /**
     * Returns the negated boolean value of the given string.
     *
     * @param string        The boolean string to be negated.
     * @param trueValue     The value used to determine if the string represents the boolean value true.
     * @param falseValue    The value used to determine if the string represents the boolean value false.
     * @return              The given boolean string negated.
     */
    public static String negate(String string, String trueValue, String falseValue) {
        return emit(negate(parse(string, trueValue, falseValue)), trueValue, falseValue);
    }

    /**
     * Returns the negated boolean value of the given string.
     *
     * @param string    The boolean string to be negated.
     * @return          The given boolean string negated.
     */
    public static String negate(String string) {
        return negate(string, null, null);
    }

    /**
     * Returns the negated value of the given boolean.
     *
     * @param bool  The boolean value to be negated.
     * @return      The given boolean value negated.
     */
    public static boolean negate(boolean bool) {
        return !bool;
    }

    /**
     * Formats the given boolean string using the given output boolean values.
     *
     * @param inString      The string to be formatted.
     * @param inTrueValue   The value used to determine if the string represents the boolean value true.
     * @param inFalseValue  The value used to determine if the string represents the boolean value false.
     * @param outTrueValue  The value returned if the given boolean is true.
     * @param outFalseValue The value returned if the given boolean is false.
     * @return              The given string reformatted.
     */
    public static String format(String inString, String inTrueValue, String inFalseValue, String outTrueValue, String outFalseValue) {
        return emit(parse(inString, inTrueValue, inFalseValue), outTrueValue, outFalseValue);
    }
}
