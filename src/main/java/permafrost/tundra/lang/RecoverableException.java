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

import com.wm.app.b2b.server.ISRuntimeException;
import com.wm.data.IData;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.IDataMap;
import java.util.Collection;

/**
 * An exception indicating that a recoverable error has occurred.
 */
public class RecoverableException extends ISRuntimeException implements IDataCodable {
    /**
     * Constructs a new RecoverableException.
     */
    public RecoverableException() {
        super();
    }

    /**
     * Constructs a new RecoverableException with the given message.
     *
     * @param message A message describing why the RecoverableException was thrown.
     */
    public RecoverableException(String message) {
        super(message);
    }

    /**
     * Constructs a new RecoverableException with the given cause.
     *
     * @param cause The cause of this RecoverableException.
     */
    public RecoverableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new RecoverableException with the given message and cause.
     *
     * @param message A message describing why the RecoverableException was thrown.
     * @param cause   The cause of this Exception.
     */
    public RecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new RecoverableException with the given list of exceptions.
     *
     * @param exceptions A collection of exceptions this exception will wrap.
     */
    public RecoverableException(Collection<? extends Throwable> exceptions) {
        super(ExceptionHelper.getMessage(exceptions));
    }

    /**
     * Constructs a new RecoverableException with the given list of exceptions.
     *
     * @param exceptions A collection of exceptions this exception will wrap.
     */
    public RecoverableException(Throwable... exceptions) {
        super(ExceptionHelper.getMessage(exceptions));
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
        map.put("$exception.recoverable?", "true");
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
}
