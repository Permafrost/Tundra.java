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

package permafrost.tundra.util.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working regular expressions.
 */
public final class PatternHelper {
    /**
     * Disallow instantiation of this class.
     */
    private PatternHelper() {}

    /**
     * Compiles a given regular expression pattern.
     *
     * @param pattern The pattern to compile.
     * @return        The compiled pattern.
     */
    public static Pattern compile(String pattern) {
        return compile(pattern, false);
    }

    /**
     * Compiles a given regular expression pattern.
     *
     * @param pattern The pattern to compile.
     * @param literal Whether the pattern is a literal pattern or regular expression.
     * @return        The compiled pattern.
     */
    public static Pattern compile(String pattern, boolean literal) {
        return pattern == null ? null : Pattern.compile(quote(pattern, literal));
    }

    /**
     * Quotes the given string so that it is treated as a regular expression literal.
     *
     * @param string    The string to be quoted.
     * @return          A regular expression literal that represents the given string.
     */
    public static String quote(String string) {
        return quote(string, true);
    }

    /**
     * Quotes the given string so that it is treated as a regular expression literal, if required.
     *
     * @param string    The string to be quoted.
     * @param literal   Whether the string should be quoted.
     * @return          A regular expression literal that represents the given string, or the given string if no
     *                  quoting was required.
     */
    public static String quote(String string, boolean literal) {
        if (string != null && literal) string = Pattern.quote(string);
        return string;
    }


    /**
     * Returns a regular expression pattern that matches any of the values in the given string list.
     *
     * @param array The list of strings to be matched.
     * @return A regular expression which literally matches any of the given strings.
     */
    public static String quote(String[] array) {
        return quote(array, true);
    }

    /**
     * Returns a regular expression pattern that matches any of the values in the given string list.
     *
     * @param array The list of strings to be matched.
     * @return A regular expression which literally matches any of the given strings.
     */
    public static String quote(String[] array, boolean literal) {
        if (array == null) return null;

        int last = array.length - 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i == 0) builder.append("(");
            builder.append(quote(array[i], literal));
            if (i < last) builder.append("|");
            if (i == last) builder.append(")");
        }

        return builder.toString();
    }

}
