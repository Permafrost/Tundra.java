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

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;
import permafrost.tundra.server.UserHelper;
import permafrost.tundra.time.DateTimeHelper;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * A service invocation processor that sets more descriptive thread names on invocation threads.
 */
public class ThreadNameProcessor extends AbstractInvokeChainProcessor {
    /**
     * The datetime pattern used to format service start time.
     */
    private static final String DATETIME_PATTERN = DateTimeHelper.DEFAULT_DATETIME_PATTERN;
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final ThreadNameProcessor INSTANCE = new ThreadNameProcessor();
    }

    /**
     * Creates a new pipeline capture processor.
     */
    private ThreadNameProcessor() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static ThreadNameProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Processes a service invocation by updating the current thread name to include the service being executed and
     * its start time as a suffix.
     *
     * @param iterator          The invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        String originalThreadName = Thread.currentThread().getName();
        try {
            String serviceName = baseService.getNSName().getFullName();
            long startTime = serviceStatus.getStartTime();
            Thread.currentThread().setName(MessageFormat.format("{0} ({1} ► {2} @ {3})", originalThreadName.replace(" (" + serviceName + ")", ""), UserHelper.getCurrentName(), serviceName, startTime == 0L ? DateTimeHelper.now(DATETIME_PATTERN) : DateTimeHelper.format(startTime, DATETIME_PATTERN)));
            super.process(iterator, baseService, pipeline, serviceStatus);
        } finally {
            Thread.currentThread().setName(originalThreadName);
        }
    }
}
