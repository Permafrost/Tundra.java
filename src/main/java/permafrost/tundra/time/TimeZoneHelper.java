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
import permafrost.tundra.data.IDataMap;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with time zones.
 */
public final class TimeZoneHelper {
    /**
     * A sorted set of all time zone IDs known to the JVM.
     */
    protected static final SortedSet<String> ZONES = new TreeSet<String>(Arrays.asList(TimeZone.getAvailableIDs()));
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
     * The UTC time zone.
     */
    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    /**
     * The default time zone used by Tundra.
     */
    public static final TimeZone DEFAULT_TIME_ZONE = UTC_TIME_ZONE;

    /**
     * Disallow instantiation of this class.
     */
    private TimeZoneHelper() {}

    /**
     * Returns the time zone associated with the given ID.
     *
     * @param id    A time zone ID.
     * @return      The time zone associated with the given ID.
     */
    public static TimeZone get(String id) {
        if (id == null) return null;

        TimeZone timezone = null;

        if (id.equals("$default") || id.equalsIgnoreCase("local") || id.equalsIgnoreCase("self")) {
            timezone = self();
        } else {
            if (id.equals("Z")) {
                timezone = TimeZone.getTimeZone("UTC");
            } else {
                java.util.regex.Matcher matcher = OFFSET_HHMM_PATTERN.matcher(id);
                if (matcher.matches()) {
                    String sign = matcher.group(1);
                    String hours = matcher.group(2);
                    String minutes = matcher.group(3);

                    int offset = Integer.parseInt(hours) * 60 * 60 * 1000 + Integer.parseInt(minutes) * 60 * 1000;
                    if (sign != null && sign.equals("-")) offset = offset * -1;

                    timezone = get(offset);
                } else {
                    matcher = OFFSET_XML_PATTERN.matcher(id);
                    if (matcher.matches()) {
                        try {
                            timezone = get(Integer.parseInt(DurationHelper.format(id, "xml", "milliseconds")));
                        } catch (NumberFormatException ex) {
                            // ignore
                        }
                    } else {
                        matcher = OFFSET_RAW_PATTERN.matcher(id);
                        if (matcher.matches()) {
                            // try parsing the id as a raw millisecond offset
                            try {
                                timezone = get(Integer.parseInt(id));
                            } catch (NumberFormatException ex) {
                                // ignore
                            }
                        } else if (ZONES.contains(id)) {
                            timezone = TimeZone.getTimeZone(id);
                        }
                    }
                }
            }
        }

        if (timezone == null) throw new IllegalArgumentException("Unknown time zone specified: '" + id + "'");

        return timezone;
    }

