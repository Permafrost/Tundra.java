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

package permafrost.tundra.xml.sax;

import org.xml.sax.InputSource;
import permafrost.tundra.io.ReaderHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with InputSource objects.
 */
public final class InputSourceHelper {
    /**
     * Disallow instantiation of this class.
     */
    private InputSourceHelper() {}

    /**
     * Returns a new InputSource object that wraps the given InputStream object.
     *
     * @param inputStream   The InputStream to be wrapped.
     * @return              A new InputSource object representing the given InputStream object.
     */
    public static InputSource normalize(InputStream inputStream) {
        return normalize(inputStream, null);
    }

    /**
     * Returns a new InputSource object that wraps the given InputStream object.
     *
     * @param inputStream   The InputStream to be wrapped.
     * @param charset       The character set used to encode the text data in the given InputStream.
     * @return              A new InputSource object representing the given InputStream object.
     */
    public static InputSource normalize(InputStream inputStream, Charset charset) {
        if (inputStream == null) return null;
        return normalize(ReaderHelper.normalize(inputStream, CharsetHelper.normalize(charset)));
    }

    /**
     * Returns a new InputSource object that wraps the given Reader object.
     *
     * @param reader    The Reader object to be wrapped.
     * @return          A new InputSource object representing the given Reader object.
     */
    public static InputSource normalize(Reader reader) {
        if (reader == null) return null;
        return new InputSource(ReaderHelper.normalize(reader));
    }
}
