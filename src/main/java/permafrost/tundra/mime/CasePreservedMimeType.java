/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Lachlan Dowding
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
import javax.activation.MimeTypeParseException;

/**
 * A subclass of MimeType that preserves the specified case of the primary and sub types. Comparisons with other MIME
 * types continues to be performed as case-insensitive, however when converting this object to a string the case
 * used in the primary and sub types will be the original case specified when the object was constructed.
 */
public class CasePreservedMimeType extends MimeType {
    /**
     * The primary type with case preserved.
     */
    protected String casePreservedPrimaryType;
    /**
     * The sub type with case preserved.
     */
    protected String casePreservedSubType;

    /**
     * Default constructor.
     */
    public CasePreservedMimeType() {
        super();
    }

    /**
     * Constructor that builds a CasePreservedMimeType from a String.
     *
     * @param contentType             The MIME type string.
     * @throws MimeTypeParseException If the MIME type string is incorrectly formatted.
     */
    public CasePreservedMimeType(String contentType) throws MimeTypeParseException {
        super(contentType);
        casePreservedParse(contentType);
    }

    /**
     * Constructor that builds a CasePreservedMimeType with the given primary and sub type but has an empty parameter list.
     *
     * @param primary                   The primary MIME type.
     * @param sub                       The MIME sub-type.
     * @throws MimeTypeParseException   If the primary type or subtype is not a valid token.
     */
    public CasePreservedMimeType(String primary, String sub) throws MimeTypeParseException {
        super(primary, sub);
        casePreservedPrimaryType = primary;
        casePreservedSubType = sub;
    }

    /**
     * Parses the primary and sub types while preserving their case.
     */
    private void casePreservedParse(String data) throws MimeTypeParseException {
        int slashIndex = data.indexOf('/');
        int semicolonIndex = data.indexOf(';');
        if ((slashIndex < 0) || (semicolonIndex >= 0 && slashIndex > semicolonIndex)) {
            throw new MimeTypeParseException("Invalid MIME type: " + data);
        } else {
            casePreservedPrimaryType = data.substring(0, slashIndex).trim();
            if (semicolonIndex < 0) {
                casePreservedSubType = data.substring(slashIndex + 1).trim();
            } else {
                casePreservedSubType = data.substring(slashIndex + 1, semicolonIndex).trim();
            }
        }
    }

    /**
     * Returns the primary type of this MIME type.
     * @return The primary type of this MIME type.
     */
    @Override
    public String getPrimaryType() {
        return casePreservedPrimaryType;
    }

    /**
     * Set the primary type for this object to the given String.
     *
     * @param     primary                The primary type.
     * @exception MimeTypeParseException If the primary type is invalid.
     */
    @Override
    public void setPrimaryType(String primary) throws MimeTypeParseException {
        super.setPrimaryType(primary);
        casePreservedPrimaryType = primary;
    }

    /**
     * Returns the subtype of this MIME type.
     * @return The subtype of this MIME type.
     */
    @Override
    public String getSubType() {
        return casePreservedSubType;
    }

    /**
     * Set the subtype for this MIME type to the give string.
     *
     * @param     sub                    The subtype value to set.
     * @exception MimeTypeParseException If the subtype is invalid.
     */
    @Override
    public void setSubType(String sub) throws MimeTypeParseException {
        super.setSubType(sub);
        casePreservedSubType = sub;
    }

    /**
     * Returns the primary and sub types separated by a slash character.
     * @return The primary and sub types separated by a slash character.
     */
    @Override
    public String getBaseType() {
        return casePreservedPrimaryType + "/" + casePreservedSubType;
    }

    /**
     * Returns true if this MIME type matches the primary and sub type of the given MIME type, with string comparisons
     * performed case-insensitively.
     *
     * @param type	The MIME type to compare to.
     * @return		True if the given type matches, otherwise false.
     */
    @Override
    public boolean match(MimeType type) {
        return getPrimaryType().equalsIgnoreCase(type.getPrimaryType()) && (getSubType().equals("*") || type.getSubType().equals("*") || (getSubType().equalsIgnoreCase(type.getSubType())));
    }
}
