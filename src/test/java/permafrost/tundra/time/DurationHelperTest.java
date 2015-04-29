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

import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import static org.junit.Assert.*;

public class DurationHelperTest {

    @Test
    public void testParse() throws Exception {
        Duration duration = DurationHelper.parse("P1Y2M3DT4H5M6.007S");
        assertEquals(1, duration.getYears());
        assertEquals(2, duration.getMonths());
        assertEquals(3, duration.getDays());
        assertEquals(4, duration.getHours());
        assertEquals(5, duration.getMinutes());
        assertEquals(6, duration.getSeconds());
        assertEquals(1, duration.getSign());
    }

    @Test
    public void testEmit() throws Exception {
        Duration duration = DatatypeFactory.newInstance().newDuration(60000);
        assertEquals("1", DurationHelper.emit(duration, DurationPattern.MINUTES));
        assertEquals("60", DurationHelper.emit(duration, DurationPattern.SECONDS));
        assertEquals("60000", DurationHelper.emit(duration, DurationPattern.MILLISECONDS));
    }
}