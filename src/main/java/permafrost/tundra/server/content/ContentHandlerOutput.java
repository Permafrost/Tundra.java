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
import java.io.OutputStream;

public class ContentHandlerOutput {
    protected OutputStream outputStream;
    protected Values values;
    protected InvokeState invokeState;

    public ContentHandlerOutput(OutputStream outputStream, Values values, InvokeState invokeState) {
        this.outputStream = outputStream;
        this.values = values;
        this.invokeState = invokeState;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Values getValues() {
        return values;
    }

    public InvokeState getInvokeState() {
        return invokeState;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void setValues(Values values) {
        this.values = values;
    }

    public void setInvokeState(InvokeState invokeState) {
        this.invokeState = invokeState;
    }
}
