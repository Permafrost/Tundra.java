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

package permafrost.tundra.server;

import com.wm.app.log.impl.sc.LevelTranslator;
import org.apache.log4j.Level;
import java.util.Locale;

/**
 * Convenience methods for working with logging levels.
 */
public class ServerLogLevelHelper {
    /**
     * The default logging level when none is specified.
     */
    public static final Level DEFAULT_LOG_LEVEL = Level.DEBUG;
    /**
     * The default logging level as an integer.
     */
    private static final int DEFAULT_LOG_LEVEL_INT = LevelTranslator.findISLevelCode(DEFAULT_LOG_LEVEL);

    /**
     * Returns the logging Level for the given integer.
     *
     * @param level The integer logging level.
     * @return      The Level that represents this integer.
     */
    public static Level toLevel(int level) {
        return LevelTranslator.findLog4jLevel(level);
    }

    /**
     * Returns the logging Level for the given string.
     *
     * @param level The string logging level.
     * @return      The Level that represents this string.
     */
    public static Level toLevel(String level) {
        return Level.toLevel(level == null ? null : level.toUpperCase(Locale.ENGLISH), DEFAULT_LOG_LEVEL);
    }

    /**
     * Returns the log level as an integer given the level as an enumeration value.
     *
     * @param level The logging level to convert.
     * @return      The integer representation of the given level.
     */
    public static int fromLevel(Level level) {
        return level == null ? DEFAULT_LOG_LEVEL_INT : LevelTranslator.findISLevelCode(level);
    }

    /**
     * Returns the log level as an integer given the level as a String.
     *
     * @param levelString The logging level to convert.
     * @return            The integer representation of the given level.
     */
    public static int fromLevel(String levelString) {
        int level;
        if (levelString != null) {
            try {
                level = Integer.parseInt(levelString);
            } catch (NumberFormatException ex) {
                level = fromLevel(toLevel(levelString));
            }
        } else {
            level = fromLevel(toLevel(levelString));
        }
        return level;
    }
}
