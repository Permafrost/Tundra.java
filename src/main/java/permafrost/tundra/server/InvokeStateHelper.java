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

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import permafrost.tundra.data.CaseInsensitiveIData;
import permafrost.tundra.data.IDataHelper;

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
        return (InvokeState)invokeState.clone();
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
