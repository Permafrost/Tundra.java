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

package permafrost.tundra.xml;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.xml.namespace.IDataNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * A collection of convenience methods for working with XPath.
 */
public class XPathHelper {
    /**
     * Disallow instantiation of this class.
     */
    private XPathHelper() {}

    /**
     * Returns true if the given XPath expression can be found in the given XML content.
     * @param content           The content to query.
     * @param expression        The XPath expression to query against the give content.
     * @param namespaceContext  Any namespace declarations required to query the given content.
     * @return                  True if the given XPath expression is found in the given XML content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean exists(InputStream content, String expression, IData namespaceContext) throws ServiceException {
        return exists(content, expression, new IDataNamespaceContext(namespaceContext));
    }

    /**
     * Returns true if the given XPath expression can be found in the given XML content.
     * @param content           The content to query.
     * @param expression        The XPath expression to query against the give content.
     * @param namespaceContext  Any namespace declarations required to query the given content.
     * @return                  True if the given XPath expression is found in the given XML content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean exists(InputStream content, String expression, NamespaceContext namespaceContext) throws ServiceException {
        boolean result = false;

        try {
            result = exists(new InputSource(content), expression, namespaceContext);
        } finally {
            StreamHelper.close(content);
        }

        return result;
    }

    /**
     * Returns true if the given XPath expression can be found in the given XML content.
     * @param content           The content to query.
     * @param expression        The XPath expression to query against the give content.
     * @param namespaceContext  Any namespace declarations required to query the given content.
     * @return                  True if the given XPath expression is found in the given XML content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean exists(InputSource content, String expression, NamespaceContext namespaceContext) throws ServiceException {
        boolean result = false;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(namespaceContext != null);
            DocumentBuilder parser = factory.newDocumentBuilder();
            result = exists(parser.parse(content), expression, namespaceContext);
        } catch(ParserConfigurationException ex) {
            ExceptionHelper.raise(ex);
        } catch(SAXException ex) {
            ExceptionHelper.raise(ex);
        } catch(IOException ex) {
            ExceptionHelper.raise(ex);
        }

        return result;
    }

    /**
     * Returns true if the given XPath expression can be found in the given XML content.
     * @param content           The content to query.
     * @param expression        The XPath expression to query against the give content.
     * @param namespaceContext  Any namespace declarations required to query the given content.
     * @return                  True if the given XPath expression is found in the given XML content.
     * @throws ServiceException If a parsing error occurs.
     */
    public static boolean exists(Document content, String expression, NamespaceContext namespaceContext) throws ServiceException {
        boolean result = false;

        try {
            XPath evaluator = XPathFactory.newInstance().newXPath();
            if (namespaceContext != null) evaluator.setNamespaceContext(namespaceContext);
            result = (Boolean)evaluator.evaluate(expression, content, XPathConstants.BOOLEAN);
        } catch(XPathExpressionException ex) {
            ExceptionHelper.raise(ex);
        }

        return result;
    }

}
