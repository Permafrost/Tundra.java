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

import java.util.Collection;

/**
 * Throw a SecurityException to indicate an authentication or authorization error.
 */
public class SecurityException extends UnrecoverableException {
    /**
     * Constructs a new SecurityException.
     */
    public SecurityException() {
        super();
    }

    /**
     * Constructs a new SecurityException with the given message.
     *
     * @param message A message describing why the SecurityException was thrown.
     */
    public SecurityException(String message) {
        super(message);
    }

    /**
     * Constructs a new SecurityException with the given cause.
     *
     * @param cause The cause of this BaseException.
     */
    public SecurityException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new SecurityException with the given message and cause.
     *
     * @param message A message describing why the SecurityException was thrown.
     * @param cause   The cause of this Exception.
     */
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SecurityException with the given message and cause.
     *
     * @param message       A message describing why the SecurityException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public SecurityException(String message, Throwable cause, Throwable... suppressed) {
        super(message, cause, suppressed);
    }

    /**
     * Constructs a new SecurityException with the given message and cause.
     *
     * @param message       A message describing why the SecurityException was thrown.
     * @param cause         Optional cause of this Exception.
     * @param suppressed    Optional list of suppressed exceptions.
     */
    public SecurityException(String message, Throwable cause, Iterable<? extends Throwable> suppressed) {
        super(message, cause, suppressed);
    }
}
