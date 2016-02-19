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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of convenience methods for working with java.util.List objects.
 */
public class ListHelper {
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
    @SuppressWarnings("unchecked")
    public static <E> List<E> append(List<E> list, int minCapacity, boolean includeNulls, E ... items) {
        list = (List)CollectionHelper.append(createOrGrow(list, minCapacity), includeNulls, items);
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
            if (index < 0) {
                index = Math.abs(index) - 1;
            }

            if (index > list.size()) {
                minCapacity = index + items.length;
            } else {
                minCapacity = list.size() + items.length;
            }
        }
        return minCapacity;
    }

    /**
     * Removes all items from the given list, or returns a new list if the given list is null.
     *
     * @param list  The list to be cleared.
     * @param klass The component type of the list and resulting array.
     * @param <E>   The component type of the list.
     * @return      The cleared list.
     */
    public static <E> List<E> clear(List<E> list, Class<E> klass) {
        if (list == null) {
            list = create();
        } else {
            CollectionHelper.clear(list);
        }

        return list;
    }

    /**
     * Creates a new list.
     *
     * @param <E>             The component type of the list.
     * @return                A new list.
     */
    public static <E> List<E> create() {
        return new ArrayList<E>();
    }

    /**
     * Creates a new list with the given initial capacity.
     *
     * @param initialCapacity The initial capacity of the new list.
     * @param <E>             The component type of the list.
     * @return                A new list.
     */
    public static <E> List<E> create(int initialCapacity) {
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

            int capacity, fillIndex;
            if (index < 0) {
                capacity = Math.abs(index) + list.size() + items.length - 1;
                index = fillIndex = 0;
            } else {
                capacity = index;
                fillIndex = list.size();
            }

            if (capacity > list.size()) {
                // fill the list with nulls if it needs to be extended
                for (int i = list.size(); i < capacity; i++) {
                    list.add(fillIndex, null);
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

    /**
     * Returns true if an item exists at the given index in the given list.
     *
     * @param list  The list to check item existence in.
     * @param index The index to check item existence of.
     * @param <E>   The component type of the list.
     * @return      True if an item exists at the given index in the given list.
     */
    public static <E> boolean exists(List<E> list, int index) {
        if (list == null) return false;

        // support negative/reverse indexing
        if (index < 0) index += list.size();

        return index >= 0 && index < list.size();
    }

    /**
     * Returns the item at the given index from the given list.
     *
     * @param list  The list to get the item from.
     * @param index The index of the item to return.
     * @param <E>   The component type of the list.
     * @return      The item at the given index in the given list, or null if the list is null or the item does not
     *              exist.
     */
    public static <E> E get(List<E> list, int index) {
        if (list == null) throw new NullPointerException("list must not be null");

        // support negative/reverse indexing
        if (index < 0) index += list.size();

        return list.get(index);
    }

    /**
     * Replaces the value of the item at the given index in the given list.
     *
     * @param list  The list to replace a value in.
     * @param index The index of the item whose value is to be replaced.
     * @param item  The new value for the item.
     * @param <E>   The component type of the list.
     * @return      The previous value of the item at the given index.
     */
    public static <E> E set(List<E> list, int index, E item) {
        if (list == null) throw new NullPointerException("list must not be null");

        // support negative/reverse indexing
        if (index < 0) index += list.size();

        return list.set(index, item);
    }

    /**
     * Removes the item at the given index in the given list.
     *
     * @param list  The list to remove the item from.
     * @param index The index of the item to be removed.
     * @param <E>   The component type of the list.
     * @return      The item previously at the given index.
     */
    public static <E> E remove(List<E> list, int index) {
        if (list == null) throw new NullPointerException("list must not be null");

        // support negative/reverse indexing
        if (index < 0) index += list.size();

        return list.remove(index);
    }

    /**
     * Removes and returns the given count of items from the head of the given list.
     *
     * @param list  The list to take the items from.
     * @param count The number of items to take from the list.
     * @param <E>   The component type of the list.
     * @return      A new list containing the items taken from the head of the given list.
     */
    public static <E> List<E> take(List<E> list, int count) {
        if (list == null) return null;
        if (count < 0) throw new IllegalArgumentException("count must not be negative");

        List<E> head = create(count);

        while(head.size() < count && list.size() > 0) {
            head.add(list.remove(0));
        }

        return head;
    }

    /**
     * Returns a new list with the items in the given list in reverse order.
     *
     * @param list  The list to be reversed.
     * @param <E>   The component type of the list.
     * @return      The reverse of the given list.
     */
    public static <E> List<E> reverse(List<E> list) {
        if (list == null) return null;

        List<E> reverseList = create();

        for (int i = list.size() - 1; i >= 0; i--) {
            reverseList.add(list.get(i));
        }

        return reverseList;
    }

    /**
     * Returns a set intersection of the given lists.
     *
     * @param lists The lists to find the set intersection of.
     * @param <E>   The component type of the lists.
     * @return      The set intersection of the given lists.
     */
    public static <E> List<E> intersect(List<E> ... lists) {
        List<E> results = create();

        boolean seeded = false;

        for (List<E> list : lists) {
            if (list != null) {
                if (seeded) {
                    results.retainAll(list);
                } else {
                    results.addAll(list);
                    seeded = true;
                }
            }
        }

        return results;
    }

    /**
     * Returns the set difference of the given lists.
     *
     * @param firstList     The list to be subtracted from.
     * @param secondList    The list subtracted from the first list.
     * @param <E>           The component type of the lists.
     * @return              The set difference of the two lists.
     */
    public static <E> List<E> difference(List<E> firstList, List<E> secondList) {
        List<E> results = create();

        if (firstList != null) results.addAll(firstList);
        if (secondList != null) results.removeAll(secondList);

        return results;
    }

    /**
     * Returns a new list with all duplicate items removed.
     *
     * @param list  A list.
     * @param <E>   The component type of the list.
     * @return      A new list containing only the unique items from the given list.
     */
    public static <E> List<E> unique(List<E> list) {
        Set<E> set = new LinkedHashSet<E>();

        if (list != null) {
            set.addAll(list);
        }

        List<E> results = create(set.size());
        results.addAll(set);

        return results;
    }
}
