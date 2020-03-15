/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

import permafrost.tundra.lang.StringHelper;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Resolves an HTTP Accept header against a list of supported media types to choose the best response type.
 */
public class MediaRange extends MimeType {
    /**
     * The wildcard character used to match any primary or subtype.
     */
    public final static String MEDIA_RANGE_WILDCARD = "*";
    /**
     * The inner MimeType object used to implement this class.
     */
    protected MimeType innerMimeType;

    /**
     * Constructs a new MediaRange object.
     *
     * @param mediaRange                The media range as a string, such as "text/html;q=0.9".
     * @throws MimeTypeParseException   If the given string is malformed and cannot be parsed.
     */
    public MediaRange(String mediaRange) throws MimeTypeParseException {
        String[] components = StringHelper.split(mediaRange, ";", true);
        if (components != null && components.length > 0) {
            String typeComponent = components[0].trim();
            String primaryType = "", subType = "";

            String[] typeParts = StringHelper.split(typeComponent, "/", true);
            if (typeParts.length > 0) {
                primaryType = typeParts[0].trim();
            }
            if (typeParts.length > 1) {
                subType = typeParts[1].trim();
            } else {
                subType = MEDIA_RANGE_WILDCARD;
            }

            innerMimeType = new MimeType(primaryType, subType);

            // parse parameters
            if (components.length > 1) {
                for (int i = 1; i < components.length; i++) {
                    String[] parameterComponents = StringHelper.split(components[i], "=", true);
                    String name = "", value = "";
                    if (parameterComponents.length > 0) {
                        name = parameterComponents[0];
                    }
                    if (parameterComponents.length > 1) {
                        value = parameterComponents[1];
                    }
                    innerMimeType.setParameter(name, value);
                }
            }
        }
    }

    /**
     * Parses the given comma-delimited list of media ranges, typically sourced from an HTTP Accept header.
     *
     * @param acceptedTypes The comma-delimited list of media ranges.
     * @return              A list of MediaRange objects.
     */
    public static List<MediaRange> parse(String acceptedTypes) {
        List<MediaRange> mediaRanges = Collections.emptyList();

        if (acceptedTypes != null) {
            String[] components = StringHelper.split(acceptedTypes, ",", true);
            if (components != null) {
                mediaRanges = new ArrayList<MediaRange>(components.length);
                for (String component : components) {
                    try {
                        mediaRanges.add(new MediaRange(component));
                    } catch (MimeTypeParseException ex) {
                        // ignore exception, client must be erroneous
                    }
                }
            }
        }

        return mediaRanges;
    }

    /**
     * Returns the quality of this MediaRange object, which is the value of the "q" parameter, or 1.0f if no "q"
     * parameter was specified.
     *
     * @return  The quality of the MediaRange object.
     */
    public float getQuality() {
        float quality = 1.0f;

        String q = getParameter("q");
        if (q != null && !"".equals(q.trim())) {
            try {
                quality = Float.parseFloat(q);
                if (quality < 0.0f) {
                    quality = 0.0f;
                } else if (quality > 1.0f) {
                    quality = 1.0f;
                }
            } catch (NumberFormatException ex) {
                // ignore exception;
            }
        }

        return quality;
    }

    /**
     * Returns true if this MediaRange object is a match for the given MimeType object.
     *
     * @param target    The MimeType object to match against.
     * @return          True if this MediaRange is a match for the given MimeType object.
     */
    protected boolean isMatch(MimeType target) {
        return target != null &&
                (MEDIA_RANGE_WILDCARD.equals(this.getPrimaryType()) || MEDIA_RANGE_WILDCARD.equals(target.getPrimaryType()) || (target.getPrimaryType().equals(this.getPrimaryType()))) &&
                (MEDIA_RANGE_WILDCARD.equals(this.getSubType()) || MEDIA_RANGE_WILDCARD.equals(target.getSubType()) || target.getSubType().equals(this.getSubType()) || target.getSubType().endsWith("+" + this.getSubType()));
    }

