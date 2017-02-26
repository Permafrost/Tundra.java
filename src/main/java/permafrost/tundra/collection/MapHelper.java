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
import permafrost.tundra.data.MapIData;
import permafrost.tundra.lang.ArrayHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A collection of convenience methods for working with java.util.Map objects.
 */
public final class MapHelper {
    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param map   The map to be operated on.
     * @param key   The key whose associated value is to be returned.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      The value to which the specified key is mapped, or null if this map contains no mapping for the key.
     */
    public static <K, V> V get(Map<K, V> map, K key) {
        V value = null;
        if (map != null) {
            value = map.get(key);
        }
        return value;
    }

    /**
     * Associates the specified value with the specified key in this map. If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.
     *
     * @param map   The map to be operated on.
     * @param key   Key with which the specified value is to be associated.
     * @param value Value to be associated with the specified key.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     */
    public static <K, V> void put(Map<K, V> map, K key, V value) {
        if (map != null) {
            map.put(key, value);
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @param map   The map to be operated on.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      The number of key-value mappings in this map.
     */
    public static <K, V> int length(Map<K, V> map) {
        if (map == null) return 0;
        return map.size();
    }

    /**
     * Removes all of the mappings from this map (optional operation). The map will be empty after this call returns.
     *
     * @param map   The map to be operated on.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     */
    public static <K, V> void clear(Map<K, V> map) {
        if (map != null) map.clear();
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param map   The map to be operated on.
     * @param key   The key whose mapping is to be removed from the map.
     * @param <K>   The class of keys in this map.
     * @param <V>   The class of values in the map.
     * @return      The previous value associated with key, or null if there was no mapping for key.
     */
    public static <K, V> V remove(Map<K, V> map, K key) {
        if (map == null) return null;
        return map.remove(key);
    }

    /**
     * Returns a newly created Map object.
     *
     * @param <K>                   The class of Map keys.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map object.
     */
    public static <K, V> Map<K, V> create() {
        return create(false);
    }

    /**
     * Returns a newly created Map object.
     *
     * @param sorted                Whether the Map should maintain keys sorted in natural ascending order.
     * @param <K>                   The class of Map keys.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map object.
     */
    public static <K, V> Map<K, V> create(boolean sorted) {
        Map<K, V> map;

        if (sorted) {
            map = new TreeMap<K, V>();
        } else {
            map = new HashMap<K, V>();
        }

        return map;
    }

    /**
     * Converts the given IData document to a Map.
     *
     * @param document              The IData document to be converted.
     * @return                      A newly created Map which contains the top-level key value elements from the
     *                              given document.
     */
    public static Map<String, Object> mapify(IData document) {
        return mapify(document, false);
    }

    /**
     * Converts the given IData document to a Map.
     *
     * @param document              The IData document to be converted.
     * @param sorted                Whether the Map should maintain keys sorted in natural ascending order.
     * @return                      A newly created Map which contains the top-level key value elements from the
     *                              given document.
     */
    public static Map<String, Object> mapify(IData document, boolean sorted) {
        return mapify(document, sorted, Object.class);
    }

    /**
     * Converts the given IData document to a Map.
     *
     * @param document              The IData document to be converted.
     * @param klass                 The class of Map values.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map which contains the top-level key value elements from the
     *                              given document.
     */
    public static <V> Map<String, V> mapify(IData document, Class<V> klass) {
        return mapify(document, false, klass);
    }

    /**
     * Converts the given IData document to a Map.
     *
     * @param document              The IData document to be converted.
     * @param sorted                Whether the Map should maintain keys sorted in natural ascending order.
     * @param klass                 The class of Map values.
     * @param <V>                   The class of Map values.
     * @return                      A newly created Map which contains the top-level key value elements from the
     *                              given document.
     */
    @SuppressWarnings("unchecked")
    public static <V> Map<String, V> mapify(IData document, boolean sorted, Class<V> klass) {
        Map<String, V> map = create(sorted);

        if (document != null) {
            IDataCursor cursor = document.getCursor();
            while (cursor.next()) {
                map.put(cursor.getKey(), (V)cursor.getValue());
            }
        }

        return map;
    }

    /**
     * Returns the list of keys stored in the given map.
     *
     * @param map       The map to be operated on.
     * @param keyClass  The class of keys stored in the map.
     * @param <K>       The class of keys stored in the map.
     * @param <V>       The class of values stored in the map.
     * @return          The list of keys stored in the map.
     */
    public static <K, V> K[] keys(Map<K, V> map, Class<K> keyClass) {
        if (map == null) return null;
        return CollectionHelper.arrayify(map.keySet(), keyClass);
    }

    /**
     * Returns the list of keys stored in the given map.
     *
     * @param map       The map to be operated on.
     * @return          The list of keys stored in the map.
     */
    public static Object[] keys(Map map) {
        if (map == null) return null;
        return CollectionHelper.arrayify(map.keySet());
    }

    /**
     * Returns the lisst of values stored in the given map.
     *
     * @param map           The map to be operated on.
     * @param valueClass    The class of values stored in the map.
     * @param <K>           The class of keys stored in the map.
     * @param <V>           The class of values stored in the map.
     * @return              The list of values stored in the map.
     */
    public static <K, V> V[] values(Map<K, V> map, Class<V> valueClass) {
        if (map == null) return null;
        return CollectionHelper.arrayify(map.values(), valueClass);
    }

    /**
     * Returns the lisst of values stored in the given map.
     *
     * @param map           The map to be operated on.
     * @return              The list of values stored in the map.
     */
    public static Object[] values(Map map) {
        if (map == null) return null;
        return CollectionHelper.arrayify(map.values());
    }
}
