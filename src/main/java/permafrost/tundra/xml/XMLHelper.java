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

package permafrost.tundra.xml;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import org.unbescape.xml.XmlEscape;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import permafrost.tundra.content.MalformedException;
import permafrost.tundra.content.ValidationException;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.xml.Decoder;
import permafrost.tundra.data.transform.xml.Encoder;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.xml.sax.InputSourceHelper;
import permafrost.tundra.xml.stream.StreamSourceHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

/**
 * A collection of convenience methods for working with XML.
 */
public final class XMLHelper {
    /**
     * The default prefix used to denote XML attributes in parsed IData documents.
     */
    public static final String DEFAULT_ATTRIBUTE_PREFIX = "@";

    /**
     * Disallow instantiation of this class.
     */
    private XMLHelper() {}

    /**
     * Returns the given input string with XML entities escaped.
     *
     * @param input         The input string to process.
     * @return              The escaped string.
     */
    public static String encode(String input) {
        return encode(input, false);
    }

    /**
     * Returns the given input string with XML entities escaped.
     *
     * @param input         The input string to process.
     * @param isAttribute   Whether the string is an XML attribute's value.
     * @return              The escaped string.
     */
    public static String encode(String input, boolean isAttribute) {
        if (input == null) return null;

        String output;
        if (isAttribute) {
            output = XmlEscape.escapeXml11Attribute(input);
        } else {
            output = XmlEscape.escapeXml11(input);
        }
        return output;
    }

    /**
     * Returns the given string array with XML entities escaped.
     *
     * @param input         The input array to process.
     * @param isAttribute   Whether the string is an XML attribute's value.
     * @return              A new output array containing the escaped strings.
     */
    public static String[] encode(String[] input, boolean isAttribute) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = encode(input[i], isAttribute);
        }

        return output;
    }

    /**
     * XML decodes string values in the given IData document.
     *
     * @param document  The IData document to encode.
     * @return          A new IData document with string values encoded.
     */
    public static IData encode(IData document) {
        return encode(document, DEFAULT_ATTRIBUTE_PREFIX);
    }

    /**
     * XML decodes string values in the given IData document.
     *
     * @param document          The IData document to encode.
     * @param attributePrefix   The prefix used to indicate when a key relates to an XML attribute.
     * @return                  A new IData document with string values encoded.
     */
    public static IData encode(IData document, String attributePrefix) {
        return Transformer.transform(document, new Encoder(attributePrefix));
    }

    /**
     * Returns the given string with XML entities unescaped.
     *
     * @param input The input string to process.
     * @return      The unescaped string.
     */
    public static String decode(String input) {
        return XmlEscape.unescapeXml(input);
    }

    /**
     * Returns the given string array with XML entities unescaped.
     *
     * @param input The input array to process.
     * @return      A new output array containing the unescaped strings.
     */
    public static String[] decode(String[] input) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = decode(input[i]);
        }

        return output;
    }

    /**
     * XML decodes string values in the given IData document.
     *
     * @param document  The IData document to decode.
     * @return          A new IData document with string values decoded.
     */
    public static IData decode(IData document) {
        return Transformer.transform(document, new Decoder());
    }

    /**
     * Validates the given content as XML, optionally against the given XML schema (XSD); throws an exception if the
     * content is malformed and raise is true, otherwise returns a list of errors if there were any, or null if the XML
     * is considered well-formed and valid.
     *
     * @param content           The XML content to be validated.
     * @param contentCharset    The character set used to encode the XML content.
     * @param schema            Optional XML schema to validate against. If null, the XML will be checked for
     *                          well-formedness only.
     * @param schemaCharset     The character set used to encode the XML schema.
     * @param raise             If true, and the XML is invalid, an exception will be thrown. If false, no exception is
     *                          thrown when the XML is invalid.
     * @return                  The list of validation errors if the XMl is invalid, or null if the XML is valid.
     * @throws ServiceException If an I/O error occurs.
     */
    public static String[] validate(InputStream content, Charset contentCharset, InputStream schema, Charset schemaCharset, boolean raise) throws ServiceException {
        if (content == null) return null;

        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(true);

            if (schema != null) {
                factory.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(StreamSourceHelper.normalize(schema, schemaCharset)));
            }

            SAXParser parser = factory.newSAXParser();
            DefaultErrorHandler handler = new DefaultErrorHandler();
            parser.parse(InputSourceHelper.normalize(content, contentCharset), handler);
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
            CloseableHelper.close(content, schema);
        }

        if (raise && errors.size() > 0) {
            if (schema == null) {
                throw new MalformedException(errors);
            } else {
                throw new ValidationException(errors);
            }

        }

        return errors.size() == 0 ? null : ExceptionHelper.getMessages(errors.toArray(new Throwable[0]));
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
         *
         * @return The list of formatted error messages encountered while parsing XML.
         */
        public Collection<Throwable> getErrors() {
            return errors;
        }

        /**
         * Handles an XML error by appending it to the list of errors encountered while parsing.
         *
         * @param exception The exception to be appended to the error list.
         * @throws SAXException Not thrown by this implementation.
         */
        public void error(SAXParseException exception) throws SAXException {
            append(exception);
        }

        /**
         * Handles a fatal XML error by appending it to the list of errors encountered while parsing.
         *
         * @param exception The exception to be appended to the error list.
         * @throws SAXException Not thrown by this implementation.
         */
        public void fatalError(SAXParseException exception) throws SAXException {
            append(exception);
        }

        /**
         * Appends the given exception to the list of errors encountered while parsing.
         *
         * @param exception The exception to be appended to the error list.
         */
        protected void append(SAXParseException exception) {
            errors.add(exception);
        }
    }

}
