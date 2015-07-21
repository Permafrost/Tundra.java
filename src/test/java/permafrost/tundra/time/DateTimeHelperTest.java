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

import java.util.Calendar;

import static org.junit.Assert.*;

public class DateTimeHelperTest {

    @Test
    public void testConcatenateNullDate() throws Exception {
        Calendar datetime = DateTimeHelper.concatenate(null, Calendar.getInstance());
        assertNull(datetime);
    }

    @Test
    public void testConcatenateNullTime() throws Exception {
        Calendar datetime = DateTimeHelper.concatenate(Calendar.getInstance(), null);
        assertNull(datetime);
    }

    @Test
    public void testConcatenateNullDateAndTime() throws Exception {
        Calendar datetime = DateTimeHelper.concatenate(null, null);
        assertNull(datetime);
    }

    @Test
    public void testConcatenateDateAndTime() throws Exception {
        String dateValue = "2015-07-01";
        String datePattern = "yyyy-MM-dd";
        String timeValue = "13:14:15.678";
        String timePattern = "HH:mm:ss.SSS";

        Calendar datetime = DateTimeHelper.concatenate(DateTimeHelper.parse(dateValue, datePattern), DateTimeHelper.parse(timeValue, timePattern));

        assertEquals(dateValue, DateTimeHelper.emit(datetime, datePattern));
        assertEquals(timeValue, DateTimeHelper.emit(datetime, timePattern));
    }
}