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
import permafrost.tundra.lang.StringHelper;

/**
 * Truncates strings to the given length.
 */
public class Truncator extends Transformer<String, String> {
    /**
     * The desired length to truncate strings to.
     */
    protected int length;
    /**
     * Whether to prefix/suffix truncated strings with an ellipsis character.
     */
    protected boolean ellipsis;

    /**
     * Creates a new Truncator object.
     *
     * @param length    The length to truncate the string to.
     * @param ellipsis  If true, the returned string is suffixed with an ellipsis character when truncated.
     */
    public Truncator(int length, boolean ellipsis) {
        this(null, length, ellipsis, true);
    }

    /**
     * Creates a new Truncator object.
     *
     * @param mode      The transformer mode to use.
     * @param length    The length to truncate the string to.
     * @param ellipsis  If true, the returned string is suffixed with an ellipsis character when truncated.
     * @param recurse   Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Truncator(TransformerMode mode, int length, boolean ellipsis, boolean recurse) {
        super(String.class, String.class, mode, recurse, true, true, true);
        this.length = length;
        this.ellipsis = ellipsis;
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
        return StringHelper.truncate(key, length, ellipsis);
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
        return StringHelper.truncate(value, length, ellipsis);
    }
}
