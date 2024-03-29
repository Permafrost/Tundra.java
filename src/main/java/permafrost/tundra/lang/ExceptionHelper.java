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

package permafrost.tundra.lang;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import org.xml.sax.SAXParseException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A collection of convenience methods for working with Integration Server service exceptions.
 */
public final class ExceptionHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ExceptionHelper() {}

    /**
     * Throws a new BaseException.
     *
     * @throws ServiceException The thrown exception.
     */
    public static void raise() throws ServiceException {
        raise((String)null);
    }

    /**
     * Throws a new BaseException.
     *
     * @param message           The exception message.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(String message) throws ServiceException {
        raise(message, null);
    }

    /**
     * Throws the given exception either by rethrowing if it is an Error or RuntimeException or ServiceException,
     * otherwise by wrapping it in a new BaseException with the given exception as the cause.
     *
     * @param exception         The exception cause.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(Throwable exception) throws ServiceException {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException)exception;
        } else if (exception instanceof Error) {
            throw (Error)exception;
        } else if (exception instanceof ServiceException) {
            throw (ServiceException)exception;
        } else {
            raise(null, exception);
        }
    }

    /**
     * Throws the given exception either by rethrowing if it is an Error or RuntimeException or ServiceException,
     * otherwise by wrapping it in a new BaseException with the given exception as the cause.
     *
     * @param exception         The exception cause.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(SQLException exception) throws ServiceException {
        raise((Iterable<? extends Throwable>)exception);
    }

    /**
     * Throws a new exception that includes all the given exceptions as suppressed exceptions.
     *
     * @param exceptions        The suppressed exceptions.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(Throwable... exceptions) throws ServiceException {
        raise(null, null, exceptions);
    }

    /**
     * Throws a new exception that includes all the given exceptions as suppressed exceptions.
     *
     * @param exceptions        The suppressed exceptions.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(Iterable<? extends Throwable> exceptions) throws ServiceException {
        raise(null, null, exceptions);
    }

    /**
     * Throws a new BaseException.
     *
     * @param message           The exception message.
     * @param cause             The optional cause of the message.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(String message, Throwable cause) throws ServiceException {
        raise(message, cause, (Iterable<? extends Throwable>)null);
    }

    /**
     * Throws a new BaseException.
     *
     * @param message           The exception message.
     * @param cause             The optional cause of the message.
     * @param suppressed        Optional list of suppressed exceptions.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(String message, Throwable cause, Throwable... suppressed) throws ServiceException {
        raise(message, cause, suppressed == null ? null : Arrays.asList(suppressed));
    }

    /**
     * Throws a new BaseException.
     *
     * @param message           The exception message.
     * @param cause             The optional cause of the message.
     * @param suppressed        Optional list of suppressed exceptions.
     * @throws ServiceException The thrown exception.
     */
    public static void raise(String message, Throwable cause, Iterable<? extends Throwable> suppressed) throws ServiceException {
        throw new BaseException(message, cause, suppressed);
    }

    /**
     * Throws a new BaseRuntimeException.
     *
     * @throws BaseRuntimeException The thrown exception.
     */
    public static void raiseUnchecked() {
        raiseUnchecked((String)null);
    }

    /**
     * Throws a new BaseException.
     *
     * @param message               The exception message.
     * @throws BaseRuntimeException The thrown exception.
     */
    public static void raiseUnchecked(String message) {
        raiseUnchecked(message, null);
    }

    /**
     * Throws a new exception that includes all the given exceptions as suppressed exceptions.
     *
     * @param exceptions        The suppressed exceptions.
     */
    public static void raiseUnchecked(Throwable... exceptions) {
        raiseUnchecked(null, null, exceptions);
    }

    /**
     * Throws a new exception that includes all the given exceptions as suppressed exceptions.
     *
     * @param exceptions        The suppressed exceptions.
     */
    public static void raiseUnchecked(Iterable<? extends Throwable> exceptions) {
        raiseUnchecked(null, null, exceptions);
    }

    /**
     * Throws a new BaseRuntimeException.
     *
     * @param message               The exception message.
     * @param cause                 The optional cause of the exception.
     * @throws BaseRuntimeException The thrown exception.
     */
    public static void raiseUnchecked(String message, Throwable cause) {
        raiseUnchecked(message, cause, (Iterable<? extends Throwable>)null);
    }

    /**
     * Throws a new BaseRuntimeException.
     *
     * @param message               The exception message.
     * @param cause                 The optional cause of the message.
     * @param suppressed            Optional list of suppressed exceptions.
     * @throws BaseRuntimeException The thrown exception.
     */
    public static void raiseUnchecked(String message, Throwable cause, Throwable... suppressed) {
        raiseUnchecked(message, cause, suppressed == null ? null : Arrays.asList(suppressed));
    }

    /**
     * Throws a new BaseRuntimeException.
     *
     * @param message               The exception message.
     * @param cause                 The optional cause of the message.
     * @param suppressed            Optional list of suppressed exceptions.
     * @throws BaseRuntimeException The thrown exception.
     */
    public static void raiseUnchecked(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        throw new BaseRuntimeException(message, cause, suppressed);
    }

    /**
     * Throws the given exception either by rethrowing if it is an Error or RuntimeException, otherwise by wrapping it
     * in a new BaseRuntimeException with the given exception as the cause.
     *
     * @param exception The exception which caused this new unchecked exception to be thrown.
     */
    public static void raiseUnchecked(Throwable exception) {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException)exception;
        } else if (exception instanceof Error) {
            throw (Error)exception;
        } else {
            throw new BaseRuntimeException(exception);
        }
    }

    /**
     * Throws the given exception either by rethrowing if it is an Error or RuntimeException, otherwise by wrapping it
     * in a new BaseRuntimeException with the given exception as the cause.
     *
     * @param exception The exception which caused this new unchecked exception to be thrown.
     */
    public static void raiseUnchecked(SQLException exception) {
        raiseUnchecked((Iterable<? extends Throwable>)exception);
    }

    /**
     * Returns the exception message to use given an optional message, cause, and list of suppressed exceptions.
     *
     * @param message       Optional exception message.
     * @param cause         Optional exception cause.
     * @param suppressed    Optional list of suppressed exceptions.
     * @return              The normalized exception message.
     */
    public static String normalizeMessage(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        if (message == null) {
            if (cause == null) {
                if (suppressed == null) {
                    message = "";
                } else {
                    for (Throwable throwable : suppressed) {
                        message = getMessage(throwable, false, false);
                        break;
                    }
                }
            } else {
                message = getMessage(cause, false, false);
            }
        }

        return message;
    }

    /**
     * Returns a message describing the given exception.
     *
     * @param exception An exception whose message is to be retrieved.
     * @return          A message describing the given exception.
     */
    public static String getMessage(Throwable exception) {
        return getMessage(exception, false);
    }

    /**
     * Returns a message describing the given exception.
     *
     * @param exception             An exception whose message is to be retrieved.
     * @param useSimpleClassName    Whether to use a simple or fully-qualifed class name.
     * @return                      A message describing the given exception.
     */
    public static String getMessage(Throwable exception, boolean useSimpleClassName) {
        return getMessage(exception, true, useSimpleClassName);
    }

    /**
     * Returns a message describing the given exception.
     *
     * @param exception             An exception whose message is to be retrieved.
     * @param useSimpleClassName    Whether to use a simple or fully-qualifed class name.
     * @return                      A message describing the given exception.
     */
    public static String getMessage(Throwable exception, boolean includeClassName, boolean useSimpleClassName) {
        if (exception == null) return "";

        StringBuilder builder = new StringBuilder();

        if (includeClassName) {
            if (useSimpleClassName) {
                builder.append(exception.getClass().getSimpleName());
            } else {
                builder.append(exception.getClass().getName());
            }
            builder.append(": ");
        }
        builder.append(exception.getMessage());

        if (exception instanceof SAXParseException) {
            SAXParseException parseException = (SAXParseException)exception;
            builder.append(String.format(" (Line %d, Column %d)", parseException.getLineNumber(), parseException.getColumnNumber()));
        }

        Throwable cause =  exception.getCause();
        if (cause != null && cause != exception) {
            builder.append("\n\tCaused by: ");
            builder.append(getMessage(cause, includeClassName, useSimpleClassName));
        }

        Throwable[] suppressed = getSuppressed(exception);
        if (suppressed != null) {
            for (Throwable throwable : suppressed) {
                if (throwable != null && throwable != exception) {
                    builder.append("\nSuppressed: ");
                    builder.append(getMessage(throwable, includeClassName, useSimpleClassName));
                }
            }
        }

        return builder.toString();
    }

    /**
     * The Throwable.getSuppressed() reflected method, if it exists.
     */
    private static Method THROWABLE_GET_SUPPRESSED_METHOD;
    static {
        try {
            THROWABLE_GET_SUPPRESSED_METHOD = Throwable.class.getDeclaredMethod("getSuppressed");
        } catch(Throwable ex) {
            THROWABLE_GET_SUPPRESSED_METHOD = null;
        }
    }

    /**
     * Returns the list of exceptions suppressed by the given exception, if any.
     *
     * @param exception The exception whose suppressed exceptions are to be returned.
     * @return          The list of exceptions suppressed by the given exception.
     */
    private static Throwable[] getSuppressed(Throwable exception) {
        Throwable[] suppressed = null;

        try {
            if (THROWABLE_GET_SUPPRESSED_METHOD != null) {
                suppressed = (Throwable[])THROWABLE_GET_SUPPRESSED_METHOD.invoke(exception);
            } else if (exception instanceof ExceptionSuppression) {
                suppressed = ((ExceptionSuppression)exception).suppressed();
            }
        } catch(IllegalAccessException ex) {
            // ignore exception
        } catch(IllegalArgumentException ex) {
            // ignore exception
        } catch(InvocationTargetException ex) {
            // ignore exception
        }

        return suppressed;
    }

    /**
     * The Throwable.addSuppressed(Throwable) reflected method, if it exists.
     */
    private static Method THROWABLE_ADD_SUPPRESSED_METHOD;
    static {
        try {
            THROWABLE_ADD_SUPPRESSED_METHOD = Throwable.class.getDeclaredMethod("addSuppressed", Throwable.class);
        } catch(Throwable ex) {
            THROWABLE_ADD_SUPPRESSED_METHOD = null;
        }
    }

    /**
     * Adds the given list of suppressed exceptions to the given exception.
     *
     * @param exception     The exception to add suppressed exceptions to.
     * @param suppressed    The list of exceptions which were suppressed.
     */
    public static void addSuppressed(Throwable exception, Throwable ...suppressed) {
        if (suppressed != null) {
            if (THROWABLE_ADD_SUPPRESSED_METHOD != null) {
                for (Throwable throwable : suppressed) {
                    try {
                        THROWABLE_ADD_SUPPRESSED_METHOD.invoke(exception, throwable);
                    } catch (IllegalAccessException ex) {
                        // ignore exception
                    } catch (IllegalArgumentException ex) {
                        // ignore exception
                    } catch (InvocationTargetException ex) {
                        // ignore exception
                    }
                }
            } else if (exception instanceof ExceptionSuppression) {
                ((ExceptionSuppression)exception).suppress(suppressed);
            }
        }
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String getMessage(Throwable... exceptions) {
        return ArrayHelper.join(getMessages(exceptions), "\n");
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String getMessage(Iterable<? extends Throwable> exceptions) {
        StringBuilder builder = new StringBuilder();
        String[] messages = getMessages(exceptions);
        int i = 0;
        for (String message : messages) {
            if (i > 0) {
                builder.append("\n");
            }
            if (message != null) {
                builder.append(message.trim());
                i++;
            }
        }
        return builder.toString();
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String[] getMessages(Throwable... exceptions) {
        if (exceptions == null) return null;

        String[] messages = new String[exceptions.length];
        for (int i = 0; i < exceptions.length; i++) {
            if (exceptions[i] != null) {
                messages[i] = String.format("[%d] %s", i, getMessage(exceptions[i]));
            }
        }

        return messages;
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String[] getMessages(Iterable<? extends Throwable> exceptions) {
        List<String> messages = null;

        if (exceptions != null) {
            messages = new ArrayList<String>();
            int i = 0;
            for (Throwable exception : exceptions) {
                messages.add(String.format("[%d] %s", i, getMessage(exception)));
                i++;
            }
        }

        return messages == null ? null : messages.toArray(new String[0]);
    }

    /**
     * Returns the call stack associated with the given exception as an IData[] document list.
     *
     * @param exception An exception to retrieve the call stack from.
     * @return The call stack associated with the given exception as an IData[] document list.
     */
    public static IData[] getStackTrace(Throwable exception) {
        if (exception == null) return null;
        return StackTraceElementHelper.toIDataArray(exception.getStackTrace());
    }

    /**
     * Returns the printed stack trace for the given exception as a string.
     *
     * @param exception The exception to print the stack trace for.
     * @return          A string containing the printed stack trace for the given exception.
     */
    public static String getStackTraceString(Throwable exception) {
        return getStackTraceString(exception, -1);
    }

    /**
     * Returns the printed stack trace for the given exception as a string.
     *
     * @param exception The exception to print the stack trace for.
     * @param level     How many levels of the stack trace to include.
     * @return          A string containing the printed stack trace for the given exception.
     */
    public static String getStackTraceString(Throwable exception, int level) {
        return getStackTraceString(exception, level, true);
    }

    /**
     * Returns the printed stack trace for the given exception as a string.
     *
     * @param exception The exception to print the stack trace for.
     * @param level     How many levels of the stack trace to include.
     * @param formatted If true, stack trace will be formatted with tabs and new lines.
     * @return          A string containing the printed stack trace for the given exception.
     */
    public static String getStackTraceString(Throwable exception, int level, boolean formatted) {
        if (exception == null) return null;

        String linePrefix, lineSuffix;
        if (formatted) {
            linePrefix = "\t";
            lineSuffix = "\n";
        } else {
            linePrefix = " ";
            lineSuffix = "";
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        exception.printStackTrace(printWriter);

        printWriter.flush();
        printWriter.close();

        String stackTrace;
        if (level < 0) {
            stackTrace = stringWriter.toString();
        } else {
            String[] lines = StringHelper.lines(stringWriter.toString());
            StringBuilder builder = new StringBuilder();
            if (lines != null) {
                for (int i = 0; i < level + 1; i++) {
                    if (i < lines.length) {
                        if (lines[i] != null) {
                            builder.append(linePrefix).append(lines[i].trim()).append(lineSuffix);
                        }
                    } else {
                        break;
                    }
                }
                if (level < lines.length - 1) {
                    builder.append(linePrefix).append("...").append(lines.length - level - 1).append(" more").append(lineSuffix);
                }
            }
            stackTrace = builder.toString();
        }

        return stackTrace;
    }

    /**
     * Returns the innermost cause of the given exception, or itself if it has no cause.
     *
     * @param exception The exception to return the initial cause of.
     * @return          The initial cause of the exception, or itself if it has no cause.
     */
    public static Throwable getInitialCause(Throwable exception) {
        if (exception != null) {
            Throwable cause;
            while ((cause = exception.getCause()) != null) {
                exception = cause;
            }
        }
        return exception;
    }
}
