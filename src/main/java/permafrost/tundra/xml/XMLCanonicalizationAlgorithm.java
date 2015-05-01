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

package permafrost.tundra.xml;

import java.util.HashMap;
import java.util.Map;

public enum XMLCanonicalizationAlgorithm {
    CANONICAL_XML_VERSION_1_0("Canonical XML Version 1.0", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315"),
    CANONICAL_XML_VERSION_1_0_WITH_COMMENTS("Canonical XML Version 1.0 With Comments", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"),
    INCLUSIVE_CANONICAL_XML_VERSION_1_0("Inclusive Canonical XML Version 1.0", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315"),
    INCLUSIVE_CANONICAL_XML_VERSION_1_0_WITH_COMMENTS("Inclusive Canonical XML Version 1.0 With Comments", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"),
    EXCLUSIVE_CANONICAL_XML_VERSION_1_0("Exclusive Canonical XML Version 1.0", "http://www.w3.org/2001/10/xml-exc-c14n#"),
    EXCLUSIVE_CANONICAL_XML_VERSION_1_0_WITH_COMMENTS("Exclusive Canonical XML Version 1.0 With Comments", "http://www.w3.org/2001/10/xml-exc-c14n#WithComments"),
    CANONICAL_XML_VERSION_1_1("Canonical XML Version 1.1", "http://www.w3.org/2006/12/xml-c14n11"),
    CANONICAL_XML_VERSION_1_1_WITH_COMMENTS("Canonical XML Version 1.1 With Comments", "http://www.w3.org/2006/12/xml-c14n11#WithComments"),
    INCLUSIVE_CANONICAL_XML_VERSION_1_1("Inclusive Canonical XML Version 1.1", "http://www.w3.org/2006/12/xml-c14n11"),
    INCLUSIVE_CANONICAL_XML_VERSION_1_1_WITH_COMMENTS("Inclusive Canonical XML Version 1.1 With Comments", "http://www.w3.org/2006/12/xml-c14n11#WithComments");

    /**
     * The default convert mode used by Tundra.
     */
    public static final XMLCanonicalizationAlgorithm DEFAULT_ALGORITHM = CANONICAL_XML_VERSION_1_0;

    private static final Map<String, XMLCanonicalizationAlgorithm> ALGORITHMS_BY_NAME = new HashMap<String, XMLCanonicalizationAlgorithm>();
    private static final Map<String, XMLCanonicalizationAlgorithm> ALGORITHMS_BY_ID = new HashMap<String, XMLCanonicalizationAlgorithm>();

    static {
        for (XMLCanonicalizationAlgorithm algorithm : XMLCanonicalizationAlgorithm.values()) {
            ALGORITHMS_BY_NAME.put(algorithm.name.toLowerCase(), algorithm);
            ALGORITHMS_BY_ID.put(algorithm.id.toLowerCase(), algorithm);
        }
    }

    private String name, id;

    /**
     * Constructs a new XML canonicalization algorithm.
     * @param name  The human-readable name of the XML canonicalization algorithm.
     * @param id    The ID of the XML canonicalization algorithm.
     */
    XMLCanonicalizationAlgorithm(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Returns an XML canonicalization algorithm associated with the given name.
     * @param name The value to be converted to an HTTPMethod.
     * @return      The HTTPMethod representing the given value.
     */
    public static XMLCanonicalizationAlgorithm normalize(String name) {
        XMLCanonicalizationAlgorithm algorithm = null;
        if (name != null) {
            name = name.toLowerCase();
            algorithm = ALGORITHMS_BY_NAME.get(name);
            if (algorithm == null) {
                algorithm = ALGORITHMS_BY_ID.get(name);
            }
        }
        return normalize(algorithm);
    }

    /**
     * Normalizes the given XMLCanonicalizationAlgorithm.
     * @param algorithm The XMLCanonicalizationAlgorithm to be normalized.
     * @return          The default XMLCanonicalizationAlgorithm if algorithm is null, otherwise algorithm.
     */
    public static XMLCanonicalizationAlgorithm normalize(XMLCanonicalizationAlgorithm algorithm) {
        return algorithm == null ? DEFAULT_ALGORITHM : algorithm;
    }

    /**
     * Returns the XML canonicalization ID of this algorithm.
     * @return The XML canonicalization ID of this algorithm.
     */
    public String getID() {
        return id;
    }
}
