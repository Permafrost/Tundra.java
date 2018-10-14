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

import com.wm.app.b2b.server.ContentHandlerFactory;
import permafrost.tundra.lang.Startable;

public abstract class FilterContentHandlerFactory extends ContentHandlerFactory implements Startable {
    /**
     * Whether content logging is started.
     */
    protected volatile boolean started = false;

    /**
     * Returns a new HTTPCompressionContentHandler object.
     *
     * @return A new HTTPCompressionContentHandler object.
     */
    @Override
    public abstract FilterContentHandler create();

    /**
     * Starts logging content.
     */
    @Override
    public synchronized void start() {
        started = true;
    }

    /**
     * Stops logging content.
     */
    @Override
    public synchronized void stop() {
        started = false;
    }

    /**
     * Returns true if content logging is started.
     *
     * @return True if content logging is started.
     */
    @Override
    public boolean isStarted() {
        return started;
    }
}
