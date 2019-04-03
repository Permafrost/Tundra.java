/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Lachlan Dowding
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import permafrost.tundra.lang.ObjectHelper;
import permafrost.tundra.math.BigDecimalHelper;
import permafrost.tundra.time.DateTimeHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Deserializes and serializes IData objects from and to Microsoft Excel spreadsheets.
 */
public class IDataExcelParser extends IDataParser {
    /**
     * The MIME media type for XLS files.
     */
    public static final String XLS_MIME_TYPE = "application/vnd.ms-excel";
    /**
     * The MIME media type for XLSX files.
     */
    public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    /**
     * The default Excel datetime pattern used.
     */
    protected static final String DEFAULT_EXCEL_DATETIME_PATTERN = "yyyy-mm-dd hh:mm:ss";
    /**
     * The default Excel date pattern used.
     */
    protected static final String DEFAULT_EXCEL_DATE_PATTERN = "yyyy-mm-dd";
    /**
     * The default Excel time pattern used.
     */
    protected static final String DEFAULT_EXCEL_TIME_PATTERN = "hh:mm:ss";

    /**
     * Whether this parser emits XLSX or XLS.
     */
    protected boolean emitXSLX;

    /**
     * Constructs a new parser.
     */
    public IDataExcelParser() {
        this(true);
    }

    /**
     * Constructs a new parser.
     *
     * @param emitXSLX If true, emits XLSX formatted files, otherwise XLS formatted.
     */
    public IDataExcelParser(boolean emitXSLX) {
        super(emitXSLX ? XLSX_MIME_TYPE : XLS_MIME_TYPE);
        this.emitXSLX = emitXSLX;
    }

    /**
     * Encodes the given IData document as Microsoft Excel spreadsheet to the given output stream.
     *
     * @param outputStream The stream to write the encoded IData to.
     * @param document     The IData document to be encoded.
     * @param charset      The character set to use.
     * @throws IOException If there is a problem writing to the stream.
     */
    @Override
    public void emit(OutputStream outputStream, IData document, Charset charset) throws IOException {
        IDataCursor cursor = document.getCursor();
        Workbook workbook = emitXSLX ? new XSSFWorkbook() : new HSSFWorkbook();

        try {
            while(cursor.next()) {
                String key = cursor.getKey();
                Object value = cursor.getValue();

                if (value instanceof IData[]) {
                    IData[] table = (IData[])value;

                    Sheet sheet = workbook.createSheet(key);
                    for (int i = 0; i < table.length; i++) {
                        if (table[i] != null) {
                            Row row = sheet.createRow(i);
                            IDataCursor rowCursor = table[i].getCursor();

                            try {
                                int index = 0;
                                while(rowCursor.next()) {
                                    setValue(row.createCell(index), rowCursor.getValue());
                                    index++;
                                }
                            } finally {
                                rowCursor.destroy();
                            }
                        }
                    }
                }
            }
        } finally {
            cursor.destroy();
        }

        workbook.write(outputStream);
    }

