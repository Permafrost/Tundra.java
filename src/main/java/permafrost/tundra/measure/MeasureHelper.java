/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

package permafrost.tundra.measure;

import permafrost.tundra.math.BigDecimalHelper;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.measure.DecimalMeasure;
import javax.measure.unit.Unit;

/**
 * A collection of convenience methods for working with measurements.
 */
public final class MeasureHelper {
    /**
     * Disallow instantiation of this class.
     */
    private MeasureHelper() {}

    /**
     * Returns a DecimalMeasure object that represent the given value and unit of measure.
     *
     * @param value  The measurement value.
     * @param unit   The unit of measure for the given value.
     * @return       A DecimalMeasure representing the given value and unit of measure.
     */
    public static DecimalMeasure parse(BigDecimal value, Unit unit) {
        if (value == null || unit == null) return null;
        return DecimalMeasure.valueOf(value, unit);
    }

    /**
     * Returns the value of the given measurement in the given unit of measure.
     *
     * @param measure   The measurement to return the value of.
     * @param unit      The unit of measure to return the value in.
     * @param context   The math context to use.
     * @return          The value of the given measurement in the given unit of measure.
     */
    public static BigDecimal emit(DecimalMeasure measure, Unit unit, MathContext context) {
        return BigDecimalHelper.normalize(measure.to(unit, context).getValue());
    }

    /**
     * Converts the given value from one unit of measure to another.
     *
     * @param value     The value to be converted.
     * @param from      The unit of measure to be converted from.
     * @param to        The unit of measure to be converted to.
     * @param context   The math context to use.
     * @return          The value converted from one unit of measure to another.
     */
    public static BigDecimal convert(BigDecimal value, Unit from, Unit to, MathContext context) {
        return emit(parse(value, from), to, context);
    }
}
