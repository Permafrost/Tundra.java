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

package permafrost.tundra.array;

import permafrost.tundra.object.ObjectHelper;

public class ArrayHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ArrayHelper() {}

    /**
     * Returns a new array, with the given element inserted at the end.
     * @param array
     * @param item
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] append(T[] array, T item, Class<T> klass) {
        return insert(array, item, -1, klass);
    }

    /**
     * Returns a new array with all null elements removed.
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] compact(T[] array) {
        if (array == null) return array;

        java.util.List<T> list = new java.util.ArrayList<T>(array.length);

        for (T item : array) {
            if (item != null) list.add(item);
        }

        return list.toArray(java.util.Arrays.copyOf(array, list.size()));
    }

    /**
     * Returns a new array which contains all the elements from the given arrays.
     *
     * @param array
     * @param items
     * @param <T>
     * @return
     */
    public static <T> T[] concatenate(T[] array, T[] items) {
        if (array == null) return items;
        if (items == null) return array;

        java.util.List<T> list = new java.util.ArrayList<T>(array.length + items.length);

        java.util.Collections.addAll(list, array);
        java.util.Collections.addAll(list, items);

        return list.toArray(java.util.Arrays.copyOf(array, 0));
    }

    /**
     * Removes the element at the given index from the given list.
     *
     * @param array
     * @param index
     * @param <T>
     * @return
     */
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
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> boolean equal(T[] firstArray, T[] secondArray) {
        boolean result = true;

        if (firstArray != null && secondArray != null) {
            result = (firstArray.length == secondArray.length);

            if (result) {
                for (int i = 0; i < firstArray.length; i++) {
                    result = result && ObjectHelper.equal(firstArray[i], secondArray[i]);
                    if (!result) break;
                }
            }
        } else {
            result = (firstArray == null && secondArray == null);
        }

        return result;
    }

    /**
     * Returns the element from the given array at the given index (supports
     * ruby-style reverse indexing).
     *
     * @param array
     * @param index
     * @param <T>
     * @return
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
     * Resizes the given array (or instantiates a new array, if null) to the
     * desired length, and pads with the given item.
     *
     * @param array
     * @param newLength
     * @param item
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] resize(T[] array, int newLength, T item, Class<T> klass) {
        if (array == null) array = (T[])java.lang.reflect.Array.newInstance(klass, 0);
        return resize(array, newLength, item);
    }

    /**
     * Resizes the given array to the desired length, and pads with the given item.
     *
     * @param array
     * @param newLength
     * @param item
     * @param <T>
     * @return
     */
    public static <T> T[] resize(T[] array, int newLength, T item) {
        if (newLength < 0) newLength = array.length + newLength;
        if (newLength < 0) newLength = 0;

        int originalLength = array.length;
        if (newLength == originalLength) return array;

        array = java.util.Arrays.copyOf(array, newLength);
        if (item != null) {
            for (int i = originalLength; i < newLength; i++) array[i] = item;
        }
        return array;
    }

    /**
     * Resizes the given array to the desired length, and pads with nulls.
     *
     * @param array
     * @param newLength
     * @param <T>
     * @return
     */
    public static <T> T[] resize(T[] array, int newLength) {
        return resize(array, newLength, null);
    }

    /**
     * Grows the size of the given array by the given count, and pads
     * with the given item.
     *
     * @param array
     * @param count
     * @param item
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] grow(T[] array, int count, T item, Class<T> klass) {
        return resize(array, array == null ? count : array.length + count, item, klass);
    }

    /**
     * Shrinks the size of the given array by the given count.
     *
     * @param array
     * @param count
     * @param <T>
     * @return
     */
    public static <T> T[] shrink(T[] array, int count) {
        if (array != null) {
            int length = array.length - count;
            array = resize(array, length < 0 ? 0 : length);
        }
        return array;
    }

    /**
     * Returns true if the given item is found in the given array.
     *
     * @param array
     * @param item
     * @param <T>
     * @return
     */
    public static <T> boolean include(T[] array, T item) {
        boolean found = false;

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                found = ObjectHelper.equal(array[i], item);
                if (found) break;
            }
        }

        return found;
    }

    /**
     * Returns a new array with the given item inserted at the given index.
     *
     * @param array
     * @param item
     * @param index
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] insert(T[] array, T item, int index, Class<T> klass) {
        if (array == null) array = (T[])java.lang.reflect.Array.newInstance(klass, 0);

        java.util.ArrayList<T> list = new java.util.ArrayList<T>(java.util.Arrays.asList(array));

        int capacity = 0;
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

        return list.toArray(array);
    }

    /**
     * Returns only the items in x that are also in y.
     *
     * @param firstArray
     * @param secondArray
     * @param <T>
     * @return
     */
    public static <T> T[] intersection(T[] firstArray, T[] secondArray) {
        if (firstArray == null || secondArray == null) return null;

        java.util.List<T> d = new java.util.ArrayList<T>(firstArray.length);
        d.addAll(java.util.Arrays.asList(firstArray));
        d.retainAll(java.util.Arrays.asList(secondArray));

        return d.toArray(java.util.Arrays.copyOf(firstArray, 0));
    }

    /**
     * Returns a string created by concatenating each element of the given array,
     * separated by the given separator string.
     *
     * @param array
     * @param separator
     * @param <T>
     * @return
     */
    public static <T> java.lang.String join(T[] array, java.lang.String separator) {
        StringBuilder builder = new StringBuilder();

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                T value = array[i];
                if (value != null) builder.append(value.toString());
                if (separator != null && i < array.length - 1) builder.append(separator);
            }
        }

        return builder.toString();
    }

    /**
     * Returns a string representation of the given array.
     * @param array The array to be stringified.
     * @param <T>   The class of items stored in the array.
     * @return      A string representation of the given array.
     */
    public static <T> String stringify(T[] array) {
        return "[" + join(array, ", ") + "]";
    }

    /**
     * Returns a new array with a new element inserted at the beginning.
     *
     * @param array
     * @param item
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] prepend(T[] array, T item, Class<T> klass) {
        return insert(array, item, 0, klass);
    }

    /**
     * Sets the element from the given array at the given index (supports ruby-style reverse
     * indexing).
     *
     * @param array
     * @param item
     * @param index
     * @param klass
     * @param <T>
     * @return
     */
    public static <T> T[] put(T[] array, T item, int index, Class<T> klass) {
        if (array == null) array = (T[])java.lang.reflect.Array.newInstance(klass, 0);

        // support reverse/tail indexing
        if (index < 0) index += array.length;
        int capacity = 0;
        if (index < 0) {
            capacity = (index * -1) + array.length;
            index = 0;
        } else {
            capacity = index + 1;
        }
        if (capacity > array.length) array = java.util.Arrays.copyOf(array, capacity);

        array[index] = item;

        return array;
    }

    /**
     * Returns a new array with all elements from the given array but in reverse order.
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] reverse(T[] array) {
        if (array == null) return array;

        java.util.List<T> list = java.util.Arrays.asList(java.util.Arrays.copyOf(array, array.length));
        java.util.Collections.reverse(list);

        return list.toArray(java.util.Arrays.copyOf(array, list.size()));
    }

    /**
     * Returns a new array which is a subset of elements from the given array.
     *
     * @param array
     * @param index
     * @param length
     * @param <T>
     * @return
     */
    public static <T> T[] slice(T[] array, int index, int length) {
        if (array == null || array.length == 0) return array;
        // support reverse/tail length
        if (length < 0) length = array.length + length;
        // support reverse/tail indexing
        if (index < 0) index += array.length;
        // don't slice past the end of the array
        if ((length += index) > array.length) length = array.length;

        return java.util.Arrays.copyOfRange(array, index, length);
    }

    /**
     * Returns a new array with all elements sorted.
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] sort(T[] array) {
        if (array == null) return array;

        T[] copy = java.util.Arrays.copyOf(array, array.length);
        java.util.Arrays.sort(copy);
        return copy;
    }

    /**
     * Returns a new array with all string items trimmed, all empty string
     * items removed, and all null items removed.
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] squeeze(T[] array) {
        if (array == null || array.length == 0) return null;

        java.util.List<T> list = new java.util.ArrayList<T>(array.length);

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i] instanceof java.lang.String) {
                T item = (T)((java.lang.String)array[i]).trim();
                if (item.equals("")) {
                    array[i] = null;
                } else {
                    array[i] = item;
                }
            }
            if (array[i] != null) list.add(array[i]);
        }

        array = list.toArray(java.util.Arrays.copyOf(array, list.size()));
        if (array.length == 0) array = null;

        return array;
    }

    /**
     * Returns a new array with all duplicate elements removed.
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] unique(T[] array) {
        if (array == null) return array;
        java.util.Set<T> set = new java.util.TreeSet<T>(java.util.Arrays.asList(array));
        return set.toArray(java.util.Arrays.copyOf(array, set.size()));
    }
}
