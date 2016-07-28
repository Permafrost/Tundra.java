/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.lang;

import java.io.Serializable;
import java.util.Locale;

/**
 * A case insensitive for comparison and case preserving for reference string wrapper.
 */
public class CaseInsensitiveString implements CharSequence, Comparable<CharSequence>, Serializable {
    /**
     * The serialization identity for this class and version.
     */
    private static final long serialVersionUID = 1;
    /**
     * The strings to be wrapped.
     */
    protected String originalString, lowercaseString;
    /**
     * The locale to use for case comparison.
     */
    protected Locale locale;

    /**
     * Constructs a new CaseInsensitiveString.
     *
     * @param charSequence The string to be wrapped.
     */
    public CaseInsensitiveString(CharSequence charSequence) {
        this(charSequence, null);
    }

    /**
     * Constructs a new CaseInsensitiveString.
     *
     * @param charSequence The string to be wrapped.
     * @param locale       The locale to use for case comparison.
     */
    public CaseInsensitiveString(CharSequence charSequence, Locale locale) {
        if (charSequence == null) throw new NullPointerException("charSequence must not be null");

        this.locale = locale == null ? Locale.getDefault() : locale;
        this.originalString = charSequence.toString();
        this.lowercaseString = this.originalString.toLowerCase(this.locale);
    }

    /**
     * Returns the character at the given index.
     *
     * @param i The character index.
     * @return  The character at the given index.
     */
    public char charAt(int i) {
        return this.originalString.charAt(i);
    }

    /**
     * Returns the number of characters in this string.
     *
     * @return The number of characters in this string.
     */
    public int length() {
        return this.originalString.length();
    }

    /**
     * Returns a sub-sequence for the given range of characters.
     *
     * @param start The start index for the range.
     * @param end   The end index for the range.
     * @return      The sub-sequence of characters for the given range.
     */
    public CaseInsensitiveString subSequence(int start, int end) {
        return new CaseInsensitiveString(originalString.subSequence(start, end));
    }

    /**
     * Performs a case-insensitive comparison with the other string.
     *
     * @param other A string to compared with this object.
     * @return      The result of the comparison.
     */
    public int compareTo(CharSequence other) {
        return this.lowercaseString.compareTo(other.toString().toLowerCase(locale));
    }

    /**
     * Compares this object against another for equality.
     *
     * @param other The other object to compare this object against for equality.
     * @return      True if the two objects are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        boolean result = false;
        if (other instanceof CharSequence) {
            String otherString = ((CharSequence)other).toString();
            result = this.lowercaseString.equals(otherString.toLowerCase(locale));
        }
        return result;
    }

    /**
     * Returns the original string wrapped by this case-insensitive string object.
     *
     * @return The original string wrapped by this case-insensitive string object.
     */
    @Override
    public String toString() {
        return this.originalString;
    }
}
