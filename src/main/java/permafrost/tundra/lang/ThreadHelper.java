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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import java.util.Arrays;
import java.util.Date;
import javax.xml.datatype.Duration;

/**
 * A collection of convenience methods for working with Thread objects.
 */
public class ThreadHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ThreadHelper() {}

    /**
     * Returns the currently executing thread.
     *
     * @return The currently executing thread.
     */
    public static Thread getCurrentThread() {
        return Thread.currentThread();
    }

    /**
     * Returns the currently executing thread in an IData representation.
     *
     * @return The currently executing thread in an IData representation.
     */
    public static IData getCurrentThreadAsIData() {
        return toIData(Thread.currentThread());
    }

    /**
     * Returns a list of all the threads in the current context.
     *
     * @return A list of all the threads in the current context.
     */
    public static Thread[] listThreads() {
        ThreadGroup root = getRootThreadGroup();

        int threadCount = 0, iteration = 0;
        Thread[] list = new Thread[threadCount];

        // because ThreadGroup.enumerate isn't thread save, keep trying to
        // enumerate for up to 10 times until we happen to have an array
        // large enough to hold all the threads that exist at the moment
        // enumerate is called
        while (iteration < 10 && threadCount >= list.length) {
            list = new Thread[root.activeCount() + (500 * ++iteration)];
            threadCount = root.enumerate(list, true);
        }

        return Arrays.copyOf(list, threadCount);
    }

    /**
     * Returns a list of all threads in the current context in an IData[] representation.
     *
     * @return A list of all threads in the current context in an IData[] representation.
     */
    public static IData[] listThreadsAsIDataArray() {
        return toIDataArray(listThreads());
    }

    /**
     * Returns the root thread group.
     *
     * @return The root thread group.
     */
    public static ThreadGroup getRootThreadGroup() {
        ThreadGroup group = getCurrentThread().getThreadGroup();
        ThreadGroup parent = group.getParent();

        while (parent != null) {
            group = parent;
            parent = group.getParent();
        }

        return group;
    }

    /**
     * Sleeps the current thread for the given duration.
     *
     * @param duration The duration to sleep.
     */
    public static void sleep(Duration duration) {
        if (duration != null) {
            sleep(duration.getTimeInMillis(new Date()));
        }
    }

    /**
     * Sleeps the current thread for the given duration.
     *
     * @param milliseconds The number of milliseconds to sleep.
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Converts a Thread object to an IData representation.
     *
     * @param thread The Thread to be converted.
     * @return An IData representation of the given Thread.
     */
    public static IData toIData(Thread thread) {
        if (thread == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        IDataUtil.put(cursor, "id", "" + thread.getId());
        IDataUtil.put(cursor, "name", thread.getName());
        IDataUtil.put(cursor, "description", thread.toString());
        IDataUtil.put(cursor, "state", thread.getState().toString());
        IDataUtil.put(cursor, "priority", "" + thread.getPriority());

        ThreadGroup group = thread.getThreadGroup();
        if (group != null) IDataUtil.put(cursor, "group", group.getName());

        IDataUtil.put(cursor, "alive?", "" + thread.isAlive());
        IDataUtil.put(cursor, "interrupted?", "" + thread.isInterrupted());
        IDataUtil.put(cursor, "daemon?", "" + thread.isDaemon());

        IData[] stack = StackTraceElementHelper.toIDataArray(thread.getStackTrace());
        if (stack != null) {
            IDataUtil.put(cursor, "stack", stack);
            IDataUtil.put(cursor, "stack.length", "" + stack.length);
        } else {
            IDataUtil.put(cursor, "stack.length", "0");
        }

        IDataUtil.put(cursor, "thread", thread);

        cursor.destroy();

        return output;
    }

    /**
     * Converts the given Thread[] to an IData[] representation.
     *
     * @param threads The Thread[] to be converted.
     * @return An IData[] representation of the given Thread[].
     */
    public static IData[] toIDataArray(Thread... threads) {
        if (threads == null) return null;

        IData[] output = new IData[threads.length];

        for (int i = 0; i < threads.length; i++) {
            output[i] = toIData(threads[i]);
        }

        return output;
    }
}