    /**
     * Scores how well this MediaRange object matches the given MimeType object.
     *
     * @param target    The MimeType object to match against.
     * @return          The match score for this MediaRange, where higher numbers are a better match, and 0 or less is
     *                  not a match.
     */
    @SuppressWarnings("unchecked")
    protected float matches(MimeType target) {
        float score = -1.0f;

        if (isMatch(target)) {
            score = 0.1f;

            if (target.getPrimaryType().equals(this.getPrimaryType())) {
                score += 1000.0f;
            }

            if (target.getSubType().equals(this.getSubType())) {
                score += 100.0f;
            } else if (target.getSubType().endsWith("+" + this.getSubType())) {
                score += 10.0f;
            }

            MimeTypeParameterList rangeParameters = this.getParameters();
            Enumeration rangeParameterNames = rangeParameters.getNames();
            while (rangeParameterNames.hasMoreElements()) {
                String parameterName = (String) rangeParameterNames.nextElement();
                if (!parameterName.equals("q")) {
                    if (this.getParameter(parameterName).equals(target.getParameter(parameterName))) {
                        score += 1.0f;
                    }
                }
            }

            score = score * getQuality();
        }

        return score;
    }

    /**
     * Returns the best match against the given list of MediaRange objects from the given supported MimeType objects.
     *
     * @param acceptedTypes     List of media ranges to resolve against.
     * @param supportedTypes    List of supported MimeType objects.
     * @return                  The MimeType object from supportedTypes which best matches the given acceptedTypes, or
     *                          null if a match was not found.
     */
    public static MimeType resolve(List<MediaRange> acceptedTypes, List<MimeType> supportedTypes) {
        if (supportedTypes == null || supportedTypes.size() == 0) return null;
        if (acceptedTypes == null || acceptedTypes.size() == 0) return supportedTypes.get(0);

        float highestScore = -1.0f;
        MimeType highestScoredMimeType = null;

        for (MimeType supportedType : supportedTypes) {
            if (supportedType != null) {
                for (MediaRange acceptedType : acceptedTypes) {
                    if (acceptedType != null) {
                        float currentScore = acceptedType.matches(supportedType);
                        if (currentScore > 0 && currentScore > highestScore) {
                            highestScore = currentScore;
                            highestScoredMimeType = supportedType;
                        }
                    }
                }
            }
        }

        return highestScoredMimeType;
    }

    @Override
    public String getBaseType() {
        return innerMimeType.getBaseType();
    }

    @Override
    public String getParameter(String name) {
        return innerMimeType.getParameter(name);
    }

    @Override
    public MimeTypeParameterList getParameters() {
        return innerMimeType.getParameters();
    }

    @Override
    public String getPrimaryType() {
        return innerMimeType.getPrimaryType();
    }

    @Override
    public String getSubType() {
        return innerMimeType.getSubType();
    }

    @Override
    public boolean match(MimeType type) {
        return innerMimeType.match(type);
    }

    @Override
    public boolean match(String rawdata) throws MimeTypeParseException {
        return innerMimeType.match(new MediaRange(rawdata));
    }

    @Override
    public void readExternal(ObjectInput in) {
        throw new UnsupportedOperationException("readExternal(ObjectInput) not implemented");
    }

    @Override
    public void removeParameter(String name) {
        innerMimeType.removeParameter(name);
    }

    @Override
    public void setParameter(String name, String value) {
        innerMimeType.setParameter(name, value);
    }

    @Override
    public void setPrimaryType(String primary) throws MimeTypeParseException {
        innerMimeType.setPrimaryType(primary);
    }

    @Override
    public void setSubType(String sub) throws MimeTypeParseException {
        innerMimeType.setSubType(sub);
    }

    @Override
    public String toString() {
        return innerMimeType.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) {
        throw new UnsupportedOperationException("writeExternal(ObjectOutput) not implemented");
    }
}