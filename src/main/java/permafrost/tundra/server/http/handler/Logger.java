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
import org.apache.log4j.Level;
import permafrost.tundra.server.ServerLogHelper;
import permafrost.tundra.server.ProtocolStateHelper;
import java.io.IOException;
import java.util.Iterator;

/**
 * Logs HTTP requests and responses to a log file.
 */
public class Logger extends StartableHandler {
    /**
     * The default log level used by this object.
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;
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
            long start = System.nanoTime(), startTime = System.currentTimeMillis();
            try {
                result = next(context, handlers);
            } finally {
                long end = System.nanoTime(), endTime = System.currentTimeMillis();
                ServerLogHelper.log(this.getClass().getName(), DEFAULT_LOG_LEVEL, this.getClass().getName() + " -- " + ProtocolStateHelper.serialize(context, end - start, startTime, endTime, null, null), null, false);
            }
        } else {
            result = next(context, handlers);
        }

        return result;
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            super.start();
        }
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            super.stop();
        }
    }
}
