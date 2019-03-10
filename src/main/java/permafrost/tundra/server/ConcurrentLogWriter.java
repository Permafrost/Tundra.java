/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

import com.wm.app.b2b.server.LogManager;
import com.wm.app.b2b.server.LogWriter;
import com.wm.app.b2b.server.Server;
import permafrost.tundra.lang.Loggable;
import permafrost.tundra.lang.Startable;
import permafrost.tundra.util.concurrent.ConcurrentWriter;
import java.io.File;
import java.io.IOException;

/**
 * A concurrent log writer for Integration Server.
 */
public class ConcurrentLogWriter extends ConcurrentWriter implements Loggable, Startable {
    /**
     * The path to the log file being written to.
     */
    protected String path;

    /**
     * Creates a new ConcurrentLogWriter.
     *
     * @param filename  The file the log will be written to.
     */
    public ConcurrentLogWriter(String filename) {
        this(filename, false);
    }

    /**
     * Creates a new ConcurrentLogWriter.
     *
     * @param filename      The file the log will be written to.
     * @param isAbsolute    Whether the given filename is an absolute or relative path.
     */
    public ConcurrentLogWriter(String filename, boolean isAbsolute) {
        super(LogManager.openLogWriter(isAbsolute ? filename : Server.getLogDir().getAbsolutePath() + File.separatorChar + filename));
    }

    /**
     * Logs the given message.
     *
     * @param message The message to be logged.
     */
    @Override
    public void log(String ...message) throws IOException {
        if (started) {
            if (message != null && message.length > 0) {
                String statement = null;
                if (message.length > 1) {
                    StringBuilder builder = new StringBuilder();
                    for (String part : message) {
                        if (part != null) {
                            builder.append(part);
                        }
                    }
                    statement = builder.toString();
                } else if (message[0] != null) {
                    statement = message[0];
                }

                if (statement != null) {
                    write(statement);
                }
            }
        }
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            if (out == null) {
                out = LogManager.openLogWriter(path);
            } else {
                path = ((LogWriter)out).getPath();
            }
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
            try {
                out.close();
                out = null;
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Returns the thread name prefix used when creating the task executor on startup.
     * @return the thread name prefix used when creating the task executor on startup.
     */
    @Override
    protected String getExecutorThreadPrefix() {
        String prefix = super.getExecutorThreadPrefix();
        return path == null ? prefix : prefix + "/" + new File(path).getName();
    }
}
