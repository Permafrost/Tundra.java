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

package permafrost.tundra.math;

import java.math.BigDecimal;

/**
 * A collection of convenience methods for working with BigDecimal precision.
 */
public final class PrecisionHelper {
    /**
     * The default decimal precision used by the methods in this class.
     */
    public static int DEFAULT_PRECISION = 0;

    /**
     * Disallow instantiation of this class.
     */
    private PrecisionHelper() {}

    /**
     * Returns the maximum precision used by the given list of decimals.
     *
     * @param decimals  A list of decimals to calculate the maximum precision of.
     * @return          The maximum precision used by the given list of decimals.
     */
    public static int maximum(BigDecimal... decimals) {
        int precision = DEFAULT_PRECISION;
        if (decimals != null) {
            for (BigDecimal decimal : decimals) {
                if (decimal != null && decimal.scale() > precision) precision = decimal.scale();
            }
        }
        return precision;
    }

    /**
     * Returns the specified precision unless it is null, in which case the maximum precision from the list of decimals
     * is returned.
     *
     * @param precision A user specified precision. A value less than zero is considered to represent null.
     * @param decimals  If precision not specified, the maximum precision from this list of decimals is returned.
     * @return          Either the given precision if greater than or equal to zero, or the maximum precision of the
     *                  given list of decimals.
     */
    public static int normalize(int precision, BigDecimal... decimals) {
        if (precision < 0) {
            precision = maximum(decimals);
        }
        return precision;
    }
}
