/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.content;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import org.apache.commons.csv.QuoteMode;
import org.w3c.dom.Node;
import permafrost.tundra.data.IDataCSVParser;
import permafrost.tundra.data.IDataExcelParser;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataHjsonParser;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.data.IDataParser;
import permafrost.tundra.data.IDataYAMLParser;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.mime.MIMEClassification;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.server.ServiceHelper;
import permafrost.tundra.xml.XMLHelper;
import javax.activation.MimeType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Parses and serializes content in the following formats: XML, Flat File, JSON, HJSON, YAML, CSV, PSV, TSV, XLS, or
 * XLSX.
 */
public class ContentParser extends IDataParser {
    public final static MimeType DEFAULT_CONTENT_TYPE = MIMETypeHelper.of("text/xml");

    /**
     * The character set to use when serializing, unless overridden in the parse or emit method call.
     */
    protected Charset charset;
    /**
     * The fully-qualified document reference or flat file schema to use when parsing data or serializing documents.
     */
    protected String schema;
    /**
     * The optional XML namespace prefixes and URIs to use when parsing or serializing XML.
     */
    protected IData namespace;
    /**
     * Whether content should be validated when parsing or serializing.
     */
    protected boolean validate;
    /**
     * Additional arbitrary inputs for the various serialization types.
     */
    protected IData pipeline;
    /**
     * The MIME media type classification, used for determining which parser to use.
     */
    protected MIMEClassification classification;

