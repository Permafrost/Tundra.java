/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.ContentHandlerFactory;

/**
 * An Integration Server content handler factory that proxies method calls to another content handler factory.
 */
public class ProxyContentHandlerFactory extends ContentHandlerFactory {
    /**
     * The content handler returned by the factory.
     */
    private ContentHandlerFactory factory;

    /**
     * Creates a new ProxyContentHandlerFactory object.
     *
     * @param factory The content handler to be returned by the factory.
     */
    public ProxyContentHandlerFactory(ContentHandlerFactory factory) {
        if (factory == null) throw new NullPointerException("handler must not null");
        this.factory = factory;
    }

    /**
     * Returns a content handler.
     *
     * @return A content handler.
     */
    public ContentHandler create() {
        return factory.create();
    }

    /**
     * Returns the content handler factory which this factory proxies method calls to.
     * @return The content handler factory which this factory proxies method calls to.
     */
    public ContentHandlerFactory getFactory() {
        return factory;
    }
}
