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
import java.util.NoSuchElementException;

/**
 * This class adapts an Enumeration object to an Iterator object.
 *
 * @param <T> The type of element iterated over.
 */
public class EnumerationIterator<T> implements Iterator<T> {
    /**
     * The enumeration that this object iterates over.
     */
    private final Enumeration<T> enumeration;

    /**
     * Constructs a new EnumerationIterator object.
     *
     * @param enumeration The enumeration to iterate over.
     */
    public EnumerationIterator(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    /**
     * Returns true if the iteration has more elements. (In other words, returns true if next() would return an element
     * rather than throwing an exception.)
     *
     * @return True if the iteration has more elements.
     */
    public boolean hasNext() {
        return enumeration != null && enumeration.hasMoreElements();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return The next element in the iteration.
     * @throws NoSuchElementException If no more elements exist.
     */
    public T next() {
        if (enumeration == null) throw new NoSuchElementException();
        return enumeration.nextElement();
    }

    /**
     * Removes from the underlying collection the last element returned by this iterator (optional operation). This
     * method can be called only once per call to next(). The behavior of an iterator is unspecified if the underlying
     * collection is modified while the iteration is in progress in any way other than by calling this method.
     *
     * @throws UnsupportedOperationException This method is not implemented by this class.
     */
    public void remove() {
        throw new UnsupportedOperationException("remove method is not supported");
    }

    /**
     * Returns a new EnumerationIterator object for the given Enumeration.
     *
     * @param enumeration The enumeration to be iterated over.
     * @param <T> The type of objects the enumerator returns.
     * @return A new EnumerationIterator object for the given Enumeration.
     */
    public static <T> EnumerationIterator<T> of(Enumeration<T> enumeration) {
        return new EnumerationIterator<T>(enumeration);
    }

    /**
     * Returns a new EnumerationIterator object for the given Enumeration.
     *
     * @param enumeration The enumeration to be iterated over.
     * @param <T> The type of objects the enumerator returns.
     * @return A new EnumerationIterator object for the given Enumeration.
     */
    public static <T> EnumerationIterator<T> newInstance(Enumeration<T> enumeration) {
        return of(enumeration);
    }
}
