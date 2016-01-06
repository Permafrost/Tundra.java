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

package permafrost.tundra.lang;

import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A collection of convenience methods for working with arrays.
 */
public final class ArrayHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ArrayHelper() {
    }

    /**
     * Returns a new array, with the given element inserted at the end.
     *
     * @param array The array to append the item to.
     * @param item  The item to be appended.
     * @param klass The class of the item being appended.
     * @param <T>   The class of the item being appended.
     * @return A copy of the given array with the given item appended to the end.
     */
    public static <T> T[] append(T[] array, T item, Class<T> klass) {
        return append(array, item, klass, true);
    }

    /**
     * Returns a new array, with the given element inserted at the end.
     *
     * @param array         The array to append the item to.
     * @param item          The item to be appended.
     * @param klass         The class of the item being appended.
     * @param includeNull   If true, null items will be inserted. If false, null items are not inserted.
     * @param <T>           The class of the item being appended.
     * @return              A copy of the given array with the given item appended to the end.
     */
    public static <T> T[] append(T[] array, T item, Class<T> klass, boolean includeNull) {
        return insert(array, item, -1, klass, includeNull);
    }

    /**
     * Returns the first non-null item from the given array.
     *
     * @param array The array to be coalesced.
     * @param <T>   The class of items stored in the array.
     * @return      The first non-null item stored in the array.
     */
    public static <T> T coalesce(T[] array) {
        return coalesce(array, null);
    }


    /**
     * Returns the first non-null item from the given array, or defaultValue if all items are null.
     *
     * @param array         The array to be coalesced.
     * @param defaultValue  The value returned if all items in the array are null.
     * @param <T>           The class of items stored in the array.
     * @return              The first non-null item stored in the array.
     */
    public static <T> T coalesce(T[] array, T defaultValue) {
        T result = null;

        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                result = array[i];
                if (result != null) break;
            }
        }

        if (result == null) result = defaultValue;

        return result;
    }

    /**
     * Returns a new array with all null elements removed.
     *
     * @param array The array to be compacted.
     * @param <T>   The class of the items in the array.
     * @return A copy of the given array with all null items removed.
     */
    public static <T> T[] compact(T[] array) {
        if (array == null) return null;

        List<T> list = new ArrayList<T>(array.length);

        for (T item : array) {
            if (item != null) list.add(item);
        }

        return list.toArray(Arrays.copyOf(array, list.size()));
    }

    /**
     * Returns a new table with all null elements removed.
     *
     * @param table The two dimensional array to be compacted.
     * @param <T>   The class of the items in the array.
     * @return A copy of the given array with all null items removed.
     */
    public static <T> T[][] compact(T[][] table) {
        if (table == null) return null;
        List<T[]> list = new ArrayList<T[]>(table.length);

        for (T[] row : table) {
            if (row != null) list.add(compact(row));
        }

        return list.toArray(Arrays.copyOf(table, list.size()));
    }

    /**
     * Returns a new array which contains all the items from the given arrays in the sequence provided.
     *
     * @param operands  An IData document containing the arrays to be concatenated.
     * @param <T>       The class of item stored in the array.
     * @return          A new array which contains all the elements from the given arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate(IData operands) {
        return concatenate((T[][])IDataHelper.getValues(operands));
    }

    /**
     * Returns a new array which contains all the items from the given arrays in the sequence provided.
     *
     * @param arrays    One or more arrays to be concatenated together.
     * @param <T>       The class of item stored in the array.
     * @return          A new array which contains all the elements from the given arrays.
     */
    public static <T> T[] concatenate(T[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;
        if (arrays.length == 1) return Arrays.copyOf(arrays[0], arrays[0].length);

        int length = 0;
        for (T[] array : arrays) {
            if (array != null) length += array.length;
        }

        List<T> list = new ArrayList<T>(length);

        for (T[] array : arrays) {
            if (array != null) Collections.addAll(list, array);
        }

        return list.toArray(Arrays.copyOf(arrays[0], length));
    }

    /**
     * Returns an array containing only the items in the first array that are not also in the second array.
     *
     * @param firstArray  The first array.
     * @param secondArray The second array.
     * @param <T>         The component type of the arrays.
     * @return            A new array containing on the items in the first array that are not also in the second array.
     */
    public static <T> T[] difference(T[] firstArray, T[] secondArray) {
        if (firstArray == null || secondArray == null) return firstArray;

        List<T> list = new ArrayList<T>(firstArray.length);
        list.addAll(Arrays.asList(firstArray));
        list.removeAll(Arrays.asList(secondArray));

        return list.toArray(Arrays.copyOf(firstArray, list.size()));
    }

    /**
     * Removes the element at the given index from the given list.
     *
     * @param array An array to remove an element from.
     * @param index The zero-based index of the element to be removed.
     * @param <T>   The class of the items stored in the array.
     * @return A new array whose length is one item less than the given array, and which includes all elements from the
     * given array except for the element at the given index.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] drop(T[] array, int index) {
        if (array != null) {
            // support reverse/tail indexing
            if (index < 0) index += array.length;
            if (index < 0 || array.length <= index) throw new ArrayIndexOutOfBoundsException(index);

            T[] head = slice(array, 0, index);
            T[] tail = slice(array, index + 1, array.length - index);

            array = concatenate(head, tail);
        }

        return array;
    }

    /**
     * Returns true if the given arrays are equal.
     *
     * @param operands  An IData document containing the arrays to be compared for equality.
     * @param <T>       The class of item stored in the array.
     * @return          True if the given arrays are all considered equivalent, otherwise false.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean equal(IData operands) {
        return equal((T[][])IDataHelper.getValues(operands));
    }

    /**
     * Returns true if the given arrays are equal.
     *
     * @param arrays One or more arrays to be compared for equality.
     * @param <T>    The class of item stored in the array.
     * @return True if the given arrays are all considered equivalent, otherwise false.
     */
    public static <T> boolean equal(T[]... arrays) {
        if (arrays == null) return false;
        if (arrays.length < 2) return false;

        boolean result = true;

        for (int i = 0; i < arrays.length - 1; i++) {
            for (int j = i + 1; j < arrays.length; j++) {
                if (arrays[i] != null && arrays[j] != null) {
                    result = (arrays[i].length == arrays[j].length);

                    if (result) {
                        for (int k = 0; k < arrays[i].length; k++) {
                            result = ObjectHelper.equal(arrays[i][k], arrays[j][k]);
                            if (!result) break;
                        }
                    }
                } else {
                    result = arrays[i] == null && arrays[j] == null;
                }
                if (!result) break;
            }
        }

        return result;
    }

    /**
     * Returns the element from the given array at the given index (supports ruby-style reverse indexing).
     *
     * @param array An array to retrieve an item from.
     * @param index The zero-based index of the item to be retrieved; supports ruby-style reverse indexing where, for
     *              example, -1 is the last item and -2 is the second last item in the array.
     * @param <T>   The class of the items stored in the array.
     * @return The item stored in the array at the given index.
     */
    public static <T> T get(T[] array, int index) {
        T item = null;

        if (array != null) {
            // support reverse/tail indexing
            if (index < 0) index += array.length;

            item = array[index];
        }

        return item;
    }

    /**
     * Resizes the given array to the desired length, and pads with nulls.
     *
     * @param array     The array to be resized, must not be null.
     * @param newLength The new length of returned array, which can be less than the given array's length in which case
     *                  it will be truncated, or more than the given array's length in which case it will be padded to
     *                  the new size with the given item (or null if no item is specified).
     * @param <T>       The class of the items stored in the array.
     * @return A new array with the items from the given array but with the new desired length.
     */
    public static <T> T[] resize(T[] array, int newLength) {
        return resize(array, newLength, null);
    }

    /**
     * Resizes the given array (or instantiates a new array, if null) to the desired length, and pads with the given
     * item.
     *
     * @param array     The array to be resized. If null, a new array will be instantiated.
     * @param newLength The new length of returned array, which can be less than the given array's length in which case
     *                  it will be truncated, or more than the given array's length in which case it will be padded to
     *                  the new size with the given item (or null if no item is specified).
     * @param item      The item to use when padding the array to a larger size.
     * @param klass     The class of the items stored in the array.
     * @param <T>       The class of the items stored in the array.
     * @return A new array with the items from the given array but with the new desired length.
     */
    public static <T> T[] resize(T[] array, int newLength, T item, Class<T> klass) {
        if (array == null) {
            array = instantiate(klass, newLength);
            if (item != null) fill(array, item, 0, newLength);
        } else {
            array = resize(array, newLength, item);
        }
        return array;
    }

    /**
     * Resizes the given array to the desired length, and pads with the given item.
     *
     * @param array     The array to be resized, must not be null.
     * @param newLength The new length of returned array, which can be less than the given array's length in which case
     *                  it will be truncated, or more than the given array's length in which case it will be padded to
     *                  the new size with the given item (or null if no item is specified).
     * @param item      The item to use when padding the array to a larger size.
     * @param <T>       The class of the items stored in the array.
     * @return A new array with the items from the given array but with the new desired length.
     */
    public static <T> T[] resize(T[] array, int newLength, T item) {
        if (array == null) throw new IllegalArgumentException("array must not be null");

        if (newLength < 0) newLength = array.length + newLength;
        if (newLength < 0) newLength = 0;

        int originalLength = array.length;
        if (newLength == originalLength) return array;

        array = Arrays.copyOf(array, newLength);
        if (item != null) {
            fill(array, item, originalLength, newLength - originalLength);
        }
        return array;
    }

    /**
     * Fills the given array with the given item for the given range.
     *
     * @param array  The array to be filled.
     * @param item   The item to fill the array with.
     * @param index  The zero-based index from which the fill should start; supports ruby-style reverse indexing where,
     *               for example, -1 is the last item and -2 is the second last item in the array.
     * @param length The number of items from the given index to be filled.
     * @param <T>    The class of item stored in the array.
     * @return The given array filled with the given item for the given range.
     */
    public static <T> T[] fill(T[] array, T item, int index, int length) {
        if (array == null) return null;
        if (length <= 0) return array;
        if (index > array.length - 1) return array;
        if (index < 0) index += array.length;

        for (int i = index; i < array.length; i++) {
            array[i] = item;
            if ((i - index) >= (length - 1)) break;
        }

        return array;
    }

    /**
     * Returns a new array which is a one-dimensional recursive flattening of the given array.
     *
     * @param array The array to be flattened.
     * @return      A new array which is a one-dimensional recursive flattening of the given array.
     */
    @SuppressWarnings("unchecked")
    public static Object[] flatten(Object[] array) {
        if (array == null || array.length == 0) return array;
        return normalize(flatten(array, new ArrayList(array.length)));
    }

    /**
     * Performs a one-dimensional recursive flattening of the given array into the given list.
     *
     * @param array The array to be flattened.
     * @param list  The list to add the flattened items to.
     */
    @SuppressWarnings("unchecked")
    private static ArrayList flatten(Object[] array, ArrayList list) {
        if (array != null) {
            for (Object item : array) {
                if (item instanceof Object[]) {
                    int length = ((Object[])item).length;
                    if (length > 0) {
                        list.ensureCapacity(list.size() + length);
                        flatten((Object[])item, list);
                    }
                } else {
                    list.add(item);
                }
            }
        }

        return list;
    }

    /**
     * Grows the size of the given array by the given count, and pads with the given item.
     *
     * @param array The array to be resized, must not be null.
     * @param count The number of additional items to be appended to the end of the array.
     * @param item  The item to use to pad the array.
     * @param klass The class of the items stored in the array.
     * @param <T>   The class of the items stored in the array.
     * @return A new array with the items from the given array but with a new length equal to the old length + count.
     */
    public static <T> T[] grow(T[] array, int count, T item, Class<T> klass) {
        return resize(array, array == null ? count : array.length + count, item, klass);
    }

    /**
     * Shrinks the size of the given array by the given count.
     *
     * @param array The array to be shrunk.
     * @param count The number of items to shrink the array by.
     * @param <T>   The class of the items stored in the array.
     * @return A new array with the items from the given array but with a new length equal to the old length - count.
     */
    public static <T> T[] shrink(T[] array, int count) {
        if (array == null) return null;
        return resize(array, array.length - count);
    }

    /**
     * Returns true if the given item is found in the given array.
     *
     * @param array The array to be searched for the given item.
     * @param item  The item to be searched for in the given array.
     * @param <T>   The class of the items stored in the array.
     * @return True if the given item was found in the given array, otherwise false.
     */
    public static <T> boolean include(T[] array, T item) {
        boolean found = false;

        // TODO: change this to Arrays.binarySearch with a custom comparator that supports IData etc.
        if (array != null) {
            for (T value : array) {
                found = ObjectHelper.equal(value, item);
                if (found) break;
            }
        }

        return found;
    }

    /**
     * Returns a new array with the given item inserted at the given index.
     *
     * @param array         The array which is to be copied to a new array.
     * @param item          The item to be inserted.
     * @param index         The zero-based index at which the item is to be inserted; supports ruby-style reverse
     *                      indexing where, for example, -1 is the last item and -2 is the second last item in the
     *                      array.
     * @param klass         The class of the items stored in the array.
     * @param <T>           The class of the items stored in the array.
     * @return              A new array which includes all the items from the given array, with the given item inserted
     *                      at the given index, and existing items at and after the given index shifted to the right
     *                      (by adding one to their indices).
     */
    public static <T> T[] insert(T[] array, T item, int index, Class<T> klass) {
        return insert(array, item, index, klass, true);
    }

    /**
     * Returns a new array with the given item inserted at the given index.
     *
     * @param array         The array which is to be copied to a new array.
     * @param item          The item to be inserted.
     * @param index         The zero-based index at which the item is to be inserted; supports ruby-style reverse
     *                      indexing where, for example, -1 is the last item and -2 is the second last item in the
     *                      array.
     * @param klass         The class of the items stored in the array.
     * @param includeNull   If true, null items will be inserted. If false, null items are not inserted.
     * @param <T>           The class of the items stored in the array.
     * @return              A new array which includes all the items from the given array, with the given item inserted
     *                      at the given index, and existing items at and after the given index shifted to the right
     *                      (by adding one to their indices).
     */
    public static <T> T[] insert(T[] array, T item, int index, Class<T> klass, boolean includeNull) {
        if (array == null) array = instantiate(klass);
        if (item == null && !includeNull) return array;

        ArrayList<T> list = new ArrayList<T>(Arrays.asList(array));

        int capacity;
        if (index < 0) index += list.size() + 1;
        if (index < 0) {
            capacity = (index * -1) + list.size();
            index = 0;
        } else {
            capacity = index;
        }

        list.ensureCapacity(capacity);
        if (capacity >= list.size()) {
            // fill the list with nulls if it needs to be extended
            for (int i = list.size(); i < capacity; i++) {
                list.add(i, null);
            }
        }
        list.add(index, item);

        return list.toArray(instantiate(klass, list.size()));
    }

    /**
     * Returns a new array that contains only the items present in all the given arrays.
     *
     * @param operands An IData document containing the arrays to be intersected.
     * @param <T>      The array component class.
     * @return         A new array containing only the items present in all given arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] intersect(IData operands) {
        return intersect((T[][])IDataHelper.getValues(operands));
    }

    /**
     * Returns a new array that contains only the items present in all the given arrays.
     *
     * @param arrays One or more arrays to be intersected.
     * @param <T>    The class of the items stored in the arrays.
     * @return A new array which is a set intersection of the given arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] intersect(T[]... arrays) {
        if (arrays == null || arrays.length == 0) return null;

        List<T> intersection = new ArrayList<T>(arrays[0].length);
        intersection.addAll(Arrays.asList(arrays[0]));

        for (int i = 1; i < arrays.length; i++) {
            intersection.retainAll(Arrays.asList(arrays[i]));
        }

        return intersection.toArray(Arrays.copyOf(arrays[0], intersection.size()));
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array     The array whose contents are to be joined.
     * @param separator An optional separator string to be used between items of the array.
     * @param <T>       The class of items stored in the array.
     * @return A string representation of the given array created by concatenating together the string representation of
     * each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String separator) {
        return join(array, separator, true);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array     The array whose contents are to be joined.
     * @param separator An optional separator string to be used between items of the array.
     * @param includeNulls If true, null values will be included in the output string, otherwise they are ignored.
     * @param <T>       The class of items stored in the array.
     * @return A string representation of the given array created by concatenating together the string representation of
     * each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String separator, boolean includeNulls) {
        if (array == null) return includeNulls ? null : "";

        StringBuilder builder = new StringBuilder();
        boolean separatorRequired = false;

        for (T item : array) {
            boolean includeItem = includeNulls || item != null;

            if (separator != null && separatorRequired && includeItem) builder.append(separator);

            if (includeItem) {
                builder.append(ObjectHelper.stringify(item));
                separatorRequired = true;
            }
        }

        return builder.toString();
    }

    /**
     * Returns a string created by concatenating each element of the given table, separated by the given separator
     * string.
     *
     * @param table     The table whose contents are to be joined.
     * @param separator An optional separator string to be used between items of the table.
     * @param <T>       The class of items stored in the table.
     * @return A string representation of the given table created by concatenating together the string representation of
     * each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[][] table, String separator) {
        return join(table, separator, true);
    }

    /**
     * Returns a string created by concatenating each element of the given table, separated by the given separator
     * string.
     *
     * @param table     The table whose contents are to be joined.
     * @param separator An optional separator string to be used between items of the table.
     * @param includeNulls If true, null values will be included in the output string, otherwise they are ignored.
     * @param <T>       The class of items stored in the table.
     * @return A string representation of the given table created by concatenating together the string representation of
     * each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[][] table, String separator, boolean includeNulls) {
        if (table == null) return includeNulls ? null : "";

        StringBuilder builder = new StringBuilder();
        boolean separatorRequired = false;

        for (T[] row : table) {
            boolean includeItem = includeNulls || row != null;

            if (separator != null && separatorRequired && includeItem) builder.append(separator);

            if (includeItem) {
                String value = join(row, separator, includeNulls);

                if (value != null) builder.append("[");
                builder.append(value);
                if (value != null) builder.append("]");

                separatorRequired = true;
            }
        }

        return builder.toString();
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array The array to be stringified.
     * @param <T>   The class of items stored in the array.
     * @return A string representation of the given array.
     */
    public static <T> String stringify(T[] array) {
        return array == null ? null : "[" + join(array, ", ") + "]";
    }

    /**
     * Returns a string representation of the given table.
     *
     * @param table The table to be stringified.
     * @param <T>   The class of items stored in the array.
     * @return A string representation of the given array.
     */
    public static <T> String stringify(T[][] table) {
        if (table == null) return null;

        String[] rows = new String[table.length];

        for (int i = 0; i < table.length; i++) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append(join(table[i], ", "));
            builder.append("]");
            rows[i] = builder.toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(join(rows, ", "));
        builder.append("]");

        return builder.toString();
    }

    /**
     * Returns a new array with a new element inserted at the beginning.
     *
     * @param array         The array to be prepended.
     * @param item          The item to prepend to the array.
     * @param klass         The class of the items stored in the array.
     * @param <T>           The class of the items stored in the array.
     * @return              A new copy of the given array with the given item prepended to the start of the array.
     */
    public static <T> T[] prepend(T[] array, T item, Class<T> klass) {
        return prepend(array, item, klass, true);
    }

    /**
     * Returns a new array with a new element inserted at the beginning.
     *
     * @param array         The array to be prepended.
     * @param item          The item to prepend to the array.
     * @param klass         The class of the items stored in the array.
     * @param includeNull   If true, null items will be inserted. If false, null items are not inserted.
     * @param <T>           The class of the items stored in the array.
     * @return              A new copy of the given array with the given item prepended to the start of the array.
     */
    public static <T> T[] prepend(T[] array, T item, Class<T> klass, boolean includeNull) {
        return insert(array, item, 0, klass, includeNull);
    }

    /**
     * Sets the element from the given array at the given index (supports ruby-style reverse indexing).
     *
     * @param array The array in which to set the item at the given index.
     * @param item  The item to be set at the given index in the array.
     * @param index The zero-based index of the array item whose value is to be set; supports ruby-style reverse
     *              indexing where, for example, -1 is the last item and -2 is the second last item in the array.
     * @param klass The class of the items stored in the array.
     * @param <T>   The class of the items stored in the array.
     * @return The given array with the item at the given index set to the given value.
     */
    public static <T> T[] put(T[] array, T item, int index, Class<T> klass) {
        if (array == null) array = instantiate(klass);

        // support reverse/tail indexing
        if (index < 0) index += array.length;

        int capacity;
        if (index < 0) {
            capacity = (index * -1) + array.length;
            index = 0;
        } else {
            capacity = index + 1;
        }

        if (capacity > array.length) array = Arrays.copyOf(array, capacity);

        array[index] = item;

        return array;
    }

    /**
     * Returns the length of the given array.
     *
     * @param array The array to return the length of.
     * @param <T>   The array component class.
     * @return      The length of the array, or 0 if it is null.
     */
    public static <T> int length(T[] array) {
        return (array == null? 0 : array.length);
    }

    /**
     * Returns a new array with all elements from the given array but in reverse order.
     *
     * @param array The array to be reversed.
     * @param <T>   The class of the items stored in the array.
     * @return A copy of the given array but with all item orders reversed.
     */
    public static <T> T[] reverse(T[] array) {
        if (array == null) return null;

        List<T> list = Arrays.asList(Arrays.copyOf(array, array.length));
        Collections.reverse(list);

        return list.toArray(Arrays.copyOf(array, list.size()));
    }

    /**
     * Returns a new array which is a subset of elements from the given array.
     *
     * @param array  The array to be sliced.
     * @param index  The zero-based start index of the subset; supports ruby-style reverse indexing where, for example,
     *               -1 is the last item and -2 is the second last item in the array.
     * @param length The desired length of the slice; supports negative lengths for slicing backwards from the end of
     *               the array.
     * @param <T>    The class of the items stored in the array.
     * @return A new array which is a subset of the given array taken at the desired index for the desired length.
     */
    public static <T> T[] slice(T[] array, int index, int length) {
        if (array == null || array.length == 0) return array;

        int inputLength = array.length, endIndex;

        // support reverse length
        if (length < 0) {
            // support reverse indexing
            if (index < 0) {
                endIndex = index + inputLength + 1;
            } else {
                if (index >= inputLength) index = inputLength - 1;
                endIndex = index + 1;
            }
            index = endIndex + length;
        } else {
            // support reverse indexing
            if (index < 0) index += inputLength;
            endIndex = index + length;
        }

        if (index < inputLength && endIndex > 0) {
            if (index < 0) index = 0;
            if (endIndex > inputLength) endIndex = inputLength;
        } else if (index >= inputLength) {
            index = endIndex = 0;
        }

        return Arrays.copyOfRange(array, index, endIndex);
    }

    /**
     * Returns a new array with all elements sorted.
     *
     * @param array The array to be sorted.
     * @param <T>   The class of items stored in the array.
     * @return A new copy of the given array but with the items sorted in their natural order.
     */
    public static <T> T[] sort(T[] array) {
        return sort(array, null);
    }

    /**
     * Returns a new array with all elements sorted according to the given comparator.
     *
     * @param array      The array to be sorted.
     * @param comparator The comparator used to determine element ordering.
     * @param <T>        The class of items stored in the array.
     * @return A new copy of the given array but with the items sorted.
     */
    public static <T> T[] sort(T[] array, Comparator<T> comparator) {
        return sort(array, comparator, false);
    }

    /**
     * Returns a new array with all elements sorted in natural ascending or descending order.
     *
     * @param array      The array to be sorted.
     * @param descending Whether to sort in descending or ascending order.
     * @param <T>        The class of items stored in the array.
     * @return A new copy of the given array but with the items sorted in their natural order.
     */
    public static <T> T[] sort(T[] array, boolean descending) {
        return sort(array, null, descending);
    }

    /**
     * Returns a new array with all elements sorted according to the given comparator.
     *
     * @param array      The array to be sorted.
     * @param comparator The comparator used to determine element ordering.
     * @param descending Whether to sort in descending or ascending order.
     * @param <T>        The class of items stored in the array.
     * @return A new copy of the given array but with the items sorted.
     */
    public static <T> T[] sort(T[] array, Comparator<T> comparator, boolean descending) {
        if (array == null) return null;

        T[] copy = Arrays.copyOf(array, array.length);
        Arrays.sort(copy, comparator);
        if (descending) copy = reverse(copy);

        return copy;
    }

    /**
     * Returns a new array with all duplicate elements removed.
     *
     * @param array The array to remove duplicates from.
     * @param <T>   The class of items stored in the array.
     * @return A new copy of the given array with all duplicate elements removed.
     */
    public static <T> T[] unique(T[] array) {
        if (array == null) return null;
        java.util.Set<T> set = new java.util.TreeSet<T>(Arrays.asList(array));
        return set.toArray(Arrays.copyOf(array, set.size()));
    }

    /**
     * Dynamically instantiates a new zero-length array of the given class.
     *
     * @param klass The class of items to be stored in the array.
     * @param <T>   The class of items to be stored in the array.
     * @return A new zero-length array of the given class.
     */
    public static <T> T[] instantiate(Class<T> klass) {
        return instantiate(klass, 0);
    }

    /**
     * Dynamically instantiates a new array of the given class with the given length.
     *
     * @param klass  The class of items to be stored in the array.
     * @param length The desired length of the returned array.
     * @param <T>    The class of items to be stored in the array.
     * @return A new array of the given class with the given length.
     */
    public static <T> T[] instantiate(Class<T> klass, int length) {
        return instantiate(klass, new int[] {length});
    }

    /**
     * Dynamically instantiates a new array of the given class with the given length.
     *
     * @param klass      The class of items to be stored in the array.
     * @param dimensions The desired length of the returned array.
     * @param <T>        The class of items to be stored in the array.
     * @return A new array of the given class with the given length.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] instantiate(Class<T> klass, int... dimensions) {
        return (T[])java.lang.reflect.Array.newInstance(klass, dimensions);
    }

    /**
     * Converts a Collection to an Object[].
     *
     * @param input A Collection to be converted to an Object[].
     * @return An Object[] representation of the given Collection.
     */
    public static Object[] toArray(Collection input) {
        if (input == null) return null;
        return normalize(input.toArray());
    }

    /**
     * Converts an Object[] to a Collection.
     *
     * @param input An Object[] to be converted to a Collection.
     * @return An Collection representation of the given Object[].
     */
    public static List toList(Object[] input) {
        if (input == null) return null;
        return Arrays.asList(input);
    }

    /**
     * Returns a new array whose class is the nearest ancestor class of all contained items.
     *
     * @param input The array to be normalized.
     * @return A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    public static Object[] normalize(Object[] input) {
        if (input == null) return null;
        return toList(input).toArray(instantiate(ObjectHelper.getNearestAncestor(input), input.length));
    }

    /**
     * Returns a new array whose class is the nearest ancestor class of all contained items.
     *
     * @param input The array to be normalized.
     * @return A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    public static Object[] normalize(Collection<?> input) {
        if (input == null) return null;
        return input.toArray(instantiate(ObjectHelper.getNearestAncestor(input), input.size()));
    }

    /**
     * Returns a new array with all string items trimmed, all empty string items removed, and all null items removed.
     *
     * @param array An array to be squeezed.
     * @param <T>   The type of item in the array.
     * @return A new array that is the given array squeezed.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] squeeze(T[] array) {
        if (array == null || array.length == 0) return null;

        List<T> list = new ArrayList<T>(array.length);

        for (T item : array) {
            if (item instanceof String) item = (T)StringHelper.squeeze((String)item, false);
            if (item != null) list.add(item);
        }

        array = list.toArray(Arrays.copyOf(array, list.size()));

        return array.length == 0 ? null : array;
    }

    /**
     * Returns a new table with all empty or null elements removed.
     *
     * @param table A table to be squeezed.
     * @param <T>   The type of item in the table.
     * @return A new table that is the given table squeezed.
     */
    private static <T> T[][] squeeze(T[][] table) {
        if (table == null || table.length == 0) return null;

        List<T[]> list = new ArrayList<T[]>(table.length);

        for (T[] row : table) {
            row = squeeze(row);
            if (row != null) list.add(row);
        }

        table = list.toArray(Arrays.copyOf(table, list.size()));

        return table.length == 0 ? null : table;
    }

    /**
     * Converts the given two dimensional array of objects to a string table.
     *
     * @param table The two dimensional array to be converted.
     * @param <T>   The type of item in the given array.
     * @return The converted string table.
     */
    public static <T> String[][] toStringTable(T[][] table) {
        if (table == null) return null;

        String[][] stringTable = new String[table.length][];
        for (int i = 0; i < table.length; i++) {
            stringTable[i] = toStringArray(table[i]);
        }

        return stringTable;
    }

    /**
     * Converts the given array to a string array.
     *
     * @param array The array to be converted
     * @param <T>   The type of item in the array.
     * @return The converted string array.
     */
    public static <T> String[] toStringArray(T[] array) {
        if (array == null) return null;

        String[] stringArray = new String[array.length];

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) stringArray[i] = array[i].toString();
        }

        return stringArray;
    }

    /**
     * Converts varargs to an array.
     *
     * @param args A varargs list of arguments.
     * @param <T>  The type of arguments.
     * @return An array representation of the given varargs argument.
     */
    public static <T> T[] arrayify(T... args) {
        return args;
    }

    /**
     * Returns a new array containing an index for each relative item in the given
     * array calculated as follows: index[n] = startIndex + n * step;
     *
     * @param array      An array to return indexes for.
     * @param startIndex The starting index for the first item in the given array.
     * @param step       The value the index will be incremented by for each subsequent item.
     * @param <T>        The type of item stored in the given array.
     * @return           A new array containing an index for each relative item in the given array.
     */
    public static <T> String[] index(T[] array, int startIndex, int step) {
        if (array == null) return null;

        String[] output = new String[array.length];
        long index = startIndex;

        for (int i = 0; i < array.length; i++) {
            output[i] = "" + index;
            index += step;
        }

        return output;
    }
}
