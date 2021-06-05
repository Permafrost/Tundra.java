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

package permafrost.tundra.xml.namespace;

import permafrost.tundra.data.IDataKey;
import javax.xml.namespace.NamespaceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience methods for working with XML namespaces.
 */
public class NamespaceHelper {
    /**
     * The character used to separate a namespace prefix from the suffix.
     */
    private static final String NAMESPACE_PREFIX_SEPARATOR = ":";
    /**
     * Regular expression pattern used for matching against an IData key to find any namespace prefixes.
     */
    private static final Pattern NAMESPACE_PREFIX_PATTERN = Pattern.compile("((@)?([^" + NAMESPACE_PREFIX_SEPARATOR + "]*)" + NAMESPACE_PREFIX_SEPARATOR + ")(.*)");

    /**
     * Disallow instantiation of this class.
     */
    private NamespaceHelper() {}

    /**
     * Normalizes the given IData key to use the highest priority namespace prefixes specified in the given
     * namespace context.
     *
     * @param key       The key to normalize namespace prefixes in.
     * @param context   The namespace context to normalize against.
     * @return          The key with namespace prefixes normalized.
     */
    public static String normalize(String key, NamespaceContext context) {
        if (key == null || context == null) return key;

        StringBuilder builder = new StringBuilder();
        List<Token> tokens = tokenize(key);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            String prefix = token.prefix;
            String namespace = token.namespace;
            String suffix = token.suffix;

            if (i > 0) {
                builder.append(IDataKey.SEPARATOR);
            }

            if (prefix != null) {
                builder.append(prefix);
            }
            if (namespace != null) {
                String uri = context.getNamespaceURI(namespace);
                if (uri != null) {
                    String normalizedNamespace = context.getPrefix(uri);
                    if (normalizedNamespace != null) {
                        namespace = normalizedNamespace;
                    }
                }
                if (!"".equals(namespace)) {
                    builder.append(namespace);
                    builder.append(NAMESPACE_PREFIX_SEPARATOR);
                }
            }
            if (suffix != null) {
                builder.append(suffix);
            }
        }

        return builder.toString();
    }

    /**
     * Tokenizes the given IData key into a list of components each comprised of a prefix and suffix.
     *
     * @param key   The key to tokenize.
     * @return      A list of components each comprised of a suffix to prefix association.
     */
    private static List<Token> tokenize(String key) {
        List<Token> tokens = new ArrayList<Token>();
        String[] components = key.split(IDataKey.SEPARATOR);
        for (String component : components) {
            String prefix, namespace, suffix;
            Matcher matcher = NAMESPACE_PREFIX_PATTERN.matcher(component);
            if (matcher.matches()) {
                prefix = matcher.group(2);
                namespace = matcher.group(3);
                suffix = matcher.group(4);
            } else {
                prefix = null;
                namespace = null;
                suffix = component;
            }
            tokens.add(new Token(prefix, namespace, suffix));
        }
        return tokens;
    }

    private static class Token {
        public String prefix;
        public String namespace;
        public String suffix;

        public Token(String prefix, String namespace, String suffix) {
            this.prefix = prefix;
            this.namespace = namespace;
            this.suffix = suffix;
        }
    }
}
