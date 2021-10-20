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

import permafrost.tundra.server.ServerThread;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A ThreadPoolExecutor that supports prioritization of tasks.
 */
public class PrioritizedThreadPoolExecutor extends ThreadPoolExecutor {
    /**
     * Creates a PrioritizedThreadPoolExecutor.
     *
     * @param corePoolSize      The minimum threads in the pool.
     * @param maximumPoolSize   The maximum threads in the pool.
     * @param keepAliveTime     The timeout for idle threads.
     * @param unit              The unit of time for the keepAliveTime.
     * @param workQueue         The queue used for storing tasks yet to be executed.
     * @param threadFactory     The factory used to create new threads for the pool.
     * @param handler           The handler called when submitted task is rejected.
     */
    public PrioritizedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * Returns a RunnableFuture that supports prioritization of the given Callable.
     *
     * @param callable  The callable to wrap.
     * @param <T>       The class of the callable's return value.
     * @return          The RunnableFuture wrapping the given callable.
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PrioritizedFutureTask<T>(callable);
    }

    /**
     * Returns a RunnableFuture that supports prioritization of the given Runnable.
     *
     * @param runnable  The runnable to wrap.
     * @param value     The class of the callable's return value.
     * @param <T>       The class of the callable's return value.
     * @return          The RunnableFuture wrapping the given runnable.
     */
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PrioritizedFutureTask<T>(runnable, value);
    }

    /**
     * Performs any work required before a task is executed.
     *
     * @param thread    The thread which will execute the task.
     * @param runnable  The task to be executed.
     */
    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        if (thread instanceof ServerThread) {
            ((ServerThread)thread).setStartTime();
        }
        super.beforeExecute(thread, runnable);
    }

    /**
     * Performs any work required after a task was executed.
     *
     * @param runnable  The task that was executed.
     * @param throwable Any exception that was encountered during task execution.
     */
    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        super.afterExecute(runnable, throwable);
        Thread thread = Thread.currentThread();
        if (thread instanceof ServerThread) {
            ((ServerThread)thread).clearStartTime();;
        }
    }
}