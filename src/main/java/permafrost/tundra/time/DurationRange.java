/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

import javax.xml.datatype.Duration;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an inclusive duration range against which datetimes can be resolved as within or without the range.
 */
public class DurationRange {
    /**
     * The regular expression pattern used to parse duration range strings.
     */
    protected static final Pattern DURATION_RANGE_PATTERN = Pattern.compile("^((-)?P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+(\\.\\d+)?S)?)?)?(\\.\\.((-)?P(\\d+Y)?(\\d+M)?(\\d+D)?(T(\\d+H)?(\\d+M)?(\\d+(\\.\\d+)?S)?)?)?)?$");
    /**
     * The start and end durations of this range. If start is null, this is a beginless range. If end is null, this is
     * an endless range.
     */
    protected Duration start, end;

    /**
     * Creates a new DurationRange.
     *
     * @param start The start of the range.
     * @param end   The end of the range.
     */
    public DurationRange(Duration start, Duration end) {
        this.start = start;
        this.end = end;

        if (this.start != null && this.end != null) {
            if (this.start.compare(this.end) > 0) {
                throw new IllegalArgumentException("duration range start is required to be smaller than range end: " + this.toString());
            }
        }
    }

    /**
     * Returns true if the given instance in time is within the given duration range when resolved against current time.
     *
     * @param instance  The instance in time to resolve against this duration range.
     * @return          True if the given instance is within the given range when resolved against current time.
     */
    public boolean within(Calendar instance) {
        return within(instance, null);
    }

    /**
     * Returns true if the given instance in time is within the given duration range when resolved against the given
     * epoch.
     *
     * @param instance  The instance in time to resolve against this duration range.
     * @param epoch     The epoch against which the duration range is resolved in absolute time. If null, the current
     *                  time is used.
     * @return          True if the given instance is within the given range when resolved against the given epoch.
     */
    public boolean within(Calendar instance, Calendar epoch) {
        boolean within = false;
        if (instance != null) {
            if (start == null && end == null) {
                within = true;
            } else {
                Calendar startTime = null, endTime = null;

                if (epoch == null) {
                    epoch = DateTimeHelper.now();
                }

                if (start != null) {
                    startTime = DateTimeHelper.add(epoch, start);
                }

                if (end != null) {
                    endTime = DateTimeHelper.add(epoch, end);
                }

                within = DateTimeHelper.within(instance, startTime, endTime);
            }
        }
        return within;
    }

    /**
     * Parses an inclusive duration range string in the format <start duration>..<end duration>.
     *
     * @param durationRangeString   The string to parse.
     * @return                      The parsed duration range.
     */
    public static DurationRange parse(String durationRangeString) {
        Duration start = null, end = null;

        if (durationRangeString != null && !"".equals(durationRangeString)) {
            Matcher matcher = DURATION_RANGE_PATTERN.matcher(durationRangeString);
            if (matcher.matches()) {
                String startString = matcher.group(1);
                if (startString != null) {
                    start = DurationHelper.parse(startString);
                }
                String endString = matcher.group(12);
                if (endString != null) {
                    end = DurationHelper.parse(endString);
                }
            }
        }

        return new DurationRange(start, end);
    }

    /**
     * Returns a string representation of this duration range.
     *
     * @return a string representation of this duration range.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (start == null && end == null) {
            builder.append("..");
        } else if (start == null) {
            builder.append("..");
            builder.append(DurationHelper.emit(end));
        } else if (end == null) {
            builder.append(DurationHelper.emit(start));
            builder.append("..");
        } else {
            builder.append(DurationHelper.emit(start));
            builder.append("..");
            builder.append(DurationHelper.emit(end));
        }

        return builder.toString();
    }
}
