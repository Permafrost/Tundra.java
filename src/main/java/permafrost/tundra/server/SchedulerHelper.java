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

import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.scheduler.ScheduleManager;
import permafrost.tundra.lang.ExceptionHelper;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A collection of methods for working with Integration Server user scheduled tasks.
 */
public class SchedulerHelper {
    /**
     * Disallow instantiation of this class.
     */
    private SchedulerHelper() {}

    /**
     * Starts the Integration Server user task scheduler.
     * @throws ServiceException If an error occurs when starting the scheduler.
     */
    public static void start() throws ServiceException {
        try {
            ScheduleManager.init();
        } catch(Exception ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Stops the Integration Server user task scheduler.
     */
    public static void stop() {
        ScheduleManager.shutdown();
    }

    /**
     * Restarts the Integration Server user task scheduler.
     * @throws ServiceException If an error occurs when restarting the scheduler.
     */
    public static void restart() throws ServiceException {
        stop();
        start();
    }

    /**
     * Returns the name of this Integration Server task scheduler node.
     *
     * @return The name of this Integration Server task scheduler node.
     * @throws ServiceException If an error occurs retrieving the name.
     */
    public static String self() throws ServiceException {
        String name = null;
        try {
            name = ScheduleManager.getNodeName();
        } catch(Exception ex) {
            ExceptionHelper.raise(ex);
        }
        return name;
    }
}
