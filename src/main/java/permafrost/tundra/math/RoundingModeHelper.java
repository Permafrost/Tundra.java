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

import java.math.RoundingMode;

/**
 * A collection of convenience methods for working with RoundingMode objects.
 */
public final class RoundingModeHelper {
    /**
     * The default rounding mode used by the methods in this class.
     */
    public static RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Disallow instantiation of this class.
     */
    private RoundingModeHelper() {}

    /**
     * Returns the given rounding mode if specified.
     *
     * @param roundingMode  An optional rounding algorithm name.
     * @return              The rounding algorithm to be used by the caller.
     */
    public static RoundingMode parse(String roundingMode) {
        return roundingMode == null ? null : RoundingMode.valueOf(roundingMode);
    }

    /**
     * Returns the given rounding mode if specified, or the default rounding mode.
     *
     * @param roundingMode  An optional rounding algorithm name.
     * @return              The rounding algorithm to be used by the caller.
     */
    public static RoundingMode normalize(RoundingMode roundingMode) {
        return roundingMode == null ? DEFAULT_ROUNDING_MODE : roundingMode;
    }
}
