/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import org.apache.log4j.Level;
import permafrost.tundra.configuration.ConfigurationManager;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.lang.Loggable;
import permafrost.tundra.lang.StartableManager;
import permafrost.tundra.util.concurrent.BlockingRejectedExecutionHandler;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Manages logging to custom server log files.
 */
public class ServerLogManager extends StartableManager<String, Loggable> {
    /**
     * Default task queue size for thread pool executor.
     */
    private static final int DEFAULT_TASK_QUEUE_CAPACITY = InputOutputHelper.DEFAULT_BUFFER_SIZE;
    /**
     * Default thread priority to use for thread pool executor.
     */
    private static final int DEFAULT_THREAD_PRIORITY = Thread.MIN_PRIORITY;
    /**
     * The executor used to write log statements asynchronously.
     */
    protected ExecutorService executorService;
    /**
     * Maps logical log names to physical log files.
     */
    protected final ConcurrentMap<String, String> targets = new ConcurrentHashMap<String, String>();

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final ServerLogManager INSTANCE = new ServerLogManager();
        private static final DefaultServerLoggable DEFAULT_SERVER_LOGGABLE = new DefaultServerLoggable();
    }

    /**
     * Create a new ServerLogManager object.
     */
    private ServerLogManager() {
        super(true);
    }

    /**
     * Returns the singleton instance ServerLogManager.
     *
     * @return The singleton instance ServerLogManager.
     */
    public static ServerLogManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns the Loggable object associated with the given name.
     *
     * @param name  The log name.
     * @return      The Loggable which can be used to write to the log with the given name.
     */
    @Override
    public Loggable get(String name) {
        Loggable loggable = null;

        if (started && name != null) {
            String target = targets.get(name.toLowerCase());
            if (target != null) {
                loggable = super.get(target);
                if (loggable == null) {
                    ServerLogWriter newLoggable = new ServerLogWriter(executorService, target);
                    if (register(target, newLoggable)) {
                        loggable = newLoggable;
                    } else {
                        loggable = super.get(target);
                    }
                }
            }
        }

        if (loggable == null) {
            loggable = Holder.DEFAULT_SERVER_LOGGABLE;
        }

        return loggable;
    }

    /**
     * Initializes the logical to physical log target mapping.
     */
    private void initializeTargets() {
        // build list of logical to physical log targets
        this.targets.clear();
        try {
            IData targets = IDataHelper.get(ConfigurationManager.get("Tundra"), "feature/log/target", IData.class);
            if (targets != null) {
                IDataCursor cursor = targets.getCursor();
                while(cursor.next()) {
                    String key = cursor.getKey();
                    Object value = cursor.getValue();
                    if (value instanceof String) {
                        this.targets.put(key.toLowerCase(), ((String)value).toLowerCase());
                    }
                }
            }
        } catch(IOException ex) {
            // do nothing
        } catch(ServiceException ex) {
            // do nothing
        }
    }

    /**
     * Starts the manager.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            executorService = createExecutor();
            initializeTargets();
            super.start();
        }
    }

    /**
     * Stops the manager.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            super.stop();
            executorService.shutdown();
            executorService = null;
            this.targets.clear();
        }
    }

    /**
     * Creates a task executor used for deferring write calls to.
     * @return a task executor used for deferring write calls to.
     */
    protected ExecutorService createExecutor() {
        ServerThreadPoolExecutor executorService = new ServerThreadPoolExecutor(1, "Tundra/Log Writer", "", DEFAULT_THREAD_PRIORITY, new LinkedBlockingDeque<Runnable>(DEFAULT_TASK_QUEUE_CAPACITY),  new BlockingRejectedExecutionHandler());
        executorService.allowCoreThreadTimeOut(true);
        return executorService;
    }

    /**
     * A loggable for the server.log file.
     */
    private static class DefaultServerLoggable implements Loggable {
        /**
         * Returns the level of logging that is being written to the log file.
         *
         * @return The level of logging that is being written to the log file.
         */
        @Override
        public Level getLogLevel() {
            return ServerLogLevelHelper.toLevel(System.getProperty("watt.debug.level", Level.INFO.toString()));
        }

        /**
         * Sets the level of logging that will be written to the log file.
         *
         * @param logLevel  The level of logging that will be written to the log file.
         */
        @Override
        public void setLogLevel(Level logLevel) {
            // do nothing
        }

        /**
         * Logs the given message.
         *
         * @param level         The logging level to use.
         * @param message       The message to be logged.
         * @param context       Optional document containing additional context for this log statement.
         * @param addPrefix     Whether to prefix the log statement with logging metadata.
         */
        @Override
        public void log(Level level, String message, IData context, boolean addPrefix) {
            ServerLogHelper.log(level, message, context, addPrefix);
        }

        /**
         * Does nothing, this object is always started.
         */
        @Override
        public void start() {
            // does nothing
        }

        /**
         * Does nothing, this object is always started.
         */
        @Override
        public void stop() {
            // does nothing
        }

        /**
         * Does nothing, this object is always started.
         */
        @Override
        public void restart() {
            // does nothing
        }

        /**
         * Returns true if the object is started.
         *
         * @return True if the object is started.
         */
        @Override
        public boolean isStarted() {
            return true;
        }
    }
}
