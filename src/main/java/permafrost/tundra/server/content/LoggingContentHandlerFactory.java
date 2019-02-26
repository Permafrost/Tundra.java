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

package permafrost.tundra.server.content;

import permafrost.tundra.lang.Loggable;
import permafrost.tundra.server.ConcurrentLogWriter;
import java.io.IOException;

/**
 * A factory for creating LoggingContentHandler objects.
 */
public class LoggingContentHandlerFactory extends FilterContentHandlerFactory implements Loggable {
    /**
     * The stream to which content will be logged.
     */
    protected volatile ConcurrentLogWriter logger;
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final LoggingContentHandlerFactory INSTANCE = new LoggingContentHandlerFactory();
    }

    /**
     * Disallow instantiation of this class.
     */
    private LoggingContentHandlerFactory() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static LoggingContentHandlerFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns a new content handler.
     *
     * @return A new content handler.
     */
    public FilterContentHandler create() {
        return new LoggingContentHandler(this, this);
    }

    /**
     * Starts logging content.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            logger = new ConcurrentLogWriter("tundra-content.log");
            super.start();
        }
    }

    /**
     * Stops logging content.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            super.stop();
            logger.stop();
            logger = null;
        }
    }

    /**
     * Logs the given message.
     *
     * @param message       The message to be logged.
     * @throws IOException  If an IO error occurred.
     */
    @Override
    public void log(String ...message) throws IOException {
        if (started && logger != null) {
            logger.log(message);
        }
    }
}
