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

package permafrost.tundra.xml.xpath;

import com.wm.app.b2b.server.ServiceException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.xml.dom.Nodes;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * A collection of convenience methods for working with XPath.
 */
public class XPathHelper {
    /**
     * Disallow instantiation of this class.
     */
    private XPathHelper() {}

    /**
     * Evaluates if the given XPathExpression resolves against the given Node with the given content.
     *
     * @param context           The XML content to select from.
     * @param expression        The XPath expression used to select from the given Node.
     * @return                  True if the XPath expression resolved against the given Node has the expected content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean evaluate(Node context, XPathExpression expression, String ... expectedContent) throws ServiceException {
        return evaluate(context, expression, expectedContent == null ? null : Arrays.asList(expectedContent));
    }

    /**
     * Evaluates if the given XPathExpression resolves against the given Node with the given content.
     *
     * @param context           The XML content to select from.
     * @param expression        The XPath expression used to select from the given Node.
     * @return                  True if the XPath expression resolved against the given Node has the expected content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean evaluate(Node context, XPathExpression expression, List<String> expectedContent) throws ServiceException {
        boolean result = false;

        if (context != null && expression != null) {
            Nodes nodes = get(context, expression);

            if (nodes == null) {
                result = expectedContent == null;
            } else {
                List<String> actualContents = nodes.getTextContents();
                result = actualContents.equals(expectedContent);
            }
        }

        return result;
    }

    /**
     * Returns true if the given XPath expression can be found in the given XML content.
     *
     * @param context           The content to query.
     * @param expression        The XPath expression to query against the give content.
     * @return                  True if the given XPath expression is found in the given XML content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean exists(Node context, XPathExpression expression) throws ServiceException {
        Nodes nodes = get(context, expression);
        return nodes != null;
    }

    /**
     * Returns the Nodes selected from the given Document by the given XPathExpression.
     *
     * @param context           The XML content to select from.
     * @param expression        The XPath expression used to select from the given Node.
     * @return                  The Nodes list containing all the nodes selected from the given Node.
     * @throws ServiceException If a parsing error occurs.
     */
    public static Nodes get(Node context, XPathExpression expression) throws ServiceException {
        if (context == null || expression == null) return null;

        Nodes nodes = null;

        try {
            nodes = Nodes.of((NodeList)expression.evaluate(context, XPathConstants.NODESET));
            if (nodes != null && nodes.getLength() == 0) nodes = null; // normalize no results to null
        } catch (XPathExpressionException ex) {
            ExceptionHelper.raise(ex);
        }

        return nodes;
    }

    /**
     * Returns a compiled XPath expression.
     *
     * @param expression        The XPath expression as a String.
     * @return                  The compiled XPathExpression.
     * @throws ServiceException If a parsing error occurs.
     */
    public static XPathExpression compile(String expression) throws ServiceException {
        return compile(expression, (NamespaceContext)null);
    }

    /**
     * Returns a compiled XPath expression.
     *
     * @param expression        The XPath expression as a String.
     * @param namespaceContext  The namespace context used by the XPath expression.
     * @return                  The compiled XPathExpression.
     * @throws ServiceException If a parsing error occurs.
     */
    public static XPathExpression compile(String expression, NamespaceContext namespaceContext) throws ServiceException {
        XPathExpression compiledExpression = null;

        try {
            XPath compiler = XPathFactory.newInstance().newXPath();
            if (namespaceContext != null) compiler.setNamespaceContext(namespaceContext);
            compiledExpression = compiler.compile(expression);
        } catch (XPathExpressionException ex) {
            ExceptionHelper.raise(ex);
        }

        return compiledExpression;
    }
}
