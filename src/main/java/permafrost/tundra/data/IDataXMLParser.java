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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.util.coder.IDataXMLCoder;
import com.wm.util.coder.XMLCoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.xml.dom.DocumentHelper;
import permafrost.tundra.xml.dom.NodeHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Deserializes and serializes IData objects from and to XML.
 */
public final class IDataXMLParser extends IDataParser {
    /**
     * The root node name that identifies a Values-encoded XML file.
     */
    private static final String VALUES_XML_ROOT_NODE_NAME = "Values";

    /**
     * The root node name that identifies an IData-encoded XML file.
     */
    private static final String IDATA_XML_ROOT_NODE_NAME = "IDataXMLCoder";

    /**
     * Construct a new IDataXMLParser.
     */
    public IDataXMLParser() {
        super("text/xml");
    }

    /**
     * Returns an IData representation of the XML data read from the given input stream.
     *
     * @param inputStream       The input stream to be decoded.
     * @param charset           The character set to use.
     * @return                  An IData representation of the given input stream data.
     * @throws IOException      If there is a problem reading from the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        // convert inputStream to resettable ByteArrayInputStream
        inputStream = InputStreamHelper.memoize(inputStream);
        IData output = null;

        try {
            Document document = DocumentHelper.parse(inputStream, charset, false);
            String rootNodeName = null;

            if (document != null) {
                Element rootNode = document.getDocumentElement();
                if (rootNode != null) {
                    rootNodeName = rootNode.getNodeName();
                }
            }

            if (rootNodeName != null && rootNodeName.equals(IDATA_XML_ROOT_NODE_NAME)) {
                IDataXMLCoder parser = new IDataXMLCoder(CharsetHelper.normalize(charset).displayName());
                inputStream.reset();
                output = parser.decode(inputStream);
            } else if (rootNodeName != null && rootNodeName.equals(VALUES_XML_ROOT_NODE_NAME)) {
                XMLCoder parser = new XMLCoder(true);
                inputStream.reset();
                output = parser.decode(inputStream);
            } else if (document != null) {
               output = NodeHelper.parse(document);
            }
        } finally {
            CloseableHelper.close(inputStream);
        }

        return output;
    }

    /**
     * Serializes the given IData document as XML to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        IDataXMLCoder parser = new IDataXMLCoder(CharsetHelper.normalize(charset).displayName());
        parser.encode(outputStream, document);
    }
}
