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
import permafrost.tundra.collection.CollectionHelper;
import permafrost.tundra.collection.ListHelper;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * A collection of convenience methods for working with arrays.
 */
public final class ArrayHelper {
    /**
     * The default separator string used between items of an array when converting it to a string.
     */
    public final static String DEFAULT_ITEM_SEPARATOR = ", ";

    /**
     * Disallow instantiation of this class.
     */
    private ArrayHelper() {}

    /**
     * Returns a new array, with the given element inserted at the end.
     *
     * @param array The array to append the item to.
     * @param item  The item to be appended.
     * @param klass The class of the item being appended.
     * @param <T>   The class of the item being appended.
     * @return      A copy of the given array with the given item appended to the end.
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
     * @return      A copy of the given array with all null items removed.
     */
    public static <T> T[] compact(T[] array) {
        if (array == null) return null;

        List<T> list = new ArrayList<T>(array.length);

        for (T item : array) {
            if (item != null) list.add(item);
        }

        return CollectionHelper.arrayify(list, array);
    }

    /**
     * Compares two Object[] objects.
     *
     * @param array1    The first Object[] to be compared.
     * @param array2    The second Object[] to be compared.
     * @return          A value less than zero if the first Object[] comes before the second Object[], a value of zero
     *                  if they are equal, or a value of greater than zero if the first Object[] comes after the second
     *                  Object[] according to the comparison of all the keys and values in each document.
     */
    public static int compare(Object[] array1, Object[] array2) {
        int result = 0;

        if (array1 == null || array2 == null) {
            if (array1 != null) {
                result = 1;
            } else if (array2 != null) {
                result = -1;
            }
        } else {
            if (array1.length < array2.length) {
                result = -1;
            } else if (array1.length > array2.length) {
                result = 1;
            } else {
                for (int i = 0; i < array1.length; i++) {
                    result = ObjectHelper.compare(array1[i], array2[i]);
                    if (result != 0) break;
                }
            }
        }
        return result;
    }

    /**
     * Returns a new array which contains all the items from the given arrays in the sequence provided.
     *
     * @param operands          An IData document containing the arrays to be concatenated.
     * @param componentClass    The component class of the arrays.
     * @param <T>               The component class of the arrays.
     * @return                  A new array which contains all the elements from the given arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concatenate(IData operands, Class<T> componentClass) {
        if (operands == null) return null;

        List<T[]> list = new ArrayList<T[]>(IDataHelper.size(operands));

        Class arrayClass = ClassHelper.getArrayClass(componentClass, 1);

        for (Map.Entry<String, Object> entry : IDataMap.of(operands)) {
            Object value = entry.getValue();
            if (arrayClass.isInstance(value)) {
                list.add((T[])value);
            }
        }

        return concatenate(list);
    }

    /**
     * Returns a new array which contains all the items from the given arrays in the sequence provided.
     *
     * @param arrays    One or more arrays to be concatenated together.
     * @param <T>       The class of item stored in the array.
     * @return          A new array which contains all the elements from the given arrays.
     */
    public static <T> T[] concatenate(T[]... arrays) {
        return concatenate(ListHelper.of(arrays));
    }

    /**
     * Returns a new array which contains all the items from the given arrays in the sequence provided.
     *
     * @param arrays    One or more arrays to be concatenated together.
     * @param <T>       The class of item stored in the array.
     * @return          A new array which contains all the elements from the given arrays.
     */
    public static <T> T[] concatenate(List<T[]> arrays) {
        T[] concatenation = null, copyable = null;

        if (arrays != null && arrays.size() > 0) {
            int length = 0;
            for (T[] array : arrays) {
                if (array != null) {
                    length += array.length;
                    copyable = array;
                }
            }

            if (copyable != null) {
                List<T> list = new ArrayList<T>(length);

                for (T[] array : arrays) {
                    if (array != null) Collections.addAll(list, array);
                }

                concatenation = CollectionHelper.arrayify(list, copyable);
            }
        }

        return concatenation;
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

        return CollectionHelper.arrayify(list, firstArray);
    }

