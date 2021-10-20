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

import com.wm.app.b2b.server.InvokeState;
import permafrost.tundra.util.concurrent.BlockingRejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;

/**
 * A Integration Server ThreadPoolExecutor which blocks on submit when all threads are busy.
 */
public class BlockingServerThreadPoolExecutor extends ServerThreadPoolExecutor {
    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize) {
        this(threadPoolSize, "Tundra");
    }

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix) {
        this(threadPoolSize, threadNamePrefix, Thread.NORM_PRIORITY);
    }

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadPriority   The priority each created thread will have.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, int threadPriority) {
        this(threadPoolSize, threadNamePrefix, threadPriority, false);
    }

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadPriority   The priority each created thread will have.
     * @param threadDaemon     Whether the created threads should be daemons.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, int threadPriority, boolean threadDaemon) {
        this(threadPoolSize, threadNamePrefix, null, threadPriority, threadDaemon);
    }

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority each created thread will have.
     * @param threadDaemon     Whether the created threads should be daemons.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, String threadNameSuffix, int threadPriority, boolean threadDaemon) {
        this(threadPoolSize, threadNamePrefix, threadNameSuffix, threadPriority, threadDaemon, InvokeState.getCurrentState());
    }

    /**
     * Creates a new BlockingServerThreadPoolExecutor.
     *
     * @param threadPoolSize   The number of threads to allocate to the pool.
     * @param threadNamePrefix The prefix to use on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority each created thread will have.
     * @param threadDaemon     Whether the created threads should be daemons.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public BlockingServerThreadPoolExecutor(int threadPoolSize, String threadNamePrefix, String threadNameSuffix, int threadPriority, boolean threadDaemon, InvokeState invokeState) {
        super(threadPoolSize, threadNamePrefix, threadNameSuffix, threadPriority, threadDaemon, invokeState, new SynchronousQueue<Runnable>(true), new BlockingRejectedExecutionHandler());
    }
}
