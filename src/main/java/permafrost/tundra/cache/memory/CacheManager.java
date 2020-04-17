/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.cache.memory;

import permafrost.tundra.data.MapIData;
import permafrost.tundra.time.DateTimeHelper;
import javax.xml.datatype.Duration;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides in-memory on-heap caching.
 *
 * @param <K>   The class of the cache keys.
 * @param <V>   The class of the cache values.
 */
public class CacheManager<K, V> {
    /**
     * Amortization schedule for removing expired cache entries.
     */
    protected static final long REMOVE_EXPIRED_ENTRIES_AFTER_CACHE_ACCESS_COUNT = 100;
    /**
     * The container for all caches.
     */
    protected final ConcurrentMap<String, ConcurrentMap<K, ExpiringValue<V>>> caches = new ConcurrentHashMap<String, ConcurrentMap<K, ExpiringValue<V>>>();
    /**
     * Counter used for amortizing the sweeping of expired values across cache accesses.
     */
    protected final AtomicLong cacheAccessCounter = new AtomicLong();

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final CacheManager<String, Object> INSTANCE = new CacheManager<String, Object>();
    }

    /**
     * Create a new CacheManager object.
     */
    public CacheManager() {}

    /**
     * Returns the default instance used by Tundra cache services.
     *
     * @return the default instance.
     */
    public static CacheManager<String, Object> getInstance() {
        return Holder.INSTANCE;
    }

    /**
     *  Returns all cached values for the cache with the given name.
     *
     * @param cacheName The name of the cache to use.
     * @return          The contents of the cache with the given name.
     */
    public MapIData<K, V> all(String cacheName) {
        ConcurrentMap<K, ExpiringValue<V>> cache = getCache(cacheName);
        MapIData<K, V> map = new MapIData<K, V>();

        for (Map.Entry<K, ExpiringValue<V>> entry : cache.entrySet()) {
            K key = entry.getKey();
            ExpiringValue<V> expiringValue = entry.getValue();

            if (!expiringValue.isExpired()) {
                map.put(key, expiringValue.getValue());
            }
        }

        return map;
    }

    /**
     * Removes all cached values from the cache with the given name.
     *
     * @param cacheName The name of the cache to use.
     */
    public void clear(String cacheName) {
        ConcurrentMap<K, ExpiringValue<V>> cache = caches.remove(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Returns true if the cache with the given name contains the given key.
     *
     * @param cacheName The name of the cache to use.
     * @param cacheKey  The key to check existence of.
     * @return          True if the cache contains the given key.
     */
    public boolean exists(String cacheName, K cacheKey) {
        return get(cacheName, cacheKey) != null;
    }

    /**
     * Returns the value associated with the given key from the cache with the given name.
     *
     * @param cacheName The name of the cache to use.
     * @param cacheKey  The key whose associated value is to be returned.
     * @return          The value associated with the given key in the cache with the given name, or null if the key
     *                  does not exist.
     */
    public ExpiringValue<V> get(String cacheName, K cacheKey) {
        ConcurrentMap<K, ExpiringValue<V>> cache = getCache(cacheName);
        ExpiringValue<V> expiringValue = cache.get(cacheKey);

        if (expiringValue != null && expiringValue.isExpired()) {
            expiringValue = null;
        }

        return expiringValue;
    }

    /**
     * Caches the given key and value in the cache with the given name.
     *
     * @param cacheName     The name of the cache to use.
     * @param cacheKey      The key to be associated with the given value.
     * @param cacheValue    The value to be associated with the given key.
     * @param expiry        How long from now before the value expires.
     * @param onlyIfAbsent  If true, only associates the key with the given value if the key does not already exist.
     * @return              The value that is associated with the key.
     */
    public ExpiringValue<V> put(String cacheName, K cacheKey, V cacheValue, Duration expiry, boolean onlyIfAbsent) {
        return put(cacheName, cacheKey, cacheValue, expiry == null ? null : DateTimeHelper.later(expiry), onlyIfAbsent);
    }

    /**
     * Caches the given key and value in the cache with the given name.
     *
     * @param cacheName     The name of the cache to use.
     * @param cacheKey      The key to be associated with the given value.
     * @param cacheValue    The value to be associated with the given key.
     * @param expiry        When the value expires.
     * @param onlyIfAbsent  If true, only associates the key with the given value if the key does not already exist.
     * @return              The value that is associated with the key.
     */
    public ExpiringValue<V> put(String cacheName, K cacheKey, V cacheValue, Calendar expiry, boolean onlyIfAbsent) {
        return put(cacheName, cacheKey, new ExpiringValue<V>(cacheValue, expiry), onlyIfAbsent);
    }

    /**
     * Caches the given key and value in the cache with the given name.
     *
     * @param cacheName     The name of the cache to use.
     * @param cacheKey      The key to be associated with the given value.
     * @param cacheValue    The value to be associated with the given key.
     * @param onlyIfAbsent  If true, only associates the key with the given value if the key does not already exist.
     * @return              The value that is associated with the key.
     */
    private ExpiringValue<V> put(String cacheName, K cacheKey, ExpiringValue<V> cacheValue, boolean onlyIfAbsent) {
        ConcurrentMap<K, ExpiringValue<V>> cache = getCache(cacheName);

        if (onlyIfAbsent) {
            ExpiringValue<V> oldValue = cache.putIfAbsent(cacheKey, cacheValue);
            if (oldValue != null) {
                if (oldValue.isExpired()) {
                    cache.replace(cacheKey, oldValue, cacheValue);
                } else {
                    cacheValue = oldValue;
                }
            }
        } else {
            cache.put(cacheKey, cacheValue);
        }

        return cacheValue;
    }

    /**
     * Removes the given key from the cache with the given name.
     *
     * @param cacheName     The name of the cache to use.
     * @param cacheKey      The key to be removed.
     * @param cacheValue    If specified, only remove the key if it is associated with the given value.
     * @return              The value the key was associated with, or null if the key was not removed.
     */
    public V remove(String cacheName, K cacheKey, V cacheValue) {
        ConcurrentMap<K, ExpiringValue<V>> cache = getCache(cacheName);

        if (cacheValue == null) {
            ExpiringValue<V> expiringValue = cache.remove(cacheKey);
            if (expiringValue != null && !expiringValue.isExpired()) {
                cacheValue = expiringValue.getValue();
            }
        } else {
            boolean removed = cache.remove(cacheKey, new ExpiringValue<V>(cacheValue));
            if (!removed) {
                cacheValue = null;
            }
        }

        return cacheValue;
    }

    /**
     * Replaces the value associated with the given key in the cache with the given name.
     *
     * @param cacheName The name of the cache to use.
     * @param cacheKey  The key whose associated value is to be replaced.
     * @param oldValue  Optional, if specified only replace if the key is currently associated with this value.
     * @param newValue  The new value which will replace the existing value.
     * @param expiry    How long from now until the new value expires.
     * @return          True if the key was already associated with a value and that value was replaced.
     */
    public boolean replace(String cacheName, K cacheKey, V oldValue, V newValue, Duration expiry) {
        return replace(cacheName, cacheKey, oldValue, newValue, expiry == null ? null : DateTimeHelper.later(expiry));
    }

    /**
     * Replaces the value associated with the given key in the cache with the given name.
     *
     * @param cacheName The name of the cache to use.
     * @param cacheKey  The key whose associated value is to be replaced.
     * @param oldValue  Optional, if specified only replace if the key is currently associated with this value.
     * @param newValue  The new value which will replace the existing value.
     * @param expiry    When the new value will expire.
     * @return          True if the key was already associated with a value and that value was replaced.
     */
    public boolean replace(String cacheName, K cacheKey, V oldValue, V newValue, Calendar expiry) {
        ConcurrentMap<K, ExpiringValue<V>> cache = getCache(cacheName);

        ExpiringValue<V> newExpiringValue = new ExpiringValue<V>(newValue, expiry);

        boolean replaced = false;
        if (oldValue == null) {
            ExpiringValue<V> oldExpiringValue = cache.replace(cacheKey, newExpiringValue);
            if (oldExpiringValue != null) {
                if (oldExpiringValue.isExpired()) {
                    // if old value was expired, replace should be rolled back
                    cache.remove(cacheKey, newExpiringValue);
                } else {
                    replaced = true;
                }
            }
        } else {
            replaced = cache.replace(cacheKey, new ExpiringValue<V>(oldValue), newExpiringValue);
        }

        return replaced;
    }

    /**
     * Removes all expired values from all caches.
     */
    private void removeExpired() {
        for (ConcurrentMap<K, ExpiringValue<V>> cache : caches.values()) {
            for (Map.Entry<K, ExpiringValue<V>> entry : cache.entrySet()) {
                K key = entry.getKey();
                ExpiringValue<V> value = entry.getValue();
                if (value != null && value.isExpired()) {
                    cache.remove(key, value);
                }
            }
        }
    }

    /**
     * Returns the cache with the given name.
     *
     * @param cacheName The name of the cache to be returned.
     * @return          The cache with the given name.
     */
    private ConcurrentMap<K, ExpiringValue<V>> getCache(String cacheName) {
        long cacheAccessCount = cacheAccessCounter.incrementAndGet();
        if (cacheAccessCount % REMOVE_EXPIRED_ENTRIES_AFTER_CACHE_ACCESS_COUNT == 0) {
            removeExpired();
        }

        ConcurrentMap<K, ExpiringValue<V>> cache = null;

        if (caches.containsKey(cacheName)) {
            cache = caches.get(cacheName);
        }

        if (cache == null) {
            cache = new ConcurrentHashMap<K, ExpiringValue<V>>();
            ConcurrentMap<K, ExpiringValue<V>> existingCache = caches.putIfAbsent(cacheName, cache);
            if (existingCache != null) cache = existingCache;
        }

        return cache;
    }

    /**
     * A value wrapper that supports time-based expiration.
     *
     * @param <V>   The class of the wrapped value.
     */
    public static class ExpiringValue<V> {
        /**
         * When the value expires.
         */
        Calendar expiry;
        /**
         * The wrapped value.
         */
        V value;

        /**
         * Create a new ExpirableValue that never expires.
         *
         * @param value     The wrapped value.
         */
        public ExpiringValue(V value) {
            this(value, null);
        }

        /**
         * Create a new ExpirableValue that expires at the given time.
         *
         * @param value     The wrapped value.
         * @param expiry    When the value expires, or null if it never expires.
         */
        public ExpiringValue(V value, Calendar expiry) {
            this.value = value;
            this.expiry = expiry;
        }

        /**
         * Returns the wrapped value.
         *
         * @return the wrapped value.
         */
        public V getValue() {
            return value;
        }

        /**
         * Returns when this value expires.
         *
         * @return when this value expires.
         */
        public Calendar getExpiry() {
            return expiry;
        }

        /**
         * Returns true if this value is expired.
         *
         * @return true if this value is expired.
         */
        public boolean isExpired() {
            return expiry != null && DateTimeHelper.compare(expiry, DateTimeHelper.now()) <= 0;
        }

        /**
         * Returns true if this object's wrapped value equals the given other object's wrapped value.
         *
         * @param other The other object to compare equality to.
         * @return      True if this object's wrapped value equals the given other object's wrapped value.
         */
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;

            boolean result = false;
            if (other instanceof ExpiringValue) {
                result = value.equals(((ExpiringValue)other).getValue());
            }

            return result;
        }
    }
}
