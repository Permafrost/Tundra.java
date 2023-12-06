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

package permafrost.tundra.data.transform;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataPortable;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.TableHelper;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class representing a transformer that operates on an IData's elements.
 *
 * @param <V>   Input value class.
 * @param <T>   Output transformed value class.
 */
public abstract class Transformer<V, T> {
    /**
     * The class of values to be transformed.
     */
    protected Class<V> valueClass;
    /**
     * The class of values after they are transformed.
     */
    protected Class<T> transformedValueClass;
    /**
     * The class of values held in an array or table to be transformed, calculated from the valueClass.
     */
    protected Class<?> arrayClass, tableClass, transformedArrayClass;
    /**
     * Whether embedded IData and IData[] children should be recursively transformed.
     */
    protected boolean recurse, includeNulls, includeEmptyDocuments, includeEmptyArrays;
    /**
     * Whether transformed arrays and tables should be normalized to use the nearest ancestor class of all items as the
     * component type.
     */
    protected boolean normalizeTransformedArrays = false, normalizeTransformedTables = false;
    /**
     * The type of transformation required.
     */
    protected TransformerMode mode;

    /**
     * Constructs a new Transformer object.
     *
     * @param valueClass            The class of values to be transformed; only values that are instances of this class
     *                              are transformed by the object.
     * @param transformedValueClass The class of transformed values.
     * @param mode                  The mode of transformation required.
     * @param recurse               Whether to recursively transform child IData and IData[] objects.
     * @param includeNulls          Whether null values should be included in transformed IData documents and IData[]
     *                              document lists.
     * @param includeEmptyDocuments Whether empty IData documents should be included in the transformation.
     * @param includeEmptyArrays    Whether empty arrays should be included in the transformation.
     */
    public Transformer(Class<V> valueClass, Class<T> transformedValueClass, TransformerMode mode, boolean recurse, boolean includeNulls, boolean includeEmptyDocuments, boolean includeEmptyArrays) {
        this.valueClass = valueClass;
        this.transformedValueClass = transformedValueClass;
        this.mode = TransformerMode.normalize(mode);
        this.recurse = recurse;
        this.includeNulls = includeNulls;
        this.includeEmptyDocuments = includeEmptyDocuments;
        this.includeEmptyArrays = includeEmptyArrays;
        this.arrayClass = Array.newInstance(valueClass, 0).getClass();
        this.tableClass = Array.newInstance(valueClass, 0, 0).getClass();
        this.transformedArrayClass = Array.newInstance(transformedValueClass, 0).getClass();
    }

    /**
     *  Transforms the elements of the given IData document.
     *
     * @param document  The IData document whose elements are to be transformed.
     * @return          A new IData document containing the transformed elements from the given IData document.
     */
    public IData transform(IData document) {
        return transformIData(document);
    }

    /**
     *  Transforms the elements of the given IData[] document list.
     *
     * @param array     The IData[] document list whose elements are to be transformed.
     * @return          A new IData[] document list containing the transformed elements from the given IData[] document
     *                  list.
     */
    public IData[] transform(IData[] array) {
        return transformIDataArray(array);
    }

