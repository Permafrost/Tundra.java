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

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A file input stream which supports the mark and reset methods.
 */
public class MarkableFileInputStream extends FileInputStream {
    /**
     * The marked position that will be returned to upon calling the reset method.
     */
    private volatile long markPosition = 0;

    /**
     * Creates a MarkableFileInputStream by opening a connection to an actual file named by the given File object.
     *
     * @param file The file to read from.
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a regular file, or for some
     *                               other reason cannot be opened for reading.
     */
    public MarkableFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    /**
     * Creates a MarkableFileInputStream by opening a connection to an actual file named by the given FileDescriptor
     * object.
     *
     * @param descriptor The file descriptor to read from.
     */
    public MarkableFileInputStream(FileDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Creates a MarkableFileInputStream by opening a connection to an actual file with the given name.
     *
     * @param filename The file to read from.
     * @throws FileNotFoundException If the file does not exist, is a directory rather than a regular file, or for some
     *                               other reason cannot be opened for reading.
     */
    public MarkableFileInputStream(String filename) throws FileNotFoundException {
        super(FileHelper.construct(filename));
    }

    /**
     * Returns true because this class supports marking and resetting the stream.
     *
     * @return True because this class supports marking and resetting the stream.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream. A subsequent call to the reset method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     *
     * @param readLimit This parameter is ignored.
     */
    @Override
    public synchronized void mark(int readLimit) {
        try {
            markPosition = getChannel().position();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Repositions this stream to the position at the time the mark method was last called on this input stream, or to
     * the start of the stream if the mark method has never been called.
     *
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public synchronized void reset() throws IOException {
        getChannel().position(markPosition);
    }
}
