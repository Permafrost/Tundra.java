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

package permafrost.tundra.net.http;

import com.wm.app.b2b.server.AccessException;
import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.HTTPDispatch;
import com.wm.app.b2b.server.HTTPDocHandler;
import com.wm.app.b2b.server.HTTPHandler;
import com.wm.app.b2b.server.HTTPInvokeHandler;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.data.IData;
import com.wm.data.IDataUtil;
import permafrost.tundra.net.uri.URIQueryHelper;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * webMethods Integration Server HTTP handler which allows arbitrary HTTP request routing.
 */
public enum HTTPRouter implements HTTPHandler {
    INSTANCE;

    protected static final HTTPInvokeHandler DEFAULT_INVOKE_HANDLER = new HTTPInvokeHandler();
    protected static final HTTPDocHandler DEFAULT_DOCUMENT_HANDLER = new HTTPDocHandler();

    /**
     * The route table, which holds routing instructions for different URIs.
     */
    protected volatile HTTPRouteTable routes = new HTTPRouteTable();

    /**
     * Processes an HTTP request.
     * @param state             The HTTP request to be processed.
     * @return                  True if this object was able to process the HTTP request, otherwise false.
     * @throws IOException      If an I/O problem is encountered reading from or writing to the client socket.
     * @throws AccessException  If the the HTTP request requires authentication or is not authorized.
     */
    public final boolean process(com.wm.app.b2b.server.ProtocolState state) throws IOException, AccessException {
        ContentHandler contentHandler = ServerAPI.getContentHandler(state.getContentType());
        Map.Entry<HTTPRoute, IData> matchResult = routes.match(HTTPMethod.valueOf(state.getRequestType()), "/" + state.getHttpRequestUrl());
        boolean result;

        if (matchResult != null) {
            // convert capture URI parameters to query string, so they get added to input pipeline
            // of the invoked service
            IData parameters = matchResult.getValue();
            String queryString = state.getHttpRequestUrlQuery();
            if (queryString != null) {
                IData queryParameters = URIQueryHelper.parse(queryString, true);
                IDataUtil.merge(parameters, queryParameters);
                parameters = queryParameters;
            }
            state.setHttpRequestUrlQuery(URIQueryHelper.emit(parameters, true));

            HTTPRoute route = matchResult.getKey();

            if (route.isInvoke()) {
                result = DEFAULT_INVOKE_HANDLER._process(state, contentHandler, route.getService());
            } else {
                String target = route.getTarget();
                if (target.startsWith("/")) target = target.substring(1, target.length());
                state.setHttpRequestUrl(target);
                result = DEFAULT_DOCUMENT_HANDLER.process(state);
            }
        } else {
            // return forbidden error
            int code = 403;
            state.setResponse(code, HTTPHelper.getResponseStatusMessage(code));
            result = true;
        }

        return result;
    }

    /**
     * Reloads the HTTP routing table from the package HTTP route configuration files.
     */
    public void refresh() {
        synchronized(this) {
            HTTPRouteTable newRoutes = HTTPRouteTable.newInstance();
            HTTPRouteTable oldRoutes = routes;

            routes = newRoutes;
            refreshDirectives(oldRoutes.getDirectives(), newRoutes.getDirectives());
        }
    }

    /**
     * Register this object with the Integration Server HTTP request dispatcher for all
     * the directives in the routing table.
     * @param oldDirectives The previously registered directives which must now be unregistered.
     * @param newDirectives The new directives to be registered.
     */
    protected void refreshDirectives(java.util.Set<String> oldDirectives, java.util.Set<String> newDirectives) {
        java.util.Set<String> toBeAdded = new java.util.TreeSet<String>(newDirectives);
        toBeAdded.removeAll(oldDirectives);
        register(toBeAdded);

        java.util.Set<String> toBeRemoved = new java.util.TreeSet<String>(oldDirectives);
        toBeRemoved.removeAll(newDirectives);
        unregister(toBeRemoved);
    }

    /**
     * Registers this object as a handler for the given directive with the Integration Server HTTP request dispatcher.
     * @param directive The directive to register this object as a handler for.
     */
    protected void register(String directive) {
        HTTPDispatch.addHandler(directive, this);
    }

    /**
     * Registers this object as a handler for the given directives with the Integration Server HTTP request dispatcher.
     * @param directives The directives to register this object as a handler for.
     */
    protected void register(Collection<String> directives) {
        for (String directive : directives) {
            register(directive);
        }
    }

    /**
     * Unregisters this object as a handler of the given directive from the Integration Server HTTP request dispatcher.
     * @param directive The directive to unregister this object as a handler of.
     */
    protected void unregister(String directive) {
        HTTPDispatch.removeHandler(directive);
    }

    /**
     * Unregisters this object as a handler of the given directives from the Integration Server HTTP request dispatcher.
     * @param directives The directives to unregister this object as a handler of.
     */
    protected void unregister(Collection<String> directives) {
        for (String directive : directives) {
            unregister(directive);
        }
    }

    /**
     * Unregisters this object as the handler of all directives in the current route table from the Integration Server
     * dispatcher, and then clears the route table.
     */
    public void clear() {
        synchronized(this) {
            unregister(routes.getDirectives());
            routes = new HTTPRouteTable();
        }
    }

    /**
     * Returns the HTTP route table that this object is handling HTTP requests for.
     * @return The HTTP route table.
     */
    public HTTPRouteTable getRoutes() {
        return routes;
    }
}
