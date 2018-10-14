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

package permafrost.tundra.data;

import com.wm.data.DataException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataHashCursor;
import com.wm.data.IDataIndexCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataSharedCursor;
import com.wm.data.IDataTreeCursor;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A List compatible IData implementation.
 *
 * @param <K> The key's class.
 * @param <V> The value's class.
 */
public class ElementList<K, V> extends AbstractList<Element<K, V>> implements IData, Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;
    /**
     * The internal list of elements.
     */
    protected List<Element<K, V>> elements;

    /**
     * Constructs a new ElementList.
     */
    public ElementList() {
        elements = new ArrayList<Element<K, V>>();
    }

    /**
     * Constructs a new ElementList seeded with the elements in the given IData document.
     *
     * @param document The IData document to seed the ElementList with.
     */
    public ElementList(IData document) {
        this();
        addAll(document);
    }

    /**
     * Constructs a new ElementList seeded with the elements in the given Map.
     *
     * @param map   The Map to seed the ElementList with.
     */
    public ElementList(Map<? extends K, ? extends V> map) {
        this();
        addAll(map);
    }

    /**
     * Constructs a new ElementList seeded with the elements in the given Collection.
     *
     * @param collection The Collection to seed the ElementList with.
     */
    public ElementList(Collection<? extends Element<K, V>> collection) {
        this();
        addAll(collection);
    }

    /**
     * Returns the Element at the given index.
     *
     * @param i The index whose Element is to be returned.
     * @return  The Element at the given index in the list.
     */
    public Element<K, V> get(int i) {
        return elements.get(i);
    }

    /**
     * Returns the number of elements in the list.
     *
     * @return The number of elements in the list.
     */
    public int size() {
        return elements.size();
    }

    /**
     * Sets the Element at the given index.
     *
     * @param i The index whose Element is to be set.
     * @param e The Element to be set.
     * @return  The previous Element at the given index.
     */
    public final Element<K, V> set(int i, Element<K, V> e) {
        return elements.set(i, normalize(e));
    }

    /**
     * Inserts the given Element at the given index.
     *
     * @param i The index to insert an Element into.
     * @param e The Element to be inserted.
     */
    public final void add(int i, Element<K, V> e) {
        elements.add(i, normalize(e));
    }

    /**
     * Recursively adds all the elements in the given IData document to this ElementList.
     *
     * @param document The document whose elements are to be added.
     */
    @SuppressWarnings("unchecked")
    public final void addAll(IData document) {
        if (document != null) {
            IDataCursor cursor = document.getCursor();
            while (cursor.next()) {
                add(normalize((K)cursor.getKey(), (V)normalize(cursor.getValue())));
            }
            cursor.destroy();
        }
    }

    /**
     * Adds all the elements in the given Map to this ElementList.
     *
     * @param map The map whose elements are to be added.
     */
    public final void addAll(Map<? extends K, ? extends V> map) {
        if (map != null) {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                add(normalize(entry));
            }
        }
    }

    /**
     * Normalizes the given Element; this default implementation returns the given element unmodified. Subclasses
     * can override this method to return a more appropriate Element subclass if required.
     *
     * @param element   The Element to be normalized.
     * @return          The normalized Element.
     */
    protected Element<K, V> normalize(Element<K, V> element) {
        return element;
    }

    /**
     * Converts the given Map.Entry to an Element object.
     *
     * @param entry     The Map.Entry to be converted.
     * @return          An Element representation of the given Map.Entry object.
     */
    protected final Element<K, V> normalize(Map.Entry<? extends K, ? extends V> entry) {
        return normalize(entry.getKey(), entry.getValue());
    }

    /**
     * Converts the given key value pair to an Element object.
     *
     * @param key   The key to be converted.
     * @param value The value to be converted.
     * @return      An Element representation of the given key value pair.
     */
    protected final Element<K, V> normalize(K key, V value) {
        return normalize(new Element<K, V>(key, value));
    }

    /**
     * Converts the given value if it is an IData or IData[] compatible object to an ElementList or
     * ElementList[] respectively.
     *
     * @param value The value to be normalized.
     * @return      If the value is an IData or IData[] compatible object, a new ElementList or
     *              ElementList[] respectively is returned which wraps the given value, otherwise
     *              the value itself is returned unmodified.
     */
    @SuppressWarnings("unchecked")
    protected Object normalize(Object value) {
        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
            value = normalize(IDataHelper.toIDataArray(value));
        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
            value = normalize(IDataHelper.toIData(value));
        }
        return value;
    }

    /**
     * Normalizes the given IData; this implementation converts the IData to an ElementList. Subclasses should
     * override this method and return an instance of their self.
     *
     * @param document  The IData to be normalized.
     * @return          The normalized IData.
     */
    protected IData normalize(IData document) {
        return new ElementList<String, Object>(document);
    }

    /**
     * Normalizes the given IData[]; this implementation converts the IData[] to an ElementList[].
     *
     * @param documents The IData[] to be normalized.
     * @return          The normalized IData[].
     */
    protected final IData[] normalize(IData[] documents) {
        IData[] output = new IData[documents == null ? 0 : documents.length];

        if (documents != null) {
            for(int i = 0; i < documents.length; i++) {
                output[i] = normalize(documents[i]);
            }
        }

        return output;
    }

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new ElementList<String, Object>();
    }

    /**
     * Removes the Element at the given index.
     *
     * @param i The index whose Element is to be removed.
     * @return  The Element previously positioned at the given index, now removed from the list.
     */
    public Element<K, V> remove(int i) {
        return elements.remove(i);
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new ElementListCursor();
    }

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return new IDataSharedCursorAdapter(getCursor());
    }

    /**
     * Returns an IDataIndexCursor for traversing this IData.
     *
     * @return An IDataIndexCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataIndexCursor getIndexCursor() {
        throw new UnsupportedOperationException("getIndexCursor not implemented");
    }

    /**
     * Returns an IDataTreeCursor for traversing this IData.
     *
     * @return An IDataTreeCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataTreeCursor getTreeCursor() {
        throw new UnsupportedOperationException("getTreeCursor not implemented");
    }

    /**
     * Returns an IDataHashCursor for traversing this IData.
     *
     * @return An IDataHashCursor for traversing this IData.
     * @throws UnsupportedOperationException As this method is not implemented.
     * @deprecated
     */
    @Override
    public IDataHashCursor getHashCursor() {
        throw new UnsupportedOperationException("getHashCursor not implemented");
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("{ ");

        boolean first = true;
        for (Element element : this) {
            if (!first) buffer.append(", ");
            buffer.append(element.toString());
            first = false;
        }

        buffer.append(" }");

        return buffer.toString();
    }

    /**
     * IDataCursor implementation for an ElementList object.
     */
    protected class ElementListCursor implements IDataCursor {
        /**
         * This cursor's position in the list.
         */
        protected int position;
        /**
         * This cursor's list iterator.
         */
        protected ListIterator<Element<K, V>> iterator;
        /**
         * The Element at the cursor's current position.
         */
        protected Element<K, V> element = null;

        /**
         * Constructs a new cursor.
         */
        public ElementListCursor() {
            initialize();
        }

        /**
         * Constructs a new cursor initialized to the given position.
         *
         * @param position The initial position of the cursor.
         */
        public ElementListCursor(int position) {
            initialize(position);
        }

        /**
         * Initializes this cursor.
         */
        protected void initialize() {
            initialize(-1);
        }

        /**
         * Initializes this cursor with the given position.
         *
         * @param position The initial position of the cursor.
         */
        protected void initialize(int position) {
            iterator = listIterator();

            if (position >= 0) {
                for (int i = 0; i <= position; i++) {
                    next();
                }
            }
        }

        /**
         * Not implemented, does nothing.
         *
         * @param mode Not used.
         */
        public void setErrorMode(int mode) {}

        /**
         * Not implemented, does nothing.
         *
         * @return Null.
         */
        public DataException getLastError() {
            return null;
        }

        /**
         * Not implemented, does nothing.
         *
         * @return False.
         */
        public boolean hasMoreErrors() {
            return false;
        }

        /**
         * Resets this cursor.
         */
        public void home() {
            initialize();
        }

        /**
         * Returns the key at the cursor's current position.
         *
         * @return The key at the cursor's current position.
         */
        public String getKey() {
            return element == null ? null : element.getKey().toString();
        }

        /**
         * Sets the key at the cursor's current position.
         *
         * @param key The key to be set.
         */
        @SuppressWarnings("unchecked")
        public void setKey(String key) {
            if (element != null) {
                element.setKey((K)key);
            }
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         */
        public Object getValue() {
            return element == null ? null : element.getValue();
        }

        /**
         * Sets the value at the cursor's current position.
         *
         * @param value The value to be set.
         */
        @SuppressWarnings("unchecked")
        public void setValue(Object value) {
            if (element != null) {
                element.setValue((V)value);
            }
        }

        /**
         * Deletes the Element at the cursor's current position.
         *
         * @return True if the Element was deleted.
         */
        public boolean delete() {
            boolean result = true;
            try {
                iterator.remove();
            } catch(IllegalStateException ex) {
                result = false;
            }
            return result;
        }

        /**
         * Inserts the key value pair before the cursor's current position.
         *
         * @param key               The key to be inserted.
         * @param value             The value to be inserted.
         */
        @SuppressWarnings("unchecked")
        public void insertBefore(String key, Object value) {
            previous();
            iterator.add(new Element((K)key, (V)value));
            next();
        }

        /**
         * Inserts the key value pair after the cursor's current position.
         *
         * @param key               The key to be inserted.
         * @param value             The value to be inserted.
         */
        @SuppressWarnings("unchecked")
        public void insertAfter(String key, Object value) {
            iterator.add(new Element((K)key, (V)value));
        }

        /**
         * Inserts the key with a new IData document before the cursor's current position.
         *
         * @param key               The key to be inserted.
         * @return                  The new IData document inserted.
         */
        public IData insertDataBefore(String key) {
            IData data = new ElementList<K, V>();
            insertBefore(key, data);
            return data;
        }

        /**
         * Inserts the key with a new IData document after the cursor's current position.
         *
         * @param key               The key to be inserted.
         * @return                  The new IData document inserted.
         */
        public IData insertDataAfter(String key) {
            IData data = new ElementList<K, V>();
            insertAfter(key, data);
            return data;
        }

        /**
         * Repositions this cursor's on the next Element.
         *
         * @return    True if the cursor was repositioned.
         */
        public boolean next() {
            boolean result = true;
            try {
                element = iterator.next();
            } catch(NoSuchElementException ex) {
                result = false;
            }
            return result;
        }

        /**
         * Repositions this cursor's on the next occurrence of the given key.
         *
         * @param key The key to reposition the cursor to.
         * @return    True if the key existed and the cursor was repositioned.
         */
        @SuppressWarnings("unchecked")
        public boolean next(String key) {
            while(next()) {
                if (element.keyEquals((K)key)) return true;
            }
            return false;
        }

        /**
         * Repositions this cursor's on the previous Element.
         *
         * @return    True if the cursor was repositioned.
         */
        public boolean previous() {
            boolean result = true;
            try {
                element = iterator.previous();
            } catch(NoSuchElementException ex) {
                result = false;
            }
            return result;
        }

        /**
         * Repositions this cursor's on the previous occurrence of the given key.
         *
         * @param key The key to reposition the cursor to.
         * @return    True if the key existed and the cursor was repositioned.
         */
        @SuppressWarnings("unchecked")
        public boolean previous(String key) {
            while(previous()) {
                if (element.keyEquals((K)key)) return true;
            }
            return false;
        }

        /**
         * Repositions this cursor's on the first Element.
         *
         * @return    True if the cursor was repositioned.
         */
        public boolean first() {
            initialize();
            return next();
        }

        /**
         * Repositions this cursor's on the first occurrence of the given key.
         *
         * @param key The key to reposition the cursor to.
         * @return    True if the key existed and the cursor was repositioned.
         */
        public boolean first(String key) {
            initialize();
            return next(key);
        }

        /**
         * Repositions this cursor's on the last Element.
         *
         * @return    True if the cursor was repositioned.
         */
        public boolean last() {
            boolean result = false;
            while(iterator.hasNext()) {
                result = true;
                next();
            }

            return result;
        }

        /**
         * Repositions this cursor's on the last occurrence of the given key.
         *
         * @param key The key to reposition the cursor to.
         * @return    True if the key existed and the cursor was repositioned.
         */
        public boolean last(String key) {
            last();
            previous(key);
            return next();
        }

        /**
         * Returns true if this cursor has more data to be iterated over.
         *
         * @return True if this cursor has more data to be iterated over.
         */
        public boolean hasMoreData() {
            return iterator.hasNext();
        }

        /**
         * Destroys this cursor.
         */
        public void destroy() {
            iterator = null;
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         */
        public IDataCursor getCursorClone() {
            return new ElementListCursor(this.position);
        }
    }
}
