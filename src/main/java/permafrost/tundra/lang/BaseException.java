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
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataMap;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Checked exception superclass inherited by all other Tundra checked exceptions.
 */
public class BaseException extends ServiceException implements ExceptionSuppression, IDataCodable, Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new BaseException.
     */
    public BaseException() {
        this((String)null);
    }

    /**
     * Constructs a new BaseException with the given message.
     *
     * @param message A message describing why the BaseException was thrown.
     */
    public BaseException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new BaseException with the given cause.
     *
     * @param cause The cause of this BaseException.
     */
    public BaseException(Throwable cause) {
        this(null, cause);
    }

    /**
     * Constructs a new BaseException with the given message and cause.
     *
     * @param message A message describing why the BaseException was thrown.
     * @param cause   The cause of this Exception.
     */
    public BaseException(String message, Throwable cause) {
        this(message, cause, (Iterable<? extends Throwable>)null);
    }

    /**
     * Constructs a new BaseException.
     *
     * @param message       A message describing why the BaseException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public BaseException(String message, Throwable cause, Throwable... suppressed) {
        this(message, cause, suppressed == null ? null : Arrays.asList(suppressed));
    }

    /**
     * Constructs a new BaseException.
     *
     * @param message       A message describing why the BaseException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public BaseException(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        super(ExceptionHelper.normalizeMessage(message, cause, suppressed));
        if (cause != null) initCause(cause);
        suppress(suppressed);
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return An IData representation of this object.
     */
    public IData getIData() {
        IDataMap map = new IDataMap();
        map.put("$exception?", "true");
        map.put("$exception.class", getClass().getName());
        map.put("$exception.message", getMessage());
        return map;
    }

    /**
     * This method has not been implemented.
     *
     * @param  document                         An IData document.
     * @throws UnsupportedOperationException    This method has not been implemented.
     */
    public void setIData(IData document) {
        throw new UnsupportedOperationException("setIData(IData) not implemented");
    }

    /**
     * List of suppressed exceptions.
     */
    private List<Throwable> suppressedExceptions = Collections.emptyList();

    /**
     * Suppresses the given exception.
     *
     * @param exception The exception to suppress.
     */
    private synchronized void suppress(Throwable exception) {
        if (exception == null || exception == this) return;

        if (suppressedExceptions.size() == 0) {
            suppressedExceptions = new ArrayList<Throwable>(1);
        }

        suppressedExceptions.add(exception);
    }

    /**
     * Suppresses the given exceptions.
     *
     * @param exceptions The exceptions to suppress.
     */
    @Override
    public synchronized void suppress(Iterable<? extends Throwable> exceptions) {
        if (exceptions != null) {
            for (Throwable exception : exceptions) {
                suppress(exception);
            }
        }
    }

    /**
     * Suppresses the given exceptions.
     *
     * @param exceptions The exceptions to suppress.
     */
    @Override
    public synchronized void suppress(Throwable ...exceptions) {
        if (exceptions != null) {
            for (Throwable exception : exceptions) {
                suppress(exception);
            }
        }
    }

    /**
     * Returns the list of suppressed exceptions.
     *
     * @return the list of suppressed exceptions.
     */
    @Override
    public synchronized Throwable[] suppressed() {
        return suppressedExceptions.toArray(new Throwable[0]);
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param printStream   The stream to print to.
     */
    @Override
    public void printStackTrace(PrintStream printStream) {
        ExceptionHelper.printStackTrace(this, printStream);
    }
}
