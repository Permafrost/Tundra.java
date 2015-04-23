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

package permafrost.tundra.io;

import java.io.*;

public class MarkableInputStream extends FilterInputStream {
    /**
     * Creates a new MarkableInputStream, which wraps the given input stream
     * object in a stream that supports the mark and reset methods.
     *
     * To provide support for mark and reset methods to a given input stream,
     * this class creates a temporary backing file, then copies the entire
     * contents of the given stream to the file, and then uses the file
     * for reading the MarkableInputStream.
     *
     * @param inputStream   The stream to be wrapped.
     * @throws IOException  If an I/O error occurs while reading from the stream.
     */
    public MarkableInputStream(InputStream inputStream) throws IOException {
        super(inputStream);

        File backingFile = FileHelper.create();
        FileHelper.writeFromStream(backingFile, inputStream, false);
        in = new DeleteOnCloseFileInputStream(backingFile);
    }

    /**
     * Returns true because this class supports marking and resetting the stream.
     * @return True because this class supports marking and resetting the stream.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to the reset
     * method repositions this stream at the last marked position so that subsequent
     * reads re-read the same bytes.
     * @param readLimit This parameter is ignored.
     */
    @Override
    public synchronized void mark(int readLimit) {
        in.mark(readLimit);
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on
     * this input stream, or to the start of the stream if the mark method has never been called.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }
}
