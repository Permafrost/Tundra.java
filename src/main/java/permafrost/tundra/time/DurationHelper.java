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

import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.time.DurationFormatter;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.math.BigIntegerHelper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

/**
 * A collection of convenience methods for working with durations.
 */
public final class DurationHelper {
    public static final long SECONDS_PER_MINUTE = 60;
    public static final long MINUTES_PER_HOUR = 60;
    public static final long HOURS_PER_DAY = 24;
    public static final long DAYS_PER_WEEK = 7;
    public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    public static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
    public static final long SECONDS_PER_WEEK = SECONDS_PER_DAY * DAYS_PER_WEEK;
    public static final long MILLISECONDS_PER_SECOND = 1000;
    public static final long NANOSECONDS_PER_SECOND = 1000000000;
    public static final long MILLISECONDS_PER_MINUTE = SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_HOUR = MINUTES_PER_HOUR * MILLISECONDS_PER_MINUTE;
    public static final long MILLISECONDS_PER_DAY = HOURS_PER_DAY * MILLISECONDS_PER_HOUR;
    private static final BigInteger SECONDS_PER_MINUTE_BIG_INTEGER = BigInteger.valueOf(SECONDS_PER_MINUTE);
    private static final BigInteger SECONDS_PER_HOUR_BIG_INTEGER = BigInteger.valueOf(SECONDS_PER_HOUR);
    private static final BigDecimal SECONDS_PER_HOUR_BIG_DECIMAL = BigDecimal.valueOf(SECONDS_PER_HOUR);
    private static final BigInteger SECONDS_PER_DAY_BIG_INTEGER = BigInteger.valueOf(SECONDS_PER_DAY);
    private static final BigDecimal SECONDS_PER_DAY_BIG_DECIMAL = BigDecimal.valueOf(SECONDS_PER_DAY);
    private static final BigInteger SECONDS_PER_WEEK_BIG_INTEGER = BigInteger.valueOf(SECONDS_PER_WEEK);
    private static final BigDecimal SECONDS_PER_WEEK_BIG_DECIMAL = BigDecimal.valueOf(SECONDS_PER_WEEK);
    private static final BigDecimal NANOSECONDS_PER_SECOND_BIG_DECIMAL = BigDecimal.valueOf(NANOSECONDS_PER_SECOND);
    private static final BigDecimal SECONDS_PER_MINUTE_BIG_DECIMAL = BigDecimal.valueOf(SECONDS_PER_MINUTE);
    private static final BigDecimal MILLISECONDS_PER_SECOND_BIG_DECIMAL = BigDecimal.valueOf(MILLISECONDS_PER_SECOND);

