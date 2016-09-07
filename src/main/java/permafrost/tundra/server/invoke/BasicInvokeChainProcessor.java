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
import java.util.Iterator;

/**
 * Provides a basic implementation of a invocation chain processor.
 */
public abstract class BasicInvokeChainProcessor extends AbstractInvokeChainProcessor {
    /**
     * Called prior to the service invocation. Subclasses should override this method to perform work prior to an
     * invocation.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    protected void processBefore(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {}

    /**
     * Calls the remaining service invocation chain processors.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    protected void processChain(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        super.process(iterator, baseService, pipeline, serviceStatus);
    }

    /**
     * Called after the service invocation. Subclasses should override this method to perform work after an invocation
     * which did not throw an exception.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    protected void processAfter(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {}

    /**
     * Called after the service invocation if it threw an exception. Subclasses should override this method to perform
     * work after an invocation threw an exception.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @param exception         The exception thrown by the invocation.
     * @throws ServerException  If the service invocation fails.
     */
    protected void processCatch(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus, Throwable exception) throws ServerException {
        if (exception instanceof ServerException) {
            throw (ServerException)exception;
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException)exception;
        } else {
            throw new ServerException(exception);
        }
    }

    /**
     * Called after the service invocation whether it threw an exception or not. Subclasses should override this method
     * to perform work after an invocation regardless of its success.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    protected void processFinal(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {}

    /**
     * Processes a service invocation.
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    public void processMain(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        try {
            processBefore(iterator, baseService, pipeline, serviceStatus);
            processChain(iterator, baseService, pipeline, serviceStatus);
            processAfter(iterator, baseService, pipeline, serviceStatus);
        } catch(Throwable ex) {
            processCatch(iterator, baseService, pipeline, serviceStatus, ex);
        } finally {
            processFinal(iterator, baseService, pipeline, serviceStatus);
        }
    }

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
        processMain(iterator, baseService, pipeline, serviceStatus);
    }
}
