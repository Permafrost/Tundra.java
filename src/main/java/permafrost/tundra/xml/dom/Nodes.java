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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.namespace.NamespaceContext;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps an org.w3c.dom.NodeList to be java.util.List compatible.
 */
public class Nodes extends AbstractList<Node> implements NodeList {
    /**
     * The wrapped org.w3c.dom.NodeList object.
     */
    private NodeList nodeList;

    /**
     * Creates a new NodeList wrapper.
     *
     * @param nodeList  The org.w3c.dom.NodeList object to be wrapped.
     */
    public Nodes(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    /**
     * Returns the item at the given index.
     *
     * @param index The index of the item to be returned.
     * @return      The item at the given index.
     */
    public Node get(int index) {
        return item(index);
    }

    /**
     * Returns the number of items in the list.
     *
     * @return The number of items in the list.
     */
    public int getLength() {
        return nodeList.getLength();
    }

    /**
     * Returns the item at the given index.
     *
     * @param index The index of the item to be returned.
     * @return      The item at the given index.
     */
    public Node item(int index) {
        return nodeList.item(index);
    }

    /**
     * Returns the number of items in the list.
     *
     * @return The number of items in the list.
     */
    public int size() {
        return getLength();
    }

    /**
     * Returns the text contents for each node in the list.
     *
     * @return  The list of text content returned by each node.getTextContent().
     */
    public List<String> getTextContents() {
        List<String> contents = new ArrayList<String>(size());
        for (Node node: this) {
            contents.add(node.getTextContent());
        }
        return contents;
    }

    /**
     * Returns an IData[] representation of this object.
     *
     * @return An IData[] representation of this object.
     */
    public IData[] reflect() throws ServiceException {
        return reflect(false);
    }

    /**
     * Returns an IData[] representation of this object.
     *
     * @param recurse   If true, child nodes will be recursed and returned also.
     * @return          An IData[] representation of this object.
     */
    public IData[] reflect(boolean recurse) throws ServiceException {
        return NodeHelper.reflect(this, recurse);
    }

    /**
     * Returns an IData[] representation of this object.
     *
     * @return                  An IData[] representation of this object.
     */
    public IData[] parse() {
        return parse(null);
    }

    /**
     * Returns an IData[] representation of this object.
     *
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @return                  An IData[] representation of this object.
     */
    public IData[] parse(NamespaceContext namespaceContext) {
        IData[] output = new IData[size()];
        for (int i = 0; i < size(); i++) {
            output[i] = NodeHelper.parse(get(i), namespaceContext);
        }
        return output;
    }

    /**
     * Wraps the given org.w3c.dom.NodeList object.
     *
     * @param nodeList  The org.w3c.dom.NodeList object to be wrapped.
     * @return          The wrapped object.
     */
    public static Nodes of(NodeList nodeList) {
        Nodes output = null;

        if (nodeList instanceof Nodes) {
            output = (Nodes)nodeList;
        } else if (nodeList != null) {
            output = new Nodes(nodeList);
        }

        return output;
    }
}
