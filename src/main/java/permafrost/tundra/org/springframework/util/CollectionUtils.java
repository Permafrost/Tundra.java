/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package permafrost.tundra.org.springframework.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Miscellaneous collection utility methods.
 * Mainly for internal use within the framework.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Arjen Poutsma
 * @since 1.1.3
 */
public abstract class CollectionUtils {

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * Adapt a {@code Map<K, List<V>>} to an {@code MultiValueMap<K, V>}.
     * @param map the original map
     * @return the multi-value map
     * @since 3.1
     */
    public static <K, V> MultiValueMap<K, V> toMultiValueMap(Map<K, List<V>> map) {
        return new MultiValueMapAdapter<K, V>(map);
    }

    /**
     * Return an unmodifiable view of the specified multi-value map.
     * @param  map the map for which an unmodifiable view is to be returned.
     * @return an unmodifiable view of the specified multi-value map.
     * @since 3.1
     */
    @SuppressWarnings("unchecked")
    public static <K, V> MultiValueMap<K, V> unmodifiableMultiValueMap(MultiValueMap<? extends K, ? extends V> map) {
        Assert.notNull(map, "'map' must not be null");
        Map<K, List<V>> result = new LinkedHashMap<K, List<V>>(map.size());
        for (Map.Entry<? extends K, ? extends List<? extends V>> entry : map.entrySet()) {
            List<? extends V> values = Collections.unmodifiableList(entry.getValue());
            result.put(entry.getKey(), (List<V>) values);
        }
        Map<K, List<V>> unmodifiableMap = Collections.unmodifiableMap(result);
        return toMultiValueMap(unmodifiableMap);
    }

    /**
     * Adapts a Map to the MultiValueMap contract.
     */
    @SuppressWarnings("serial")
    private static class MultiValueMapAdapter<K, V> implements MultiValueMap<K, V>, Serializable {

        private final Map<K, List<V>> map;

        public MultiValueMapAdapter(Map<K, List<V>> map) {
            Assert.notNull(map, "'map' must not be null");
            this.map = map;
        }

        @Override
        public void add(K key, V value) {
            List<V> values = this.map.get(key);
            if (values == null) {
                values = new LinkedList<V>();
                this.map.put(key, values);
            }
            values.add(value);
        }

        @Override
        public V getFirst(K key) {
            List<V> values = this.map.get(key);
            return (values != null ? values.get(0) : null);
        }

        @Override
        public void set(K key, V value) {
            List<V> values = new LinkedList<V>();
            values.add(value);
            this.map.put(key, values);
        }

        @Override
        public void setAll(Map<K, V> values) {
            for (Entry<K, V> entry : values.entrySet()) {
                set(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Map<K, V> toSingleValueMap() {
            LinkedHashMap<K, V> singleValueMap = new LinkedHashMap<K,V>(this.map.size());
            for (Entry<K, List<V>> entry : map.entrySet()) {
                singleValueMap.put(entry.getKey(), entry.getValue().get(0));
            }
            return singleValueMap;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return this.map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.map.containsValue(value);
        }

        @Override
        public List<V> get(Object key) {
            return this.map.get(key);
        }

        @Override
        public List<V> put(K key, List<V> value) {
            return this.map.put(key, value);
        }

        @Override
        public List<V> remove(Object key) {
            return this.map.remove(key);
        }

        @Override
        public void putAll(Map<? extends K, ? extends List<V>> map) {
            this.map.putAll(map);
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<K> keySet() {
            return this.map.keySet();
        }

        @Override
        public Collection<List<V>> values() {
            return this.map.values();
        }

        @Override
        public Set<Entry<K, List<V>>> entrySet() {
            return this.map.entrySet();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            return map.equals(other);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }

        @Override
        public String toString() {
            return this.map.toString();
        }
    }

}
