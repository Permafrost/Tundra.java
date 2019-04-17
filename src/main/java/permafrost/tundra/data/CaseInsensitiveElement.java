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
import permafrost.tundra.lang.CaseInsensitiveString;
import permafrost.tundra.lang.LocaleHelper;
import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * An element whose keys are case-insensitive for comparison and case-preserving for reference.
 *
 * @param <V> The value's class.
 */
public class CaseInsensitiveElement<V> extends KeyAliasElement<String, V> implements Serializable {
    /**
     * The serialization class version identity.
     */
    private static final long serialVersionUID = 1;
    /**
     * Case insensitive version of the key.
     */
    protected CaseInsensitiveString caseInsensitiveKey;
    /**
     * The locale to use for case insensitivity.
     */
    protected Locale locale;

    /**
     * Constructs a new element using the given existing element.
     *
     * @param element   The element to use when constructing this new element.
     */
    public CaseInsensitiveElement(Element<String, V> element) {
        this(element, null);
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param element   The element to use when constructing this new element.
     * @param locale    The locale to use for case comparisons.
     */
    public CaseInsensitiveElement(Element<String, V> element, Locale locale) {
        this(element.getKey(), element.getValue(), locale);
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     */
    public CaseInsensitiveElement(String key, V value) {
        this(key, value, null);
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     * @param locale    The locale used for case comparison.
     */
    public CaseInsensitiveElement(String key, V value, Locale locale) {
        this(key, value, locale, (Collection<String>)null);
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     * @param locale    The locale used for case comparison.
     * @param aliases   The list of aliases for this element's key.
     */
    public CaseInsensitiveElement(String key, V value, Locale locale, String ...aliases) {
        this(key, value, locale, ListHelper.of(aliases));
    }

    /**
     * Constructs a new element using the given key and value.
     *
     * @param key       The key for the element.
     * @param value     The value for the element.
     * @param locale    The locale used for case comparison.
     * @param aliases   The list of aliases for this element's key.
     */
    public CaseInsensitiveElement(String key, V value, Locale locale, Collection<String> aliases) {
        super(key, value, aliases);
        setLocale(locale);
        setKey(key);
    }

    /**
     * Returns the key of the element.
     *
     * @return The key of the element.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the element.
     *
     * @param newKey    The key to be set.
     * @return          The previous key of the element.
     */
    public String setKey(String newKey) {
        if (newKey == null) throw new NullPointerException("key must not be null");
        String oldKey = key;
        key = newKey;
        caseInsensitiveKey = new CaseInsensitiveString(key, locale);
        return oldKey;
    }

    /**
     * Returns the locale associated with this element.
     *
     * @return The locale associated with this element.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale to use for case comparison.
     *
     * @param locale The locale to use for case comparison.
     */
    private void setLocale(Locale locale) {
        this.locale = LocaleHelper.normalize(locale);
    }

    /**
     * Returns true if another entry's key is equal to this entry's key.
     *
     * @param other The other entry to compare key equality to.
     * @return      True if the other entry's key is equal to this entry's key.
     */
    public boolean keyEquals(String other) {
        boolean result = caseInsensitiveKey.equals(other);
        if (result) return true;

        for (String alias : keyAliases) {
            result = new CaseInsensitiveString(alias, locale).equals(other);
            if (result) return true;
        }

        return false;
    }

    /**
     * Compares this element's key with the given other element's key, if they implement the Comparable interface.
     *
     * @param other The other element to compare this object with.
     * @return      The comparison result.
     */
    @SuppressWarnings("unchecked")
    public int compareTo(Map.Entry<? extends String, ? extends V> other) {
        return caseInsensitiveKey.compareTo(other.getKey());
    }

    /**
     * Returns the hash code for this element.
     *
     * @return The hash code for this element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public int hashCode() {
        return getKey().hashCode();
    }

    /**
     * Returns the given element if it is already a CaseInsensitiveElement that uses the specified
     * locale, otherwise returns a new CaseInsensitiveElement with the given element's key and value.
     *
     * @param element               The element to be normalized.
     * @param locale                Optional locale to use for case comparison.
     * @param <V>                   The class of values held by the element.
     * @return                      The normalized element.
     * @throws NullPointerException If the given element is null.
     */
    @SuppressWarnings("unchecked")
    public static <V> Element<String, V> normalize(Element<String, V> element, Locale locale) {
        CaseInsensitiveElement<V> output;

        if (element instanceof CaseInsensitiveElement) {
            locale = LocaleHelper.normalize(locale);
            if (!locale.equals(((CaseInsensitiveElement)element).getLocale())) {
                output = new CaseInsensitiveElement<V>(element, locale);
            } else {
                output = (CaseInsensitiveElement)element;
            }
        } else {
            output = new CaseInsensitiveElement<V>(element, locale);
        }

        return output;
    }
}
