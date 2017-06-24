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

import java.util.Arrays;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection of convenience methods for working with java.util.SortedSet objects.
 */
public class SortedSetHelper {
    /**
     * Disallow instantiation of this class.
     */
    private SortedSetHelper() {}

    /**
     * Returns a new SortedSet containing the given items.
     *
     * @param items     The items to be added to the returned SortedSet.
     * @param <T>       The class of the items in the SortedSet.
     * @return          A new SortedSet containing the given items, or null if items was null.
     */
    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> of(T... items) {
        return of(false, items);
    }

    /**
     * Returns a new SortedSet containing the given items.
     *
     * @param returnEmpty   If true, an empty SortedSet will be returned if items is null, otherwise null is returned.
     * @param items         The items to be added to the returned SortedSet.
     * @param <T>           The class of the items in the SortedSet.
     * @return              A new SortedSet containing the given items.
     */
    public static <T> SortedSet<T> of(boolean returnEmpty, T... items) {
        SortedSet<T> set;

        if (items == null) {
            set = new TreeSet<T>();
        } else {
            set = new TreeSet<T>(Arrays.asList(items));
        }

        return set;
    }

    /**
     * Returns a new SortedSet containing the items in the given collection.
     *
     * @param collection    A collection containing the items to be added to the returned SortedSet.
     * @param <T>           The class of the items in the SortedSet.
     * @return              A new SortedSet containing the given items, or null if items was null.
     */
    @SuppressWarnings("unchecked")
    public static <T> SortedSet<T> of(Collection<T> collection) {
        return of(false, collection);
    }

    /**
     * Returns a new SortedSet containing the given items.
     *
     * @param returnEmpty   If true, an empty SortedSet will be returned if collection is null, otherwise null is
     *                      returned.
     * @param collection    A collection containing the items to be added to the returned SortedSet.
     * @param <T>           The class of the items in the SortedSet.
     * @return              A new SortedSet containing the given items.
     */
    public static <T> SortedSet<T> of(boolean returnEmpty, Collection<T> collection) {
        SortedSet<T> set;

        if (collection == null) {
            set = new TreeSet<T>();
        } else {
            set = new TreeSet<T>(collection);
        }

        return set;
    }
}
