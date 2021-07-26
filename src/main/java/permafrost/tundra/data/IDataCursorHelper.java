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
import java.util.Collections;
import java.util.List;

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
                while (cursor.delete());

                append(preservedCursor, cursor);
            } finally {
                if (preservedCursor != null) {
                    preservedCursor.destroy();
                }
            }
        }
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
    @SuppressWarnings("unchecked")
    public static <V> boolean first(IDataCursor cursor, Class<V> valueClass, String key) {
        boolean first = false;
        if (cursor != null) {
            if (cursor.first(key)) {
                Object candidateValue = cursor.getValue();
                if (valueClass.isInstance(candidateValue)) {
                    first = true;
                } else {
                    while(cursor.next(key)) {
                        candidateValue = cursor.getValue();
                        if (valueClass.isInstance(candidateValue)) {
                            first = true;
                            break;
                        }
                    }
                }
            }
        }
        return first;
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
     * Renames the first element's key in the cursor with the given key whose value has the specified class.
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
     * Replaces the value of the first element in the cursor with the given key whose existing value has the specified
     * class.
     *
     * @param cursor        The cursor containing elements.
     * @param valueClass    The required class of the element's existing value.
     * @param key           The element's key.
     * @param newValue      The new value to be associated with the given key.
     * @param <V>           The required class of the element's existing value.
     */
    @SuppressWarnings("unchecked")
    public static <V> void replace(IDataCursor cursor, Class<V> valueClass, String key, Object newValue) {
        if (first(cursor, valueClass, key)) {
            cursor.setValue(newValue);
        }
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
        if (cursor != null) {
            if (cursor.first()) {
                List<String> keys = keys(cursor);
                Collections.sort(keys);

                IData sortedDocument = IDataFactory.create();
                IDataCursor sortedCursor = sortedDocument.getCursor();
                try {
                    for(String key : keys) {
                        if (cursor.first(key)) {
                            sortedCursor.insertAfter(key, cursor.getValue());
                            cursor.delete();
                        }
                    }

                    replace(sortedCursor, cursor);
                } finally {
                    sortedCursor.destroy();
                }
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

        if (cursor != null) {
            if (cursor.first()) {
                size++;
                while (cursor.next()) {
                    size++;
                }
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
        ArrayList<String> keys = new ArrayList<String>(size(cursor));

        if (cursor != null) {
            if (cursor.first()) {
                keys.add(cursor.getKey());
                while(cursor.next()) {
                    keys.add(cursor.getKey());
                }
            }
        }

        return keys;
    }

    /**
     * Returns the list of values present in the given cursor.
     *
     * @param cursor    The cursor to return the list of values from.
     * @return          The list of values present in the given cursor.
     */
    public static List<Object> values(IDataCursor cursor) {
        ArrayList<Object> values = new ArrayList<Object>(size(cursor));

        if (cursor != null) {
            if (cursor.first()) {
                values.add(cursor.getValue());
                while(cursor.next()) {
                    values.add(cursor.getValue());
                }
            }
        }

        return values;
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
