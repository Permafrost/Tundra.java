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

import com.wm.data.IData;
import com.wm.util.JournalLogger;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.Loggable;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Convenience methods for logging to the Integration Server server log.
 */
public class ServerLogHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ServerLogHelper() {}

    /**
     * Logs the given message and context automatically prefixed by the current user and callstack.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     */
    public static void log(ServerLogLevel level, String function, String message, IData context) {
        log(level, message, context, true);
    }

    /**
     * Logs the given message and context optionally prefixed by the current user and callstack.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     * @param addPrefix Whether to prefix log statement with logging metadata.
     */
    public static void log(ServerLogLevel level, String message, IData context, boolean addPrefix) {
        ServerLogStatement statement = new ServerLogStatement(level, message, context, addPrefix);
        log(level, null, statement.toString());
    }

    /**
     * Logs the given message and context optionally prefixed by the current user and callstack.
     *
     * @param name      Logical name of the log target file to use.
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to be logged.
     * @param addPrefix Whether to prefix log statement with logging metadata.
     */
    public static void log(String name, ServerLogLevel level, String message, IData context, boolean addPrefix) {
        Loggable loggable = ServerLogManager.getInstance().get(name);
        if (loggable != null) {
            try {
                loggable.log(level, message, context, addPrefix);
            } catch(IOException ex) {
                ExceptionHelper.raiseUnchecked(ex);
            }
        }
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the fatal level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void fatal(String function, String message, Object... arguments) {
        log(ServerLogLevel.FATAL, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the error level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void error(String function, String message, Object... arguments) {
        log(ServerLogLevel.ERROR, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the warn level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void warn(String function, String message, Object... arguments) {
        log(ServerLogLevel.WARN, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the info level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void info(String function, String message, Object... arguments) {
        log(ServerLogLevel.INFO, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the debug level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void debug(String function, String message, Object... arguments) {
        log(ServerLogLevel.DEBUG, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the trace level.
     *
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void trace(String function, String message, Object... arguments) {
        log(ServerLogLevel.TRACE, function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    public static void log(ServerLogLevel level, String function, String message, Object... arguments) {
        log(level == null ? ServerLogLevel.DEFAULT_LOG_LEVEL.getLevelCode() : level.getLevelCode(), function, message, arguments);
    }

    /**
     * Logs the given message formatted with the given arguments against the given function at the given level.
     *
     * @param level     The logging level to use.
     * @param function  The function against which the message is being logged.
     * @param message   The message to be logged.
     * @param arguments The arguments to be included when formatting the message.
     */
    private static void log(int level, String function, String message, Object... arguments) {
        if (function == null) {
            JournalLogger.log(3, 90, level, (arguments != null && arguments.length > 0) ? MessageFormat.format(message, arguments) : message);
        } else {
            JournalLogger.log(4, 90, level, function, (arguments != null && arguments.length > 0) ? MessageFormat.format(message, arguments) : message);
        }
    }
}
