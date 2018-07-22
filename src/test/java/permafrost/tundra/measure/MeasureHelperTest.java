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

import static org.junit.Assert.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MeasureHelperTest {
    @Test
    public void testConvertMilesToKilometres() throws Exception {
        assertEquals(new BigDecimal("48.28032"), MeasureHelper.convert(new BigDecimal("30"), UnitHelper.parse("mi"), UnitHelper.parse("km"), new MathContext(0, RoundingMode.HALF_UP)));
    }

    @Test
    public void testConvertKilogramsToPounds() throws Exception {
        assertEquals(new BigDecimal("11.02311310924387903614869006725135"), MeasureHelper.convert(new BigDecimal("5"), UnitHelper.parse("kg"), UnitHelper.parse("lb"), MathContext.DECIMAL128));
    }
}