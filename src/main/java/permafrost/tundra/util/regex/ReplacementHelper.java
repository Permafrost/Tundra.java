/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

/**
 * A collection of convenience methods for working regular expression replacement strings.
 */
public final class ReplacementHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ReplacementHelper() {}

    /**
     * Quotes the given string so that it is treated as a literal replacement string.
     *
     * @param string    The string to be quoted.
     * @return          A literal replacement string.
     */
    public static String quote(String string) {
        return quote(string, true);
    }

    /**
     * Quotes the given string so that it is treated as a regular expression literal, if required.
     *
     * @param string    The string to be quoted.
     * @param literal   Whether the string should be quoted.
     * @return          A literal replacement string.
     */
    public static String quote(String string, boolean literal) {
        if (string != null && literal) string = Matcher.quoteReplacement(string);
        return string;
    }
}
