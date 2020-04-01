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

package permafrost.tundra.mime;

import javax.activation.MimeType;

/**
 * Represents a classification of a MIME media type into what kind of parser is needed for parsing or serializing
 * content of that type.
 */
public enum MIMEClassification {
    XML("text/xml", "xml"),
    PLAIN("text/plain", "txt"),
    JSON("application/json", "json"),
    CSV("text/csv", "csv"),
    TSV("text/tsv", "tsv"),
    PSV("text/psv", "psv"),
    YAML("text/yaml", "yaml"),
    XLS("application/vnd.ms-excel", "xls"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    HJSON("application/hjson", "hjson"),
    HTML("text/html", "html"),
    HTML_FORM("application/x-www-form-urlencoded", "txt"),
    UNKNOWN("application/octet-stream", "dat");

    /**
     * The default classification used by Tundra.
     */
    public static MIMEClassification DEFAULT_MIME_CLASSIFICATION = UNKNOWN;

    /**
     * The MimeType that is associated with the classification.
     */
    private MimeType associatedType;
    /**
     * The default file extension to use for this classification.
     */
    private String defaultFileExtension;

    /**
     * Constructs a new MIMEClassification object.
     *
     * @param associatedType        The MimeType associated with this classification.
     * @param defaultFileExtension  The default file extension to use for files with this classification.
     */
    MIMEClassification(String associatedType, String defaultFileExtension) {
        this(MIMETypeHelper.of(associatedType), defaultFileExtension);
    }

    /**
     * Constructs a new MIMEClassification object.
     *
     * @param associatedType        The MimeType associated with this classification.
     * @param defaultFileExtension  The default file extension to use for files with this classification.
     */
    MIMEClassification(MimeType associatedType, String defaultFileExtension) {
        this.associatedType = associatedType;
        this.defaultFileExtension = defaultFileExtension;
    }

    /**
     * Converts the given string value as an enum value.
     *
     * @param value The string to be converted.
     * @return      The enum value the given string represents.
     */
    public static MIMEClassification normalize(String value) {
        return value == null ? DEFAULT_MIME_CLASSIFICATION : valueOf(value.trim().toUpperCase());
    }

    /**
     * Returns the MimeType associated with this classification.
     *
     * @return the MimeType associated with this classification.
     */
    public MimeType getAssociatedType() {
        return associatedType;
    }

    /**
     * Returns the default file extension to use for files with this classification.
     *
     * @return the default file extension to use for files with this classification.
     */
    public String getDefaultFileExtension() {
        return defaultFileExtension;
    }
}
