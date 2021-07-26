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
import com.wm.app.b2b.server.ISRuntimeException;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.lang.ns.NSService;
import com.wm.util.ServerException;
import permafrost.tundra.lang.RecoverableException;
import permafrost.tundra.lang.UnrecoverableException;
import permafrost.tundra.server.ServiceHelper;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Converts recoverable exceptions thrown by a registered service invocation to be instances of ISRuntimeException to
 * support being retried.
 */
public class RetryableServiceProcessor extends AbstractInvokeChainProcessor {
    /**
     * Exceptions whose message match this pattern will not be converted to an ISRuntimeException.
     */
    protected static Pattern EXCLUDED_EXCEPTION_MESSAGE_PATTERN = Pattern.compile("(ISC\\.0049\\.9005|ISC\\.0049\\.9006)");
    /**
     * The service invocation instances in which to convert exceptions to be instances of ISRuntimeException.
     */
    protected ConcurrentMap<Thread, String> registry = new ConcurrentHashMap<Thread, String>();

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final RetryableServiceProcessor INSTANCE = new RetryableServiceProcessor();
    }

    /**
     * Disallow instantiation of this class.
     */
    private RetryableServiceProcessor() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static RetryableServiceProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Registers the calling service for processing by this processor, unless a parent service is already registered.
     */
    public void register() {
        register(ServiceHelper.self());
    }

    /**
     * Registers the given service for processing by this processor, unless a parent service is already registered.
     *
     * @param service       The service to be registered.
     */
    protected void register(NSService service) {
        register(service == null ? null : service.getNSName().getFullName());
    }

    /**
     * Registers the given service for processing by this processor, unless a parent service is already registered.
     *
     * @param serviceName   The service to be registered.
     */
    protected void register(String serviceName) {
        if (started && serviceName != null) {
            registry.putIfAbsent(Thread.currentThread(), serviceName);
        }
    }

    /**
     * Processes a service invocation: if invoked by a trigger, converts all exceptions thrown to be instances of
     * ISRuntimeException so that the service can be retried.
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
        } catch(Throwable ex) {
            if (registry.remove(Thread.currentThread(), baseService.getNSName().getFullName()) && !EXCLUDED_EXCEPTION_MESSAGE_PATTERN.matcher(ex.getMessage()).find()) {
                if (ex instanceof UnrecoverableException) {
                    // do not retry unrecoverable exceptions
                    throw (UnrecoverableException)ex;
                } else if (ex instanceof ISRuntimeException) {
                    // rethrow if exception is already recoverable
                    throw (ISRuntimeException)ex;
                } else {
                    // convert all other exceptions to be instances of ISRuntimeException
                    throw new RecoverableException(ex);
                }
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            } else if (ex instanceof ServerException) {
                throw (ServerException)ex;
            } else {
                throw new ServerException(ex);
            }
        } finally {
            // clean up registry whether or not an exception was thrown
            registry.remove(Thread.currentThread(), baseService.getNSName().getFullName());
        }
    }

    /**
     * Registers this class as an invocation handler and starts saving pipelines.
     */
    public synchronized void start() {
        if (!started) {
            registry.clear();
            super.start();
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops saving pipelines.
     */
    public synchronized void stop() {
        if (started) {
            super.stop();
            registry.clear();
        }
    }
}
