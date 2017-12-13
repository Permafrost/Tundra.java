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

package permafrost.tundra.time;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.math.BigDecimal;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class DurationHelperTest {
    @Test
    public void testParseXML() throws Exception {
        Duration duration = DurationHelper.parse("P1Y2M3DT4H5M6.123456789S");
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
        assertEquals(new BigDecimal("6.123456789"), duration.getField(DatatypeConstants.SECONDS));
        assertEquals(1, duration.getSign());
    }

    @Test
    public void testParseXMLNegative() throws Exception {
        Duration duration = DurationHelper.parse("-P1Y2M3DT4H5M6.123456789S");
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
        assertEquals(new BigDecimal("6.123456789"), duration.getField(DatatypeConstants.SECONDS));
        assertEquals(-1, duration.getSign());
    }

    @Test
    public void testParseXMLZero() throws Exception {
        Duration duration = DurationHelper.parse("PT0S");
        assertEquals(0, duration.getYears());
        assertEquals(0, duration.getMonths());
        assertEquals(0, duration.getDays());
        assertEquals(0, duration.getHours());
        assertEquals(0, duration.getMinutes());
        assertEquals(0, duration.getSeconds());
        assertEquals(new BigDecimal("0"), duration.getField(DatatypeConstants.SECONDS));
        assertEquals(0, duration.getSign());
    }

    @Test
    public void testEmit() throws Exception {
        Duration duration = DatatypeFactory.newInstance().newDuration(60000);
        assertEquals("1", DurationHelper.emit(duration, DurationPattern.MINUTES));
        assertEquals("60", DurationHelper.emit(duration, DurationPattern.SECONDS));
        assertEquals("60000", DurationHelper.emit(duration, DurationPattern.MILLISECONDS));
        assertEquals("60000000000", DurationHelper.emit(duration, DurationPattern.NANOSECONDS));
    }

    @Test
    public void testEmitNegative() throws Exception {
        Duration duration = DatatypeFactory.newInstance().newDuration(-60000);
        assertEquals("-1", DurationHelper.emit(duration, DurationPattern.MINUTES));
        assertEquals("-60", DurationHelper.emit(duration, DurationPattern.SECONDS));
        assertEquals("-60000", DurationHelper.emit(duration, DurationPattern.MILLISECONDS));
        assertEquals("-60000000000", DurationHelper.emit(duration, DurationPattern.NANOSECONDS));
    }

    @Test
    public void testEmitZero() throws Exception {
        Duration duration = DatatypeFactory.newInstance().newDuration(0);
        assertEquals("0", DurationHelper.emit(duration, DurationPattern.MINUTES));
        assertEquals("0", DurationHelper.emit(duration, DurationPattern.SECONDS));
        assertEquals("0", DurationHelper.emit(duration, DurationPattern.MILLISECONDS));
        assertEquals("0", DurationHelper.emit(duration, DurationPattern.NANOSECONDS));
    }

    @Test
    public void testParseFractionalMilliseconds() throws Exception {
        BigDecimal milliseconds = new BigDecimal("987.654321");
        Duration duration = DurationHelper.parse(milliseconds.toString(), DurationPattern.MILLISECONDS);
        assertEquals(milliseconds.divide(new BigDecimal("1000")), duration.getField(DatatypeConstants.SECONDS));
    }

    @Test
    public void testParseFractionalSeconds() throws Exception {
        BigDecimal seconds = new BigDecimal("1.123456789");
        Duration duration = DurationHelper.parse(seconds);
        assertEquals(seconds, duration.getField(DatatypeConstants.SECONDS));
    }

    @Test
    public void testEmitFractionalMilliseconds() throws Exception {
        BigDecimal milliseconds = new BigDecimal("987.654321");
        Duration duration = DurationHelper.parse(milliseconds.toString(), DurationPattern.MILLISECONDS);
        assertEquals("987654321", DurationHelper.emit(duration, DurationPattern.NANOSECONDS));
    }

    @Test
    public void testEmitNanoseconds() throws Exception {
        BigDecimal nanoseconds = new BigDecimal("123456789123456789123456789123456789123456789");
        Duration duration = DurationHelper.parse(nanoseconds.toString(), DurationPattern.NANOSECONDS);
        String result = DurationHelper.emit(duration, DurationPattern.NANOSECONDS);
        assertEquals("123456789123456789123456789123456789123456789", result);
    }
}