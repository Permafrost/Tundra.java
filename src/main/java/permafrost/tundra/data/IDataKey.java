/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

import permafrost.tundra.lang.ArrayHelper;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience class for fully qualified IData keys.
 */
public class IDataKey extends ArrayDeque<IDataKey.Part> {
    /**
     * The string which separates individual key parts in a fully-qualified key string.
     */
    public static final String SEPARATOR = "/";

    /**
     * A regular expression pattern which matches key strings array and item indexes.
     */
    public static final Pattern INDEX_PATTERN = Pattern.compile("(\\[(-?\\d+?)\\]|\\((\\d+?)\\))$");

    /**
     * Creates a new IDataKey object given a key string such as 'a/b[0]/c'.
     *
     * @param key A fully-qualified key string such as 'a/b[0]/c'.
     */
    public IDataKey(String key) {
        this(key, false);
    }

    /**
     * Creates a new IDataKey object given a key string such as 'a/b[0]/c'.
     *
     * @param key     A fully-qualified key string such as 'a/b[0]/c'.
     * @param literal If true, the key will be treated as a literal key, rather than potentially as a
     *                fully-qualified key.
     */
    public IDataKey(String key, boolean literal) {
        if (key == null) throw new NullPointerException("key must not be null");

        for (String part : literal ? new String[] { key } : key.split(SEPARATOR)) {
            add(new Part(part, literal));
        }
    }

    /**
     * Creates a new IDataKey from the given parts.
     *
     * @param parts A queue of key parts.
     */
    private IDataKey(Queue<Part> parts) {
        super(parts);
    }

    /**
     * Returns a new IDataKey object for the given key string.
     *
     * @param key A fully-qualified key string such as 'a/b[0]/c'.
     * @return    A new IDataKey object for the given key string.
     */
    public static IDataKey of(String key) {
        if (key == null) return null;
        return new IDataKey(key);
    }

    /**
     * Returns a new IDataKey object for the given key string.
     *
     * @param key     A fully-qualified key string such as 'a/b[0]/c'.
     * @param literal If true, the key will be treated as a literal key, rather than potentially as a
     *                fully-qualified key.
     * @return        A new IDataKey object for the given key string.
     */
    public static IDataKey of(String key, boolean literal) {
        if (key == null) return null;
        return new IDataKey(key, literal);
    }

    /**
     * Returns a clone of this object.
     *
     * @return A clone of this object.
     */
    public IDataKey clone() {
        return new IDataKey(this);
    }

    /**
     * Returns true if the given key string starts with "/", indicating it is an absolute path.
     *
     * @param key     A key string.
     * @return        True if the given key is accessing the root scope.
     */
    public static boolean isAbsolute(String key) {
        return isAbsolute(key, false);
    }

    /**
     * Returns true if the given key string starts with "/", indicating it is an absolute path.
     *
     * @param key     A key string.
     * @param literal If true, the key will be treated as a literal key, rather than potentially as a
     *                fully-qualified key.
     * @return        True if the given key is an absolute path.
     */
    public static boolean isAbsolute(String key, boolean literal) {
        return !literal && key != null && key.startsWith(SEPARATOR);
    }


    /**
     * Returns true if the given IData key is considered fully-qualified (because it contains either an array index,
     * key index, or path separated components).
     *
     * @param key An IData key string.
     * @return True if the given key is considered fully-qualified.
     */
    public static boolean isFullyQualified(String key) {
        return isFullyQualified(key, false);
    }

    /**
     * Returns true if the given IData key is considered fully-qualified (because it contains either an array index,
     * key index, or path separated components).
     *
     * @param key     An IData key string.
     * @param literal If true, the key will be treated as a literal key, rather than potentially as a
     *                fully-qualified key.
     * @return True if the given key is considered fully-qualified.
     */
    public static boolean isFullyQualified(String key, boolean literal) {
        return !literal && key != null && (key.contains(SEPARATOR) || INDEX_PATTERN.matcher(key).find());
    }

    /**
     * Returns a string representation of this key.
     *
     * @return A string representation of this key.
     */
    @Override
    public String toString() {
        return ArrayHelper.join(this.toArray(new Part[0]), "/");
    }

    /**
     * Represents an individual key part of a fully-qualified key.
     */
    public static class Part {
        protected boolean hasArrayIndex = false, hasKeyIndex = false;
        protected int index = 0;
        protected String key = null;

        /**
         * Constructs a new key object given a key string.
         *
         * @param key An IData key as a string.
         */
        public Part(String key) {
            this(key, false);
        }

        /**
         * Constructs a new key object given a key string.
         *
         * @param key     An IData key as a string.
         * @param literal If true, the key is treated literally rather than as a fully-qualified key that could contain
         *                array or key indexing.
         */
        public Part(String key, boolean literal) {
            if (key == null) throw new NullPointerException("key must not be null");

            if (literal) {
                this.key = key;
            } else {
                StringBuffer buffer = new StringBuffer();

                Matcher matcher = INDEX_PATTERN.matcher(key);
                while (matcher.find()) {
                    String arrayIndexString = matcher.group(2);
                    String keyIndexString = matcher.group(3);

                    if (arrayIndexString != null) {
                        hasArrayIndex = true;
                        index = Integer.parseInt(arrayIndexString);
                    } else {
                        hasKeyIndex = true;
                        index = Integer.parseInt(keyIndexString);
                    }
                    matcher.appendReplacement(buffer, "");
                }
                matcher.appendTail(buffer);

                this.key = buffer.toString();
            }
        }

        /**
         * Returns true if this key includes an array index.
         *
         * @return true if this key includes an array index.
         */
        public boolean hasArrayIndex() {
            return hasArrayIndex;
        }

        /**
         * Returns true if this key includes an key index.
         *
         * @return true if this key includes an key index.
         */
        public boolean hasKeyIndex() {
            return hasKeyIndex;
        }

        /**
         * Returns this key's index value.
         *
         * @return This key's index value.
         */
        public int getIndex() {
            return index;
        }

        /**
         * Returns the key-only component of this Key (with no array or key indexing).
         *
         * @return The key-only component of this Key (with no array or key indexing).
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns a string representation of this key.
         *
         * @return A string representation of this key.
         */
        @Override
        public String toString() {
            String output;
            if (hasKeyIndex()) {
                output = key + "(" + index + ")";
            } else if (hasArrayIndex()) {
                output = key + "[" + index + "]";
            } else {
                output = key;
            }
            return output;
        }
    }
}
