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

import permafrost.tundra.collection.ListHelper;
import permafrost.tundra.collection.SortedSetHelper;
import permafrost.tundra.lang.ObjectHelper;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * An element that supports key aliases.
 *
 * @param <K>   The key's class.
 * @param <V>   The value's class.
 */
public class KeyAliasElement<K, V> extends Element<K, V> implements Serializable {
    /**
     * The serialization class version identity.
     */
    private static final long serialVersionUID = 1;
    /**
     * Set of aliases for the key of this element.
     */
    protected Set<K> keyAliases;

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     */
    public KeyAliasElement(K key, V value) {
        this(key, value, (Collection<K>)null);
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     * @param aliases   Optional key aliases.
     */
    public KeyAliasElement(K key, V value, K ... aliases) {
        this(key, value, ListHelper.of(aliases));
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     * @param aliases   Optional key aliases.
     */
    public KeyAliasElement(K key, V value, Collection<K> aliases) {
        super(key, value);
        keyAliases = SortedSetHelper.of(true, aliases);
    }

    /**
     * Adds one or more aliases for this element's key.
     *
     * @param aliases The aliases to be added.
     */
    public void addAlias(K ...aliases) {
        if (aliases != null) addAlias(Arrays.asList(aliases));
    }

    /**
     * Adds one or more aliases for this element's key.
     *
     * @param aliases The aliases to be added.
     */
    public void addAlias(Collection<K> aliases) {
        if (aliases != null) keyAliases.addAll(aliases);
    }

    /**
     * Removes one or more aliases for this element's key.
     *
     * @param aliases The aliases to be removed.
     */
    public void removeAlias(K ...aliases) {
        if (aliases != null) removeAlias(Arrays.asList(aliases));
    }

    /**
     * Removes one or more aliases for this element's key.
     *
     * @param aliases The aliases to be removed.
     */
    public void removeAlias(Collection<K> aliases) {
        if (aliases != null) keyAliases.removeAll(aliases);
    }

    /**
     * Returns true if another entry's key is equal to this entry's key.
     *
     * @param other The other entry to compare key equality to.
     * @return      True if the other entry's key is equal to this entry's key.
     */
    public boolean keyEquals(K other) {
        boolean result = ObjectHelper.equal(getKey(), other);
        if (result) return true;

        for (K alias : keyAliases) {
            result = ObjectHelper.equal(alias, other);
            if (result) return true;
        }

        return false;
    }

    /**
     * Returns the given element if it is already a CaseInsensitiveElement that uses the specified
     * locale, otherwise returns a new CaseInsensitiveElement with the given element's key and value.
     *
     * @param element               The element to be normalized.
     * @param <K>                   The class of keys held by the element.
     * @param <V>                   The class of values held by the element.
     * @return                      The normalized element.
     * @throws NullPointerException If the given element is null.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Element<K, V> normalize(Element<K, V> element) {
        if (element == null) throw new NullPointerException("element must not be null");

        KeyAliasElement<K, V> output;

        if (element instanceof KeyAliasElement) {
            output = (KeyAliasElement)element;
        } else {
            output = new KeyAliasElement<K, V>(element.getKey(), element.getValue());
        }

        return output;
    }
}
