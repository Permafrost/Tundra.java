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

import permafrost.tundra.array.ArrayHelper;
import permafrost.tundra.exception.ExceptionHelper;
import permafrost.tundra.object.ObjectHelper;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import permafrost.tundra.exception.BaseException;

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
    public static String[] getKeys(com.wm.data.IData input) {
        return getKeys(input, null);
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
    public static String[] getKeys(com.wm.data.IData input, String patternString) {
        java.util.regex.Pattern pattern = null;
        if (patternString != null) pattern = java.util.regex.Pattern.compile(patternString);

        java.util.List<String> keys = new java.util.ArrayList<String>();
        if (input != null) {
            IDataCursor cursor = input.getCursor();
            while(cursor.next()) {
                String key = cursor.getKey();

                if (pattern == null) {
                    keys.add(key);
                } else {
                    java.util.regex.Matcher matcher = pattern.matcher(key);
                    if (matcher.matches()) keys.add(key);
                }
            }
            cursor.destroy();
        }
        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns all the values in the given document.
     *
     * @param input An IData document from which to return all values.
     * @return The list of values present in the given IData document.
     */
    public static java.lang.Object[] getValues(com.wm.data.IData input) {
        java.util.List values = new java.util.ArrayList();
        java.util.Set<Class<?>> classes = new java.util.LinkedHashSet<Class<?>>();

        if (input != null) {
            IDataCursor cursor = input.getCursor();
            while(cursor.next()) {
                java.lang.Object value = cursor.getValue();
                if (value != null) classes.add(value.getClass());
                values.add(value);
            }
            cursor.destroy();
        }

        Class<?> nearestAncestor = ObjectHelper.nearestAncestor(classes);
        if (nearestAncestor == null) nearestAncestor = java.lang.Object.class;

        return values.toArray((java.lang.Object[]) java.lang.reflect.Array.newInstance(nearestAncestor, 0));
    }

    /**
     * Merges multiple IData documents into a single new IData document.
     *
     * @param input One or more IData documents to be merged.
     * @return A new IData document containing the keys and values from all
     *         merged input documents.
     */
    public static com.wm.data.IData merge(com.wm.data.IData... input) {
        com.wm.data.IData output = IDataFactory.create();
        if (input != null) {
            for (int i = 0; i < input.length; i++) {
                com.wm.data.IDataUtil.merge(input[i], output);
            }
        }
        return output;
    }

    /**
     * Sorts the given IData document by its keys in natural ascending order.
     *
     * @param input An IData document to be sorted by its keys.
     * @return A new IData document which is duplicate of the given input
     *         IData document but with its keys sorted in natural ascending
     *         order.
     */
    public static com.wm.data.IData sort(com.wm.data.IData input) {
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
    public static com.wm.data.IData sort(com.wm.data.IData input, boolean recurse) {
        if (input == null) return null;

        String[] keys = getKeys(input);
        java.util.Arrays.sort(keys);

        com.wm.data.IData output = IDataFactory.create();
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
                java.lang.Object value = ic.getValue();

                if (value != null && recurse) {
                    if (value instanceof com.wm.data.IData) {
                        value = sort((com.wm.data.IData)value, recurse);
                    } else if (value instanceof com.wm.data.IData[] || value instanceof com.wm.util.Table) {
                        com.wm.data.IData[] array = value instanceof com.wm.data.IData[] ? (com.wm.data.IData[])value : ((com.wm.util.Table)value).getValues();
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
     * Returns the number of top-level key value pairs in the given IData document.
     *
     * @param input An IData document.
     * @return The number of key value pairs in the given IData document.
     */
    public static int size(com.wm.data.IData input) {
        int size = 0;
        if (input != null) {
            IDataCursor cursor = input.getCursor();
            size = com.wm.data.IDataUtil.size(cursor);
            cursor.destroy();
        }
        return size;
    }

    /**
     *  Returns a copy of the given IData document.
     *
     *  @param input    An IData document to be duplicated.
     *  @return         A new IData document which is a copy of the given IData document.
     *  @throws BaseException
     */
    public static com.wm.data.IData duplicate(com.wm.data.IData input) throws BaseException {
        return duplicate(input, true);
    }

    /**
     *  Returns a copy of the given IData document.
     *
     *  @param input    An IData document to be duplicated.
     *  @param recurse  When true, nested IData documents and IData[] document lists will
     *                  also be duplicated.
     *  @return         A new IData document which is a copy of the given IData document.
     *  @throws BaseException
     */
    public static com.wm.data.IData duplicate(com.wm.data.IData input, boolean recurse) throws BaseException {
        com.wm.data.IData output = null;
        if (input != null) {
            IDataCursor cursor = input.getCursor();
            try {
                if (recurse) {
                    output = com.wm.data.IDataUtil.deepClone(input);
                } else {
                    output = com.wm.data.IDataUtil.clone(input);
                }
            } catch (java.io.IOException ex) {
                ExceptionHelper.raise(ex);
            } finally {
                cursor.destroy();
            }
        }
        return output;
    }

    /**
     *  Removes the value with the given key from the given IData document.
     *
     *  @param input    An IData document.
     *  @param key      A simple or fully-qualified key identifying the value to
     *                  be removed from the given IData document.
     */
    public static com.wm.data.IData drop(com.wm.data.IData input, String key) {
        if (input != null && key != null) {
            IDataCursor cursor = input.getCursor();
            com.wm.data.IDataUtil.remove(cursor, key);
            cursor.destroy();

            if (Key.isFullyQualified(key)) drop(input, Key.parse(key));
        }
        return input;
    }

    /**
     *  Removes the value with the given key from the given IData document.
     *
     *  @param input    An IData document.
     *  @param keys     A fully-qualified key identifying the value to
     *                  be removed from the given IData document.
     */
    private static com.wm.data.IData drop(com.wm.data.IData input, java.util.Queue<Key> keys) {
        if (input != null && keys != null && keys.size() > 0) {
            IDataCursor cursor = input.getCursor();
            Key key = keys.remove();

            if (keys.size() > 0) {
                if (key.hasIndex()) {
                    com.wm.data.IData[] array = com.wm.data.IDataUtil.getIDataArray(cursor, key.toString());
                    drop(ArrayHelper.get(array, key.getIndex()), keys);
                } else {
                    drop(com.wm.data.IDataUtil.getIData(cursor, key.toString()), keys);
                }
            } else {
                if (key.hasIndex()) {
                    java.lang.Object[] array = com.wm.data.IDataUtil.getObjectArray(cursor, key.toString());
                    com.wm.data.IDataUtil.put(cursor, key.toString(), ArrayHelper.drop(array, key.getIndex()));
                } else {
                    com.wm.data.IDataUtil.remove(cursor, key.toString());
                }
            }
            cursor.destroy();
        }
        return input;
    }

    /**
     * Renames a key from source to target within the given IData document
     *
     * @param input  An IData document.
     * @param source A simple or fully-qualified key identifying the value in
     *               the given IData document to be renamed.
     * @param target The new simple or fully-qualified key for the renamed value.
     */
    public static void rename(com.wm.data.IData input, String source, String target) {
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
    public static void copy(com.wm.data.IData input, String source, String target) {
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
    private static java.lang.Object normalizeObject(java.lang.Object value) {
        if (value != null) {
            if (value instanceof com.wm.util.coder.IDataCodable) {
                value = normalize((com.wm.util.coder.IDataCodable) value);
            } else if (value instanceof com.wm.util.coder.ValuesCodable) {
                value = normalize((com.wm.util.coder.ValuesCodable) value);
            } else if (value instanceof com.wm.util.coder.IDataCodable[]) {
                value = normalize((com.wm.util.coder.IDataCodable[]) value);
            } else if (value instanceof com.wm.util.coder.ValuesCodable[]) {
                value = normalize((com.wm.util.coder.ValuesCodable[]) value);
            } else if (value instanceof com.wm.util.Table) {
                value = normalize((com.wm.util.Table) value);
            } else if (value instanceof com.wm.data.IData[]) {
                value = normalize((com.wm.data.IData[]) value);
            } else if (value instanceof com.wm.data.IData) {
                value = normalize((com.wm.data.IData) value);
            } else if (value instanceof java.util.Map) {
                value = normalize((java.util.Map) value);
            } else if (value instanceof java.util.Collection) {
                value = normalize((java.util.Collection) value);
            }
        }

        return value;
    }

    /**
     * Returns a new IData document, where all nested IData and IData[] objects are implemented
     * with the same class, and all fully-qualified keys are replaced with their representative
     * nested structure.
     *
     * @param input   An IData document to be normalized.
     * @return        A new normalized version of the given IData document.
     */
    public static com.wm.data.IData normalize(com.wm.data.IData input) {
        if (input == null) return null;

        com.wm.data.IData output = IDataFactory.create();
        IDataCursor inputCursor = input.getCursor();

        try {
            while(inputCursor.next()) {
                // normalize fully-qualified keys by using Tundra put rather than IDataUtil put
                put(output, inputCursor.getKey(), normalizeObject(inputCursor.getValue()));
            }
        } finally {
            inputCursor.destroy();
        }

        return output;
    }

    /**
     * Converts a java.util.Map to an IData object.
     *
     * @param input A java.util.Map to be converted to an IData object.
     * @return      An IData representation of the given java.util.Map object.
     */
    public static com.wm.data.IData normalize(java.util.Map input) {
        if (input == null) return null;

        com.wm.data.IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        for (java.lang.Object key : input.keySet()) {
            if (key != null) {
                put(output, key.toString(), normalizeObject(input.get(key)));
            }
        }

        cursor.destroy();

        return output;
    }

    /**
     * Normalizes a java.util.List to an Object[].
     *
     * @param input A java.util.List to be converted to an Object[].
     * @return      An Object[] representation of the given java.util.List object.
     */
    public static java.lang.Object[] normalize(java.util.Collection input) {
        if (input == null) return null;

        java.util.List values = new java.util.ArrayList(input.size());
        java.util.Set<Class<?>> classes = new java.util.LinkedHashSet<Class<?>>();

        for (java.lang.Object value : input) {
            value = normalizeObject(value);
            if (value != null) classes.add(value.getClass());
            values.add(value);
        }

        Class<?> nearestAncestor = ObjectHelper.nearestAncestor(classes);
        if (nearestAncestor == null) nearestAncestor = java.lang.Object.class;

        return values.toArray((java.lang.Object[]) java.lang.reflect.Array.newInstance(nearestAncestor, values.size()));
    }

    /**
     * Normalizes an IDataCodable object to an IData representation.
     *
     * @param input     An IDataCodable object to be normalized.
     * @return          An IData representation for the given IDataCodable object.
     */
    public static com.wm.data.IData normalize(com.wm.util.coder.IDataCodable input) {
        if (input == null) return null;
        return normalize(input.getIData());
    }

    /**
     * Normalizes an IDataCodable[] where all items are converted to IData documents
     * implemented with the same class, and all fully-qualified keys are replaced with
     * their representative nested structure.
     *
     * @param input   An IDataCodable[] list to be normalized.
     * @return        A new normalized IData[] version of the given IDataCodable[] list.
     */
    public static com.wm.data.IData[] normalize(com.wm.util.coder.IDataCodable[] input) {
        if (input == null) return null;

        com.wm.data.IData[] output = new com.wm.data.IData[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = normalize(input[i]);
        }

        return output;
    }

    /**
     * Normalizes an ValuesCodable object to an IData representation.
     *
     * @param input     An ValuesCodable object to be normalized.
     * @return          An IData representation for the given ValuesCodable object.
     */
    public static com.wm.data.IData normalize(com.wm.util.coder.ValuesCodable input) {
        if (input == null) return null;
        return normalize(input.getValues());
    }

    /**
     * Normalizes an ValuesCodable[] where all items are converted to IData documents
     * implemented with the same class, and all fully-qualified keys are replaced with
     * their representative nested structure.
     *
     * @param input   An ValuesCodable[] list to be normalized.
     * @return        A new normalized IData[] version of the given ValuesCodable[] list.
     */
    public static com.wm.data.IData[] normalize(com.wm.util.coder.ValuesCodable[] input) {
        if (input == null) return null;

        com.wm.data.IData[] output = new com.wm.data.IData[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = normalize(input[i]);
        }

        return output;
    }

    /**
     * Normalizes an IData[] where all IData objects are implemented with the same class, and
     * all fully-qualified keys are replaced with their representative nested structure.
     *
     * @param input   An IData[] document list to be normalized.
     * @return        A new normalized version of the given IData[] document list.
     */
    public static com.wm.data.IData[] normalize(com.wm.data.IData[] input) {
        if (input == null) return null;

        com.wm.data.IData[] output = new com.wm.data.IData[input.length];
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
    public static com.wm.data.IData[] normalize(com.wm.util.Table input) {
        if (input == null) return null;
        return normalize(input.getValues());
    }

    /**
     * Removes all key value pairs from the given IData document except those with
     * a specified key.
     *
     * @param document          An IData document to be cleared.
     * @param keysToBePreserved List of simple or fully-qualified keys identifying
     *                          items that should not be removed.
     */
    public static void clear(com.wm.data.IData document, String[] keysToBePreserved) {
        if (document == null) return;

        com.wm.data.IData saved = IDataFactory.create();
        if (keysToBePreserved != null) {
            for (String key : keysToBePreserved) {
                put(saved, key, get(document, key), false);
            }
        }

        IDataCursor cursor = document.getCursor();
        cursor.first();
        while(cursor.delete());
        cursor.destroy();

        if (keysToBePreserved != null) com.wm.data.IDataUtil.merge(saved, document);
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
    public static java.lang.Object get(com.wm.data.IData input, String key, java.lang.Object defaultValue) {
        java.lang.Object value = get(input, key);
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
    public static java.lang.Object get(com.wm.data.IData input, String key) {
        if (input == null || key == null) return null;

        java.lang.Object value = null;
        // try finding a value that matches the literal key
        IDataCursor cursor = input.getCursor();
        try {
            value = com.wm.data.IDataUtil.get(cursor, key);
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
    private static java.lang.Object get(com.wm.data.IData input, java.util.Queue<Key> fullyQualifiedKey) {
        java.lang.Object value = null;

        if (input != null && fullyQualifiedKey != null && fullyQualifiedKey.size() > 0) {
            IDataCursor cursor = input.getCursor();
            Key key = fullyQualifiedKey.remove();

            if (fullyQualifiedKey.size() > 0) {
                if (key.hasIndex()) {
                    value = com.wm.data.IDataUtil.get(cursor, key.toString());
                    if (value != null) {
                        if (value instanceof com.wm.data.IData[] || value instanceof com.wm.util.Table) {
                            com.wm.data.IData[] array = value instanceof com.wm.data.IData[] ? (com.wm.data.IData[])value : ((com.wm.util.Table)value).getValues();
                            value = get(ArrayHelper.get(array, key.getIndex()), fullyQualifiedKey);
                        } else {
                            value = null;
                        }
                    }
                } else {
                    value = get(com.wm.data.IDataUtil.getIData(cursor, key.toString()), fullyQualifiedKey);
                }
            } else {
                if (key.hasIndex()) {
                    value = com.wm.data.IDataUtil.get(cursor, key.toString());
                    if (value != null) {
                        if (value instanceof java.lang.Object[] || value instanceof com.wm.util.Table) {
                            java.lang.Object[] array = value instanceof java.lang.Object[] ? (java.lang.Object[])value : ((com.wm.util.Table)value).getValues();
                            value = ArrayHelper.get(array, key.getIndex());
                        } else {
                            value = null;
                        }
                    }
                } else {
                    value = com.wm.data.IDataUtil.get(cursor, key.toString());
                }
            }

            cursor.destroy();
        }

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
    public static com.wm.data.IData put(com.wm.data.IData input, String key, java.lang.Object value) {
        return put(input, key == null ? null : Key.parse(key), value, true);
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
    public static com.wm.data.IData put(com.wm.data.IData input, String key, java.lang.Object value, boolean includeNull) {
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
    private static com.wm.data.IData put(com.wm.data.IData input, java.util.Queue<Key> fullyQualifiedKey, java.lang.Object value, boolean includeNull) {
        if (!includeNull && value == null) return input;

        if (fullyQualifiedKey != null && fullyQualifiedKey.size() > 0) {
            if (input == null) input = IDataFactory.create();

            IDataCursor cursor = input.getCursor();
            Key key = fullyQualifiedKey.remove();

            if (fullyQualifiedKey.size() > 0) {
                if (key.hasIndex()) {
                    com.wm.data.IData[] array = com.wm.data.IDataUtil.getIDataArray(cursor, key.toString());
                    com.wm.data.IData child = null;
                    try { child = ArrayHelper.get(array, key.getIndex()); } catch(ArrayIndexOutOfBoundsException ex) { }
                    value = ArrayHelper.put(array, put(child, fullyQualifiedKey, value, includeNull), key.getIndex(), com.wm.data.IData.class);
                } else {
                    value = put(com.wm.data.IDataUtil.getIData(cursor, key.toString()), fullyQualifiedKey, value, includeNull);
                }
            } else if (key.hasIndex()) {
                Class klass = java.lang.Object.class;
                if (value != null) {
                    if (value instanceof String) {
                        klass = String.class;
                    } else if (value instanceof com.wm.data.IData) {
                        klass = com.wm.data.IData.class;
                    }
                }
                value = ArrayHelper.put(com.wm.data.IDataUtil.getObjectArray(cursor, key.toString()), value, key.getIndex(), klass);
            }
            com.wm.data.IDataUtil.put(cursor, key.toString(), value);
            cursor.destroy();
        }

        return input;
    }

    /**
     * Converts an IData object to a java.util.Map object.
     *
     * @param input An IData object to be converted.
     * @return      A java.util.Map representation of the given IData object.
     */
    public static java.util.Map<String, java.lang.Object> toMap(com.wm.data.IData input) {
        if (input == null) return null;

        IDataCursor cursor = input.getCursor();
        int size = com.wm.data.IDataUtil.size(cursor);
        cursor.destroy();
        cursor = input.getCursor();

        java.util.Map<String, java.lang.Object> output = new java.util.LinkedHashMap<String, java.lang.Object>(size);

        while(cursor.next()) {
            String key = cursor.getKey();
            java.lang.Object value = cursor.getValue();
            if (value != null) {
                if (value instanceof com.wm.data.IData) {
                    value = toMap((com.wm.data.IData)value);
                } else if (value instanceof com.wm.data.IData[] || value instanceof com.wm.util.Table) {
                    value = toList(value instanceof com.wm.data.IData[] ? (com.wm.data.IData[])value : ((com.wm.util.Table)value).getValues());
                }
            }
            output.put(key, value);
        }

        cursor.destroy();

        return output;
    }

    /**
     * Converts an IData[] object to a java.util.List object.
     *
     * @param input An IData[] object to be converted.
     * @return      A java.util.List representation of the given IData[] object.
     */
    public static java.util.List<java.util.Map<String, java.lang.Object>> toList(com.wm.data.IData[] input) {
        if (input == null) return null;

        java.util.List<java.util.Map<String, java.lang.Object>> output = new java.util.ArrayList<java.util.Map<String, java.lang.Object>>(input.length);

        for (com.wm.data.IData item : input) {
            output.add(toMap(item));
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
    public static String[] getKeys(com.wm.data.IData[] input) {
        return getKeys(input, (java.util.regex.Pattern)null);
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
    public static String[] getKeys(com.wm.data.IData[] input, String patternString) {
        return getKeys(input, patternString == null ? null : java.util.regex.Pattern.compile(patternString));
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
    public static String[] getKeys(com.wm.data.IData[] input, java.util.regex.Pattern pattern) {
        java.util.Set<String> keys = new java.util.LinkedHashSet<String>();

        if (input != null) {
            for (com.wm.data.IData document : input) {
                if (document != null) {
                    IDataCursor cursor = document.getCursor();
                    while(cursor.next()) {
                        String key = cursor.getKey();
                        if (pattern == null) {
                            keys.add(key);
                        } else {
                            java.util.regex.Matcher matcher = pattern.matcher(key);
                            if (matcher.matches()) keys.add(key);
                        }
                    }
                    cursor.destroy();
                }
            }
        }

        return keys.toArray(new String[keys.size()]);
    }

    /**
     * Returns a new IData[] array with all elements sorted in ascending order
     * by the values associated with the given key.
     *
     * @param array An IData[] array to be sorted.
     * @param key   The key to use to sort the array.
     * @return      A new IData[] array sorted by the given key.
     */
    public static com.wm.data.IData[] sort(com.wm.data.IData[] array, String key) {
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
    public static com.wm.data.IData[] sort(com.wm.data.IData[] array, String key, boolean ascending) {
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
    public static com.wm.data.IData[] sort(com.wm.data.IData[] array, String[] keys) {
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
    public static com.wm.data.IData[] sort(com.wm.data.IData[] array, String[] keys, boolean ascending) {
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
     *
     * @param array     An IData[] array to be sorted.
     * @param criteria  One or more sort criteria.
     * @return          A new IData[] array sorted by the given criteria.
     */
    public static com.wm.data.IData[] sort(com.wm.data.IData[] array, IDataComparisonCriterion ... criteria) {
        if (array == null) return null;

        array = java.util.Arrays.copyOf(array, array.length);
        if (!(array.length < 2 || criteria == null || criteria.length == 0)) {
            java.util.Arrays.sort(array, new IDataComparator(criteria));
        }

        return array;
    }

    /**
     * Returns the values associated with the given key from each item in the
     * given IData[] document list.
     *
     * @param input An IData[] array to return values from.
     * @param key   A fully-qualified key identifying the values to return.
     * @param defaultValue
     */
    public static java.lang.Object[] getValues(com.wm.data.IData[] input, String key, java.lang.Object defaultValue) {
        if (input == null || key == null) return null;

        java.util.Set<Class<?>> classes = new java.util.LinkedHashSet<Class<?>>();
        java.util.List list = new java.util.ArrayList(input.length);

        for (int i = 0; i < input.length; i++) {
            java.lang.Object value = get(input[i], key, defaultValue);
            if (value != null) classes.add(value.getClass());
            list.add(value);
        }

        Class<?> nearestAncestor = ObjectHelper.nearestAncestor(classes);
        if (nearestAncestor == null) nearestAncestor = java.lang.Object.class;

        return list.toArray((java.lang.Object[])java.lang.reflect.Array.newInstance(nearestAncestor, 0));
    }

    /**
     * Convenience class for fully qualified IData keys.
     */
    private static class Key {
        public static final String SEPARATOR = "/";
        public static final java.util.regex.Pattern INDEX_PATTERN = java.util.regex.Pattern.compile("\\[(-?\\d+?)\\]$");

        protected boolean hasIndex = false;
        protected int index = 0;
        protected String key = null;

        /**
         * Constructs a new key object given a key string.
         *
         * @param key An IData key as a string.
         */
        public Key(String key) {
            java.util.regex.Matcher matcher = INDEX_PATTERN.matcher(key);
            StringBuffer buffer = new StringBuffer();
            while(matcher.find()) {
                hasIndex = true;
                index = Integer.parseInt(matcher.group(1));
                matcher.appendReplacement(buffer, "");
            }
            matcher.appendTail(buffer);
            this.key = buffer.toString();
        }

        /**
         * @return true if this key includes an array index.
         */
        public boolean hasIndex() {
            return hasIndex;
        }

        /**
         * @return This key's array index value.
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return A string representation of this key.
         */
        public String toString() {
            return key;
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
         * it contains either an array index or path separated components).
         *
         * @param key An IData key string.
         * @return    True if the given key is considered fully-qualified.
         */
        public static boolean isFullyQualified(String key) {
            return key != null && (key.contains(SEPARATOR) || INDEX_PATTERN.matcher(key).find());
        }
    }

}
