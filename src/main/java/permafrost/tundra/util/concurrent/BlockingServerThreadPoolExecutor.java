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

package permafrost.tundra.util.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServerAPI;
import permafrost.tundra.server.ServerThreadFactory;

/**
 * A Integration Server ThreadPoolExecutor which blocks on submit when all threads are busy, and forcibly stops
 * all threads on shutdown.
 */
public class BlockingServerThreadPoolExecutor extends ThreadPoolExecutor {
    private static final long THREAD_KEEP_ALIVE_TIMEOUT_SECONDS = 60;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 60;

    /**
     * Contains all executing commands and the thread that is executing it.
     */
    protected Map<Runnable, Thread> executingThreads;

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     * @param threadPriority   The priority each created thread will have.
     */
    BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, InvokeState invokeState, int threadPriority) {
        super(threadPoolSize, threadPoolSize, THREAD_KEEP_ALIVE_TIMEOUT_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true), new ServerThreadFactory(threadNamePrefix, invokeState, threadPriority), new BlockingRejectedExecutionHandler());
        executingThreads = new ConcurrentHashMap<Runnable, Thread>(threadPoolSize);
    }

    /**
     * Save a reference to the executing command and thread.
     *
     * @param thread  The thread allocated to execute this command.
     * @param command The command to be executed.
     */
    @Override
    protected void beforeExecute(Thread thread, Runnable command) {
        executingThreads.put(command, thread);
        super.beforeExecute(thread, command);
    }

    /**
     * Remove this command from the list of currently executing threads.
     *
     * @param command   The command that has finished executing.
     * @param exception The uncaught exception the command threw, if relevant.
     */
    @Override
    protected void afterExecute(Runnable command, Throwable exception) {
        executingThreads.remove(command);
        if (exception != null) ServerAPI.logError(exception);
        super.afterExecute(command, exception);
    }

    /**
     * Shutdown the pool immediately, wait for a bit, and then stop all threads forcibly. This is unfortunately
     * required because service invocations do not respond to Thread.interrupt().
     */
    void onShutdown() {
        try {
            shutdownNow();
            awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch(InterruptedException ex) {
            // ignore interruption
        }

        for (Map.Entry<Runnable, Thread> executingThread : executingThreads.entrySet()) {
            Thread thread = executingThread.getValue();
            if (thread != null) thread.stop();
        }
    }
}