    /**
     * Returns the first matching time zone ID for the given raw millisecond time zone offset.
     *
     * @param offset    A time zone offset in milliseconds.
     * @return          The ID of the first matching time zone with the given offset.
     */
    protected static TimeZone get(int offset) {
        DecimalFormat decimalFormat = new DecimalFormat("00");

        String sign = offset < 0 ? "-" : "+";
        int hours = Math.abs(offset / (1000 * 60 * 60));
        int minutes = Math.abs(offset / (1000 * 60)) - (hours * 60);

        String timezoneID = "GMT" + sign + decimalFormat.format(hours) + ":" + decimalFormat.format(minutes);

        return TimeZone.getTimeZone(timezoneID);
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
     *
     * @param calendar  The Calendar object to be normalized.
     * @return          The given Calendar object converted to the default time zone.
     */
    public static Calendar normalize(Calendar calendar) {
        return convert(calendar, DEFAULT_TIME_ZONE);
    }

    /**
     * Converts the given calendar to the given time zone.
     *
     * @param input     The calendar to be coverted to another time zone.
     * @param timezone  The time zone ID identifying the time zone the calendar will be converted to.
     * @return          A new calendar representing the same instant in time as the given calendar but in the given time.
     */
    public static Calendar convert(Calendar input, String timezone) {
        return convert(input, get(timezone));
    }

    /**
     * Converts the given calendar to the given time zone.
     *
     * @param input     The calendar to be converted to another time zone.
     * @param timezone  The time zone the calendar will be converted to.
     * @return          A new calendar representing the same instant in time as the given calendar but in the given time.
     */
    public static Calendar convert(Calendar input, TimeZone timezone) {
        if (input == null || timezone == null || timezone.equals(input.getTimeZone())) return input;

        Calendar output = Calendar.getInstance(timezone);
        output.setTimeInMillis(input.getTimeInMillis());
        return output;
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     *
     * @param input     The calendar to replace the time zone on.
     * @param timezone  A time zone ID identifying the time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static Calendar replace(Calendar input, String timezone) {
        return replace(input, get(timezone));
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     *
     * @param input     The calendar to replace the time zone on.
     * @param timezone  The new time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static Calendar replace(Calendar input, TimeZone timezone) {
        if (input == null || timezone == null || timezone.equals(input.getTimeZone())) return input;

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

    /**
     * Returns an IData representation of the given TimeZone object, using the given datetime to resolve whether
     * daylight savings is active.
     *
     * @param timezone  The TimeZone object to be converted to an IData representation.
     * @param instant   The datetime used to resolve the status of daylight savings.
     * @return          An IData representation of the given TimeZone object.
     */
    public static IData toIData(TimeZone timezone, Calendar instant) {
        if (timezone == null) return null;
        if (instant == null) instant = Calendar.getInstance();

        Date dateInstant = instant.getTime();
        IDataMap output = new IDataMap();

        boolean dstActive = timezone.inDaylightTime(dateInstant);

        output.put("id", timezone.getID());
        output.put("name", timezone.getDisplayName(dstActive, TimeZone.SHORT));
        output.put("description", timezone.getDisplayName(dstActive, TimeZone.LONG));
        output.put("utc.offset", DurationHelper.format(timezone.getOffset(dateInstant.getTime()), DurationPattern.XML));
        output.put("dst.used?", "" + timezone.useDaylightTime());
        output.put("dst.active?", "" + dstActive);
        output.put("dst.offset", DurationHelper.format(timezone.getDSTSavings(), DurationPattern.XML));

        return output;
    }

    /**
     * Returns an IData representation of the given TimeZone object, using the given datetime string to resolve whether
     * daylight savings is active.
     *
     * @param timezone  The TimeZone object to be converted to an IData representation.
     * @param datetime  The datetime string used to resolve the status of daylight savings.
     * @param pattern   The pattern to use to parse the given datetime string.
     * @return          An IData representation of the given TimeZone object.
     */
    public static IData toIData(TimeZone timezone, String datetime, String pattern) {
        return toIData(timezone, DateTimeHelper.parse(datetime, pattern));
    }

    /**
     * Returns an IData representation of the given TimeZone objects, using the given datetime to resolve whether
     * daylight savings is active.
     *
     * @param timezones A list of TimeZone objects to be converted to an IData representation.
     * @param instant   The datetime used to resolve the status of daylight savings.
     * @return          An IData[] representation of the given TimeZone objects.
     */
    public static IData[] toIDataArray(TimeZone[] timezones, Calendar instant) {
        if (timezones == null) return null;

        IData[] output = new IData[timezones.length];

        for (int i = 0; i < timezones.length; i++) {
            output[i] = toIData(timezones[i], instant);
        }

        return output;
    }

    /**
     * Returns an IData representation of the given TimeZone objects, using the given datetime string to resolve whether
     * daylight savings is active.
     *
     * @param timezones A list of TimeZone objects to be converted to an IData representation.
     * @param datetime  The datetime string used to resolve the status of daylight savings.
     * @param pattern   The pattern to use to parse the given datetime string.
     * @return          An IData[] representation of the given TimeZone objects.
     */
    public static IData[] toIDataArray(TimeZone[] timezones, String datetime, String pattern) {
        return toIDataArray(timezones, DateTimeHelper.parse(datetime, pattern));
    }
}