    /**
     *  Transforms the elements of the given IData document.
     *
     * @param document  The IData document whose elements are to be transformed.
     * @return          A new IData document containing the transformed elements from the given IData document.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    protected IData transformIData(IData document) {
        if (document == null) return null;

        boolean transformKeys = (mode == TransformerMode.KEYS || mode == TransformerMode.KEYS_AND_VALUES);
        boolean transformValues = (mode == TransformerMode.VALUES || mode == TransformerMode.KEYS_AND_VALUES);

        IData output = IDataFactory.create();
        IDataCursor inputCursor = document.getCursor();
        IDataCursor outputCursor = output.getCursor();

        while (inputCursor.next()) {
            String key = inputCursor.getKey();
            Object value = inputCursor.getValue();
            String transformedKey;

            if (transformKeys) {
                transformedKey = transformKey(key, value);
            } else {
                transformedKey = key;
            }

            if (transformedKey != null) {
                if (transformValues) {
                    if (value == null) {
                        T transformedValue = transformNull(key);

                        if (includeNulls || transformedValue != null) {
                            outputCursor.insertAfter(transformedKey, transformedValue);
                        }
                    } else if (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[]) {
                        if (recurse) {
                            IData[] transformedArray = transformIDataArray(IDataHelper.toIDataArray(value));

                            if (includeNulls || (transformedArray != null && (includeEmptyArrays || transformedArray.length > 0))) {
                                outputCursor.insertAfter(transformedKey, transformedArray);
                            }
                        } else {
                            outputCursor.insertAfter(transformedKey, value);
                        }
                    } else if (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable) {
                        if (recurse) {
                            IData transformedIData = transformIData(IDataHelper.toIData(value));

                            if (includeNulls || (transformedIData != null && (includeEmptyDocuments || IDataHelper.size(transformedIData) > 0))) {
                                outputCursor.insertAfter(transformedKey, transformedIData);
                            }
                        } else {
                            outputCursor.insertAfter(transformedKey, value);
                        }
                    } else if (tableClass.isInstance(value)) {
                        T[][] transformedTable = transformTable(key, (V[][])value);

                        if (includeNulls || (transformedTable != null && (includeEmptyArrays || transformedTable.length > 0))) {
                            outputCursor.insertAfter(transformedKey, transformedTable);
                        }
                    } else if (arrayClass.isInstance(value)) {
                        T[] transformedArray = transformArray(key, (V[])value);

                        if (includeNulls || (transformedArray != null && (includeEmptyArrays || transformedArray.length > 0))) {
                            outputCursor.insertAfter(transformedKey, transformedArray);
                        }
                    } else if (valueClass.isInstance(value)) {
                        T transformedValue = transformValue(key, (V)value);

                        if (includeNulls || transformedValue != null) {
                            outputCursor.insertAfter(transformedKey, transformedValue);
                        }
                    } else {
                        outputCursor.insertAfter(transformedKey, value);
                    }
                } else if (recurse && (value instanceof IData[] || value instanceof Table || value instanceof IDataCodable[] || value instanceof IDataPortable[] || value instanceof ValuesCodable[])) {
                    IData[] transformedArray = transformIDataArray(IDataHelper.toIDataArray(value));
                    if (includeNulls || (transformedArray != null && (includeEmptyArrays || transformedArray.length > 0))) {
                        outputCursor.insertAfter(transformedKey, transformedArray);
                    }
                } else if (recurse && (value instanceof IData || value instanceof IDataCodable || value instanceof IDataPortable || value instanceof ValuesCodable)) {
                    IData transformedIData = transformIData(IDataHelper.toIData(value));
                    if (includeNulls || (transformedIData != null && (includeEmptyDocuments || IDataHelper.size(transformedIData) > 0))) {
                        outputCursor.insertAfter(transformedKey, transformedIData);
                    }
                } else {
                    outputCursor.insertAfter(transformedKey, value);
                }
            }
        }

        inputCursor.destroy();
        outputCursor.destroy();

        return includeEmptyDocuments || IDataHelper.size(output) > 0 ? output : null;
    }

    /**
     *  Transforms the elements of the given IData[] document list.
     *
     * @param array     The IData[] document list whose elements are to be transformed.
     * @return          A new IData[] document list containing the transformed elements from the given IData[] document
     *                  list.
     */
    protected IData[] transformIDataArray(IData[] array) {
        if (array == null) return null;

        List<IData> output = new ArrayList<IData>(array.length);

        for (IData document : array) {
            IData transformedDocument = transformIData(document);

            if (includeNulls || transformedDocument != null) {
                output.add(transformedDocument);
            }
        }

        return includeEmptyArrays || output.size() > 0 ? output.toArray(new IData[0]) : null;
    }

    /**
     * Transforms a key.
     *
     * @param key   The key to be transformed.
     * @param value The value associated with the key being transformed.
     * @return      The transformed key.
     */
    protected String transformKey(String key, Object value) {
        return key;
    }

    /**
     * Transforms a value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    protected abstract T transformValue(String key, V value);

    /**
     * Transforms a null value.
     *
     * @param key   The key associated with the null value being transformed.
     * @return      The transformed value.
     */
    protected T transformNull(String key) {
        return null;
    }

    /**
     * Transforms an array of values.
     *
     * @param key   The key associated with the array value being transformed.
     * @param array The array of values to be transformed.
     * @return      A new array of transformed values.
     */
    @SuppressWarnings("unchecked")
    protected T[] transformArray(String key, V[] array) {
        if (array == null) return null;

        List<T> output = new ArrayList<T>(array.length);

        for (V item : array) {
            T transformedItem = item == null ? transformNull(key) : transformValue(key, item);

            if (includeNulls || transformedItem != null) {
                output.add(transformedItem);
            }
        }

        if (includeEmptyArrays || output.size() > 0) {
            if (normalizeTransformedArrays) {
                return ArrayHelper.normalize(output);
            } else {
                return output.toArray(ArrayHelper.instantiate(transformedValueClass, 0));
            }
        } else {
            return null;
        }
    }

    /**
     * Transforms a table of values.
     *
     * @param key   The key associated with the table value being transformed.
     * @param table The table of values to be transformed.
     * @return      A new table of transformed values.
     */
    @SuppressWarnings("unchecked")
    protected T[][] transformTable(String key, V[][] table) {
        if (table == null) return null;

        List<T[]> output = new ArrayList<T[]>(table.length);

        for (V[] array : table) {
            T[] transformedArray = transformArray(key, array);

            if (includeNulls || (transformedArray != null && (includeEmptyArrays || transformedArray.length > 0))) {
                output.add(transformedArray);
            }
        }

        if (includeEmptyArrays || output.size() > 0) {
            if (normalizeTransformedTables) {
                return TableHelper.normalize(output);
            } else {
                return output.toArray((T[][])TableHelper.instantiate(transformedValueClass, 0, 0));
            }
        } else {
            return null;
        }
    }

    /**
     * Transforms the given IData document using the given transformer.
     *
     * @param document      The IData document to be transformed.
     * @param transformer   The Transformer object to use to transform the given IData document.
     * @return              The resulting transformed IData document.
     */
    public static IData transform(IData document, Transformer transformer) {
        return transformer.transform(document);
    }

    /**
     * Transforms the given IData[] document list using the given transformer.
     *
     * @param array         The IData[] document list to be transformed.
     * @param transformer   The Transformer object to use to transform the given IData[] document list.
     * @return              The resulting transformed IData[] document list.
     */
    public static IData[] transform(IData[] array, Transformer transformer) {
        return transformer.transform(array);
    }
}
