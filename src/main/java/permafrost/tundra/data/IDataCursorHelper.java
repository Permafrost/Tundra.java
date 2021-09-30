/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
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
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Collection of convenience methods for working with IDataCursor objects.
 */
public class IDataCursorHelper {
    /**
     * Disallow instantiation of this class.
     */
    private IDataCursorHelper() {}

    /**
     * Append all elements in the given source cursor to the end of the given target cursor.
     *
     * @param sourceCursor  The cursor containing elements to be appended.
     * @param targetCursor  The cursor to which the elements will be appended.
     */
    public static void append(IDataCursor sourceCursor, IDataCursor targetCursor) {
        if (sourceCursor != null && targetCursor != null) {
            targetCursor.last();
            if (sourceCursor.first()) {
                targetCursor.insertAfter(sourceCursor.getKey(), sourceCursor.getValue());
                while (sourceCursor.next()) {
                    targetCursor.insertAfter(sourceCursor.getKey(), sourceCursor.getValue());
                }
            }
        }
    }

    /**
     * Prepend all elements in the given source cursor to the beginning of the given target cursor.
     *
     * @param sourceCursor  The cursor containing elements to be appended.
     * @param targetCursor  The cursor to which the elements will be appended.
     */
    public static void prepend(IDataCursor sourceCursor, IDataCursor targetCursor) {
        if (sourceCursor != null && targetCursor != null) {
            targetCursor.first();
            if (sourceCursor.last()) {
                targetCursor.insertBefore(sourceCursor.getKey(), sourceCursor.getValue());
                while (sourceCursor.previous()) {
                    targetCursor.insertBefore(sourceCursor.getKey(), sourceCursor.getValue());
                }
            }
        }
    }

    /**
     * Removes all elements from the given cursor.
     *
     * @param cursor    The cursor from which to remove all elements.
     */
    public static void clear(IDataCursor cursor) {
        clear(cursor, null);
    }

    /**
     * Removes all elements from the given cursor.
     *
     * @param cursor    The cursor from which to remove all elements.
     */
    public static void clear(IDataCursor cursor, Iterable<String> keysToPreserve) {
        if (cursor != null) {
            IDataCursor preservedCursor = null;
            try {
                if (keysToPreserve != null) {
                    IData preservedDocument = IDataFactory.create();
                    preservedCursor = preservedDocument.getCursor();
                    for (String key : keysToPreserve) {
                        if (key != null) {
                            while(cursor.first(key)) {
                                preservedCursor.insertAfter(key, cursor.getValue());
                                cursor.delete();
                            }
                        }
                    }
                }
                cursor.first();
                while(cursor.delete());
                append(preservedCursor, cursor);
            } finally {
                if (preservedCursor != null) {
                    preservedCursor.destroy();
                }
            }
        }
    }

    /**
     * Positions the cursor on the first element with the given key.
     *
     * @param cursor        The cursor to be positioned.
     * @param key           The element's key.
     * @return              True if the key existed and the cursor was repositioned, otherwise false.
     */
    public static boolean first(IDataCursor cursor, String key) {
        return first(cursor, Object.class, key);
    }

