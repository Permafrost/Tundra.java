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

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import javax.xml.datatype.Duration;

/**
 * A collection of convenience methods for working with datetimes.
 */
public class DateTimeHelper {
    public static final String DEFAULT_DATETIME_PATTERN = "datetime";
    private static final java.util.Map<String, String> NAMED_PATTERNS = new java.util.TreeMap<String, String>();

    static {
        NAMED_PATTERNS.put("datetime.jdbc", "yyyy-MM-dd HH:mm:ss.SSS");
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
     * @param date     The Date to add the duration to.
     * @param duration The duration to be added.
     * @return         A new Date representing the given date's time plus
     *                 the given duration.
     */
    public static Date add(Date date, Duration duration) {
        return toDate(add(toCalendar(date), duration));
    }

    /**
     * Adds the given XML duration to the given datetime.
     * 
     * @param calendar The calendar to add the duration to.
     * @param duration The duration to be added.
     * @return         A new calendar representing the given calendar's time plus 
     *                 the given duration.
     */
    public static Calendar add(Calendar calendar, Duration duration) {
        if (calendar == null || duration == null) return calendar;

        try {
            java.util.GregorianCalendar gcal = new java.util.GregorianCalendar();
            gcal.setTime(calendar.getTime());
            gcal.setTimeZone(calendar.getTimeZone());
            javax.xml.datatype.XMLGregorianCalendar xcal = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            xcal.add(duration);

            calendar = xcal.toGregorianCalendar();
        } catch (javax.xml.datatype.DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }

        return calendar;
    }

    /**
     * Adds the given duration to the given datetime.
     *
     * @param datetime          The datetime to add the duration to.
     * @param datetimePattern   The pattern the given datetime conforms to.
     * @param duration          The duration to be added.
     * @param durationPattern   The pattern the given duration conforms to.
     * @return                  A new datetime representing the given datetime plus the given duration.
     */
    public static String add(String datetime, String datetimePattern, String duration, String durationPattern) {
        return emit(add(parse(datetime, datetimePattern), DurationHelper.parse(duration, durationPattern)), datetimePattern);
    }

    /**
     * Subtracts the given XML duration from the given datetime.
     *
     * @param date     The date to subtract the duration from.
     * @param duration The duration to be subtracted.
     * @return         A new calendar representing the given date's time minus
     *                 the given duration.
     */
    public static Date subtract(Date date, Duration duration) {
        return toDate(subtract(toCalendar(date), duration));
    }

    /**
     * Subtracts the given XML duration from the given datetime.
     *
     * @param calendar The calendar to subtract the duration from.
     * @param duration The duration to be subtracted.
     * @return         A new calendar representing the given calendar's time minus
     *                 the given duration.
     */
    public static Calendar subtract(Calendar calendar, Duration duration) {
        if (duration != null) duration = duration.negate();
        return add(calendar, duration);
    }

    /**
     * Returns the current datetime minus the given duration.
     * @param duration The duration to be subtracted from the current datetime.
     * @return         The current datetime minus the given duration.
     */
    public static Calendar earlier(Duration duration) {
        return subtract(Calendar.getInstance(), duration);
    }

    /**
     * Returns the current datetime plus the given duration.
     * @param duration The duration to be added from the current datetime.
     * @return         The current datetime plus the given duration.
     */
    public static Calendar later(Duration duration) {
        return add(Calendar.getInstance(), duration);
    }

    /**
     * Compares two datetimes.
     * 
     * @param firstCalendar     The first calendar to compare.
     * @param secondCalendar    The second calendar to compare.
     * @return                  Zero if both calendars represent that same instant in time, 
     *                          less than zero if the firstCalendar is an earlier instant in
     *                          time than the secondCalendar, or greater than zero if the
     *                          firstCalendar is greater than the secondCalendar.
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
     * @return                  Zero if both calendars represent that same instant in time,
     *                          less than zero if the firstCalendar is an earlier instant in
     *                          time than the secondCalendar, or greater than zero if the
     *                          firstCalendar is greater than the secondCalendar.
     */
    public static int compare(String firstDate, String firstPattern, String secondDate, String secondPattern) {
        return compare(parse(firstDate, firstPattern), parse(secondDate, secondPattern));
    }

    /**
     * Returns the duration of time between two given Calendar objects.
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
     * @param input The datetime to be serialized to an XML string.
     * @return      The given datetime serialized as an XML string.
     */
    public static String emit(Date input) {
        return emit(input, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input   The datetime to be serialized to a string.
     * @param pattern The serialization pattern to use.
     * @return        The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Date input, String pattern) {
        return emit(input, pattern, (TimeZone) null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input    The datetime to be serialized to a string.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone ID identifying the desired timezone the datetime string should use.
     * @return         The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Date input, String pattern, String timezone) {
        return emit(input, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input    The datetime to be serialized to a string.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone the datetime string should use.
     * @return         The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Date input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        return emit(toCalendar(input), pattern, timezone);
    }

    /**
     * Returns the given datetime as an XML datetime string.
     * @param input The datetime to be serialized to an XML string.
     * @return      The given datetime serialized as an XML string.
     */
    public static String emit(Calendar input) {
        return emit(input, null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input   The datetime to be serialized to a string.
     * @param pattern The serialization pattern to use.
     * @return        The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Calendar input, String pattern) {
        return emit(input, pattern, (TimeZone)null);
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input    The datetime to be serialized to a string.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone ID identifying the desired timezone the datetime string should use.
     * @return         The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Calendar input, String pattern, String timezone) {
        return emit(input, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Serializes the given datetime to a string using the given pattern.
     * @param input    The datetime to be serialized to a string.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone the datetime string should use.
     * @return         The given datetime serialized to a string using the given pattern.
     */
    public static String emit(Calendar input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        if (pattern == null) pattern = DEFAULT_DATETIME_PATTERN;

        String output = null;

        if (timezone != null) input = TimeZoneHelper.convert(input, timezone);

        if (pattern.equals("datetime") || pattern.equals("datetime.xml")) {
            output = javax.xml.bind.DatatypeConverter.printDateTime(input);
        } else if (pattern.equals("milliseconds")) {
            output = "" + input.getTimeInMillis();
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
     * @param inputs The datetimes to be serialized to XML strings.
     * @return       The given datetimes serialized as XML strings.
     */
    public static String[] emit(Date[] inputs) {
        return emit(inputs, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs  The datetimes to be serialized to strings.
     * @param pattern The serialization pattern to use.
     * @return        The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Date[] inputs, String pattern) {
        return emit(inputs, pattern, (TimeZone)null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs   The datetimes to be serialized to strings.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone ID identifying the desired timezone the datetime strings should use.
     * @return         The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Date[] inputs, String pattern, String timezone) {
        return emit(inputs, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs   The datetimes to be serialized to strings.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone the datetime strings should use.
     * @return         The given datetimes serialized to strings using the given pattern.
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
     * @param inputs The datetimes to be serialized to XML strings.
     * @return       The given datetimes serialized as XML strings.
     */
    public static String[] emit(Calendar[] inputs) {
        return emit(inputs, null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs  The datetimes to be serialized to strings.
     * @param pattern The serialization pattern to use.
     * @return        The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Calendar[] inputs, String pattern) {
        return emit(inputs, pattern, (TimeZone) null);
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs   The datetimes to be serialized to strings.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone ID identifying the desired timezone the datetime strings should use.
     * @return         The given datetimes serialized to strings using the given pattern.
     */
    public static String[] emit(Calendar[] inputs, String pattern, String timezone) {
        return emit(inputs, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Serializes the given datetimes to strings using the given pattern.
     * @param inputs   The datetimes to be serialized to strings.
     * @param pattern  The serialization pattern to use.
     * @param timezone The timezone the datetime strings should use.
     * @return         The given datetimes serialized to strings using the given pattern.
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
     * Parses an XML datetime string and returns a Calendar object.
     * @param input The XML datetime string to be parsed.
     * @return      A Calendar object representing the parsed XML datetime string.
     */
    public static Calendar parse(String input) {
        return parse(input, (String) null);
    }

    /**
     * Parses a datetime string that adheres to the given pattern and returns a Calendar object.
     * @param input   The datetime string to be parsed.
     * @param pattern The datetime pattern the given string adheres to.
     * @return        A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String pattern) {
        return parse(input, pattern, (TimeZone) null);
    }

    /**
     * Parses a datetime string that adheres to the given pattern and returns a Calendar object.
     * @param input    The datetime string to be parsed.
     * @param pattern  The datetime pattern the given string adheres to.
     * @param timezone The time zone ID identifying the time zone into which the
     *                 parsed string will be forced.
     * @return         A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String pattern, String timezone) {
        return parse(input, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Parses a datetime string that adheres to the given pattern and returns a Calendar object.
     * @param input    The datetime string to be parsed.
     * @param pattern  The datetime pattern the given string adheres to.
     * @param timezone The time zone into which the parsed string will be forced.
     * @return         A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String pattern, TimeZone timezone) {
        if (input == null) return null;
        if (pattern == null) pattern = DEFAULT_DATETIME_PATTERN;

        Calendar output = null;

        try {
            if (pattern.equals("datetime") || pattern.equals("datetime.xml")) {
                output = javax.xml.bind.DatatypeConverter.parseDateTime(input);
            } else if (pattern.equals("datetime.jdbc")) {
                output = Calendar.getInstance();
                output.setTime(Timestamp.valueOf(input));
            } else if (pattern.equals("date") || pattern.equals("date.xml")) {
                output = javax.xml.bind.DatatypeConverter.parseDate(input);
            } else if (pattern.equals("time") || pattern.equals("time.xml")) {
                output = javax.xml.bind.DatatypeConverter.parseTime(input);
            } else if (pattern.equals("milliseconds")) {
                output = Calendar.getInstance();
                output.setTimeInMillis(Long.parseLong(input));
            } else {
                if (NAMED_PATTERNS.containsKey(pattern)) pattern = NAMED_PATTERNS.get(pattern);

                DateFormat formatter = new SimpleDateFormat(pattern);
                formatter.setLenient(false);
                output = Calendar.getInstance();
                output.setTime(formatter.parse(input));
            }

            if (timezone != null) output = TimeZoneHelper.replace(output, timezone);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unparseable datetime: '" + input + "' does not conform to pattern '" + pattern + "'", ex);
        }

        return output;
    }

    /**
     * Parses a datetime string that adheres to one of the given patterns and
     * returns a Calendar object.
     * @param input    The datetime string to be parsed.
     * @param patterns A list of datetime patterns the given string might adhere to.
     * @return         A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String[] patterns) {
        return parse(input, patterns, (String)null);
    }

    /**
     * Parses a datetime string that adheres to one of the given patterns and
     * returns a Calendar object.
     * @param input    The datetime string to be parsed.
     * @param patterns A list of datetime patterns the given string might adhere to.
     * @param timezone The time zone ID identifying the time zone into which the
     *                 parsed string will be forced.
     * @return         A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String[] patterns, String timezone) {
        return parse(input, patterns, TimeZoneHelper.get(timezone));
    }

    /**
     * Parses a datetime string that adheres to one of the given patterns and
     * returns a Calendar object.
     * @param input    The datetime string to be parsed.
     * @param patterns A list of datetime patterns the given string might adhere to.
     * @param timezone The time zone into which the parsed string will be forced.
     * @return         A Calendar object representing the parsed datetime string.
     */
    public static Calendar parse(String input, String[] patterns, TimeZone timezone) {
        if (input == null) return null;
        if (patterns == null) patterns = new String[1];

        Calendar output = null;

        boolean parsed = false;
        for (String pattern : patterns) {
            try {
                output = parse(input, pattern, timezone);
                parsed = true;
                break;
            } catch (IllegalArgumentException ex) {
                // ignore
            }
        }
        if (!parsed) throw new IllegalArgumentException("Unparseable datetime: '" + input + "' does not conform to patterns [" + ArrayHelper.join(patterns, ", ") + "]");

        return output;
    }

    /**
     * Parses a list of XML datetime strings and returns a list of Calendar objects.
     * @param inputs The list of XML datetime strings to be parsed.
     * @return       A list of Calendar objects representing the parsed XML datetime strings.
     */
    public static Calendar[] parse(String[] inputs) {
        return parse(inputs, (String)null);
    }

    /**
     * Parses a list of datetime strings that adhere to the given pattern and
     * returns a list of Calendar objects.
     * @param inputs  The list of datetime strings to be parsed.
     * @param pattern The datetime pattern the given string adheres to.
     * @return        A list of Calendar objects representing the parsed
     *                datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String pattern) {
        return parse(inputs, pattern, (TimeZone)null);
    }

    /**
     * Parses a list of datetime strings that adhere to the given pattern and
     * returns a list of Calendar objects.
     * @param inputs   The list of datetime strings to be parsed.
     * @param pattern  The datetime pattern the given string adheres to.
     * @param timezone The time zone ID identifying the time zone into which the
     *                 parsed string will be forced.
     * @return         A list of Calendar objects representing the parsed
     *                 datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String pattern, String timezone) {
        return parse(inputs, pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Parses a list of datetime strings that adhere to the given pattern and
     * returns a list of Calendar objects.
     * @param inputs   The list of datetime strings to be parsed.
     * @param pattern  The datetime pattern the given string adheres to.
     * @param timezone The time zone into which the parsed string will be forced.
     * @return         A list of Calendar objects representing the parsed
     *                 datetime strings.
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
     * Parses a list of datetime strings that adhere to one of patterns in the given the
     * list and returns a list of Calendar objects.
     * @param inputs   The list of datetime strings to be parsed.
     * @param patterns The list of datetime patterns the given strings might adhere to.
     * @return         A list of Calendar objects representing the parsed
     *                 datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String[] patterns) {
        return parse(inputs, patterns, (TimeZone) null);
    }

    /**
     * Parses a list of datetime strings that adhere to one of patterns in the given the
     * list and returns a list of Calendar objects.
     * @param inputs   The list of datetime strings to be parsed.
     * @param patterns The list of datetime patterns the given strings might adhere to.
     * @param timezone The time zone ID identifying the time zone into which the
     *                 parsed string will be forced.
     * @return         A list of Calendar objects representing the parsed
     *                 datetime strings.
     */
    public static Calendar[] parse(String[] inputs, String[] patterns, String timezone) {
        return parse(inputs, patterns, TimeZoneHelper.get(timezone));
    }

    /**
     * Parses a list of datetime strings that adhere to one of patterns in the given the
     * list and returns a list of Calendar objects.
     * @param inputs   The list of datetime strings to be parsed.
     * @param patterns The list of datetime patterns the given strings might adhere to.
     * @param timezone The time zone into which the parsed string will be forced.
     * @return         A list of Calendar objects representing the parsed
     *                 datetime strings.
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
     * Returns the current datetime as an XML datetime string.
     * @return The current datetime as an XML datetime string.
     */
    public static String now() {
        return now(null);
    }

    /**
     * Returns the current datetime as a string formatted according to the given pattern.
     * @param pattern The serialization pattern to use.
     * @return        The current datetime as a string formatted according to the given pattern.
     */
    public static String now(String pattern) {
        return now(pattern, (TimeZone) null);
    }

    /**
     * Returns the current datetime as a string formatted according to the given pattern.
     * @param pattern  The serialization pattern to use.
     * @param timezone The time zone ID identifying the time zone the current datetime should be returned in.
     * @return         The current datetime as a string formatted according to the given pattern.
     */
    public static String now(String pattern, String timezone) {
        return now(pattern, TimeZoneHelper.get(timezone));
    }

    /**
     * Returns the current datetime as a string formatted according to the given pattern.
     * @param pattern  The serialization pattern to use.
     * @param timezone The time zone the current datetime should be returned in.
     * @return         The current datetime as a string formatted according to the given pattern.
     */
    public static String now(String pattern, TimeZone timezone) {
        return emit(Calendar.getInstance(), pattern, timezone);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input      The datetime string to be reformatted.
     * @param inPattern  The pattern the given input datetime string adheres to.
     * @param outPattern The pattern the datetime string should be reformatted as.
     * @return           The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String inPattern, String outPattern) {
        return format(input, inPattern, (TimeZone) null, outPattern, (TimeZone) null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input       The datetime string to be reformatted.
     * @param inPattern   The pattern the given input datetime string adheres to.
     * @param inTimeZone  The time zone ID identifying the time zone the given datetime string should be parsed in.
     * @param outPattern  The pattern the datetime string should be reformatted as.
     * @param outTimeZone The time zone ID identifying the time zone the returned datetime string should be in.
     * @return            The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String inPattern, String inTimeZone, String outPattern, String outTimeZone) {
        return format(input, inPattern, TimeZoneHelper.get(inTimeZone), outPattern, TimeZoneHelper.get(outTimeZone));
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input       The datetime string to be reformatted.
     * @param inPattern   The pattern the given input datetime string adheres to.
     * @param inTimeZone  The time zone the given datetime string should be parsed in.
     * @param outPattern  The pattern the datetime string should be reformatted as.
     * @param outTimeZone The time zone the returned datetime string should be in.
     * @return            The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String inPattern, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return emit(parse(input, inPattern, inTimeZone), outPattern, outTimeZone);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input      The datetime string to be reformatted.
     * @param inPatterns The list of patterns the given input datetime string might adhere to.
     * @param outPattern The pattern the datetime string should be reformatted as.
     * @return           The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String[] inPatterns, String outPattern) {
        return format(input, inPatterns, (TimeZone) null, outPattern, (TimeZone) null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input      The datetime string to be reformatted.
     * @param inPatterns The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone  The time zone ID identifying the time zone the given datetime string should be parsed in.
     * @param outPattern The pattern the datetime string should be reformatted as.
     * @param outTimeZone The time zone ID identifying the time zone the returned datetime string should be in.
     * @return           The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String[] inPatterns, String inTimeZone, String outPattern, String outTimeZone) {
        return format(input, inPatterns, TimeZoneHelper.get(inTimeZone), outPattern, TimeZoneHelper.get(outTimeZone));
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param input      The datetime string to be reformatted.
     * @param inPatterns The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone The time zone the given datetime string should be parsed in.
     * @param outPattern The pattern the datetime string should be reformatted as.
     * @param outTimeZone The time zone the returned datetime string should be in.
     * @return           The given datetime string reformatted according to the given outPattern.
     */
    public static String format(String input, String[] inPatterns, TimeZone inTimeZone, String outPattern, TimeZone outTimeZone) {
        return emit(parse(input, inPatterns, inTimeZone), outPattern, outTimeZone);
    }

    /**
     * Reformats the given list of datetime strings according to the desired pattern.
     * @param inputs     The list of datetime strings to be reformatted.
     * @param inPattern  The pattern the given input datetime strings adhere to.
     * @param outPattern The pattern the datetime strings should be reformatted as.
     * @return           The given datetime strings reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String inPattern, String outPattern) {
        return format(inputs, inPattern, (TimeZone) null, outPattern, (TimeZone) null);
    }

    /**
     * Reformats the given list of datetime strings according to the desired pattern.
     * @param inputs      The list of datetime strings to be reformatted.
     * @param inPattern   The pattern the given input datetime strings adhere to.
     * @param inTimeZone  The time zone ID identifying the time zone the given datetime string should be parsed in.
     * @param outPattern  The pattern the datetime strings should be reformatted as.
     * @param outTimeZone The time zone ID identifying the time zone the returned datetime string should be in.
     * @return            The given datetime strings reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String inPattern, String inTimeZone, String outPattern, String outTimeZone) {
        return format(inputs, inPattern, TimeZoneHelper.get(inTimeZone), outPattern, TimeZoneHelper.get(outTimeZone));
    }

    /**
     * Reformats the given list of datetime strings according to the desired pattern.
     * @param inputs      The list of datetime strings to be reformatted.
     * @param inPattern   The pattern the given input datetime strings adhere to.
     * @param inTimeZone  The time zone the given datetime string should be parsed in.
     * @param outPattern  The pattern the datetime strings should be reformatted as.
     * @param outTimeZone The time zone the returned datetime string should be in.
     * @return            The given datetime strings reformatted according to the given outPattern.
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
     * @param inputs     The list of datetime strings to be reformatted.
     * @param inPatterns The list of patterns the given input datetime string might adhere to.
     * @param outPattern The pattern the datetime string should be reformatted as.
     * @return           The given datetime string reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String[] inPatterns, String outPattern) {
        return format(inputs, inPatterns, (TimeZone) null, outPattern, (TimeZone) null);
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param inputs         The list of datetime strings to be reformatted.
     * @param inPatterns     The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone     The time zone ID identifying the time zone the given datetime string should be parsed in.
     * @param outPattern     The pattern the datetime string should be reformatted as.
     * @param outTimeZone    The time zone ID identifying the time zone the returned datetime string should be in.
     * @return               The given datetime string reformatted according to the given outPattern.
     */
    public static String[] format(String[] inputs, String[] inPatterns, String inTimeZone, String outPattern, String outTimeZone) {
        return format(inputs, inPatterns, TimeZoneHelper.get(inTimeZone), outPattern, TimeZoneHelper.get(outTimeZone));
    }

    /**
     * Reformats the given datetime string according to the desired pattern.
     * @param inputs         The list of datetime strings to be reformatted.
     * @param inPatterns     The list of patterns the given input datetime string might adhere to.
     * @param inTimeZone     The time zone the given datetime string should be parsed in.
     * @param outPattern     The pattern the datetime string should be reformatted as.
     * @param outTimeZone    The time zone the returned datetime string should be in.
     * @return               The given datetime string reformatted according to the given outPattern.
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
     * Converts the given Date object to a Calendar object.
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
     * @param input The Calendar object to be converted.
     * @return      The Date object representing the given Calendar object.
     */
    public static Date toDate(Calendar input) {
        if (input == null) return null;
        return input.getTime();
    }
}
