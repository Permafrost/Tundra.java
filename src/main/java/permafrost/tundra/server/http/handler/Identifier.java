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
import com.wm.app.b2b.server.ProtocolState;
import permafrost.tundra.id.UUIDHelper;
import java.io.IOException;
import java.util.Iterator;

/**
 * Adds an HTTP header to the HTTP response with a unique request ID to support log correlation.
 */
public class Identifier extends StartableHandler {
    /**
     * The HTTP header used for conveying the request ID.
     */
    public static final String HTTP_HEADER = "X-Request-ID";

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final Identifier INSTANCE = new Identifier();
    }

    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static Identifier getInstance() {
        return Holder.INSTANCE;
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
        if (started) {
            String requestID = context.getRequestFieldValue(HTTP_HEADER);
            if (requestID == null) requestID = UUIDHelper.generate();
            context.setResponseFieldValue(HTTP_HEADER, requestID);
        }

        return next(context, handlers);
    }
}
