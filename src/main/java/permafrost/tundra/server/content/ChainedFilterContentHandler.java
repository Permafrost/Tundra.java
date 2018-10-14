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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A content handler which wraps a list of chained content handlers where each handler is invoked in sequence
 * to process content.
 */
public class ChainedFilterContentHandler extends ProxyContentHandler {
    /**
     * The list of filters chained together.
     */
    protected List<FilterContentHandler> filters;

    /**
     * Constructs a new ChainedContentHandler given a list of ContentHandler objects.
     *
     * @param handler   The content handlers which ultimately handles content.
     * @param filters   The chained filters to be wrapped.
     */
    ChainedFilterContentHandler(ContentHandler handler, List<FilterContentHandler> filters) {
        super(handler);
        this.filters = filters;
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
    @Override
    public Values getInputValues(InputStream inputStream, InvokeState invokeState) throws IOException {
        ContentHandlerInput contentHandlerInput = new ContentHandlerInput(inputStream, invokeState, new Values());

        if (filters != null) {
            for (FilterContentHandler filter : filters) {
                filter.getInputValues(contentHandlerInput);
            }
        }

        Values values = super.getInputValues(contentHandlerInput.getInputStream(), contentHandlerInput.getInvokeState());
        contentHandlerInput.getValues().copyFrom(values);

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
    @Override
    public void putOutputValues(OutputStream outputStream, Values values, InvokeState invokeState) throws IOException {
        ContentHandlerOutput contentHandlerOutput = new ContentHandlerOutput(outputStream, values, invokeState);

        if (filters != null) {
            for (FilterContentHandler filter : filters) {
                filter.putOutputValues(contentHandlerOutput);
            }
        }

        super.putOutputValues(contentHandlerOutput.getOutputStream(), contentHandlerOutput.getValues(), contentHandlerOutput.getInvokeState());
    }
}
