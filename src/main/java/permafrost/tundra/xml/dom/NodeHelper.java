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
import com.wm.data.IDataCursor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A collection of convenience methods for working with org.w3c.dom.Node objects.
 */
public final class NodeHelper {

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

            try {
                // defend against denial of service attacks
                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch(Throwable ex) {
                // do nothing, method is not supported
            }

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
     * Returns an IData representation of the given Node.
     *
     * @param node              The Node to be parsed.
     * @return                  An IData[] representation of this object.
     */
    public static IData parse(Node node) {
        return parse(node, null);
    }

    /**
     * Returns an IData representation of the given Node.
     *
     * @param node              The Node to be parsed.
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @return                  An IData[] representation of this object.
     */
    public static IData parse(Node node, NamespaceContext namespaceContext) {
        return parse(node, namespaceContext, true);
    }

    /**
     * Returns an IData representation of the given Node.
     *
     * @param node              The Node to be parsed.
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @param recurse           If true, child elements will be recursed and returned also.
     * @return                  An IData[] representation of this object.
     */
    public static IData parse(Node node, NamespaceContext namespaceContext, boolean recurse) {
        IDataMap map = new IDataMap();
        parse(node, namespaceContext, map, recurse);
        return map;
    }

    /**
     * Creates an IData representation of the given Node in the given IDataMap.
     *
     * @param node              The Node to be parsed.
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @param output            The IDataMap in which the IData representation is created.
     * @param recurse           If true, child elements will be recursed and returned also.
     */
    private static void parse(Node node, NamespaceContext namespaceContext, IDataMap output, boolean recurse) {
        if (node == null || output == null) return;

        if (node instanceof Document) {
            Document document = (Document)node;
            Node root = document.getDocumentElement();
            IDataMap child = new IDataMap();
            parse(root, namespaceContext, child, recurse);
            output.put(getNodeName(root, namespaceContext), child, false);
        } else if (node instanceof Element) {
            Element element = (Element)node;

            boolean hasChildElements = ElementHelper.hasChildElements(element);
            boolean hasAttributes = element.hasAttributes();
            String content = ElementHelper.getTextContent(element);

            if (hasAttributes || hasChildElements) {
                if (element.hasAttributes()) {
                    NamedNodeMap attributes = element.getAttributes();
                    int length = attributes.getLength();
                    for (int i = 0; i < length; i++) {
                        parse(attributes.item(i), namespaceContext, output, recurse);
                    }
                }

                output.put("*body", content, false);

                if (recurse && hasChildElements) {
                    IDataCursor cursor = output.getCursor();
                    Nodes children = Nodes.of(element.getChildNodes());
                    for (Node childNode : children) {
                        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element childElement = (Element)childNode;
                            String name = getNodeName(childElement, namespaceContext);

                            if (childElement.hasAttributes() || ElementHelper.hasChildElements(childElement)) {
                                IDataMap childNodeMap = new IDataMap();
                                parse(childElement, namespaceContext, childNodeMap, recurse);
                                cursor.insertAfter(name, childNodeMap);
                            } else {
                                cursor.insertAfter(name, ElementHelper.getTextContent(childElement));
                            }
                        }
                    }
                    cursor.destroy();
                }
            } else {
                output.put("*body", content, false);
            }
        } else if (node instanceof Attr) {
            Attr attribute = (Attr)node;
            output.put("@" + getNodeName(attribute, namespaceContext), attribute.getValue(), false);
        } else {
            // do nothing for other node types
        }
    }

