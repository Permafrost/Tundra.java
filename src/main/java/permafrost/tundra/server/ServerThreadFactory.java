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
import permafrost.tundra.id.ULID;
import permafrost.tundra.lang.ThreadHelper;
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
    protected String threadNamePrefix, threadNameSuffix;

    /**
     * The count of threads created by this factory, used to suffix thread names.
     */
    protected AtomicLong count = new AtomicLong(1);

    /**
     * The priority threads will be created with.
     */
    protected int threadPriority;

    /**
     * The invoke state threads will be created with.
     */
    protected InvokeState invokeState;

    /**
     * Whether created threads should be daemon threads.
     */
    protected boolean daemon;

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, InvokeState invokeState) {
        this(threadNamePrefix, null, invokeState);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState invokeState) {
        this(threadNamePrefix, threadNameSuffix, Thread.NORM_PRIORITY, invokeState);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The prefix used on all created thread names.
     * @param threadPriority   The priority used for each thread created.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, int threadPriority, InvokeState invokeState) {
        this(threadNamePrefix, null, threadPriority, invokeState);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority used for each thread created.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, int threadPriority, InvokeState invokeState) {
        this(threadNamePrefix, threadNameSuffix, threadPriority, false, invokeState);
    }

    /**
     * Constructs a new ServerThreadFactory.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param threadPriority   The priority used for each thread created.
     * @param daemon           Whether the created threads should be daemon threads.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, int threadPriority, boolean daemon, InvokeState invokeState) {
        if (threadNamePrefix == null) throw new NullPointerException("threadNamePrefix must not be null");
        if (invokeState == null) throw new NullPointerException("invokeState must not be null");

        this.threadNamePrefix = threadNamePrefix;
        this.threadNameSuffix = threadNameSuffix;
        this.invokeState = invokeState;
        this.threadPriority = ThreadHelper.normalizePriority(threadPriority);
        this.daemon = daemon;
    }

    /**
     * Returns a newly constructed Thread that will execute the given Runnable.
     *
     * @param runnable  The Runnable to be executed by the thread.
     * @return          The newly constructed thread.
     */
    @Override
    public Thread newThread(Runnable runnable) {
        ServerThread thread = new ServerThread(runnable);
        thread.setInvokeState(cloneInvokeStateWithStack());
        String threadContext = ULID.generate();
        if (threadNameSuffix != null) {
            thread.setName(String.format("%s #%03d ThreadContext=%s %s", threadNamePrefix, count.getAndIncrement(), threadContext, threadNameSuffix));
        } else {
            thread.setName(String.format("%s #%03d ThreadContext=%s", threadNamePrefix, count.getAndIncrement(), threadContext));
        }
        thread.setUncaughtExceptionHandler(UncaughtExceptionLogger.getInstance());
        thread.setPriority(threadPriority);
        thread.setDaemon(daemon);
        return thread;
    }

    /**
     * Clones the invoke invokeState with its call stack intact.
     *
     * @return A clone of the invoke invokeState used for new threads.
     */
    protected InvokeState cloneInvokeStateWithStack() {
        InvokeState outputState = (InvokeState)invokeState.clone();
        Stack stack = (Stack)invokeState.getCallStack().clone();
        while (!stack.empty()) {
            NSService service = (NSService)stack.remove(0);
            outputState.pushService(service);
        }
        return outputState;
    }
}
