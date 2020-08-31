/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.util.concurrent;

import permafrost.tundra.lang.Startable;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

/**
 * A concurrent writer.
 */
public class ConcurrentWriter extends FilterWriter implements Startable {
    /**
     * Is this handler started or stopped?
     */
    protected volatile boolean started;
    /**
     * The task executor used to defer writing to.
     */
    protected ExecutorService executorService;

    /**
     * Creates a new ConcurrentWriter object.
     *
     * @param writer The Writer this object delegates to.
     */
    protected ConcurrentWriter(ExecutorService executorService, Writer writer) {
        super(writer);
        if (executorService == null) throw new NullPointerException("executorService must not be null");
        this.executorService = executorService;
    }

    @Override
    public void write(final int c) {
        if (started) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    synchronized(out) {
                        try {
                            out.write(c);
                            out.flush();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void write(char[] cbuf, final int off, final int len) {
        if (started) {
            final char[] copy = Arrays.copyOf(cbuf, cbuf.length);
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    synchronized(out) {
                        try {
                            out.write(copy, off, len);
                            out.flush();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void write(final String str, final int off, final int len) {
        if (started) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    synchronized(out) {
                        try {
                            out.write(str, off, len);
                            out.flush();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            });
        }
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            started = true;
        }
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            started = false;
        }
    }

    /**
     * Restarts this object.
     */
    @Override
    public synchronized void restart() {
        stop();
        start();
    }

    /**
     * Returns true if the object is started.
     *
     * @return True if the object is started.
     */
    @Override
    public boolean isStarted() {
        return started;
    }
}
