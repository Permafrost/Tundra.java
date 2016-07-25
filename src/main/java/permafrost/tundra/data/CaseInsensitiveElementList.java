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
import com.wm.data.IDataPortable;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.lang.LocaleHelper;
import java.io.Serializable;
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
        this(null);
    }

    /**
     * Constructs a new CaseInsensitiveElementList.
     *
     * @param locale The locale to use for case comparison.
     */
    public CaseInsensitiveElementList(Locale locale) {
        setLocale(locale);
    }

    /**
     * Sets the locale to use for case comparison.
     *
     * @param locale The locale to use for case comparison.
     */
    private void setLocale(Locale locale) {
        this.locale = LocaleHelper.normalize(locale);
    }

    /**
     * Sets the element at the given index.
     *
     * @param i The index whose element is to be set.
     * @param e The element to be set.
     * @return  The previous element at the given index.
     */
    @Override
    public Element<String, V> set(int i, Element<String, V> e) {
        return elements.set(i, CaseInsensitiveElement.normalize(e, locale));
    }

    /**
     * Inserts the given element at the given index.
     *
     * @param i The index to insert an element into.
     * @param e The element to be inserted.
     */
    @Override
    public void add(int i, Element<String, V> e) {
        elements.add(i, CaseInsensitiveElement.normalize(e, locale));
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
     * @return          A new CaseInsensitiveElementList which is a recursive clone of the given IData document.
     */
    public static IData of(IData document) {
        return of(document, null);
    }

    /**
     * Recursively builds a clone of the given IData document as a new CaseInsensitiveElementList.
     *
     * @param document  The document to clone.
     * @param locale    The locale used for case comparison.
     * @return          A new CaseInsensitiveElementList which is a recursive clone of the given IData document.
     */
    public static IData of(IData document, Locale locale) {
        CaseInsensitiveElementList<Object> output = new CaseInsensitiveElementList<Object>();

        if (document != null) {
            IDataCursor cursor = document.getCursor();
            while (cursor.next()) {
                output.add(new CaseInsensitiveElement<Object>(cursor.getKey(), normalize(cursor.getValue(), locale), locale));
            }
            cursor.destroy();
        }

        return output;
    }

    /**
     * Recursively builds a clone of the given IData[] document list as a new CaseInsensitiveElementList[].
     *
     * @param documents The documents to clone.
     * @return          A new CaseInsensitiveElementList[] which is a recursive clone of the given IData[] documents.
     */
    public static IData[] of(IData[] documents) {
        return of(documents, null);
    }

    /**
     * Recursively builds a clone of the given IData[] document list as a new CaseInsensitiveElementList[].
     *
     * @param documents The documents to clone.
     * @param locale    The locale used for case comparison.
     * @return          A new CaseInsensitiveElementList[] which is a recursive clone of the given IData[] documents.
     */
    public static IData[] of(IData[] documents, Locale locale) {
        IData[] output;

        if (documents == null) {
            output = new IData[0];
        } else {
            output = new IData[documents.length];
            for (int i = 0; i < documents.length; i++) {
                output[i] = of(documents[i], locale);
            }
        }

        return output;
    }

    /**
     * Converts the given value if it is an IData or IData[] compatible object to a CopyOnWriteIDataMap or
     * CopyOnWriteIDataMap[] respectively.
     *
     * @param value The value to be normalized.
     * @return      If the value is an IData or IData[] compatible object, a new CopyOnWriteIDataMap or
     *              CopyOnWriteIDataMap[] respectively is returned which wraps the given value, otherwise
     *              the value itself is returned unmodified.
     */
    private static Object normalize(Object value, Locale locale) {
        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
            value = (IData[])of(IDataHelper.toIDataArray(value), locale);
        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
            value = (IData)of(IDataHelper.toIData(value), locale);
        }
        return value;
    }

    /**
     * An element whose keys are case-insensitive for comparison and case-preserving for reference.
     *
     * @param <V> The value's class.
     */
    protected static class CaseInsensitiveElement<V> extends Element<String, V> {
        /**
         * Case insensitive version of the key.
         */
        protected CaseInsensitiveString caseInsensitiveKey;
        /**
         * The locale to use for case insensitivity.
         */
        protected Locale locale;

        /**
         * Constructs a new element using the given existing element.
         *
         * @param element   The element to use when constructing this new element.
         */
        public CaseInsensitiveElement(Element<String, V> element) {
            this(element, null);
        }

        /**
         * Constructs a new element using the given key and value.
         *
         * @param element   The element to use when constructing this new element.
         * @param locale    The locale to use for case comparisons.
         */
        public CaseInsensitiveElement(Element<String, V> element, Locale locale) {
            this(element.getKey(), element.getValue(), locale);
        }

        /**
         * Constructs a new element using the given key and value.
         *
         * @param key       The key for the element.
         * @param value     The value for the element.
         */
        public CaseInsensitiveElement(String key, V value) {
            this(key, value, null);
        }

        /**
         * Constructs a new element using the given key and value.
         *
         * @param key       The key for the element.
         * @param value     The value for the element.
         * @param locale    The locale used for case comparison.
         */
        public CaseInsensitiveElement(String key, V value, Locale locale) {
            setLocale(locale);
            setKey(key);
            setValue(value);
        }

        /**
         * Returns the key of the element.
         *
         * @return The key of the element.
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the key of the element.
         *
         * @param newKey    The key to be set.
         * @return          The previous key of the element.
         */
        public String setKey(String newKey) {
            if (newKey == null) throw new NullPointerException("key must not be null");
            String oldKey = key;
            key = newKey;
            caseInsensitiveKey = new CaseInsensitiveString(key, locale);
            return oldKey;
        }

        /**
         * Returns the locale associated with this element.
         *
         * @return The locale associated with this element.
         */
        public Locale getLocale() {
            return locale;
        }

        /**
         * Sets the locale to use for case comparison.
         *
         * @param locale The locale to use for case comparison.
         */
        private void setLocale(Locale locale) {
            this.locale = LocaleHelper.normalize(locale);
        }

        /**
         * Returns whether this element is equivalent to the given other element.
         *
         * @param other The other element to compare equivalency with.
         * @return      True if this element is equivalent to the give other element.
         */
        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object other) {
            if (this == other) return true;

            boolean result = false;
            if (other instanceof Map.Entry) {
                result = keyEquals((Map.Entry)other);
            }

            return result;
        }

        /**
         * Returns true if another entry's key is equal to this entry's key.
         *
         * @param other The other entry to compare key equality to.
         * @return      True if the other entry's key is equal to this entry's key.
         */
        @Override
        public boolean keyEquals(String other) {
            return caseInsensitiveKey.equals(other);
        }

        /**
         * Compares this element's key with the given other element's key, if they implement the Comparable interface.
         *
         * @param other The other element to compare this object with.
         * @return      The comparison result.
         */
        @SuppressWarnings("unchecked")
        public int compareTo(Map.Entry<? extends String, ? extends V> other) {
            return caseInsensitiveKey.compareTo(other.getKey());
        }

        /**
         * Returns the hash code for this element.
         *
         * @return The hash code for this element.
         */
        @Override
        @SuppressWarnings("unchecked")
        public int hashCode() {
            return getKey().hashCode();
        }

        /**
         * Returns a new CaseInsensitiveElement using the given element's key and value.
         *
         * @param element               An element to be represented as a CaseInsensitiveElement.
         * @param locale                The locale to use for case comparison.
         * @param <V>                   The class of values held by the element.
         * @return                      A new CaseInsensitiveElement with the given element's key and value.
         * @throws NullPointerException If the given element is null.
         */
        public static <V> CaseInsensitiveElement<V> of(Element<String, V> element, Locale locale) {
            return new CaseInsensitiveElement<V>(element, locale);
        }

        /**
         * Returns the given element if it is already a CaseInsensitiveElement that uses the specified
         * locale, otherwise returns a new CaseInsensitiveElement with the given element's key and value.
         *
         * @param element               The element to be normalized.
         * @param <V>                   The class of values held by the element.
         * @return                      The normalized element.
         * @throws NullPointerException If the given element is null.
         */
        public static <V> CaseInsensitiveElement<V> normalize(Element<String, V> element) {
            return normalize(element, null);
        }

        /**
         * Returns the given element if it is already a CaseInsensitiveElement that uses the specified
         * locale, otherwise returns a new CaseInsensitiveElement with the given element's key and value.
         *
         * @param element               The element to be normalized.
         * @param locale                The locale to use for case comparison.
         * @param <V>                   The class of values held by the element.
         * @return                      The normalized element.
         * @throws NullPointerException If the given element is null.
         */
        @SuppressWarnings("unchecked")
        public static <V> CaseInsensitiveElement<V> normalize(Element<String, V> element, Locale locale) {
            CaseInsensitiveElement<V> output = null;

            if (element instanceof CaseInsensitiveElement) {
                locale = LocaleHelper.normalize(locale);
                if (!locale.equals(((CaseInsensitiveElement)element).getLocale())) {
                    output = new CaseInsensitiveElement<V>((CaseInsensitiveElement)element, locale);
                } else {
                    output = (CaseInsensitiveElement)element;
                }
            } else {
                output = of(element, locale);
            }

            return output;
        }
    }

    /**
     * A case insensitive for comparison and case preserving for reference string wrapper.
     */
    protected static class CaseInsensitiveString implements CharSequence, Comparable<CharSequence>, Serializable {
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
