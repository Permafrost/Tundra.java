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

package permafrost.tundra.data.transform.xml;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.xml.XMLHelper;

/**
 * XML encodes string elements in an IData document or IData[] document list.
 */
public class Encoder extends Transformer<String, String> {
    /**
     * The prefix used to denote XML attributes in a parsed IData document.
     */
    protected String attributePrefix;

    /**
     * Creates a new Encoder object.
     */
    public Encoder() {
        this(null);
    }

    /**
     * Creates a new Encoder object.
     *
     * @param attributePrefix   The prefix used to denote XML attributes in a parsed IData document.
     */
    public Encoder(String attributePrefix) {
        this(attributePrefix, true);
    }

    /**
     * Creates a new Encoder object.
     *
     * @param recurse   Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Encoder(boolean recurse) {
        this(null, recurse);
    }

    /**
     * Creates a new Encoder object.
     *
     * @param attributePrefix   The prefix used to denote XML attributes in a parsed IData document.
     * @param recurse           Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Encoder(String attributePrefix, boolean recurse) {
        super(String.class, String.class, TransformerMode.VALUES, recurse, true, true, true);
        this.attributePrefix = attributePrefix == null ? XMLHelper.DEFAULT_ATTRIBUTE_PREFIX : attributePrefix;
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    protected String transformValue(String key, String value) {
        return XMLHelper.encode(value, key.startsWith(attributePrefix));
    }
}