    /**
     * Removes the element at the given index from the given list.
     *
     * @param array An array to remove an element from.
     * @param index The zero-based index of the element to be removed.
     * @param <T>   The class of the items stored in the array.
     * @return      A new array whose length is one item less than the given array, and which includes all elements
     *              from the given array except for the element at the given index.
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
     * @param operands          An IData document containing the arrays to be compared for equality.
     * @param componentClass    The component class of the arrays.
     * @param <T>               The component class of the arrays.
     * @return                  True if the given arrays are all considered equivalent, otherwise false.
     */
    @SuppressWarnings("unchecked")
    public static <T> boolean equal(IData operands, Class<T> componentClass) {
        if (operands == null) return false;

        List<T[]> list = new ArrayList<T[]>(IDataHelper.size(operands));

        Class arrayClass = ClassHelper.getArrayClass(componentClass, 1);
        for (Map.Entry<String, Object> entry : IDataMap.of(operands)) {
            Object value = entry.getValue();
            if (arrayClass.isInstance(value)) {
                list.add((T[])value);
            }
        }

        return equal(list);
    }

    /**
     * Returns true if the given arrays are equal.
     *
     * @param arrays    One or more arrays to be compared for equality.
     * @param <T>       The class of item stored in the array.
     * @return          True if the given arrays are all considered equivalent, otherwise false.
     */
    public static <T> boolean equal(T[]... arrays) {
        return equal(ListHelper.of(arrays));
    }

