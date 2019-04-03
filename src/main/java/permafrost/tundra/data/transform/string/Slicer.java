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
 * Slices a section from String elements in an IData document or IData[] document list.
 */
public class Slicer extends Transformer<String, String> {
    /**
     * The zero-based starting index of the slice, and the length in characters of the slice.
     */
    protected int index, length;

    /**
     * Creates a new Slicer object.
     *
     * @param index   The zero-based starting index of the slice.
     * @param length  The length in characters of the slice.
     */
    public Slicer(int index, int length) {
        this(null, index, length);
    }

    /**
     * Creates a new Slicer object.
     *
     * @param mode    The transformer mode to use.
     * @param index   The zero-based starting index of the slice.
     * @param length  The length in characters of the slice.
     */
    public Slicer(TransformerMode mode, int index, int length) {
        this(mode, index, length, true);
    }

    /**
     * Creates a new Slicer object.
     *
     * @param mode    The transformer mode to use.
     * @param index   The zero-based starting index of the slice.
     * @param length  The length in characters of the slice.
     * @param recurse Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Slicer(TransformerMode mode, int index, int length, boolean recurse) {
        super(String.class, String.class, mode, recurse, true, true, true);
        this.index = index;
        this.length = length;
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
        return StringHelper.slice(key, index, length);
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
        return StringHelper.slice(value, index, length);
    }
}
