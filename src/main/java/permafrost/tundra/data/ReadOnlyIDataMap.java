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
import com.wm.txn.ITransaction;
import com.wm.txn.TransactionException;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.util.Map;

public class ReadOnlyIDataMap extends IDataMap {
    /**
     * Construct a new ReadOnlyIDataMap object.
     *
     * @param document The IData document to wrap in a read-only representation.
     */
    public ReadOnlyIDataMap(IData document) {
        super(IDataHelper.duplicate(document, true));
        // recursively freeze all child IData and IData[] elements as read-only
        freeze();
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
        // recursively freeze all child IData and IData[] elements as read-only
        freeze();
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
        // recursively freeze all child IData and IData[] elements as read-only
        freeze();
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
        // recursively freeze all child IData and IData[] elements as read-only
        freeze();
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
    public ReadOnlyIDataMap(Map<?, ?> map, IDataComparator comparator) {
        this(map);
        setComparator(comparator);
    }

    /**
     * Returns a new IDataMap wrapping the given IData document.
     *
     * @param document The document to be wrapped.
     * @return A new IDataMap wrapping the given IData document.
     */
    public static ReadOnlyIDataMap of(IData document) {
        if (document instanceof ReadOnlyIDataMap) {
            return (ReadOnlyIDataMap)document;
        } else {
            return new ReadOnlyIDataMap(document);
        }
    }

    /**
     * Returns a new IDataMap[] representation of the given IData[] document list.
     *
     * @param array An IData[] document list.
     * @return A new IDataMap[] representation of the given IData[] document list.
     */
    public static ReadOnlyIDataMap[] of(IData[] array) {
        ReadOnlyIDataMap[] output = null;

        if (array instanceof ReadOnlyIDataMap[]) {
            output = (ReadOnlyIDataMap[])array;
        } else if (array != null) {
            output = new ReadOnlyIDataMap[array.length];
            for (int i = 0; i < array.length; i++) {
                output[i] = new ReadOnlyIDataMap(array[i]);
            }
        }

        return output;
    }

    /**
     * Converts all IData and IData[] compatible elements to read-only representations.
     */
    private void freeze() {
        IDataCursor cursor = document.getCursor();

        while (cursor.next()) {
            Object value = cursor.getValue();
            if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                cursor.setValue(ReadOnlyIDataMap.of(IDataHelper.toIDataArray(value)));
            } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                cursor.setValue(ReadOnlyIDataMap.of(IDataHelper.toIData(value)));
            }
        }

