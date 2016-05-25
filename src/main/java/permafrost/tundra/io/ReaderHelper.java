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

package permafrost.tundra.io;

import permafrost.tundra.lang.CharsetHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with Reader objects.
 */
public class ReaderHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ReaderHelper() {}

    /**
     * Returns a new Reader object given an InputStream.
     *
     * @param inputStream   The InputStream to be converted to a Reader.
     * @return              A new Reader object representing the given InputStream.
     */
    public static Reader normalize(InputStream inputStream) {
        return normalize(inputStream, null);
    }

    /**
     * Returns a new Reader object given an InputStream.
     *
     * @param inputStream   The InputStream to be converted to a Reader.
     * @param charset       The Charset to use when reading from the InputStream.
     * @return              A new Reader object representing the given InputStream.
     */
    public static Reader normalize(InputStream inputStream, Charset charset) {
        if (inputStream == null) return null;
        return normalize(new InputStreamReader(inputStream, CharsetHelper.normalize(charset)));
    }

    /**
     * Normalizes the given Reader, by wrapping it in a BufferedReader where appropriate.
     *
     * @param reader    A Reader to be normalized.
     * @return          The normalized Reader.
     */
    public static Reader normalize(Reader reader) {
        if (reader == null) return null;

        if (!(reader instanceof BufferedReader)) {
            reader = new BufferedReader(reader, InputOutputHelper.DEFAULT_BUFFER_SIZE);
        }

        return reader;
    }

    /**
     * Reads all data from the given Reader, and then closes it when done.
     *
     * @param reader        A Reader containing data to be read.
     * @return              Returns a String containing all the data read from the given Reader.
     * @throws IOException  If there is a problem reading from the Reader.
     */
    public static String read(Reader reader) throws IOException {
        return read(reader, true);
    }

    /**
     * Reads all data from the given input stream, and optionally closes it when done.
     *
     * @param reader        A Reader containing data to be read.
     * @param close         When true the Reader will be closed when done.
     * @return              Returns a String containing all the data read from the given Reader.
     * @throws IOException  If there is a problem reading from the Reader.
     */
    public static String read(Reader reader, boolean close) throws IOException {
        if (reader == null) return null;

        StringWriter writer = new StringWriter();
        InputOutputHelper.copy(reader, writer, close);
        return writer.toString();
    }
}
