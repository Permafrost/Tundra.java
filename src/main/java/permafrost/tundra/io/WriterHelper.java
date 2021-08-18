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

import permafrost.tundra.lang.CharsetHelper;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for Writer objects.
 */
public final class WriterHelper {
    /**
     * Disallow instantiation of this class.
     */
    private WriterHelper() {}

    /**
     * Returns a new Writer object which wraps the given OutputStream object.
     *
     * @param outputStream  The OutputStream to be wrapped.
     * @param charset       The character set used to encode the text data in the given OutputStream.
     * @return              A new Writer object which represents the given OutputStream.
     */
    public static Writer normalize(OutputStream outputStream, Charset charset) {
        return normalize(outputStream, charset, -1);
    }

    /**
     * Returns a new Writer object which wraps the given OutputStream object.
     *
     * @param outputStream  The OutputStream to be wrapped.
     * @param charset       The character set used to encode the text data in the given OutputStream.
     * @param bufferSize    The buffering size in bytes.
     * @return              A new Writer object which represents the given OutputStream.
     */
    public static Writer normalize(OutputStream outputStream, Charset charset, int bufferSize) {
        if (outputStream == null) return null;
        return normalize(new OutputStreamWriter(outputStream, CharsetHelper.normalize(charset)), bufferSize);
    }

    /**
     * Normalizes the given Writer, by wrapping it in a BufferedWriter where appropriate.
     *
     * @param writer        A Writer to be normalized.
     * @return              The normalized Writer.
     */
    public static Writer normalize(Writer writer) {
        return normalize(writer, -1);
    }

    /**
     * Normalizes the given Writer, by wrapping it in a BufferedWriter where appropriate.
     *
     * @param writer        A Writer to be normalized.
     * @param bufferSize    The buffering size in bytes.
     * @return              The normalized Writer.
     */
    public static Writer normalize(Writer writer, int bufferSize) {
        if (writer == null) return null;

        if (!(writer instanceof BufferedWriter)) {
            writer = new BufferedWriter(writer, InputOutputHelper.normalizeBufferSize(bufferSize));
        }

        return writer;
    }
}
