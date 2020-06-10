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
import java.util.Arrays;
import java.util.Collection;

/**
 * A collection of convenience methods for working with Integration Server service exceptions.
 */
public final class ExceptionHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ExceptionHelper() {}

    /**
     * Throws a new ServiceException whose message is constructed from the given list of causes.
     *
     * @param causes The list of exceptions which caused this new ServiceException to be thrown.
     * @throws ServiceException Always throws a new ServiceException using the given list of causes.
     */
    public static void raise(Throwable... causes) throws ServiceException {
        raise(getMessage(causes));
    }

    /**
     * Throws a new ServiceException whose message is constructed from the given list of causes.
     *
     * @param causes The list of exceptions which caused this new ServiceException to be thrown.
     * @throws ServiceException Always throws a new ServiceException using the given list of causes.
     */
    public static void raise(Collection<? extends Throwable> causes) throws ServiceException {
        raise(getMessage(causes));
    }

    /**
     * Throws a new ServiceException whose message is constructed from the given cause.
     *
     * @param message A message describing why this new ServiceException was thrown.
     * @param cause   The exception which caused this new ServiceException to be thrown.
     * @throws ServiceException Always throws a new ServiceException using the given message and cause.
     */
    public static void raise(String message, Throwable cause) throws ServiceException {
        throw new BaseException(message, cause);
    }

    /**
     * Throws a new ServiceException whose message is constructed from the given cause, unless the cause is already a
     * ServiceException or is an unchecked exception, in which case it is rethrown without modification.
     *
     * @param cause The exception which caused this new ServiceException to be thrown.
     * @throws ServiceException The given Throwable if it is already a ServiceException or an unchecked exception,
     *                          otherwise a new ServiceException constructed with the given Throwable as its cause.
     */
    public static void raise(Throwable cause) throws ServiceException {
        if (cause instanceof ServiceException) {
            throw (ServiceException)cause;
        } else if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        } else {
            throw new BaseException(cause);
        }
    }

    /**
     * Throws a new RuntimeException constructed with the given cause, unless the cause is already a
     * an unchecked exception, in which case it is rethrown without modification.
     *
     * @param cause The exception which caused this new unchecked exception to be thrown.
     */
    public static void raiseUnchecked(Throwable cause) {
        if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
        } else if (cause instanceof Error) {
            throw (Error)cause;
        } else {
            throw new RuntimeException(cause);
        }
    }

    /**
     * Throws a new ServiceException with the given message.
     *
     * @param message A message describing why this new ServiceException was thrown.
     * @throws ServiceException Always throws a new ServiceException using the given message.
     */
    public static void raise(String message) throws ServiceException {
        throw new BaseException(message);
    }

    /**
     * Throws a new ServiceException with an empty message.
     *
     * @throws ServiceException Always throws a new ServiceException.
     */
    public static void raise() throws ServiceException {
        throw new BaseException();
    }

    /**
     * Returns a message describing the given exception.
     *
     * @param exception An exception whose message is to be retrieved.
     * @return A message describing the given exception.
     */
    public static String getMessage(Throwable exception) {
        if (exception == null) return "";

        StringBuilder builder = new StringBuilder();

        builder.append(exception.getClass().getName());
        builder.append(": ");
        builder.append(exception.getMessage());

        if (exception instanceof SAXParseException) {
            SAXParseException parseException = (SAXParseException)exception;
            //builder.append(" (Line ").append("" + ex.getLineNumber()).append(", Column ").append("" + ex.getColumnNumber()).append(")");
            builder.append(String.format(" (Line %d, Column %d)", parseException.getLineNumber(), parseException.getColumnNumber()));
        }

        return builder.toString();
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String getMessage(Collection<? extends Throwable> exceptions) {
        if (exceptions == null) return "";
        return getMessage(exceptions.toArray(new Throwable[0]));
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
    public static Collection<String> getMessages(Collection<? extends Throwable> exceptions) {
        if (exceptions == null) return null;
        return Arrays.asList(getMessages(exceptions.toArray(new Throwable[0])));
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
        if (exception == null) return null;

        final String linePrefix = "\t", lineSuffix = "\n";

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
