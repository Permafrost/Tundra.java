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

package permafrost.tundra.server.service;

import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceThread;
import com.wm.app.b2b.server.Session;
import com.wm.data.IData;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSName;
import java.util.concurrent.Callable;

/**
 * Wraps a call to a webMethods Integration Server service with the Callable and Runnable interface.
 */
public class CallableService implements Callable<IData> {
    /**
     * The service to be invoked.
     */
    private NSName service;

    /**
     * The pipeline the service is invoked with.
     */
    private IData pipeline;

    /**
     * The session the service is invoked under.
     */
    private Session session;

    /**
     * Constructs a new CallableService for invoking a webMethods Integration Server service via the Callable
     * interface.
     *
     * @param service  The service to be invoked.
     */
    public CallableService(NSName service) {
        this(service, IDataFactory.create());
    }

    /**
     * Constructs a new CallableService for invoking a webMethods Integration Server service via the Callable
     * interface.
     *
     * @param service  The service to be invoked.
     * @param pipeline The input pipeline the service is invoked with.
     */
    public CallableService(NSName service, IData pipeline) {
        this(service, Service.getSession(), pipeline);
    }

    /**
     * Constructs a new CallableService for invoking a webMethods Integration Server service via the Callable
     * interface.
     *
     * @param service  The service to be invoked.
     * @param session  The session to invoke the service under.
     * @param pipeline The input pipeline the service is invoked with.
     */
    public CallableService(NSName service, Session session, IData pipeline) {
        if (service == null) throw new NullPointerException("service must not be null");

        this.service = service;
        this.pipeline = pipeline;
        this.session = session;
    }

    /**
     * Invokes the specified service with the provided pipeline and session.
     *
     * @return The output pipeline returned by the invocation.
     * @throws Exception if the service encounters an error.
     */
    @Override
    public IData call() throws Exception {
        return Service.doInvoke(service, session, pipeline);
    }

    /**
     * Invokes the specified service on a different thread with the provided pipeline and session.
     *
     * @return The thread on which the service is invoked.
     */
    public ServiceThread fork() {
        return Service.doThreadInvoke(service, session, pipeline);
    }
}
