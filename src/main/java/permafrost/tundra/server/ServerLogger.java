package permafrost.tundra.server;

import com.wm.app.log.impl.sc.LevelTranslator;
import com.wm.util.JournalLogger;
import org.apache.log4j.Level;
import java.text.MessageFormat;

/**
 * Convenience methods for logging to the Integration Server server log.
 */
public class ServerLogger {
    /**
     * Disallow instantiation of this class.
     */
    private ServerLogger() {}

    /**
     * Logs the given message formatted with the given arguments at the fatal level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void fatal(String message, Object... arguments) {
        log(Level.FATAL, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the fatal level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void fatal(String function, String message, Object... arguments) {
        log(Level.FATAL, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the error level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void error(String message, Object... arguments) {
        log(Level.ERROR, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the error level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void error(String function, String message, Object... arguments) {
        log(Level.ERROR, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the warn level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void warn(String message, Object... arguments) {
        log(Level.WARN, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the warn level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void warn(String function, String message, Object... arguments) {
        log(Level.WARN, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the info level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void info(String message, Object... arguments) {
        log(Level.INFO, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the info level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void info(String function, String message, Object... arguments) {
        log(Level.INFO, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the debug level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void debug(String message, Object... arguments) {
        log(Level.DEBUG, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the debug level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void debug(String function, String message, Object... arguments) {
        log(Level.DEBUG, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the trace level.
     *
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void trace(String message, Object... arguments) {
        log(Level.TRACE, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the trace level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void trace(String function, String message, Object... arguments) {
        log(Level.TRACE, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments function at the given level.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(String level, String message, Object... arguments) {
        log(level, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(String level, String function, String message, Object... arguments) {
        log(getLevel(level), function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the given level.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(Level level, String message, Object... arguments) {
        log(level, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(Level level, String function, String message, Object... arguments) {
        log(getLevel(level), function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments at the given level.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(int level, String message, Object... arguments) {
        log(level, null, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(int level, String function, String message, Object... arguments) {
        JournalLogger.log(4, 90, level, function, (arguments != null && arguments.length > 0) ? MessageFormat.format(message, arguments) : message);
    }

    /**
     * Returns the log level as an integer given the level as an enumeration value.
     *
     * @param level The logging level to convert.
     * @return      The integer representation of the given level.
     */
    private static int getLevel(Level level) {
        return LevelTranslator.findISLevelCode(level);
    }

    /**
     * Returns the log level as an integer given the level as a String.
     *
     * @param levelString The logging level to convert.
     * @return            The integer representation of the given level.
     */
    private static int getLevel(String levelString) {
        int level;
        try {
            level = Integer.parseInt(levelString);
        } catch(NumberFormatException ex) {
            level = LevelTranslator.findISLevelCode(Level.toLevel(levelString));
        }
        return level;
    }
}
