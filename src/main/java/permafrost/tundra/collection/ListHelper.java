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
        return append(list, calculateMinimumCapacity(list, items), items);
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
        if (list == null) {
            list = create(minCapacity);
        } else if (list instanceof ArrayList) {
            ((ArrayList)list).ensureCapacity(minCapacity);
        }
        if (items != null) {
            for (E item : items) {
                if (item != null) list.add(item);
            }
        }

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
        int minCapacity = 0;
        if (items != null && list != null) {
            minCapacity = list.size() + items.length;
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
}
