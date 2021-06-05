/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Lachlan Dowding
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

package permafrost.tundra.data.transform.xml.namespace;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.xml.namespace.NamespaceHelper;
import javax.xml.namespace.NamespaceContext;

/**
 * Normalizes the namespace prefixes specified in document keys using the given
 * context.
 */
public class Normalizer extends Transformer<String, String> {
    /**
     * The namespace context used for normalizing prefixes against.
     */
    protected NamespaceContext context;

    /**
     * Normalizes all key's namespace prefixes against the given namespace
     * context.
     *
     * @param context   The namespace context to use.
     */
    public Normalizer(NamespaceContext context, TransformerMode transformerMode, boolean recurse) {
        super(String.class, String.class, transformerMode, recurse, true, true, true);
        this.context = context;
    }

    /**
     * Normalizes the namespace prefixes on the given key.
     *
     * @param key   The key to be transformed.
     * @param value The value associated with the key being transformed.
     * @return      The key with normalized namespace prefixes.
     */
    @Override
    protected String transformKey(String key, Object value) {
        return NamespaceHelper.normalize(key, context);
    }

    /**
     * Normalizes the namespace prefixes on the given value.
     *
     * @param value The value to transform.
     * @return      The value with normalized namespace prefixes.
     */
    @Override
    protected String transformValue(String key, String value) {
        return NamespaceHelper.normalize(value, context);
    }
}
