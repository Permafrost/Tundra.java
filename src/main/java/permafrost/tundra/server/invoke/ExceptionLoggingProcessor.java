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
import com.wm.app.b2b.server.ServerAPI;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;
import java.util.Iterator;

/**
 * A service invocation processor that logs service exceptions to the server error log.
 */
public class ExceptionLoggingProcessor extends AbstractInvokeChainProcessor {
    /**
     * Whether only top level service exceptions should be logged.
     */
    private volatile boolean topServiceOnly = false;

    /**
     * Creates a new logging exception handler.
     */
    public ExceptionLoggingProcessor() {}

    /**
     * Creates a new pipeline capture processor.
     *
     * @param topServiceOnly    Whether only top level service exceptions should be logged.
     */
    public ExceptionLoggingProcessor(boolean topServiceOnly) {
        this.topServiceOnly = topServiceOnly;
    }

    /**
     * Returns true if only the top level service exceptions are being logged.
     *
     * @return True if only the top level service exceptions are being logged.
     */
    public boolean getTopServiceOnly() {
        return topServiceOnly;
    }

    /**
     * Sets whether only top level service exceptions should be logged.
     *
     * @param topServiceOnly    Whether only top level service exceptions should be logged.
     */
    public void setTopServiceOnly(boolean topServiceOnly) {
        this.topServiceOnly = topServiceOnly;
    }

    /**
     * Processes a service invocation by logging any exceptions thrown.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        try {
            super.process(iterator, baseService, pipeline, serviceStatus);
        } catch(Throwable exception) {
            if (!topServiceOnly || serviceStatus.isTopService()) ServerAPI.logError(exception);
        }
    }
}
