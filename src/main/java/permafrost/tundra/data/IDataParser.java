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

package permafrost.tundra.data;

import com.wm.data.IData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Generic interface for serializing and deserializing IData documents.
 */
public interface IDataParser {
    /**
     * Parses the data in the given input stream, returning an IData representation.
     * @param inputStream   The input stream to be parsed.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    IData parse(InputStream inputStream) throws IOException;

    /**
     * Parses the data in the given input stream, returning an IData representation.
     * @param inputStream   The input stream to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    IData parse(InputStream inputStream, Charset charset) throws IOException;

    /**
     * Parses the data in the given input stream, returning an IData representation.
     * @param inputStream   The input stream to be parsed.
     * @param charsetName   The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    IData parse(InputStream inputStream, String charsetName) throws IOException;

    /**
     * Serializes the given IData document.
     * @param document      The IData document to be serialized.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    InputStream emit(IData document) throws IOException;

    /**
     * Serializes the given IData document.
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    InputStream emit(IData document, Charset charset) throws IOException;

    /**
     * Serializes the given IData document.
     * @param document      The IData document to be serialized.
     * @param charsetName   The character set to use when serializing the IData document.
     * @return              A serialized representation of the given IData document.
     * @throws IOException  If an I/O error occurs.
     */
    InputStream emit(IData document, String charsetName) throws IOException;
}
