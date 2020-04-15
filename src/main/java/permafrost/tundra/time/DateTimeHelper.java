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
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.time.DateTimeFormatter;
import permafrost.tundra.lang.ArrayHelper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * A collection of convenience methods for working with datetimes.
 */
public final class DateTimeHelper {
    /**
     * The default pattern used, when none is specified.
     */
    public static final String DEFAULT_DATETIME_PATTERN = "datetime";
    /**
     * List of well-known named patterns.
     */
    private static final Map<String, String> NAMED_PATTERNS = new TreeMap<String, String>();
    /**
     * DB2 specific datetime pattern.
     */
    private static final Pattern DB2_DATETIME_PATTERN = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})\\.(\\d{2})\\.(\\d{2})\\.(\\d{6})");

    static {
        // Initialize the list of well-known named patterns.
        NAMED_PATTERNS.put("datetime.jdbc", "yyyy-MM-dd HH:mm:ss.SSS");
        NAMED_PATTERNS.put("datetime.db2", "yyyy-MM-dd-HH.mm.ss.SSS'000'");
        NAMED_PATTERNS.put("date", "yyyy-MM-dd");
        NAMED_PATTERNS.put("date.jdbc", "yyyy-MM-dd");
        NAMED_PATTERNS.put("date.xml", "yyyy-MM-dd");
        NAMED_PATTERNS.put("time", "HH:mm:ss.SSS");
        NAMED_PATTERNS.put("time.jdbc", "HH:mm:ss");
        NAMED_PATTERNS.put("time.xml", "HH:mm:ss.SSS");
    }

    /**
     * Disallow instantiation of this class.
     */
    private DateTimeHelper() {}

    /**
     * Adds the given XML duration to the given datetime.
     *
     * @param date      The Date to add the duration to.
     * @param duration  The duration to be added.
     * @return          A new Date representing the given date's time plus the given duration.
     */
    public static Date add(Date date, Duration duration) {
        return toDate(add(toCalendar(date), duration));
    }

    /**
     * Adds the given XML duration to the given datetime.
     *
     * @param calendar  The calendar to add the duration to.
     * @param duration  The duration to be added.
     * @return          A new calendar representing the given calendar's time plus the given duration.
     */
    public static Calendar add(Calendar calendar, Duration duration) {
        if (calendar == null || duration == null) return calendar;

        try {
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(calendar.getTime());
            gcal.setTimeZone(calendar.getTimeZone());
            XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            xcal.add(duration);

            calendar = xcal.toGregorianCalendar();
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        return calendar;
    }

    /**
     * Subtracts the given XML duration from the given datetime.
     *
     * @param date      The date to subtract the duration from.
     * @param duration  The duration to be subtracted.
     * @return          A new calendar representing the given date's time minus the given duration.
     */
    public static Date subtract(Date date, Duration duration) {
        return toDate(subtract(toCalendar(date), duration));
    }

    /**
     * Subtracts the given XML duration from the given datetime.
     *
     * @param calendar  The calendar to subtract the duration from.
     * @param duration  The duration to be subtracted.
     * @return          A new calendar representing the given calendar's time minus the given duration.
     */
    public static Calendar subtract(Calendar calendar, Duration duration) {
        if (duration != null) duration = duration.negate();
        return add(calendar, duration);
    }

    /**
     * Returns the current datetime minus the given duration.
     *
     * @param duration  The duration to be subtracted from the current datetime.
     * @return          The current datetime minus the given duration.
     */
    public static Calendar earlier(Duration duration) {
        return subtract(Calendar.getInstance(), duration);
    }

    /**
     * Returns the current datetime plus the given duration.
     *
     * @param duration  The duration to be added from the current datetime.
     * @return          The current datetime plus the given duration.
     */
    public static Calendar later(Duration duration) {
        return add(Calendar.getInstance(), duration);
    }

    /**
     * Compares two datetimes.
     *
     * @param firstCalendar     The first calendar to compare.
     * @param secondCalendar    The second calendar to compare.
     * @return                  Zero if both calendars represent that same instant in time, less than zero if the
     *                          firstCalendar is an earlier instant in time than the secondCalendar, or greater than
     *                          zero if the firstCalendar is greater than the secondCalendar.
     */
    public static int compare(Calendar firstCalendar, Calendar secondCalendar) {
        if (firstCalendar == null && secondCalendar == null) return 0;
        if (firstCalendar == null) return -1;
        if (secondCalendar == null) return 1;

        return firstCalendar.compareTo(secondCalendar);
    }

    /**
     * Compares two datetime strings.
     *
     * @param firstDate         The first datetime string to compare.
     * @param firstPattern      The datetime pattern firstDate conforms to.
     * @param secondDate        The second datetime string to compare.
     * @param secondPattern     The datetime pattern secondDate conforms to.
     * @return                  Zero if both calendars represent that same instant in time, less than zero if the
     *                          firstCalendar is an earlier instant in time than the secondCalendar, or greater than
     *                          zero if the firstCalendar is greater than the secondCalendar.
     */
    public static int compare(String firstDate, String firstPattern, String secondDate, String secondPattern) {
        return compare(parse(firstDate, firstPattern), parse(secondDate, secondPattern));
    }

    /**
     * Returns the duration of time between two given Calendar objects.
     *
     * @param startDate The starting instant in time.
     * @param endDate   The ending instant in time.
     * @return          The duration of time from the start instant to the end instant.
     */
    public static Duration duration(Date startDate, Date endDate) {
        return duration(toCalendar(startDate), toCalendar(endDate));
    }

    /**
     * Returns the duration of time between two given Calendar objects.
     *
     * @param startCalendar The starting instant in time.
     * @param endCalendar   The ending instant in time.
     * @return              The duration of time from the start instant to the end instant.
     */
    public static Duration duration(Calendar startCalendar, Calendar endCalendar) {
        if (startCalendar == null || endCalendar == null) return null;
        return DurationHelper.parse((endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis()));
    }

    /**
     * Returns the given datetime as an XML datetime string.
     *
     * @param input The datetime to be serialized to an XML string.
     * @return      The given datetime serialized as an XML string.
     */
    public static String emit(Date input) {
        return emit(input, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     *
     * @param input     The datetime to be serialized to a string.
     * @param pattern   The serialization pattern to use.
     * @return          The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Date input, String pattern) {
        return emit(input, pattern, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     *
     * @param input     The datetime to be serialized to a string.
     * @param pattern   The serialization pattern to use.
     * @param timezone  The timezone the datetime string should use.
     * @return          The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Date input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        return emit(toCalendar(input), pattern, timezone);
    }

    /**
     * Returns the given datetime as an XML datetime string.
     *
     * @param input The datetime to be serialized to an XML string.
     * @return      The given datetime serialized as an XML string.
     */
    public static String emit(Calendar input) {
        return emit(input, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     *
     * @param input     The datetime to be serialized to a string.
     * @param pattern   The serialization pattern to use.
     * @return          The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Calendar input, String pattern) {
        return emit(input, pattern, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     *
     * @param input     The datetime to be serialized to a string.
     * @param pattern   The serialization pattern to use.
     * @param timezone  The timezone the datetime string should use.
     * @return          The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Calendar input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        if (pattern == null) pattern = DEFAULT_DATETIME_PATTERN;

        String output;

        if (timezone != null) {
            input = TimeZoneHelper.convert(input, timezone);
        }

        if (pattern.equals("datetime") || pattern.equals("datetime.xml")) {
            output = DatatypeConverter.printDateTime(input);
        } else if (pattern.equals("milliseconds")) {
            output = "" + input.getTimeInMillis();
        } else if (pattern.equals("seconds")) {
            output = "" + (input.getTimeInMillis() / 1000L);
        } else {
            if (NAMED_PATTERNS.containsKey(pattern)) pattern = NAMED_PATTERNS.get(pattern);
            DateFormat formatter = new SimpleDateFormat(pattern);
            formatter.setTimeZone(input.getTimeZone());
            formatter.setLenient(false);
            output = formatter.format(input.getTime());
        }

        return output;
    }

    /**
     * Returns the given list of datetimes as a list of XML datetime strings.
     *
     * @param inputs    The datetimes to be serialized to XML strings.
     * @return          The given datetimes serialized as XML strings.
     */
    public static String[] emit(Date[] inputs) {
        return emit(inputs, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     *
     * @param inputs    The datetimes to be serialized to strings.
     * @param pattern   The serialization pattern to use.
     * @return          The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Date[] inputs, String pattern) {
        return emit(inputs, pattern, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     *
     * @param inputs    The datetimes to be serialized to strings.
     * @param pattern   The serialization pattern to use.
     * @param timezone  The timezone the datetime strings should use.
     * @return          The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Date[] inputs, String pattern, TimeZone timezone) {
        String[] outputs = null;
        if (inputs != null) {
            outputs = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                outputs[i] = emit(inputs[i], pattern, timezone);
            }
        }
        return outputs;
    }

    /**
     * Returns the given list of datetimes as a list of XML datetime strings.
     *
     * @param inputs    The datetimes to be serialized to XML strings.
     * @return          The given datetimes serialized as XML strings.
     */
    public static String[] emit(Calendar[] inputs) {
        return emit(inputs, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     *
     * @param inputs    The datetimes to be serialized to strings.
     * @param pattern   The serialization pattern to use.
     * @return          The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Calendar[] inputs, String pattern) {
        return emit(inputs, pattern, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     *
     * @param inputs    The datetimes to be serialized to strings.
     * @param pattern   The serialization pattern to use.
     * @param timezone  The timezone the datetime strings should use.
     * @return          The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Calendar[] inputs, String pattern, TimeZone timezone) {
        String[] outputs = null;
        if (inputs != null) {
            outputs = new String[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                outputs[i] = emit(inputs[i], pattern, timezone);
            }
        }
        return outputs;
    }

    /**
     * Emits the given milliseconds as a datetime string formatted according to the given pattern.
     *
     * @param milliseconds  The milliseconds since epoch value to emit.
     * @param pattern       The datetime string pattern to format the resulting string with.
     * @param timezone      The timezone the returned datetime string should be in.
     * @return              The given milliseconds formatted as a datetime string.
     */
    public static String emit(Number milliseconds, String pattern, TimeZone timezone) {
        return emit(parse(milliseconds), pattern, timezone);
    }

    /**
     * Emits the given milliseconds as a datetime string formatted according to the given pattern.
     *
     * @param milliseconds  The milliseconds since epoch value to emit.
     * @param pattern       The datetime string pattern to format the resulting string with.
     * @param timezone      The timezone the returned datetime string should be in.
     * @return              The given milliseconds formatted as a datetime string.
     */
    public static String emit(long milliseconds, String pattern, TimeZone timezone) {
        return emit(parse(milliseconds), pattern, timezone);
    }

    /**
     * Parses a milliseconds since epoch value into a Calendar object.
     *
     * @param milliseconds  The milliseconds since epoch value.
     * @return              A Calendar object representing the parsed value.
     */
    public static Calendar parse(long milliseconds) {
        return parse(milliseconds, null);
    }

    /**
     * Parses a milliseconds since epoch value into a Calendar object.
     *
     * @param milliseconds  The milliseconds since epoch value.
     * @param timezone      The time zone into which the parsed string will be forced.
     * @return              A Calendar object representing the parsed value.
     */
    public static Calendar parse(long milliseconds, TimeZone timezone) {
        Calendar output = Calendar.getInstance();
        output.setTimeInMillis(milliseconds);
        return TimeZoneHelper.convert(output, timezone);
    }

    /**
     * Parses a milliseconds since epoch value into a Calendar object.
     *
     * @param milliseconds  The milliseconds since epoch value.
     * @return              A Calendar object representing the parsed value.
     */
    public static Calendar parse(Number milliseconds) {
        return parse(milliseconds, null);
    }

    /**
     * Parses a milliseconds since epoch value into a Calendar object.
     *
     * @param milliseconds  The milliseconds since epoch value.
     * @param timezone      The time zone into which the parsed string will be forced.
     * @return              A Calendar object representing the parsed value.
     */
    public static Calendar parse(Number milliseconds, TimeZone timezone) {
        if (milliseconds == null) return null;
        return parse(milliseconds.longValue(), timezone);
    }

    /**
     * Parses an XML datetime string and returns a Calendar object.
     *
     * @param input The XML datetime string to be parsed.
     * @return      A Calendar object representing the parsed XML datetime string.
     */
    public static Calendar parse(String input) {
        return parse(input, (String)null);
    }

    /**
     * Parses a datetime string that adheres to the given pattern and returns a Calendar object.
     *
     * @param input     The datetime string to be parsed.
     * @param pattern   The datetime pattern the given string adheres to.
     * @return          A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String pattern) {
        return parse(input, pattern, (TimeZone)null);
    }

    /**
     * BigDecimal constant for the value 1,000, which is used to convert a fractional seconds value to a Calendar.
     */
    private static final BigDecimal BIG_DECIMAL_ONE_THOUSAND = BigDecimal.valueOf(1000L);

    /**
     * Parses a datetime string that adheres to the given pattern and returns a Calendar object.
     *
     * @param input     The datetime string to be parsed.
     * @param pattern   The datetime pattern the given string adheres to.
     * @param timezone  The time zone into which the parsed string will be forced.
     * @return          A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        if (pattern == null) pattern = DEFAULT_DATETIME_PATTERN;

        Calendar output;

        try {
            if (pattern.equals("datetime") || pattern.equals("datetime.xml")) {
                output = DatatypeConverter.parseDateTime(input);
            } else if (pattern.equals("datetime.jdbc")) {
                output = Calendar.getInstance();
                output.setTime(Timestamp.valueOf(input));
            } else if (pattern.equals("datetime.db2")) {
                Matcher matcher = DB2_DATETIME_PATTERN.matcher(input);
                if (matcher.matches()) {
                    output = Calendar.getInstance();
                    output.setLenient(false);

                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));
                    int day = Integer.parseInt(matcher.group(3));
                    int hour = Integer.parseInt(matcher.group(4));
                    int minute = Integer.parseInt(matcher.group(5));
                    int second = Integer.parseInt(matcher.group(6));
                    int microsecond = Integer.parseInt(matcher.group(7));

                    output.set(year, month - 1, day, hour, minute, second);
                    output.set(Calendar.MILLISECOND, microsecond / 1000);
                } else {
                    throw new ParseException(MessageFormat.format("Unparseable date: {0}", input), 0);
                }
            } else if (pattern.equals("date") || pattern.equals("date.xml")) {
                output = DatatypeConverter.parseDate(input);
            } else if (pattern.equals("time") || pattern.equals("time.xml")) {
                output = DatatypeConverter.parseTime(input);
            } else if (pattern.equals("milliseconds")) {
                output = Calendar.getInstance();
                if (isBigDecimal(input)) {
                    output.setTimeInMillis(new BigDecimal(input).longValue());
                } else {
                    output.setTimeInMillis(Long.parseLong(input));
                }
            } else if (pattern.equals("seconds")) {
                output = Calendar.getInstance();
                if (isBigDecimal(input)) {
                    BigDecimal seconds = new BigDecimal(input);
                    output.setTimeInMillis(seconds.multiply(BIG_DECIMAL_ONE_THOUSAND).longValue());
                } else {
                    output.setTimeInMillis(Long.parseLong(input) * 1000L);
                }
            } else {
                if (NAMED_PATTERNS.containsKey(pattern)) pattern = NAMED_PATTERNS.get(pattern);

                DateFormat formatter = new SimpleDateFormat(pattern);
                formatter.setLenient(false);
                output = Calendar.getInstance();
                output.setTime(formatter.parse(input));
            }

            if (timezone != null) output = TimeZoneHelper.replace(output, timezone);
        } catch(Exception ex) {
            throw new IllegalArgumentException(MessageFormat.format("Unparseable datetime: {0} does not conform to the specified pattern {1}", input, pattern), ex);
        }

        return output;
    }

    /**
     * Returns true if the given string is likely a BigDecimal rather than Long value.
     *
     * @param input The input string.
     * @return      True if the given string is likely a BigDecimal rather than Long value.
     */
    private static boolean isBigDecimal(String input) {
        return input.indexOf('.') >= 0 || input.indexOf('E') >= 0;
    }

    /**
     * Parses a datetime string that adheres to one of the given patterns and returns a Calendar object.
     *
     * @param input     The datetime string to be parsed.
     * @param patterns  A list of datetime patterns the given string might adhere to.
     * @return          A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String[] patterns) {
        return parse(input, patterns, null);
    }

    /**
     * Parses a datetime string that adheres to one of the given patterns and returns a Calendar object.
     *
     * @param input     The datetime string to be parsed.
     * @param patterns  A list of datetime patterns the given string might adhere to.
     * @param timezone  The time zone into which the parsed string will be forced.
     * @return          A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String[] patterns, TimeZone timezone) {
        if (input == null) return null;
        if (patterns == null) patterns = new String[1];

        Calendar output = null;
        boolean parsed = false;
        Throwable exception = null;

        for (String pattern : patterns) {
            try {
                output = parse(input, pattern, timezone);
                parsed = true;
                break;
            } catch (IllegalArgumentException ex) {
                exception = ex;
            }
        }

        if (!parsed) {
            throw new IllegalArgumentException(MessageFormat.format("Unparseable datetime: \"{0}\" does not conform to any of the specified patterns [\"{1}\"]", input, ArrayHelper.join(patterns, "\", \"")), exception);
        }

        return output;
    }

    /**
     * Parses a list of XML datetime strings and returns a list of Calendar objects.
     *
     * @param inputs    The list of XML datetime strings to be parsed.
     * @return          A list of Calendar objects representing the parsed XML datetime strings.
     */
    public static Calendar[] parse(String[] inputs) {
        return parse(inputs, (String)null);
    }

    /**
     * Parses a list of datetime strings that adhere to the given pattern and returns a list of Calendar objects.
     *
     * @param inputs    The list of datetime strings to be parsed.
     * @param pattern   The datetime pattern the given string adheres to.
     * @return          A list of Calendar objects representing the parsed datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String pattern) {
        return parse(inputs, pattern, null);
    }

    /**
     * Parses a list of milliseconds since epoch values and returns a list of Calendar objects.
     *
     * @param inputs    The list of milliseconds since epoch values to be parsed.
     * @return          A list of Calendar objects representing the parsed values.
     */
    public static Calendar[] parse(long[] inputs) {
        if (inputs == null) return null;

        Calendar[] outputs = new Calendar[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = parse(inputs[i]);
        }

        return outputs;
    }

    /**
     * Parses a list of datetime strings that adhere to the given pattern and returns a list of Calendar objects.
     *
     * @param inputs    The list of datetime strings to be parsed.
     * @param pattern   The datetime pattern the given string adheres to.
     * @param timezone  The time zone into which the parsed string will be forced.
     * @return          A list of Calendar objects representing the parsed datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String pattern, TimeZone timezone) {
        if (inputs == null) return null;

        Calendar[] outputs = new Calendar[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = parse(inputs[i], pattern, timezone);
        }

        return outputs;
    }

    /**
     * Parses a list of datetime strings that adhere to one of patterns in the given the list and returns a list of
     * Calendar objects.
     *
     * @param inputs    The list of datetime strings to be parsed.
     * @param patterns  The list of datetime patterns the given strings might adhere to.
     * @return          A list of Calendar objects representing the parsed datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String[] patterns) {
        return parse(inputs, patterns, null);
    }

    /**
     * Parses a list of datetime strings that adhere to one of patterns in the given the list and returns a list of
     * Calendar objects.
     *
     * @param inputs    The list of datetime strings to be parsed.
     * @param patterns  The list of datetime patterns the given strings might adhere to.
     * @param timezone  The time zone into which the parsed string will be forced.
     * @return          A list of Calendar objects representing the parsed datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String[] patterns, TimeZone timezone) {
        if (inputs == null) return null;

        Calendar[] outputs = new Calendar[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = parse(inputs[i], patterns, timezone);
        }

        return outputs;
    }

    /**
     * Returns the current datetime as a java.util.Calendar.
     *
     * @return The current datetime as a java.util.Calendar.
     */
    public static Calendar now() {
        return Calendar.getInstance();
    }

    /**
     * Returns the current datetime as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @return          The current datetime as a string formatted according to the given pattern.
     */
    public static String now(String pattern) {
        return now(pattern, null);
    }

    /**
     * Returns the current datetime as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @param timezone  The time zone the datetime should be returned in.
     * @return          The current datetime as a string formatted according to the given pattern.
     */
    public static String now(String pattern, TimeZone timezone) {
        return emit(now(), pattern, timezone);
    }

    /**
     * Returns the current date as a java.util.Calendar.
     *
     * @return The current date as a java.util.Calendar.
     */
    public static Calendar today() {
        Calendar today = now();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return today;
    }

    /**
     * Returns the current date as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @return          The current date as a string formatted according to the given pattern.
     */
    public static String today(String pattern) {
        return today(pattern, null);
    }

    /**
     * Returns the current date as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @param timezone  The time zone the datetime should be returned in.
     * @return          The current date as a string formatted according to the given pattern.
     */
    public static String today(String pattern, TimeZone timezone) {
        return emit(TimeZoneHelper.replace(today(), timezone), pattern);
    }

    /**
     * Returns the current date plus 1 day as a java.util.Calendar.
     *
     * @return The current date plus 1 day as a java.util.Calendar.
     */
    public static Calendar tomorrow() {
        return add(today(), DurationHelper.parse(DurationHelper.MILLISECONDS_PER_DAY));
    }

    /**
     * Returns the current date plus 1 day as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @return          The current date plus 1 day as a string formatted according to the given pattern.
     */
    public static String tomorrow(String pattern) {
        return tomorrow(pattern, null);
    }

    /**
     * Returns the current date plus 1 day as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @param timezone  The time zone the datetime should be returned in.
     * @return          The current date plus 1 day as a string formatted according to the given pattern.
     */
    public static String tomorrow(String pattern, TimeZone timezone) {
        return emit(TimeZoneHelper.replace(tomorrow(), timezone), pattern);
    }

    /**
     * Returns the current date minus 1 day as a java.util.Calendar.
     *
     * @return The current date minus 1 day as a java.util.Calendar.
     */
    public static Calendar yesterday() {
        return subtract(today(), DurationHelper.parse(DurationHelper.MILLISECONDS_PER_DAY));
    }

    /**
     * Returns the current date minus 1 day as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @return          The current date minus 1 day as a string formatted according to the given pattern.
     */
    public static String yesterday(String pattern) {
        return yesterday(pattern, null);
    }

    /**
     * Returns the current date minus 1 day as a string formatted according to the given pattern.
     *
     * @param pattern   The serialization pattern to use.
     * @param timezone  The time zone the datetime should be returned in.
     * @return          The current date minus 1 day as a string formatted according to the given pattern.
     */
    public static String yesterday(String pattern, TimeZone timezone) {
        return emit(TimeZoneHelper.replace(yesterday(), timezone), pattern);
    }

    /**
     * Reformats the given milliseconds since epoch value as an XML datetime string.
     *
     * @param milliseconds  The milliseconds since epoch value to be formatted.
     * @return              The given milliseconds since epoch value formatted as an XML datetime string.
     */
    public static String format(long milliseconds) {
        return format(milliseconds, null, (TimeZone)null);
    }

    /**
     * Reformats the given milliseconds since epoch value according to the desired pattern.
     *
     * @param milliseconds  The milliseconds since epoch value to be formatted.
     * @param pattern       The pattern the resulting datetime string should be formatted as.
     * @return              The given milliseconds since epoch value formatted according to the given pattern.
     */
    public static String format(long milliseconds, String pattern) {
        return format(milliseconds, pattern, null);
    }

    /**
     * Reformats the given milliseconds since epoch value according to the desired pattern.
     *
     * @param milliseconds  The milliseconds since epoch value to be formatted.
     * @param pattern       The pattern the resulting datetime string should be formatted as.
     * @param timezone      The time zone ID identifying the time zone the returned datetime string should be in.
     * @return              The given milliseconds since epoch value formatted according to the given pattern.
     */
    public static String format(long milliseconds, String pattern, TimeZone timezone) {
        return emit(parse(milliseconds), pattern, timezone);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param input         The datetime string to be reformatted.
     * @param inPattern     The pattern the given input datetime string adheres to.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String inPattern, String outPattern) {
        return format(input, inPattern, null, outPattern, null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param input         The datetime string to be reformatted.
     * @param inPattern     The pattern the given input datetime string adheres to.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String inPattern, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return emit(parse(input, inPattern, inTimeZone), outPattern, outTimeZone);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param input         The datetime string to be reformatted.
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String[] inPatterns, String outPattern) {
        return format(input, inPatterns, null, outPattern, null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param input         The datetime string to be reformatted.
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String[] inPatterns, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return emit(parse(input, inPatterns, inTimeZone), outPattern, outTimeZone);
    }

    /**
     * Reformats the given list of milliseconds since epoch values to XML datetime strings.
     *
     * @param inputs    The list of milliseconds since epoch value to be formatted.
     * @return          The given milliseconds since epoch value formatted to XML datetime strings.
     */
    public static String[] format(long[] inputs) {
        return format(inputs, null, (TimeZone)null);
    }

    /**
     * Reformats the given list of milliseconds since epoch values according to the desired pattern.
     *
     * @param inputs    The list of milliseconds since epoch value to be formatted.
     * @param pattern   The pattern the resulting datetime string should be formatted as.
     * @return          The given milliseconds since epoch value formatted according to the given pattern.
     */
    public static String[] format(long[] inputs, String pattern) {
        return format(inputs, pattern, (TimeZone)null);
    }

    /**
     * Reformats the given list of milliseconds since epoch values according to the desired pattern.
     *
     * @param inputs    The list of milliseconds since epoch values to be formatted.
     * @param pattern   The pattern the resulting datetime string should be formatted as.
     * @param timezone  The time zone ID identifying the time zone the returned datetime string should be in.
     * @return          The given milliseconds since epoch value formatted according to the given pattern.
     */
    public static String[] format(long[] inputs, String pattern, String timezone) {
        return format(inputs, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Reformats the given list of milliseconds since epoch values according to the desired pattern.
     *
     * @param inputs    The list of milliseconds since epoch values to be formatted.
     * @param pattern   The pattern the resulting datetime string should be formatted as.
     * @param timezone  The time zone ID identifying the time zone the returned datetime string should be in.
     * @return          The given milliseconds since epoch value formatted according to the given pattern.
     */
    public static String[] format(long[] inputs, String pattern, TimeZone timezone) {
        if (inputs == null) return null;

        String[] outputs = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = format(inputs[i], pattern, timezone);
        }
        return outputs;
    }

    /**
     * Reformats the given list of datetime strings according to the desired pattern.
     *
     * @param inputs        The list of datetime strings to be reformatted.
     * @param inPattern     The pattern the given input datetime strings adhere to.
     * @param outPattern    The pattern the datetime strings should be reformatted as.
     * @return              The given datetime strings reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String inPattern, String outPattern) {
        return format(inputs, inPattern, null, outPattern, null);
    }

    /**
     * Reformats the given list of datetime strings according to the desired pattern.
     *
     * @param inputs        The list of datetime strings to be reformatted.
     * @param inPattern     The pattern the given input datetime strings adhere to.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime strings should be reformatted as.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @return              The given datetime strings reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String inPattern, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        if (inputs == null) return null;

        String[] outputs = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = format(inputs[i], inPattern, inTimeZone, outPattern, outTimeZone);
        }
        return outputs;
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param inputs        The list of datetime strings to be reformatted.
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String[] inPatterns, String outPattern) {
        return format(inputs, inPatterns, null, outPattern, null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     *
     * @param inputs        The list of datetime strings to be reformatted.
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @return              The given datetime string reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String[] inPatterns, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        if (inputs == null) return null;

        String[] outputs = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            outputs[i] = format(inputs[i], inPatterns, inTimeZone, outPattern, outTimeZone);
        }
        return outputs;
    }

    /**
     * Reformats the datetime strings in the given IData document according to the desired pattern.
     *
     * @param document      The IData document containing strings to be reformatted.
     * @param inPattern     The pattern the given input datetime strings adhere to.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @return              The given datetime strings reformatted according to the given outPattern.
     */
    public static IData format(IData document, String inPattern, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return Transformer.transform(document, new DateTimeFormatter(inPattern, inTimeZone, outPattern, outTimeZone));
    }

    /**
     * Reformats the datetime strings in the given IData document according to the desired pattern.
     *
     * @param document      The IData document containing strings to be reformatted.
     * @param inPatterns    The list of patterns the given input datetime string might adhere to.
     * @param outTimeZone   The time zone the returned datetime string should be in.
     * @param inTimeZone    The time zone the given datetime string should be parsed in.
     * @param outPattern    The pattern the datetime string should be reformatted as.
     * @return              The given datetime strings reformatted according to the given outPattern.
     */
    public static IData format(IData document, String[] inPatterns, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return Transformer.transform(document, new DateTimeFormatter(inPatterns, inTimeZone, outPattern, outTimeZone));
    }

    /**
     * Converts the given Date object to a Calendar object.
     *
     * @param input The Date object to be converted.
     * @return      The Calendar object representing the given Date object.
     */
    public static Calendar toCalendar(Date input) {
        if (input == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(input);
        return calendar;
    }

    /**
     * Converts the given Calendar object to a Date object.
     *
     * @param input The Calendar object to be converted.
     * @return      The Date object representing the given Calendar object.
     */
    public static Date toDate(Calendar input) {
        if (input == null) return null;
        return input.getTime();
    }

    /**
     * Returns a new calendar that is constructed from the date part of the given date, and the time part from the given
     * time.
     *
     * @param date      The date part to be concatenated.
     * @param time      The time part to be concatenated.
     * @return          The resulting calendar with date and time parts from the given date and time calendars.
     */
    public static Calendar concatenate(Calendar date, Calendar time) {
        if (date == null || time == null) return null;

        // set date part of given time to epoch date to ensure it is not included in resulting datetime
        time.set(Calendar.DAY_OF_MONTH, 1);
        time.set(Calendar.MONTH, 0);
        time.set(Calendar.YEAR, 1970);

        Calendar datetime = Calendar.getInstance(time.getTimeZone());
        datetime.setTimeInMillis(time.getTimeInMillis());

        datetime.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        datetime.set(Calendar.MONTH, date.get(Calendar.MONTH));
        datetime.set(Calendar.YEAR, date.get(Calendar.YEAR));

        return datetime;
    }

    /**
     * Returns the largest datetime from the given list.
     *
     * @param calendars The list of datetimes to find the maximum in.
     * @return          The maximum datetime in the given list.
     */
    public static Calendar maximum(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) return null;
        return maximum(Arrays.asList(calendars));
    }

    /**
     * Returns the largest datetime from the given list.
     *
     * @param calendars The list of datetimes to find the maximum in.
     * @return          The maximum datetime in the given list.
     */
    public static Calendar maximum(Collection<Calendar> calendars) {
        if (calendars == null || calendars.size() == 0) return null;

        SortedSet<Calendar> set = new TreeSet<Calendar>();

        for (Calendar calendar : calendars) {
            if (calendar != null) set.add(calendar);
        }

        return set.size() == 0 ? null : set.last();
    }

    /**
     * Returns the smallest datetime from the given list.
     *
     * @param calendars The list of datetimes to find the minimum in.
     * @return          The minimum datetime in the given list.
     */
    public static Calendar minimum(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) return null;
        return minimum(Arrays.asList(calendars));
    }

    /**
     * Returns the smallest datetime from the given list.
     *
     * @param calendars The list of datetimes to find the minimum in.
     * @return          The minimum datetime in the given list.
     */
    public static Calendar minimum(Collection<Calendar> calendars) {
        if (calendars == null || calendars.size() == 0) return null;

        SortedSet<Calendar> set = new TreeSet<Calendar>();

        for (Calendar calendar : calendars) {
            if (calendar != null) set.add(calendar);
        }

        return set.size() == 0 ? null : set.first();
    }

    /**
     * Normalizes the given Object to a Calendar: if it's already a Calendar, it is returned;
     * if it's a Date, it is converted to a Calendar; if it's a Number, it is treated as
     * a milliseconds since epoch value to create a new Calendar; otherwise it is parsed as
     * a datetime string.
     *
     * @param object    The Object to be normalized to a Calendar.
     * @return          A Calendar object representing the given Object.
     */
    public static Calendar normalize(Object object) {
        return normalize(object, null);
    }

    /**
     * Normalizes the given Object to a Calendar: if it's already a Calendar, it is returned;
     * if it's a Date, it is converted to a Calendar; if it's a Number, it is treated as
     * a milliseconds since epoch value to create a new Calendar; otherwise it is parsed as
     * a datetime string.
     *
     * @param object    The Object to be normalized to a Calendar.
     * @param pattern   An optional datetime pattern the given Object adheres to, if it's a String.
     * @return          A Calendar object representing the given Object.
     */
    public static Calendar normalize(Object object, String pattern) {
        return normalize(object, pattern, null);
    }

    /**
     * Normalizes the given Object to a Calendar: if it's already a Calendar, it is returned;
     * if it's a Date, it is converted to a Calendar; if it's a Number, it is treated as
     * a milliseconds since epoch value to create a new Calendar; otherwise it is parsed as
     * a datetime string.
     *
     * @param object    The Object to be normalized to a Calendar.
     * @param pattern   An optional datetime pattern the given Object adheres to, if it's a String.
     * @param timezone  The time zone into which the parsed string will be forced.
     * @return          A Calendar object representing the given Object.
     */
    public static Calendar normalize(Object object, String pattern, TimeZone timezone) {
        if (object == null) return null;

        Calendar calendar;

        if (object instanceof Calendar) {
            calendar = TimeZoneHelper.convert((Calendar)object, timezone);
        } else if (object instanceof Date) {
            calendar = TimeZoneHelper.convert(toCalendar((Date)object), timezone);
        } else if (object instanceof Number) {
            calendar = parse((Number)object, timezone);
        } else {
            calendar = parse(object.toString(), pattern, timezone);
        }

        return calendar;
    }

    /**
     * Returns true if the given datetime instance is within the given datetime range.
     *
     * @param instance  The datetime instance to determine if within the given range.
     * @param start     Optional datetime range start. If null, this is a beginless range.
     * @param end       Optional datetime range end. If null, this is an endless range.
     * @return          True if the given datetime instance is within the given datetime range.
     */
    public static boolean within(Calendar instance, Calendar start, Calendar end) {
        boolean within = false;
        if (instance != null) {
            if (start == null) {
                // beginless range
                within = DateTimeHelper.compare(instance, end) <= 0;
            } else if (end == null) {
                // endless range
                within = DateTimeHelper.compare(instance, start) >= 0;
            } else {
                if (DateTimeHelper.compare(start, end) > 0) {
                    throw new IllegalArgumentException("datetime range start " + emit(start) + " is required to be before range end " + emit(end));
                }
                within = DateTimeHelper.compare(instance, start) >= 0 && DateTimeHelper.compare(instance, end) <= 0;
            }
        }
        return within;
    }
}
