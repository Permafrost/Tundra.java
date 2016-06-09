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

package permafrost.tundra.xml.dom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A collection of convenience methods for working with org.w3c.dom.Element objects.
 */
public class ElementHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ElementHelper() {}

    /**
     * Returns the concatenated content of the given Element from its TEXT_NODE and CDATA_SECTION_NODE children.
     *
     * @param element   The element to return the content of.
     * @return          The concatenated text content from the given element's TEXT_NODE and CDATA_SECTION_NODE
     *                  children.
     */
    public static String getTextContent(Element element) {
        if (element == null) return null;

        StringBuilder builder = new StringBuilder();
        boolean hasTextContent = false, hasMixedContent = false;

        for (Node child : Nodes.of(element.getChildNodes())) {
            switch(child.getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    hasTextContent = true;
                    builder.append(child.getNodeValue());
                    break;
                case Node.ELEMENT_NODE:
                    hasMixedContent = true;
                    break;
                default:
                    // for non-text children, do nothing
                    break;
            }
        }

        String content = builder.toString();
        if (!hasTextContent || (hasMixedContent && content.trim().equals(""))) content = null;

        return content;
    }

    /**
     * Return true if the given element has any children which are also elements.
     *
     * @param element   The element to check for element children.
     * @return          True if the given element has children which are also elements.
     */
    public static boolean hasChildElements(Element element) {
        boolean hasChildElements = false;

        if (element != null) {
            if (element.hasChildNodes()) {
                Nodes nodes = Nodes.of(element.getChildNodes());
                for (Node node : nodes) {
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        hasChildElements = true;
                        break;
                    }
                }
            }
        }

        return hasChildElements;
    }
}
