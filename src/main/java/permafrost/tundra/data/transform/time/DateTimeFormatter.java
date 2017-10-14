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

package permafrost.tundra.data.transform.time;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.time.DateTimeHelper;
import java.util.TimeZone;

/**
 * Reformats datetime strings in IData documents and IData[] document lists.
 */
public class DateTimeFormatter extends Transformer<String, String> {
    protected String[] inPatterns;
    protected String outPattern;
    protected TimeZone inTimeZone, outTimeZone;

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPattern     The pattern the given input datetime string adheres to.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public DateTimeFormatter(String inPattern, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone, boolean recurse) {
        this(ArrayHelper.arrayify(inPattern), inTimeZone, outPattern, outTimeZone, recurse);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone    The time zone ID identifying the time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @param outTimeZone   The time zone ID identifying the time zone the returned datetime string should be in.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public DateTimeFormatter(String[] inPatterns, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone, boolean recurse) {
        super(String.class, String.class, TransformerMode.VALUES, recurse, true, true, true);
        this.inPatterns = inPatterns;
        this.outPattern = outPattern;
        this.inTimeZone = inTimeZone;
        this.outTimeZone = outTimeZone;
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
        return DateTimeHelper.format(value, inPatterns, inTimeZone, outPattern, outTimeZone);
    }
}
