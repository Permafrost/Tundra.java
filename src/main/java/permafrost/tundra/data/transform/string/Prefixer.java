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

package permafrost.tundra.data.transform.string;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;

/**
 * Adds a given prefix to string elements in an IData document or IData[] document list.
 */
public class Prefixer extends Transformer<String, String> {
    /**
     * The prefix to be added to string elements.
     */
    protected String prefix;
    /**
     * Whether to add the prefix even if a string already starts with it.
     */
    protected boolean force;

    /**
     * Creates a new Prefixer object.
     *
     * @param mode          The transformer mode to use.
     * @param prefix        The prefix to be added to string elements.
     * @param force         Whether to add the prefix even when a string is already prefixed with it.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Prefixer(TransformerMode mode, String prefix, boolean force, boolean recurse) {
        super(String.class, String.class, mode, recurse, true, true, true);
        this.prefix = prefix;
        this.force = force;
    }

    /**
     * Transforms the given key.
     *
     * @param key   The key to be transformed.
     * @param value The value associated with the key being transformed.
     * @return      The transformed key.
     */
    @Override
    protected String transformKey(String key, Object value) {
        return addPrefix(key);
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    @Override
    protected String transformValue(String key, String value) {
        return addPrefix(value);
    }

    /**
     * Adds the prefix to the given string.
     *
     * @param string    The string to add the prefix to.
     * @return          The string prefixed with the prefix.
     */
    protected String addPrefix(String string) {
        return prefix != null && (force || !string.startsWith(prefix)) ? prefix + string : string;
    }
}
