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
import com.wm.data.IData;
import org.apache.log4j.Level;
import permafrost.tundra.lang.Loggable;
import permafrost.tundra.util.concurrent.ConcurrentWriter;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * A concurrent log writer for Integration Server.
 */
public class ServerLogWriter extends ConcurrentWriter implements Loggable {
    /**
     * The level of logging that will be written to the log file.
     */
    protected Level logLevel;
    /**
     * The path to the log file being written to.
     */
    protected String path;

    /**
     * Creates a new ConcurrentLogWriter.
     *
     * @param filename  The file the log will be written to.
     */
    public ServerLogWriter(ExecutorService executorService, String filename) {
        this(executorService, filename, false);
    }

    /**
     * Creates a new ConcurrentLogWriter.
     *
     * @param filename      The file the log will be written to.
     * @param isAbsolute    Whether the given filename is an absolute or relative path.
     */
    public ServerLogWriter(ExecutorService executorService, String filename, boolean isAbsolute) {
        super(executorService, LogManager.openLogWriter(isAbsolute ? filename : Server.getLogDir().getAbsolutePath() + File.separatorChar + filename));
    }

    /**
     * Returns the level of logging that is being written to the log file.
     *
     * @return The level of logging that is being written to the log file.
     */
    @Override
    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the level of logging that will be written to the log file.
     *
     * @param logLevel  The level of logging that will be written to the log file.
     */
    @Override
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Logs the given message.
     *
     * @param level         The logging level to use.
     * @param message       The message to be logged.
     * @param context       Optional document containing additional context for this log statement.
     * @param addPrefix     Whether to prefix the log statement with logging metadata.
     * @throws IOException  If an IO error occurs.
     */
    @Override
    public void log(Level level, String message, IData context, boolean addPrefix) throws IOException {
        if (started) {
            if (level == null) level = ServerLogLevelHelper.DEFAULT_LOG_LEVEL;
            Level logLevel = this.logLevel == null ? ServerLogLevelHelper.toLevel(System.getProperty("watt.debug.level", Level.INFO.toString())) : this.logLevel;
            if (level.isGreaterOrEqual(logLevel)) {
                String statement = ServerLogStatement.of(level, message, context, addPrefix);
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
}
