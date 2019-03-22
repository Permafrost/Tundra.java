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
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.io.IOException;
import java.util.Iterator;

/**
 * Adds an HTTP header to the HTTP response with the measured duration of the request processing.
 */
public class Measurer extends StartableHandler {
    /**
     * The HTTP header prefix used for the measured duration header.
     */
    public static final String HTTP_HEADER_PREFIX = "X-Response-Duration";
    /**
     * The HTTP header added to the HTTP response for the measured duration.
     */
    protected String header;

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final Measurer INSTANCE = new Measurer();
    }

    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static Measurer getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Creates a new Measurer.
     */
    public Measurer() {
        this(null);
    }

    /**
     * Creates a new Measurer.
     *
     * @param headerSuffix  The optional suffix to add to the HTTP header name.
     */
    public Measurer(String headerSuffix) {
        this.header = HTTP_HEADER_PREFIX;
        if (headerSuffix != null) this.header = this.header + "-" + headerSuffix;
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
        boolean result;

        if (started) {
            long startTime = System.nanoTime();
            try {
                result = next(context, handlers);
            } finally {
                long endTime = System.nanoTime();
                context.setResponseFieldValue(header, DurationHelper.format((endTime - startTime) / 1000000000.0, DurationPattern.XML_NANOSECONDS));
            }
        } else {
            result = next(context, handlers);
        }

        return result;
    }
}