    /**
     * Returns the node name using the prefixes defined in the given namespace context rather than
     * the prefixes used in the parsed XML.
     *
     * @param node              The node to return the name of.
     * @param namespaceContext  The namespace context to use for prefixing qualified names.
     * @return                  The name of the node.
     */
    private static String getNodeName(Node node, NamespaceContext namespaceContext) {
        if (node == null) return null;

        String name = node.getNodeName();
        if (name.startsWith("xmlns:")) {
            String uri = node.getNodeValue();
            if (uri != null && namespaceContext != null) {
                String prefix = namespaceContext.getPrefix(uri);
                if (prefix != null) {
                    if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                        // correct the prefix on the namespace declaration, if using a different one when parsing
                        name = "xmlns:" + prefix;
                    }
                }
            }
        } else {
            String uri = node.getNamespaceURI();
            if (uri != null && namespaceContext != null) {
                String prefix = namespaceContext.getPrefix(uri);
                if (prefix != null) {
                    name = node.getLocalName();
                    if (!prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                        name = prefix + ":" + name;
                    }
                }
            }
        }
        return name;
    }

    /**
     * Returns an IData representation of the given Node object.
     *
     * @param node              A Node object.
     * @param namespaceContext  The namespace context to use for prefixing qualified names.
     * @return                  An IData representation of the given Node object.
     * @throws ServiceException If an error occurs.
     */
    public static IData reflect(Node node, NamespaceContext namespaceContext) throws ServiceException {
        return reflect(node, namespaceContext, false);
    }

    /**
     * Returns an IData representation of the given Node object.
     *
     * @param node              A Node object.
     * @param namespaceContext  The namespace context to use for prefixing qualified names.
     * @param recurse           If true, child nodes will be recursed and returned also.
     * @return                  An IData representation of the given Node object.
     * @throws ServiceException If an error occurs.
     */
    public static IData reflect(Node node, NamespaceContext namespaceContext, boolean recurse) throws ServiceException {
        if (node == null) return null;

        // if node is a Document, then use its root node instead
        if (node instanceof Document) node = ((Document)node).getDocumentElement();

        IDataMap map = new IDataMap();

        map.put("node", node);
        map.put("name.qualified", getNodeName(node, namespaceContext));

        String localName = node.getLocalName();
        if (localName != null) map.put("name.local", localName);

        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) {
            String prefix = node.getPrefix();
            if (prefix != null) {
                if (namespaceContext != null) prefix = namespaceContext.getPrefix(namespaceURI);
                map.put("name.prefix", prefix);
            }
            map.put("name.uri", namespaceURI);
        }

        map.put("type", nodeTypeToString(node.getNodeType()));

        String value = getValue(node);
        if (value != null) map.put("value", value);

        if (recurse && node.hasAttributes()) map.put("attributes", reflect(node.getAttributes(), namespaceContext, recurse));

        if (recurse && node.hasChildNodes()) {
            boolean hasElementChildren = false;

            Nodes children = Nodes.of(node.getChildNodes());
            List<IData> list = new ArrayList<IData>(children.size());

            for (Node child : children) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    hasElementChildren = true;
                    list.add(reflect(child, namespaceContext, recurse));
                }
            }

            if (hasElementChildren) map.put("elements", list.toArray(new IData[list.size()]));
        }

        return map;
    }

    /**
     * Returns the value of the given node. If the node is an element, the value returned is the
     * concatenated text from its text and CDATA child nodes.
     *
     * @param node  A node to return the value of.
     * @return      The value of the given node.
     */
    public static String getValue(Node node) {
        String value;
        if (node instanceof Element) {
            value = ElementHelper.getTextContent((Element) node);
        } else {
            value = node.getNodeValue();
        }

        return value;
    }

    /**
     * Returns a String representation of the given Node type.
     *
     * @param nodeType  A Node type.
     * @return          A String representation of the given Node type.
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
                throw new IllegalStateException("Unknown org.w3c.dom.Node type specified: " + nodeType);
        }

        return output;
    }

    /**
     * Returns an IData[] representation of the given NamedNodeMap.
     *
     * @param namedNodeMap      A NamedNodeMap object.
     * @param namespaceContext  The namespace context to use for prefixing qualified names.
     * @param recurse           If true, child nodes will be recursed and returned also.
     * @return                  An IData[] representation of the given NamedNodeMap object.
     * @throws ServiceException If an error occurs.
     */
    public static IData[] reflect(NamedNodeMap namedNodeMap, NamespaceContext namespaceContext, boolean recurse) throws ServiceException {
        if (namedNodeMap == null) return null;

        int length = namedNodeMap.getLength();
        IData[] output = new IData[length];
        for (int i = 0; i < length; i++) {
            output[i] = reflect(namedNodeMap.item(i), namespaceContext, recurse);
        }

        return output;
    }

    /**
     * Returns an IData[] representation of the given NodeList object.
     *
     * @param nodeList          A NodeList object.
     * @param namespaceContext  The namespace context to use for prefixing qualified names.
     * @param recurse           If true, child nodes will be recursed and returned also.
     * @return                  An IData[] representation of the given NodeList object.
     * @throws ServiceException If an error occurs.
     */
    public static IData[] reflect(NodeList nodeList, NamespaceContext namespaceContext, boolean recurse) throws ServiceException {
        if (nodeList == null) return null;

        int length = nodeList.getLength();
        IData[] output = new IData[length];

        for (int i = 0; i < length; i++) {
            output[i] = reflect(nodeList.item(i), namespaceContext, recurse);
        }

        return output;
    }
}
