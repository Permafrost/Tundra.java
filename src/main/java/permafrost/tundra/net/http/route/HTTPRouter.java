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

package permafrost.tundra.net.http.route;

import com.wm.app.b2b.server.AccessException;
import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.HTTPDispatch;
import com.wm.app.b2b.server.HTTPDocHandler;
import com.wm.app.b2b.server.HTTPHandler;
import com.wm.app.b2b.server.HTTPInvokeHandler;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.app.b2b.server.ProtocolState;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.data.IData;
import com.wm.data.IDataUtil;
import com.wm.net.HttpHeader;
import com.wm.util.Config;
import permafrost.tundra.net.http.HTTPHelper;
import permafrost.tundra.net.http.HTTPMethod;
import permafrost.tundra.net.uri.URIQueryHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * webMethods Integration Server HTTP handler which allows arbitrary HTTP request routing.
 */
public class HTTPRouter implements HTTPHandler {
    /**
     * The header used to store the original request URI.
     */
    public static final String REQUEST_URI_HEADER = "X-Tundra-HTTPRouter-Request-URI";

    /**
     * The header used to return the response time in milliseconds.
     */
    private static final String RESPONSE_DURATION_HEADER = "X-Response-Duration";

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final HTTPRouter INSTANCE = new HTTPRouter();
    }

    /**
     * The ProtocolInfoIf property for the request header.
     */
    private static final String PROTOCOL_REQUEST_HEADER_PROPERTY = "Req_Header";
    /**
     * The handler used to invoke services.
     */
    private static final HTTPInvokeHandler DEFAULT_INVOKE_HANDLER = new HTTPInvokeHandler();
    /**
     * The handler used to serve documents.
     */
    private static final HTTPDocHandler DEFAULT_DOCUMENT_HANDLER = new HTTPDocHandler();
    /**
     * The default directive used for invoking services.
     */
    public static final String DEFAULT_INVOKE_DIRECTIVE = "invoke";
    /**
     * The Integration Server configuration key for a custom invoke directive.
     */
    public static final String WATT_SERVER_INVOKE_DIRECTIVE = "watt.server.invokeDirective";
    /**
     * A request URL which matches the invoke directive, used to fool the AuditLogManager so it does not log spurious
     * Access Denied errors.
     */
    public static final String INVOKE_REQUEST_URL = "/" + Config.getProperty(DEFAULT_INVOKE_DIRECTIVE, WATT_SERVER_INVOKE_DIRECTIVE);
    /**
     * The route table, which holds routing instructions for different URIs.
     */
    private volatile HTTPRouteTable routes = new HTTPRouteTable();
    /**
     * The method to use to overwrite the request URL on the current invoke state HTTP headers.
     */
    private static Method overwriteReqUrlMethod = null;
    /**
     * The field to use to overwrite the request URL on the current invoke state HTTP headers, if the above method
     * does not exist.
     */
    private static Field requestUrlField = null;

    static {
        try {
            overwriteReqUrlMethod = HttpHeader.class.getDeclaredMethod("overwriteReqUrlMethod", String.class);
        } catch (NoSuchMethodException ex) {
            try {
                requestUrlField = HttpHeader.class.getDeclaredField("requestUrl");
                requestUrlField.setAccessible(true);
            } catch (NoSuchFieldException exception) {
                // ignore exception
            }
        }
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static HTTPRouter getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Processes an HTTP request.
     *
     * @param state             The HTTP request to be processed.
     * @return                  True if this object was able to process the HTTP request, otherwise false.
     * @throws IOException      If an I/O problem is encountered reading from or writing to the client socket.
     * @throws AccessException  If the HTTP request requires authentication or is not authorized.
     */
    public final boolean process(ProtocolState state) throws IOException, AccessException {
        long startTime = System.nanoTime();

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
                // fool the AuditLogManager that the requested URL was for the invoke directive so that it does not
                // inadvertently log spurious access denied errors
                String originalRequestURL = setRequestURI(INVOKE_REQUEST_URL);
                setHeader(REQUEST_URI_HEADER, originalRequestURL);

                result = DEFAULT_INVOKE_HANDLER._process(state, contentHandler, route.getService());

                // restore the original request URL to the invoke state
                setRequestURI(originalRequestURL);
                removeHeader(REQUEST_URI_HEADER);
            } else {
                String target = route.getTarget();
                if (target.startsWith("/")) target = target.substring(1);
                state.setHttpRequestUrl(target);
                result = DEFAULT_DOCUMENT_HANDLER.process(state);
            }
        } else {
            // return forbidden error
            int code = 403;
            state.setResponse(code, HTTPHelper.getResponseStatusMessage(code));
            result = true;
        }

        long endTime = System.nanoTime();
        state.setResponseFieldValue(RESPONSE_DURATION_HEADER, DurationHelper.format((endTime - startTime) / 1000000000.0, DurationPattern.XML_NANOSECONDS));

        return result;
    }

    /**
     * Returns the HTTP request URI from the HttpHeader object in the current InvokeState.
     * @return The HTTP request URI from the HttpHeader object in the current InvokeState.
     */
    public static String getRequestURI() {
        String requestURI = null;

        InvokeState invokeState = InvokeState.getCurrentState();
        if (invokeState != null) {
            ProtocolInfoIf protocolInfoIf = invokeState.getProtocolInfoIf();
            if (protocolInfoIf != null) {
                Object object = protocolInfoIf.getProtocolProperty(PROTOCOL_REQUEST_HEADER_PROPERTY);
                if (object instanceof HttpHeader) {
                    HttpHeader header = (HttpHeader)object;
                    requestURI = header.getFieldValue(REQUEST_URI_HEADER);
                    if (requestURI == null) {
                        requestURI = header.getRequestUrl();
                    }
                }
            }
        }

        return requestURI;
    }

    /**
     * Sets the requestUrl field on the HttpHeader object in the current InvokeState.
     *
     * @param newRequestURI The value to set.
     * @return              The previous value.
     */
    private static String setRequestURI(String newRequestURI) {
        String originalRequestURL = null;

        InvokeState invokeState = InvokeState.getCurrentState();
        if (invokeState != null) {
            ProtocolInfoIf protocolInfoIf = invokeState.getProtocolInfoIf();
            if (protocolInfoIf != null) {
                Object object = protocolInfoIf.getProtocolProperty(PROTOCOL_REQUEST_HEADER_PROPERTY);
                if (object instanceof HttpHeader) {
                    HttpHeader header = (HttpHeader)object;
                    originalRequestURL = header.getRequestUrl();
                    try {
                        if (overwriteReqUrlMethod != null) {
                            overwriteReqUrlMethod.invoke(header, newRequestURI);
                        } else if (requestUrlField != null) {
                            requestUrlField.set(header, newRequestURI);
                        }
                    } catch (InvocationTargetException ex) {
                        // ignore exception
                    } catch (IllegalAccessException ex) {
                        // ignore exception
                    }
                }
            }
        }

        return originalRequestURL;
    }

    /**
     * Sets the header field on the HttpHeader object in the current InvokeState.
     *
     * @param key           The header key to set.
     * @param value         The header value to set.
     * @return              The previous value.
     */
    private static String setHeader(String key, String value) {
        String originalValue = null;

        InvokeState invokeState = InvokeState.getCurrentState();
        if (invokeState != null) {
            ProtocolInfoIf protocolInfoIf = invokeState.getProtocolInfoIf();
            if (protocolInfoIf != null) {
                Object object = protocolInfoIf.getProtocolProperty(PROTOCOL_REQUEST_HEADER_PROPERTY);
                if (object instanceof HttpHeader) {
                    HttpHeader header = (HttpHeader)object;
                    originalValue = header.getFieldValue(key);
                    header.setField(key, value);
                }
            }
        }

        return originalValue;
    }

    /**
     * Removes the header field with the given key on the HttpHeader object in the current InvokeState.
     *
     * @param key           The key identifying the header to be removed.
     * @return              The previous value.
     */
    private static String removeHeader(String key) {
        String originalValue = null;

        InvokeState invokeState = InvokeState.getCurrentState();
        if (invokeState != null) {
            ProtocolInfoIf protocolInfoIf = invokeState.getProtocolInfoIf();
            if (protocolInfoIf != null) {
                Object object = protocolInfoIf.getProtocolProperty(PROTOCOL_REQUEST_HEADER_PROPERTY);
                if (object instanceof HttpHeader) {
                    HttpHeader header = (HttpHeader)object;
                    originalValue = header.getFieldValue(key);
                    header.clearField(key);
                }
            }
        }

        return originalValue;
    }

    /**
     * Reloads the HTTP routing table from the package HTTP route configuration files.
     */
    public synchronized void refresh() {
        HTTPRouteTable newRoutes = HTTPRouteTable.newInstance();
        HTTPRouteTable oldRoutes = routes;

        routes = newRoutes;
        refreshDirectives(oldRoutes.getDirectives(), newRoutes.getDirectives());
    }

    /**
     * Register this object with the Integration Server HTTP request dispatcher for all the directives in the routing
     * table.
     *
     * @param oldDirectives The previously registered directives which must now be unregistered.
     * @param newDirectives The new directives to be registered.
     */
    protected void refreshDirectives(Set<String> oldDirectives, Set<String> newDirectives) {
        Set<String> toBeAdded = new TreeSet<String>(newDirectives);
        toBeAdded.removeAll(oldDirectives);
        register(toBeAdded);

        Set<String> toBeRemoved = new TreeSet<String>(oldDirectives);
        toBeRemoved.removeAll(newDirectives);
        unregister(toBeRemoved);
    }

    /**
     * Registers this object as a handler for the given directive with the Integration Server HTTP request dispatcher.
     *
     * @param directive The directive to register this object as a handler for.
     */
    protected void register(String directive) {
        HTTPDispatch.addHandler(directive, this);
    }

    /**
     * Registers this object as a handler for the given directives with the Integration Server HTTP request dispatcher.
     *
     * @param directives The directives to register this object as a handler for.
     */
    protected void register(Collection<String> directives) {
        for (String directive : directives) {
            register(directive);
        }
    }

    /**
     * Unregisters this object as a handler of the given directive from the Integration Server HTTP request dispatcher.
     *
     * @param directive The directive to unregister this object as a handler of.
     */
    protected void unregister(String directive) {
        HTTPDispatch.removeHandler(directive);
    }

    /**
     * Unregisters this object as a handler of the given directives from the Integration Server HTTP request
     * dispatcher.
     *
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
    public synchronized void clear() {
        unregister(routes.getDirectives());
        routes = new HTTPRouteTable();
    }

    /**
     * Returns the HTTP route table that this object is handling HTTP requests for.
     *
     * @return The HTTP route table.
     */
    public HTTPRouteTable getRoutes() {
        return routes;
    }
}
