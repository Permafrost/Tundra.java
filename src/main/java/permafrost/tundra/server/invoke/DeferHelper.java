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

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Service;
import com.wm.data.IData;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.server.ServerThreadFactory;
import permafrost.tundra.server.service.CallableService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A collection of convenience methods for invoking services.
 */
public final class DeferHelper {
    /**
     * The amount of time to wait for all queued deferred services to finish executing before shutdown.
     */
    private static final long SHUTDOWN_TIMEOUT_MINUTES = 5;

    /**
     * Disallow instantiation of this class.
     */
    private DeferHelper() {}

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * Singleton instance of executor service for invoking deferred services.
         */
        private static final ExecutorService DEFER_EXECUTOR = Executors.newSingleThreadExecutor(new ServerThreadFactory("Tundra/Defer Worker", InvokeState.getCurrentState()));
    }

    /**
     * Queues the given service and input pipeline for execution later by a dedicated single thread.
     *
     * @param service   The service to be executed some time later.
     * @param pipeline  The input pipeline for the service.
     */
    public static void defer(String service, IData pipeline) {
        defer(NSName.create(service), pipeline);
    }

    /**
     * Queues the given service and input pipeline for execution later by a dedicated single thread.
     *
     * @param service   The service to be executed some time later.
     * @param pipeline  The input pipeline for the service.
     */
    public static void defer(NSName service, IData pipeline) {
        Holder.DEFER_EXECUTOR.submit(new CallableService(service, IDataHelper.duplicate(pipeline)));
    }

    /**
     * Shuts down the thread pool used for executing deferred services.
     */
    public static void shutdown() {
        try {
            Holder.DEFER_EXECUTOR.shutdown();
            Holder.DEFER_EXECUTOR.awaitTermination(SHUTDOWN_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        } catch(InterruptedException ex) {
            // ignore interruption to this thread
        } finally {
            Holder.DEFER_EXECUTOR.shutdownNow();
        }
    }
}
