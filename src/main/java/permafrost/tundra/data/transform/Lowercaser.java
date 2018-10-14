/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

import permafrost.tundra.lang.LocaleHelper;
import java.util.Locale;

/**
 * Converts String elements to lowercase in an IData document or IData[] document list.
 */
public class Lowercaser extends Transformer<String, String> {
    /**
     * The locale used for the lowercase rules.
     */
    protected Locale locale;
    
    /**
     * Creates a new Lowercaser object.
     *
     * @param locale        The locale to use for lowercasing rules.
     * @param mode          The transformer mode to use.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     * @param includeNulls  Whether null values should be included in transformed IData documents and IData[] document
     *                      lists.
     */
    public Lowercaser(Locale locale, TransformerMode mode, boolean recurse, boolean includeNulls, boolean includeEmptyDocuments, boolean includeEmptyArrays) {
        super(String.class, String.class, mode, recurse, includeNulls, includeEmptyDocuments, includeEmptyArrays);
        this.locale = LocaleHelper.normalize(locale);
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
        return key.toLowerCase(locale);
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
        return value.toLowerCase(locale);
    }
}
