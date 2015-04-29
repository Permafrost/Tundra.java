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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.CharsetHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IDataCSVParser extends IDataTextParser {
    protected static IDataCSVParser INSTANCE = new IDataCSVParser();

    protected char delimiter = ',';
    protected String contentType = "text/csv";

    /**
     * Construct a new IDataCSVCoder, using the default delimiter ',' and
     * default content type "text/csv".
     */
    public IDataCSVParser() {}

    /**
     * Construct a new IDataCSVCoder, using the given delimiter and
     * given content type.
     * @param delimiter     The delimiter character to use.
     * @param contentType   The content type to use.
     */
    public IDataCSVParser(char delimiter, String contentType) {
        this.delimiter = delimiter;
        this.contentType = contentType;
    }

    /**
     * Returns a default instance of this class with default settings.
     * @return The default instance of this class.
     */
    public static IDataCSVParser getInstance() {
        return INSTANCE;
    }

    /**
     * Encodes the given IData document as CSV to the given output stream.
     * @param outputStream  The stream to write the encoded IData to.
     * @param document      The IData document to be encoded.
     * @param charset       The character set to use.
     * @throws IOException  If there is a problem writing to the stream.
     */
    public void encode(OutputStream outputStream, IData document, Charset charset) throws IOException {
        StreamHelper.copy(StreamHelper.normalize(encodeToString(document), charset), outputStream);
    }

    /**
     * The MIME media type for CSV.
     * @return CSV MIME media type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns an IData representation of the CSV data in the given input stream.
     * @param inputStream                       The input stream to be decoded.
     * @param charset                           The character set to use.
     * @return                                  An IData representation of the given input stream data.
     * @throws IOException                      If there is a problem reading from the stream.
     */
    @Override
    public IData decode(InputStream inputStream, Charset charset) throws IOException {
        if (inputStream == null) return null;

        Reader reader = new InputStreamReader(inputStream, CharsetHelper.normalize(charset));
        CSVFormat format = CSVFormat.DEFAULT.withHeader().withNullString("");
        format = format.withDelimiter(delimiter);

        CSVParser parser = format.parse(reader);

        Set<String> keys = parser.getHeaderMap().keySet();
        List<IData> list = new ArrayList<IData>();

        for (CSVRecord record : parser) {
            IData document = IDataFactory.create();
            IDataCursor cursor = document.getCursor();
            for (String key : keys) {
                if (record.isSet(key)) {
                    String value = record.get(key);
                    if (value != null) IDataUtil.put(cursor, key, value);
                }
            }
            cursor.destroy();
            list.add(document);
        }

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();
        IDataUtil.put(cursor, "recordWithNoID", list.toArray(new IData[list.size()]));

        return output;
    }

    /**
     * Returns a CSV representation of the given IData object.
     * @param document  The IData to convert to CSV.
     * @return          The CSV representation of the IData.
     */
    @Override
    public String encodeToString(IData document) throws IOException {
        if (document == null) return null;

        IDataCursor cursor = document.getCursor();
        IData[] records = IDataUtil.getIDataArray(cursor, "recordWithNoID");
        cursor.destroy();

        if (records == null) return null;
        if (records.length == 0) return "";

        StringBuilder builder = new StringBuilder();
        CSVFormat format = CSVFormat.DEFAULT.withHeader(IDataHelper.getKeys(records));
        format.withDelimiter(delimiter);

        CSVPrinter printer = new CSVPrinter(builder, format);

        for (IData record : records) {
            if (record != null) printer.printRecord(IDataHelper.getValues(record));
        }

        return builder.toString();
    }
}
