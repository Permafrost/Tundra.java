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
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;
import permafrost.tundra.lang.Startable;
import java.util.Iterator;

/**
 * An abstract base class for invoke chain processors.
 */
public abstract class AbstractInvokeChainProcessor implements InvokeChainProcessor, Startable {
    /**
     * Whether the processor is started or not.
     */
    protected volatile boolean started = false;

    /**
     * Processes a service invocation.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        if (iterator.hasNext()) ((InvokeChainProcessor)iterator.next()).process(iterator, baseService, pipeline, serviceStatus);
    }

    /**
     * Returns true if this processor is started.
     *
     * @return True if this processor is started.
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Registers this class as an invocation handler and starts processing.
     */
    public synchronized void start() {
        if (!started) {
            started = true;
            InvokeManager.getDefault().registerProcessor(this);
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops processing.
     */
    public synchronized void stop() {
        if (started) {
            started = false;
            InvokeManager.getDefault().unregisterProcessor(this);
        }
    }
}
