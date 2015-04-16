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

import javax.xml.bind.DatatypeConverter;

/**
 * A collection of convenience methods for working with booleans.
 */
public class BooleanHelper {
    /**
     * Disallow instantiation of this class.
     */
    private BooleanHelper() {}

    /**
     * Normalizes a boolean string to either "true" or "false", substituting the given default
     * if the string is null.
     * @param string        The boolean string to normalize.
     * @param defaultValue  The value to use if string is null.
     * @return              The normalized boolean string.
     */
    public static String normalize(String string, String defaultValue) {
        return normalize(string == null ? defaultValue : string);
    }

    /**
     * Normalizes a boolean string to either "true" or "false".
     * @param string The boolean string to normalize.
     * @return       The normalized boolean string.
     */
    public static String normalize(String string) {
        return emit(parse(string));
    }
    
    /**
     * Parses a string that can contain "true" (ignoring case) or "1" to represent
     * true, and "false" (ignoring case) or "0" to represent false.
     * @param object The boolean object to parse.
     * @return       The boolean value of the given object.
     */
    public static boolean parse(Object object) {
        boolean result = false;

        if (object != null) {
            if (object instanceof Boolean) {
                result = ((Boolean)object).booleanValue();
            } else {
                result = (parse(object.toString()));
            }
        }
        return result;
    }

    /**
     * Parses a string that can contain "true" (ignoring case) or "1" to represent
     * true, and "false" (ignoring case) or "0" to represent false.
     * @param string The boolean string to be parsed.
     * @return       The boolean value of the given string.
     */
    public static boolean parse(String string) {
        return DatatypeConverter.parseBoolean(string == null ? null : string.toLowerCase());
    }

    /**
     * Returns a string representation of the given boolean object.
     * @param object     The boolean object to convert to a string.
     * @param trueValue  The value returned if the given boolean is true.
     * @param falseValue The value returned if the given boolean is false.
     * @return           The string representation of the given boolean.
     */
    public static String emit(Object object, String trueValue, String falseValue) {
        return emit(parse(object), trueValue, falseValue);
    }

    /**
     * Returns a string representation of the given boolean value.
     * @param bool       The boolean to convert to a string.
     * @param trueValue  The value returned if the given boolean is true.
     * @param falseValue The value returned if the given boolean is false.
     * @return           The string representation of the given boolean.
     */
    public static String emit(boolean bool, String trueValue, String falseValue) {
        return bool ? (trueValue == null ? emit(bool) : trueValue) : (falseValue == null ? emit(bool) : falseValue);
    }

    /**
     * Returns a boolean value in its canonical string form: "true" or "false".
     * @param bool The boolean value to serialize to a string.
     * @return     The canonical string representation of the given boolean value.
     */
    public static String emit(boolean bool) {
        return "" + bool;
    }

    /**
     * Returns the negated boolean value of the given string.
     * @param string The boolean string to be negated.
     * @return       The given boolean string negated.
     */
    public static String negate(String string) {
        return emit(negate(parse(string)));
    }

    /**
     * Returns the negated value of the given boolean.
     * @param bool The boolean value to be negated.
     * @return     The given boolean value negated.
     */
    public static boolean negate(boolean bool) {
        return !bool;
    }
}
