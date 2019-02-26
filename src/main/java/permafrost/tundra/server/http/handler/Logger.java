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
import com.wm.net.HttpHeader;
import permafrost.tundra.server.ConcurrentLogWriter;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;

/**
 * Logs HTTP requests and responses to a log file.
 */
public class Logger extends StartableHandler {

    /**
     * The logger to use when logging requests.
     */
    protected ConcurrentLogWriter logger;

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final Logger INSTANCE = new Logger();
    }

    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static Logger getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Creates a new Logger.
     */
    private Logger() {}

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
                log(context, endTime - startTime);
            }
        } else {
            result = next(context, handlers);
        }

        return result;
    }

    /**
     * Logs the HTTP request.
     *
     * @param context   The HTTP request to be logged.
     * @param duration  The measured duration for processing the request in nanoseconds.
     */
    protected void log(ProtocolState context, long duration) {
        try {
            String forwarded = context.getRequestFieldValue("X-Forwarded-For");

            String pattern;
            if (forwarded == null) {
                pattern = "{0} -- {2}:{3} {4} {5} -- {6} {7} {8}";
            } else {
                pattern = "{0} -- {1} {4} {5} -- {6} {7} {8}";
            }

            final String message = MessageFormat.format(pattern,
                    context.getInvokeState().getUser(),
                    forwarded,
                    context.getRemoteHost(),
                    Integer.toString(context.getRemotePort()),
                    HttpHeader.reqStrType[context.getRequestType()],
                    context.getRequestUrl(),
                    context.getResponseCode(),
                    context.getResponseMessage(),
                    DurationHelper.format(duration / 1000000000.0, DurationPattern.XML)
            );

            logger.log(message);
        } catch(Exception ex) {
            // do nothing
        }
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            logger = new ConcurrentLogWriter("tundra-http.log");
        }
        super.start();
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            logger.stop();
            logger = null;
        }
        super.stop();
    }
}
