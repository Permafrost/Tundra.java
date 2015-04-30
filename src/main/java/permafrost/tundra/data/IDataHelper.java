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
import com.wm.data.IDataPortable;
import com.wm.data.IDataUtil;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.lang.ArrayHelper;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with IData objects.
 */
public class IDataHelper {
    /**
     * Do not allow this class to be instantiated.
     */
    private IDataHelper() {}

    /**
     * Returns all the keys in the given IData document.
     *
     * @param input An IData document to retrieve the keys from.
     * @return The list of keys present in the given IData document.
     */
    public static String[] getKeys(IData input) {
        return getKeys(input, (Pattern) null);
    }

    /**
     * Returns the keys that match the given regular expression pattern
     * in the given IData document.
     *
     * @param input         An IData document to retrieve the keys from.
     * @param patternString A regular expression pattern which the returned
     *                      set of keys must match.
     * @return              The list of keys present in the given IData
     *                      document that match the given regular expression
     *                      pattern.
     */
    public static String[] getKeys(IData input, String patternString) {
        return getKeys(input, patternString == null ? null : Pattern.compile(patternString));
    }

    /**
     * Returns the keys that match the given regular expression pattern
     * in the given IData document.
     *
     * @param input         An IData document to retrieve the keys from.
     * @param pattern       A regular expression pattern which the returned
     *                      set of keys must match.
     * @return              The list of keys present in the given IData
     *                      document that match the given regular expression
     *                      pattern.
     */
    public static String[] getKeys(IData input, Pattern pattern) {
        java.util.List<String> keys = new java.util.ArrayList<String>();
        for (Map.Entry<String, Object> entry : new IDataMap(input)) {
            String key = entry.getKey();

            if (pattern == null) {
                keys.add(key);
            } else {
                Matcher matcher = pattern.matcher(key);
                if (matcher.matches()) keys.add(key);
            }
        }

        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns all the values in the given document.
     *
     * @param input An IData document from which to return all values.
     * @return The list of values present in the given IData document.
     */
    public static Object[] getValues(IData input) {
        List values = new ArrayList();

        for(Map.Entry<String, Object> entry : new IDataMap(input)) {
            values.add(entry.getValue());
        }

        return ArrayHelper.normalize(values.toArray());
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param input One or more IData documents to be merged.
     * @return A new IData document containing the keys and values from all
     *         merged input documents.
     */
    public static IData merge(IData... input) {
        IData output = IDataFactory.create();
        if (input != null) {
            for (int i = 0; i < input.length; i++) {
                IDataUtil.merge(input[i], output);
            }
        }
        return output;
    }

    /**
     * Returns the number of top-level key value pairs in the given IData document.
     *
     * @param input An IData document.
     * @return The number of key value pairs in the given IData document.
     */
    public static int size(IData input) {
        int size = 0;
        if (input != null) {
            IDataCursor cursor = input.getCursor();
            size = IDataUtil.size(cursor);
            cursor.destroy();
        }
        return size;
    }

    /**
     * Returns the given key from the given IData document, returning the associated
     * value if one exists.
     * @param document  The document to remove the key from.
     * @param key       The key to remove.
     * @return          The value that was associated with the given key.
     */
    public static Object remove(IData document, String key) {
        Object value = get(document, key);
        drop(document, key);
        return value;
    }

    /**
     *  Returns a copy of the given IData document.
     *
     *  @param input    An IData document to be duplicated.
     *  @return         A new IData document which is a copy of the given IData document.
     *  @throws IOException If a problem writing to the serialization stream is encountered.
     */
    public static IData duplicate(IData input) throws IOException {
        return duplicate(input, true);
    }

    /**
     *  Returns a copy of the given IData document.
     *
     *  @param input    An IData document to be duplicated.
     *  @param recurse  When true, nested IData documents and IData[] document lists will
     *                  also be duplicated.
     *  @return         A new IData document which is a copy of the given IData document.
     *  @throws IOException If a problem writing to the serialization stream is encountered.
     */
    public static IData duplicate(IData input, boolean recurse) throws IOException {
        IData output = null;
        if (input != null) {
            IDataCursor cursor = input.getCursor();
            try {
                if (recurse) {
                    output = IDataUtil.deepClone(input);
                } else {
                    output = IDataUtil.clone(input);
                }
            } finally {
                cursor.destroy();
            }
        }
        return output;
    }

    /**
     * Removes the value with the given key from the given IData document.
     *
     * @param input    An IData document.
     * @param key      A simple or fully-qualified key identifying the value to
     *                 be removed from the given IData document.
     * @return         The input IData document.
     */
    public static IData drop(IData input, String key) {
        if (input != null && key != null) {
            IDataCursor cursor = input.getCursor();
            IDataUtil.remove(cursor, key);
            cursor.destroy();

            if (Key.isFullyQualified(key)) drop(input, Key.parse(key));
        }
        return input;
    }

    /**
     * Removes the value with the given key from the given IData document.
     *
     * @param input    An IData document.
     * @param keys     A fully-qualified key identifying the value to
     *                 be removed from the given IData document.
     * @return         The input IData document.
     */
    private static IData drop(IData input, java.util.Queue<Key> keys) {
        if (input != null && keys != null && keys.size() > 0) {
            IDataCursor cursor = input.getCursor();
            Key key = keys.remove();

            if (keys.size() > 0) {
                if (key.hasArrayIndex()) {
                    drop(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, key.getKey())), key.getIndex()), keys);
                } else if (key.hasKeyIndex()) {
                    drop(toIData(get(input, key.getKey(), key.getIndex())), keys);
                } else {
                    drop(toIData(IDataUtil.get(cursor, key.getKey())), keys);
                }
            } else {
                if (key.hasArrayIndex()) {
                    IDataUtil.put(cursor, key.getKey(), ArrayHelper.drop(IDataUtil.getObjectArray(cursor, key.getKey()), key.getIndex()));
                } else if (key.hasKeyIndex()) {
                    drop(input, key.getKey(), key.getIndex());
                } else {
                    IDataUtil.remove(cursor, key.getKey());
                }
            }
            cursor.destroy();
        }
        return input;
    }

    /**
     * Removes the element with the given nth key from the given IData document.
     * @param document The IData document to remove the key value pair from.
     * @param key      The key to be removed.
     * @param n        Determines which occurrence of the key to remove.
     */
    private static void drop(IData document, String key, int n) {
        if (document == null || key == null || n < 0) return;

        Object value = null;
        int i = 0;

        IDataCursor cursor = document.getCursor();
        while(cursor.next(key) && i++ < n);
        if (i > n) cursor.delete();
        cursor.destroy();
    }

    /**
     * Renames a key from source to target within the given IData document
     *
     * @param input  An IData document.
     * @param source A simple or fully-qualified key identifying the value in
     *               the given IData document to be renamed.
     * @param target The new simple or fully-qualified key for the renamed value.
     */
    public static void rename(IData input, String source, String target) {
        if (!source.equals(target)) {
            copy(input, source, target);
            drop(input, source);
        }
    }

    /**
     * Copies a value from source key to target key within the given IData document.
     *
     * @param input  An IData document.
     * @param source A simple or fully-qualified key identifying the value in the given
     *               IData document to be copied.
     * @param target A simple or fully-qualified key the source value will be copied to.
     */
    public static void copy(IData input, String source, String target) {
        if (!source.equals(target)) {
            put(input, target, get(input, source));
        }
    }

    /**
     * Normalizes the given Object.
     *
     * @param value   An Object to be normalized.
     * @return        A new normalized version of the given Object.
     */
    private static Object normalize(Object value) {
        if (value instanceof IData) {
            value = normalize((IData) value);
        } else if (value instanceof IDataCodable) {
            value = normalize((IDataCodable) value);
        } else if (value instanceof IDataPortable) {
            value = normalize((IDataPortable)value);
        } else if (value instanceof ValuesCodable) {
            value = normalize((ValuesCodable) value);
        } else if (value instanceof Map) {
            value = normalize((Map) value);
        } else if (value instanceof IData[]) {
            value = normalize((IData[]) value);
        } else if (value instanceof Table) {
            value = normalize((Table) value);
        } else if (value instanceof IDataCodable[]) {
            value = normalize((IDataCodable[]) value);
        } else if (value instanceof IDataPortable[]) {
            value = normalize((IDataPortable[])value);
        } else if (value instanceof ValuesCodable[]) {
            value = normalize((ValuesCodable[]) value);
        } else if (value instanceof Collection) {
            value = normalize((Collection) value);
        } else if (value instanceof Map[]) {
            value = normalize((Map[]) value);
        }

        return value;
    }

    /**
     * Normalizes the given Object[].
     * @param input The Object[] to be normalized.
     * @return      Normalized version of the Object[].
     */
    private static Object[] normalize(Object[] input) {
        return (Object[])normalize((Object)ArrayHelper.normalize(input));
    }

    /**
     * Returns a new IData document, where all nested IData and IData[] objects are implemented
     * with the same class, and all fully-qualified keys are replaced with their representative
     * nested structure.
     *
     * @param input   An IData document to be normalized.
     * @return        A new normalized version of the given IData document.
     */
    public static IData normalize(IData input) {
        if (input == null) return null;

        IData output = IDataFactory.create();

        for(Map.Entry<String, Object> entry : new IDataMap(input)) {
            // normalize fully-qualified keys by using Tundra put rather than IDataUtil put
            put(output, entry.getKey(), normalize(entry.getValue()));
        }

        return output;
    }

    /**
     * Converts a java.util.Map to an IData object.
     *
     * @param input A java.util.Map to be converted to an IData object.
     * @return      An IData representation of the given java.util.Map object.
     */
    private static IData normalize(Map input) {
        return normalize(toIData(input));
    }

    /**
     * Normalizes a java.util.List to an Object[].
     *
     * @param input A java.util.List to be converted to an Object[].
     * @return      An Object[] representation of the given java.util.List object.
     */
    private static Object[] normalize(Collection input) {
        return normalize(ArrayHelper.toArray(input));
    }

    /**
     * Normalizes an IDataCodable object to an IData representation.
     *
     * @param input     An IDataCodable object to be normalized.
     * @return          An IData representation for the given IDataCodable object.
     */
    public static IData normalize(IDataCodable input) {
        return normalize(toIData(input));
    }

    /**
     * Normalizes an IDataCodable[] where all items are converted to IData documents
     * implemented with the same class, and all fully-qualified keys are replaced with
     * their representative nested structure.
     *
     * @param input   An IDataCodable[] list to be normalized.
     * @return        A new normalized IData[] version of the given IDataCodable[] list.
     */
    public static IData[] normalize(IDataCodable[] input) {
        return normalize(toIDataArray(input));
    }

    /**
     * Normalizes an IDataPortable object to an IData representation.
     *
     * @param input     An IDataPortable object to be normalized.
     * @return          An IData representation for the given IDataPortable object.
     */
    public static IData normalize(IDataPortable input) {
        return normalize(toIData(input));
    }

    /**
     * Normalizes an IDataPortable[] where all items are converted to IData documents
     * implemented with the same class, and all fully-qualified keys are replaced with
     * their representative nested structure.
     *
     * @param input   An IDataPortable[] list to be normalized.
     * @return        A new normalized IData[] version of the given IDataPortable[] list.
     */
    public static IData[] normalize(IDataPortable[] input) {
        return normalize(toIDataArray(input));
    }

    /**
     * Normalizes an ValuesCodable object to an IData representation.
     *
     * @param input     An ValuesCodable object to be normalized.
     * @return          An IData representation for the given ValuesCodable object.
     */
    public static IData normalize(ValuesCodable input) {
        return normalize(toIData(input));
    }

    /**
     * Normalizes an ValuesCodable[] where all items are converted to IData documents
     * implemented with the same class, and all fully-qualified keys are replaced with
     * their representative nested structure.
     *
     * @param input   An ValuesCodable[] list to be normalized.
     * @return        A new normalized IData[] version of the given ValuesCodable[] list.
     */
    public static IData[] normalize(ValuesCodable[] input) {
        return normalize(toIDataArray(input));
    }

    /**
     * Normalizes an IData[] where all IData objects are implemented with the same class, and
     * all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param input   An IData[] document list to be normalized.
     * @return        A new normalized version of the given IData[] document list.
     */
    public static IData[] normalize(IData[] input) {
        if (input == null) return null;

        IData[] output = new IData[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = normalize(input[i]);
        }

        return output;
    }

    /**
     * Normalizes a com.wm.util.Table object to an IData[] representation.
     *
     * @param input     A com.wm.util.Table object to be normalized.
     * @return          An IData[] representation of the given com.wm.util.Table object.
     */
    public static IData[] normalize(Table input) {
        return normalize(toIDataArray(input));
    }

    /**
     * Normalizes a Map[] object to an IData[] representation.
     *
     * @param input     A Map[] object to be normalized.
     * @return          An IData[] representation of the given Map[] object.
     */
    public static IData[] normalize(Map[] input) {
        return normalize(toIDataArray(input));
    }

    /**
     * Removes all key value pairs from the given IData document.
     *
     * @param document          An IData document to be cleared.
     */
    public static void clear(IData document) {
        clear(document, (String) null);
    }

    /**
     * Removes all key value pairs from the given IData document except those with
     * a specified key.
     *
     * @param document          An IData document to be cleared.
     * @param keysToBePreserved List of simple or fully-qualified keys identifying
     *                          items that should not be removed.
     */
    public static void clear(IData document, String ... keysToBePreserved) {
        if (document == null) return;

        IData saved = IDataFactory.create();
        if (keysToBePreserved != null) {
            for (String key : keysToBePreserved) {
                if (key != null) put(saved, key, get(document, key), false);
            }
        }

        IDataCursor cursor = document.getCursor();
        cursor.first();
        while(cursor.delete());
        cursor.destroy();

        if (keysToBePreserved != null) IDataUtil.merge(saved, document);
    }

    /**
     * Returns the value associated with the given key from the given IData document, or
     * if null the specified default value.
     *
     * @param input        An IData document.
     * @param key          A simple or fully-qualified key identifying the value in the given
     *                     IData document to be returned.
     * @param defaultValue A default value to be returned if the existing value
     *                     associated with the given key is null.
     * @return             Either the value associated with the given key in the given IData
     *                     document, or the given defaultValue if null.
     */
    public static Object get(IData input, String key, Object defaultValue) {
        Object value = get(input, key);
        if (value == null) value = defaultValue;

        return value;
    }

    /**
     * Returns the value associated with the given key from the given IData document.
     *
     * @param input        An IData document.
     * @param key          A simple or fully-qualified key identifying the value in the given
     *                     IData document to be returned.
     * @return             Either the value associated with the given key in the given IData
     *                     document.
     */
    public static Object get(IData input, String key) {
        if (input == null || key == null) return null;

        Object value = null;
        // try finding a value that matches the literal key
        IDataCursor cursor = input.getCursor();
        try {
            value = IDataUtil.get(cursor, key);
        } finally {
            cursor.destroy();
        }

        // if value wasn't found using the literal key, the key could be fully qualified
        if (value == null && Key.isFullyQualified(key)) value = get(input, Key.parse(key));

        return value;
    }

    /**
     * Returns the value associated with the given fully-qualified key from the given IData
     * document.
     *
     * @param input             An IData document.
     * @param fullyQualifiedKey A fully-qualified key identifying the value in the given
     *                          IData document to be returned.
     * @return                  Either the value associated with the given key in the given
     *                          IData document.
     */
    private static Object get(IData input, java.util.Queue<Key> fullyQualifiedKey) {
        Object value = null;

        if (input != null && fullyQualifiedKey != null && fullyQualifiedKey.size() > 0) {
            IDataCursor cursor = input.getCursor();
            Key key = fullyQualifiedKey.remove();

            if (fullyQualifiedKey.size() > 0) {
                if (key.hasArrayIndex()) {
                    value = get(ArrayHelper.get(toIDataArray(IDataUtil.get(cursor, key.getKey())), key.getIndex()), fullyQualifiedKey);
                } else if (key.hasKeyIndex()) {
                    value = get(toIData(get(input, key.getKey(), key.getIndex())), fullyQualifiedKey);
                } else {
                    value = get(IDataUtil.getIData(cursor, key.getKey()), fullyQualifiedKey);
                }
            } else {
                if (key.hasArrayIndex()) {
                    value = IDataUtil.get(cursor, key.getKey());
                    if (value != null) {
                        if (value instanceof Object[] || value instanceof Table) {
                            Object[] array = value instanceof Object[] ? (Object[]) value : ((Table) value).getValues();
                            value = ArrayHelper.get(array, key.getIndex());
                        } else {
                            value = null;
                        }
                    }
                } else if (key.hasKeyIndex()) {
                    value = get(input, key.getKey(), key.getIndex());
                } else {
                    value = IDataUtil.get(cursor, key.getKey());
                }
            }

            cursor.destroy();
        }

        return value;
    }

    /**
     * Returns the nth value associated with the given key.
     * @param document The IData document to return the value from.
     * @param key      The key whose associated value is to be returned.
     * @param n        Determines which occurrence of the key to return the value for.
     * @return         The value associated with the nth occurrence of the given key in the given IData document.
     */
    private static Object get(IData document, String key, int n) {
        if (document == null || key == null || n < 0) return null;

        Object value = null;
        int i = 0;

        IDataCursor cursor = document.getCursor();
        while(cursor.next(key) && i++ < n);
        if (i > n) value = cursor.getValue();
        cursor.destroy();

        return value;
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note
     * that this method mutates the given IData document in place.
     *
     * @param input An IData document.
     * @param key   A simple or fully-qualified key identifying the value to be set.
     * @param value The value to be set.
     * @return      The input IData document with the value set.
     */
    public static IData put(IData input, String key, Object value) {
        return put(input, key, value, true);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note
     * that this method mutates the given IData document in place.
     *
     * @param input         An IData document.
     * @param key           A simple or fully-qualified key identifying the value to be set.
     * @param value         The value to be set.
     * @param includeNull   When true the value is set even when null, otherwise the value
     *                      is only set when it is not null.
     * @return              The input IData document with the value set.
     */
    public static IData put(IData input, String key, Object value, boolean includeNull) {
        return put(input, key == null ? null : Key.parse(key), value, includeNull);
    }

    /**
     * Sets the value associated with the given key in the given IData document. Note
     * that this method mutates the given IData document in place.
     *
     * @param input             An IData document.
     * @param fullyQualifiedKey A fully-qualified key identifying the value to be set.
     * @param value             The value to be set.
     * @param includeNull       When true the value is set even when null, otherwise the value
     *                          is only set when it is not null.
     * @return                  The input IData document with the value set.
     */
    private static IData put(IData input, java.util.Queue<Key> fullyQualifiedKey, Object value, boolean includeNull) {
        if (!includeNull && value == null) return input;

        if (fullyQualifiedKey != null && fullyQualifiedKey.size() > 0) {
            if (input == null) input = IDataFactory.create();

            IDataCursor cursor = input.getCursor();
            Key key = fullyQualifiedKey.remove();

            if (fullyQualifiedKey.size() > 0) {
                if (key.hasArrayIndex()) {
                    IData[] array = IDataUtil.getIDataArray(cursor, key.getKey());
                    IData child = null;
                    try { child = ArrayHelper.get(array, key.getIndex()); } catch(ArrayIndexOutOfBoundsException ex) { }
                    value = ArrayHelper.put(array, put(child, fullyQualifiedKey, value, includeNull), key.getIndex(), IData.class);
                } else if (key.hasKeyIndex()) {
                    value = put(toIData(get(input, key.getKey(), key.getIndex())), fullyQualifiedKey, value, includeNull);
                } else {
                    value = put(IDataUtil.getIData(cursor, key.getKey()), fullyQualifiedKey, value, includeNull);
                }
            } else if (key.hasArrayIndex()) {
                Class klass = Object.class;
                if (value != null) {
                    if (value instanceof String) {
                        klass = String.class;
                    } else if (value instanceof IData) {
                        klass = IData.class;
                    }
                }
                value = ArrayHelper.put(IDataUtil.getObjectArray(cursor, key.getKey()), value, key.getIndex(), klass);
            }

            if (key.hasKeyIndex()) {
                put(input, key.getKey(), key.getIndex(), value);
            } else {
                IDataUtil.put(cursor, key.getKey(), value);
            }
            cursor.destroy();
        }

        return input;
    }

    /**
     * Sets the value associated with the given nth key in the given IData document. Note
     * that this method mutates the given IData document in place.
     * @param document The IData document to set the key's associated value in.
     * @param key      The key whose value is to be set.
     * @param n        Determines which occurrence of the key to set the value for.
     * @param value    The value to be set.
     * @return         The IData document with the given nth key set to the given value.
     */
    private static IData put(IData document, String key, int n, Object value) {
        if (document == null || key == null || n < 0) return null;

        IDataCursor cursor = document.getCursor();
        for(int i = 0; i < n; i++) {
            if (!cursor.next(key)) cursor.insertAfter(key, null);
        }
        cursor.insertAfter(key, value);
        cursor.destroy();

        return document;
    }

    /**
     * Converts the given object to a Map object, if possible.
     * @param input The object to be converted.
     * @return      A Map representation of the given object if its type
     *              is compatible (IData, IDataCodable, IDataPortable,
     *              ValuesCodable), otherwise null.
     */
    private static Map<String, Object> toMap(Object input) {
        if (input == null) return null;

        Map<String, Object> output = null;

        if (input instanceof IData) {
            output = toMap((IData)input);
        } else if (input instanceof IDataCodable) {
            output = toMap((IDataCodable)input);
        } else if (input instanceof IDataPortable) {
            output = toMap((IDataPortable)input);
        } else if (input instanceof ValuesCodable) {
            output = toMap((ValuesCodable) input);
        }

        return output;
    }

    /**
     * Converts an IData object to a java.util.Map object.
     *
     * @param input An IData object to be converted.
     * @return      A java.util.Map representation of the given IData object.
     */
    public static Map<String, Object> toMap(IData input) {
        if (input == null) return null;

        IDataCursor cursor = input.getCursor();
        int size = IDataUtil.size(cursor);
        cursor.destroy();

        Map<String, Object> output = new java.util.LinkedHashMap<String, Object>(size);

        for(Map.Entry<String, Object> entry : new IDataMap(input)) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                value = toMap(value);
            } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                value = toList(value);
            }
            output.put(key, value);
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts an IDataCodable object to a java.util.Map object.
     *
     * @param input An IDataCodable object to be converted.
     * @return      A java.util.Map representation of the given IDataCodable object.
     */
    public static Map<String, Object> toMap(IDataCodable input) {
        return toMap(toIData(input));
    }

    /**
     * Converts an IDataPortable object to a java.util.Map object.
     *
     * @param input An IDataPortable object to be converted.
     * @return      A java.util.Map representation of the given IDataPortable object.
     */
    public static Map<String, Object> toMap(IDataPortable input) {
        return toMap(toIData(input));
    }

    /**
     * Converts an ValuesCodable object to a java.util.Map object.
     *
     * @param input An ValuesCodable object to be converted.
     * @return      A java.util.Map representation of the given ValuesCodable object.
     */
    public static Map<String, Object> toMap(ValuesCodable input) {
        return toMap(toIData(input));
    }

    /**
     * Converts an object to a java.util.List object, if possible.
     *
     * @param input An object to be converted.
     * @return      A java.util.List representation of the given object, if
     *              the object was a compatible type (IData[], Table, IDataCodable[],
     *              IDataPortable[], ValuesCodable[]), otherwise null.
     */
    private static List<Map<String, Object>> toList(Object input) {
        if (input == null) return null;

        List<Map<String, Object>> output = null;

        if (input instanceof IData[]) {
            output = toList((IData[])input);
        } else if (input instanceof Table) {
            output = toList((Table)input);
        } else if (input instanceof IDataCodable[]) {
            output = toList((IDataCodable[])input);
        } else if (input instanceof IDataPortable[]) {
            output = toList((IDataPortable[])input);
        } else if (input instanceof ValuesCodable[]) {
            output = toList((ValuesCodable[])input);
        }

        return output;
    }

    /**
     * Converts an IData[] object to a java.util.List object.
     *
     * @param input An IData[] object to be converted.
     * @return      A java.util.List representation of the given IData[] object.
     */
    public static List<Map<String, Object>> toList(IData[] input) {
        if (input == null) return null;

        List<Map<String, Object>> output = new java.util.ArrayList<Map<String, Object>>(input.length);

        for (IData item : input) {
            output.add(toMap(item));
        }

        return output;
    }

    /**
     * Converts a Table object to a java.util.List object.
     *
     * @param input An Table object to be converted.
     * @return      A java.util.List representation of the given Table object.
     */
    public static List<Map<String, Object>> toList(Table input) {
        return toList(toIDataArray(input));
    }

    /**
     * Converts an IDataCodable[] object to a java.util.List object.
     *
     * @param input An IDataCodable[] object to be converted.
     * @return      A java.util.List representation of the given IDataCodable[] object.
     */
    public static List<Map<String, Object>> toList(IDataCodable[] input) {
        return toList(toIDataArray(input));
    }

    /**
     * Converts an IDataPortable[] object to a java.util.List object.
     *
     * @param input An IDataPortable[] object to be converted.
     * @return      A java.util.List representation of the given IDataPortable[] object.
     */
    public static List<Map<String, Object>> toList(IDataPortable[] input) {
        return toList(toIDataArray(input));
    }

    /**
     * Converts an ValuesCodable[] object to a java.util.List object.
     *
     * @param input An ValuesCodable[] object to be converted.
     * @return      A java.util.List representation of the given ValuesCodable[] object.
     */
    public static List<Map<String, Object>> toList(ValuesCodable[] input) {
        return toList(toIDataArray(input));
    }

    /**
     * Returns an IData representation of the given object, if possible.
     * @param input The object to convert.
     * @return      An IData representing the given object if its type
     *              is compatible (IData, IDataCodable, IDataPortable,
     *              ValuesCodable), otherwise null.
     */
    public static IData toIData(Object input) {
        if (input == null) return null;

        IData output = null;

        if (input instanceof IData) {
            output = (IData)input;
        } else if (input instanceof IDataCodable) {
            output = toIData((IDataCodable)input);
        } else if (input instanceof IDataPortable) {
            output = toIData((IDataPortable)input);
        } else if (input instanceof ValuesCodable) {
            output = toIData((ValuesCodable)input);
        } else if (input instanceof Map) {
            output = toIData((Map) input);
        }

        return output;
    }

    /**
     * Returns an IData representation of the given IDataCodable object.
     * @param input The IDataCodable object to be converted to an IData object.
     * @return      An IData representation of the give IDataCodable object.
     */
    public static IData toIData(IDataCodable input) {
        if (input == null) return null;
        return input.getIData();
    }

    /**
     * Returns an IData representation of the given IDataPortable object.
     * @param input The IDataPortable object to be converted to an IData object.
     * @return      An IData representation of the give IDataPortable object.
     */
    public static IData toIData(IDataPortable input) {
        if (input == null) return null;
        return input.getAsData();
    }

    /**
     * Returns an IData representation of the given ValuesCodable object.
     * @param input The ValuesCodable object to be converted to an IData object.
     * @return      An IData representation of the give ValuesCodable object.
     */
    public static IData toIData(ValuesCodable input) {
        if (input == null) return null;
        return input.getValues();
    }

    /**
     * Returns an IData representation of the given Map.
     * @param input The Map to be converted.
     * @return      An IData representation of the given map.
     */
    public static IData toIData(Map input) {
        if (input == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();
        for (Object key : input.keySet()) {
            if (key != null) {
                put(output, key.toString(), normalize(input.get(key)));
            }
        }
        cursor.destroy();

        return output;
    }

    /**
     * Returns an IData[] representation of the given object, if possible.
     * @param input The Table object to be converted to an IData[] object.
     * @return      An IData[] representation of the give object if the
     *              object was a compatible type (IData[], Table,
     *              IDataCodable[], IDataPortable[], ValuesCodable[]),
     *              otherwise null.
     */
    public static IData[] toIDataArray(Object input) {
        if (input == null) return null;

        IData[] output = null;

        if (input instanceof IData[]) {
            output = (IData[]) input;
        } else if (input instanceof Table) {
            output = toIDataArray((Table) input);
        } else if (input instanceof IDataCodable[]) {
            output = toIDataArray((IDataCodable[]) input);
        } else if (input instanceof IDataPortable[]) {
            output = toIDataArray((IDataPortable[]) input);
        } else if (input instanceof ValuesCodable[]) {
            output = toIDataArray((ValuesCodable[]) input);
        } else if (input instanceof Map[]) {
            output = toIDataArray((Map[]) input);
        }

        return output;
    }

    /**
     * Returns an IData[] representation of the given Table object.
     * @param input The Table object to be converted to an IData[] object.
     * @return      An IData[] representation of the give Table object.
     */
    public static IData[] toIDataArray(Table input) {
        if (input == null) return null;
        return input.getValues();
    }

    /**
     * Returns an IData[] representation of the given IDataCodable[] object.
     * @param input The IDataCodable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give IDataCodable[] object.
     */
    public static IData[] toIDataArray(IDataCodable[] input) {
        if (input == null) return null;
        IData[] output = new IData[input.length];
        for(int i = 0; i < input.length; i++) {
            output[i] = toIData(input[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given IDataPortable[] object.
     * @param input The IDataPortable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give IDataPortable[] object.
     */
    public static IData[] toIDataArray(IDataPortable[] input) {
        if (input == null) return null;
        IData[] output = new IData[input.length];
        for(int i = 0; i < input.length; i++) {
            output[i] = toIData(input[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given ValuesCodable[] object.
     * @param input The ValuesCodable[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give ValuesCodable[] object.
     */
    public static IData[] toIDataArray(ValuesCodable[] input) {
        if (input == null) return null;
        IData[] output = new IData[input.length];
        for(int i = 0; i < input.length; i++) {
            output[i] = toIData(input[i]);
        }
        return output;
    }

    /**
     * Returns an IData[] representation of the given Map[] object.
     * @param input The Map[] object to be converted to an IData[] object.
     * @return      An IData[] representation of the give Map[] object.
     */
    public static IData[] toIDataArray(Map[] input) {
        if (input == null) return null;
        IData[] output = new IData[input.length];
        for(int i = 0; i < input.length; i++) {
            output[i] = toIData(input[i]);
        }
        return output;
    }

    /**
     * Returns the union set of keys present in every item in the given
     * IData[] document list.
     *
     * @param input An IData[] to retrieve the union set of keys from.
     * @return      The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] input) {
        return getKeys(input, (Pattern) null);
    }

    /**
     * Returns the union set of keys present in every item in the given
     * IData[] document list that match the given regular expression pattern.
     *
     * @param input         An IData[] to retrieve the union set of keys from.
     * @param patternString A regular expression pattern the returned keys
     *                      must match.
     * @return              The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] input, String patternString) {
        return getKeys(input, patternString == null ? null : Pattern.compile(patternString));
    }

    /**
     * Returns the union set of keys present in every item in the given
     * IData[] document list that match the given regular expression pattern.
     *
     * @param input   An IData[] to retrieve the union set of keys from.
     * @param pattern A regular expression pattern the returned keys
     *                must match.
     * @return        The union set of keys from the given IData[].
     */
    public static String[] getKeys(IData[] input, Pattern pattern) {
        java.util.Set<String> keys = new java.util.LinkedHashSet<String>();

        if (input != null) {
            for (IData document : input) {
                if (document != null) {
                    for (Map.Entry<String, Object> entry : new IDataMap(document)) {
                        String key = entry.getKey();
                        if (pattern == null) {
                            keys.add(key);
                        } else {
                            Matcher matcher = pattern.matcher(key);
                            if (matcher.matches()) keys.add(key);
                        }
                    }
                }
            }
        }

        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Sorts the given IData document by its keys in natural ascending order.
     *
     * @param input An IData document to be sorted by its keys.
     * @return A new IData document which is duplicate of the given input
     *         IData document but with its keys sorted in natural ascending
     *         order.
     */
    public static IData sort(IData input) {
        return sort(input, true);
    }

    /**
     * Sorts the given IData document by its keys in natural ascending order.
     *
     * @param input     An IData document to be sorted by its keys.
     * @param recurse   A boolean which when true will also recursively sort
     *                  nested IData document and IData[] document lists.
     * @return          A new IData document which is duplicate of the given input
     *                  IData document but with its keys sorted in natural ascending
     *                  order.
     */
    public static IData sort(IData input, boolean recurse) {
        if (input == null) return null;

        String[] keys = getKeys(input);
        java.util.Arrays.sort(keys);

        IData output = IDataFactory.create();
        IDataCursor ic = input.getCursor();
        IDataCursor oc = output.getCursor();

        for (int i = 0; i < keys.length; i++) {
            boolean result;

            if (i > 0 && keys[i].equals(keys[i-1])) {
                result = ic.next(keys[i]);
            } else {
                result = ic.first(keys[i]);
            }

            if (result) {
                Object value = ic.getValue();
                if (recurse) {
                    if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                        value = sort(toIData(value), recurse);
                    } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                        IData[] array = toIDataArray(value);
                        for (int j = 0; j < array.length; j++) {
                            array[j] = sort(array[j], recurse);
                        }
                        value = array;
                    }
                }
                oc.insertAfter(keys[i], value);
            }
        }

        ic.destroy();
        oc.destroy();

        return output;
    }

    /**
     * Returns a new IData[] array with all elements sorted in ascending order
     * by the values associated with the given key.
     *
     * @param array An IData[] array to be sorted.
     * @param key   The key to use to sort the array.
     * @return      A new IData[] array sorted by the given key.
     */
    public static IData[] sort(IData[] array, String key) {
        return sort(array, key, true);
    }

    /**
     * Returns a new IData[] array with all elements sorted in either ascending
     * or descending order by the values associated with the given key.
     *
     * @param array     An IData[] array to be sorted.
     * @param key       The key to use to sort the array.
     * @param ascending When true, the array will be sorted in ascending order,
     *                  otherwise it will be sorted in descending order.
     * @return          A new IData[] array sorted by the given key.
     */
    public static IData[] sort(IData[] array, String key, boolean ascending) {
        String[] keys = null;
        if (key != null) {
            keys = new String[1];
            keys[0] = key;
        }

        return sort(array, keys, ascending);
    }

    /**
     * Returns a new IData[] array with all elements sorted in ascending
     * order by the values associated with the given keys.
     *
     * @param array     An IData[] array to be sorted.
     * @param keys      The list of keys in order of precedence to use to sort
     *                  the array.
     * @return          A new IData[] array sorted by the given keys.
     */
    public static IData[] sort(IData[] array, String[] keys) {
        return sort(array, keys, true);
    }

    /**
     * Returns a new IData[] array with all elements sorted in either
     * ascending or descending order by the values associated with the
     * given keys.
     *
     * @param array     An IData[] array to be sorted.
     * @param keys      The list of keys in order of precedence to use to sort
     *                  the array.
     * @param ascending When true, the array will be sorted in ascending order,
     *                  otherwise it will be sorted in descending order.
     * @return          A new IData[] array sorted by the given keys.
     */
    public static IData[] sort(IData[] array, String[] keys, boolean ascending) {
        if (array == null || array.length < 2 || keys == null || keys.length == 0) return array;

        IDataComparisonCriterion[] criteria = new IDataComparisonCriterion[keys.length];
        for (int i = 0; i < keys.length; i++) {
            criteria[i] = new IDataComparisonCriterion(keys[i], !ascending);
        }

        return sort(array, criteria);
    }

    /**
     * Returns a new IData[] array with all elements sorted according
     * to the specified criteria.
     * @param array     An IData[] array to be sorted.
     * @param criteria  One or more sort criteria.
     * @return          A new IData[] array sorted by the given criteria.
     */
    public static IData[] sort(IData[] array, IDataComparisonCriterion... criteria) {
        if (array == null) return null;

        if (criteria != null && criteria.length > 0) {
            array = ArrayHelper.sort(array, new CriteriaBasedIDataComparator(criteria));
        } else {
            array = java.util.Arrays.copyOf(array, array.length);
        }

        return array;
    }

    /**
     * Returns a new IData[] array with all elements sorted according
     * to the specified criteria.
     * @param array         An IData[] array to be sorted.
     * @param comparator    An IDataComparator object used to determine element ordering.
     * @return              A new IData[] array sorted by the given criteria.
     */
    public static IData[] sort(IData[] array, IDataComparator comparator) {
        if (array == null) return null;
        return ArrayHelper.sort(array, comparator);
    }

    /**
     * Returns the values associated with the given key from each item in the
     * given IData[] document list.
     * @param input         An IData[] array to return values from.
     * @param key           A fully-qualified key identifying the values to return.
     * @param defaultValue  The default value returned if the key does not exist.
     * @return              The values associated with the given key from each IData
     *                      item in the given array.
     */
    public static Object[] getValues(IData[] input, String key, Object defaultValue) {
        if (input == null || key == null) return null;

        List list = new ArrayList(input.length);

        for (int i = 0; i < input.length; i++) {
            list.add(get(input[i], key, defaultValue));
        }

        return ArrayHelper.normalize(list.toArray());
    }

    /**
     * Convenience class for fully qualified IData keys.
     */
    private static class Key {
        public static final String SEPARATOR = "/";
        public static final Pattern INDEX_PATTERN = Pattern.compile("(\\[(-?\\d+?)\\]|\\((\\d+?)\\))$");

        protected boolean hasArrayIndex = false, hasKeyIndex = false;
        protected int index = 0;
        protected String key = null;

        /**
         * Constructs a new key object given a key string.
         *
         * @param key An IData key as a string.
         */
        public Key(String key) {
            if (key == null) throw new NullPointerException("key must not be null");

            StringBuffer buffer = new StringBuffer();

            Matcher matcher = INDEX_PATTERN.matcher(key);
            while(matcher.find()) {
                String arrayIndexString = matcher.group(2);
                String keyIndexString = matcher.group(3);

                if (arrayIndexString != null) {
                    hasArrayIndex = true;
                    index = Integer.parseInt(arrayIndexString);
                } else {
                    hasKeyIndex = true;
                    index = Integer.parseInt(keyIndexString);
                }
                matcher.appendReplacement(buffer, "");
            }
            matcher.appendTail(buffer);

            this.key = buffer.toString();
        }

        /**
         * Returns true if this key includes an array index.
         * @return true if this key includes an array index.
         */
        public boolean hasArrayIndex() {
            return hasArrayIndex;
        }

        /**
         * Returns true if this key includes an key index.
         * @return true if this key includes an key index.
         */
        public boolean hasKeyIndex() {
            return hasKeyIndex;
        }

        /**
         * Returns this key's index value.
         * @return This key's index value.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the key-only component of this Key (with no array or key indexing).
         * @return The key-only component of this Key (with no array or key indexing).
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns a string representation of this key.
         * @return A string representation of this key.
         */
        public String toString() {
            String output;
            if (hasKeyIndex()) {
                output = key + "(" + index + ")";
            } else if (hasArrayIndex()) {
                output = key + "[" + index + "]";
            } else {
                output = key;
            }
            return output;
        }

        /**
         * Parses a fully-qualified IData key string into its constituent parts.
         * @param key A fully-qualified IData key string.
         * @return    The parsed key as a java.util.Queue of individual key parts.
         */
        public static java.util.Queue<Key> parse(String key) {
            String[] parts = key.split(SEPARATOR);
            java.util.Queue<Key> queue = new java.util.ArrayDeque<Key>(parts.length);
            for (String part : parts) {
                queue.add(new Key(part));
            }
            return queue;
        }

        /**
         * Returns true if the given IData key is considered fully-qualified (because
         * it contains either an array index, key index, or path separated components).
         *
         * @param key An IData key string.
         * @return    True if the given key is considered fully-qualified.
         */
        public static boolean isFullyQualified(String key) {
            return key != null && (key.contains(SEPARATOR) || INDEX_PATTERN.matcher(key).find());
        }
    }

}
