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

package permafrost.tundra.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.wm.app.b2b.server.InvokeState;
import permafrost.tundra.server.ServerThreadFactory;

/**
 * A Integration Server Thread Executor.
 */
public class ServerThreadPoolExecutor extends ThreadPoolExecutor {
    /**
     * How long an idle thread is kept alive in the pool.
     */
    protected static final long DEFAULT_THREAD_KEEP_ALIVE_TIMEOUT_SECONDS = 60;
    /**
     * How long to wait for a normal orderly shutdown to finish.
     */
    protected static final long DEFAULT_SHUTDOWN_TIMEOUT_SECONDS = 120;

    /**
     * Creates a new ServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority used for each thread created.
     * @param workQueue        The queue to use for storing submitted jobs prior to their execution by a thread.
     * @param handler          The policy used to handle when a submitted job is rejected due to resource exhaustion.
     * @param threadPriority   The priority each created thread will have.
     */
    public ServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, String threadNameSuffix, int threadPriority, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(threadPoolSize, threadNamePrefix, threadNameSuffix, threadPriority, false, InvokeState.getCurrentState(), workQueue, handler);
    }

    /**
     * Creates a new ServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority used for each thread created.
     * @param threadDaemon     Whether the created threads should be daemons.
     * @param invokeState      The invoke state to clone for each thread created.
     * @param workQueue        The queue to use for storing submitted jobs prior to their execution by a thread.
     * @param handler          The policy used to handle when a submitted job is rejected due to resource exhaustion.
     * @param threadPriority   The priority each created thread will have.
     */
    public ServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, String threadNameSuffix, int threadPriority, boolean threadDaemon, InvokeState invokeState, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(threadPoolSize, threadPoolSize, DEFAULT_THREAD_KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS, workQueue, new ServerThreadFactory(threadNamePrefix, threadNameSuffix, threadPriority, threadDaemon, invokeState), handler);
    }

    /**
     * Shutdown the pool immediately, wait for a bit, and then stop all threads forcibly. This is unfortunately
     * required because service invocations do not respond to Thread.interrupt().
     */
    @Override
    public void shutdown() {
        try {
            super.shutdown();
            awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch(InterruptedException ex) {
            // ignore interruption to this thread
        } finally {
            shutdownNow();
        }
    }
}
