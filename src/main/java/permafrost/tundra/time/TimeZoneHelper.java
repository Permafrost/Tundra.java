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

import java.util.Arrays;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with time zones.
 */
public class TimeZoneHelper {
    /**
     * A sorted set of all time zone IDs known to the JVM.
     */
    protected static final SortedSet<String> ZONES = new TreeSet(Arrays.asList(TimeZone.getAvailableIDs()));
    /**
     * Regular expression pattern for matching a time zone offset specified as HH:mm (hours and minutes).
     */
    protected static final Pattern OFFSET_HHMM_PATTERN = Pattern.compile("([\\+-])?(\\d?\\d):(\\d\\d)");
    /**
     * Regular expression pattern for matching a time zone offset specified as an XML duration string.
     */
    protected static final Pattern OFFSET_XML_PATTERN = Pattern.compile("-?P(\\d+|T\\d+).+");
    /**
     * Regular expression pattern for matching a time zone offset specified in milliseconds.
     */
    protected static final Pattern OFFSET_RAW_PATTERN = Pattern.compile("[\\+-]?\\d+");
    /**
     * The default time zone used by Tundra.
     */
    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("UTC");

    /**
     * Disallow instantiation of this class.
     */
    private TimeZoneHelper() {}

    /**
     * Returns the time zone associated with the given ID.
     * @param id A time zone ID.
     * @return   The time zone associated with the given ID.
     */
    public static TimeZone get(String id) {
        if (id == null) return null;

        TimeZone timezone = null;

        if (id.equals("$default") || id.equalsIgnoreCase("local") || id.equalsIgnoreCase("self")) {
            timezone = self();
        } else {
            if (id.equals("Z")) {
                id = "UTC";
            } else {
                java.util.regex.Matcher matcher = OFFSET_HHMM_PATTERN.matcher(id);
                if (matcher.matches()) {
                    String sign = matcher.group(1);
                    String hours = matcher.group(2);
                    String minutes = matcher.group(3);

                    int offset = Integer.parseInt(hours) * 60 * 60 * 1000 + Integer.parseInt(minutes) * 60 * 1000;
                    if (sign != null && sign.equals("-")) offset = offset * -1;

                    String candidate = get(offset);
                    if (candidate != null) id = candidate;
                } else {
                    matcher = OFFSET_XML_PATTERN.matcher(id);
                    if (matcher.matches()) {
                        try {
                            String candidate = get(Integer.parseInt(DurationHelper.format(id, "xml", "milliseconds")));
                            if (candidate != null) id = candidate;
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    } else {
                        matcher = OFFSET_RAW_PATTERN.matcher(id);
                        if (matcher.matches()) {
                            // try parsing the id as a raw millisecond offset
                            try {
                                String candidate = get(Integer.parseInt(id));
                                if (candidate != null) id = candidate;
                            } catch (NumberFormatException ex) {
                                // ignore
                            }
                        }
                    }
                }
            }

            if (ZONES.contains(id)) {
                timezone = TimeZone.getTimeZone(id);
            }
        }

        if (timezone == null) throw new IllegalArgumentException("Unknown time zone specified: '" + id + "'");

        return timezone;
    }

    /**
     * Returns the first matching time zone ID for the given raw millisecond time zone offset.
     * @param offset A time zone offset in milliseconds.
     * @return       The ID of the first matching time zone with the given offset.
     */
    protected static String get(int offset) {
        String id = null;
        String[] candidates = TimeZone.getAvailableIDs(offset);
        if (candidates != null && candidates.length > 0) id = candidates[0]; // default to the first candidate timezone ID
        return id;
    }

    /**
     * @return The JVM's default time zone.
     */
    public static TimeZone self() {
        return TimeZone.getDefault();
    }

    /**
     * @return All time zones known to the JVM.
     */
    public static TimeZone[] list() {
        String[] id = TimeZone.getAvailableIDs();
        TimeZone[] zones = new TimeZone[id.length];

        for (int i = 0; i < id.length; i++) {
            zones[i] = get(id[i]);
        }

        return zones;
    }

    /**
     * Returns the given Calendar object converted to the default time zone.
     * @param calendar  The Calendar object to be normalized.
     * @return          The given Calendar object converted to the default time zone.
     */
    public static Calendar normalize(Calendar calendar) {
        return convert(calendar, DEFAULT_TIME_ZONE);
    }

    /**
     * Converts the given calendar to the given time zone.
     * @param input     The calendar to be coverted to another time zone.
     * @param timezone  The time zone ID identifying the time zone the calendar
     *                  will be converted to.
     * @return          A new calendar representing the same instant in time
     *                  as the given calendar but in the given time.
     */
    public static Calendar convert(Calendar input, String timezone) {
        return convert(input, get(timezone));
    }

    /**
     * Converts the given calendar to the given time zone.
     * @param input     The calendar to be converted to another time zone.
     * @param timezone  The time zone the calendar will be converted to.
     * @return          A new calendar representing the same instant in time
     *                  as the given calendar but in the given time.
     */
    public static Calendar convert(Calendar input, TimeZone timezone) {
        if (input == null || timezone == null) return input;

        Calendar output = Calendar.getInstance(timezone);
        output.setTimeInMillis(input.getTimeInMillis());
        return output;
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     * @param input     The calendar to replace the time zone on.
     * @param timezone  A time zone ID identifying the time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static Calendar replace(Calendar input, String timezone) {
        return replace(input, get(timezone));
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     * @param input     The calendar to replace the time zone on.
     * @param timezone  The new time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static Calendar replace(Calendar input, TimeZone timezone) {
        if (input == null || timezone == null) return input;

        long instant = input.getTimeInMillis();
        TimeZone currentZone = input.getTimeZone();
        int currentOffset = currentZone.getOffset(instant);
        int desiredOffset = timezone.getOffset(instant);

        // reset instant to UTC time then force it to input timezone
        instant = instant + currentOffset - desiredOffset;

        // convert to output zone
        Calendar output = Calendar.getInstance(timezone);
        output.setTimeInMillis(instant);

        return output;
    }
}
