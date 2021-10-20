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

package permafrost.tundra.server;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Session;
import permafrost.tundra.id.UUIDHelper;
import permafrost.tundra.lang.ThreadHelper;
import java.util.concurrent.ThreadFactory;

/**
 * A thread factory that creates webMethods Integration Server ServerThread threads.
 */
public class ServerThreadFactory implements ThreadFactory {
    /**
     * The prefix used on all created thread names.
     */
    protected String threadNamePrefix;
    /**
     * The optional suffix used on all created thread names.
     */
    protected String threadNameSuffix;
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
     * Creates a new ServerThreadFactory object.
     *
     * @param threadNamePrefix  The threadNamePrefix of the factory, used to prefix thread names.
     * @param invokeState       The invoke state to clone for each thread created.
     */
    public ServerThreadFactory(String threadNamePrefix, InvokeState invokeState) {
        this(threadNamePrefix, null, invokeState, Thread.NORM_PRIORITY, false);
    }

    /**
     * Creates a new ServerThreadFactory object.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     * @param threadPriority   The priority used for each thread created.
     * @param daemon           Whether the created threads should be daemon threads.
     */
    public ServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState invokeState, int threadPriority, boolean daemon) {
        if (threadNamePrefix == null) throw new NullPointerException("threadNamePrefix must not be null");
        if (invokeState == null) throw new NullPointerException("invokeState must not be null");

        this.threadNamePrefix = threadNamePrefix;
        this.threadNameSuffix = threadNameSuffix;
        this.threadPriority = ThreadHelper.normalizePriority(threadPriority);
        this.invokeState = invokeState;
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
        thread.setName(newThreadName());

        InvokeState invokeState = InvokeStateHelper.clone(this.invokeState);
        Session session = new Session(UUIDHelper.generate(), Long.MAX_VALUE, thread.getName());
        session.setUser(invokeState.getUser());
        invokeState.setSession(session);
        invokeState.setCheckAccess(false);
        thread.setInvokeState(invokeState);

        thread.setUncaughtExceptionHandler(UncaughtExceptionLogger.getInstance());
        thread.setPriority(threadPriority);
        thread.setDaemon(daemon);

        return thread;
    }

    /**
     * Returns a thread name for a newly generated thread.
     *
     * @return a thread name for a newly generated thread.
     */
    protected String newThreadName() {
        String threadName;

        String threadContext = UUIDHelper.generate();

        if (threadNameSuffix != null) {
            threadName = String.format("%s %s %s", threadNamePrefix, threadContext, threadNameSuffix);
        } else {
            threadName = String.format("%s %s", threadNamePrefix, threadContext);
        }

        return threadName;
    }
}
