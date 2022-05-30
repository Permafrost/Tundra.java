/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
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

import permafrost.tundra.time.DateTimeHelper;

/**
 * Wrapper for Integration Server thread, which allows the task start time to be cleared.
 */
public class ServerThread extends com.wm.app.b2b.server.ServerThread {
    /**
     * The time this thread started work on a new task.
     */
    protected volatile Long startTime;

    /**
     * Create a new ServerThread object.
     *
     * @param runnable  The task this thread will run.
     */
    public ServerThread(Runnable runnable) {
        super(runnable);
    }

    /**
     * Sets the start time of this thread to current time.
     */
    @Override
    public void setStartTime() {
        super.setStartTime();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Clears the start time of this thread to indicate it is now idle.
     */
    public void clearStartTime() {
        this.startTime = null;
    }

    /**
     * Returns the formatted start time of this thread for display.
     *
     * @return the formatted start time of this thread for display.
     */
    @Override
    public String getStartTime() {
        Long startTime = this.startTime;
        String formattedStartTime = null;
        if (startTime != null) {
            formattedStartTime = DateTimeHelper.format(startTime, "EE, d MMM yyyy HH:mm:ss");
        }
        return formattedStartTime;
    }
}
