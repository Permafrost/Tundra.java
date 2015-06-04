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

import java.util.Enumeration;
import java.util.Iterator;

/**
 * This class adapts an Enumeration object to a single use Iterable object.
 * @param <T> The type of item iterated over.
 */
public class IterableEnumeration<T> implements Iterable<T> {
    /**
     * The iterator this object iterates over.
     */
    private Iterator<T> iterator;

    /**
     * Constructs a new IterableEnumeration object.
     *
     * @param enumeration The enumeration to be iterated over.
     * @throws NullPointerException If the enumeration is null.
     */
    public IterableEnumeration(Enumeration<T> enumeration) {
        this.iterator = new EnumerationIterator<T>(enumeration);
    }

    /**
     * Returns an iterator over a set of elements of type T.
     * @return An iterator over a set of elements of type T.
     */
    public Iterator<T> iterator() {
        return iterator;
    }

    /**
     * Constructs a new IterableEnumeration object.
     *
     * @param enumeration   The enumeration to be iterated over.
     * @param <T>           The generic type iterated over.
     * @return              The constructed IterableEnumeration object.
     * @throws NullPointerException If the enumeration is null.
     */
    public static <T> Iterable<T> of(Enumeration<T> enumeration) {
        return new IterableEnumeration<T>(enumeration);
    }

    /**
     * Constructs a new IterableEnumeration object. This is an alias for the of method.
     *
     * @param enumeration   The enumeration to be iterated over.
     * @param <T>           The generic type iterated over.
     * @return              The constructed IterableEnumeration object.
     * @throws NullPointerException If the enumeration is null.
     */
    public static <T> Iterable<T> newInstance(Enumeration<T> enumeration) {
        return of(enumeration);
    }
}
