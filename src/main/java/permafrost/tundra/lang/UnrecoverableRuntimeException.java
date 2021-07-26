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

package permafrost.tundra.lang;

import java.util.Map;

/**
 * An unchecked exception indicating that an unrecoverable error has occurred.
 */
public class UnrecoverableRuntimeException extends BaseRuntimeException {
    /**
     * Constructs a new UnrecoverableRuntimeException.
     */
    public UnrecoverableRuntimeException() {
        super();
    }

    /**
     * Constructs a new UnrecoverableRuntimeException with the given message.
     *
     * @param message A message describing why the UnrecoverableRuntimeException was thrown.
     */
    public UnrecoverableRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnrecoverableRuntimeException with the given cause.
     *
     * @param cause The cause of this UnrecoverableRuntimeException.
     */
    public UnrecoverableRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new UnrecoverableRuntimeException with the given message and cause.
     *
     * @param message A message describing why the UnrecoverableRuntimeException was thrown.
     * @param cause   The cause of this Exception.
     */
    public UnrecoverableRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UnrecoverableRuntimeException with the given message and cause.
     *
     * @param message       A message describing why the UnrecoverableRuntimeException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public UnrecoverableRuntimeException(String message, Throwable cause, Throwable... suppressed) {
        super(message, cause, suppressed);
    }

    /**
     * Constructs a new UnrecoverableRuntimeException with the given message and cause.
     *
     * @param message       A message describing why the UnrecoverableRuntimeException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public UnrecoverableRuntimeException(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        super(message, cause, suppressed);
    }

    /**
     * Returns the properties of this object.
     *
     * @return the properties of this object.
     */
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put("$exception.recoverable?", "false");
        return properties;
    }
}
