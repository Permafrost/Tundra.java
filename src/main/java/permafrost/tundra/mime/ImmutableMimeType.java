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

package permafrost.tundra.mime;

import java.io.IOException;
import java.io.ObjectInput;
import java.util.ArrayList;
import java.util.Collection;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * A subclass of MimeType that is immutable and supports comparisons and equality checks based on MimeType.match, which
 * allows this class to be used as keys in a Map.
 */
public class ImmutableMimeType extends MimeType implements Comparable<MimeType> {
    /**
     * Default constructor.
     */
    public ImmutableMimeType() {
        super();
    }

    /**
     * Constructor that builds a nImmutableMimeType from a String.
     *
     * @param rawData                 The MIME type string.
     * @throws MimeTypeParseException If the MIME type string is incorrectly formatted.
     */
    public ImmutableMimeType(String rawData) throws MimeTypeParseException {
        super(rawData);
    }

    /**
     * Constructor that builds an ImmutableMimeType with the given primary and sub type but has an empty parameter list.
     *
     * @param primary                   The primary MIME type.
     * @param sub                       The MIME sub-type.
     * @throws MimeTypeParseException   If the primary type or subtype is not a valid token.
     */
    public ImmutableMimeType(String primary, String sub) throws MimeTypeParseException {
        super(primary, sub);
    }

    /**
     * Constructor that builds an ImmutableMimeType given an existing MimeType object.
     *
     * @param mimeType                  The MimeType object to clone.
     * @throws MimeTypeParseException   If the MimeType object returns an incorrectly formatted MIME type string.
     */
    public ImmutableMimeType(MimeType mimeType) throws MimeTypeParseException {
        super(mimeType.toString());
    }

    /**
     * Compares this object with another MimeType object.
     *
     * @param other The other MimeType object to compare with.
     * @return      -1 if this MimeType is less than the other, 0 if they are equal, and 1 if this MimeType is
     *              greater than the other.
     */
    public int compareTo(MimeType other) {
        if (other == null) return 1;

        int result = this.getPrimaryType().compareTo(other.getPrimaryType());

        if (result == 0) {
            if (!(this.getSubType().equals("*") || other.getSubType().equals("*") || this.getSubType().equals(other.getSubType()))) {
                result = this.getSubType().compareTo(other.getSubType());
            }
        }

        return result;
    }

    /**
     * Returns true if the other MimeType is a match for this MimeType.
     *
     * @param other The other MimeType to compare equality with.
     * @return      True if the other MimeType is a match for this MimeType.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof MimeType && match((MimeType)other);
    }

    /**
     * This method is not implemented by this class.
     *
     * @param in                             Not used.
     * @throws IOException                   Never thrown.
     * @throws ClassNotFoundException        Never thrown.
     * @throws UnsupportedOperationException Always thrown as this method is not implemented.
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException("ImmutableMimeType.readExternal is deliberately not implemented to prevent mutation");
    }

    /**
     * This method is not implemented by this class.
     *
     * @param name                           Not used.
     * @throws UnsupportedOperationException Always thrown as this method is not implemented.
     */
    @Override
    public void removeParameter(String name) {
        throw new UnsupportedOperationException("ImmutableMimeType.removeParameter is deliberately not implemented to prevent mutation");
    }

    /**
     * This method is not implemented by this class.
     *
     * @param name                           Not used.
     * @param value                          Not used
     * @throws UnsupportedOperationException Always thrown as this method is not implemented.
     */
    @Override
    public void setParameter(String name, String value) {
        throw new UnsupportedOperationException("ImmutableMimeType.setParameter is deliberately not implemented to prevent mutation");
    }

    /**
     * This method is not implemented by this class.
     *
     * @param primary                        Not used.
     * @throws UnsupportedOperationException Always thrown as this method is not implemented.
     */
    @Override
    public void setPrimaryType(String primary) {
        throw new UnsupportedOperationException("ImmutableMimeType.setPrimaryType is deliberately not implemented to prevent mutation");
    }

    /**
     * This method is not implemented by this class.
     *
     * @param sub                            Not used.
     * @throws UnsupportedOperationException Always thrown as this method is not implemented.
     */
    @Override
    public void setSubType(String sub) {
        throw new UnsupportedOperationException("ImmutableMimeType.setSubType is deliberately not implemented to prevent mutation");
    }

    /**
     * Returns a newly constructed ImmutableMimeType object given an MIME type string.
     *
     * @param mimeType                  The MIME type string.
     * @return                          A newly constructed ImmutableMimeType object.
     * @throws MimeTypeParseException   If the MIME type string is incorrectly formatted.
     */
    public static ImmutableMimeType of(String mimeType) throws MimeTypeParseException {
        return mimeType == null ? null : new ImmutableMimeType(mimeType);
    }

    /**
     * Returns a newly constructed ImmutableMimeType object given another MimeType object.
     *
     * @param mimeType                  The MimeType object to be cloned.
     * @return                          A newly constructed ImmutableMimeType object.
     * @throws MimeTypeParseException   If the MIME type string returned by the given MimeType object is incorrectly
     *                                  formatted.
     */
    public static ImmutableMimeType of(MimeType mimeType) throws MimeTypeParseException {
        return mimeType == null ? null : (mimeType instanceof ImmutableMimeType ? (ImmutableMimeType)mimeType : new ImmutableMimeType(mimeType));
    }

    /**
     * Returns a list of newly constructed ImmutableMimeType objects given a list of MIME type strings.
     *
     * @param mimeTypes                 The list of MIME type strings.
     * @return                          A list of newly constructed ImmutableMimeType objects.
     * @throws MimeTypeParseException   If a MIME type string is incorrectly formatted.
     */
    public static ImmutableMimeType[] of(String ...mimeTypes) throws MimeTypeParseException {
        if (mimeTypes == null) return null;

        ImmutableMimeType[] output = new ImmutableMimeType[mimeTypes.length];

        for(int i = 0; i < mimeTypes.length; i++) {
            output[i] = of(mimeTypes[i]);
        }

        return output;
    }

    /**
     * Returns a list of newly constructed ImmutableMimeType objects given a list of MimeType objects.
     *
     * @param mimeTypes                 The list of MimeType objects.
     * @return                          A list of newly constructed ImmutableMimeType objects.
     * @throws MimeTypeParseException   If MimeType object returns an incorrectly formatted MIME type string.
     */
    public static ImmutableMimeType[] of(MimeType ...mimeTypes) throws MimeTypeParseException {
        if (mimeTypes == null) return null;

        ImmutableMimeType[] output = new ImmutableMimeType[mimeTypes.length];

        for(int i = 0; i < mimeTypes.length; i++) {
            output[i] = of(mimeTypes[i]);
        }

        return output;
    }

    /**
     * Returns a list of newly constructed ImmutableMimeType objects given a list of existing MimeType objects.
     *
     * @param mimeTypes                 The list of MimeType objects.
     * @return                          A list of newly constructed ImmutableMimeType objects.
     * @throws MimeTypeParseException   If a MimeType object returns an incorrectly formatted MIME type string.
     */
    public static Collection<ImmutableMimeType> of(Collection<MimeType> mimeTypes) throws MimeTypeParseException {
        if (mimeTypes == null) return null;

        Collection<ImmutableMimeType> output = new ArrayList<ImmutableMimeType>(mimeTypes.size());

        for (MimeType mimeType : mimeTypes) {
            output.add(of(mimeType));
        }

        return output;
    }
}
