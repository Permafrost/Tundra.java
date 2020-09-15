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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

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
     * @throws ServiceException The thrown exception.
     */
    public static void raiseUnchecked(Throwable... exceptions) {
        raiseUnchecked(null, null, exceptions);
    }

    /**
     * Throws a new exception that includes all the given exceptions as suppressed exceptions.
     *
     * @param exceptions        The suppressed exceptions.
     * @throws ServiceException The thrown exception.
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
                    message = getMessage(suppressed);
                }
            } else {
                message = getMessage(cause);
            }
        }

        return message;
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

    /*
     * The following code for exception suppression handling was adapted from the OpenJDK Throwable class source code,
     * as allowed by the GNU General Public License, version 2, with the Classpath Exception.
     * Refer: ./licenses/OpenJDK/LICENSE.txt
     */

    /**
     * Caption for the cause of this exception.
     */
    private static final String CAUSE_CAPTION = "Caused by: ";
    /**
     * Caption for suppressed exceptions.
     */
    private static final String SUPPRESSED_CAPTION = "Suppressed: ";

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param printStream   The stream to print to.
     */
    public static void printStackTrace(Throwable exception, PrintStream printStream) {
        printStackTrace(exception, new WrappedPrintStream(printStream));
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param printStreamOrWriter   The stream to print to.
     */
    private static void printStackTrace(Throwable exception, PrintStreamOrWriter printStreamOrWriter) {
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(exception);

        synchronized (printStreamOrWriter.lock()) {
            // stack trace
            printStreamOrWriter.println(exception);
            StackTraceElement[] trace = exception.getStackTrace();
            for (StackTraceElement traceElement : trace)
                printStreamOrWriter.println("\tat " + traceElement);

            // suppressed exceptions
            if (exception instanceof ExceptionSuppression) {
                for (Throwable suppressedException : ((ExceptionSuppression)exception).suppressed()) {
                    Throwable[] childSuppressedExceptions = null;
                    if (suppressedException instanceof ExceptionSuppression) {
                        childSuppressedExceptions = ((ExceptionSuppression)suppressedException).suppressed();
                    }
                    printEnclosedStackTrace(suppressedException, childSuppressedExceptions, printStreamOrWriter, trace, SUPPRESSED_CAPTION, "\t", dejaVu);
                }
            }

            // cause
            Throwable cause = exception.getCause();
            if (cause != null) {
                Throwable[] childSuppressedExceptions = null;
                if (cause instanceof ExceptionSuppression) {
                    childSuppressedExceptions = ((ExceptionSuppression)cause).suppressed();
                }
                printEnclosedStackTrace(cause, childSuppressedExceptions, printStreamOrWriter, trace, CAUSE_CAPTION, "", dejaVu);
            }
        }
    }

    /**
     * Print stack trace as an enclosed exception for the specified stack trace.
     */
    private static void printEnclosedStackTrace(Throwable exception, Throwable[] suppressedExceptions, PrintStreamOrWriter printStreamOrWriter, StackTraceElement[] enclosingTrace, String caption, String prefix, Set<Throwable> dejaVu) {
        assert Thread.holdsLock(printStreamOrWriter.lock());
        if (dejaVu.contains(exception)) {
            printStreamOrWriter.println("\t[CIRCULAR REFERENCE:" + exception + "]");
        } else {
            dejaVu.add(exception);
            // Compute number of frames in common between this and enclosing trace
            StackTraceElement[] trace = exception.getStackTrace();
            int m = trace.length - 1;
            int n = enclosingTrace.length - 1;
            while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            // Print our stack trace
            printStreamOrWriter.println(prefix + caption + exception);
            for (int i = 0; i <= m; i++)
                printStreamOrWriter.println(prefix + "\tat " + trace[i]);
            if (framesInCommon != 0)
                printStreamOrWriter.println(prefix + "\t... " + framesInCommon + " more");

            if (suppressedExceptions != null) {
                for (Throwable suppressedException : suppressedExceptions) {
                    Throwable[] childSuppressedExceptions = null;
                    if (suppressedException instanceof ExceptionSuppression) {
                        childSuppressedExceptions = ((ExceptionSuppression)suppressedException).suppressed();
                    }
                    printEnclosedStackTrace(suppressedException, childSuppressedExceptions, printStreamOrWriter, trace, SUPPRESSED_CAPTION, prefix + "\t", dejaVu);
                }
            }

            // Print cause, if any
            Throwable cause = exception.getCause();
            if (cause != null) {
                Throwable[] childSuppressedExceptions = null;
                if (cause instanceof ExceptionSuppression) {
                    childSuppressedExceptions = ((ExceptionSuppression)cause).suppressed();
                }
                printEnclosedStackTrace(cause, childSuppressedExceptions, printStreamOrWriter, trace, CAUSE_CAPTION, prefix, dejaVu);
            }
        }
    }

    /**
     * Wrapper class for PrintStream and PrintWriter to enable a single implementation of printStackTrace.
     */
    private abstract static class PrintStreamOrWriter {
        /** Returns the object to be locked when using this StreamOrWriter */
        abstract Object lock();

        /** Prints the specified string as a line on this StreamOrWriter */
        abstract void println(Object o);
    }

    /**
     * Wrapper class implementation for PrintStream and PrintWriter to enable a single implementation of printStackTrace.
     */
    private static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        Object lock() {
            return printStream;
        }

        void println(Object o) {
            printStream.println(o);
        }
    }
}
