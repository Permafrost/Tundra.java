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

package permafrost.tundra.io.filter;

/**
 * The different types of filename filters supported by the InclusionFilenameFilter and ExclusionFilenameFilter classes.
 */
public enum FilenameFilterType {
    REGULAR_EXPRESSION, WILDCARD;
    /**
     * The default FilenameFilterType, if none is specified.
     */
    public static final FilenameFilterType DEFAULT_FILENAME_FILTER_TYPE = REGULAR_EXPRESSION;

    /**
     * Returns an FilenameFilterType for the given string value.
     *
     * @param value The value to be converted to an FilenameFilterType.
     * @return      The FilenameFilterType representing the given value.
     */
    public static FilenameFilterType normalize(String value) {
        if (value != null && (value.equalsIgnoreCase("regex") || value.equalsIgnoreCase("regular expression"))) value = "REGULAR_EXPRESSION";
        return normalize(value == null ? null : valueOf(value.trim().toUpperCase()));
    }

    /**
     * Normalizes an FilenameFilterType.
     *
     * @param type The FilenameFilterType to be normalized.
     * @return      If the given type is null the default type, otherwise the given type.
     */
    public static FilenameFilterType normalize(FilenameFilterType type) {
        return type == null ? DEFAULT_FILENAME_FILTER_TYPE : type;
    }
}
