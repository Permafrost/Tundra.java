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
import java.util.Map;

/**
 * Wraps an IData in a read-only IData and Map compatible object.
 */
public class ReadOnlyIDataMap extends IDataMap implements Cloneable, Serializable {
    /**
     * The serialization identity for this class and version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Construct a new ReadOnlyIDataMap object.
     *
     * @param document The IData document to wrap in a read-only representation.
     */
    public ReadOnlyIDataMap(IData document) {
        super(document);
    }

    /**
     * Construct a new ReadOnlyIDataMap object.
     *
     * @param document   The IData document to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ReadOnlyIDataMap(IData document, IDataComparator comparator) {
        this(document);
        setComparator(comparator);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given IDataCodable object.
     *
     * @param codable The IDataCodable object to be wrapped.
     */
    public ReadOnlyIDataMap(IDataCodable codable) {
        super(codable);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given IDataCodable object.
     *
     * @param codable    The IDataCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ReadOnlyIDataMap(IDataCodable codable, IDataComparator comparator) {
        this(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given IDataPortable object.
     *
     * @param portable The IDataPortable object to be wrapped.
     */
    public ReadOnlyIDataMap(IDataPortable portable) {
        super(portable);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given IDataPortable object.
     *
     * @param portable   The IDataPortable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ReadOnlyIDataMap(IDataPortable portable, IDataComparator comparator) {
        this(portable);
        setComparator(comparator);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given ValuesCodable object.
     *
     * @param codable The ValuesCodable object to be wrapped.
     */
    public ReadOnlyIDataMap(ValuesCodable codable) {
        super(codable);
    }

    /**
     * Constructs a new ReadOnlyIDataMap wrapping the given ValuesCodable object.
     *
     * @param codable    The ValuesCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ReadOnlyIDataMap(ValuesCodable codable, IDataComparator comparator) {
        this(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new ReadOnlyIDataMap seeded with the given Map of key value entries.
     *
     * @param map The map to see this new object with.
     */
    public ReadOnlyIDataMap(Map<?, ?> map) {
        this(IDataHelper.toIData(map));
    }

    /**
     * Constructs a new ReadOnlyIDataMap seeded with the given Map of key value entries.
     *
     * @param map        The map to see this new object with.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public ReadOnlyIDataMap(Map<? extends String, ?> map, IDataComparator comparator) {
        this(map);
        setComparator(comparator);
    }

    /**
     * Returns a new ReadOnlyIDataMap wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new ReadOnlyIDataMap wrapping the given IData document.
     */
    public static ReadOnlyIDataMap of(IData document) {
        return new ReadOnlyIDataMap(document);
    }

    /**
     * Returns a new ReadOnlyIDataMap[] representation of the given IData[] document list.
     *
     * @param array     An IData[] document list.
     * @return          A new ReadOnlyIDataMap[] representation of the given IData[] document list.
     */
    public static ReadOnlyIDataMap[] of(IData[] array) {
        ReadOnlyIDataMap[] output = null;

        if (array != null) {
            output = new ReadOnlyIDataMap[array.length];
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    output[i] = ReadOnlyIDataMap.of(array[i]);
                }
            }
        }

        return output;
    }

    /**
     * Converts the given value if it is an IData or IData[] compatible object to a ReadOnlyIDataMap or
     * ReadOnlyIDataMap[] respectively.
     *
     * @param value The value to be normalized.
     * @return      If the value is an IData or IData[] compatible object, a new ReadOnlyIDataMap or
     *              ReadOnlyIDataMap[] respectively is returned which wraps the given value, otherwise
     *              the value itself is returned unmodified.
     */
    private static Object normalize(Object value) {
        if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
            value = ReadOnlyIDataMap.of(IDataHelper.toIDataArray(value));
        } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
            value = ReadOnlyIDataMap.of(IDataHelper.toIData(value));
        }
        return value;
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return new ReadOnlyIDataCursor(document.getCursor());
    }

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return new ReadOnlyIDataSharedCursor(document.getSharedCursor());
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
     * Removes the mapping for a key from this map if it is present (optional operation).
     *
     * @param key A key whose mapping is to be removed from the map.
     * @return The previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public Object remove(Object key) {
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   Key with which the specified value is to be associated.
     * @param value Value to be associated with the specified key.
     * @return The previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public Object put(String key, Object value) {
        return null;
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param map Mappings to be stored in this map.
     */
    @Override
    public void putAll(Map<? extends String, ?> map) {
        // do nothing as cursor is read only
    }

    /**
     * Copies all of the mappings from the specified document to this map.
     *
     * @param document Mappings to be stored in this map.
     */
    @Override
    public void putAll(IData document) {
        // do nothing as cursor is read only
    }

    /**
     * Copies all of the mappings from the specified document to this map.
     *
     * @param document Mappings to be stored in this map.
     */
    @Override
    public void merge(IData document) {
        // do nothing as cursor is read only
    }

    /**
     * Copies all of the mappings from the specified Map to this IDataMap.
     *
     * @param map A Map containing key value pairs to be stored in this IDataMap.
     */
    @Override
    public void merge(Map<? extends String, ?> map) {
        // do nothing as cursor is read only
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        // do nothing as cursor is read only
    }

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new ReadOnlyIDataMap((IData)null);
    }

    /**
     * Returns a clone of this IData object.
     *
     * @return A clone of this IData object.
     */
    @Override
    public ReadOnlyIDataMap clone() {
        return new ReadOnlyIDataMap(document);
    }

    /**
     * Read-only wrapper for an IDataCursor.
     */
    private static class ReadOnlyIDataCursor extends IDataCursorEnvelope {
        /**
         * Constructs a new read-only cursor.
         *
         * @param cursor The cursor to be wrapped.
         */
        public ReadOnlyIDataCursor(IDataCursor cursor) {
            super(cursor);
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param key Not used.
         */
        @Override
        public void setKey(String key) {
            // do nothing as cursor is read only
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         */
        @Override
        public Object getValue() {
            return normalize(cursor.getValue());
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param value Not used.
         */
        @Override
        public void setValue(Object value) {
            // do nothing as cursor is read only
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @return False.
         */
        @Override
        public boolean delete() {
            return false;
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param key   Not used.
         * @param value Not used.
         */
        @Override
        public void insertBefore(String key, Object value) {
            // do nothing as cursor is read only
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param key   Not used.
         * @param value Not used.
         */
        @Override
        public void insertAfter(String key, Object value) {
            // do nothing as cursor is read only
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param key   Not used.
         * @return      Null.
         */
        @Override
        public IData insertDataBefore(String key) {
            return null;
        }

        /**
         * Does nothing, as the cursor is read-only.
         *
         * @param key   Not used.
         * @return      Null.
         */
        @Override
        public IData insertDataAfter(String key) {
            return null;
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         */
        @Override
        public IDataCursor getCursorClone() {
            return new ReadOnlyIDataCursor(cursor.getCursorClone());
        }
    }

    /**
     * Read-only wrapper for an IDataSharedCursor.
     */
    private static class ReadOnlyIDataSharedCursor extends IDataSharedCursorEnvelope {
        /**
         * Constructs a new read-only cursor.
         *
         * @param cursor The cursor to be wrapped.
         */
        public ReadOnlyIDataSharedCursor(IDataSharedCursor cursor) {
            super(cursor);
        }

        /**
         * Does nothing, as this cursor is read-only.
         *
         * @param key Not used.
         * @throws DataException Never thrown.
         */
        @Override
        public void setKey(String key) throws DataException {
            // do nothing as cursor is read only
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         * @throws DataException If an error occurs.
         */
        @Override
        public Object getValue() throws DataException {
            return normalize(cursor.getValue());
        }

        /**
         * Does nothing, as this cursor is read-only.
         *
         * @param value Not used.
         * @throws DataException Never thrown.
         */
        @Override
        public void setValue(Object value) throws DataException {
            // do nothing as cursor is read only
        }

        /**
         * Returns the value at the cursor's current position.
         *
         * @return The value at the cursor's current position.
         * @throws DataException If an error occurs.
         */
        @Override
        public Object getValueReference() throws DataException {
            return normalize(cursor.getValueReference());
        }

        /**
         * Does nothing, as this is a read only cursor.
         *
         * @return False.
         * @throws DataException Never thrown.
         */
        @Override
        public boolean delete() throws DataException {
            return false;
        }

        /**
         * Does nothing, as this is a read only cursor.
         *
         * @param key   Not used.
         * @param value Not used.
         * @throws DataException Never thrown.
         */
        @Override
        public void insertBefore(String key, Object value) throws DataException {
            // do nothing as cursor is read only
        }

        /**
         * Does nothing, as this is a read only cursor.
         *
         * @param key   Not used.
         * @param value Not used.
         * @throws DataException Never thrown.
         */
        @Override
        public void insertAfter(String key, Object value) throws DataException {
            // do nothing as cursor is read only
        }

        /**
         * Does nothing, as this is a read only cursor.
         *
         * @param key   Not used.
         * @return      Null.
         * @throws DataException Never thrown.
         */
        @Override
        public IData insertDataBefore(String key) throws DataException {
            return null;
        }

        /**
         * Does nothing, as this is a read only cursor.
         *
         * @param key   Not used.
         * @return      Null.
         * @throws DataException Never thrown.
         */
        @Override
        public IData insertDataAfter(String key) throws DataException {
            return null;
        }

        /**
         * Returns a clone of this cursor.
         *
         * @return A clone of this cursor.
         * @throws DataException If an error occurs.
         */
        @Override
        public IDataSharedCursor getCursorClone() throws DataException {
            return new ReadOnlyIDataSharedCursor(cursor.getCursorClone());
        }
    }
}
