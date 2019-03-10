/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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

import permafrost.tundra.lang.StringHelper;

/**
 * Capitalizes the first character in either the first word or all words in the String elements in an IData document or
 * IData[] document list.
 */
public class Capitalizer extends Transformer<String, String> {
    /**
     * Whether only the first word should be capitalized, or all words.
     */
    protected boolean firstWordOnly;

    /**
     * Creates a new Capitalizer object.
     *
     * @param firstWordOnly Whether only the first word should be capitalized, or all words.
     */
    public Capitalizer(boolean firstWordOnly) {
        this(firstWordOnly, TransformerMode.VALUES);
    }

    /**
     * Creates a new Capitalizer object.
     *
     * @param firstWordOnly Whether only the first word should be capitalized, or all words.
     * @param mode          The transformer mode to use.
     */
    public Capitalizer(boolean firstWordOnly, TransformerMode mode) {
        this(firstWordOnly, mode, true, true, true, true);
    }

    /**
     * Creates a new Capitalizer object.
     *
     * @param firstWordOnly         Whether only the first word should be capitalized, or all words.
     * @param mode                  The transformer mode to use.
     * @param recurse               Whether to recursively transform child IData documents and IData[] document lists.
     * @param includeNulls          Whether null values should be included in transformed IData documents and IData[]
     *                              document lists.
     * @param includeEmptyDocuments Whether empty IData documents should be included in the transformation.
     * @param includeEmptyArrays    Whether empty arrays should be included in the transformation.
     */
    public Capitalizer(boolean firstWordOnly, TransformerMode mode, boolean recurse, boolean includeNulls, boolean includeEmptyDocuments, boolean includeEmptyArrays) {
        super(String.class, String.class, mode, recurse, includeNulls, includeEmptyDocuments, includeEmptyArrays);
        this.firstWordOnly = firstWordOnly;
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
        return StringHelper.capitalize(key, firstWordOnly);
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
        return StringHelper.capitalize(value, firstWordOnly);
    }
}
