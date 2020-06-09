package permafrost.tundra.server;

import com.wm.app.b2b.server.ServiceException;
import com.wm.app.log.impl.sc.LevelTranslator;
import com.wm.data.IData;
import com.wm.lang.ns.NSService;
import com.wm.util.JournalLogger;
import org.apache.log4j.Level;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.lang.IterableHelper;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

/**
 * Convenience methods for logging to the Integration Server server log.
 */
public class ServerLogger {
    /**
     * The default logging level when none is specified.
     */
    public static final Level DEFAULT_LOG_LEVEL = Level.DEBUG;

    /**
     * Disallow instantiation of this class.
     */
    private ServerLogger() {}

    /**
     * Logs the given message and context automatically prefixed by the current user and callstack.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     */
    public static void log(String level, String message, IData context) {
        log(fromLevel(level), message, context);
    }

    /**
     * Logs the given message and context automatically prefixed by the current user and callstack.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     */
    public static void log(Level level, String message, IData context) {
        log(fromLevel(level), message, context);
    }

    /**
     * Logs the given message and context automatically prefixed by the current user and callstack.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     */
    public static void log(int level, String message, IData context) {
        String user = UserHelper.getCurrentName();

        if (context != null) {
            try {
                IDataJSONParser parser = new IDataJSONParser(false);
                String contextString = parser.emit(context, String.class);

                if (message == null || message.equals("")) {
                    message = contextString;
                } else {
                    message = message + " -- " + contextString;
                }
            } catch(IOException ex) {
                // do nothing, we should never get this exception
            } catch(ServiceException ex) {
                // do nothing, we should never get this exception
            }
        }

        if (message == null) message = "";

        String callstack = null;
        List<NSService> callers = ServiceHelper.getCallStack();
        if (callers != null) {
            if (callers.size() > 1) {
                callers.remove(callers.size() - 1);
            }
            callstack = IterableHelper.join(callers, " → ", false);
        }

        String function = null;
        if (user != null && callstack != null) {
            function = user + " -- " + callstack;
        } else if (callstack != null) {
            function = callstack;
        } else if (user != null) {
            function = user;
        } else {
            function = "";
        }


        log(level, function, message);
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
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(String level, String function, String message, Object... arguments) {
        log(fromLevel(level), function, message, arguments);
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
        log(fromLevel(level), function, message, arguments);
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
        return LevelTranslator.findISLevelCode(level);
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
