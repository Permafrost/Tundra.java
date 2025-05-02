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

package permafrost.tundra.mime;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.util.Locale;

/**
 * A subclass of MimeType that preserves the specified case of the primary and sub types. Comparisons with other MIME
 * types continues to be performed as case-insensitive, however when converting this object to a string the case
 * used in the primary and sub types will be the original case specified when the object was constructed. Further, the
 * parsing of MIME types has been relaxed to accept empty or missing primary or sub types to support malformed MIME
 * types that are unfortunately used in the wild.
 */
public class CasePreservedMimeType extends MimeType {
    /**
     * The primary type with case preserved.
     */
    protected String primaryType;
    /**
     * The subtype with case preserved.
     */
    protected String subType;
    /**
     * The list of parameters.
     */
    protected MimeTypeParameterList parameters;

    /**
     * Default constructor.
     */
    public CasePreservedMimeType() {
        this("", "");
    }

    /**
     * Constructor that builds a CasePreservedMimeType with the given primary and sub type with an empty parameter list.
     *
     * @param primary                   The primary MIME type.
     * @param sub                       The MIME subtype.
     */
    public CasePreservedMimeType(String primary, String sub) {
        primaryType = primary;
        subType = sub;
        parameters = new MimeTypeParameterList();
    }

    /**
     * Constructor that builds a CasePreservedMimeType from a String.
     *
     * @param contentType             The MIME type string.
     * @throws MimeTypeParseException If the MIME type string is incorrectly formatted.
     */
    public CasePreservedMimeType(String contentType) throws MimeTypeParseException {
        parse(contentType);
    }

    /**
     * Parses the given MIME type string, using relaxed parsing that allows for missing or empty primary and sub types.
     *
     * @param data                    The MIME type string.
     * @throws MimeTypeParseException If the MIME type string is incorrectly formatted.
     */
    protected void parse(String data) throws MimeTypeParseException {
        primaryType = "";
        subType = "";
        parameters = null;

        if (data != null) {
            int indexSlash = data.indexOf('/');
            int indexSemicolon = data.indexOf(';');

            if (indexSlash < 0 && indexSemicolon < 0) {
                // "FoO"
                primaryType = data;
            } else if (indexSlash < 0) {
                // "FoO;BaZ=value" or ";BaZ=value"
                primaryType = data.substring(0, indexSemicolon);
                parameters = new MimeTypeParameterList(data.substring(indexSemicolon));
            } else if (indexSemicolon < 0) {
                // "FoO/BaR" or "/BaR"
                primaryType = data.substring(0, indexSlash);
                subType = data.substring(indexSlash + 1);
            } else if (indexSlash < indexSemicolon) {
                // "FoO/BaR;BaZ=value"
                primaryType = data.substring(0, indexSlash);
                subType = data.substring(indexSlash + 1, indexSemicolon);
                parameters = new MimeTypeParameterList(data.substring(indexSemicolon));
            } else {
                // "FoO;BaZ=a/b" or ";BaZ=a/b"
                primaryType = data.substring(0, indexSemicolon);
                parameters = new MimeTypeParameterList(data.substring(indexSemicolon));
            }
        }

        if (parameters == null) parameters = new MimeTypeParameterList();
        primaryType = primaryType.trim();
        subType = subType.trim();
    }

    /**
     * Returns the primary type of this MIME type.
     * @return The primary type of this MIME type.
     */
    @Override
    public String getPrimaryType() {
        return primaryType;
    }

    /**
     * Set the primary type for this object to the given String.
     *
     * @param     primary                The primary type.
     */
    @Override
    public void setPrimaryType(String primary) {
        primaryType = primary == null ? "" : primary;
    }

    /**
     * Returns the subtype of this MIME type.
     * @return The subtype of this MIME type.
     */
    @Override
    public String getSubType() {
        return subType;
    }

    /**
     * Set the subtype for this MIME type to the give string.
     *
     * @param     sub                    The subtype value to set.
     */
    @Override
    public void setSubType(String sub) {
        subType = sub == null ? "" : sub;
    }

    /**
     * Returns the list of parameters.
     * @return The list of parameters.
     */
    @Override
    public MimeTypeParameterList getParameters() {
        return parameters;
    }

    /**
     * Returns the value of the parameter with the given name.
     *
     * @param name	The name of the parameter to return the value of.
     * @return      The value of the parameter with the given name.
     */
    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Sets the parameter with the given name to the given value.
     *
     * @param name	The name of the parameter to be set.
     * @param value	The value to set the parameter to.
     */
    @Override
    public void setParameter(String name, String value) {
        parameters.set(name, value);
    }

    /**
     * Removes the parameter with the given name from the parameter list.
     *
     * @param name	The name of the parameter to be removed.
     */
    @Override
    public void removeParameter(String name) {
        parameters.remove(name);
    }

    /**
     * Returns the MIME type as a string.
     * @return the MIME type as a string.
     */
    @Override
    public String toString() {
        return getBaseType() + parameters.toString();
    }

    /**
     * Returns the primary and sub types separated by a slash character.
     * @return The primary and sub types separated by a slash character.
     */
    @Override
    public String getBaseType() {
        String separator = subType.isEmpty() ? "" : "/";
        return primaryType + separator + subType;
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
        return equalsIgnoreCase(primaryType, type.getPrimaryType()) && ("*".equals(subType) || "*".equals(type.getSubType()) || (equalsIgnoreCase(subType, type.getSubType())));
    }

    /**
     * Returns true if this MIME type matches the primary and sub type of the given MIME type, with string comparisons
     * performed case-insensitively.
     *
     * @param data	The MIME type string to compare to.
     * @return		True if the given type matches, otherwise false.
     */
    @Override
    public boolean match(String data) throws MimeTypeParseException {
        return match(new CasePreservedMimeType(data));
    }

    /**
     * Performs a case-insensitive match for equality between two strings using the english locale.
     *
     * @param string1   The first string.
     * @param string2   The second string.
     * @return          True if both strings are equal ignoring case differences.
     */
    protected boolean equalsIgnoreCase(String string1, String string2) {
        boolean equals;
        if (string1 == null || string2 == null) {
            equals = string1 == null && string2 == null;
        } else {
            equals = string1.toLowerCase(Locale.ENGLISH).equals(string2.toLowerCase(Locale.ENGLISH));
        }
        return equals;
    }
}
