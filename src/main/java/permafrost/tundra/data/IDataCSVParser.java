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

package permafrost.tundra.data;

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deserializes and serializes IData objects from and to CSV.
 */
public class IDataCSVParser extends IDataParser {
    /**
     * The default MIME media type for CSV content.
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/csv";
    /**
     * The default delimiter character used by the parser.
     */
    public static final Character DEFAULT_DELIMITER_CHARACTER = ',';
    /**
     * The default escape character used by the parser.
     */
    public static final Character DEFAULT_ESCAPE_CHARACTER = null;
    /**
     * The default quote character used by the parser.
     */
    public static final Character DEFAULT_QUOTE_CHARACTER = '"';
    /**
     * The default quote mode used by the parser.
     */
    public static final QuoteMode DEFAULT_QUOTE_MODE = QuoteMode.MINIMAL;
    /**
     * The delimiter, quote, and escape characters used by the parser.
     */
    protected Character delimiter, quote, escape;
    /**
     * The quote mode used by the parser.
     */
    protected QuoteMode quoteMode;
    /**
     * The column names to use when parsing.
     */
    protected String[] columns;
    /**
     * Whether the parsed and emitted CSVs have header rows.
     */
    protected boolean hasHeader;

    /**
     * Construct a new IDataCSVCoder, using the default delimiter ',' and default content type "text/csv".
     */
    public IDataCSVParser() {
        this(DEFAULT_DELIMITER_CHARACTER);
    }

    /**
     * Construct a new IDataCSVCoder.
     *
     * @param delimiter   The delimiter character to use.
     */
    public IDataCSVParser(Character delimiter) {
        this(delimiter, DEFAULT_CONTENT_TYPE, true, null);
    }

    /**
     * Construct a new IDataCSVCoder.
     *
     * @param delimiter   The delimiter character to use.
     */
    public IDataCSVParser(String delimiter) {
        this(delimiter, null, null, null, DEFAULT_CONTENT_TYPE, true, null);
    }

    /**
     * Construct a new IDataCSVCoder.
     *
     * @param delimiter   The delimiter character to use.
     * @param contentType The content type to use.
     * @param hasHeader   Whether to use a header row.
     * @param columns     The column names to use.
     */
    public IDataCSVParser(Character delimiter, String contentType, boolean hasHeader, String[] columns) {
        this(delimiter, DEFAULT_ESCAPE_CHARACTER, DEFAULT_QUOTE_CHARACTER, DEFAULT_QUOTE_MODE, contentType, hasHeader, columns);
    }

    /**
     * Construct a new IDataCSVCoder.
     *
     * @param delimiter   The delimiter character to use.
     * @param escape      The escape character to use.
     * @param quote       The quote character to use.
     * @param quoteMode   The quote mode to use.
     * @param contentType The content type to use.
     * @param hasHeader   Whether to use a header row.
     * @param columns     The column names to use.
     */
    public IDataCSVParser(Character delimiter, Character escape, Character quote, QuoteMode quoteMode, String contentType, boolean hasHeader, String[] columns) {
        super(contentType == null ? DEFAULT_CONTENT_TYPE : contentType);
        if (delimiter == null) throw new NullPointerException("delimiter must not be null");
        this.delimiter = delimiter;
        this.escape = escape;
        this.quote = quote;
        this.quoteMode = quoteMode == null ? QuoteMode.MINIMAL : quoteMode;
        this.hasHeader = hasHeader;
        this.columns = columns;
    }

    /**
     * Construct a new IDataCSVCoder.
     *
     * @param delimiter   The delimiter character to use.
     * @param escape      The escape character to use.
     * @param quote       The quote character to use.
     * @param quoteMode   The quote mode to use.
     * @param contentType The content type to use.
     * @param hasHeader   Whether to use a header row.
     * @param columns     The column names to use.
     */
    public IDataCSVParser(String delimiter, String escape, String quote, QuoteMode quoteMode, String contentType, boolean hasHeader, String[] columns) {
        this(toCharacter(delimiter, DEFAULT_DELIMITER_CHARACTER), toCharacter(escape, DEFAULT_ESCAPE_CHARACTER), toCharacter(quote, DEFAULT_QUOTE_CHARACTER), quoteMode, contentType, hasHeader, columns);
    }

