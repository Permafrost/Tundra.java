/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Lachlan Dowding
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

package permafrost.tundra.data.transform.bool;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.lang.BooleanHelper;

/**
 * Formats a boolean String using the provided input and output values that represent true and false.
 */
public class Formatter extends Transformer<String, String> {
    /**
     * The input and output values used to represent true and false values.
     */
    protected String inTrueValue, inFalseValue, outTrueValue, outFalseValue;

    /**
     * Creates a new Formatter object which formats boolean input strings to boolean output strings "true" or "false".
     *
     * @param inTrueValue   The string value used to represent a true value in input values to be formatted.
     * @param inFalseValue  The string value used to represent a false value in input values to be formatted.
     */
    public Formatter(String inTrueValue, String inFalseValue) {
        this(inTrueValue, inFalseValue, null, null);
    }

    /**
     * Creates a new Formatter object which formats boolean input strings to boolean output strings.
     *
     * @param inTrueValue   The string value used to represent a true value in input values to be formatted.
     * @param inFalseValue  The string value used to represent a false value in input values to be formatted.
     * @param outTrueValue  The string value used to represent a true value in the formatted values.
     * @param outFalseValue The string value used to represent a false value in the formatted values.
     */
    public Formatter(String inTrueValue, String inFalseValue, String outTrueValue, String outFalseValue) {
        super(String.class, String.class, TransformerMode.VALUES, true, true, true, true);
        this.inTrueValue = inTrueValue;
        this.inFalseValue = inFalseValue;
        this.outTrueValue = outTrueValue;
        this.outFalseValue = outFalseValue;
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
        return BooleanHelper.format(key, inTrueValue, inFalseValue, outTrueValue, outFalseValue);
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
        return BooleanHelper.format(value, inTrueValue, inFalseValue, outTrueValue, outFalseValue);
    }
}
