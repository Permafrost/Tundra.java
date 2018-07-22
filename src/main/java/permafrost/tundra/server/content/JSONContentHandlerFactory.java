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
 * An Integration Server content handler factory for JSON.
 */
public class JSONContentHandlerFactory extends ContentHandlerFactory {
    /**
     * The MIME media type to be handled by objects created by this factory.
     */
    private String contentType;

    /**
     * Creates a new JSONContentHandlerFactory object for the default JSON MIME media type.
     */
    public JSONContentHandlerFactory() {
        this(JSONContentHandler.DEFAULT_JSON_CONTENT_TYPE);
    }

    /**
     * Creates a new JSONContentHandlerFactory object for the given MIME media type.
     * @param contentType  The MIME media type to be handled by objects created with this factory.
     */
    public JSONContentHandlerFactory(String contentType) {
        if (contentType == null) throw new NullPointerException("contentType must not be null");
        this.contentType = contentType;
    }

    /**
     * Returns a new content handler for handling JSON.
     *
     * @return A new content handler for handling JSON.
     */
    public ContentHandler create() {
        return new JSONContentHandler(contentType);
    }
}
