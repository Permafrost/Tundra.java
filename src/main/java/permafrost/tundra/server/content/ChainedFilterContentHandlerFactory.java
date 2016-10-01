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

package permafrost.tundra.server.content;

import com.wm.app.b2b.server.ContentHandler;
import com.wm.app.b2b.server.ContentHandlerFactory;
import com.wm.app.b2b.server.ContentManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A content handler factory which wraps a list of chained content handler factories where each factory is used in
 * sequence to create a chained content handler.
 */
public class ChainedFilterContentHandlerFactory extends ProxyContentHandlerFactory {
    /**
     * The list of filters chained together.
     */
    protected List<FilterContentHandlerFactory> filters;

    /**
     * Constructs a new factory that wraps a chain of filters around another factory.
     *
     * @param factory The factory which ultimately handles content.
     * @param filters The chain of filters wrapped by this factory.
     */
    public ChainedFilterContentHandlerFactory(ContentHandlerFactory factory, FilterContentHandlerFactory ...filters) {
        this(factory, filters == null ? null : Arrays.asList(filters));
    }

    /**
     * Constructs a new factory that wraps a chain of factories.
     *
     * @param factory The factory which ultimately handles content.
     * @param filters The chain of filters wrapped by this factory.
     */
    public ChainedFilterContentHandlerFactory(ContentHandlerFactory factory, List<FilterContentHandlerFactory> filters) {
        super(factory);
        this.filters = filters == null ? null : new ArrayList<FilterContentHandlerFactory>(filters);
    }

    /**
     * Returns a new content handler.
     *
     * @return A new content handler.
     */
    public ContentHandler create() {
        ContentHandler handler = factory.create();

        if (filters != null && filters.size() > 0) {
            List<FilterContentHandler> handlers = new ArrayList<FilterContentHandler>(filters.size());

            for (FilterContentHandlerFactory filter : filters) {
                handlers.add(filter.create());
            }

            handler = new ChainedFilterContentHandler(handler, handlers);
        }

        return handler;
    }

    /**
     * Wraps all existing content part handler registrations with a ChainedFilterContentHandlerFactory.
     *
     * @param filters   The filters to be registered.
     */
    public static void register(FilterContentHandlerFactory ...filters) {
        register(filters == null ? null : Arrays.asList(filters));
    }

    /**
     * Wraps all existing content part handler registrations with a ChainedFilterContentHandlerFactory.
     *
     * @param filters   The filters to be registered.
     */
    public static void register(List<FilterContentHandlerFactory> filters) {
        if (filters != null && filters.size() > 0) {
            for (Map.Entry<String, ContentHandlerFactory> entry : ContentManagerHelper.getRegistrations().entrySet()) {
                ContentManager.registerHandler(entry.getKey(), new ChainedFilterContentHandlerFactory(entry.getValue(), filters));
            }
        }
    }

    /**
     * Unwraps all existing content part handler by replacing ChainedFilterContentHandlerFactory objects with
     * the original factories that were registered.
     */
    public static void unregister() {
        for (Map.Entry<String, ContentHandlerFactory> entry : ContentManagerHelper.getRegistrations().entrySet()) {
            ContentHandlerFactory factory = entry.getValue();
            if (factory instanceof ChainedFilterContentHandlerFactory) {
                // restore original content handler factory
                ContentManager.registerHandler(entry.getKey(), ((ChainedFilterContentHandlerFactory)factory).getFactory());
            }
        }
    }
}
