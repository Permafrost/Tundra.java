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

package permafrost.tundra.idata;

import com.wm.data.IData;
import com.wm.data.IDataCursor;

import java.util.AbstractMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Allows the use of a Java for each loop over an IData document.
 */
public class IDataIteratorFactory implements Iterable<Map.Entry<String, Object>> {
    protected IData document;

    /**
     * Construct a new IDataIterator
     * @param document
     */
    public IDataIteratorFactory(IData document) {
        if (document == null) throw new IllegalArgumentException("document must not be null");
        this.document = document;
    }

    /**
     * Returns an iterator over a set of elements of type Map.Entry.
     * @return An iterator.
     */
    @Override
    public IDataIterator iterator() {
        return new IDataIteratorImplementation(document);
    }

    /**
     * Implementation class for an IDataIterator.
     */
    private static class IDataIteratorImplementation implements IDataIterator {
        protected IDataCursor cursor;

        /**
         * Constructs a new IDataIterator object for iterating over the given IData document.
         * @param document The document to be iterated over.
         */
        public IDataIteratorImplementation(IData document) {
            if (document == null) throw new IllegalArgumentException("document must not be null");
            this.cursor = document.getCursor();
        }

        /**
         * Returns true if the iteration has more elements. (In other words, returns true if next()
         * would return an element rather than throwing an exception.)
         * @return True if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return cursor.hasMoreData();
        }

        /**
         * Returns the next element in the iteration.
         * @return The next element in the iteration.
         * @throws NoSuchElementException If the iteration has no more elements.
         */
        @Override
        public AbstractMap.SimpleImmutableEntry<String, Object> next() throws NoSuchElementException {
            if (cursor.next()) {
                return new AbstractMap.SimpleImmutableEntry<String, Object>(cursor.getKey(), cursor.getValue());
            } else {
                throw new NoSuchElementException("No more elements were available for iteration in IData document");
            }
        }

        /**
         * Throws an UnsupportedOperationException because the remove operation is not supported by this
         * iterator.
         * @throws UnsupportedOperationException The remove operation is not supported by this iterator.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove is not implemented by this class");
        }
    }
}