    /**
     * Constructs a new ContentParser object.
     *
     * @param contentType       The MIME media type of the data format to serialize the document in.
     * @param charset           The character set to serialize with.
     * @param schema            The fully-qualified document reference or flat file schema to use to serialize XML or
     *                          flat file content respectively.
     * @param namespace         Optional XML namespace prefixes and URIs to use when serializing to XML.
     * @param validate          Whether the content should be validated against the given schema.
     * @param pipeline          Additional arbitrary inputs for the various serialization types.
     */
    public ContentParser(MimeType contentType, Charset charset, String schema, IData namespace, boolean validate, IData pipeline) {
        super(contentType == null ? DEFAULT_CONTENT_TYPE : contentType);

        this.charset = CharsetHelper.normalize(charset, contentType, MIMETypeHelper.isText(this.contentType));
        this.schema = schema;
        this.namespace = namespace;
        this.validate = validate;
        this.pipeline = pipeline == null ? IDataFactory.create() : pipeline;

        this.classification = MIMETypeHelper.classify(this.contentType, schema);
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param content       The content to be parsed.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(Object content) throws IOException {
        return parse(content, null);
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param content       The content to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    public IData parse(Object content, Charset charset) throws IOException {
        IData document = null;

        if (content != null) {
            charset = normalize(charset);

            IData pipeline = IDataHelper.duplicate(this.pipeline);
            IDataCursor cursor = pipeline.getCursor();

            MIMEClassification classification;
            IDataParser parser;
            Node node = null;
            InputStream inputStream = null;

            try {
                if (content instanceof Node) {
                    node = (Node)content;
                    classification = MIMEClassification.XML;
                } else {
                    inputStream = InputStreamHelper.normalize(content, charset);
                    classification = this.classification;
                }

                switch (classification) {
                    case XML:
                    case UNKNOWN:
                        if (inputStream != null) {
                            IData scope = IDataFactory.create();
                            IDataCursor scopeCursor = scope.getCursor();
                            try {
                                IDataHelper.put(scopeCursor, "$filestream", inputStream);
                                if (charset != null) IDataHelper.put(scopeCursor, "encoding", charset.displayName());
                                IDataHelper.put(scopeCursor, "isXML", "true");
                                IDataHelper.put(scopeCursor, "expandDTD", IDataHelper.get(cursor, "expandDTD", String.class), false);
                                IDataHelper.put(scopeCursor, "expandGeneralEntities", IDataHelper.get(cursor, "expandGeneralEntities", String.class), false);
                                scopeCursor.destroy();

                                scope = ServiceHelper.invoke("pub.xml:xmlStringToXMLNode", scope);

                                scopeCursor = scope.getCursor();
                                node = IDataHelper.get(scopeCursor, "node", Node.class);
                                scopeCursor.destroy();
                            } finally {
                                scopeCursor.destroy();
                            }
                        }

                        IDataHelper.put(cursor, "node", node);
                        IDataHelper.put(cursor, "makeArrays", schema == null, String.class);
                        IDataHelper.put(cursor, "nsDecls", namespace, false);
                        IDataHelper.put(cursor, "documentTypeName", schema, false);
                        cursor.destroy();

                        pipeline = ServiceHelper.invoke("pub.xml:xmlNodeToDocument", pipeline);

                        cursor = pipeline.getCursor();
                        document = IDataHelper.get(cursor, "document", IData.class);
                        break;

                    case JSON:
                        parser = new IDataJSONParser();
                        document = parser.parse(inputStream);
                        break;

                    case PLAIN:
                        IDataHelper.put(cursor, "ffData", inputStream);
                        IDataHelper.put(cursor, "ffSchema", schema);
                        if (charset != null) IDataHelper.put(cursor, "encoding", charset.displayName());
                        IDataHelper.put(cursor, "keepResults", "true");
                        IDataHelper.put(cursor, "validate", validate, String.class);
                        IDataHelper.put(cursor, "returnErrors", "asArray");
                        cursor.destroy();

                        pipeline = ServiceHelper.invoke("pub.flatFile:convertToValues", pipeline);

                        cursor = pipeline.getCursor();
                        document = IDataHelper.get(cursor, "ffValues", IData.class);

                        boolean isValid = IDataHelper.getOrDefault(cursor, "isValid", Boolean.class, true);
                        IData[] errors = IDataHelper.get(cursor, "errors", IData[].class);

                        ValidationHelper.buildResult(schema, isValid, errors).raiseIfInvalid();
                        break;

                    case CSV:
                    case PSV:
                    case TSV:
                        String delimiterCharacter = IDataHelper.first(cursor, String.class, "$content.delimiter.character", "$delimiter");
                        String escapeCharacter = IDataHelper.get(cursor, "$content.escape.character", String.class);
                        String quoteCharacter = IDataHelper.get(cursor, "$content.quote.character", String.class);
                        QuoteMode quoteMode = IDataHelper.get(cursor, "$content.quote.mode", QuoteMode.class);
                        boolean hasHeader = IDataHelper.firstOrDefault(cursor, Boolean.class, true, "$content.header?", "$header?");

                        if (delimiterCharacter == null) {
                            if (classification == MIMEClassification.PSV) {
                                delimiterCharacter = "|";
                            } else if (classification == MIMEClassification.TSV) {
                                delimiterCharacter = "\t";
                            }
                        }

                        parser = new IDataCSVParser(delimiterCharacter, escapeCharacter, quoteCharacter, quoteMode, null, hasHeader, null);
                        document = parser.parse(inputStream, charset);
                        break;

                    case YAML:
                        parser = new IDataYAMLParser();
                        document = parser.parse(inputStream, charset);
                        break;

                    case XLS:
                    case XLSX:
                        parser = new IDataExcelParser(classification == MIMEClassification.XLSX);
                        document = parser.parse(inputStream, charset);
                        break;

                    case HJSON:
                        parser = new IDataHjsonParser();
                        document = parser.parse(inputStream, charset);
                        break;

                    default:
                        throw new UnsupportedOperationException("Unsupported content type cannot be parsed: " + contentType.toString());
                }

                if (validate && classification != MIMEClassification.PLAIN) {
                    ValidationHelper.validate(document, schema).raiseIfInvalid();
                }
            } catch(ServiceException ex) {
                throw new IOException(ex);
            } finally {
                cursor.destroy();
            }
        }

        return document;
    }

    /**
     * Parses the data in the given input stream, returning an IData representation.
     *
     * @param inputStream   The input stream to be parsed.
     * @param charset       The character set to use when decoding the data in the input stream.
     * @return              An IData representation of the data in the given input stream.
     * @throws IOException  If an I/O error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        return parse((Object)inputStream, charset);
    }

    /**
     * Serializes the given IData document.
     *
     * @param outputStream  The output stream the serialized IData is written to.
     * @param document      The IData document to be serialized.
     * @param charset       The character set to use when serializing the IData document.
     * @throws IOException  If an I/O error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException {
        if (document != null) {
            document = normalize(document);
            charset = normalize(charset);

            IData pipeline = IDataHelper.duplicate(this.pipeline);
            IDataCursor cursor = pipeline.getCursor();

            try {
                // do not validate flat files here, as it requires less compute to do it after serialization
                if (validate && MIMEClassification.PLAIN != classification) {
                    ValidationHelper.validate(document, schema).raiseIfInvalid();
                }

                IDataParser parser;

                switch (classification) {
                    case XML:
                    case UNKNOWN:
                        boolean encode = IDataHelper.getOrDefault(cursor, "encode", Boolean.class, true);
                        String attrPrefix = IDataHelper.get(cursor, "attrPrefix", String.class);
                        if (encode) document = XMLHelper.encode(document, attrPrefix);

                        IDataHelper.put(cursor, "document", document);
                        IDataHelper.put(cursor, "nsDecls", namespace, false);
                        IDataHelper.put(cursor, "encode", "false");
                        IDataHelper.put(cursor, "documentTypeName", schema, false);
                        cursor.destroy();

                        pipeline = ServiceHelper.invoke("pub.xml:documentToXMLString", pipeline);

                        cursor = pipeline.getCursor();
                        String xmldata = IDataHelper.get(cursor, "xmldata", String.class);
                        InputOutputHelper.copy(InputStreamHelper.normalize(xmldata, charset), outputStream);
                        break;

                    case JSON:
                        boolean minify = IDataHelper.firstOrDefault(cursor, Boolean.class, false, "$content.minify?", "$minify?");
                        parser = new IDataJSONParser(!minify);
                        parser.emit(outputStream, document, charset);
                        break;

                    case PLAIN:
                        IDataHelper.put(cursor, "ffValues", document);
                        IDataHelper.put(cursor, "ffSchema", schema);
                        IDataHelper.put(cursor, "returnAsBytes", "false");
                        cursor.destroy();

                        pipeline = ServiceHelper.invoke("pub.flatFile:convertToString", pipeline);

                        cursor = pipeline.getCursor();
                        String flatFileData = IDataHelper.get(cursor, "string", String.class);
                        InputOutputHelper.copy(InputStreamHelper.normalize(flatFileData, charset), outputStream);

                        // validate flat files after serialization for better performance because the validator uses
                        // the serialized data rather than the IData document to work
                        if (validate) {
                            ValidationHelper.validate(flatFileData, schema).raiseIfInvalid();
                        }

                        break;

                    case CSV:
                    case PSV:
                    case TSV:
                        String delimiterCharacter = IDataHelper.first(cursor, String.class, "$content.delimiter.character", "$delimiter");
                        String escapeCharacter = IDataHelper.get(cursor, "$content.escape.character", String.class);
                        String quoteCharacter = IDataHelper.get(cursor, "$content.quote.character", String.class);
                        QuoteMode quoteMode = IDataHelper.get(cursor, "$content.quote.mode", QuoteMode.class);
                        boolean hasHeader = IDataHelper.firstOrDefault(cursor, Boolean.class, true, "$content.header?", "$header?");
                        String[] columns = IDataHelper.first(cursor, String[].class, "$content.headings", "$columns");

                        if (delimiterCharacter == null) {
                            if (classification == MIMEClassification.PSV) {
                                delimiterCharacter = "|";
                            } else if (classification == MIMEClassification.TSV) {
                                delimiterCharacter = "\t";
                            }
                        }

                        parser = new IDataCSVParser(delimiterCharacter, escapeCharacter, quoteCharacter, quoteMode, null, hasHeader, columns);
                        parser.emit(outputStream, document, charset);
                        break;

                    case YAML:
                        parser = new IDataYAMLParser();
                        parser.emit(outputStream, document, charset);
                        break;

                    case XLS:
                    case XLSX:
                        parser = new IDataExcelParser(classification == MIMEClassification.XLSX);
                        parser.emit(outputStream, document, charset);
                        break;

                    case HJSON:
                        parser = new IDataHjsonParser();
                        parser.emit(outputStream, document, charset);
                        break;

                    default:
                        throw new UnsupportedOperationException("Unsupported content type cannot be parsed: " + contentType.toString());
                }
            } catch(ServiceException ex) {
                throw new IOException(ex);
            } finally {
                cursor.destroy();
            }
        }
    }

    /**
     * Returns the character set used by this parser.
     *
     * @return the character set used by this parser.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Normalizes the given IData document prior to serialization by removing "_env" and "_properties" elements if they
     * exist.
     *
     * @param document  The IData document to be normalized for serialization.
     * @return          The normalized IData document.
     */
    private IData normalize(IData document) {
        if (document != null) {
            document = IDataHelper.duplicate(document);

            IDataCursor cursor = document.getCursor();
            try {
                IDataHelper.remove(cursor, "_env");
                IDataHelper.remove(cursor, "_properties");
            } finally {
                cursor.destroy();
            }
        }

        return document;
    }

    /**
     * Normalizes the given charset.
     *
     * @param charset   The charset to normalize.
     * @return          The normalized charset.
     */
    private Charset normalize(Charset charset) {
        if (charset == null) {
            charset = this.charset;
        }
        return charset;
    }
}
