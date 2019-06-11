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
import permafrost.tundra.id.UUIDHelper;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A thread factory that creates webMethods Integration Server ServerThread threads.
 */
public class CountingServerThreadFactory extends ServerThreadFactory {
    /**
     * The count of threads created by this factory, used to suffix thread names.
     */
    protected AtomicLong count = new AtomicLong(1);

    /**
     * Constructs a new CountingServerThreadFactory.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     */
    public CountingServerThreadFactory(String threadNamePrefix, InvokeState invokeState) {
        this(threadNamePrefix, null, invokeState, Thread.NORM_PRIORITY, false);
    }

    /**
     * Constructs a new CountingServerThreadFactory.
     *
     * @param threadNamePrefix The threadNamePrefix of the factory, used to prefix thread names.
     * @param threadNameSuffix The suffix used on all created thread names.
     * @param invokeState      The invoke state to clone for each thread created.
     * @param threadPriority   The priority used for each thread created.
     * @param daemon           Whether the created threads should be daemon threads.
     */
    public CountingServerThreadFactory(String threadNamePrefix, String threadNameSuffix, InvokeState invokeState, int threadPriority, boolean daemon) {
        super(threadNamePrefix, threadNameSuffix, invokeState, threadPriority, daemon);
    }

    /**
     * Returns a thread name for a newly generated thread.
     *
     * @return a thread name for a newly generated thread.
     */
    @Override
    protected String newThreadName() {
        String threadName;

        String threadContext = UUIDHelper.generate();
        long threadCount = count.getAndIncrement();

        if (threadNameSuffix != null) {
            threadName = String.format("%s #%03d Thread=%s %s", threadNamePrefix, threadCount, threadContext, threadNameSuffix);
        } else {
            threadName = String.format("%s #%03d Thread=%s", threadNamePrefix, threadCount, threadContext);
        }

        return threadName;
    }
}
