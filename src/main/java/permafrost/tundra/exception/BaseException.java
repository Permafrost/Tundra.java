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

package permafrost.tundra.exception;

import com.wm.app.b2b.server.ServiceException;

public class BaseException extends ServiceException {
    /**
     * Constructs a new BaseException.
     */
    public BaseException() {
        super("");
    }

    /**
     * Constructs a new BaseException with the given message.
     *
     * @param message A message describing why the BaseException was thrown.
     */
    public BaseException(java.lang.String message) {
        super(message);
    }

    /**
     * Constructs a new BaseException with the given cause.
     *
     * @param cause The cause of this BaseException.
     */
    public BaseException(Throwable cause) {
        this(ExceptionHelper.getMessage(cause), cause);
    }

    /**
     * Constructs a new BaseException with the given message and cause.
     *
     * @param message A message describing why the BaseException was thrown.
     * @param cause The cause of this Exception.
     */
    public BaseException(java.lang.String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
