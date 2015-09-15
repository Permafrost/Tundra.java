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
import com.wm.app.b2b.server.Session;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;

/**
 * Wraps a call to a webMethods Integration Server service with the Runnable interface.
 */
public class RunnableService implements Runnable {
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
     * Constructs a new RunnableService for invoking a webMethods Integration Server service via the Runnable
     * interface.
     *
     * @param service  The fully-qualified service name to be invoked.
     * @param session  The session to invoke the service under.
     * @param pipeline The input pipeline the service is invoked with.
     */
    public RunnableService(String service, Session session, IData pipeline) {
        this(NSName.create(service), session, pipeline);
    }

    /**
     * Constructs a new RunnableService for invoking a webMethods Integration Server service via the Runnable
     * interface.
     *
     * @param service  The service to be invoked.
     * @param session  The session to invoke the service under.
     * @param pipeline The input pipeline the service is invoked with.
     */
    public RunnableService(NSName service, Session session, IData pipeline) {
        this.service = service;
        this.pipeline = pipeline;
        this.session = session;
    }

    /**
     * Invokes the specified service with the provided pipeline and session.
     */
    @Override
    public void run() {
        try {
            Service.doInvoke(service, session, pipeline);
        } catch(Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            } else {
                throw new RuntimeException(ex);
            }
        }
    }
}
