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

import permafrost.tundra.lang.ArrayHelper;
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
    public static final long MILLISECONDS_PER_SECOND = 1000;
    public static final long MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
    public static final long MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
    public static final long MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;
    public static final long MILLISECONDS_PER_WEEK = 7 * MILLISECONDS_PER_DAY;

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
     * Returns a new Duration given a duration in milliseconds.
     *
     * @param milliseconds  The duration in milliseconds.
     * @return              A Duration object representing the duration in milliseconds.
     */
    public static Duration parse(long milliseconds) {
        return DATATYPE_FACTORY.newDuration(milliseconds);
    }

    /**
     * Returns a new Duration given a duration in fractional seconds.
     *
     * @param seconds   The duration in fractional seconds.
     * @return          A Duration object representing the duration in fractional seconds.
     */
    public static Duration parse(double seconds) {
        return DATATYPE_FACTORY.newDuration(seconds >= 0.0, null, null, null, null, null, new BigDecimal(seconds).abs());
    }

    /**
     * Returns a new Duration given a duration in fractional seconds.
     *
     * @param seconds   The duration in fractional seconds.
     * @param precision The number of decimal places to respect int the given fraction.
     * @return          A Duration object representing the duration in fractional seconds.
     */
    public static Duration parse(double seconds, int precision) {
        return DATATYPE_FACTORY.newDuration(seconds >= 0.0, null, null, null, null, null, (new BigDecimal(seconds)).abs().setScale(precision, RoundingMode.HALF_UP));
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

        try {
            if (pattern == DurationPattern.XML) {
                output = DATATYPE_FACTORY.newDuration(input);
            } else {
                BigDecimal decimalValue = new BigDecimal(input);
                BigInteger integerValue = decimalValue.toBigInteger();

                switch (pattern) {
                    case MILLISECONDS:
                        // convert milliseconds to fractional seconds
                        decimalValue = decimalValue.divide(DECIMAL_ONE_THOUSAND, RoundingMode.HALF_UP);
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
                    default:
                        throw new IllegalArgumentException("Unsupported duration pattern: " + pattern);
                }
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unparseable duration: '" + input + "' does not conform to pattern '" + pattern.toString() + "'", ex);
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
            throw new IllegalArgumentException(MessageFormat.format("Unparseable duration: \"{0}\" does not conform to pattern \"{1}\"", input, ArrayHelper.stringify(patterns)));
        }

        return output;
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
        String output = null;

        switch (pattern) {
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
