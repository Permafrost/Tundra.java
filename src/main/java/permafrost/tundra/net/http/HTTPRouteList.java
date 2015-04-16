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

package permafrost.tundra.net.http;

import com.wm.data.IData;

import java.util.*;

/**
 * A list of HTTP routes.
 */
public class HTTPRouteList extends ArrayList<HTTPRoute> {
    /**
     * Constructs a new HTTP route list.
     */
    public HTTPRouteList() {
        super();
    }

    /**
     * Constructs a new HTTP route list with the given intial capacity.
     * @param intialCapacity The initial capacity (number of items) in the underlying array.
     */
    public HTTPRouteList(int intialCapacity) {
        super(intialCapacity);
    }

    /**
     * Constructs a new HTTP route list seeded with the given collection of routes.
     * @param collection The collection to seed the list with.
     */
    public HTTPRouteList(Collection<? extends HTTPRoute> collection) {
        super(collection);
    }

    /**
     * Add the given route to the given list, returning the list. If the given list is null,
     * a new list is constructed containing the given route and returned.
     * @param list  The list to add the route to.
     * @param route The route to be added.
     * @return      The list after the route was added. If the given list was null, a new
     *              list containing the route will be returned.
     */
    public static HTTPRouteList add(HTTPRouteList list, HTTPRoute route) {
        if (list == null) list = new HTTPRouteList();
        list.add(route);
        return list;
    }

    /**
     * Returns an IData[] representation of the route list.
     * @return An IData[] representation of the route list.
     */
    public IData[] toIDataArray() {
        List<IData> output = new ArrayList<IData>(size());
        for (HTTPRoute route : this) {
            output.add(route.getIData());
        }
        return output.toArray(new IData[output.size()]);
    }

    /**
     * Returns the route that matches the given HTTP request method and URI, or null if no route in the list matches.
     * @param method The HTTP method to match against.
     * @param uri    The URI to match against.
     * @return       The route that matched the given method and uri, or null if no route in the list matches.
     */
    public Map.Entry<HTTPRoute, IData> match(HTTPMethod method, String uri) {
        for (HTTPRoute route : this) {
            IData parameters = route.match(method, uri);
            if (parameters != null) {
                return new AbstractMap.SimpleImmutableEntry<HTTPRoute, IData>(route, parameters);
            }
        }
        return null;
    }

    /**
     * Returns the route that matches the given HTTP request method and URI, or null if no route in the list matches.
     * @param method The HTTP method to match against.
     * @param uri    The URI to match against.
     * @return       The route that matched the given method and uri, or null if no route in the list matches.
     */
    public Map.Entry<HTTPRoute, IData> match(String method, String uri) {
        return match(HTTPMethod.normalize(method), uri);
    }
}
