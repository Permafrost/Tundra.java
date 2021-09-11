/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
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
import permafrost.tundra.util.regex.PatternHelper;
import java.util.regex.Pattern;

/**
 * Removes either the first or all occurrences of the given regular expression.
 */
public class Remover extends Transformer<String, String> {
    /**
     * The regular expression pattern to be removed.
     */
    protected Pattern pattern;
    /**
     * Whether only the first or all matches should be removed.
     */
    protected boolean firstOnly;

    /**
     * Creates a new Remover object.
     *
     * @param mode          The transformer mode to use.
     * @param pattern       The regular expression pattern.
     * @param firstOnly     If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Remover(TransformerMode mode, Pattern pattern, boolean firstOnly, boolean recurse) {
        super(String.class, String.class, mode, recurse, true, true, true);
        this.pattern = pattern;
        this.firstOnly = firstOnly;
    }

    /**
     * Creates a new Remover object.
     *
     * @param mode          The transformer mode to use.
     * @param pattern       The pattern to remove.
     * @param literal       Whether the pattern is literal or a regular expression.
     * @param firstOnly     If true, only the first occurrence is replaced, otherwise all occurrences are replaced.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Remover(TransformerMode mode, String pattern, boolean literal, boolean firstOnly, boolean recurse) {
        this(mode, PatternHelper.compile(pattern, literal), firstOnly, recurse);
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
        return StringHelper.remove(key, pattern, firstOnly);
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
        return StringHelper.remove(value, pattern, firstOnly);
    }
}
