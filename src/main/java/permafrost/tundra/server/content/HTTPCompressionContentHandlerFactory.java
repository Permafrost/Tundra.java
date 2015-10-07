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
import com.wm.app.b2b.server.ContentManager;
import java.util.Map;

/**
 * An Integration Server content handler factory for HTTPCompressionContentHandler objects.
 */
public class HTTPCompressionContentHandlerFactory extends ProxyContentHandlerFactory {
    /**
     * Creates a new HTTPCompressionContentHandlerFactory object.
     *
     * @param factory The content handler factory to be proxied.
     */
    public HTTPCompressionContentHandlerFactory(ContentHandlerFactory factory) {
        super(factory);
    }

    /**
     * Returns a new HTTPCompressionContentHandler object.
     *
     * @return A new HTTPCompressionContentHandler object.
     */
    public ContentHandler create() {
        return new HTTPCompressionContentHandler(super.create());
    }

    /**
     * Wraps all existing content part handler registrations with a HTTPCompressionContentHandlerFactory.
     */
    public static void register() {
        for (Map.Entry<String, ContentHandlerFactory> entry : ContentManagerHelper.getRegistrations().entrySet()) {
            String type = entry.getKey();
            ContentHandlerFactory factory = entry.getValue();

            if (type != null && (!(type.equals("application/wm-soap") || type.equals("application/soap") || type.equals("application/soap+xml"))) && factory != null && (!(factory instanceof HTTPCompressionContentHandlerFactory))) {
                ContentManager.registerHandler(entry.getKey(), new HTTPCompressionContentHandlerFactory(entry.getValue()));
            }
        }
    }
}
