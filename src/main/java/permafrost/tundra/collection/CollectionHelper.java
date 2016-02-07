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
import java.util.Collection;

/**
 * A collection of convenience methods for working with java.util.Collection objects.
 */
public class CollectionHelper {
    /**
     * Disallow instantiation of this class.
     */
    private CollectionHelper() {}

    /**
     * Returns the number of items in the given collection.
     *
     * @param collection The collection to return the length of.
     * @return           The number of items in the given collection.
     */
    public static <E> int length(Collection<E> collection) {
        if (collection == null) return 0;
        return collection.size();
    }

    /**
     * Appends the given items to the given collection. Null items are not appended.
     *
     * @param collection    The collection to append the items to.
     * @param items         The items to be appended.
     * @param <E>           The component type of the items stored in the collection.
     * @return              The given collection.
     */
    public static <E> Collection append(Collection<E> collection, E ...items) {
        return append(collection, false, items);
    }

    /**
     * Appends the given items to the given collection.
     *
     * @param collection    The collection to append the items to.
     * @param includeNulls  If true, null values will be appended, otherwise they will not.
     * @param items         The items to be appended.
     * @param <E>           The component type of the items stored in the collection.
     * @return              The given collection.
     */
    public static <E> Collection append(Collection<E> collection, boolean includeNulls, E ...items) {
        if (collection != null && items != null) {
            for (E item : items) {
                if (includeNulls || item != null) collection.add(item);
            }
        }

        return collection;
    }

    /**
     * Removes all elements from the given collection.
     *
     * @param collection    The collection to be cleared.
     * @param <E>           The component type of the collection.
     * @return              The cleared collection.
     */
    public static <E> Collection<E> clear(Collection<E> collection) {
        if (collection != null) {
            collection.clear();
        }
        return collection;
    }

    /**
     * Converts the given collection to an array.
     *
     * @param collection    The collection to be converted.
     * @param klass         The component type of the collection and resulting array.
     * @param <E>           The component type of the collection and resulting array.
     * @return              An array representation of the given collection.
     */
    public static <E> E[] arrayify(Collection<E> collection, Class<E> klass) {
        if (collection == null) return null;
        return collection.toArray(ArrayHelper.instantiate(klass, collection.size()));
    }

    /**
     * Returns true if the given collection includes the given item.
     *
     * @param collection    The collection to check item existence in.
     * @param item          The item whose existence in the collection is to be checked.
     * @param <E>           The component type of the collection.
     * @return              True if the given collection contains the given item.
     */
    public static <E> boolean includes(Collection<E> collection, E item) {
        if (collection == null) return false;
        return collection.contains(item);
    }
}