        cursor.destroy();
    }

    /**
     * Returns an IDataCursor for this IData object. An IDataCursor contains the basic methods you use to traverse an
     * IData object and get or set elements within it.
     *
     * @return An IDataCursor for this object.
     */
    @Override
    public IDataCursor getCursor() {
        return ReadOnlyIDataCursor.of(document.getCursor());
    }

    /**
     * Returns an IDataSharedCursor for this IData object. An IDataSharedCursor contains the basic methods you use to
     * traverse an IData object and get or set elements within it.
     *
     * @return An IDataSharedCursor for this object.
     */
    @Override
    public IDataSharedCursor getSharedCursor() {
        return ReadOnlyIDataSharedCursor.of(document.getSharedCursor());
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
     * Read-only wrapper for an IDataCursor.
     */
    private static class ReadOnlyIDataCursor implements IDataCursor {
        protected IDataCursor cursor;

        public ReadOnlyIDataCursor(IDataCursor cursor) {
            if (cursor == null) throw new NullPointerException("cursor must not be null");
            this.cursor = cursor;
        }

        public static ReadOnlyIDataCursor of(IDataCursor cursor) {
            ReadOnlyIDataCursor readOnlyIDataCursor = null;

            if (cursor instanceof ReadOnlyIDataCursor) {
                readOnlyIDataCursor = (ReadOnlyIDataCursor)cursor;
            } else if (cursor != null) {
                readOnlyIDataCursor = new ReadOnlyIDataCursor(cursor);
            }

            return readOnlyIDataCursor;
        }

        public void setErrorMode(int mode) {
            cursor.setErrorMode(mode);
        }

        public DataException getLastError() {
            return cursor.getLastError();
        }

        public boolean hasMoreErrors() {
            return cursor.hasMoreErrors();
        }

        public void home() {
            cursor.home();
        }

        public String getKey() {
            return cursor.getKey();
        }

        public void setKey(String key) {
            // do nothing as cursor is read only
        }

        public Object getValue() {
            return cursor.getValue();
        }

        public void setValue(Object value) {
            // do nothing as cursor is read only
        }

        public boolean delete() {
            return false;
        }

        public void insertBefore(String key, Object value) {
            // do nothing as cursor is read only
        }

        public void insertAfter(String key, Object value) {
            // do nothing as cursor is read only
        }

        public IData insertDataBefore(String key) {
            return null;
        }

        public IData insertDataAfter(String key) {
            return null;
        }

        public boolean next() {
            return cursor.next();
        }

        public boolean next(String key) {
            return cursor.next(key);
        }

        public boolean previous() {
            return cursor.previous();
        }

        public boolean previous(String key) {
            return cursor.previous(key);
        }

        public boolean first() {
            return cursor.first();
        }

        public boolean first(String key) {
            return cursor.first(key);
        }

        public boolean last() {
            return cursor.last();
        }

        public boolean last(String key) {
            return cursor.last(key);
        }

        public boolean hasMoreData() {
            return cursor.hasMoreData();
        }

        public void destroy() {
            cursor.destroy();
        }

        public IDataCursor getCursorClone() {
            return of(cursor.getCursorClone());
        }
    }

    /**
     * Read-only wrapper for an IDataSharedCursor.
     */
    private static class ReadOnlyIDataSharedCursor implements IDataSharedCursor {
        IDataSharedCursor cursor;

        public ReadOnlyIDataSharedCursor(IDataSharedCursor cursor) {
            if (cursor == null) throw new NullPointerException("cursor must not be null");
            this.cursor = cursor;
        }

        public static ReadOnlyIDataSharedCursor of(IDataSharedCursor cursor) {
            ReadOnlyIDataSharedCursor readOnlyIDataSharedCursor = null;

            if (cursor instanceof ReadOnlyIDataCursor) {
                readOnlyIDataSharedCursor = (ReadOnlyIDataSharedCursor)cursor;
            } else if (cursor != null) {
                readOnlyIDataSharedCursor = new ReadOnlyIDataSharedCursor(cursor);
            }

            return readOnlyIDataSharedCursor;
        }

        public void home() throws DataException {
            cursor.home();
        }

        public String getKey() throws DataException {
            return cursor.getKey();
        }

        public void setKey(String key) throws DataException {
            // do nothing as cursor is read only
        }

        public Object getValue() throws DataException {
            return cursor.getValue();
        }

        public void setValue(Object value) throws DataException {
            // do nothing as cursor is read only
        }

        public Object getValueReference() throws DataException {
            return cursor.getValueReference();
        }

        public boolean delete() throws DataException {
            return false;
        }

        public void insertBefore(String key, Object value) throws DataException {
            // do nothing as cursor is read only
        }

        public void insertAfter(String key, Object value) throws DataException {
            // do nothing as cursor is read only
        }

        public IData insertDataBefore(String key) throws DataException {
            return null;
        }

        public IData insertDataAfter(String key) throws DataException {
            return null;
        }

        public boolean next() throws DataException {
            return cursor.next();
        }

        public boolean next(String key) throws DataException {
            return cursor.next(key);
        }

        public boolean previous() throws DataException {
            return cursor.previous();
        }

        public boolean previous(String key) throws DataException {
            return cursor.previous(key);
        }

        public boolean first() throws DataException {
            return cursor.first();
        }

        public boolean first(String key) throws DataException {
            return cursor.first(key);
        }

        public boolean last() throws DataException {
            return cursor.last();
        }

        public boolean last(String key) throws DataException {
            return cursor.last(key);
        }

        public boolean hasMoreData() throws DataException {
            return cursor.hasMoreData();
        }

        public void destroy() {
            cursor.destroy();
        }

        public IDataSharedCursor getCursorClone() throws DataException {
            return of(cursor.getCursorClone());
        }

        public boolean isTXNSupported() {
            return cursor.isTXNSupported();
        }

        public ITransaction startTXN() throws TransactionException {
            return cursor.startTXN();
        }

        public void txnJoin(ITransaction transaction) throws TransactionException {
            cursor.txnJoin(transaction);
        }

        public void txnAborted() throws TransactionException {
            cursor.txnAborted();
        }

        public void txnCommitted() throws TransactionException {
            cursor.txnCommitted();
        }
    }
}
