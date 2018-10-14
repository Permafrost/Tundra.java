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

import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.InvokeState;
import com.wm.util.Values;
import permafrost.tundra.lang.Startable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An abstract class for a content handler which filters the inputs to another content handler.
 */
public abstract class FilterContentHandler implements ContentHandler {
    /**
     * Whether this content handler is started or stopped.
     */
    protected Startable startable;
    /**
     * The MIME media type this content handler handles.
     */
    protected volatile String contentType = "*/*";

    /**
     * Constructs a new FilterContentHandler.
     *
     * @param startable Used to start and stop content filtering.
     */
    public FilterContentHandler(Startable startable) {
        if (startable == null) throw new NullPointerException("startable must not be null");
        this.startable = startable;
    }

    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input.
     *
     * @param contentHandlerInput   The input arguments for processing by the content handler.
     * @throws IOException          If an error occurs reading from the input stream.
     */
    public void getInputValues(ContentHandlerInput contentHandlerInput) throws IOException {}

    /**
     * Encodes output of service invocation. Writes output data (including possibly error messages)
     * directly to the output stream.
     *
     * @param contentHandlerOutput  The input arguments for processing by the content handler.
     * @throws IOException          If an error occurs writing to the output stream.
     */
    public void putOutputValues(ContentHandlerOutput contentHandlerOutput) throws IOException {}
    /**
     * Reads input for service invocation. Turns properly formatted input into an instance of Values
     * suitable for service invocation. Called before service invocation to provide input.
     *
     * @param inputStream  The input stream from which to read.
     * @param invokeState  The current invocation state (such as the current user).
     * @return             The prepared service invocation input pipeline.
     * @throws IOException If an error occurs reading from the input stream.
     */
    public final Values getInputValues(InputStream inputStream, InvokeState invokeState) throws IOException {
        ContentHandlerInput contentHandlerInput = new ContentHandlerInput(inputStream, invokeState, new Values());
        getInputValues(contentHandlerInput);
        return contentHandlerInput.getValues();
    }

    /**
     * Encodes output of service invocation. Writes output data (including possibly error messages)
     * directly to the output stream.
     *
     * @param outputStream The output stream to which to write.
     * @param values       The output values to encode.
     * @param invokeState  The current invocation state (such as the current user).
     * @throws IOException If an error occurs writing to the output stream.
     */
    public final void putOutputValues(OutputStream outputStream, Values values, InvokeState invokeState) throws IOException {
        putOutputValues(new ContentHandlerOutput(outputStream, values, invokeState));
    }

    /**
     * Sets the MIME media type this content handler handles.
     *
     * @param contentType   The MIME media type this content handler handles.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the MIME media type this content handler handles.
     *
     * @return the MIME media type this content handler handles.
     */
    public String getContentType() {
        return contentType;
    }
}
