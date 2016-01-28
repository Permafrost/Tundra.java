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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An ExecutorService implementation which runs commands immediately on the current thread.
 */
public class DirectExecutorService extends AbstractExecutorService {
    /**
     * Whether the service has been shut down.
     */
    protected AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Returns true if this executor has been shut down.
     *
     * @return True if this executor has been shut down.
     */
    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    /**
     * Returns true if all tasks have completed following shut down. Note that isTerminated is never
     * true unless either shutdown or shutdownNow was called first.
     *
     * @return True if all tasks have completed following shut down.
     */
    @Override
    public boolean isTerminated() {
        return isShutdown.get();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs,
     * or the current thread is interrupted, whichever happens first.
     *
     * @param timeout The maximum time to wait.
     * @param unit    The time unit of the timeout argument.
     * @return        True if this executor terminated and false if the timeout elapsed before termination.
     * @throws InterruptedException If interrupted while waiting.
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return isShutdown.get();
    }

    /**
     * Shuts down the service immediately.
     *
     * @return The list of tasks that never commenced execution; always empty.
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    /**
     * Shuts down the service.
     */
    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    /**
     * Executes the given command immediately in the current thread.
     *
     * @param command The runnable task.
     */
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
