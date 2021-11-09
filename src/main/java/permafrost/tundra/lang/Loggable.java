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

package permafrost.tundra.lang;

import permafrost.tundra.server.ServerLogLevel;
import java.io.IOException;

/**
 * A generic interface for objects which support logging.
 */
public interface Loggable extends Startable {
    /**
     * Logs the given message.
     *
     * @param level         The logging level to use.
     * @param message       The message to be logged.
     * @param context       Optional document containing additional context for this log statement.
     * @param addPrefix     Whether to prefix the log statement with logging metadata.
     * @throws IOException  If an IO error occurs.
     */
    void log(ServerLogLevel level, String message, Object context, boolean addPrefix) throws IOException;

    /**
     * Returns the level of logging that is being written to the log file.
     *
     * @return The level of logging that is being written to the log file.
     */
    ServerLogLevel getLogLevel();

    /**
     * Sets the level of logging that will be written to the log file.
     *
     * @param logLevel  The level of logging that will be written to the log file.
     */
    void setLogLevel(ServerLogLevel logLevel);
}
