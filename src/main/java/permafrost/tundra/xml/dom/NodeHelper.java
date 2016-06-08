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
import com.wm.data.IData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A collection of convenience methods for working with org.w3c.dom.Node objects.
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

    /**
     * Returns an IData representation of the given Node object.
     *
     * @param node      A Node object.
     * @return          An IData representation of the given Node object.
     */
    public static IData toIData(Node node) throws ServiceException {
        return toIData(node, true);
    }

    /**
     * Returns an IData representation of the given Node object.
     *
     * @param node      A Node object.
     * @param recurse   If true, child nodes will be recursed and returned also.
     * @return          An IData representation of the given Node object.
     */
    public static IData toIData(Node node, boolean recurse) throws ServiceException {
        if (node == null) return null;

        // if node is a Document, then convert from root node down
        if (node instanceof Document) node = ((Document)node).getDocumentElement();

        IDataMap map = new IDataMap();

        map.put("node", node);
        map.put("name", node.getNodeName());

        String localName = node.getLocalName();
        if (localName != null) map.put("name.local", localName);

        map.put("type", nodeTypeToString(node.getNodeType()));

        String prefix = node.getPrefix();
        if (prefix != null) map.put("namespace.prefix", prefix);

        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) map.put("namespace.uri", namespaceURI);

        try {
            String baseURI = node.getBaseURI();
            if (baseURI != null) map.put("base.uri", baseURI);
        } catch (UnsupportedOperationException ex) {
            // do nothing, not supported
        }

        try {
            String content = null;

            if (node instanceof Element) {
                content = ElementHelper.getContent((Element) node);
            } else {
                content = node.getNodeValue();
            }

            if (content != null) {
                map.put("content", content.trim());
                map.put("content.raw", content);
            }
        } catch (UnsupportedOperationException ex) {
            // do nothing, not supported
        }

        if (node.hasAttributes()) map.put("attributes", toIDataArray(node.getAttributes()));

        if (recurse && node.hasChildNodes()) {
            boolean hasElementChildren = false;

            Nodes children = Nodes.of(node.getChildNodes());
            List<IData> list = new ArrayList<IData>(children.size());

            for (Node child : children) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    hasElementChildren = true;
                    list.add(toIData(child));
                }
            }

            if (hasElementChildren) map.put("children", list.toArray(new IData[list.size()]));
        }

        return map;
    }

    /**
     * Returns a String representation of the given Node type.
     *
     * @param nodeType  A Node type.
     * @return          A String represntation of the given Node type.
     */
    private static String nodeTypeToString(int nodeType) {
        String output = null;

        switch(nodeType) {
            case Node.ATTRIBUTE_NODE:
                output = "ATTRIBUTE_NODE";
                break;
            case Node.CDATA_SECTION_NODE:
                output = "CDATA_SECTION_NODE";
                break;
            case Node.COMMENT_NODE:
                output = "COMMENT_NODE";
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                output = "DOCUMENT_FRAGMENT_NODE";
                break;
            case Node.DOCUMENT_NODE:
                output = "DOCUMENT_NODE";
                break;
            case Node.DOCUMENT_TYPE_NODE:
                output = "DOCUMENT_TYPE_NODE";
                break;
            case Node.ELEMENT_NODE:
                output = "ELEMENT_NODE";
                break;
            case Node.ENTITY_NODE:
                output = "ENTITY_NODE";
                break;
            case Node.ENTITY_REFERENCE_NODE:
                output = "ENTITY_REFERENCE_NODE";
                break;
            case Node.NOTATION_NODE:
                output = "NOTATION_NODE";
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                output = "PROCESSING_INSTRUCTION_NODE";
                break;
            case Node.TEXT_NODE:
                output = "TEXT_NODE";
                break;
            default:
                output = "UNKNOWN_NODE";
                break;
        }

        return output;
    }

    /**
     * Returns an IData representation of the given NamedNodeMap.
     *
     * @param namedNodeMap  A NamedNodeMap object.
     * @return              An IData representation of the given NamedNodeMap object.
     */
    public static IData toIData(NamedNodeMap namedNodeMap) throws ServiceException {
        if (namedNodeMap == null) return null;

        IDataMap map = new IDataMap();
        int length = namedNodeMap.getLength();
        for (int i = 0; i < length; i++) {
            Node node = namedNodeMap.item(i);
            map.put(node.getNodeName(), toIData(node));
        }

        return map;
    }

    /**
     * Returns an IData[] representation of the given NamedNodeMap.
     *
     * @param namedNodeMap  A NamedNodeMap object.
     * @return              An IData[] representation of the given NamedNodeMap object.
     */
    public static IData[] toIDataArray(NamedNodeMap namedNodeMap) throws ServiceException {
        if (namedNodeMap == null) return null;

        int length = namedNodeMap.getLength();
        IData[] output = new IData[length];
        for (int i = 0; i < length; i++) {
            output[i] = toIData(namedNodeMap.item(i));
        }

        return output;
    }

    /**
     * Returns an IData[] representation of the given NodeList object.
     *
     * @param nodeList  A NodeList object.
     * @return          An IData[] representation of the given NodeList object.
     */
    public static IData[] toIDataArray(NodeList nodeList) throws ServiceException {
        return toIDataArray(nodeList, true);
    }

    /**
     * Returns an IData[] representation of the given NodeList object.
     *
     * @param nodeList  A NodeList object.
     * @param recurse   If true, child nodes will be recursed and returned also.
     * @return          An IData[] representation of the given NodeList object.
     */
    public static IData[] toIDataArray(NodeList nodeList, boolean recurse) throws ServiceException {
        if (nodeList == null) return null;

        int length = nodeList.getLength();
        IData[] output = new IData[length];

        for (int i = 0; i < length; i++) {
            output[i] = toIData(nodeList.item(i), recurse);
        }

        return output;
    }
}