    /**
     * Returns an IData representation of the Microsoft Excel spreadsheet data in the given input stream.
     *
     * @param inputStream   The input stream to be decoded.
     * @param charset       The character set to use.
     * @return              An IData representation of the given input stream data.
     * @throws IOException  If there is a problem reading from the stream.
     */
    @Override
    public IData parse(InputStream inputStream, Charset charset) throws IOException {
        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            int numberOfSheets = workbook.getNumberOfSheets();

            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                int numberOfRows = sheet.getLastRowNum();

                List<IData> outputRows = new ArrayList<IData>(numberOfRows);

                if (numberOfRows > 0) {
                    for (int j = 0; j < numberOfRows + 1; j++) {
                        IData outputRow = IDataFactory.create();
                        IDataCursor rowCursor = outputRow.getCursor();

                        try {
                            Row row = sheet.getRow(j);
                            if (row != null) {
                                int numberOfCells = row.getLastCellNum();

                                for (int k = 0; k < numberOfCells; k++) {
                                    Cell cell = row.getCell(k);
                                    String key = getKey(k);
                                    Object value = getValue(cell, evaluator);

                                    rowCursor.insertAfter(key, value);
                                }
                            }

                            outputRows.add(outputRow);
                        } finally {
                            rowCursor.destroy();
                        }
                    }
                }

                cursor.insertAfter(sheet.getSheetName(), outputRows.toArray(new IData[0]));
            }
        } catch(InvalidFormatException ex) {
            throw new RuntimeException(ex);
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Returns an Excel-style column name for a given zero-based index.
     *
     * @param  index Zero-based column index.
     * @return       The Excel-style column name for the given index
     */
    protected static String getKey(int index) {
        if (index < 0) throw new IllegalArgumentException("Microsoft Excel spreadsheet column index must be >= 0");

        StringBuilder builder = new StringBuilder();

        index = index + 1;

        while(index > 0) {
            int remainder = (index - 1) % 26;
            builder.append((char)('A' + remainder));

            index = (index - remainder) / 26;
        }

        return builder.reverse().toString();
    }

    /**
     * Returns the value of the given cell.
     *
     * @param cell      The cell whose value is to be returned.
     * @param evaluator The formula evaluator to use to evaluate cells containing formulas.
     * @return          The value of the given cell.
     */
    protected static Object getValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        Object value;

        if (evaluator != null) evaluator.evaluateInCell(cell);
        CellType cellType = cell.getCellTypeEnum();

        switch (cellType) {
            case BLANK:
            case STRING:
                value = cell.getStringCellValue();
                break;
            case BOOLEAN:
                value = cell.getBooleanCellValue();
                break;
            case NUMERIC:
                double number = cell.getNumericCellValue();
                if (isDateTime(cell)) {
                    value = DateUtil.getJavaDate(number);
                } else {
                    value = number;
                }
                break;
            case FORMULA:
                value = "=" + cell.getCellFormula();
                break;
            case ERROR:
                value = cell.getErrorCellValue();
                break;
            default:
                value = null;
        }

        return value;
    }

    /**
     * Sets the given cell to have the given value with the most appropriate cell type.
     *
     * @param cell  The cell whose value is to be set.
     * @param value The value to set.
     */
    protected static void setValue(Cell cell, Object value) {
        if (cell != null) {
            if (value instanceof String) {
                String string = (String)value;
                if (string.length() > 1 && string.startsWith("\"") && string.endsWith("\"")) {
                    cell.setCellValue(string.substring(1, string.length() - 1));
                } else if (string.startsWith("=")) {
                    cell.setCellFormula(string.substring(1));
                } else {
                    try {
                        cell.setCellValue(BigDecimalHelper.parse(string).doubleValue());
                    } catch(NumberFormatException ex) {
                        if (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("false")) {
                            cell.setCellValue(Boolean.valueOf(string));
                        } else {
                            if (!setDateTime(cell, string)) {
                                cell.setCellValue(string);
                            }
                        }
                    }
                }
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean)value);
            } else if (value instanceof Number) {
                cell.setCellValue(((Number)value).doubleValue());
            } else if (value instanceof Calendar) {
                cell.setCellValue((Calendar)value);
                setDataFormat(cell, DEFAULT_EXCEL_DATETIME_PATTERN);
            } else if (value instanceof Date) {
                cell.setCellValue((Date)value);
                setDataFormat(cell, DEFAULT_EXCEL_DATETIME_PATTERN);
            } else {
                cell.setCellValue(ObjectHelper.stringify(value));
            }
        }
    }

    /**
     * Attempt to parse the given value as a datetime, and then set the cell value to the resulting Calendar object.
     *
     * @param cell  The cell whose value is to be set.
     * @param value A string value that might be a datetime.
     * @return      True if the string value was able to be parsed as a datetime and the cell value was set to the
     *              parsed Calendar object, otherwise false where the string value could not be parsed as a datetime\
     *              and the cell value was not set.
     */
    protected static boolean setDateTime(Cell cell, String value) {
        try {
            cell.setCellValue(DateTimeHelper.parse(value, "datetime"));
            if (value.matches("^\\d\\d\\d\\d-\\d\\d-\\d\\d.*$")) {
                if (value.contains("T")) {
                    setDataFormat(cell, DEFAULT_EXCEL_DATETIME_PATTERN);
                } else {
                    setDataFormat(cell, DEFAULT_EXCEL_DATE_PATTERN);
                }
            } else {
                setDataFormat(cell, DEFAULT_EXCEL_TIME_PATTERN);
            }
        } catch(IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Sets the format or pattern used to display the cell value in the spreadsheet.
     *
     * @param cell   The cell whose format is to be set.
     * @param format The format to set.
     */
    protected static void setDataFormat(Cell cell, String format) {
        Workbook workbook = cell.getRow().getSheet().getWorkbook();
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(format));
        cell.setCellStyle(style);
    }

    /**
     * Returns true if the given cell is formatted as a date and/or time. Supports internal and custom formats.
     *
     * @param cell    The cell to check.
     * @return        True if the given cell is formatted as a date and/or time.
     */
    protected static boolean isDateTime(Cell cell) {
        boolean isDateTime = DateUtil.isCellDateFormatted(cell);

        if (!isDateTime) {
            DataFormat formats = cell.getRow().getSheet().getWorkbook().createDataFormat();
            String format = formats.getFormat(cell.getCellStyle().getDataFormat()).replaceAll("General|\\[Red\\]", "");

            isDateTime = format.contains("y") || format.contains("m") || format.contains("d") ||
                         format.contains("h") || format.contains("s") || format.contains("AM/PM") ||
                         format.contains("A/P");
        }

        return isDateTime;
    }
}