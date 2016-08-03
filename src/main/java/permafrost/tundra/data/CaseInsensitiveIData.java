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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.util.Table;
import permafrost.tundra.lang.LocaleHelper;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps an IData in a case-insensitive envelope.
 */
public class CaseInsensitiveIData extends IDataEnvelope implements Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;
    /**
     * Map of case-insensitive keys to preserved-case keys in the wrapped IData document.
     */
    protected Map<String, String> keys = new ConcurrentHashMap<String, String>();
    /**
     * The locale used for case comparison.
     */
    protected Locale locale;

    /**
     * Wraps an IData document in a case-insensitive envelope.
     */
    public CaseInsensitiveIData() {
        this(null, null);
    }

    /**
     * Wraps an IData document in a case-insensitive envelope.
     *
     * @param locale    The locale used for case comparison.
     */
    public CaseInsensitiveIData(Locale locale) {
        this(null, locale);
    }

    /**
     * Wraps an IData document in an case-insensitive envelope.
     *
     * @param document  The document to be wrapped.
     */
    public CaseInsensitiveIData(IData document) {
        this(document, null);
    }

    /**
     * Wraps an IData document in an case-insensitive envelope.
     *
     * @param document  The document to be wrapped.
     * @param locale    The locale used for case comparison.
     */
    public CaseInsensitiveIData(IData document, Locale locale) {
        super(document);
        this.locale = LocaleHelper.normalize(locale);
        initialize();
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new CaseInsensitiveIDataCursor(super.getCursor());
    }

    /**
     * Constructs the map of case-insensitive keys to preserved-case keys held in the wrapped IData document.
     */
    protected void initialize() {
        IDataCursor cursor = document.getCursor();
        while(cursor.next()) {
            String key = cursor.getKey();
            keys.put(key.toLowerCase(locale), key);
        }
    }

    /**
     * Converts the given case-insensitive key to the actual case-preserved key.
     *
     * @param key   A case-insensitive key.
     * @return      Null if the key doesn't exist, or the case-preserved key used in the wrapped IData document.
     */
    protected String normalizeKey(String key) {
        if (key == null) return null;
        return keys.get(key.toLowerCase(locale));
    }

    /**
     * Adds a key to the key set used for the case-insensitive feature.
     *
     * @param key   The key to be added.
     */
    protected void addKey(String key) {
        if (key != null) keys.put(key.toLowerCase(locale), key);
    }

    /**
     * Removes a key from the key set used for the case-insensitive feature.
     *
     * @param key   The key to be removed.
     */
    protected void removeKey(String key) {
        if (key != null) keys.remove(key.toLowerCase(locale));
    }

    /**
     * Replaces an existing key with a new key.
     *
     * @param oldKey    The old key to be replaced by the new key.
     * @param newKey    The new key replacing the old key.
     */
    protected void replaceKey(String oldKey, String newKey) {
        removeKey(oldKey);
        addKey(newKey);
    }

    /**
     * Returns a new CaseInsensitiveIData wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new CaseInsensitiveIData wrapping the given IData document.
     */
    public static IData of(IData document) {
        return new CaseInsensitiveIData(document);
    }

    /**
     * Returns a new CaseInsensitiveIData[] representation of the given IData[] document list.
     *
     * @param array     An IData[] document list.
     * @return          A new CaseInsensitiveIData[] representation of the given IData[] document list.
     */
    public static IData[] of(IData[] array) {
        if (array == null) return null;

        IData[] output = new CaseInsensitiveIData[array.length];

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                output[i] = CaseInsensitiveIData.of(array[i]);
            }
        }

        return output;
    }

    /**
     * Static factory method used by IData XML deserialization.
     *
     * @return A new CaseInsensitiveIData instance.
     */
    public static IData create() {
        return new CaseInsensitiveIData();
    }

    /**
     * Implementation of the case-insensitive IDataCursor.
     */
    private class CaseInsensitiveIDataCursor extends IDataCursorEnvelope {
        /**
         * Constructs a new case-insensitive cursor.
         *
         * @param cursor The cursor to be wrapped.
         */
        public CaseInsensitiveIDataCursor(IDataCursor cursor) {
            super(cursor);
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         */
        @Override
        public Object getValue() {
            Object value = cursor.getValue();
            if (value instanceof IData[] || value instanceof Table) {
                value = of(IDataHelper.toIDataArray(value));
            } else if (value instanceof IData) {
                value = of(IDataHelper.toIData(value));
            }
            return value;
        }

        /**
         * Sets the key at the cursor's current position.
         *
         * @param key The key to be set.
         */
        public void setKey(String key) {
            replaceKey(getKey(), key);
            super.setKey(key);
        }

        /**
         * Deletes the element at the cursor's current position.
         *
         * @return True if the element was deleted.
         */
        public boolean delete() {
            removeKey(getKey());
            return super.delete();
        }

        /**
         * Inserts the given element before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         */
        public void insertBefore(String key, Object value) {
            addKey(key);
            super.insertBefore(key, value);
        }

        /**
         * Inserts the given element after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @param value The value to be inserted.
         */
        public void insertAfter(String key, Object value) {
            addKey(key);
            super.insertAfter(key, value);
        }

        /**
         * Inserts a new IData document with the given key before the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The newly inserted IData document.
         */
        public IData insertDataBefore(String key) {
            addKey(key);
            return super.insertDataBefore(key);
        }

        /**
         * Inserts a new IData document with the given key after the cursor's current position.
         *
         * @param key   The key to be inserted.
         * @return      The newly inserted IData document.
         */
        public IData insertDataAfter(String key) {
            addKey(key);
            return super.insertDataAfter(key);
        }

        /**
         * Moves the cursor's position to the next element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        public boolean next(String key) {
            return super.next(normalizeKey(key));
        }

        /**
         * Moves the cursor's position to the previous element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        public boolean previous(String key) {
            return super.previous(normalizeKey(key));
        }

        /**
         * Moves the cursor's position to the first element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        public boolean first(String key) {
            return super.first(normalizeKey(key));
        }

        /**
         * Moves the cursor's position to the last element with the given key.
         *
         * @param key   The key to reposition to.
         * @return      True if the cursor was repositioned.
         */
        public boolean last(String key) {
            return super.last(normalizeKey(key));
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         */
        @Override
        public IDataCursor getCursorClone() {
            return new CaseInsensitiveIDataCursor(cursor.getCursorClone());
        }
    }
}
