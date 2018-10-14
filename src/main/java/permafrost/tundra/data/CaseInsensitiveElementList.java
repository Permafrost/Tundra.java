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
import permafrost.tundra.lang.LocaleHelper;
import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A case-insensitive IData implementation.
 *
 * @param <V> The class of values held by this IData implementation.
 */
public class CaseInsensitiveElementList<V> extends ElementList<String, V> implements Serializable {
    /**
     * The serialization identity for this class and version.
     */
    private static final long serialVersionUID = 1;
    /**
     * The locale used for case comparison.
     */
    protected Locale locale;

    /**
     * Constructs a new CaseInsensitiveElementList.
     */
    public CaseInsensitiveElementList() {
        this((Locale)null);
    }

    /**
     * Constructs a new CaseInsensitiveElementList.
     *
     * @param locale    The locale to use for case comparison.
     */
    public CaseInsensitiveElementList(Locale locale) {
        setLocale(locale);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given IData document.
     *
     * @param document  The IData document to seed the CaseInsensitiveElementList with.
     */
    public CaseInsensitiveElementList(IData document) {
        this(document, null);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given IData document.
     *
     * @param document  The IData document to seed the CaseInsensitiveElementList with.
     * @param locale    The locale to use for case comparison.
     */
    public CaseInsensitiveElementList(IData document, Locale locale) {
        this(locale);
        addAll(document);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given Map.
     *
     * @param map       The Map to seed the CaseInsensitiveElementList with.
     */
    public CaseInsensitiveElementList(Map<String, ? extends V> map) {
        this(map, null);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given Map.
     *
     * @param map       The Map to seed the CaseInsensitiveElementList with.
     * @param locale    The locale to use for case comparison.
     *
     */
    public CaseInsensitiveElementList(Map<String, ? extends V> map, Locale locale) {
        this(locale);
        addAll(map);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given Collection.
     *
     * @param collection    The Collection to seed the CaseInsensitiveElementList with.
     */
    public CaseInsensitiveElementList(Collection<Element<String, V>> collection) {
        this(collection, null);
    }

    /**
     * Constructs a new CaseInsensitiveElementList seeded with the elements in the given Collection.
     *
     * @param collection    The Collection to seed the CaseInsensitiveElementList with.
     * @param locale        The locale to use for case comparison.
     */
    public CaseInsensitiveElementList(Collection<Element<String, V>> collection, Locale locale) {
        this(locale);
        addAll(collection);
    }

    /**
     * Sets the locale to use for case comparison.
     *
     * @param locale The locale to use for case comparison.
     */
    protected void setLocale(Locale locale) {
        this.locale = LocaleHelper.normalize(locale);
    }

    /**
     * Normalizes the given Element; this default implementation returns the given element unmodified. Subclasses
     * can override this method to return a more appropriate Element subclass if required.
     *
     * @param element   The Element to be normalized.
     * @return          The normalized Element.
     */
    @Override
    protected Element<String, V> normalize(Element<String, V> element) {
        return new CaseInsensitiveElement<V>(element, locale);
    }

    /**
     * Normalizes the given IData; this implementation converts the IData to an ElementList. Subclasses should
     * override this method and return an instance of their self.
     *
     * @param document  The IData to be normalized.
     * @return          The normalized IData.
     */
    @Override
    protected IData normalize(IData document) {
        return new CaseInsensitiveElementList<Object>(document);
    }

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new CaseInsensitiveElementList<Object>();
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new CaseInsensitiveElementListIDataCursor();
    }

    /**
     * Recursively builds a clone of the given IData document as a new CaseInsensitiveElementList.
     *
     * @param document  The document to clone.
     * @param locale    The locale used for case comparison.
     * @return          A new CaseInsensitiveElementList which is a recursive clone of the given IData document.
     */
    public static IData of(IData document, Locale locale) {
        return new CaseInsensitiveElementList<Object>(document, locale);
    }

    /**
     * Recursively builds a clone of the given IData[] document list as a new CaseInsensitiveElementList[].
     *
     * @param documents The documents to clone.
     * @param locale    The locale used for case comparison.
     * @return          A new CaseInsensitiveElementList[] which is a recursive clone of the given IData[] documents.
     */
    public static IData[] of(IData[] documents, Locale locale) {
        IData[] output = new IData[documents == null ? 0 : documents.length];

        if (documents != null) {
            for (int i = 0; i < documents.length; i++) {
                output[i] = of(documents[i], locale);
            }
        }

        return output;
    }

    /**
     * IDataCursor implementation for an ElementList object.
     */
    protected class CaseInsensitiveElementListIDataCursor implements IDataCursor {
        /**
         * This cursor's position in the list.
         */
        protected int position;
        /**
         * This cursor's list iterator.
         */
        protected ListIterator<Element<String, V>> iterator;
        /**
         * The element at the cursor's current position.
         */
        protected Element<String, V> element = null;

        /**
         * Constructs a new cursor.
         */
        public CaseInsensitiveElementListIDataCursor() {
            initialize();
        }

        /**
         * Constructs a new cursor initialized to the given position.
         *
         * @param position The initial position of the cursor.
         */
        public CaseInsensitiveElementListIDataCursor(int position) {
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
            return element == null ? null : element.getKey();
        }

        /**
         * Sets the key at the cursor's current position.
         *
         * @param key The key to be set.
         */
        public void setKey(String key) {
            if (element != null) {
                element.setKey(key);
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
         * Deletes the element at the cursor's current position.
         *
         * @return True if the element was deleted.
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
            iterator.add(new CaseInsensitiveElement<V>(key, (V)value));
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
            iterator.add(new CaseInsensitiveElement<V>(key, (V)value));
        }

        /**
         * Inserts the key with a new IData document before the cursor's current position.
         *
         * @param key               The key to be inserted.
         * @return                  The new IData document inserted.
         */
        public IData insertDataBefore(String key) {
            IData data = new CaseInsensitiveElementList<V>();
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
            IData data = new CaseInsensitiveElementList<V>();
            insertAfter(key, data);
            return data;
        }

        /**
         * Repositions this cursor's on the next element.
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
        public boolean next(String key) {
            while(next()) {
                if (element.keyEquals(new CaseInsensitiveElement<V>(key, null))) return true;
            }
            return false;
        }

        /**
         * Repositions this cursor's on the previous element.
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
        public boolean previous(String key) {
            while(previous()) {
                if (element.equals(new CaseInsensitiveElement<V>(key, null))) return true;
            }
            return false;
        }

        /**
         * Repositions this cursor's on the first element.
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
         * Repositions this cursor's on the last element.
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
            return new CaseInsensitiveElementListIDataCursor(this.position);
        }
    }
}
