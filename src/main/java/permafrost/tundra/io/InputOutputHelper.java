/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * A collection of I/O related convenience methods.
 */
public final class InputOutputHelper {
    /**
     * The default I/O buffer size used by Tundra.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Disallow instantiation of this class.
     */
    private InputOutputHelper() {}

    /**
     * Copies all data from the given input stream to the given output stream, then closes both streams.
     *
     * @param inputStream   An input stream containing data to be copied.
     * @param outputStream  An output stream to where the copied data will be written.
     * @throws IOException  If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, true);
    }

    /**
     * Copies all data from the given input stream to the given output stream, then optionally closes both streams.
     *
     * @param inputStream   An input stream containing data to be copied.
     * @param outputStream  An output stream to where the copied data will be written.
     * @param close         When true, both the input and output streams will be closed when done.
     * @throws IOException  If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean close) throws IOException {
        copy(inputStream, outputStream, close, -1);
    }

    /**
     * Copies all data from the given input stream to the given output stream, then optionally closes both streams.
     *
     * @param inputStream   An input stream containing data to be copied.
     * @param outputStream  An output stream to where the copied data will be written.
     * @param close         When true, both the input and output streams will be closed when done.
     * @param bufferSize    The size of the buffer to use when copying the data.
     * @throws IOException  If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean close, int bufferSize) throws IOException {
        if (inputStream == null || outputStream == null) return;

        try {
            bufferSize = normalizeBufferSize(bufferSize);

            inputStream = InputStreamHelper.normalize(inputStream, bufferSize);
            outputStream = OutputStreamHelper.normalize(outputStream, bufferSize);

            int length;
            byte[] buffer = new byte[bufferSize];

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (close) CloseableHelper.close(inputStream, outputStream);
        }
    }

    /**
     * Normalizes the given buffer size, if the given size is less than or equal to zero then set to the default
     * buffer size.
     *
     * @param bufferSize    The buffer size to normalize.
     * @return              The normalized buffer size.
     */
    public static int normalizeBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        return bufferSize;
    }

    /**
     * Copies all the data from the given reader to the given writer, then closes both.
     *
     * @param reader        The reader to copy data from.
     * @param writer        The writer to copy data to.
     * @throws IOException  If there is a problem reading from the reader or writing to the writer.
     */
    public static void copy(Reader reader, Writer writer) throws IOException {
        copy(reader, writer, true);
    }

    /**
     * Copies all the data from the given reader to the given writer, then optionally closes both.
     *
     * @param reader        The reader to copy data from.
     * @param writer        The writer to copy data to.
     * @param close         When true, both the reader and writer will be closed when done.
     * @throws IOException  If there is a problem reading from the reader or writing to the writer.
     */
    public static void copy(Reader reader, Writer writer, boolean close) throws IOException {
        copy(reader, writer, close, -1);
    }

    /**
     * Copies all the data from the given reader to the given writer, then optionally closes both.
     *
     * @param reader        The reader to copy data from.
     * @param writer        The writer to copy data to.
     * @param close         When true, both the reader and writer will be closed when done.
     * @param bufferSize    The buffering size in bytes.
     * @throws IOException  If there is a problem reading from the reader or writing to the writer.
     */
    public static void copy(Reader reader, Writer writer, boolean close, int bufferSize) throws IOException {
        if (reader == null || writer == null) return;

        try {
            bufferSize = normalizeBufferSize(bufferSize);

            reader = ReaderHelper.normalize(reader, bufferSize);
            writer = WriterHelper.normalize(writer, bufferSize);

            int length;
            char[] buffer = new char[bufferSize];

            while ((length = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, length);
            }
        } finally {
            if (close) CloseableHelper.close(reader, writer);
        }
    }
}
