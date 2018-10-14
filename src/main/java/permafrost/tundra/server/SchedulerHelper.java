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

import com.wm.app.b2b.server.scheduler.ScheduleManager;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A collection of methods for working with the Integration Server user task scheduler.
 */
public final class SchedulerHelper {
    /**
     * Disallow instantiation of this class.
     */
    private SchedulerHelper() {}

    /**
     * @return the current status of the Integration Server user task scheduler.
     */
    public static SchedulerStatus status() {
        return SchedulerStatus.normalize(isRunning(), isPaused());
    }

    /**
     * @return true if the Integration Server user task scheduler is paused.
     */
    @SuppressWarnings("unchecked")
    public static boolean isPaused() {
        boolean isPaused;

        try {
            Method isPausedMethod = ScheduleManager.class.getDeclaredMethod("isPaused");
            isPaused = (Boolean)isPausedMethod.invoke(null);
        } catch(NoSuchMethodException ex) {
            isPaused = false;
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        return isPaused;
    }

    /**
     * @return true if the Integration Server user task scheduler is running.
     */
    @SuppressWarnings("unchecked")
    public static boolean isRunning() {
        boolean isRunning;

        try {
            Method isRunningMethod = ScheduleManager.class.getDeclaredMethod("isRunning");
            isRunning = (Boolean)isRunningMethod.invoke(null);
        } catch(NoSuchMethodException ex) {
            try {
                Field gRunning = ScheduleManager.class.getDeclaredField("gRunning");
                gRunning.setAccessible(true);
                isRunning = gRunning.getBoolean(null);
            } catch(Exception exception) {
                throw new RuntimeException(exception);
            }
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        return isRunning;
    }

    /**
     * Starts the Integration Server user task scheduler.
     */
    public static void start() {
        try {
            ScheduleManager.init();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
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
     */
    public static void restart() {
        stop();
        start();
    }

    /**
     * Pauses the Integration Server user task scheduler. When run on Integration Server 7.1,
     * stops the scheduler instead as the pause function is not supported on that version.
     */
    public static void pause() {
        try {
            Method pause = ScheduleManager.class.getDeclaredMethod("pause");
            pause.invoke(null);
        } catch(NoSuchMethodException ex) {
            stop();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Resumes the Integration Server user task scheduler. When run on Integration Server 7.1,
     * starts the scheduler instead as the resume function is not supported on that version.
     */
    public static void resume() {
        try {
            Method resume = ScheduleManager.class.getDeclaredMethod("resume");
            resume.invoke(null);
        } catch(NoSuchMethodException ex) {
            start();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the name of this Integration Server task scheduler node.
     */
    public static String self() {
        String name = null;
        try {
            name = ScheduleManager.getNodeName();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        return name;
    }
}
