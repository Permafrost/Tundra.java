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

/**
 * A collection of convenience methods for working with time zones.
 */
public class TimeZoneHelper {
    private static java.util.SortedSet<String> ZONES = new java.util.TreeSet(java.util.Arrays.asList(java.util.TimeZone.getAvailableIDs()));
    private static java.util.regex.Pattern OFFSET_HHMM_PATTERN = java.util.regex.Pattern.compile("([\\+-])?(\\d?\\d):(\\d\\d)");
    private static java.util.regex.Pattern OFFSET_XML_PATTERN = java.util.regex.Pattern.compile("-?P(\\d+|T\\d+).+");
    private static java.util.regex.Pattern OFFSET_RAW_PATTERN = java.util.regex.Pattern.compile("[\\+-]?\\d+");

    /**
     * Disallow instantiation of this class.
     */
    private TimeZoneHelper() {}

    /**
     * Returns the time zone associated with the given ID.
     * @param id A time zone ID.
     * @return   The time zone associated with the given ID.
     */
    public static java.util.TimeZone get(String id) {
        if (id == null) return null;

        java.util.TimeZone timezone = null;

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
                timezone = java.util.TimeZone.getTimeZone(id);
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
        String[] candidates = java.util.TimeZone.getAvailableIDs(offset);
        if (candidates != null && candidates.length > 0) id = candidates[0]; // default to the first candidate timezone ID
        return id;
    }

    /**
     * @return The JVM's default time zone.
     */
    public static java.util.TimeZone self() {
        return java.util.TimeZone.getDefault();
    }

    /**
     * @return All time zones known to the JVM.
     */
    public static java.util.TimeZone[] list() {
        String[] id = java.util.TimeZone.getAvailableIDs();
        java.util.TimeZone[] zones = new java.util.TimeZone[id.length];

        for (int i = 0; i < id.length; i++) {
            zones[i] = get(id[i]);
        }

        return zones;
    }

    /**
     * Converts the given calendar to the given time zone.
     * @param input     The calendar to be coverted to another time zone.
     * @param timezone  The time zone ID identifying the time zone the calendar
     *                  will be converted to.
     * @return          A new calendar representing the same instant in time
     *                  as the given calendar but in the given time.
     */
    public static java.util.Calendar convert(java.util.Calendar input, String timezone) {
        return convert(input, get(timezone));
    }

    /**
     * Converts the given calendar to the given time zone.
     * @param input     The calendar to be converted to another time zone.
     * @param timezone  The time zone the calendar will be converted to.
     * @return          A new calendar representing the same instant in time
     *                  as the given calendar but in the given time.
     */
    public static java.util.Calendar convert(java.util.Calendar input, java.util.TimeZone timezone) {
        if (input == null || timezone == null) return input;

        java.util.Calendar output = java.util.Calendar.getInstance(timezone);
        output.setTimeInMillis(input.getTimeInMillis());
        return output;
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     * @param input     The calendar to replace the time zone on.
     * @param timezone  A time zone ID identifying the time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static java.util.Calendar replace(java.util.Calendar input, String timezone) {
        return replace(input, get(timezone));
    }

    /**
     * Replaces the time zone on the given calendar with the given time zone.
     * @param input     The calendar to replace the time zone on.
     * @param timezone  The new time zone the calendar will be forced into.
     * @return          A new calendar that has been forced into a new time zone.
     */
    public static java.util.Calendar replace(java.util.Calendar input, java.util.TimeZone timezone) {
        if (input == null || timezone == null) return input;

        long instant = input.getTimeInMillis();
        java.util.TimeZone currentZone = input.getTimeZone();
        int currentOffset = currentZone.getOffset(instant);
        int desiredOffset = timezone.getOffset(instant);

        // reset instant to UTC time then force it to input timezone
        instant = instant + currentOffset - desiredOffset;

        // convert to output zone
        java.util.Calendar output = java.util.Calendar.getInstance(timezone);
        output.setTimeInMillis(instant);

        return output;
    }
}
