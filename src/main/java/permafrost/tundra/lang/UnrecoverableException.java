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
import permafrost.tundra.data.IDataMap;
import java.util.Collection;

/**
 * An exception indicating that an unrecoverable error has occurred.
 */
public class UnrecoverableException extends BaseException {
    /**
     * Constructs a new UnrecoverableException.
     */
    public UnrecoverableException() {
        super();
    }

    /**
     * Constructs a new UnrecoverableException with the given message.
     *
     * @param message A message describing why the UnrecoverableException was thrown.
     */
    public UnrecoverableException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnrecoverableException with the given cause.
     *
     * @param cause The cause of this UnrecoverableException.
     */
    public UnrecoverableException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UnrecoverableException with the given message and cause.
     *
     * @param message A message describing why the UnrecoverableException was thrown.
     * @param cause   The cause of this Exception.
     */
    public UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnrecoverableException with the given message and cause.
     *
     * @param message       A message describing why the UnrecoverableException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public UnrecoverableException(String message, Throwable cause, Throwable ...suppressed) {
        super(message, cause, suppressed);
    }

    /**
     * Constructs a new UnrecoverableException with the given message and cause.
     *
     * @param message       A message describing why the UnrecoverableException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public UnrecoverableException(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        super(message, cause, suppressed);
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return An IData representation of this object.
     */
    @Override
    public IData getIData() {
        IDataMap map = IDataMap.of(super.getIData());
        map.put("$exception.recoverable?", "false");
        return map;
    }
}