    private static DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Disallow instantiation of this class.
     */
    private DurationHelper() {}

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration      The duration string to be formatted.
     * @param inPattern     The pattern the duration string adheres to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @return              The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration      The duration string to be formatted.
     * @param inPattern     The pattern the duration string adheres to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @return              The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, (Date)null);
    }

    /**
     * Formats a duration in milliseconds to the desired pattern.
     *
     * @param milliseconds  The duration to be formatted, specified as milliseconds.
     * @param pattern       The pattern the duration will be reformatted to.
     * @return              The duration reformatted according to the given pattern.
     */
    public static String format(long milliseconds, DurationPattern pattern) {
        return emit(parse(milliseconds), pattern);
    }

    /**
     * Formats a duration in fractional seconds to the desired pattern.
     *
     * @param seconds   The duration to be formatted, specified as fractional seconds.
     * @param pattern   The pattern the duration will be reformatted to.
     * @return          The duration reformatted according to the given pattern.
     */
    public static String format(double seconds, DurationPattern pattern) {
        return emit(parse(seconds), pattern);
    }

    /**
     * Formats a duration in fractional seconds to the desired pattern.
     *
     * @param seconds   The duration to be formatted, specified as fractional seconds.
     * @param precision The number of decimal places to be respected in the given fraction.
     * @param pattern   The pattern the duration will be reformatted to.
     * @return          The duration reformatted according to the given pattern.
     */
    public static String format(double seconds, int precision, DurationPattern pattern) {
        return emit(parse(seconds, precision), pattern);
    }

    /**
     * Formats a duration in fractional seconds to the desired pattern.
     *
     * @param seconds   The duration to be formatted, specified as fractional seconds.
     * @param pattern   The pattern the duration will be reformatted to.
     * @return          The duration reformatted according to the given pattern.
     */
    public static String format(BigDecimal seconds, DurationPattern pattern) {
        return emit(parse(seconds), pattern);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration   The duration string to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param datetime   An XML datetime string used as a starting instant to resolve indeterminate values (such as the
     *                   number of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime) {
        return format(duration, inPattern, outPattern, datetime, null);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime, String datetimePattern) {
        return format(duration, DurationPattern.normalize(inPattern), DurationPattern.normalize(outPattern), datetime, datetimePattern);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration          The duration string to be formatted.
     * @param inPatterns        A list of possible patterns the duration string adheres to.
     * @param outPattern        The pattern the duration will be reformatted to.
     * @param datetime          A datetime string used as a starting instant to resolve indeterminate values (such as
     *                          the number of days in a month).
     * @param datetimePattern   The pattern the given datetime adheres to.
     * @return                  The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String[] inPatterns, String outPattern, String datetime, String datetimePattern) {
        return format(duration, DurationPattern.normalize(inPatterns), DurationPattern.normalize(outPattern), datetime, datetimePattern);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, String datetime, String datetimePattern) {
        return format(duration, inPattern, outPattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration          The duration string to be formatted.
     * @param inPatterns        A list of possible patterns the duration string adheres to.
     * @param outPattern        The pattern the duration will be reformatted to.
     * @param datetime          A datetime string used as a starting instant to resolve indeterminate values (such as
     *                          the number of days in a month).
     * @param datetimePattern   The pattern the given datetime adheres to.
     * @return                  The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern[] inPatterns, DurationPattern outPattern, String datetime, String datetimePattern) {
        return format(duration, inPatterns, outPattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration   The duration string to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                   number of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, Calendar instant) {
        return format(duration, inPattern, outPattern, instant == null ? null : instant.getTime());
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration      The duration string to be formatted.
     * @param inPatterns    A list of possible patterns the duration string adheres to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @param instant       A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                      number of days in a month).
     * @return              The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern[] inPatterns, DurationPattern outPattern, Calendar instant) {
        return format(duration, inPatterns, outPattern, instant == null ? null : instant.getTime());
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration   The duration string to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Formats a duration string to the desired pattern.
     *
     * @param duration      The duration string to be formatted.
     * @param inPatterns    A list of possible patterns the duration string adheres to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @param instant       A java.util.Date used as a starting instant to resolve indeterminate values (such as the
     *                      number of days in a month).
     * @return              The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern[] inPatterns, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPatterns), outPattern, instant);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations  The duration strings to be formatted.
     * @param inPattern  The pattern the duration strings adhere to.
     * @param outPattern The pattern the durations will be reformatted to.
     * @return           The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern) {
        return format(durations, inPattern, outPattern, null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations  The duration strings to be formatted.
     * @param inPattern  The pattern the duration strings adhere to.
     * @param outPattern The pattern the durations will be reformatted to.
     * @param datetime   An XML datetime string used as a starting instant to resolve indeterminate values (such as the
     *                   number of days in a month).
     * @return           The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime) {
        return format(durations, inPattern, outPattern, datetime, null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime, String datetimePattern) {
        return format(durations, DurationPattern.normalize(inPattern), DurationPattern.normalize(outPattern), datetime, datetimePattern);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations  The duration strings to be formatted.
     * @param inPattern  The pattern the duration strings adhere to.
     * @param outPattern The pattern the durations will be reformatted to.
     * @return           The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern) {
        return format(durations, inPattern, outPattern, (Date)null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, String datetime, String datetimePattern) {
        return format(durations, inPattern, outPattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations  The duration strings to be formatted.
     * @param inPattern  The pattern the duration strings adhere to.
     * @param outPattern The pattern the durations will be reformatted to.
     * @param instant    A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                   number of days in a month).
     * @return           The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, Calendar instant) {
        return format(durations, inPattern, outPattern, instant == null ? null : instant.getTime());
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     *
     * @param durations  The duration strings to be formatted.
     * @param inPattern  The pattern the duration strings adhere to.
     * @param outPattern The pattern the durations will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(durations, inPattern), outPattern, instant);
    }

    /**
     * Formats the duration strings in the given IData document to the desired pattern.
     *
     * @param document      The IData document containing the duration strings to be formatted.
     * @param inPattern     The pattern the duration strings adhere to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @param instant       A java.util.Date used as a starting instant to resolve indeterminate values (such as the
     *                      number of days in a month).
     * @return              The duration strings reformatted according to the outPattern.
     */
    public static IData format(IData document, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return IDataHelper.transform(document, new DurationFormatter(inPattern, outPattern, instant, true));
    }

    /**
     * Formats the duration strings in the given IData document to the desired pattern.
     *
     * @param document      The IData document containing the duration strings to be formatted.
     * @param inPatterns    A list of possible patterns the duration string adheres to.
     * @param outPattern    The pattern the duration will be reformatted to.
     * @param instant       A java.util.Date used as a starting instant to resolve indeterminate values (such as the
     *                      number of days in a month).
     * @return              The duration strings reformatted according to the outPattern.
     */
    public static IData format(IData document, DurationPattern[] inPatterns, DurationPattern outPattern, Date instant) {
        return IDataHelper.transform(document, new DurationFormatter(inPatterns, outPattern, instant, true));
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(BigDecimal duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(BigDecimal duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(BigInteger duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(BigInteger duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(long duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(long duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(double duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration to the desired pattern.
     *
     * @param duration   The duration to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param instant    A java.util.Date used as a starting instant to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(double duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Returns a new Duration given a duration in milliseconds.
     *
     * @param milliseconds  The duration in milliseconds.
     * @return              A Duration object representing the duration in milliseconds.
     */
    public static Duration parse(long milliseconds) {
        return fromMilliseconds(BigDecimal.valueOf(milliseconds));
    }

    /**
     * Returns a new Duration given a duration in fractional seconds.
     *
     * @param seconds   The duration in fractional seconds.
     * @return          A Duration object representing the duration in fractional seconds.
     */
    public static Duration parse(double seconds) {
        return parse(seconds, 9);
    }

    /**
     * Returns a new Duration given a duration in fractional seconds.
     *
     * @param seconds   The duration in fractional seconds.
     * @param precision The number of decimal places to respect int the given fraction.
     * @return          A Duration object representing the duration in fractional seconds.
     */
    public static Duration parse(double seconds, int precision) {
        return parse((new BigDecimal(seconds)).setScale(precision, RoundingMode.HALF_UP));
    }

    /**
     * Returns a new Duration given a duration in fractional seconds.
     *
     * @param seconds   The duration in fractional seconds.
     * @return          A Duration object representing the duration in fractional seconds.
     */
    public static Duration parse(BigDecimal seconds) {
        return fromFractionalSeconds(seconds);
    }

    /**
     * Parses the given duration string to a Duration object.
     *
     * @param input The duration string to be parsed.
     * @return      A Duration object which represents the given duration string.
     */
    public static Duration parse(String input) {
        return parse(input, (DurationPattern)null);
    }

    /**
     * Parses the given duration string to a Duration object.
     *
     * @param input     The duration string to be parsed.
     * @param pattern   The pattern the duration string adheres to.
     * @return          A Duration object which represents the given duration string.
     */
    public static Duration parse(String input, String pattern) {
        return parse(input, DurationPattern.normalize(pattern));
    }

    /**
     * Parses the given duration string to a Duration object.
     *
     * @param input     The duration string to be parsed.
     * @param pattern   The pattern the duration string adheres to.
     * @return          A Duration object which represents the given duration string.
     */
    public static Duration parse(String input, DurationPattern pattern) {
        if (input == null) return null;

        pattern = DurationPattern.normalize(pattern);
        Duration output;

        if (pattern == DurationPattern.XML) {
            output = DATATYPE_FACTORY.newDuration(input);
        } else {
            output = parse(new BigDecimal(input), pattern);
        }

        return output;
    }

    /**
     * Parses the given duration string to a Duration object.
     *
     * @param input     The duration string to be parsed.
     * @param patterns  A list of possible patterns the duration string adheres to.
     * @return          A Duration object which represents the given duration string.
     */
    public static Duration parse(String input, DurationPattern[] patterns) {
        if (input == null) return null;
        if (patterns == null) patterns = new DurationPattern[1];

        Duration output = null;
        boolean parsed = false;

        for (DurationPattern pattern : patterns) {
            try {
                output = parse(input, pattern);
                parsed = true;
                break;
            } catch (IllegalArgumentException ex) {
                // ignore and continue on to try the next pattern
            }
        }

        if (!parsed) {
            throw new IllegalArgumentException(MessageFormat.format("Unparseable duration does not conform to any of the specified patterns [{0}]: {1}", ArrayHelper.join(patterns, ", ", input)));
        }

        return output;
    }

    /**
     * Parses the given duration string to a Duration object.
     *
     * @param input     The duration string to be parsed.
     * @param patterns  A list of possible patterns the duration string adheres to.
     * @return          A Duration object which represents the given duration string.
     */
    public static Duration parse(String input, String[] patterns) {
        return parse(input, DurationPattern.normalize(patterns));
    }

    /**
     * Parses the given duration to a Duration object.
     *
     * @param duration  The duration to be parsed.
     * @param pattern   The pattern the duration adheres to.
     * @return          A Duration object which represents the given input duration.
     */
    public static Duration parse(BigDecimal duration, DurationPattern pattern) {
        if (duration == null) return null;

        pattern = DurationPattern.normalize(pattern);
        Duration output;

        try {
            BigInteger integerValue;

            switch (pattern) {
                case NANOSECONDS:
                    output = fromNanoseconds(duration);
                    break;
                case MILLISECONDS:
                    output = fromMilliseconds(duration);
                    break;
                case SECONDS:
                    output = fromFractionalSeconds(duration);
                    break;
                case MINUTES:
                    output = fromMinutes(duration);
                    break;
                case HOURS:
                    output = fromHours(duration);
                    break;
                case DAYS:
                    output = fromDays(duration);
                    break;
                case WEEKS:
                    output = fromWeeks(duration);
                    break;
                case MONTHS:
                    try {
                        integerValue = duration.toBigIntegerExact();
                        output = DATATYPE_FACTORY.newDuration(integerValue.signum() >= 0, null, integerValue.abs(), null, null, null, null);
                    } catch(ArithmeticException ex) {
                        throw new IllegalArgumentException(MessageFormat.format("Unsupported decimal precision in specified duration: {0}", duration), ex);
                    }
                    break;
                case YEARS:
                    try {
                        integerValue = duration.toBigIntegerExact();
                        output = DATATYPE_FACTORY.newDuration(integerValue.signum() >= 0, integerValue.abs(), null, null, null, null, null);
                    } catch(ArithmeticException ex) {
                        throw new IllegalArgumentException(MessageFormat.format("Unsupported decimal precision in specified duration: {0}", duration), ex);
                    }
                    break;
                default:
                    throw new IllegalArgumentException(MessageFormat.format("Unsupported duration pattern: {0}", pattern));
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(MessageFormat.format("Unparseable duration does not conform to the specified pattern {0}: {1}", pattern, duration), ex);
        }

        return output;
    }

    /**
     * Parses the given duration to a Duration object.
     *
     * @param duration  The duration to be parsed.
     * @param pattern   The pattern the duration adheres to.
     * @return          A Duration object which represents the given input duration.
     */
    public static Duration parse(BigInteger duration, DurationPattern pattern) {
        return duration == null ? null : parse(new BigDecimal(duration), pattern);
    }

    /**
     * Parses the given duration to a Duration object.
     *
     * @param duration  The duration to be parsed.
     * @param pattern   The pattern the duration adheres to.
     * @return          A Duration object which represents the given input duration.
     */
    public static Duration parse(long duration, DurationPattern pattern) {
        return parse(BigDecimal.valueOf(duration), pattern);
    }

    /**
     * Parses the given duration to a Duration object.
     *
     * @param duration  The duration to be parsed.
     * @param pattern   The pattern the duration adheres to.
     * @return          A Duration object which represents the given input duration.
     */
    public static Duration parse(double duration, DurationPattern pattern) {
        return parse(BigDecimal.valueOf(duration), pattern);
    }

    /**
     * Parses the given duration strings to Duration objects.
     *
     * @param input The list of duration strings to be parsed.
     * @return      A list of Duration objects which represents the given duration strings.
     */
    public static Duration[] parse(String[] input) {
        return parse(input, (DurationPattern)null);
    }

    /**
     * Parses the given duration strings to Duration objects.
     *
     * @param input     The list of duration strings to be parsed.
     * @param pattern   The pattern the duration strings adhere to.
     * @return          A list of Duration objects which represents the given duration strings.
     */
    public static Duration[] parse(String[] input, String pattern) {
        return parse(input, DurationPattern.normalize(pattern));
    }

    /**
     * Parses the given duration strings to Duration objects.
     *
     * @param input     The list of duration strings to be parsed.
     * @param pattern   The pattern the duration strings adhere to.
     * @return          A list of Duration objects which represents the given duration strings.
     */
    public static Duration[] parse(String[] input, DurationPattern pattern) {
        if (input == null) return null;
        pattern = DurationPattern.normalize(pattern);

        Duration[] output = new Duration[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = parse(input[i], pattern);
        }

        return output;
    }

    /**
     * Converts the given fractional seconds to a Duration.
     *
     * @param fractionalSeconds The fractional seconds to convert to a Duration.
     * @return                  The resulting Duration.
     */
    private static Duration fromFractionalSeconds(BigDecimal fractionalSeconds) {
        BigInteger seconds = fractionalSeconds.toBigInteger().abs();
        BigDecimal fraction = fractionalSeconds.subtract(new BigDecimal(seconds)).abs();

        BigInteger[] results;

        results = seconds.divideAndRemainder(SECONDS_PER_DAY_BIG_INTEGER);
        BigInteger days = results[0];
        seconds = results[1];

        results = seconds.divideAndRemainder(SECONDS_PER_HOUR_BIG_INTEGER);
        BigInteger hours = results[0];
        seconds = results[1];

        results = seconds.divideAndRemainder(SECONDS_PER_MINUTE_BIG_INTEGER);
        BigInteger minutes = results[0];
        seconds = results[1];

        fraction = fraction.add(new BigDecimal(seconds));

        return DATATYPE_FACTORY.newDuration(fractionalSeconds.signum() >= 0, BigInteger.ZERO, BigInteger.ZERO, days, hours, minutes, fraction);
    }

    /**
     * Converts the given nanoseconds to a Duration.
     *
     * @param nanoseconds   The nanoseconds to convert to a Duration.
     * @return              The resulting Duration.
     */
    private static Duration fromNanoseconds(BigDecimal nanoseconds) {
        return fromFractionalSeconds(nanoseconds.divide(NANOSECONDS_PER_SECOND_BIG_DECIMAL));
    }

    /**
     * Converts the given milliseconds to a Duration.
     *
     * @param milliseconds  The milliseconds to convert to a Duration.
     * @return              The resulting Duration.
     */
    private static Duration fromMilliseconds(BigDecimal milliseconds) {
        return fromFractionalSeconds(milliseconds.divide(MILLISECONDS_PER_SECOND_BIG_DECIMAL));
    }

    /**
     * Converts the given minutes to a Duration.
     *
     * @param minutes   The minutes to convert to a Duration.
     * @return          The resulting Duration.
     */
    private static Duration fromMinutes(BigDecimal minutes) {
        return fromFractionalSeconds(minutes.multiply(SECONDS_PER_MINUTE_BIG_DECIMAL));
    }

    /**
     * Converts the given hours to a Duration.
     *
     * @param hours     The hours to convert to a Duration.
     * @return          The resulting Duration.
     */
    private static Duration fromHours(BigDecimal hours) {
        return fromFractionalSeconds(hours.multiply(SECONDS_PER_HOUR_BIG_DECIMAL));
    }

    /**
     * Converts the given days to a Duration.
     *
     * @param days      The days to convert to a Duration.
     * @return          The resulting Duration.
     */
    private static Duration fromDays(BigDecimal days) {
        return fromFractionalSeconds(days.multiply(SECONDS_PER_DAY_BIG_DECIMAL));
    }

    /**
     * Converts the given weeks to a Duration.
     *
     * @param weeks     The weeks to convert to a Duration.
     * @return          The resulting Duration.
     */
    private static Duration fromWeeks(BigDecimal weeks) {
        return fromFractionalSeconds(weeks.multiply(SECONDS_PER_WEEK_BIG_DECIMAL));
    }

    /**
     * Returns the given duration as an XML duration string.
     *
     * @param input The duration to be serialized.
     * @return      An XML duration string representing the given duration.
     */
    public static String emit(Duration input) {
        return emit(input, (DurationPattern)null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input     The duration to be serialized.
     * @param pattern   The pattern to use to format the duration string.
     * @return          A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, String pattern) {
        return emit(input, pattern, null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input     The duration to be serialized.
     * @param pattern   The pattern to use to format the duration string.
     * @return          A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern) {
        return emit(input, pattern, (Date)null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input     The duration to be serialized.
     * @param pattern   The pattern to use to format the duration string.
     * @param datetime  A datetime string used as a starting instant to resolve indeterminate values (such as the number
     *                  of days in a month).
     * @return          A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, String pattern, String datetime) {
        return emit(input, pattern, datetime, null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, String pattern, String datetime, String datetimePattern) {
        return emit(input, DurationPattern.normalize(pattern), datetime, datetimePattern);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, String datetime, String datetimePattern) {
        return emit(input, pattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input   The duration to be serialized.
     * @param pattern The pattern to use to format the duration string.
     * @param instant A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                number of days in a month).
     * @return        A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, Calendar instant) {
        return emit(input, pattern, instant == null ? null : instant.getTime());
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     *
     * @param input   The duration to be serialized.
     * @param pattern The pattern to use to format the duration string.
     * @param instant A java.util.Date used as a starting instant to resolve indeterminate values (such as the number of
     *                days in a month).
     * @return        A duration string formatted according to the pattern representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, Date instant) {
        if (input == null) return null;
        if (instant == null) instant = new Date();

        pattern = DurationPattern.normalize(pattern);

        String output;

        // TODO: support MONTHS and YEARS as output types using the given instant for normalization.
        switch (pattern) {
            case NANOSECONDS:
                output = toNanoseconds(input, instant).toString();
                break;
            case MILLISECONDS:
                output = toMilliseconds(input, instant).toString();
                break;
            case SECONDS:
                output = toSeconds(input, instant).toString();
                break;
            case MINUTES:
                output = toMinutes(input, instant).toString();
                break;
            case HOURS:
                output = toHours(input, instant).toString();
                break;
            case DAYS:
                output = toDays(input, instant).toString();
                break;
            case WEEKS:
                output = toWeeks(input, instant).toString();
                break;
            case XML:
                output = toISO8601(input);
                break;
            default:
                throw new IllegalArgumentException(MessageFormat.format("Unsupported duration pattern: {0}", pattern));
        }

        return output;
    }

    /**
     * Returns a XML duration string representing the given Duration object.
     *
     * @param duration The duration to convert to an XML duration string.
     * @return         An XML duration string representing the given Duration object.
     */
    private static String toISO8601(Duration duration) {
        BigInteger years = BigIntegerHelper.normalize(duration.getField(DatatypeConstants.YEARS));
        if (years == null) years = BigInteger.ZERO;

        BigInteger months = BigIntegerHelper.normalize(duration.getField(DatatypeConstants.MONTHS));
        if (months == null) months = BigInteger.ZERO;

        BigInteger days = BigIntegerHelper.normalize(duration.getField(DatatypeConstants.DAYS));
        if (days == null) days = BigInteger.ZERO;

        BigInteger hours = BigIntegerHelper.normalize(duration.getField(DatatypeConstants.HOURS));
        if (hours == null) hours = BigInteger.ZERO;

        BigInteger minutes = BigIntegerHelper.normalize(duration.getField(DatatypeConstants.MINUTES));
        if (minutes == null) minutes = BigInteger.ZERO;

        BigDecimal seconds = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.SECONDS));
        if (seconds == null) {
            seconds = BigDecimal.ZERO;
        } else {
            seconds = seconds.stripTrailingZeros();
        }

        StringBuilder builder = new StringBuilder();
        if (duration.getSign() < 0) {
            builder.append('-');
        }
        builder.append('P');

        boolean isYearsZero = years.equals(BigInteger.ZERO),
                isMonthsZero = months.equals(BigInteger.ZERO), isDaysZero = days.equals(BigInteger.ZERO),
                isHoursZero = hours.equals(BigInteger.ZERO), isMinutesZero = minutes.equals(BigInteger.ZERO),
                isSecondsZero = BigDecimalHelper.isZero(seconds), hasDate = (!isYearsZero || !isMonthsZero || !isDaysZero);

        if (!isYearsZero) {
            builder.append(years).append('Y');
        }
        if (!isMonthsZero) {
            builder.append(months).append('M');
        }
        if (!isDaysZero) {
            builder.append(days).append('D');
        }

        if (!hasDate || !isHoursZero || !isMinutesZero || !isSecondsZero) {
            builder.append('T');
            if (!isHoursZero) {
                builder.append(hours).append('H');
            }
            if (!isMinutesZero) {
                builder.append(minutes).append('M');
            }
            if (!isSecondsZero || (!hasDate && isHoursZero && isMinutesZero)) {
                if (isSecondsZero) {
                    builder.append(0);
                } else {
                    builder.append(seconds.toPlainString());
                }
                builder.append('S');
            }
        }

        return builder.toString();
    }

    /**
     * Converts the given duration to a fractional seconds representation.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The fractional seconds representing the given duration.
     */
    private static BigDecimal toFractionalSeconds(Duration duration, Date instant) {
        return toFractionalSeconds(duration, DateTimeHelper.toCalendar(instant));
    }

    /**
     * Converts the given duration to a fractional seconds representation.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The fractional seconds representing the given duration.
     */
    private static BigDecimal toFractionalSeconds(Duration duration, Calendar instant) {
        BigDecimal sign = BigDecimal.valueOf(duration.getSign());

        BigDecimal years = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.YEARS));
        if (years == null) years = BigDecimal.ZERO;

        BigDecimal months = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.MONTHS));
        if (months == null) months = BigDecimal.ZERO;

        // if the duration contains indeterminate components, i.e. years or months, then normalize it
        if (!BigDecimalHelper.isZero(years) || !BigDecimalHelper.isZero(months)) duration = duration.normalizeWith(instant);

        BigDecimal days = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.DAYS));
        if (days == null) days = BigDecimal.ZERO;

        BigDecimal hours = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.HOURS));
        if (hours == null) hours = BigDecimal.ZERO;

        BigDecimal minutes = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.MINUTES));
        if (minutes == null) minutes = BigDecimal.ZERO;

        BigDecimal seconds = BigDecimalHelper.normalize(duration.getField(DatatypeConstants.SECONDS));
        if (seconds == null) seconds = BigDecimal.ZERO;

        return sign.multiply(
                seconds.add(minutes.multiply(SECONDS_PER_MINUTE_BIG_DECIMAL))
                       .add(hours.multiply(SECONDS_PER_HOUR_BIG_DECIMAL))
                       .add(days.multiply(SECONDS_PER_DAY_BIG_DECIMAL)));
    }

    /**
     * Converts the given duration to a whole number of weeks with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of weeks which represent the given duration.
     */
    private static BigInteger toWeeks(Duration duration, Date instant) {
        return toWeeks(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of weeks with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of weeks.
     */
    private static BigInteger toWeeks(BigDecimal fractionalSeconds) {
        return fractionalSeconds.toBigInteger().divide(SECONDS_PER_WEEK_BIG_INTEGER);
    }

    /**
     * Converts the given duration to a whole number of days with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of days which represent the given duration.
     */
    private static BigInteger toDays(Duration duration, Date instant) {
        return toDays(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of days with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of days.
     */
    private static BigInteger toDays(BigDecimal fractionalSeconds) {
        return fractionalSeconds.toBigInteger().divide(SECONDS_PER_DAY_BIG_INTEGER);
    }

    /**
     * Converts the given duration to a whole number of hours with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of hours which represent the given duration.
     */
    private static BigInteger toHours(Duration duration, Date instant) {
        return toHours(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of hours with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of hours.
     */
    private static BigInteger toHours(BigDecimal fractionalSeconds) {
        return fractionalSeconds.toBigInteger().divide(SECONDS_PER_HOUR_BIG_INTEGER);
    }

    /**
     * Converts the given duration to a whole number of minutes with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of minutes which represent the given duration.
     */
    private static BigInteger toMinutes(Duration duration, Date instant) {
        return toMinutes(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of minutes with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of minutes.
     */
    private static BigInteger toMinutes(BigDecimal fractionalSeconds) {
        return fractionalSeconds.toBigInteger().divide(SECONDS_PER_MINUTE_BIG_INTEGER);
    }

    /**
     * Converts the given duration to a whole number of seconds with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of seconds which represent the given duration.
     */
    private static BigInteger toSeconds(Duration duration, Date instant) {
        return toSeconds(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of seconds with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of seconds.
     */
    private static BigInteger toSeconds(BigDecimal fractionalSeconds) {
        return fractionalSeconds.toBigInteger();
    }

    /**
     * Converts the given duration to a whole number of milliseconds with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of milliseconds which represent the given duration.
     */
    private static BigInteger toMilliseconds(Duration duration, Date instant) {
        return toMilliseconds(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of milliseconds with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of milliseconds.
     */
    private static BigInteger toMilliseconds(BigDecimal fractionalSeconds) {
        return fractionalSeconds.multiply(MILLISECONDS_PER_SECOND_BIG_DECIMAL).toBigInteger();
    }

    /**
     * Converts the given duration to a whole number of nanoseconds with no rounding.
     *
     * @param duration  The duration to convert.
     * @param instant   The instant to normalize the duration with.
     * @return          The number of nanoseconds which represent the given duration.
     */
    private static BigInteger toNanoseconds(Duration duration, Date instant) {
        return toNanoseconds(toFractionalSeconds(duration, instant));
    }

    /**
     * Converts the given fraction seconds to a whole number of nanoseconds with no rounding.
     *
     * @param fractionalSeconds The fractional seconds to convert.
     * @return                  The whole number of nanoseconds.
     */
    private static BigInteger toNanoseconds(BigDecimal fractionalSeconds) {
        return fractionalSeconds.multiply(NANOSECONDS_PER_SECOND_BIG_DECIMAL).toBigInteger();
    }

    /**
     * Returns the given durations as XML duration strings.
     *
     * @param input The list of durations to be serialized.
     * @return      XML duration strings representing the given durations.
     */
    public static String[] emit(Duration[] input) {
        return emit(input, (String)null);
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input     The list of durations to be serialized.
     * @param pattern   The pattern to use to format the duration strings.
     * @return          The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration[] input, String pattern) {
        return emit(input, pattern, null);
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input     The list of durations to be serialized.
     * @param pattern   The pattern to use to format the duration strings.
     * @return          The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration[] input, DurationPattern pattern) {
        return emit(input, pattern, (Date)null);
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input    The list of durations to be serialized.
     * @param pattern  The pattern to use to format the duration strings.
     * @param datetime A datetime string used as a starting instant to resolve indeterminate values (such as the number
     *                 of days in a month).
     * @return         The duration strings formatted according to the pattern representing the given duration.
     */
    public static String[] emit(Duration[] input, String pattern, String datetime) {
        return emit(input, pattern, datetime, null);
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input           The list of durations to be serialized.
     * @param pattern         The pattern to use to format the duration strings.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration[] input, String pattern, String datetime, String datetimePattern) {
        return emit(input, DurationPattern.normalize(pattern), datetime, datetimePattern);
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input           The list of durations to be serialized.
     * @param pattern         The pattern to use to format the duration strings.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration[] input, DurationPattern pattern, String datetime, String datetimePattern) {
        return emit(input, pattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input   The list of durations to be serialized.
     * @param pattern The pattern to use to format the duration strings.
     * @param instant A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                number of days in a month).
     * @return        The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration input[], DurationPattern pattern, Calendar instant) {
        return emit(input, pattern, instant == null ? null : instant.getTime());
    }


    /**
     * Returns the given durations as duration strings formatted to the desired pattern.
     *
     * @param input   The list of durations to be serialized.
     * @param pattern The pattern to use to format the duration strings.
     * @param instant A java.util.Date used as a starting instant to resolve indeterminate values (such as the number of
     *                days in a month).
     * @return        The duration strings formatted according to the pattern representing the given durations.
     */
    public static String[] emit(Duration[] input, DurationPattern pattern, Date instant) {
        if (input == null) return null;
        if (instant == null) instant = new Date();
        pattern = DurationPattern.normalize(pattern);

        String[] output = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = emit(input[i], pattern, instant);
        }

        return output;
    }

    /**
     * Adds the given list of durations together, returning the result.
     *
     * @param durations The list of durations to be added.
     * @return          The result of all the given durations added together.
     */
    public static Duration add(Duration... durations) {
        Duration result = DATATYPE_FACTORY.newDuration(0);

        if (durations != null) {
            for (int i = 0; i < durations.length; i++) {
                if (durations[i] != null) {
                    result = result.add(durations[i]);
                }
            }
        }

        return result;
    }

    /**
     * Subtracts the tail of the given list of durations from the head of the list, returning the result.
     *
     * @param durations The list of durations whose tail is to be subtracted from the head.
     * @return          The result of subtracting the tail of the list of durations from the head of the list.
     */
    public static Duration subtract(Duration... durations) {
        Duration result = DATATYPE_FACTORY.newDuration(0);
        boolean first = true;

        if (durations != null && durations.length > 0) {
            for (int i = 0; i < durations.length; i++) {

                if (durations[i] != null) {
                    if (first) {
                        result = durations[i]; // initialize result to first duration
                        first = false;
                    } else {
                        result = result.subtract(durations[i]);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Compares two durations, returning whether the first is less than, greater than, equal to, or cannot be compared
     * (indeterminate) to the second duration.
     *
     * @param x The first duration to be compared.
     * @param y The second duration to be compared.
     * @return  javax.xml.datatype.DatatypeConstants.LESSER if less than, javax.xml.datatype.DatatypeConstants.EQUAL if
     *          equal, javax.xml.datatype.DatatypeConstants.GREATER if greater than,
     *          javax.xml.datatype.DatatypeConstants.INDETERMINATE if durations cannot be compared.
     */
    public static int compare(Duration x, Duration y) {
        if (x == null && y == null) return DatatypeConstants.EQUAL;
        if (x == null || y == null) return DatatypeConstants.INDETERMINATE;

        return x.compare(y);
    }

    /**
     * Negates the given duration.
     *
     * @param input The duration to be negated.
     * @return      The negated duration.
     */
    public static Duration negate(Duration input) {
        if (input == null) return null;
        return input.negate();
    }

    /**
     * Returns the given duration multiplied by the given factor.
     *
     * @param input     The duration to be multiplied.
     * @param factor    The factor to multiply the duration by.
     * @return          The duration multiplied by the factor.
     */
    public static Duration multiply(Duration input, BigDecimal factor) {
        return multiply(input, factor, null, null);
    }

    /**
     * Returns the given duration multiplied by the given factor.
     *
     * @param input    The duration to be multiplied.
     * @param factor   The factor to multiply the duration by.
     * @param datetime A datetime string used as a starting instant to resolve indeterminate values (such as the number
     *                 of days in a month).
     * @return         The duration multiplied by the factor.
     */
    public static Duration multiply(Duration input, BigDecimal factor, String datetime) {
        return multiply(input, factor, datetime, null);
    }

    /**
     * Returns the given duration multiplied by the given factor.
     *
     * @param input           The duration to be multiplied.
     * @param factor          The factor to multiply the duration by.
     * @param datetime        A datetime string used as a starting instant to resolve indeterminate values (such as the
     *                        number of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration multiplied by the factor.
     */
    public static Duration multiply(Duration input, BigDecimal factor, String datetime, String datetimePattern) {
        return multiply(input, factor, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Returns the given duration multiplied by the given factor.
     *
     * @param input   The duration to be multiplied.
     * @param factor  The factor to multiply the duration by.
     * @param instant A java.util.Date used as a starting instant to resolve indeterminate values (such as the number of
     *                days in a month).
     * @return        The duration multiplied by the factor.
     */
    public static Duration multiply(Duration input, BigDecimal factor, Date instant) {
        return multiply(input, factor, DateTimeHelper.toCalendar(instant));
    }

    /**
     * Returns the given duration multiplied by the given factor.
     *
     * @param input   The duration to be multiplied.
     * @param factor  The factor to multiply the duration by.
     * @param instant A java.util.Calendar used as a starting instant to resolve indeterminate values (such as the
     *                number of days in a month).
     * @return        The duration multiplied by the factor.
     */
    public static Duration multiply(Duration input, BigDecimal factor, Calendar instant) {
        if (input == null || factor == null) return input;
        if (instant == null) instant = Calendar.getInstance();
        return input.normalizeWith(instant).multiply(factor);
    }

    /**
     * Normalizes the given object to a Duration object.
     *
     * @param input     The object to be normalized.
     * @param pattern   The pattern the object adhere to, if it is a string.
     * @return          A Duration object which represents the given object.
     */
    public static Duration normalize(Object input, String pattern) {
        return normalize(input, DurationPattern.normalize(pattern));
    }

    /**
     * Normalizes the given object to a Duration object.
     *
     * @param input     The object to be normalized.
     * @param pattern   The pattern the object adhere to, if it is a string.
     * @return          A Duration object which represents the given object.
     */
    public static Duration normalize(Object input, DurationPattern pattern) {
        if (input == null) return null;

        Duration output = null;

        if (input instanceof String) {
            output = parse((String)input, pattern);
        } else if (input instanceof Number) {
            output = parse(((Number)input).longValue());
        } else if (input instanceof Duration) {
            output = (Duration)input;
        }

        return output;
    }

    /**
     * Normalizes the given objects to Duration objects.
     *
     * @param input     The list of objects to be normalized.
     * @param pattern   The pattern the objects adhere to, if they are strings.
     * @return          A list of Duration objects which represents the given objects.
     */
    public static Duration[] normalize(Object[] input, String pattern) {
        return normalize(input, DurationPattern.normalize(pattern));
    }

    /**
     * Normalizes the given objects to Duration objects.
     *
     * @param input     The list of objects to be normalized.
     * @param pattern   The pattern the objects adhere to, if they are strings.
     * @return          A list of Duration objects which represents the given objects.
     */
    public static Duration[] normalize(Object[] input, DurationPattern pattern) {
        if (input == null) return null;

        Duration[] output = new Duration[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = normalize(input[i], pattern);
        }

        return output;
    }
}
