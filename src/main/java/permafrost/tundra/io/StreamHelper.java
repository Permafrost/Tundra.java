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
import java.nio.charset.Charset;

import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;

/**
 * A collection of convenience methods for working with streams.
 */
public class StreamHelper {
    /**
     * The default buffer size used by Tundra.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Disallow instantiation of this class.
     */
    private StreamHelper() {}

    /**
     * Closes all given closeable objects, suppressing any encountered
     * java.io.IOExceptions.
     * @param closeables One or more java.io.Closeable object to be closed.
     */
    public static void close(Closeable ... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                try {
                    if (closeable != null) closeable.close();
                } catch (IOException ex) {
                    // suppress the exception
                }
            }
        }
    }

    /**
     * Normalizes the given java.io.InputStream, by wrapping it in a
     * java.io.BufferedInputStream where appropriate.
     * @param inputStream   A java.io.InputStream to be normalized.
     * @return              The normalized java.io.InputStream.
     */
    public static InputStream normalize(InputStream inputStream) {
        if (!(inputStream instanceof FilterInputStream || inputStream instanceof ByteArrayInputStream)) {
            inputStream = new BufferedInputStream(inputStream, DEFAULT_BUFFER_SIZE);
        }

        return inputStream;
    }

    /**
     * Normalizes the given java.io.OutputStream, by wrapping it in a
     * java.io.BufferedOutputStream where appropriate.
     * @param outputStream  A java.io.OutputStream to be normalized.
     * @return              The normalized java.io.OutputStream.
     */
    public static OutputStream normalize(OutputStream outputStream) {
        if (!(outputStream instanceof FilterOutputStream || outputStream instanceof ByteArrayOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, DEFAULT_BUFFER_SIZE);
        }

        return outputStream;
    }

    /**
     * Normalizes the given java.io.Reader, by wrapping it in a
     * java.io.BufferedReader where appropriate.
     * @param reader    A java.io.Reader to be normalized.
     * @return          The normalized java.io.Reader.
     */
    public static Reader normalize(Reader reader) {
        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader, DEFAULT_BUFFER_SIZE);
        }

        return reader;
    }

    /**
     * Normalizes the given java.io.Writer, by wrapping it in a
     * java.io.BufferedWriter where appropriate.
     * @param writer    A java.io.Writer to be normalized.
     * @return          The normalized java.io.Writer.
     */
    public static Writer normalize(Writer writer) {
        if (!(writer instanceof BufferedWriter)) {
            writer = new BufferedWriter(writer, DEFAULT_BUFFER_SIZE);
        }

        return writer;
    }

    /**
     * Converts the given string to a java.io.InputStream.
     *
     * @param string    A string to be converted.
     * @return          A java.io.InputStream representation of the given string.
     */
    public static InputStream normalize(String string) {
        return normalize(string, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given string to a java.io.InputStream.
     *
     * @param string        A string to be converted.
     * @param charsetName   The character encoding set to use.
     * @return              A java.io.InputStream representation of the given string.
     */
    public static InputStream normalize(String string, String charsetName) {
        return normalize(string, CharsetHelper.normalize(charsetName));
    }

    /**
     * Converts the given string to a java.io.InputStream.
     *
     * @param string    A string to be converted.
     * @param charset   The character encoding set to use.
     * @return          A java.io.InputStream representation of the given string.
     */
    public static InputStream normalize(String string, Charset charset) {
        return normalize(BytesHelper.normalize(string, CharsetHelper.normalize(charset)));
    }

    /**
     * Converts the given byte[] to a java.io.InputStream.
     *
     * @param bytes A byte[] to be converted.
     * @return      A java.io.InputStream representation of the given byte[].
     */
    public static InputStream normalize(byte[] bytes) {
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    /**
     * Converts the given Object to a java.io.InputStream.
     *
     * @param object    A String, byte[], or java.io.InputStream object to be converted.
     * @return          A java.io.InputStream representation of the given object.
     */
    public static InputStream normalize(Object object) {
        return normalize(object, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Converts the given Object to a java.io.InputStream.
     *
     * @param object        A String, byte[], or java.io.InputStream object to be converted.
     * @param charsetName   The character encoding set to use.
     * @return              A java.io.InputStream representation of the given object.
     */
    public static InputStream normalize(Object object, String charsetName) {
        return normalize(object, CharsetHelper.normalize(charsetName));
    }

    /**
     * Converts the given Object to a java.io.InputStream.
     *
     * @param object    A String, byte[], or java.io.InputStream object to be converted.
     * @param charset   The character encoding set to use.
     * @return          A java.io.InputStream representation of the given object.
     */
    public static InputStream normalize(Object object, Charset charset) {
        if (object == null) return null;

        InputStream output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object);
        } else if (object instanceof String) {
            output = normalize((String)object, charset);
        } else if (object instanceof InputStream) {
            output = normalize((InputStream)object);
        } else {
            throw new IllegalArgumentException("object must be a String, byte[], or java.io.InputStream");
        }

        return output;
    }

    /**
     * Copies all data from the given input stream to the given output stream, and optionally
     * closes both streams.
     *
     * @param inputStream       An input stream containing data to be copied.
     * @param outputStream      An output stream to where the copied data will be written.
     * @param close             When true, both the input and output streams will be closed when done.
     * @throws IOException      If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream, boolean close) throws IOException {
        if (inputStream == null || outputStream == null) return;

        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;

            inputStream = normalize(inputStream);
            outputStream = normalize(outputStream);

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (close) close(inputStream, outputStream);
        }
    }

    /**
     * Copies all data from the given input stream to the given output stream, and then
     * closes both streams.
     *
     * @param inputStream    An input stream containing data to be copied.
     * @param outputStream   An output stream to where the copied data will be written.
     * @throws IOException   If there is a problem reading from or writing to the streams.
     */
    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, true);
    }

    /**
     * Copies all the data from the given reader to the given writer, then closes both.
     *
     * @param reader            The reader to copy data from.
     * @param writer            The writer to copy data to.
     * @throws IOException     If there is a problem reading from the reader or writing to the writer.
     */
    public static void copy(Reader reader, Writer writer) throws IOException {
        if (reader == null || writer == null) return;

        try {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int length;

            reader = normalize(reader);
            writer = normalize(writer);

            while ((length = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, length);
            }
        } finally {
            close(reader, writer);
        }
    }

    /**
     * Reads all data from the given input stream, and optionally closes it when done.
     *
     * @param inputStream       An input stream containing data to be read.
     * @param close             When true the input stream will be closed when done.
     * @throws IOException      If there is a problem reading from the stream.
     */
    public static byte[] readToBytes(InputStream inputStream, boolean close) throws IOException {
        if (inputStream == null) return null;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamHelper.copy(inputStream, outputStream, close);
        return outputStream.toByteArray();
    }

    /**
     * Reads all data from the given input stream, and optionally closes it when done.
     *
     * @param inputStream       An input stream containing data to be read.
     * @throws IOException      If there is a problem reading from the stream.
     */
    public static byte[] readToBytes(InputStream inputStream) throws IOException {
        return readToBytes(inputStream, true);
    }
}
