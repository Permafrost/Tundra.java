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
import permafrost.tundra.server.ServerThreadPoolExecutor;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

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
    protected ExecutorService executor;

    /**
     * Creates a new ConcurrentWriter object.
     *
     * @param writer The Writer this object delegates to.
     */
    protected ConcurrentWriter(Writer writer) {
        super(writer);
        start();
    }

    @Override
    public void write(final int c) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write(c);
                    out.flush();
                } catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void write(char[] cbuf, final int off, final int len) {
        final char[] copy = Arrays.copyOf(cbuf, cbuf.length);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write(copy, off, len);
                    out.flush();
                } catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void write(final String str, final int off, final int len) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write(str, off, len);
                    out.flush();
                } catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            executor = createExecutor();
            started = true;
        }
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            executor.shutdown();
            executor = null;
            started = false;
        }
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


    /**
     * Creates a task executor used for deferring write calls to.
     * @return a task executor used for deferring write calls to.
     */
    protected ExecutorService createExecutor() {
        return new ServerThreadPoolExecutor(1, getExecutorThreadPrefix(), getExecutorThreadSuffix(), getExecutorThreadPriority(), new LinkedBlockingDeque<Runnable>(getExecutorTaskQueueCapacity()),  new BlockingRejectedExecutionHandler());
    }

    /**
     * Returns the pending task queue capacity used when creating the task executor on startup.
     * @return the pending task queue capacity used when creating the task executor on startup.
     */
    protected int getExecutorTaskQueueCapacity() {
        return 8192;
    }

    /**
     * Returns the thread name prefix used when creating the task executor on startup.
     * @return the thread name prefix used when creating the task executor on startup.
     */
    protected String getExecutorThreadPrefix() {
        return "Tundra/" + this.getClass().getSimpleName();
    }

    /**
     * Returns the thread name suffix used when creating the task executor on startup.
     * @return the thread name suffix used when creating the task executor on startup.
     */
    protected String getExecutorThreadSuffix() {
        return null;
    }

    /**
     * Returns the thread priority used when creating the task executor on startup.
     * @return the thread priority used when creating the task executor on startup.
     */
    protected int getExecutorThreadPriority() {
        return Thread.MIN_PRIORITY;
    }
}
