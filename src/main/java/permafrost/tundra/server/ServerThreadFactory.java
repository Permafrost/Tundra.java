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

package permafrost.tundra.server;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServerThread;
import com.wm.lang.ns.NSService;
import java.util.Stack;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread factory that creates webMethods Integration Server ServerThread threads.
 */
public class ServerThreadFactory implements ThreadFactory {
    /**
     * The prefix and suffix used on all created thread names.
     */
    private String threadNamePrefix, threadNameSuffix;

    /**
     * The count of threads created by this factory, used to suffix thread names.
     */
    private AtomicLong count = new AtomicLong(1);

    /**
     * The priority threads will be created with.
     */
    private int priority;

    /**
     * The invoke state threads will be created with.
     */
    private InvokeState state;

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param state The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, InvokeState state) {
        this(threadNamePrefix, null, state);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param state The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState state) {
        this(threadNamePrefix, threadNameSuffix, state, Thread.NORM_PRIORITY);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param state The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, InvokeState state, int priority) {
        this(threadNamePrefix, null, state, priority);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix  The threadNamePrefix of the factory, used to prefix thread names.
     * @param state The invoke state to clone for each thread created.
     * @param priority The priority used for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState state, int priority) {
        if (threadNamePrefix == null) throw new NullPointerException("threadNamePrefix must not be null");
        if (state == null) throw new NullPointerException("state must not be null");
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("priority out of range");
        }

        this.threadNamePrefix = threadNamePrefix;
        this.threadNameSuffix = threadNameSuffix;
        this.state = state;
        this.priority = priority;
    }

    /**
     * Returns a newly constructed Thread that will execute the given Runnable.
     *
     * @param runnable The Runnable to be executed by the thread.
     * @return The newly constructed thread.
     */
    @Override
    public Thread newThread(Runnable runnable) {
        ServerThread thread = new ServerThread(runnable);
        thread.setInvokeState(cloneInvokeStateWithStack());
        if (threadNameSuffix != null) {
            thread.setName(String.format("%s Thread#%03d %s", threadNamePrefix, count.getAndIncrement(), threadNameSuffix));
        } else {
            thread.setName(String.format("%s Thread#%03d", threadNamePrefix, count.getAndIncrement()));
        }
        thread.setUncaughtExceptionHandler(UncaughtExceptionLogger.getInstance());
        thread.setPriority(priority);
        return thread;
    }

    /**
     * Clones the invoke state with its call stack intact.
     *
     * @return A clone of the invoke state used for new threads.
     */
    private InvokeState cloneInvokeStateWithStack() {
        InvokeState outputState = (InvokeState)state.clone();
        Stack stack = (Stack)state.getCallStack().clone();
        while (!stack.empty()) {
            NSService service = (NSService)stack.remove(0);
            outputState.pushService(service);
        }
        return outputState;
    }
}
