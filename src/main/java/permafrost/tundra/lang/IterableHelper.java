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

package permafrost.tundra.lang;

/**
 * A collection of convenience methods for working with Iterable objects.
 */
public class IterableHelper {
    /**
     * Returns a string created by concatenating each element of the given iterable, separated by the given separator
     * string.
     *
     * @param iterable   The iterable whose contents are to be joined.
     * @param separator  An optional separator string to be used between elements of the iterable.
     * @param <E>        The class of element stored in the iterable.
     * @return           A string representation of the given iterable created by concatenating together the string
     *                   representation of each element in order, optionally separated by the given separator string.
     */
    public static <E> String join(Iterable<E> iterable, String separator) {
        return join(iterable, separator, true);
    }

    /**
     * Returns a string created by concatenating each element of the given iterable, separated by the given separator
     * string.
     *
     * @param iterable     The iterable whose contents are to be joined.
     * @param separator    An optional separator string to be used between elements of the iterable.
     * @param includeNulls If true, null values will be included in the output string, otherwise they are ignored.
     * @param <E>          The class of element stored in the iterable.
     * @return             A string representation of the given iterable created by concatenating together the string
     *                     representation of each element in order, optionally separated by the given separator string.
     */
    public static <E> String join(Iterable<E> iterable, String separator, boolean includeNulls) {
        if (iterable == null) return null;

        StringBuilder builder = new StringBuilder();
        boolean separatorRequired = false;

        for (E element : iterable) {
            boolean includeElement = includeNulls || element != null;

            if (separator != null && separatorRequired && includeElement) builder.append(separator);

            if (includeElement) {
                builder.append(ObjectHelper.stringify(element));
                separatorRequired = true;
            }
        }

        return builder.toString();
    }
}
