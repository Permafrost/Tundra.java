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

package permafrost.tundra.server;

import com.wm.app.b2b.server.HTTPState;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import permafrost.tundra.data.CaseInsensitiveIData;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A collection of convenience methods for working with InvokeState objects.
 */
public final class InvokeStateHelper {
    /**
     * Disallow instantiation of this class.
     */
    private InvokeStateHelper() {}

    /**
     * Returns a clone of the given InvokeState, with the call stack also cloned.
     *
     * @param invokeState   The object to clone.
     * @return              A clone of the given object, with the call stack also cloned.
     */
    @SuppressWarnings("unchecked")
    public static InvokeState clone(InvokeState invokeState) {
        if (invokeState == null) throw new NullPointerException("invokeState must not be null");

        InvokeState clone = (InvokeState)invokeState.clone();
        sanitize(clone);
        return clone;
    }

    /**
     * Cleans the given InvokeState object so that it can be used by another threaded service invocation.
     *
     * @param invokeState   The InvokeState to sanitize.
     */
    private static void sanitize(InvokeState invokeState) {
        ProtocolInfoIf oldState = invokeState.getProtocolInfoIf();
        if (oldState instanceof HTTPState) {
            ConcurrentHTTPState newState = new ConcurrentHTTPState(oldState);
            invokeState.setProtocolInfoIf(newState);
        }
    }

    /**
     * A thread safe HTTPState class.
     */
    private static class ConcurrentHTTPState extends HTTPState {
        /**
         * Stores the properties of this object.
         */
        protected ConcurrentMap<String, Object> properties = new ConcurrentHashMap<String, Object>();

        /**
         * Create a new ConcurrentHTTPState object.
         *
         * @param protocolInfo  The state object to copy properties from.
         */
        ConcurrentHTTPState(ProtocolInfoIf protocolInfo) {
            String[] keys = protocolInfo.getProtocolPropertyList();
            for (String key : keys) {
                properties.put(key, protocolInfo.getProtocolProperty(key));
            }
        }

        /**
         * Returns the property for the given key.
         *
         * @param key   The property key.
         * @return      The property value with the given key.
         */
        @Override
        public Object getProtocolProperty(String key) {
            return properties.get(key);
        }

        /**
         * Returns a list of property keys.
         *
         * @return a list of property keys.
         */
        @Override
        public String[] getProtocolPropertyList() {
            return properties.keySet().toArray(new String[0]);
        }

        /**
         * Returns an IData representation of this object's properties.
         *
         * @return an IData representation of this object's properties.
         */
        @Override
        public IData getResponseProtocolProperties() {
            return new IDataMap(properties);
        }

        /**
         * Sets the property with the given key to the given value.
         *
         * @param key   The property key.
         * @param value The value to set the property to.
         * @return      True if the property with the given key was set to the given value.
         */
        @Override
        public boolean setProtocolProperty(String key, String value) {
            properties.put(key, value);
            return true;
        }
    }

    /**
     * Returns the current invoke state.
     *
     * @return The current invoke state.
     */
    public static InvokeState current() {
        return InvokeState.getCurrentState();
    }

    /**
     * Returns the transport information associated with the current invoke state.
     *
     * @return The transport information associated with the current invoke state.
     */
    public static IData currentTransport() {
        return getTransport(current());
    }

    /**
     * Returns the redacted transport information associated with the current invoke state.
     *
     * @return The redacted transport information associated with the current invoke state.
     */
    public static IData currentRedactedTransport() {
        return getRedactedTransport(current());
    }

    /**
     * Returns true if the given InvokeState has a response body set.
     *
     * @return true if the given InvokeState has a response body set.
     */
    public static boolean hasResponseBody(InvokeState invokeState) {
        return invokeState != null && (invokeState.getPrivateData("$msgBytesOut") != null || invokeState.getPrivateData("$msgStreamOut") != null);
    }

    /**
     * Returns the transport information associated with the given invoke state.
     *
     * @param invokeState   The invoke state to retrieve the transport information from.
     * @return              The transport information associated with the given invoke state.
     */
    public static IData getTransport(InvokeState invokeState) {
        IData transport = null;
        if (invokeState != null) {
            ProtocolInfoIf protocolInfo = invokeState.getProtocolInfoIf();
            if (protocolInfo != null) {
                transport = (IData)protocolInfo.getProtocolProperty("transport");
            }
        }
        return transport;
    }

    /**
     * Returns the transport information associated with the given invoke state, with credentials redacted if
     * applicable.
     *
     * @param invokeState   The invoke state to retrieve the transport information from.
     * @return              The transport information associated with the given invoke state.
     */
    public static IData getRedactedTransport(InvokeState invokeState) {
        IData transport = null;
        if (invokeState != null) {
            ProtocolInfoIf protocolInfo = invokeState.getProtocolInfoIf();
            if (protocolInfo != null) {
                transport = (IData)protocolInfo.getProtocolProperty("transport");

                if (transport != null) {
                    // take a copy of the transport data structure, so the following changes won't affect the source
                    transport = IDataHelper.duplicate(transport, true);

                    IDataCursor cursor = transport.getCursor();

                    try {
                        IData email = IDataHelper.get(cursor, "email", IData.class);
                        if (email != null) {
                            IDataCursor emailCursor = email.getCursor();
                            try {
                                // remove email content from transport, as it could be very large
                                IDataHelper.remove(emailCursor, "content");
                            } finally {
                                emailCursor.destroy();
                            }
                        }

                        IData http = IDataHelper.get(cursor, "http", IData.class);
                        if (http != null) {
                            IDataCursor httpCursor = http.getCursor();
                            try {
                                // redact authentication related headers, as they are confidential
                                final String headersKey = "requestHdrs";
                                IData headers = IDataHelper.get(httpCursor, headersKey, IData.class);
                                IDataHelper.put(httpCursor, headersKey, redactHeaders(IDataHelper.sort(headers)), false);
                            } finally {
                                httpCursor.destroy();
                            }
                        }
                    } finally {
                        cursor.destroy();
                    }
                }
            }
        }
        return transport;
    }

    /**
     * Redacts the authentication related headers, if any, in the given set of HTTP headers.
     *
     * @param headers   The HTTP headers to redact.
     * @return          The redacted HTTP headers.
     */
    private static IData redactHeaders(IData headers) {
        if (headers != null) {
            headers = CaseInsensitiveIData.of(headers);
            IDataCursor headersCursor = headers.getCursor();
            try {
                String authorization = IDataHelper.get(headersCursor, "Authorization", String.class);
                if (authorization != null && !authorization.equals("")) {
                    IDataHelper.put(headersCursor, "Authorization", "REDACTED");
                }
            } finally {
                headersCursor.destroy();
            }
        }

        return headers;
    }

}