    /**
     * Positions the cursor on the first element with the given key whose value has the specified class.
     *
     * @param cursor        The cursor to be positioned.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              True if the key existed with a value of the required class and the cursor was repositioned,
     *                      otherwise false.
     */
    public static <V> boolean first(IDataCursor cursor, Class<V> valueClass, String key) {
        boolean found = false;
        if (cursor != null && cursor.first(key)) {
            Object candidateValue = cursor.getValue();
            if (valueClass.isInstance(candidateValue)) {
                found = true;
            } else if (next(cursor, valueClass, key)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Returns true if an element with the given key exists in the given cursor.
     *
     * @param cursor        The cursor to be positioned.
     * @param key           The element's key.
     * @return              True if the key existed, otherwise false.
     */
    public static boolean exists(IDataCursor cursor, String key) {
        return exists(cursor, Object.class, key);
    }

    /**
     * Returns true if an element with the given key whose value has the specified class exists in the given cursor.
     *
     * @param cursor        The cursor to be positioned.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              True if the key existed with a value of the required class, otherwise false.
     */
    public static <V> boolean exists(IDataCursor cursor, Class<V> valueClass, String key) {
        return first(cursor, valueClass, key);
    }

    /**
     * Positions the cursor on the next element with the given key.
     *
     * @param cursor        The cursor to be positioned.
     * @param key           The element's key.
     * @return              True if the key existed and the cursor was repositioned, otherwise false.
     */
    public static boolean next(IDataCursor cursor, String key) {
        return next(cursor, Object.class, key);
    }

    /**
     * Positions the cursor on the next element with the given key whose value has the specified class.
     *
     * @param cursor        The cursor to be positioned.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              True if the key existed with a value of the required class and the cursor was repositioned,
     *                      otherwise false.
     */
    public static <V> boolean next(IDataCursor cursor, Class<V> valueClass, String key) {
        boolean found = false;
        if (cursor != null) {
            while(cursor.next(key)) {
                Object candidateValue = cursor.getValue();
                if (valueClass.isInstance(candidateValue)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Positions the cursor on the previous element with the given key.
     *
     * @param cursor        The cursor to be positioned.
     * @param key           The element's key.
     * @return              True if the key existed and the cursor was repositioned, otherwise false.
     */
    public static boolean previous(IDataCursor cursor, String key) {
        return previous(cursor, Object.class, key);
    }

    /**
     * Positions the cursor on the previous element with the given key whose value has the specified class.
     *
     * @param cursor        The cursor to be positioned.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              True if the key existed with a value of the required class and the cursor was repositioned,
     *                      otherwise false.
     */
    public static <V> boolean previous(IDataCursor cursor, Class<V> valueClass, String key) {
        boolean found = false;
        if (cursor != null) {
            while(cursor.previous(key)) {
                Object candidateValue = cursor.getValue();
                if (valueClass.isInstance(candidateValue)) {
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    /**
     * Positions the cursor on the last element with the given key.
     *
     * @param cursor        The cursor to be positioned.
     * @param key           The element's key.
     * @return              True if the key existed and the cursor was repositioned, otherwise false.
     */
    public static boolean last(IDataCursor cursor, String key) {
        return last(cursor, Object.class, key);
    }

    /**
     * Positions the cursor on the last element with the given key whose value has the specified class.
     *
     * @param cursor        The cursor to be positioned.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              True if the key existed with a value of the required class and the cursor was repositioned,
     *                      otherwise false.
     */
    public static <V> boolean last(IDataCursor cursor, Class<V> valueClass, String key) {
        boolean found = false;
        if (cursor != null && cursor.last(key)) {
            Object candidateValue = cursor.getValue();
            if (valueClass.isInstance(candidateValue)) {
                found = true;
            } else if (previous(cursor, valueClass, key)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Returns the value of the first element in the cursor with the given key.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @return              The value associated with the given key, if any.
     */
    public static Object get(IDataCursor cursor, String key) {
        return get(cursor, Object.class, key);
    }

    /**
     * Returns the value of the first element in the cursor with the given key whose value has the specified class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              The value associated with the given key, if any.
     */
    @SuppressWarnings("unchecked")
    public static <V> V get(IDataCursor cursor, Class<V> valueClass, String key) {
        V value;
        if (first(cursor, valueClass, key)) {
            value = (V)cursor.getValue();
        } else {
            value = null;
        }
        return value;
    }

    /**
     * Returns the values of all elements in the cursor with the given key.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @return              The values associated with the given key.
     */
    public static List<Object> list(IDataCursor cursor, String key) {
        return list(cursor, Object.class, key);
    }

    /**
     * Returns the values of all elements in the cursor with the given key whose value has the specified class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              The values associated with the given key.
     */
    @SuppressWarnings("unchecked")
    public static <V> List<V> list(IDataCursor cursor, Class<V> valueClass, String key) {
        List<V> values;
        if (first(cursor, valueClass, key)) {
            values = new ArrayList<V>();
            values.add((V)cursor.getValue());
            while(next(cursor, valueClass, key)) {
                values.add((V)cursor.getValue());
            }
        } else {
            values = Collections.emptyList();
        }
        return values;
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @param value         The value to be associated with the given key.
     */
    public static void put(IDataCursor cursor, String key, Object value) {
        put(cursor, Object.class, key, value);
    }

    /**
     * Associates the given key with the given value in an IDataCursor.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param value         The value to be associated with the given key.
     * @param <V>           The required class of the element's value.
     */
    public static <V> void put(IDataCursor cursor, Class<V> valueClass, String key, V value) {
        if (first(cursor, valueClass, key)) {
            cursor.setValue(value);
        } else {
            cursor.insertAfter(key, value);
        }
    }

    /**
     * Removes the first element in the cursor with the given key whose value.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @return              The value associated with the given key, if any.
     */
    public static Object remove(IDataCursor cursor, String key) {
        return remove(cursor, Object.class, key);
    }

    /**
     * Removes the first element in the cursor with the given key whose value has the specified class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              The value associated with the given key, if any.
     */
    @SuppressWarnings("unchecked")
    public static <V> V remove(IDataCursor cursor, Class<V> valueClass, String key) {
        V value;
        if (first(cursor, valueClass, key)) {
            value = (V)cursor.getValue();
            cursor.delete();
        } else {
            value = null;
        }
        return value;
    }

    /**
     * Removes all elements in the cursor with the given key.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @return              The value associated with the given key, if any.
     */
    public static List<Object> removeAll(IDataCursor cursor, String key) {
        return removeAll(cursor, Object.class, key);
    }

    /**
     * Removes all elements in the cursor with the given key whose value has the specified class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param key           The element's key.
     * @param <V>           The required class of the element's value.
     * @return              The value associated with the given key, if any.
     */
    @SuppressWarnings("unchecked")
    public static <V> List<V> removeAll(IDataCursor cursor, Class<V> valueClass, String key) {
        List<V> values = new ArrayList<V>();
        while(first(cursor, valueClass, key)) {
            values.add((V) cursor.getValue());
            cursor.delete();
        }
        return values;
    }

    /**
     * Renames the first element associated with the given source key to have the given target key.
     *
     * @param cursor        The cursor containing elements.
     * @param sourceKey     The element's key before renaming.
     * @param targetKey     The element's key after renaming.
     * @return              The value associated with the given key, if any.
     */
    public static Object rename(IDataCursor cursor, String sourceKey, String targetKey) {
        return rename(cursor, Object.class, sourceKey, targetKey);
    }

    /**
     * Renames the first element associated with the given source key whose value has the specified class to have the
     * given target key.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param sourceKey     The element's key before renaming.
     * @param targetKey     The element's key after renaming.
     * @param <V>           The required class of the element's value.
     * @return              The value associated with the given key, if any.
     */
    @SuppressWarnings("unchecked")
    public static <V> V rename(IDataCursor cursor, Class<V> valueClass, String sourceKey, String targetKey) {
        V value;
        if (first(cursor, valueClass, sourceKey)) {
            value = (V)cursor.getValue();
            cursor.delete();
            cursor.insertAfter(targetKey, value);
        } else {
            value = null;
        }
        return value;
    }

    /**
     * Renames all elements associated with the given source key to have the given target key.
     *
     * @param cursor        The cursor containing elements.
     * @param sourceKey     The element's key before renaming.
     * @param targetKey     The element's key after renaming.
     * @return              The value associated with the given key, if any.
     */
    public static List<Object> renameAll(IDataCursor cursor, String sourceKey, String targetKey) {
        return renameAll(cursor, Object.class, sourceKey, targetKey);
    }

    /**
     * Renames all elements associated with the given source key whose value has the specified class to have the given
     * target key.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's value.
     * @param sourceKey     The element's key before renaming.
     * @param targetKey     The element's key after renaming.
     * @param <V>           The required class of the element's value.
     * @return              The value associated with the given key, if any.
     */
    @SuppressWarnings("unchecked")
    public static <V> List<V> renameAll(IDataCursor cursor, Class<V> valueClass, String sourceKey, String targetKey) {
        List<V> values = new ArrayList<V>();
        while(first(cursor, valueClass, sourceKey)) {
            V value = (V)cursor.getValue();
            values.add(value);
            cursor.delete();
            cursor.insertAfter(targetKey, value);
        }
        return values;
    }

    /**
     * Replaces the value of the first element in the cursor with the given key.
     *
     * @param cursor        The cursor containing elements.
     * @param key           The element's key.
     * @param newValue      The new value to be associated with the given key.
     * @return              True if the value was replaced.
     */
    public static boolean replace(IDataCursor cursor, String key, Object newValue) {
        return replace(cursor, Object.class, key, newValue);
    }

    /**
     * Replaces the value of the first element in the cursor with the given key whose existing value has the specified
     * class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's existing value.
     * @param key           The element's key.
     * @param newValue      The new value to be associated with the given key.
     * @param <V>           The required class of the element's existing value.
     * @return              True if the value was replaced.
     */
    public static <V> boolean replace(IDataCursor cursor, Class<V> valueClass, String key, Object newValue) {
        boolean replaced;
        if (first(cursor, valueClass, key)) {
            cursor.setValue(newValue);
            replaced = true;
        } else {
            replaced = false;
        }
        return replaced;
    }

    /**
     * Replaces all elements in the given target cursor with the elements in the given source cursor.
     *
     * @param sourceCursor  The cursor containing the elements to be used to replace the target cursor elements.
     * @param targetCursor  The cursor whose elements are to be replaced with the source cursor elements.
     */
    public static void replace(IDataCursor sourceCursor, IDataCursor targetCursor) {
         clear(targetCursor);
         append(sourceCursor, targetCursor);
    }

    /**
     * Lexicographically sorts the elements in the given cursor by key.
     *
     * @param cursor    The cursor whose elements are to be sorted.
     */
    public static void sort(IDataCursor cursor) {
        if (cursor != null && cursor.first()) {
            SortedSet<String> keys = new TreeSet<String>();
            insertKeysInto(cursor, keys);
            IData sortedDocument = IDataFactory.create();
            IDataCursor sortedCursor = sortedDocument.getCursor();
            try {
                for(String key : keys) {
                    if (cursor.first(key)) {
                        sortedCursor.insertAfter(key, cursor.getValue());
                        while(cursor.next(key)) {
                            sortedCursor.insertAfter(key, cursor.getValue());
                        }
                    }
                }
                replace(sortedCursor, cursor);
            } finally {
                sortedCursor.destroy();
            }
        }
    }

    /**
     * Returns true if the given cursor is empty.
     *
     * @param cursor    The cursor to be checked if empty.
     * @return          True if the cursor contains no elements, otherwise false.
     */
    public static boolean isEmpty(IDataCursor cursor) {
        return cursor == null || !cursor.first();
    }

    /**
     * Returns the number of items in the given cursor.
     *
     * @param cursor    The cursor whose size is to be returned.
     * @return          The number of items in the given cursor.
     */
    public static int size(IDataCursor cursor) {
        int size = 0;
        if (cursor != null && cursor.first()) {
            size++;
            while (cursor.next()) {
                size++;
            }
        }
        return size;
    }

    /**
     * Returns the number of items in the given cursor.
     *
     * @param cursor    The cursor whose size is to be returned.
     * @return          The number of items in the given cursor.
     */
    public static int size(IDataCursor cursor, String key) {
        return size(cursor, Object.class, key);
    }

    /**
     * Returns the number of items in the given cursor.
     *
     * @param cursor    The cursor whose size is to be returned.
     * @return          The number of items in the given cursor.
     */
    public static <V> int size(IDataCursor cursor, Class<V> valueClass, String key) {
        int size = 0;
        if (cursor != null && first(cursor, valueClass, key)) {
            size++;
            while (next(cursor, valueClass, key)) {
                size++;
            }
        }
        return size;
    }

    /**
     * Returns the list of keys present in the given cursor.
     *
     * @param cursor    The cursor to return the list of keys from.
     * @return          The list of keys present in the given cursor.
     */
    public static List<String> keys(IDataCursor cursor) {
        List<String> keys = new ArrayList<String>();
        insertKeysInto(cursor, keys);
        return keys;
    }

    /**
     * Adds the keys present in the given cursor to the given collection.
     *
     * @param cursor    The cursor containing keys.
     * @param keys      The collection to add the keys to.
     */
    private static void insertKeysInto(IDataCursor cursor, Collection<String> keys) {
        if (cursor != null && cursor.first()) {
            keys.add(cursor.getKey());
            while(cursor.next()) {
                keys.add(cursor.getKey());
            }
        }
    }

    /**
     * Returns the list of values present in the given cursor.
     *
     * @param cursor    The cursor to return the list of values from.
     * @return          The list of values present in the given cursor.
     */
    public static List<Object> values(IDataCursor cursor) {
        ArrayList<Object> values = new ArrayList<Object>();
        insertValuesInto(cursor, values);
        return values;
    }

    /**
     * Adds the values present in the given cursor to the given collection.
     *
     * @param cursor    The cursor containing values.
     * @param values    The collection to add the values to.
     */
    private static void insertValuesInto(IDataCursor cursor, Collection<Object> values) {
        if (cursor != null && cursor.first()) {
            values.add(cursor.getValue());
            while(cursor.next()) {
                values.add(cursor.getValue());
            }
        }
    }

    /**
     * Sanitizes the given cursor against the given record by removing all disallowed unspecified values.
     *
     * @param cursor    The cursor to be sanitized.
     * @param record    The record that defines the structure the cursor will be sanitized against.
     * @param recurse   Whether to recursively sanitize child IData and IData[] elements.
     */
    public static void sanitize(IDataCursor cursor, NSRecord record, boolean recurse) {
        if (cursor != null && record != null) {
            NSField[] fields = record.getFields();
            if (fields != null) {
                IData sanitizedDocument = IDataFactory.create();
                IDataCursor sanitizedCursor = sanitizedDocument.getCursor();
                try {
                    for (NSField field : fields) {
                        if (field != null) {
                            String key = field.getName();
                            if (cursor.first(key)) {
                                Object value = sanitize(cursor.getValue(), field, recurse);
                                if (value != null) {
                                    sanitizedCursor.insertAfter(key, value);
                                    cursor.delete();
                                } else {
                                    while(cursor.next(key)) {
                                        value = sanitize(cursor.getValue(), field, recurse);
                                        if (value != null) {
                                            sanitizedCursor.insertAfter(key, value);
                                            cursor.delete();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (record.isClosed()) {
                        // if the record disallows unspecified fields, then remove all remaining unsanitized fields
                        clear(cursor);
                    } else {
                        // if the record allows unspecified fields, then include any remaining keys at the end of the
                        // document, but sort them lexicographically for predictable repeatable results
                        sort(cursor);
                    }

                    prepend(sanitizedCursor, cursor);
                } finally {
                    sanitizedCursor.destroy();
                }
            }
        }
    }

    /**
     * Sanitizes the given value against the given field.
     *
     * @param value     The value to sanitize.
     * @param field     The field against which to sanitize.
     * @param recurse   Whether to recursively sanitize child IData and IData[] objects.
     * @return          The sanitized value, or null if not a valid value for this field.
     */
    private static Object sanitize(Object value, NSField field, boolean recurse) {
        Object sanitizedValue = null;

        int fieldType = field.getType();
        int fieldDimensions = field.getDimensions();

        if (field instanceof NSRecord) {
            if (fieldDimensions == NSField.DIM_ARRAY) {
                IData[] array = IDataHelper.toIDataArray(value);
                if (array != null) {
                    if (recurse) {
                        for (IData document : array) {
                            if (document != null) {
                                IDataCursor documentCursor = document.getCursor();
                                try {
                                    sanitize(documentCursor, (NSRecord)field, recurse);
                                } finally {
                                    documentCursor.destroy();
                                }
                            }
                        }
                    }
                    sanitizedValue = array;
                }
            } else if (fieldDimensions == NSField.DIM_SCALAR) {
                IData document = IDataHelper.toIData(value);
                if (document != null) {
                    if (recurse) {
                        IDataCursor documentCursor = document.getCursor();
                        try {
                            sanitize(documentCursor, (NSRecord)field, recurse);
                        } finally {
                            documentCursor.destroy();
                        }
                    }
                    sanitizedValue = document;
                }
            }
        } else if (fieldType == NSField.FIELD_STRING) {
            if ((fieldDimensions == NSField.DIM_TABLE && value instanceof String[][]) ||
                (fieldDimensions == NSField.DIM_ARRAY && value instanceof String[]) ||
                (fieldDimensions == NSField.DIM_SCALAR && value instanceof String)) {
                sanitizedValue = value;
            }
        } else {
            if ((fieldDimensions == NSField.DIM_TABLE && value instanceof Object[][]) ||
                (fieldDimensions == NSField.DIM_ARRAY && value instanceof Object[]) ||
                (fieldDimensions == NSField.DIM_SCALAR && value instanceof Object)) {
                sanitizedValue = value;
            }
        }

        return sanitizedValue;
    }
}
