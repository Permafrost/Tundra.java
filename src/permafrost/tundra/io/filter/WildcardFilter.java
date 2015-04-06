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

package permafrost.tundra.io.filter;

/**
 * A FilenameFilter that only accepts objects that match the given wildcard expression.
 */
public class WildcardFilter extends RegularExpressionFilter {
    /**
     * Constructs a new WilcardFilter using the given wildcard pattern.
     * @param pattern A wildcard pattern to use for filtering files.
     */
    public WildcardFilter(String pattern) {
        super(toRegularExpression(pattern));
    }

    /**
     * Returns a regular expression representation of the given wildcard pattern.
     * @param wildcardPattern The wildcardPattern to be converted to a regular expression.
     * @return                A regular expression representation of the given wildcard pattern.
     */
    protected static String toRegularExpression(String wildcardPattern) {
        StringBuilder buffer = new StringBuilder();
        char[] characters = wildcardPattern.toCharArray();

        for (int i = 0; i < characters.length; ++i) {
            if (characters[i] == '*') {
                buffer.append(".*");
            } else if (characters[i] == '?') {
                buffer.append(".");
            } else if ("+()^$.{}[]|\\".indexOf(characters[i]) != -1) {
                buffer.append('\\').append(characters[i]);
            } else {
                buffer.append(characters[i]);
            }
        }
        return buffer.toString();
    }
}
