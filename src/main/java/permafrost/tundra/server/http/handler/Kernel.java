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
import com.wm.app.b2b.server.HTTPHandler;
import com.wm.app.b2b.server.ProtocolState;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Proxies HTTP requests to the HTTPHandler object registered for the request directive.
 */
public class Kernel extends Handler {
    /**
     * The default handler for requests whose directive does not have a registered handler.
     */
    protected HTTPHandler defaultHandler;
    /**
     * The directives and their respective handlers that terminate the handler chain.
     */
    protected ConcurrentMap<String, HTTPHandler> handlers;

    /**
     * Creates a new Kernel.
     */
    public Kernel() {
        this(null);
    }

    /**
     * Creates a new Kernel.
     *
     * @param defaultHandler    The handler for requests whose directive has no registered handler.
     */
    public Kernel(HTTPHandler defaultHandler) {
        this(defaultHandler, null);
    }

    /**
     * Creates a new Kernel.
     *
     * @param defaultHandler    The handler for requests whose directive has no registered handler.
     * @param handlers          The directives and their respective handlers that terminate the handler chain.
     */
    public Kernel(HTTPHandler defaultHandler, Map<? extends String, ? extends HTTPHandler> handlers) {
        this.defaultHandler = defaultHandler;
        this.handlers = new ConcurrentHashMap<String, HTTPHandler>();
        if (handlers != null) this.handlers.putAll(handlers);
    }

    /**
     * Processes an HTTP request.
     *
     * @param context           The HTTP request context.
     * @param handlers          The queue of subsequent handlers to be called to handle the request.
     * @return                  True if the request was processed.
     * @throws IOException      If an IO error occurs.
     * @throws AccessException  If a security error occurs.
     */
    @Override
    public boolean handle(ProtocolState context, Iterator<Handler> handlers) throws IOException, AccessException {
        HTTPHandler handler = resolve(context);
        if (handler == null) {
            throw new IllegalStateException(MessageFormat.format("No HTTP handler was found to process this HTTP request: {0}", context.getRequestUrl()));
        } else {
            return handler.process(context);
        }
    }

    /**
     * Registers the given handler for the given directive.
     *
     * @param directive The directive to register the handler against.
     * @param handler   The handler to be registered.
     * @return          The previous handler for this directive if any, otherwise null.
     */
    public HTTPHandler register(String directive, HTTPHandler handler) {
        return handlers.put(directive, handler);
    }

    /**
     * Unregisters the given directive.
     *
     * @param directive The directive to unregister.
     * @return          The handler that was associated with the given directive if any, otherwise null.
     */
    public HTTPHandler unregister(String directive) {
        return handlers.remove(directive);
    }

    /**
     * Unregisters the given directive if it is associated with the given handler.
     *
     * @param directive The directive to unregister.
     * @param handler   The handler the directive must be associated with the be unregistered.
     * @return          True if the given directive was associated with the given handler and was unregistered.
     */
    public boolean unregister(String directive, HTTPHandler handler) {
        return handlers.remove(directive, handler);
    }

    /**
     * Returns the default handler for requests whose directive does not have a registered handler.
     *
     * @return The default handler for requests whose directive does not have a registered handler.
     */
    public HTTPHandler getDefaultHandler() {
        return defaultHandler;
    }

    /**
     * Returns the registered directives and their respective handlers as an unmodifiable map.
     *
     * @return The registered directives and their respective handlers as an unmodifiable map.
     */
    public Map<String, HTTPHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * Returns the handler for the given directive.
     *
     * @param directive The directive to resolve.
     * @return          The handler for handling requests with the given directive.
     */
    public HTTPHandler resolve(String directive) {
        HTTPHandler handler;
        if (directive == null) {
            handler = defaultHandler;
        } else {
             handler = handlers.get(directive.toLowerCase());
             if (handler == null) handler = defaultHandler;
        }
        return handler;
    }

    /**
     * Returns the handler for the given request context.
     *
     * @param context   The request context to resolve.
     * @return          The handler for handling the given request.
     */
    public HTTPHandler resolve(ProtocolState context) {
        return resolve(getDirective(context.getHttpRequestUrl()));
    }

    /**
     * Returns the directive given a URI.
     *
     * @param uri   The URI to return the directive for.
     * @return      The directive for this URI.
     */
    protected static String getDirective(String uri) {
        int index = uri.indexOf("/");

        String directive;
        if (index < 0) {
            directive = uri;
        } else {
            directive = uri.substring(0, index);
        }

        return directive;
    }
}
