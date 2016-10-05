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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import permafrost.tundra.data.ConcurrentMapIData;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A collection of convenience methods for working with java.util.concurrent.ConcurrentMap objects.
 */
public class ConcurrentMapHelper {
    /**
     * Returns a newly created Map object.
     *
     * @param <K>                   The class of Map keys.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map object.
     */
    public static <K, V> ConcurrentMap<K, V> create() {
        return new ConcurrentHashMap<K, V>();
    }

    /**
     * Converts the given IData document to a Map.
     *
     * @param document              The IData document to be converted.
     * @param <K>                   The class of Map keys.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map which contains the top-level key value elements from the
     *                              given document.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ConcurrentMap<K, V> mapify(IData document) {
        if (document == null) return null;

        ConcurrentMap<K, V> map = create();

        IDataCursor cursor = document.getCursor();
        while(cursor.next()) {
            map.put((K)cursor.getKey(), (V)cursor.getValue());
        }

        // wrap the map in an IData compatible wrapper for developer convenience
        return new ConcurrentMapIData<K, V>(map);
    }

    /**
     * If the specified key is not already associated with a value, associate it with the given value.
     *
     * @param map   The map to be operated on.
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      The previous value associated with the specified key, or null if there was no mapping for the key.
     */
    public static <K, V> V putIfAbsent(ConcurrentMap<K, V> map, K key, V value) {
        if (map == null) return null;
        return map.putIfAbsent(key, value);
    }

    /**
     * Removes the entry for a key only if currently mapped to a given value.
     *
     * @param map   The map to be operated on.
     * @param key   The key with which the specified value is associated.
     * @param value The value expected to be associated with the specified key.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      True if the value was removed.
     */
    public static <K, V> boolean remove(ConcurrentMap<K, V> map, K key, V value) {
        return map != null && map.remove(key, value);
    }

    /**
     * Replaces the entry for a key only if currently mapped to some value.
     *
     * @param map   The map to be operated on.
     * @param key   The key with which the specified value is associated.
     * @param value The value to be associated with the specified key.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      The previous value associated with the specified key, or null if there was no mapping for the key.
     */
    public static <K, V> V replace(ConcurrentMap<K, V> map, K key, V value) {
        if (map == null) return null;
        return map.replace(key, value);
    }

    /**
     * Replaces the entry for a key only if currently mapped to a given value.
     *
     * @param map       The map to be operated on.
     * @param key       The key with which the specified value is associated.
     * @param oldValue  The value expected to be associated with the specified key.
     * @param newValue  The value to be associated with the specified key.
     * @param <K>       The class of keys in this map.
     * @param <V>       The class of values in the map.
     * @return          True if the value was replaced.
     */
    public static <K, V> boolean replace(ConcurrentMap<K, V> map, K key, V oldValue, V newValue) {
        return map != null && map.replace(key, oldValue, newValue);
    }
}
