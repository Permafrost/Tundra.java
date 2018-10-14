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
import com.wm.app.b2b.server.InvokeState;
import com.wm.util.Values;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An Integration Server content handler that proxies method calls to another content handler.
 */
abstract public class ProxyContentHandler implements ContentHandler {
    /**
     * The content handler that method calls will be proxied to.
     */
    protected ContentHandler handler;

    /**
     * Creates a new ProxyContentHandler object.
     *
     * @param handler The content handler which method calls will be proxied to by this object.
     */
    public ProxyContentHandler(ContentHandler handler) {
        if (handler == null) throw new NullPointerException("handler must not be null");
        this.handler = handler;
    }

    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input.
     *
     * @param inputStream  The input stream from which to read.
     * @param invokeState  The current invocation state (such as the current user).
     * @return             The prepared service invocation input pipeline.
     * @throws IOException If an error occurs reading from the input stream.
     */
    public Values getInputValues(InputStream inputStream, InvokeState invokeState) throws IOException {
        return handler.getInputValues(inputStream, invokeState);
    }

    /**
     * Encodes output of service invocation. Writes output data (including possibly error messages)
     * directly to the output stream.
     *
     * @param outputStream The output stream to which to write.
     * @param output       The output values to encode.
     * @param invokeState  The current invocation state (such as the current user).
     * @throws IOException If an error occurs writing to the output stream.
     */
    public void putOutputValues(OutputStream outputStream, Values output, InvokeState invokeState) throws IOException {
        handler.putOutputValues(outputStream, output, invokeState);
    }

    /**
     * Returns the MIME media type that this content handler handles.
     *
     * @return The MIME media type that this content handler handles.
     */
    public String getContentType() {
        return handler.getContentType();
    }

    /**
     * Returns the content handler which is being proxied.
     *
     * @return The content handler which is being proxied.
     */
    public ContentHandler getContentHandler() {
        return handler;
    }
}
