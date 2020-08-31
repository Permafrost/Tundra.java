/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

package permafrost.tundra.server.http.handler;

import com.wm.app.b2b.server.AccessException;
import com.wm.app.b2b.server.HTTPDispatch;
import com.wm.app.b2b.server.HTTPHandler;
import com.wm.app.b2b.server.ProtocolState;
import permafrost.tundra.lang.Startable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The HTTP request manager.
 */
public class Manager implements Startable, HTTPHandler {
    /**
     * The list of HTTP request handlers used to handle requests.
     */
    private List<Handler> handlers;
    /**
     * The HTTP request handler which proxies request handling to the Integration Server registered handler.
     */
    private Kernel kernel;

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final Manager INSTANCE = new Manager();
    }

    /**
     * Is the manager started or stopped?
     */
    protected volatile boolean started = false;

    /**
     * Disallow instantiation of this class.
     */
    private Manager() {
        handlers = new CopyOnWriteArrayList<Handler>();
    }

    /**
     * Returns the HTTP handler manager.
     *
     * @return The HTTP handler manager.
     */
    public static Manager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns true if started.
     *
     * @return True if started.
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * Registers the given HTTP request handler for processing requests.
     *
     * @param handler The handler to be registered.
     */
    public void register(Handler handler) {
        handlers.add(0, handler);
    }

    /**
     * Unregisters the given HTTP request handler from processing requests.
     *
     * @param handler The handler to be unregistered.
     */
    public void unregister(Handler handler) {
        handlers.remove(handler);
    }

    /**
     * Handles an HTTP request via the registered handler chain.
     *
     * @param context           The HTTP request context.
     * @return                  True if the request was handled.
     * @throws IOException      If an IO error occurs.
     * @throws AccessException  If a security error occurs.
     */
    @Override
    public boolean process(ProtocolState context) throws IOException, AccessException {
        boolean result = false;

        Iterator<Handler> iterator = handlers.iterator();
        if (iterator.hasNext()) {
            result = iterator.next().handle(context, iterator);
        }

        return result;
    }

    /**
     * Starts the HTTP request manager.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            started = true;
            try {
                Map<String, HTTPHandler> registeredHandlers = getRegisteredHandlers();
                HTTPHandler defaultHandler = getRegisteredDefaultHandler();

                kernel = new Kernel(defaultHandler, registeredHandlers);
                handlers.add(kernel);

                setRegisteredDefaultHandler(this);

                for (Map.Entry<String, HTTPHandler> entry : registeredHandlers.entrySet()) {
                    HTTPDispatch.addHandler(entry.getKey(), this);
                }
            } catch(Throwable ex) {
                stop();
            }
        }
    }

    /**
     * Stops the HTTP handler manager.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            started = false;
            try {
                Map<String, HTTPHandler> registeredHandlers = getRegisteredHandlers();
                HTTPHandler defaultHandler = getRegisteredDefaultHandler();

                if (defaultHandler == this) {
                    setRegisteredDefaultHandler(kernel.getDefaultHandler());
                }

                for (Map.Entry<String, HTTPHandler> entry : registeredHandlers.entrySet()) {
                    if (entry.getValue() == this) {
                        HTTPHandler originalHandler = kernel.resolve(entry.getKey());
                        if (originalHandler == null) {
                            HTTPDispatch.removeHandler(entry.getKey());
                        } else {
                            HTTPDispatch.addHandler(entry.getKey(), originalHandler);
                        }
                    }
                }
            } finally {
                for (Handler handler : handlers) {
                    if (handler instanceof Startable) {
                        ((Startable)handler).stop();
                    }
                }
                handlers.clear();
                kernel = null;
            }
        }
    }

    /**
     * Restarts the HTTP handler manager.
     */
    @Override
    public synchronized void restart() {
        stop();
        start();
    }

    /**
     * Returns the currently registered Integration Server HTTP handlers as a Map.
     *
     * @return The currently registered Integration Server HTTP handlers as a Map.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, HTTPHandler> getRegisteredHandlers() {
        try {
            Field httpHandlers = HTTPDispatch.class.getDeclaredField("httpHandlers");
            httpHandlers.setAccessible(true);
            return (Map<String, HTTPHandler>)httpHandlers.get(null);
        } catch(NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns the current Integration Server default HTTP handler.
     *
     * @return The current Integration Server default HTTP handler.
     */
    @SuppressWarnings("unchecked")
    private static HTTPHandler getRegisteredDefaultHandler() {
        try {
            Field defaultHandler = HTTPDispatch.class.getDeclaredField("defaultHandler");
            defaultHandler.setAccessible(true);
            return (HTTPHandler)defaultHandler.get(null);
        } catch(NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Registers the given HTTP handler as the default HTTP handler for Integration Server.
     *
     * @param handler   The HTTP handler to be registered as the default handler.
     */
    @SuppressWarnings("unchecked")
    private static void setRegisteredDefaultHandler(HTTPHandler handler) {
        try {
            Field defaultHandler = HTTPDispatch.class.getDeclaredField("defaultHandler");
            defaultHandler.setAccessible(true);
            defaultHandler.set(null, handler);
        } catch(NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        } catch(IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
