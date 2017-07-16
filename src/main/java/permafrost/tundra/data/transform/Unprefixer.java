/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

package permafrost.tundra.data.transform;

/**
 * Removes a given prefix from string elements in an IData document or IData[] document list.
 */
public class Unprefixer extends Transformer<String, String> {
    /**
     * The prefix to be removed from string elements.
     */
    protected String prefix;

    /**
     * Creates a new Unprefixer object.
     *
     * @param prefix        The prefix to be removed from string elements.
     * @param mode          The transformer mode to use.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Unprefixer(String prefix, TransformerMode mode, boolean recurse) {
        super(String.class, String.class, mode, recurse, true, true, true);
        this.prefix = prefix;
    }

    /**
     * Transforms the given key.
     *
     * @param key   The key to be transformed.
     * @param value The value associated with the key being transformed.
     * @return      The transformed key.
     */
    protected String transformKey(String key, Object value) {
        return removePrefix(key);
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    protected String transformValue(String key, String value) {
        return removePrefix(value);
    }

    /**
     * Removes the prefix to the given string.
     *
     * @param string    The string to remove the prefix from.
     * @return          The string with the prefix removed.
     */
    protected String removePrefix(String string) {
        return prefix != null && string.startsWith(prefix) ? string.substring(prefix.length()) : string;
    }
}
