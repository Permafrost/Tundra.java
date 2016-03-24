/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

package permafrost.tundra.content;

import java.util.Collection;

/**
 * Throw a MalformedException when data is malformed.
 */
public class MalformedException extends StrictException {
    /**
     * Constructs a new MalformedException.
     */
    public MalformedException() {
        super();
    }

    /**
     * Constructs a new MalformedException with the given message.
     *
     * @param message A message describing why the MalformedException was thrown.
     */
    public MalformedException(String message) {
        super(message);
    }

    /**
     * Constructs a new MalformedException with the given cause.
     *
     * @param cause The cause of this MalformedException.
     */
    public MalformedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MalformedException with the given message and cause.
     *
     * @param message A message describing why the MalformedException was thrown.
     * @param cause   The cause of this Exception.
     */
    public MalformedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MalformedException with the given list of exceptions.
     *
     * @param exceptions A collection of exceptions this exception will wrap.
     */
    public MalformedException(Collection<? extends Throwable> exceptions) {
        super(exceptions);
    }

    /**
     * Constructs a new MalformedException with the given list of exceptions.
     *
     * @param exceptions A collection of exceptions this exception will wrap.
     */
    public MalformedException(Throwable... exceptions) {
        super(exceptions);
    }
}
