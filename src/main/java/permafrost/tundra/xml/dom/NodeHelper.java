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

package permafrost.tundra.xml.dom;

import com.wm.app.b2b.server.ServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A collection of convenience methods for working with Node objects.
 */
public class NodeHelper {
    /**
     * Disallow instantiation of this class.
     */
    private NodeHelper() {}

    /**
     * Serializes a Node object to a stream using the default character set.
     *
     * @param node              The Node to be serialized.
     * @return                  The serialized Node.
     * @throws ServiceException If an XML transformation error occurs.
     */
    public static InputStream emit(Node node) throws ServiceException {
        return emit(node, null);
    }

    /**
     * Serializes a Node object to a stream using the given character set.
     *
     * @param node              The Node to be serialized.
     * @param charset           The character set to use.
     * @return                  The serialized Node.
     * @throws ServiceException If an XML transformation error occurs.
     */
    public static InputStream emit(Node node, Charset charset) throws ServiceException {
        if (node == null) return null;

        InputStream content = null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            // always defend against denial of service attacks
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, CharsetHelper.normalize(charset).displayName());
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, node instanceof Document ? "no" : "yes");

            transformer.transform(new DOMSource(node), new StreamResult(byteArrayOutputStream));

            content = InputStreamHelper.normalize(byteArrayOutputStream.toByteArray());
        } catch (TransformerException ex) {
            ExceptionHelper.raise(ex);
        }

        return content;
    }

}
