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
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A collection of convenience methods for working with XML.
 */
public class XMLHelper {
    /**
     * Disallow instantiation of this class.
     */
    private XMLHelper() {}

    /**
     * Validates the given content as XML, optionally against the given XML schema (XSD); throws an
     * exception if the content is malformed and raise is true, otherwise returns a list of errors if
     * there were any, or null if the XML is considered well-formed and valid.
     *
     * @param content           The XML content to be validated.
     * @param schema            Optional XML schema to validate against. If null, the XML will be checked for
     *                          well-formedness only.
     * @param raise             If true, and the XML is invalid, an exception will be thrown. If false, no
     *                          exception is thrown when the XML is invalid.
     * @return                  The list of validation errors if the XMl is invalid, or null if the XML is valid.
     * @throws ServiceException If an I/O error occurs.
     */
    public static String[] validate(InputStream content, InputStream schema, boolean raise) throws ServiceException {
        if (content == null) return null;

        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);

            if (schema != null) {
                factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(schema)));
            }

            SAXParser parser = factory.newSAXParser();
            DefaultErrorHandler handler = new DefaultErrorHandler();
            parser.parse(content, handler);
            errors.addAll(handler.getErrors());
        } catch (IOException ex) {
            errors.add(ex);
        } catch (ParserConfigurationException ex) {
            errors.add(ex);
        } catch (SAXParseException ex) {
            errors.add(ex);
        } catch (SAXException ex) {
            errors.add(ex);
        } finally {
            StreamHelper.close(content, schema);
        }

        if (raise && errors.size() > 0) {
            throw new BaseException(errors);
        }

        return errors.size() == 0 ? null : ExceptionHelper.getMessages(errors.toArray(new Throwable[errors.size()]));
    }

    /**
     * Validates the given content as XML, optionally against the given XML schema (XSD); throws an
     * exception if the content is malformed and raise is true, otherwise returns a list of errors if
     * there were any, or null if the XML is considered well-formed and valid.
     *
     * @param content           The XML content to be validated.
     * @param schema            Optional XML schema to validate against. If null, the XML will be checked for
     *                          well-formedness only.
     * @param raise             If true, and the XML is invalid, an exception will be thrown. If false, no
     *                          exception is thrown when the XML is invalid.
     * @return                  The list of validation errors if the XMl is invalid, or null if the XML is valid.
     * @throws ServiceException If an I/O error occurs.
     */
    public static String[] validate(byte[] content, byte[] schema, boolean raise) throws ServiceException {
        return validate(StreamHelper.normalize(content), StreamHelper.normalize(schema), raise);
    }

    /**
     * Validates the given content as XML, optionally against the given XML schema (XSD); throws an
     * exception if the content is malformed and raise is true, otherwise returns a list of errors if
     * there were any, or null if the XML is considered well-formed and valid.
     *
     * @param content           The XML content to be validated.
     * @param schema            Optional XML schema to validate against. If null, the XML will be checked for
     *                          well-formedness only.
     * @param raise             If true, and the XML is invalid, an exception will be thrown. If false, no
     *                          exception is thrown when the XML is invalid.
     * @return                  The list of validation errors if the XMl is invalid, or null if the XML is valid.
     * @throws ServiceException If an I/O error occurs.
     */
    public static String[] validate(String content, String schema, boolean raise) throws ServiceException {
        return validate(StreamHelper.normalize(content), StreamHelper.normalize(schema), raise);
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, Charset charset, XMLCanonicalizationAlgorithm algorithm) throws ServiceException {
        byte[] output = null;

        try {
            Init.init();
            input = CharsetHelper.convert(input, charset, CharsetHelper.normalize(Canonicalizer.ENCODING));
            output = Canonicalizer.getInstance(XMLCanonicalizationAlgorithm.normalize(algorithm).getID()).canonicalize(input);
        } catch(XMLSecurityException ex) {
            ExceptionHelper.raise(ex);
        } catch(ParserConfigurationException ex) {
            ExceptionHelper.raise(ex);
        } catch(SAXException ex) {
            ExceptionHelper.raise(ex);
        } catch(IOException ex) {
            ExceptionHelper.raise(ex);
        }

        return output;
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, Charset charset, String algorithm) throws ServiceException {
        return canonicalize(input, charset, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charsetName       The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, String charsetName, XMLCanonicalizationAlgorithm algorithm) throws ServiceException {
        return canonicalize(input, CharsetHelper.normalize(charsetName), algorithm);
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charsetName       The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, String charsetName, String algorithm) throws ServiceException {
        return canonicalize(input, charsetName, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, Charset charset, XMLCanonicalizationAlgorithm algorithm) throws ServiceException, IOException {
        return StreamHelper.normalize(canonicalize(BytesHelper.normalize(input), charset, algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, Charset charset, String algorithm) throws ServiceException, IOException {
        return canonicalize(input, charset, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charsetName       The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, String charsetName, XMLCanonicalizationAlgorithm algorithm) throws ServiceException, IOException {
        return canonicalize(input, CharsetHelper.normalize(charsetName), algorithm);
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charsetName       The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, String charsetName, String algorithm) throws ServiceException, IOException {
        return canonicalize(input, charsetName, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }

    /**
     * SAX parsing handler that records all errors encountered during a parse.
     */
    private static class DefaultErrorHandler extends DefaultHandler {
        private List<Throwable> errors = new ArrayList<Throwable>();

        /**
         * Constructs a new error handler.
         */
        public DefaultErrorHandler() {
            super();
        }

        /**
         * Returns the list of formatted error messages encountered while parsing XML.
         * @return The list of formatted error messages encountered while parsing XML.
         */
        public Collection<Throwable> getErrors() {
            return errors;
        }

        /**
         * Handles an XML error by appending it to the list of errors encountered while parsing.
         * @param exception     The exception to be appended to the error list.
         * @throws SAXException Not thrown by this implementation.
         */
        public void error(SAXParseException exception) throws SAXException {
            append(exception);
        }

        /**
         * Handles a fatal XML error by appending it to the list of errors encountered while parsing.
         * @param exception     The exception to be appended to the error list.
         * @throws SAXException Not thrown by this implementation.
         */
        public void fatalError(SAXParseException exception) throws SAXException {
            append(exception);
        }

        /**
         * Appends the given exception to the list of errors encountered while parsing.
         * @param exception     The exception to be appended to the error list.
         */
        protected void append(SAXParseException exception) {
            errors.add(exception);
        }
    }
}
