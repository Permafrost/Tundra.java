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

import java.util.HashMap;
import java.util.Map;

/**
 * The duration patterns supported by the methods on DurationHelper.
 */
public enum DurationPattern {
    XML(0), MILLISECONDS(1), SECONDS(2), MINUTES(3), HOURS(4), DAYS(5), WEEKS(6), MONTHS(7), YEARS(8);

    /**
     * The default duration pattern used by Tundra.
     */
    public static final DurationPattern DEFAULT_DURATION_PATTERN = XML;

    private int value;
    private static Map<Integer, DurationPattern> map = new HashMap<Integer, DurationPattern>();

    DurationPattern(int value) {
        this.value = value;
    }

    static {
        for (DurationPattern type : DurationPattern.values()) {
            map.put(type.value, type);
        }
    }

    /**
     * Returns the enumeration value associated with the given integer.
     * @param value The integer identifying the enumeration value to be returned.
     * @return      Null if the given integer does not identify an enumeration constant, otherwise
     *              the associated enumeration constant.
     */
    public static DurationPattern valueOf(int value) {
        return map.get(value);
    }

    /**
     * Returns the enumeration value associated with the given name.
     * @param name  The name of the enumeration constant to be returned.
     * @return      The enumeration value associated with the given name, or the default value if no value
     *              is associated with the given name.
     */
    public static DurationPattern normalize(String name) {
        return normalize(name == null ? (DurationPattern)null : valueOf(name.trim().toUpperCase()));
    }

    /**
     * Normalizes the given pattern.
     * @param pattern The pattern to be normalized.
     * @return        The given pattern if not null, or the default pattern.
     */
    public static DurationPattern normalize(DurationPattern pattern) {
        return pattern == null ? DEFAULT_DURATION_PATTERN : pattern;
    }
}
