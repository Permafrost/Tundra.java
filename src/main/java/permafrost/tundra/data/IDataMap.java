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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Wraps an IData document in an implementation of the Iterable, Comparable, and Map interfaces.
 */
public class IDataMap extends IDataAdapter implements Iterable<Map.Entry<String, Object>>, Comparable<IData>, Map<String, Object>, Cloneable, Serializable {
    private static final long serialVersionUID = 1;
    protected IDataComparator comparator;

    /**
     * Construct a new IDataMap object.
     */
    public IDataMap() {
        super();
    }

    /**
     * Construct a new IDataMap object.
     *
     * @param document The IData document to be wrapped.
     */
    public IDataMap(IData document) {
        this(document, null);
    }

    /**
     * Construct a new IDataMap object.
     *
     * @param document   The IData document to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public IDataMap(IData document, IDataComparator comparator) {
        super(document);
        setComparator(comparator);
    }

    /**
     * Constructs a new IDataMap wrapping the given IDataCodable object.
     *
     * @param codable The IDataCodable object to be wrapped.
     */
    public IDataMap(IDataCodable codable) {
        this(codable, null);
    }

    /**
     * Constructs a new IDataMap wrapping the given IDataCodable object.
     *
     * @param codable    The IDataCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public IDataMap(IDataCodable codable, IDataComparator comparator) {
        super(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new IDataMap wrapping the given IDataPortable object.
     *
     * @param portable The IDataPortable object to be wrapped.
     */
    @SuppressWarnings("deprecation")
    public IDataMap(IDataPortable portable) {
        this(portable, null);
    }

    /**
     * Constructs a new IDataMap wrapping the given IDataPortable object.
     *
     * @param portable   The IDataPortable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    @SuppressWarnings("deprecation")
    public IDataMap(IDataPortable portable, IDataComparator comparator) {
        super(portable);
        setComparator(comparator);
    }

    /**
     * Constructs a new IDataMap wrapping the given ValuesCodable object.
     *
     * @param codable The ValuesCodable object to be wrapped.
     */
    public IDataMap(ValuesCodable codable) {
        this(codable, null);
    }

    /**
     * Constructs a new IDataMap wrapping the given ValuesCodable object.
     *
     * @param codable    The ValuesCodable object to be wrapped.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public IDataMap(ValuesCodable codable, IDataComparator comparator) {
        super(codable);
        setComparator(comparator);
    }

    /**
     * Constructs a new IDataMap seeded with the given Map of key value entries.
     *
     * @param map The map to see this new object with.
     */
    public IDataMap(Map<?, ?> map) {
        this(IDataHelper.toIData(map));
    }

    /**
     * Constructs a new IDataMap seeded with the given Map of key value entries.
     *
     * @param map        The map to see this new object with.
     * @param comparator The IDataComparator to be used to compare IData objects.
     */
    public IDataMap(Map<?, ?> map, IDataComparator comparator) {
        this(map);
        setComparator(comparator);
    }

    /**
     * Returns a Collection view of the values contained in this map.
     *
     * @return A collection view of the values contained in this map.
     */
    @Override
    public Collection<Object> values() {
        return Arrays.asList(IDataHelper.getValues(this));
    }

