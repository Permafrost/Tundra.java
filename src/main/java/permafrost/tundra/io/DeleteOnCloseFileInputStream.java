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
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Automatically deletes the underlying file when the close method is called on the input stream.
 */
public class DeleteOnCloseFileInputStream extends MarkableFileInputStream {
    private File file = null;
    private boolean isClosed = false;

    /**
     * Constructs a new AutoDeleteFileInputStream by opening a connection to an actual file, the file
     * named by the File object file in the file system.
     * @param file                      The file to be opened for reading.
     * @throws FileNotFoundException    If the file does not exist, is a directory rather than a regular
     *                                  file, or for some other reason cannot be opened for reading.
     */
    public DeleteOnCloseFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /**
     * Constructs a new AutoDeleteFileInputStream by opening a connection to an actual file, the file
     * named by the path name in the file system.
     * @param name                      The file to be opened for reading.
     * @throws FileNotFoundException    If the file does not exist, is a directory rather than a regular
     *                                  file, or for some other reason cannot be opened for reading.
     */
    public DeleteOnCloseFileInputStream(String name) throws FileNotFoundException {
        super(name);
        if (name != null) file = new File(name);
    }

    /**
     * Closes this file input stream and releases any system resources associated with the stream, then
     * deletes the file.
     * @throws IOException If the file cannot be deleted, or if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();

        synchronized (this) {
            // automatically delete the file after closing the stream
            if (!isClosed && file != null && !file.delete())
                throw new IOException("File could not be deleted: " + FileHelper.normalize(file));
            isClosed = true;
        }
    }

    /**
     * Returns the file this input stream reads from.
     * @return The file this input stream reads from.
     */
    public File getFile() {
        return file;
    }
}