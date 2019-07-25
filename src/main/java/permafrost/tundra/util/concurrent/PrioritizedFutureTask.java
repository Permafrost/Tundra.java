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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * A FutureTask that supports task prioritization.
 *
 * @param <T> The class of the return value.
 */
public class PrioritizedFutureTask<T> extends FutureTask<T> implements Prioritized {
    /**
     * The priority of this task.
     */
    protected volatile Priority priority;

    /**
     * Creates a new task.
     *
     * @param runnable  The runnable that this task will run.
     * @param result    The class of the return value.
     */
    public PrioritizedFutureTask(Runnable runnable, T result) {
        this(runnable, result, runnable instanceof Prioritized ? ((Prioritized)runnable).getPriority() : null);
    }

    /**
     * Creates a new task.
     *
     * @param runnable  The runnable that this task will run.
     * @param result    The class of the return value.
     * @param priority  The priority of this task.
     */
    public PrioritizedFutureTask(Runnable runnable, T result, Priority priority) {
        super(runnable, result);
        this.priority = priority;
    }

    /**
     * Creates a new task.
     *
     * @param callable  The callable that this task will call.
     */
    public PrioritizedFutureTask(Callable<T> callable) {
        this(callable, callable instanceof Prioritized ? ((Prioritized)callable).getPriority() : null);
    }

    /**
     * Creates a new task.
     *
     * @param callable  The callable that this task will call.
     * @param priority  The priority of this task.
     */
    public PrioritizedFutureTask(Callable<T> callable, Priority priority) {
        super(callable);
        this.priority = priority;
    }

    /**
     * Returns the priority of this object, where larger values are higher priority, and with a nominal range of 1..10.
     *
     * @return the priority of this object, where larger values are higher priority, and with a nominal range of 1..10.
     */
    @Override
    public Priority getPriority() {
        return priority;
    }

    /**
     * Compares the priority of this task with another task.
     *
     * @param other The other task to be compared to.
     * @return      Whether this task is less than, equal to, or greater than the other task.
     */
    @Override
    public int compareTo(Prioritized other) {
        return AbstractPrioritized.compareTo(this, other);
    }
}