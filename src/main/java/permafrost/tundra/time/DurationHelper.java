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

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.DatatypeConfigurationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/**
 * A collection of convenience methods for working with durations.
 */
public class DurationHelper {
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long MILLISECONDS_PER_MINUTE =   60 * MILLISECONDS_PER_SECOND;
    private static final long MILLISECONDS_PER_HOUR   =   60 * MILLISECONDS_PER_MINUTE;
    private static final long MILLISECONDS_PER_DAY    =   24 * MILLISECONDS_PER_HOUR;
    private static final long MILLISECONDS_PER_WEEK   =    7 * MILLISECONDS_PER_DAY;

    private static final BigDecimal DECIMAL_ONE_THOUSAND = new BigDecimal(1000);
    private static final BigInteger INTEGER_SEVEN = new BigInteger("7");

    private static DatatypeFactory DATATYPE_FACTORY = null;

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
     * @param duration   The duration string to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern) {
        return format(duration, inPattern, outPattern, null);
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern) {
        return format(duration, inPattern, outPattern, (Date)null);
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param milliseconds    The duration to be formatted, specified as milliseconds.
     * @param pattern         The pattern the duration will be reformatted to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(long milliseconds, DurationPattern pattern) {
        return emit(parse(milliseconds), pattern);
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration   The duration string to be formatted.
     * @param inPattern  The pattern the duration string adheres to.
     * @param outPattern The pattern the duration will be reformatted to.
     * @param datetime   An XML datetime string used as a starting instant
     *                   to resolve indeterminate values (such as the number
     *                   of days in a month).
     * @return           The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime) {
        return format(duration, inPattern, outPattern, datetime, null);
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime, String datetimePattern) {
        return format(duration, DurationPattern.normalize(inPattern), DurationPattern.normalize(outPattern), datetime, datetimePattern);
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, String datetime, String datetimePattern) {
        return format(duration, inPattern, outPattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param instant         A java.util.Calendar used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, Calendar instant) {
        return format(duration, inPattern, outPattern, instant == null ? null : instant.getTime());
    }

    /**
     * Formats a duration string to the desired pattern.
     * @param duration        The duration string to be formatted.
     * @param inPattern       The pattern the duration string adheres to.
     * @param outPattern      The pattern the duration will be reformatted to.
     * @param instant         A java.util.Date used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                The duration string reformatted according to the outPattern.
     */
    public static String format(String duration, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        return emit(parse(duration, inPattern), outPattern, instant);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern) {
        return format(durations, inPattern, outPattern, null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param datetime        An XML datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime) {
        return format(durations, inPattern, outPattern, datetime, null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime, String datetimePattern) {
        return format(durations, DurationPattern.normalize(inPattern), DurationPattern.normalize(outPattern), datetime, datetimePattern);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern) {
        return format(durations, inPattern, outPattern, (Date)null);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, String datetime, String datetimePattern) {
        return format(durations, inPattern, outPattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param instant         A java.util.Calendar used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, Calendar instant) {
        return format(durations, inPattern, outPattern, instant == null ? null : instant.getTime());
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @param instant         A java.util.Date used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                The duration strings reformatted according to the outPattern.
     */
    public static String[] format(String[] durations, DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        if (durations == null) return null;

        String[] results = new String[durations.length];
        for (int i = 0; i < durations.length; i++) {
            results[i] = format(durations[i], inPattern, outPattern, instant);
        }
        return results;
    }

    /**
     * Returns a new Duration given a duration in milliseconds.
     * @param milliseconds  The duration in milliseconds.
     * @return              A Duration object representing the duration in milliseconds.
     */
    public static Duration parse(long milliseconds) {
        return DATATYPE_FACTORY.newDuration(milliseconds);
    }

    /**
     * Parses the given duration string to a Duration object.
     * @param input    The duration string to be parsed.
     * @return         A Duration object which represents the
     *                 given duration string.
     */
    public static Duration parse(String input) {
        return parse(input, (DurationPattern)null);
    }

    /**
     * Parses the given duration string to a Duration object.
     * @param input   The duration string to be parsed.
     * @param pattern The pattern the duration string adheres to.
     * @return        A Duration object which represents the
     *                given duration string.
     */
    public static Duration parse(String input, String pattern) {
        return parse(input, DurationPattern.normalize(pattern));
    }

    /**
     * Parses the given duration string to a Duration object.
     * @param input   The duration string to be parsed.
     * @param pattern The pattern the duration string adheres to.
     * @return        A Duration object which represents the
     *                given duration string.
     */
    public static Duration parse(String input, DurationPattern pattern) {
        if (input == null) return null;

        pattern = DurationPattern.normalize(pattern);
        BigInteger integerValue = new BigInteger(input);
        BigDecimal decimalValue = new BigDecimal(input);
        Duration output = null;

        try {
            switch (pattern) {
                case MILLISECONDS:
                    // convert milliseconds to fractional seconds
                    decimalValue = decimalValue.divide(DECIMAL_ONE_THOUSAND);
                    output = DATATYPE_FACTORY.newDuration(decimalValue.compareTo(BigDecimal.ZERO) >= 0, null, null, null, null, null, decimalValue.abs());
                    break;
                case SECONDS:
                    output = DATATYPE_FACTORY.newDuration(decimalValue.compareTo(BigDecimal.ZERO) >= 0, null, null, null, null, null, decimalValue.abs());
                    break;
                case MINUTES:
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, null, null, null, null, integerValue.abs(), null);
                    break;
                case HOURS:
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, null, null, null, integerValue.abs(), null, null);
                    break;
                case DAYS:
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, null, null, integerValue.abs(), null, null, null);
                    break;
                case WEEKS:
                    // convert weeks to days by multiplying by 7
                    integerValue = integerValue.multiply(INTEGER_SEVEN);
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, null, null, integerValue.abs(), null, null, null);
                    break;
                case MONTHS:
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, null, integerValue.abs(), null, null, null, null);
                    break;
                case YEARS:
                    output = DATATYPE_FACTORY.newDuration(integerValue.compareTo(BigInteger.ZERO) >= 0, integerValue.abs(), null, null, null, null, null);
                    break;
                case XML:
                    output = DATATYPE_FACTORY.newDuration(input);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported duration pattern: " + pattern);
            }
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unparseable duration: '" + input + "' does not conform to pattern '" + pattern.toString() + "'", ex);
        }

        return output;
    }

    /**
     * Returns the given duration as an XML duration string.
     * @param input The duration to be serialized.
     * @return      An XML duration string representing the given duration.
     */
    public static String emit(Duration input) {
        return emit(input, (String)null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input   The duration to be serialized.
     * @param pattern The pattern to use to format the duration string.
     * @return        A duration string formatted according to the pattern representing
     *                the given duration.
     */
    public static String emit(Duration input, String pattern) {
        return emit(input, pattern, null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @return                A duration string formatted according to the pattern
     *                        representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern) {
        return emit(input, pattern, (Date)null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input    The duration to be serialized.
     * @param pattern  The pattern to use to format the duration string.
     * @param datetime A datetime string used as a starting instant
     *                 to resolve indeterminate values (such as the number
     *                 of days in a month).
     * @return         A duration string formatted according to the pattern representing
     *                 the given duration.
     */
    public static String emit(Duration input, String pattern, String datetime) {
        return emit(input, pattern, datetime, null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                A duration string formatted according to the pattern
     *                        representing the given duration.
     */
    public static String emit(Duration input, String pattern, String datetime, String datetimePattern) {
        return emit(input, DurationPattern.normalize(pattern), datetime, datetimePattern);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param datetime        A datetime string used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @param datetimePattern The pattern the given datetime adheres to.
     * @return                A duration string formatted according to the pattern
     *                        representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, String datetime, String datetimePattern) {
        return emit(input, pattern, DateTimeHelper.parse(datetime, datetimePattern));
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param instant         A java.util.Calendar used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                A duration string formatted according to the pattern
     *                        representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, Calendar instant) {
        return emit(input, pattern, instant == null ? null : instant.getTime());
    }


    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input           The duration to be serialized.
     * @param pattern         The pattern to use to format the duration string.
     * @param instant         A java.util.Date used as a starting instant
     *                        to resolve indeterminate values (such as the number
     *                        of days in a month).
     * @return                A duration string formatted according to the pattern
     *                        representing the given duration.
     */
    public static String emit(Duration input, DurationPattern pattern, Date instant) {
        if (input == null) return null;
        if (instant == null) instant = new Date();

        pattern = DurationPattern.normalize(pattern);
        String output = null;

        switch(pattern) {
            case MILLISECONDS:
                output = "" + input.getTimeInMillis(instant);
                break;
            case SECONDS:
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_SECOND);
                break;
            case MINUTES:
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_MINUTE);
                break;
            case HOURS:
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_HOUR);
                break;
            case DAYS:
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_DAY);
                break;
            case WEEKS:
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_WEEK);
                break;
            case XML:
                output = input.toString();
                break;
            default:
                throw new IllegalArgumentException("Unsupported duration pattern: " + pattern);
        }

        return output;
    }
}
