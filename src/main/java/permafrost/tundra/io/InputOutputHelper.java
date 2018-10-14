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
     * Copies all data from the given input stream to the given output stream, and then closes both streams.
     *
     * @param inputStream  An input stream containing data to be copied.
     * @param outputStream An output stream to where the copied data will be written.
     * @throws IOException If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, true);
    }

    /**
     * Copies all data from the given input stream to the given output stream, and optionally closes both streams.
     *
     * @param inputStream  An input stream containing data to be copied.
     * @param outputStream An output stream to where the copied data will be written.
     * @param close        When true, both the input and output streams will be closed when done.
     * @throws IOException If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean close) throws IOException {
        if (inputStream == null || outputStream == null) return;

        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;

            inputStream = InputStreamHelper.normalize(inputStream);
            outputStream = OutputStreamHelper.normalize(outputStream);

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (close) CloseableHelper.close(inputStream, outputStream);
        }
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
     * Copies all the data from the given reader to the given writer, then closes both.
     *
     * @param reader        The reader to copy data from.
     * @param writer        The writer to copy data to.
     * @param close         When true, both the reader and writer will be closed when done.
     * @throws IOException  If there is a problem reading from the reader or writing to the writer.
     */
    public static void copy(Reader reader, Writer writer, boolean close) throws IOException {
        if (reader == null || writer == null) return;

        try {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int length;

            reader = ReaderHelper.normalize(reader);
            writer = WriterHelper.normalize(writer);

            while ((length = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, length);
            }
        } finally {
            if (close) CloseableHelper.close(reader, writer);
        }
    }
}
