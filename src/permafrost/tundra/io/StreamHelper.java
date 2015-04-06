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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import permafrost.tundra.bytes.BytesHelper;
import permafrost.tundra.exception.BaseException;

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
     *
     * @param closeables One or more java.io.Closeable object to be closed.
     */
    public static void close(Closeable ... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException ex) {
                // suppress the exception
            }
        }
    }

    /**
     * Normalizes the given java.io.InputStream, by wrapping it in a
     * java.io.BufferedInputStream where appropriate.
     *
     * @param in A java.io.InputStream to be normalized.
     * @return   The normalized java.io.InputStream.
     */
    public static InputStream normalize(InputStream in) {
        if (in == null) return null;

        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in, DEFAULT_BUFFER_SIZE);
        }

        return in;
    }

    /**
     * Normalizes the given java.io.OutputStream, by wrapping it in a
     * java.io.BufferedOutputStream where appropriate.
     *
     * @param out A java.io.OutputStream to be normalized.
     * @return   The normalized java.io.OutputStream.
     */
    public static OutputStream normalize(OutputStream out) {
        if (out == null) return null;

        if (!(out instanceof BufferedOutputStream)) {
            out = new BufferedOutputStream(out, DEFAULT_BUFFER_SIZE);
        }

        return out;
    }

    /**
     * Converts the given string to a java.io.InputStream.
     *
     * @param in A string to be converted.
     * @return   A java.io.InputStream representation of the given string.
     * @throws BaseException
     */
    public static InputStream normalize(String in) throws BaseException {
        return normalize(in, null);
    }

    /**
     * Converts the given string to a java.io.InputStream.
     *
     * @param in        A string to be converted.
     * @param encoding  The character encoding set to use.
     * @return          A java.io.InputStream representation of the given string.
     * @throws BaseException
     */
    public static InputStream normalize(String in, String encoding) throws BaseException {
        return normalize(BytesHelper.normalize(in, encoding));
    }

    /**
     * Converts the given byte[] to a java.io.InputStream.
     *
     * @param bytes A byte[] to be converted.
     * @return      A java.io.InputStream representation of the given byte[].
     */
    public static InputStream normalize(byte[] bytes) {
        if (bytes == null) return null;
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Converts the given Object to a java.io.InputStream.
     *
     * @param object    A String, byte[], or java.io.InputStream object to be converted.
     * @return          A java.io.InputStream representation of the given object.
     */
    public static InputStream normalize(Object object) throws BaseException {
        return normalize(object, null);
    }

    /**
     * Converts the given Object to a java.io.InputStream.
     *
     * @param object    A String, byte[], or java.io.InputStream object to be converted.
     * @param encoding  The character encoding set to use.
     * @return          A java.io.InputStream representation of the given object.
     */
    public static InputStream normalize(Object object, String encoding) throws BaseException {
        if (object == null) return null;

        InputStream output;

        if (object instanceof byte[]) {
            output = normalize((byte[])object);
        } else if (object instanceof String) {
            output = normalize((String)object, encoding);
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
     * @param in    An input stream containing data to be copied.
     * @param out   An output stream to where the copied data will be written.
     * @param close When true, both the input and output streams will be closed when done.
     * @throws BaseException
     */
    public static void copy(InputStream in, OutputStream out, boolean close) throws BaseException {
        if (in == null || out == null) return;

        try {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;

            in = normalize(in);
            out = normalize(out);

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch(IOException ex) {
            throw new StreamException(ex);
        } finally {
            if (close) close(in, out);
        }
    }

    /**
     * Copies all data from the given input stream to the given output stream, and then
     * closes both streams.
     *
     * @param in    An input stream containing data to be copied.
     * @param out   An output stream to where the copied data will be written.
     * @throws BaseException
     */
    public static void copy(InputStream in, OutputStream out) throws BaseException {
        copy(in, out, true);
    }

    /**
     * Copies all the data from the given reader to the given writer, then closes both.
     *
     * @param reader The reader to copy data from.
     * @param writer The writer to copy data to.
     * @throws BaseException
     */
    public static void copy(java.io.Reader reader, java.io.Writer writer) throws BaseException {
        if (reader == null || writer == null) return;

        try {
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];
            int length;

            if (!(reader instanceof java.io.BufferedReader)) {
                reader = new java.io.BufferedReader(reader, DEFAULT_BUFFER_SIZE);
            }
            if (!(writer instanceof java.io.BufferedWriter)) {
                writer = new java.io.BufferedWriter(writer, DEFAULT_BUFFER_SIZE);
            }

            while ((length = reader.read(buffer)) > 0) {
                writer.write(buffer, 0, length);
            }
        } catch(IOException ex) {
            throw new StreamException(ex);
        } finally {
            close(reader, writer);
        }
    }
}
