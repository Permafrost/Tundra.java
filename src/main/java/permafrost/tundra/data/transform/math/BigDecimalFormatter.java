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

package permafrost.tundra.data.transform.math;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.math.BigDecimalHelper;

/**
 * Reformats decimal strings in IData documents and IData[] document lists.
 */
public class BigDecimalFormatter extends Transformer<String, String> {
    protected String[] inPatterns;
    protected String outPattern;

    /**
     * Creates a new BigDecimalFormatter object.
     *
     * @param inPattern     The pattern the input strings adhere to.
     * @param outPattern    The pattern the input strings are reformatted to.
     */
    public BigDecimalFormatter(String inPattern, String outPattern) {
        this(inPattern, outPattern, true);
    }

    /**
     * Creates a new BigDecimalFormatter object.
     *
     * @param inPattern     The pattern the input strings adhere to.
     * @param outPattern    The pattern the input strings are reformatted to.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public BigDecimalFormatter(String inPattern, String outPattern, boolean recurse) {
        this(ArrayHelper.arrayify(inPattern), outPattern, recurse);
    }

    /**
     * Creates a new BigDecimalFormatter object.
     *
     * @param inPatterns    A list of patterns one of which the input strings adhere to.
     * @param outPattern    The pattern the input strings are reformatted to.
     */
    public BigDecimalFormatter(String[] inPatterns, String outPattern) {
        this(inPatterns, outPattern, true);
    }

    /**
     * Creates a new BigDecimalFormatter object.
     *
     * @param inPatterns    A list of patterns one of which the input strings adhere to.
     * @param outPattern    The pattern the input strings are reformatted to.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public BigDecimalFormatter(String[] inPatterns, String outPattern, boolean recurse) {
        super(String.class, String.class, TransformerMode.VALUES, recurse, true, true, true);
        this.inPatterns = inPatterns;
        this.outPattern = outPattern;
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
        return BigDecimalHelper.format(value, inPatterns, outPattern);
    }
}
