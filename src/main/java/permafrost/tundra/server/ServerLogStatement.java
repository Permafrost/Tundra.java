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
import com.wm.lang.ns.NSService;
import org.apache.log4j.Level;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.lang.ExceptionHelper;
import java.util.List;

/**
 * Represents a server log statement.
 */
public class ServerLogStatement {
    /**
     * The logging level to use.
     */
    protected Level level;
    /**
     * The message to be logged.
     */
    protected String message;
    /**
     * The optional context to include.
     */
    protected IData context;
    /**
     * Whether to prefix the log statement with logging metadata.
     */
    protected boolean addPrefix;

    /**
     * Creates a new ServerLogStatement object.
     *
     * @param message   The message to be logged.
     * @param context   The optional context to include.
     * @param addPrefix Whether to prefix the log statement with logging metadata.
     */
    public ServerLogStatement(Level level, String message, IData context, boolean addPrefix) {
        this.level = level == null ? ServerLogLevelHelper.DEFAULT_LOG_LEVEL : level;
        this.message = message;
        this.context = context;
        this.addPrefix = addPrefix;
    }

    /**
     * Returns the function to be logged.
     *
     * @return The function to be logged.
     */
    public String getFunction() {
        return addPrefix ? getFunction(UserHelper.getCurrentName(), ServiceHelper.getCallStack(), false) : null;
    }

    /**
     * Returns the function to be logged given a callstack.
     *
     * @oaram user          The user invoking the function.
     * @param callstack     The callstack to convert to a function.
     * @param removeTail    Whether to remove the tail of the callstack.
     * @return              The resulting function to log.
     */
    public static String getFunction(String user, List<NSService> callstack, boolean removeTail) {
        String function = null;

        if (callstack != null) {
            if (removeTail && callstack.size() > 1) {
                callstack.remove(callstack.size() - 1);
            }
            if (callstack.size() > 2) {
                function = callstack.get(0) + " → … → " + callstack.get(callstack.size() - 1);
            } else if (callstack.size() == 2) {
                function = callstack.get(0) + " → " + callstack.get(1);
            } else if (callstack.size() == 1) {
                function = callstack.get(0).toString();
            }
        }

        if (user != null) {
            if (function != null) {
                function = user + " ► " + function;
            } else {
                function = user;
            }
        }

        return function;
    }

    /**
     * Returns the message to be logged.
     *
     * @return The message to be logged.
     */
    public String getMessage() {
        StringBuilder builder = new StringBuilder();

        if (message != null) {
            builder.append(message);
        }
        if (context != null) {
            if (message != null) {
                builder.append(" -- ");
            }
            try {
                IDataJSONParser parser = new IDataJSONParser(false);
                parser.emit(builder, context);
            } catch (Exception ex) {
                builder.append(ExceptionHelper.getMessage(ex));
            }
        }

        return builder.toString();
    }

    /**
     * Returns this log statement as a string.
     *
     * @return This log statement as a string.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        String function = getFunction();
        String message = getMessage();

        if (function != null) {
            builder.append(function);
        }

        if (message != null && !message.equals("")) {
            if (function != null) {
                builder.append(" -- ");
            }
            builder.append(message);
        }

        return builder.toString();
    }

    /**
     * Returns a log statement string given a log message, context, and prefix.
     *
     * @param level     The logging level to use.
     * @param message   The message to be logged.
     * @param context   The optional context to include.
     * @param addPrefix Whether to prefix the log statement with logging metadata.
     * @return          The log statement as a string.
     */
    public static String of(Level level, String message, IData context, boolean addPrefix) {
        return new ServerLogStatement(level, message, context, addPrefix).toString();
    }
}