    /**
     * Returns an IData representation of the CSV data in the given input stream.
     *
     * @param inputStream       The input stream to be decoded.
     * @param charset           The character set to use.
     * @return                  An IData representation of the given input stream data.
     * @throws IOException      If there is a problem reading from the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException, ServiceException {
        if (inputStream == null) return null;

        String[] columns = this.columns;
        IData output = IDataFactory.create();
        IDataCursor outputCursor = output.getCursor();
        Reader reader = new InputStreamReader(inputStream, CharsetHelper.normalize(charset));

        try {
            CSVParser parser = getRecordsFormatter(hasHeader, columns).parse(reader);

            Map<Integer, String> keys = flip(parser.getHeaderMap());
            List<IData> list = new ArrayList<IData>();

            for (CSVRecord record : parser) {
                IData document = IDataFactory.create();
                IDataCursor cursor = document.getCursor();

                try {
                    for (int i = 0; i < record.size(); i++) {
                        String value = record.get(i);
                        String key;

                        if (keys != null) {
                            key = keys.get(i);
                            if (key == null) key = "";
                        } else {
                            key = Integer.toString(i + 1);
                        }
                        cursor.insertAfter(key, value);
                    }
                } finally {
                    cursor.destroy();
                }

                list.add(document);
            }

            IDataUtil.put(outputCursor, "recordWithNoID", list.toArray(new IData[0]));
        } finally {
            CloseableHelper.close(reader);
            outputCursor.destroy();
        }

        return output;
    }

    /**
     * Encodes the given IData document as CSV to the given output stream.
     *
     * @param outputStream      The stream to write the encoded IData to.
     * @param document          The IData document to be encoded.
     * @param charset           The character set to use.
     * @throws IOException      If there is a problem writing to the stream.
     * @throws ServiceException If any other error occurs.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException, ServiceException {
        PrintStream printStream = new PrintStream(new BufferedOutputStream(outputStream, InputOutputHelper.DEFAULT_BUFFER_SIZE), false, CharsetHelper.normalize(charset).displayName());

        try {
            IDataCursor cursor = document.getCursor();
            Object values = IDataUtil.get(cursor, "recordWithNoID");
            cursor.destroy();

            if (values instanceof IData[]) {
                IData[] table = (IData[])values;

                String[] columns = this.columns;
                String[] keys = IDataHelper.getKeys(table);

                if (columns == null || columns.length == 0) {
                    columns = keys;
                } else if (columns.length < keys.length) {
                    List<String> columnList = new ArrayList<String>(keys.length);
                    columnList.addAll(Arrays.asList(columns));
                    columnList.addAll(Arrays.asList(keys).subList(columns.length, keys.length));
                    columns = columnList.toArray(new String[0]);
                }

                CSVPrinter printer = new CSVPrinter(printStream, getRecordsFormatter(hasHeader, columns));

                for (IData row : table) {
                    if (row != null) printer.printRecord(IDataHelper.getValues(row, keys));
                }
            } else if (values instanceof Object[][]) {
                Object[][] table = (Object[][])values;

                CSVPrinter printer = new CSVPrinter(printStream, getRecordsFormatter(hasHeader, this.columns));

                for (Object[] row : table) {
                    if (row != null) printer.printRecord(row);
                }
            } else if (values != null) {
                CSVPrinter printer = new CSVPrinter(printStream, getValuesFormatter(hasHeader));

                if (values instanceof Object[]) {
                    printer.printRecord((Object[])values);
                } else {
                    printer.printRecord(values);
                }
            }
        } finally {
            CloseableHelper.close(printStream);
        }
    }

    /**
     * Flips the given map.
     *
     * @param map The map to flip.
     * @param <K> The key class.
     * @param <V> The value class.
     * @return    The flipped map.
     */
    protected static <K, V> Map<V, K> flip(Map<K, V> map) {
        if (map == null) return null;

        Map<V, K> output = new HashMap<V, K>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            output.put(entry.getValue(), entry.getKey());
        }
        return output;
    }

    /**
     * Returns a new CSVFormatter configured with this parser's settings.
     *
     * @param hasHeader Whether there is a header record.
     * @return          A new CSVFormatter configured with this parser's settings.
     */
    protected CSVFormat getValuesFormatter(boolean hasHeader) {
        return getRecordsFormatter(hasHeader,null).withSkipHeaderRecord().withRecordSeparator("");
    }

    /**
     * Returns a new CSVFormatter configured with this parser's settings.
     *
     * @param hasHeader Whether there is a header record.
     * @param columns   The column names to use.
     * @return          A new CSV getRecordsFormatter configured with this parser's settings.
     */
    protected CSVFormat getRecordsFormatter(boolean hasHeader, String[] columns) {
        CSVFormat format;
        if (hasHeader) {
            if (columns == null) {
                format = CSVFormat.DEFAULT.withHeader();
            } else {
                format = CSVFormat.DEFAULT.withHeader(columns);
            }
        } else {
            format =  CSVFormat.DEFAULT.withHeader((String[])null);
        }
        return format.withSkipHeaderRecord(!hasHeader).withDelimiter(delimiter).withEscape(escape).withQuote(quote).withQuoteMode(quoteMode).withNullString("").withAllowMissingColumnNames();
    }

    /**
     * Returns the first character of the given String.
     *
     * @param character    The String to retrieve the delimiter character from.
     * @param defaultValue The default value returned if the given character is null or has zero length.
     * @return             The first character of the given String.
     */
    protected static Character toCharacter(String character, Character defaultValue) {
        if (character == null || character.length() == 0) return defaultValue;
        if (character.equals("$null")) return null;
        return character.charAt(0);
    }
}
