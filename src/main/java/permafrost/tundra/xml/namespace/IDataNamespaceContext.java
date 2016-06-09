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

package permafrost.tundra.xml.namespace;

import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * An XML namespace context that uses an IData document as its source of namespace prefix URI mappings.
 */
public class IDataNamespaceContext implements NamespaceContext {
    protected Map<String, String> namespacesByPrefix = new TreeMap<String, String>();
    protected Map<String, List<String>> namespacesByURI = new TreeMap<String, List<String>>();

    /**
     * Constructs a new IDataNamespaceContext using the given IData as the source of namespace prefix URI mappings.
     *
     * @param document The IData document used as a source of namespace prefix URI mappings.
     */
    public IDataNamespaceContext(IData document) {
        for (Map.Entry<String, Object> entry : IDataMap.of(document)) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key != null && value != null) {
                // treat `default` as an alias to the default namespace prefix
                if (key.equals("default")) key = XMLConstants.DEFAULT_NS_PREFIX;

                namespacesByPrefix.put(key, value.toString());

                List<String> list = namespacesByURI.get(value.toString());
                if (list == null) {
                    list = new ArrayList<String>();
                    namespacesByURI.put(value.toString(), list);
                }
                list.add(key);
            }
        }
    }

    /**
     * Returns a new IDataNamespaceContext given an IData document.
     *
     * @param document  The IData document containing the namespace declarations.
     * @return          A new IDataNamespaceContext if the given IData was not null
     *                  and contained one or more namespace declarations, otherwise
     *                  null.
     */
    public static IDataNamespaceContext of(IData document) {
        return IDataHelper.size(document) == 0 ? null : new IDataNamespaceContext(document);
    }

    /**
     * Returns the URI associated with the given prefix.
     *
     * @param prefix The prefix whose URI is to be returned.
     * @return The URI associated with the given prefix.
     */
    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new IllegalArgumentException("prefix must not be null");

        String uri = namespacesByPrefix.get(prefix);

        return uri == null ? XMLConstants.NULL_NS_URI : uri;
    }

    /**
     * Returns the prefix associated with the given URI.
     *
     * @param uri The URI whose prefix is to be returned.
     * @return The prefix associated with the given URI.
     */
    @Override
    public String getPrefix(String uri) {
        if (uri == null) throw new IllegalArgumentException("uri must not be null");

        String prefix = null;

        List<String> list = namespacesByURI.get(uri);
        if (list != null && list.size() > 0) prefix = list.get(0);

        return prefix;
    }

    /**
     * Returns an iterator which iterates over all the prefixes associated with the given URI.
     *
     * @param uri The URI whose prefixes are to be iterated over.
     * @return An Iterator which iterates over all the prefixes associated with the given URI.
     */
    @Override
    public Iterator getPrefixes(String uri) {
        if (uri == null) throw new IllegalArgumentException("uri must not be null");

        Iterator iterator = null;

        List<String> list = namespacesByURI.get(uri);
        if (list != null) iterator = list.iterator();

        return iterator;
    }
}
