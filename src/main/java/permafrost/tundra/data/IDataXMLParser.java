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
import com.wm.util.coder.IDataXMLCoder;
import permafrost.tundra.lang.CharsetHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Deserializes and serializes IData objects from and to XML.
 */
public class IDataXMLParser extends IDataTextParser {
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final IDataXMLParser INSTANCE = new IDataXMLParser();
    }

    /**
     * Disallow instantiation of this class.
     */
    private IDataXMLParser() {}

    /**
     * Returns the singleton instance of this class.
     * @return The singleton instance of this class.
     */
    public static IDataXMLParser getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns the MIME type this parser handles.
     * @return The MIME type this parser handles.
     */
    public String getContentType() {
        return "text/xml";
    }

    /**
     * Returns an IData representation of the XML data read from the given input stream.
     *
     * @param inputStream                       The input stream to be decoded.
     * @param charset                           The character set to use.
     * @return                                  An IData representation of the given input stream data.
     * @throws IOException                      If there is a problem reading from the stream.
     */
    public IData decode(InputStream inputStream, Charset charset) throws IOException {
        IDataXMLCoder parser = new IDataXMLCoder(CharsetHelper.normalize(charset).displayName());
        return parser.decode(inputStream);
    }

    /**
     * Serializes the given IData document as XML to the given output stream.
     *
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, Charset charset) throws IOException {
        IDataXMLCoder parser = new IDataXMLCoder(CharsetHelper.normalize(charset).displayName());
        parser.encode(outputStream, document);
    }
}
