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

import com.wm.app.b2b.server.InvokeState;
import com.wm.util.Values;
import java.io.InputStream;

/**
 * Wraps the arguments to the ContentHandler getInputValues method, so that they can be mutated.
 */
public class ContentHandlerInput {
    /**
     * The input stream to be processed by a content handler.
     */
    protected InputStream inputStream;
    /**
     * The current invocation state.
     */
    protected InvokeState invokeState;
    /**
     * The resulting values to be added to the pipeline after processing the input stream.
     */
    protected Values values;

    /**
     * Constructs a new ContentHandlerInput object.
     *
     * @param inputStream   The input stream.
     * @param invokeState   The current invocation state.
     * @param values        The resulting values.
     */
    public ContentHandlerInput(InputStream inputStream, InvokeState invokeState, Values values) {
        this.inputStream = inputStream;
        this.invokeState = invokeState;
        this.values = values;
    }

    /**
     * Returns the input stream.
     *
     * @return The input stream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the invocation state.
     *
     * @return The invocation state.
     */
    public InvokeState getInvokeState() {
        return invokeState;
    }

    /**
     * Returns the values.
     *
     * @return The values.
     */
    public Values getValues() {
        return values;
    }

    /**
     * Sets the stored input stream to be the given stream.
     *
     * @param inputStream   The new input stream.
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Sets the stored invocation state to be the given invocation state.
     *
     * @param invokeState   The new invocation state.
     */
    public void setInvokeState(InvokeState invokeState) {
        this.invokeState = invokeState;
    }

    /**
     * Sets the stored values to be the given values.
     *
     * @param values        The new values.
     */
    public void setValues(Values values) {
        this.values = values;
    }
}
