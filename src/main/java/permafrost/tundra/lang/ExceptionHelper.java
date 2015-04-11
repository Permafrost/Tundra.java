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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

public class ExceptionHelper {
    /**
     * Throws a new BaseException whose message is constructed from the given
     * list of causes.
     *
     * @param causes The list of exceptions which caused this new BaseException to be thrown.
     * @throws ChainableServiceException Always throws a new BaseException using the given list of causes.
     */
    public static void raise(Throwable[] causes) throws ChainableServiceException {
        raise(getMessage(causes));
    }

    /**
     * Throws a new BaseException whose message is constructed from the given
     * list of causes.
     *
     * @param causes The list of exceptions which caused this new BaseException to be thrown.
     * @throws ChainableServiceException Always throws a new BaseException using the given list of causes.
     */
    public static void raise(java.util.Collection<Throwable> causes) throws ChainableServiceException {
        raise(getMessage(causes == null ? null : causes.toArray(new Throwable[causes.size()])));
    }

    /**
     * Throws a new Exception whose message is constructed from the given
     * cause.
     *
     * @param message A message describing why this new BaseException was thrown.
     * @param cause The exception which caused this new BaseException to be thrown.
     * @throws ChainableServiceException Always throws a new BaseException using the given message and cause.
     */
    public static void raise(String message, Throwable cause) throws ChainableServiceException {
        throw new ChainableServiceException(message, cause);
    }

    /**
     * Throws a new BaseException whose message is constructed from the given
     * cause.
     *
     * @param cause The exception which caused this new BaseException to be thrown.
     * @throws ChainableServiceException Always throws a new BaseException using the given cause.
     */
    public static void raise(Throwable cause) throws ChainableServiceException {
        throw new ChainableServiceException(cause);
    }

    /**
     * Throws a new BaseException with the given message.
     *
     * @param message A message describing why this new BaseException was thrown.
     * @throws ChainableServiceException Always throws a new BaseException using the given message.
     */
    public static void raise(String message) throws ChainableServiceException {
        throw new ChainableServiceException(message == null ? "" : message);
    }

    /**
     * Throws a new BaseException.
     *
     * @throws ChainableServiceException Always throws a new BaseException.
     */
    public static void raise() throws ChainableServiceException {
        throw new ChainableServiceException("");
    }

    /**
     * Returns a message describing the given exception.
     *
     * @param exception An exception whose message is to be retrieved.
     * @return A message describing the given exception.
     */
    public static String getMessage(Throwable exception) {
        if (exception == null) return "";
        return exception.getClass().getName() + ": " + exception.getMessage();
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String getMessage(java.util.Collection<Throwable> exceptions) {
        return getMessage(exceptions.toArray(new Throwable[exceptions.size()]));
    }

    /**
     * Returns a message describing the given list of exceptions.
     *
     * @param exceptions A list of exceptions whose messages are to be retrieved.
     * @return A message describing all exceptions in the given list.
     */
    public static String getMessage(Throwable[] exceptions) {
        StringBuilder msg = new StringBuilder();
        if (exceptions != null) {
            if (exceptions.length == 1 && exceptions[0] != null) {
                msg.append(getMessage(exceptions[0]));
            } else {
                for (int i = 0; i < exceptions.length; i++) {
                    if (exceptions[i] != null) {
                        msg.append("[").append(i).append("]: ").append(getMessage(exceptions[i]));
                        if (i < exceptions.length - 1) msg.append("\n");
                    }
                }
            }
        }
        return msg.toString();
    }

    /**
     * Returns the call stack associated with the given exception as an IData[] document list.
     *
     * @param exception An exception to retrieve the call stack from.
     * @return The call stack associated with the given exception as an IData[] document list.
     */
    public static IData[] getStackTrace(Throwable exception) {
        IData[] output = null;

        if (exception != null) {
            StackTraceElement[] stack = exception.getStackTrace();
            output = new IData[stack.length];

            for (int i = 0; i < stack.length; i++) {
                output[i] = IDataFactory.create();
                IDataCursor cursor = output[i].getCursor();
                IDataUtil.put(cursor, "description", stack[i].toString());
                IDataUtil.put(cursor, "file", stack[i].getFileName());
                IDataUtil.put(cursor, "class", stack[i].getClassName());
                IDataUtil.put(cursor, "method", stack[i].getMethodName());
                IDataUtil.put(cursor, "line", "" + stack[i].getLineNumber());
                IDataUtil.put(cursor, "native?", "" + stack[i].isNativeMethod());
                cursor.destroy();
            }
        }

        return output;
    }

}
