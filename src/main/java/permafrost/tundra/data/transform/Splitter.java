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

package permafrost.tundra.data.transform;

import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.util.regex.PatternHelper;
import java.util.regex.Pattern;

/**
 * Splits strings using a given regular expression pattern.
 */
public class Splitter extends Transformer<String, String[]> {
    /**
     * The regular expression pattern used for splitting strings.
     */
    private final Pattern pattern;

    /**
     * Creates a new Splitter object.
     *
     * @param pattern   The literal or regular expression pattern to split around.
     * @param literal   Whether the pattern is a literal pattern or a regular expression.
     */
    public Splitter(String pattern, boolean literal) {
        super(String.class, String[].class, TransformerMode.VALUES, true, true, true, true);
        this.pattern = PatternHelper.compile(pattern, literal);
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    @Override
    protected String[] transformValue(String key, String value) {
        return StringHelper.split(value, pattern);
    }
}
