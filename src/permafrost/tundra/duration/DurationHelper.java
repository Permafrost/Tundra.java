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

package permafrost.tundra.duration;

import permafrost.tundra.datetime.DateTimeHelper;
import permafrost.tundra.exception.BaseException;
import permafrost.tundra.exception.ExceptionHelper;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.DatatypeConfigurationException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class DurationHelper {
    /**
     * The default pattern for a duration string.
     */
    public static final String DEFAULT_DURATION_PATTERN = "xml";

    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long MILLISECONDS_PER_MINUTE =   60 * MILLISECONDS_PER_SECOND;
    private static final long MILLISECONDS_PER_HOUR   =   60 * MILLISECONDS_PER_MINUTE;
    private static final long MILLISECONDS_PER_DAY    =   24 * MILLISECONDS_PER_HOUR;
    private static final long MILLISECONDS_PER_WEEK   =    7 * MILLISECONDS_PER_DAY;

    private static DatatypeFactory factory = null;

    static {
        try {
            factory = DatatypeFactory.newInstance();
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
     * @throws BaseException
     */
    public static String format(String duration, String inPattern, String outPattern) throws BaseException {
        return format(duration, inPattern, outPattern, null);
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
     * @throws BaseException
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime) throws BaseException {
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
     * @throws BaseException
     */
    public static String format(String duration, String inPattern, String outPattern, String datetime, String datetimePattern) throws BaseException {
        return emit(parse(duration, inPattern), outPattern, datetime, datetimePattern);
    }

    /**
     * Formats a list of duration strings to the desired pattern.
     * @param durations       The duration strings to be formatted.
     * @param inPattern       The pattern the duration strings adhere to.
     * @param outPattern      The pattern the durations will be reformatted to.
     * @return                The duration strings reformatted according to the outPattern.
     * @throws BaseException
     */
    public static String[] format(String[] durations, String inPattern, String outPattern) throws BaseException {
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
     * @throws BaseException
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime) throws BaseException {
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
     * @throws BaseException
     */
    public static String[] format(String[] durations, String inPattern, String outPattern, String datetime, String datetimePattern) throws BaseException {
        String[] results = null;
        if (durations != null) {
            results = new String[durations.length];

            for (int i = 0; i < durations.length; i++) {
                results[i] = format(durations[i], inPattern, outPattern, datetime, datetimePattern);
            }
        }
        return results;
    }

    /**
     * Parses the given duration string to a Duration object.
     * @param input    The duration string to be parsed.
     * @return         A Duration object which represents the
     *                 given duration string.
     * @throws BaseException
     */
    public static Duration parse(String input) throws BaseException {
        return parse(input, null);
    }

    /**
     * Parses the given duration string to a Duration object.
     * @param input   The duration string to be parsed.
     * @param pattern The pattern the duration string adheres to.
     * @return        A Duration object which represents the
     *                given duration string.
     * @throws BaseException
     */
    public static Duration parse(String input, String pattern) throws BaseException {
        if (pattern == null) pattern = DEFAULT_DURATION_PATTERN;

        BigInteger zero = new BigInteger("0");
        BigDecimal zero_ = new BigDecimal("0");

        Duration output = null;

        if (input != null) {
            if (pattern.equals("milliseconds")) {
                BigDecimal value = (new BigDecimal(input)).divide(new BigDecimal(1000));
                output = factory.newDuration(value.compareTo(new BigDecimal(zero)) >= 0, null, null, null, null, null, value.abs());
            } else if (pattern.equals("seconds")) {
                BigDecimal value = new BigDecimal(input);
                output = factory.newDuration(value.compareTo(new BigDecimal(zero)) >= 0, null, null, null, null, null, value.abs());
            } else if (pattern.equals("minutes")) {
                BigInteger value = new BigInteger(input);
                output = factory.newDuration(value.compareTo(zero) >= 0, null, null, null, null, value.abs(), null);
            } else if (pattern.equals("hours")) {
                BigInteger value = new BigInteger(input);
                output = factory.newDuration(value.compareTo(zero) >= 0, null, null, null, value.abs(), null, null);
            } else if (pattern.equals("days")) {
                BigInteger value = new BigInteger(input);
                output = factory.newDuration(value.compareTo(zero) >= 0, null, null, value.abs(), null, null, null);
            } else if (pattern.equals("weeks")) {
                // convert weeks to days by multiplying by 7
                BigInteger value = (new BigInteger(input)).multiply(new BigInteger("7"));
                output = factory.newDuration(value.compareTo(zero) >= 0, null, null, value.abs(), null, null, null);
            } else if (pattern.equals("months")) {
                BigInteger value = new BigInteger(input);
                output = factory.newDuration(value.compareTo(zero) >= 0, null, value.abs(), null, null, null, null);
            } else if (pattern.equals("years")) {
                BigInteger value = new BigInteger(input);
                output = factory.newDuration(value.compareTo(zero) >= 0, value.abs(), null, null, null, null, null);
            } else if (pattern.equals("xml")) {
                output = factory.newDuration(input);
            } else {
                ExceptionHelper.raise("Unparseable pattern: " + pattern);
            }
        }

        return output;
    }

    /**
     * Returns the given duration as an XML duration string.
     * @param input The duration to be serialized.
     * @return      An XML duration string representing the given duration.
     * @throws BaseException
     */
    public static String emit(Duration input) throws BaseException {
        return emit(input, null, null);
    }

    /**
     * Returns the given duration as a duration string formatted to the desired pattern.
     * @param input   The duration to be serialized.
     * @param pattern The pattern to use to format the duration string.
     * @return        A duration string formatted according to the pattern representing
     *                the given duration.
     * @throws BaseException
     */
    public static String emit(Duration input, String pattern) throws BaseException {
        return emit(input, pattern, null);
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
     * @throws BaseException
     */
    public static String emit(Duration input, String pattern, String datetime) throws BaseException {
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
     * @throws BaseException
     */
    private static String emit(Duration input, String pattern, String datetime, String datetimePattern) throws BaseException {
        if (pattern == null) pattern = DEFAULT_DURATION_PATTERN;

        Date instant = null;
        if (datetime == null) {
            instant = new Date();
        } else {
            instant = DateTimeHelper.parse(datetime, datetimePattern).getTime();
        }

        String output = null;

        if (input != null) {
            if (pattern.equals("milliseconds")) {
                output = "" + input.getTimeInMillis(instant);
            } else if (pattern.equals("seconds")) {
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_SECOND);
            } else if (pattern.equals("minutes")) {
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_MINUTE);
            } else if (pattern.equals("hours")) {
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_HOUR);
            } else if (pattern.equals("days")) {
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_DAY);
            } else if (pattern.equals("weeks")) {
                output = "" + (input.getTimeInMillis(instant) / MILLISECONDS_PER_WEEK);
            } else if (pattern.equals("xml")) {
                output = input.toString();
            } else {
                ExceptionHelper.raise("Unparseable pattern: " + pattern);
            }
        }

        return output;
    }
}
