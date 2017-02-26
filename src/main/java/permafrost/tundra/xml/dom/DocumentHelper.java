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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.xml.sax.InputSourceHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Collection of convenience methods for working with Document objects.
 */
public final class DocumentHelper {
    /**
     * Disallow instantiation of this class.
     */
    private DocumentHelper() {}

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputStream content) throws ServiceException {
        return parse(content, true);
    }

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @param close             If true, the given input stream will be closed after the parse is complete.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputStream content, boolean close) throws ServiceException {
        return parse(content, null, close);
    }

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @param charset           The character set that was used to encode the text data in the given stream.
     * @param close             If true, the given input stream will be closed after the parse is complete.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputStream content, Charset charset, boolean close) throws ServiceException {
        return parse(content, charset, close, null);
    }

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @param charset           The character set that was used to encode the text data in the given stream.
     * @param close             If true, the given input stream will be closed after the parse is complete.
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputStream content, Charset charset, boolean close, NamespaceContext namespaceContext) throws ServiceException {
        try {
            return parse(InputSourceHelper.normalize(content, charset), namespaceContext);
        } finally {
            if (close) CloseableHelper.close(content);
        }
    }

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputSource content) throws ServiceException {
        return parse(content, null);
    }

    /**
     * Parses the given content to an XML document.
     *
     * @param content           The XML content to parse.
     * @param namespaceContext  Any namespace declarations used in the XML content.
     * @return                  The parsed XML document.
     * @throws ServiceException If a parsing or I/O error occurs.
     */
    public static Document parse(InputSource content, NamespaceContext namespaceContext) throws ServiceException {
        if (content == null) return null;

        Document document = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(namespaceContext != null);
            factory.setExpandEntityReferences(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(content);
        } catch (ParserConfigurationException ex) {
            ExceptionHelper.raise(ex);
        } catch (SAXException ex) {
            ExceptionHelper.raise(ex);
        } catch (IOException ex) {
            ExceptionHelper.raise(ex);
        }

        return document;
    }

    /**
     * Serializes a document to a stream using the default character set.
     *
     * @param document          The document to be serialized.
     * @return                  The serialized document.
     * @throws ServiceException If an XML transformation error occurs.
     */
    public static InputStream emit(Document document) throws ServiceException {
        return emit(document, null);
    }

    /**
     * Serializes a document to a stream using the given character set.
     *
     * @param document          The document to be serialized.
     * @param charset           The character encoding to use.
     * @return                  The serialized document.
     * @throws ServiceException If an XML transformation error occurs.
     */
    public static InputStream emit(Document document, Charset charset) throws ServiceException {
        return NodeHelper.emit(document, charset);
    }
}
