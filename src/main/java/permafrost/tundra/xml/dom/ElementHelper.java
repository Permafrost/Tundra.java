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
    public static String getContent(Element element) {
        String output = null;

        if (element != null && element.hasChildNodes()) {
            boolean hasContent = false;

            StringBuilder builder = new StringBuilder();

            for (Node child : Nodes.of(element.getChildNodes())) {
                switch(child.getNodeType()) {
                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                        hasContent = true;
                        builder.append(child.getNodeValue());
                        break;
                    default:
                        // for non-text children, do nothing
                        break;
                }
            }

            if (hasContent) output = builder.toString();
        }

        return output;
    }
}