    /**
     * Returns true if the given arrays are equal.
     *
     * @param arrays    One or more arrays to be compared for equality.
     * @param <T>       The class of item stored in the array.
     * @return          True if the given arrays are all considered equivalent, otherwise false.
     */
    public static <T> boolean equal(List<T[]> arrays) {
        if (arrays == null) return false;
        if (arrays.size() < 2) return false;

        boolean result = true;

        for (int i = 0; i < arrays.size() - 1; i++) {
            for (int j = i + 1; j < arrays.size(); j++) {
                T[] firstArray = arrays.get(i);
                T[] secondArray = arrays.get(j);
                if (firstArray != null && secondArray != null) {
                    result = (firstArray.length == secondArray.length);

                    if (result) {
                        for (int k = 0; k < firstArray.length; k++) {
                            result = ObjectHelper.equal(firstArray[k], secondArray[k]);
                            if (!result) break;
                        }
                    }
                } else {
                    result = firstArray == null && secondArray == null;
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
     * @return      The item stored in the array at the given index.
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
     * @return          A new array with the items from the given array but with the new desired length.
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
     * @return          A new array with the items from the given array but with the new desired length.
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
     * @return          A new array with the items from the given array but with the new desired length.
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
     * @param array     The array to be filled.
     * @param item      The item to fill the array with.
     * @param index     The zero-based index from which the fill should start; supports ruby-style reverse indexing
     *                  where, for example, -1 is the last item and -2 is the second last item in the array.
     * @param length    The number of items from the given index to be filled.
     * @param <T>       The class of item stored in the array.
     * @return          The given array filled with the given item for the given range.
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
     * @param array         The array to be flattened.
     * @return              A new array which is a one-dimensional recursive flattening of the given array.
     */
    public static Object[] flatten(Object[] array) {
        return flatten(array, true);
    }

    /**
     * Returns a new array which is a one-dimensional recursive flattening of the given array.
     *
     * @param array         The array to be flattened.
     * @param includeNulls  If true, null items in the given array will be included in the returned array.
     * @return              A new array which is a one-dimensional recursive flattening of the given array.
     */
    public static Object[] flatten(Object[] array, boolean includeNulls) {
        return normalize(flatten(array, new ArrayList<Object>(array.length), includeNulls));
    }

    /**
     * Performs a one-dimensional recursive flattening of the given array into the given list.
     *
     * @param array         The array to be flattened.
     * @param list          The list to add the flattened items to.
     * @return              The given list with the added items from the given array. The list is mutated in place,
     *                      and is only returned to allow method chaining.
     */
    public static List<Object> flatten(Object[] array, List<Object> list) {
        return flatten(array, list, true);
    }

    /**
     * Performs a one-dimensional recursive flattening of the given array into the given list.
     *
     * @param array         The array to be flattened.
     * @param list          The list to add the flattened items to.
     * @param includeNulls  If true, null items in the given array will be added to the given list.
     * @return              The given list with the added items from the given array. The list is mutated in place,
     *                      and is only returned to allow method chaining.
     */
    public static List<Object> flatten(Object[] array, List<Object> list, boolean includeNulls) {
        if (array != null && list != null) {
            for (Object item : array) {
                if (item instanceof Object[]) {
                    int length = ((Object[])item).length;
                    if (length > 0) {
                        if (list instanceof ArrayList) {
                            ((ArrayList)list).ensureCapacity(list.size() + length);
                        }
                        flatten((Object[])item, list, includeNulls);
                    }
                } else if (includeNulls || item != null){
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
     * @return      A new array with the items from the given array but with a new length equal to the old length + count.
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
     * @return      A new array with the items from the given array but with a new length equal to the old length - count.
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
     * @return      True if the given item was found in the given array, otherwise false.
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

        int capacity, fillIndex;
        if (index < 0) index += list.size() + 1;
        if (index < 0) {
            capacity = Math.abs(index) + list.size();
            index = fillIndex = 0;
        } else {
            capacity = index;
            fillIndex = list.size();
        }

        list.ensureCapacity(capacity);
        if (capacity > list.size()) {
            // fill the list with nulls if it needs to be extended
            for (int i = list.size(); i < capacity; i++) {
                list.add(fillIndex, null);
            }
        }
        list.add(index, item);

        return list.toArray(instantiate(klass, 0));
    }

    /**
     * Returns a new array that contains only the items present in all the given arrays.
     *
     * @param operands          An IData document containing the arrays to be intersected.
     * @param componentClass    The component class of the arrays.
     * @param <T>               The component class of the arrays.
     * @return                  A new array containing only the items present in all given arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] intersect(IData operands, Class<T> componentClass) {
        if (operands == null) return null;

        List<T[]> list = new ArrayList<T[]>(IDataHelper.size(operands));

        Class arrayClass = ClassHelper.getArrayClass(componentClass, 1);
        for (Map.Entry<String, Object> entry : IDataMap.of(operands)) {
            Object value = entry.getValue();
            if (arrayClass.isInstance(value)) {
                list.add((T[])value);
            }
        }

        return intersect(list);
    }

    /**
     * Returns a new array that contains only the items present in all the given arrays.
     *
     * @param arrays    One or more arrays to be intersected.
     * @param <T>       The class of the items stored in the arrays.
     * @return          A new array which is a set intersection of the given arrays.
     */
    public static <T> T[] intersect(T[]... arrays) {
        return intersect(ListHelper.of(arrays));
    }

    /**
     * Returns a new array that contains only the items present in all the given arrays.
     *
     * @param arrays    One or more arrays to be intersected.
     * @param <T>       The class of the items stored in the arrays.
     * @return          A new array which is a set intersection of the given arrays.
     */
    public static <T> T[] intersect(List<T[]> arrays) {
        if (arrays == null || arrays.size() == 0) return null;

        List<T> intersection = new ArrayList<T>(arrays.get(0).length);
        intersection.addAll(Arrays.asList(arrays.get(0)));

        for (int i = 1; i < arrays.size(); i++) {
            intersection.retainAll(Arrays.asList(arrays.get(i)));
        }

        return CollectionHelper.arrayify(intersection, arrays.get(0));
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by ", ".
     *
     * @param array         The array whose contents are to be joined.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, separated by ", ".
     */
    public static <T> String join(T[] array) {
        return join(array, (Sanitization)null);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by ", ".
     *
     * @param array         The array whose contents are to be joined.
     * @param mode          The type of compaction to be applied to the array, if any.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, separated by ", ".
     */
    public static <T> String join(T[] array, Sanitization mode) {
        return join(array, ", ", mode);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array         The array whose contents are to be joined.
     * @param itemSeparator An optional separator string to be used between items of the array.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String itemSeparator) {
        return join(array, itemSeparator, (Sanitization)null);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array         The array whose contents are to be joined.
     * @param itemSeparator An optional separator string to be used between items of the array.
     * @param mode          The type of compaction to be applied to the array, if any.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String itemSeparator, Sanitization mode) {
        return join(array, itemSeparator, null, mode);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array         The array whose contents are to be joined.
     * @param itemSeparator An optional separator string to be used between items of the array.
     * @param defaultValue  An optional value returned if the given array is null or empty.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String itemSeparator, String defaultValue) {
        return join(array, itemSeparator, defaultValue, null);
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array         The array whose contents are to be joined.
     * @param itemSeparator An optional separator string to be used between items of the array.
     * @param defaultValue  An optional value returned if the given array is null or empty.
     * @param sanitization  The type of sanitization to be applied to the array, if any.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array created by concatenating together the string
     *                      representation of each item in order, optionally separated by the given separator string.
     */
    public static <T> String join(T[] array, String itemSeparator, String defaultValue, Sanitization sanitization) {
        array = sanitize(array, sanitization);

        if (array == null || array.length == 0) return defaultValue;

        StringBuilder builder = new StringBuilder();
        join(array, itemSeparator, builder);
        return builder.toString();
    }

    /**
     * Returns a string created by concatenating each element of the given array, separated by the given separator
     * string.
     *
     * @param array         The array whose contents are to be joined.
     * @param itemSeparator An optional separator string to be used between items of the array.
     * @param builder       The string builder to use when building the joined string.
     * @param <T>           The class of items stored in the array.
     */
    public static <T> void join(T[] array, String itemSeparator, StringBuilder builder) {
        if (array == null || builder == null) return;

        for (int i = 0; i < array.length; i++) {
            if (itemSeparator != null && i > 0) builder.append(itemSeparator);
            builder.append(ObjectHelper.stringify(array[i]));
        }
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array         The array to be stringified.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array.
     */
    public static <T> String stringify(T[] array) {
        return stringify(array, null, (Sanitization)null);
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array         The array to be stringified.
     * @param mode          The type of compaction to be applied to the array, if any.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array.
     */
    public static <T> String stringify(T[] array, Sanitization mode) {
        return stringify(array, null, mode);
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array         The array to be stringified.
     * @param itemSeparator The separator string used between items of the array.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array.
     */
    public static <T> String stringify(T[] array, String itemSeparator) {
        return stringify(array, itemSeparator, (Sanitization)null);
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array         The array to be stringified.
     * @param itemSeparator The separator string used between items of the array.
     * @param sanitization  The type of sanitization to be applied to the array, if any.
     * @param <T>           The class of items stored in the array.
     * @return              A string representation of the given array.
     */
    public static <T> String stringify(T[] array, String itemSeparator, Sanitization sanitization) {
        array = sanitize(array, sanitization);

        if (array == null) return null;

        StringBuilder builder = new StringBuilder();
        stringify(array, itemSeparator, builder);
        return builder.toString();
    }

    /**
     * Returns a string representation of the given array.
     *
     * @param array         The array to be stringified.
     * @param itemSeparator The separator string used between items of the array.
     * @param builder       The string builder used to build the string.
     * @param <T>           The class of items stored in the array.
     */
    public static <T> void stringify(T[] array, String itemSeparator, StringBuilder builder) {
        if (array == null || builder == null) return;
        if (itemSeparator == null) itemSeparator = DEFAULT_ITEM_SEPARATOR;

        builder.append("[");
        join(array, itemSeparator, builder);
        builder.append("]");
    }

    /**
     * Sanitizes the given array by removing nulls, or removing blanks and nulls.
     *
     * @param array         The array to be compacted.
     * @param sanitization  The type of sanitization required.
     * @param <T>           The class of item stored in the array.
     * @return              The resulting sanitized array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sanitize(T[] array, Sanitization sanitization) {
        if (array != null && sanitization != null) {
            if (sanitization == Sanitization.REMOVE_NULLS) {
                array = compact(array);
            } else if (sanitization == Sanitization.REMOVE_NULLS_AND_BLANKS) {
                array = squeeze(array);
            } else if (sanitization == Sanitization.CONVERT_NULLS_TO_BLANKS) {
                array = blankify(array);
            }
        }

        return array;
    }

    /**
     * Replaces nulls to blank strings in the given array.
     *
     * @param array The array to process.
     * @param <T>   The component class of the array.
     * @return      A new array containing all items of the given array, but with nulls replaced with blank strings.
     * @throws IllegalArgumentException If given array is not an instanceof String[].
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] blankify(T[] array) {
        if (array == null || array.length == 0) return null;

        if (array instanceof String[]) {
            return (T[])StringHelper.blankify((String[])array);
        } else {
            throw new IllegalArgumentException("array must be an instance of class String[]; class provided: " + array.getClass().getName());
        }
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
     * @return      The given array with the item at the given index set to the given value.
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
     * @return      A copy of the given array but with all item orders reversed.
     */
    public static <T> T[] reverse(T[] array) {
        if (array == null) return null;

        List<T> list = Arrays.asList(Arrays.copyOf(array, array.length));
        Collections.reverse(list);

        return CollectionHelper.arrayify(list, array);
    }

    /**
     * Returns a new array which is a subset of elements from the given array.
     *
     * @param array     The array to be sliced.
     * @param index     The zero-based start index of the subset; supports ruby-style reverse indexing where, for
     *                  example, -1 is the last item and -2 is the second last item in the array.
     * @param length    The desired length of the slice; supports negative lengths for slicing backwards from the end
     *                  of the array.
     * @param <T>       The class of the items stored in the array.
     * @return          A new array which is a subset of the given array taken at the desired index for the desired
     *                  length.
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
     * @return      A new copy of the given array but with the items sorted in their natural order.
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
     * @return           A new copy of the given array but with the items sorted.
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
     * @return           A new copy of the given array but with the items sorted in their natural order.
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
     * @return           A new copy of the given array but with the items sorted.
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
     * @return      A new copy of the given array with all duplicate elements removed.
     */
    public static <T> T[] unique(T[] array) {
        if (array == null) return null;
        return CollectionHelper.arrayify(new TreeSet<T>(Arrays.asList(array)), array);
    }

    /**
     * Dynamically instantiates a new zero-length array of the given class.
     *
     * @param componentClass    The class of items to be stored in the array.
     * @param <T>               The class of items to be stored in the array.
     * @return                  A new zero-length array of the given class.
     */
    public static <T> T[] instantiate(Class<T> componentClass) {
        return instantiate(componentClass, 0);
    }

    /**
     * Dynamically instantiates a new array of the given class with the given length.
     *
     * @param componentClass    The class of items to be stored in the array.
     * @param length            The desired length of the returned array.
     * @param <T>               The class of items to be stored in the array.
     * @return                  A new array of the given class with the given length.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] instantiate(Class<T> componentClass, int length) {
        return (T[])Array.newInstance(componentClass, length);
    }

    /**
     * Converts a Collection to an array.
     *
     * @param input A Collection to be converted to an array.
     * @param <T>   The type of item in the array.
     * @return      An array representation of the given Collection.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] of(Collection<T> input) {
        if (input == null) return null;
        return (T[])normalize(input.toArray());
    }

    /**
     * Returns a new array whose class is the nearest ancestor class of all contained items.
     *
     * @param input The array to be normalized.
     * @param <T>   The type of item in the array.
     * @return      A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] normalize(T[] input) {
        return normalize(input, ObjectHelper.getNearestAncestor(input));
    }

    /**
     * Returns a new array using the given class as its new component type.
     *
     * @param input The array to be normalized.
     * @param klass The class to use.
     * @param <T>   The type of item in the array.
     * @return      A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] normalize(T[] input, Class<?> klass) {
        if (input == null) return null;
        return Arrays.asList(input).toArray((T[])instantiate(klass, 0));
    }

    /**
     * Returns a new array whose class is the nearest ancestor class of all contained items.
     *
     * @param input The array to be normalized.
     * @param <T>   The type of item in the collection.
     * @return      A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] normalize(Collection<T> input) {
        return normalize(input, ObjectHelper.getNearestAncestor(input));
    }

    /**
     * Returns a new array whose class is the nearest ancestor class of all contained items.
     *
     * @param input The array to be normalized.
     * @param klass The class to use.
     * @param <T>   The type of item in the collection.
     * @return      A new copy of the given array whose class is the nearest ancestor of all contained items.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] normalize(Collection<T> input, Class<?> klass) {
        if (input == null) return null;
        return input.toArray((T[])instantiate(klass, 0));
    }

    /**
     * Returns a new array with all string items trimmed, all empty string items removed, and all null items removed.
     *
     * @param array An array to be squeezed.
     * @param <T>   The type of item in the array.
     * @return      A new array that is the given array squeezed.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] squeeze(T[] array) {
        if (array == null || array.length == 0) return null;

        List<T> list = new ArrayList<T>(array.length);

        for (T item : array) {
            if (item instanceof String) {
                item = (T)StringHelper.squeeze((String)item);
            }

            if (item != null) {
                list.add(item);
            }
        }

        array = CollectionHelper.arrayify(list, array);

        return array.length == 0 ? null : array;
    }

    /**
     * Converts each string in the given array to null if it only contains whitespace characters.
     *
     * @param array   The array to be nullified.
     * @param <T>     The component type of the array.
     * @return        The nullified array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] nullify(T[] array) {
        if (array == null || array.length == 0) return null;

        List<T> list = new ArrayList<T>(array.length);

        for (T item : array) {
            if (item instanceof String) item = (T)StringHelper.nullify((String)item);
            if (item != null) list.add(item);
        }

        array = CollectionHelper.arrayify(list, array);

        return array.length == 0 ? null : array;
    }

    /**
     * Converts the given array to a string array.
     *
     * @param array The array to be converted
     * @param <T>   The type of item in the array.
     * @return      The converted string array.
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
     * @param args  A varargs list of arguments.
     * @param <T>   The type of arguments.
     * @return      An array representation of the given varargs argument.
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
