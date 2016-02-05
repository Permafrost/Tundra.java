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

package permafrost.tundra.collection;

import permafrost.tundra.lang.ArrayHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of convenience methods for working with java.util.List objects.
 */
public class ListHelper {
    public static int DEFAULT_LIST_CAPACITY = 64;

    /**
     * Disallow instantiation of this class.
     */
    private ListHelper() {}

    /**
     * Appends the given items to the given list.
     *
     * @param list          The list to append the items to.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> append(List<E> list, E ... items) {
        return append(list, false, items);
    }

    /**
     * Appends the given items to the given list.
     *
     * @param list          The list to append the items to.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> append(List<E> list, boolean includeNulls, E ... items) {
        return append(list, calculateMinimumCapacity(list, items), includeNulls, items);
    }

    /**
     * Appends the given items to the given list.
     *
     * @param list          The list to append the items to.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> append(List<E> list, int minCapacity, E ... items) {
        return append(list, minCapacity, false, items);
    }

    /**
     * Appends the given items to the given list.
     *
     * @param list          The list to append the items to.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> append(List<E> list, int minCapacity, boolean includeNulls, E ... items) {
        CollectionHelper.append(createOrGrow(list, minCapacity), includeNulls, items);
        return list;
    }

    /**
     * Returns the minimum capacity required of a list to hold the given items.
     *
     * @param list  The list whose minimum capacity is to be calculated.
     * @param items The items to be added to the list.
     * @param <E>   The component type of the list.
     * @return      The minimum capacity required of the given list to hold the given items.
     */
    private static <E> int calculateMinimumCapacity(List<E> list, E ... items) {
        return calculateMinimumCapacity(list, 0, items);
    }

    /**
     * Returns the minimum capacity required of a list to hold the given items.
     *
     * @param list  The list whose minimum capacity is to be calculated.
     * @param items The items to be added to the list.
     * @param <E>   The component type of the list.
     * @return      The minimum capacity required of the given list to hold the given items.
     */
    private static <E> int calculateMinimumCapacity(List<E> list, int index, E ... items) {
        int minCapacity = 0;
        if (items != null && list != null) {
            // support reverse/tail indexing
            if (index < 0) index += list.size();

            if (index > list.size()) {
                minCapacity = index + items.length;
            } else {
                minCapacity = list.size() + items.length;
            }
        }
        return minCapacity;
    }

    /**
     * Creates a new list.
     *
     * @param <E>             The component type of the list.
     * @return                A new list.
     */
    public static <E> List<E> create() {
        return create(DEFAULT_LIST_CAPACITY);
    }

    /**
     * Creates a new list with the given initial capacity.
     *
     * @param initialCapacity The initial capacity of the new list.
     * @param <E>             The component type of the list.
     * @return                A new list.
     */
    public static <E> List<E> create(int initialCapacity) {
        if (initialCapacity < DEFAULT_LIST_CAPACITY) initialCapacity = DEFAULT_LIST_CAPACITY;
        return new ArrayList<E>(initialCapacity);
    }

    /**
     * Grows the given list to the given capacity, or creates a new list if the given list is null.
     *
     * @param list          The list to be grown, or null.
     * @param minCapacity   The capacity to grow the list to.
     * @param <E>           The component type of the list.
     * @return              Either the given list grown to the given capacity, or a new list.
     */
    private static <E> List<E> createOrGrow(List<E> list, int minCapacity) {
        if (list == null) {
            list = create(minCapacity);
        } else if (list instanceof ArrayList) {
            ((ArrayList)list).ensureCapacity(minCapacity);
        }
        return list;
    }

    /**
     * Prepends the given items to the front of the given list, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> prepend(List<E> list, E ...items) {
        return prepend(list, false, items);
    }

    /**
     * Prepends the given items to the front of the given list, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> prepend(List<E> list, boolean includeNulls, E ...items) {
        return prepend(list, calculateMinimumCapacity(list, items), includeNulls, items);
    }

    /**
     * Prepends the given items to the front of the given list, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> prepend(List<E> list, int minCapacity, E ...items) {
        return prepend(list, minCapacity, false, items);
    }

    /**
     * Prepends the given items to the front of the given list, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> prepend(List<E> list, int minCapacity, boolean includeNulls, E ...items) {
        return insert(list, minCapacity, includeNulls, 0, items);
    }

    /**
     * Inserts the given items at the given index, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param index         The index at which the items will be inserted.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> insert(List<E> list, int index, E ...items) {
        return insert(list, false, index, items);
    }

    /**
     * Inserts the given items at the given index, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param index         The index at which the items will be inserted.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> insert(List<E> list, boolean includeNulls, int index, E ...items) {
        return insert(list, calculateMinimumCapacity(list, index, items), includeNulls, index, items);
    }

    /**
     * Inserts the given items at the given index, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param index         The index at which the items will be inserted.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> insert(List<E> list, int minCapacity, int index, E ...items) {
        return insert(list, minCapacity, false, index, items);
    }

    /**
     * Inserts the given items at the given index, shifting the existing items if any to the right.
     *
     * @param list          The list to insert the items into.
     * @param minCapacity   The minimum capacity the list should have before adding the items.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param index         The index at which the items will be inserted.
     * @param items         The items to be added to the list.
     * @param <E>           The component type of the list.
     * @return              The given list.
     */
    public static <E> List<E> insert(List<E> list, int minCapacity, boolean includeNulls, int index, E ...items) {
        list = createOrGrow(list, minCapacity);

        if (items != null) {
            // support reverse/tail indexing
            if (index < 0) index += list.size() + 1;

            int capacity;
            if (index < 0) {
                capacity = (index * -1) + list.size() + items.length;
                index = 0;
            } else {
                capacity = index;
            }

            if (capacity >= list.size()) {
                // fill the list with nulls if it needs to be extended
                for (int i = list.size(); i < capacity; i++) {
                    list.add(i, null);
                }
            }

            for (E item : items) {
                if (includeNulls || item != null) {
                    list.add(index++, item);
                }
            }
        }

        return list;
    }

    /**
     * Converts the given list to an array.
     *
     * @param list  The list to be converted.
     * @param klass The component type of the list and resulting array.
     * @param <E>   The component type of the list and resulting array.
     * @return      An array representation of the given list.
     */
    public static <E> E[] arrayify(List<E> list, Class<E> klass) {
        if (list == null) return null;
        return list.toArray(ArrayHelper.instantiate(klass, list.size()));
    }

    /**
     * Converts the given array to a list.
     *
     * @param array The array to be converted.
     * @param klass The component type of the list and resulting array.
     * @param <E>   The component type of the array and resulting list.
     * @return      A list representation of the given array.
     */
    public static <E> List<E> listify(E[] array, Class<E> klass) {
        if (array == null) return null;
        List<E> list = create(array.length);
        return append(list, array);
    }
}