    /**
     * Returns a Set view of the keys contained in this map.
     *
     * @return A set view of the keys contained in this map.
     */
    @Override
    public Set<String> keySet() {
        String[] keys = IDataHelper.getKeys(this);
        Set<String> keySet = new LinkedHashSet<String>(keys.length);
        keySet.addAll(Arrays.asList(keys));

        return keySet;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key   A key whose presence in this map is to be tested.
     * @return      True if this map contains a mapping for the specified key.
     */
    @Override
    public boolean containsKey(Object key) {
        IDataCursor cursor = this.getCursor();
        boolean contains = cursor.next((String)key);
        cursor.destroy();

        return contains;
    }

    /**
     * Returns true if this map maps one or more keys to the specified value.
     *
     * @param value The value whose presence in this map is to be tested.
     * @return      True if this map maps one or more keys to the specified value.
     */
    @Override
    public boolean containsValue(Object value) {
        return Arrays.binarySearch(IDataHelper.getValues(this), value) >= 0;
    }

    /**
     * Removes the mapping for a key from this map if it is present (optional operation).
     *
     * @param key   A key whose mapping is to be removed from the map.
     * @return      The previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public Object remove(Object key) {
        IDataCursor cursor = this.getCursor();
        Object value = get(key);
        IDataUtil.remove(cursor, (String)key);
        return value;
    }

    /**
     * Returns true if this map contains no key-value mappings.
     *
     * @return True if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return The number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return IDataHelper.size(this);
    }

    /**
     * Returns a Set view of the mappings contained in this map.
     *
     * @return A set view of the mappings contained in this map.
     */
    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> set = new LinkedHashSet<Map.Entry<String, Object>>(size());

        for (Map.Entry<String, Object> entry : this) {
            set.add(entry);
        }

        return set;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key           Key with which the specified value is to be associated.
     * @param value         Value to be associated with the specified key.
     * @param includeNull   If true, null values will be inserted into the map.
     * @return              The previous value associated with key, or null if there was no mapping for key.
     */
    public Object put(String key, Object value, boolean includeNull) {
        Object previousValue = get(key);

        if (includeNull || value != null) {
            IDataCursor cursor = this.getCursor();
            IDataUtil.put(cursor, key, value);
            cursor.destroy();
        } else {
            IDataCursor cursor = this.getCursor();
            IDataUtil.remove(cursor, key);
            cursor.destroy();
        }

        return previousValue;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   Key with which the specified value is to be associated.
     * @param value Value to be associated with the specified key.
     * @return      The previous value associated with key, or null if there was no mapping for key.
     */
    @Override
    public Object put(String key, Object value) {
        return put(key, value, true);
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     *
     * @param map Mappings to be stored in this map.
     */
    @Override
    public void putAll(Map<? extends String, ?> map) {
        for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Copies all of the mappings from the specified document to this map.
     *
     * @param document Mappings to be stored in this map.
     */
    public void putAll(IData document) {
        putAll((Map)IDataMap.of(document));
    }

    /**
     * Copies all of the mappings from the specified document to this map.
     *
     * @param document Mappings to be stored in this map.
     */
    public void merge(IData document) {
        putAll(document);
    }

    /**
     * Copies all of the mappings from the specified Map to this IDataMap.
     *
     * @param map A Map containing key value pairs to be stored in this IDataMap.
     */
    public void merge(Map<? extends String, ?> map) {
        putAll(map);
    }

    /**
     * Returns a new IDataMap wrapping the given IData document.
     *
     * @param document  The document to be wrapped.
     * @return          A new IDataMap wrapping the given IData document.
     */
    public static IDataMap of(IData document) {
        return new IDataMap(document);
    }

    /**
     * Returns a new IDataMap[] representation of the given IData[] document list.
     *
     * @param array An IData[] document list.
     * @return      A new IDataMap[] representation of the given IData[] document list.
     */
    public static IDataMap[] of(IData[] array) {
        IDataMap[] output = null;

        if (array != null) {
            output = new IDataMap[array.length];
            for (int i = 0; i < array.length; i++) {
                output[i] = new IDataMap(array[i]);
            }
        }

        return output;
    }

    /**
     * Returns a new IDataMap that includes all the key value pairs from the given Map.
     *
     * @param map   The Map to seed the new IDataMap with.
     * @return      A new IDataMap that includes all the key value pairs from the given Map.
     */
    public static IDataMap of(Map<? extends String, ?> map) {
        return new IDataMap(map);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param key   The key whose associated value is to be returned.
     * @return      The value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @Override
    public Object get(Object key) {
        IDataCursor cursor = getCursor();
        Object value = IDataUtil.get(cursor, (String)key);
        cursor.destroy();

        return value;
    }

    /**
     * Removes all of the mappings from this map. The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        IDataHelper.clear(this);
    }

    /**
     * Returns an iterator over a set of elements of type Map.Entry.
     *
     * @return An iterator.
     */
    @Override
    public IDataIterator iterator() {
        return new IDataIteratorImplementation(getCursor());
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param other The object to be compared with this object.
     * @return      A negative integer, zero, or a positive integer as this object is less than, equal to, or greater
     *              than the specified object.
     */
    @Override
    public int compareTo(IData other) {
        return comparator.compare(document, other);
    }

    /**
     * Returns true if this object is equal to the given object.
     *
     * @param other The object to compare to.
     * @return      True if this object is equal to the given object.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof IData)) return false;
        return this == other || comparator.compare(document, (IData)other) == 0;
    }

    /**
     * Returns the IDataComparator used to compare IData objects.
     *
     * @return The IDataComparator used to compare IData objects.
     */
    public IDataComparator getComparator() {
        return comparator;
    }

    /**
     * Sets the IDataComparator to be used when comparing IData objects.
     *
     * @param comparator The IDataComparator to be used when comparing IData objects.
     */
    public void setComparator(IDataComparator comparator) {
        if (comparator == null) {
            this.comparator = new BasicIDataComparator();
        } else {
            this.comparator = comparator;
        }
    }

    /**
     * Returns a newly created IData object.
     *
     * @return A newly created IData object.
     */
    public static IData create() {
        return new IDataMap();
    }

    /**
     * Returns a clone of this IData object.
     *
     * @return A clone of this IData object.
     */
    @Override
    public IDataMap clone() {
        return new IDataMap(IDataHelper.duplicate(document));
    }

    /**
     * Implementation class for an IDataIterator.
     */
    private static class IDataIteratorImplementation implements IDataIterator {
        protected IDataCursor cursor;

        /**
         * Constructs a new IDataIterator object for iterating with the given IDataCursor.
         *
         * @param cursor The cursor to be iterated with.
         */
        public IDataIteratorImplementation(IDataCursor cursor) {
            this.cursor = cursor;
        }

        /**
         * Returns true if the iteration has more elements. (In other words, returns true if next() would return an
         * element rather than throwing an exception.)
         *
         * @return True if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return cursor != null && cursor.hasMoreData();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return The next element in the iteration.
         * @throws NoSuchElementException If the iteration has no more elements.
         */
        @Override
        public Map.Entry<String, Object> next() throws NoSuchElementException {
            if (cursor != null && cursor.next()) {
                return new AbstractMap.SimpleImmutableEntry<String, Object>(cursor.getKey(), cursor.getValue());
            } else {
                throw new NoSuchElementException("No more elements were available for iteration in IData document");
            }
        }

        /**
         * Throws an UnsupportedOperationException because the remove operation is not supported by this iterator.
         *
         * @throws UnsupportedOperationException The remove operation is not supported by this iterator.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove method is not implemented by this iterator class");
        }
    }
}
