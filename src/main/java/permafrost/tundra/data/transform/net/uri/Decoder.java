/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

package permafrost.tundra.data.transform.net.uri;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.net.uri.URIHelper;
import java.nio.charset.Charset;

/**
 * URI decodes strings in IData documents and IData[] document lists.
 */
public class Decoder extends Transformer<String, String> {
    protected Charset charset;

    /**
     * Creates a new Decoder object.
     */
    public Decoder() {
        this(null);
    }

    /**
     * Creates a new Decoder object.
     *
     * @param charset   The character set to use when URI decoding the string.
     */
    public Decoder(Charset charset) {
        this(charset, true);
    }

    /**
     * Creates a new Decoder object.
     *
     * @param charset   The character set to use when URI decoding the string.
     * @param recurse   Whether to recursively transform child IData documents and IData[] document lists.
     */
    public Decoder(Charset charset, boolean recurse) {
        super(String.class, String.class, TransformerMode.VALUES, recurse, true, true, true);
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    @Override
    protected String transformValue(String key, String value) {
        return URIHelper.decode(value, charset);
    }
}
