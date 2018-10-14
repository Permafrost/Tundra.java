/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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

import java.util.concurrent.Callable;

/**
 * Wraps a Callable in a Runnable compatible wrapper.
 */
public class RunnableCallable implements Runnable {
    /**
     * The Callable which will be called when this Runnable runs.
     */
    protected Callable callable;

    /**
     * Constructs a new RunnableCallable object.
     *
     * @param callable The Callable which will be called when this Runnable runs.
     */
    public RunnableCallable(Callable callable) {
        this.callable = callable;
    }

    /**
     * Calls the wrapped Callable.
     */
    @Override
    public void run() {
        try {
            callable.call();
        } catch(Throwable ex) {
            ExceptionHelper.raiseUnchecked(ex);
        }
    }
}
