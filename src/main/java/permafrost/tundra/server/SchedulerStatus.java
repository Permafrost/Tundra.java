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

package permafrost.tundra.server;

/**
 * The Integration Server user task scheduler statuses.
 */
public enum SchedulerStatus {
    STARTED, PAUSED, STOPPED;

    /**
     * Returns the SchedulerStatus representing the given String.
     *
     * @param status    The status to be normalized.
     * @return          The SchedulerStatus representing the given string.
     */
    public static SchedulerStatus normalize(String status) {
        if (status == null) return null;
        return valueOf(status.trim().toUpperCase());
    }

    /**
     * Returns the SchedulerStatus appropriate for the given ScheduleManager state flags.
     *
     * @param isRunning Is the ScheduleManager running?
     * @param isPaused  Is the ScheduleManager paused?
     * @return          The relevant ScheduleStatus given the ScheduleManager state flags.
     */
    public static SchedulerStatus normalize(boolean isRunning, boolean isPaused) {
        SchedulerStatus status;
        if (isRunning) {
            if (isPaused) {
                status = PAUSED;
            } else {
                status = STARTED;
            }
        } else {
            status = STOPPED;
        }
        return status;
    }
}
