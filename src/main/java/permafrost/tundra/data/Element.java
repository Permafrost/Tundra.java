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

package permafrost.tundra.data;

import permafrost.tundra.lang.ObjectHelper;
import java.io.Serializable;
import java.util.Map;

/**
 * An element is a key value pair.
 *
 * @param <K>   The key's class.
 * @param <V>   The value's class.
 */
public class Element<K, V> implements Map.Entry<K, V>, Comparable<Map.Entry<? extends K, ? extends V>>, Serializable {
    /**
     * The serialization class version identity.
     */
    private static final long serialVersionUID = 1;
    /**
     * The key of the element.
     */
    protected K key;
    /**
     * The value of the element.
     */
    protected V value;

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key   The key for the element.
     * @param value The value for the element.
     */
    public Element(K key, V value) {
        setKey(key);
        setValue(value);
    }

    /**
     * Returns the key of the element.
     *
     * @return The key of the element.
     */
    public K getKey() {
        return key;
    }

    /**
     * Sets the key of the element.
     *
     * @param key The key to be set.
     * @return    The previous key of the element.
     */
    public K setKey(K key) {
        if (key == null) throw new NullPointerException("key must not be null");
        K oldKey = this.key;
        this.key = key;
        return oldKey;
    }

    /**
     * Returns the value of the element.
     *
     * @return The value of the element.
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value of the element.
     *
     * @param value The value to be set.
     * @return      The previous value of the element.
     */
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    /**
     * Returns whether this element is equivalent to the given other element.
     *
     * @param other The other element to compare equivalency with.
     * @return      True if this element is equivalent to the give other element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other) {
        if (this == other) return true;

        boolean result = false;
        if (other instanceof Map.Entry) {
            result = keyEquals((Map.Entry)other);
        }

        return result;
    }

    /**
     * Returns true if another entry's key is equal to this entry's key.
     *
     * @param other The other entry to compare key equality to.
     * @return      True if the other entry's key is equal to this entry's key.
     */
    public boolean keyEquals(Map.Entry<K, V> other) {
        return keyEquals(other.getKey());
    }

    /**
     * Returns true if another entry's key is equal to this entry's key.
     *
     * @param other The other entry to compare key equality to.
     * @return      True if the other entry's key is equal to this entry's key.
     */
    public boolean keyEquals(K other) {
        return ObjectHelper.equal(getKey(), other);
    }

    /**
     * Compares this element's key with the given other element's key, if they implement the Comparable interface.
     *
     * @param other The other element to compare this object with.
     * @return      The comparison result.
     */
    @SuppressWarnings("unchecked")
    public int compareTo(Map.Entry<? extends K, ? extends V> other) {
        K key = getKey();
        if (key instanceof Comparable) {
            return ((Comparable)key).compareTo(other.getKey());
        } else {
            throw new UnsupportedOperationException("compareTo() not implemented as key class does not implement Comparable interface: " + key.getClass().getName());
        }
    }

    /**
     * Returns the hash code for this element.
     *
     * @return The hash code for this element.
     */
    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    /**
     * Returns a string representation of this element.
     *
     * @return A string representation of this element.
     */
    @Override
    public String toString() {
        return getKey() + " = " + ObjectHelper.stringify(getValue());
    }
}
