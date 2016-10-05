/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a given Map object in a Map and IData compatible interface.
 *
 * @param <K>   The class of keys held by this Map.
 * @param <V>   The class of values held by this Map.
 */
public abstract class MapEnvelope<K, V> extends AbstractIData implements Map<K, V> {
    /**
     * The wrapped map object.
     */
    protected Map<K, V> map;

    /**
     * Constructs a new MapEnvelope.
     *
     * @param map   The map to be wrapped.
     */
    public MapEnvelope(Map<K, V> map) {
        if (map == null) throw new NullPointerException("map must not be null");
        this.map = map;
    }

    /**
     * Returns the map which this object wraps.
     *
     * @return The map which this object wraps.
     */
    public Map<K, V> getMap() {
        return map;
    }

    /**
     * Removes all elements from the map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Returns true if this map contains an element with the given key.
     *
     * @param key   The key whose presence is to be tested.
     * @return      True if this map contains an element with the given key.
     */
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * Returns true if this map contains one or more elements with the given value.
     *
     * @param value The value whose presence is to be tested.
     * @return      True if this map contains one or more elements with the given value.
     */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * Returns a set view of the elements in this map.
     *
     * @return A set view of the elements in this map.
     */
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    /**
     * Returns true if this object is equal to the given other object.
     *
     * @param other The object to compare equality with.
     * @return      True if this object is equal to the given other object.
     */
    public boolean equals(Object other) {
        return map.equals(other);
    }

    /**
     * Returns the value associated with the given key.
     *
     * @param key   The key whose value is to be returned.
     * @return      The value associated with the given key, or null if the key does not exist.
     */
    public V get(Object key) {
        return map.get(key);
    }

    /**
     * Returns the hash code value for this object.
     *
     * @return The hash code value for this object.
     */
    public int hashCode() {
        return map.hashCode();
    }

    /**
     * Returns true if there are no elements in this map.
     *
     * @return True if there are no elements in this map.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns the set of keys held by this map.
     *
     * @return The set of keys held by this map.
     */
    public Set<K> keySet() {
        return map.keySet();
    }

    /**
     * Associated the given value with the given key in this map.
     *
     * @param key   The key to associate with the given value.
     * @param value The value to associate with the given key.
     * @return      The previous value associated with the key, or null if there was no previous value.
     */
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * Copies all of the elements from the given map to this map.
     *
     * @param other The map to copy elements from.
     */
    public void putAll(Map<? extends K, ? extends V> other) {
        map.putAll(other);
    }

    /**
     * Removes the element with the given key from this map.
     *
     * @param key The key whose associated element is to be removed.
     * @return    The previous value associated with the key, or null if there was no previous value.
     */
    public V remove(Object key) {
        return map.remove(key);
    }

    /**
     * Returns the number of elements in this map.
     *
     * @return The number of elements in this map.
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns a string representation of this object.
     *
     * @return A string representation of this object.
     */
    public String toString() {
        return map.toString();
    }

    /**
     * Returns a Collection view if the values contained in this map.
     *
     * @return A Collection view if the values contained in this map.
     */
    public Collection<V> values() {
        return map.values();
    }
}