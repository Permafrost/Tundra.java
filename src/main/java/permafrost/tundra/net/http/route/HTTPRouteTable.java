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

package permafrost.tundra.net.http.route;

import com.wm.app.b2b.server.ServerAPI;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.net.http.HTTPMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A table of HTTP routing instructions, ordered by Integration Server HTTP dispatch directive.
 */
public class HTTPRouteTable extends TreeMap<String, HTTPRouteList> {
    protected final static Pattern ROUTE_PATTERN = Pattern.compile("(?i)(?m)^[ \\t]*(get|put|post|head|connect|options|delete|trace)[ \\t]+(\\/?([^{}\\s\\/]+)(\\/\\S+)?)[ \\t]+(\\S+)([ \\t]+(.*)[ \\t]*)?$");
    protected final static String CONFIGURATION_FILE_NAME = "http-routes.cnf";
    protected final static Set<String> PROHIBITED_DIRECTIVES = new TreeSet<String>();

    /**
     * Set up prohibited directives so we don't break built-in IS functionality.
     */
    static {
        PROHIBITED_DIRECTIVES.add("invoke");
        PROHIBITED_DIRECTIVES.add(com.wm.util.Config.getProperty("invoke", "watt.server.invokeDirective"));
        PROHIBITED_DIRECTIVES.add(com.wm.app.b2b.server.SOAP.getSOAPdirective());
        PROHIBITED_DIRECTIVES.add("web");
        PROHIBITED_DIRECTIVES.add("wm-message");
        PROHIBITED_DIRECTIVES.add("ws");
    }

    /**
     * Constructs a new HTTP route table.
     */
    public HTTPRouteTable() {
        super();
    }

    /**
     * Returns all the HTTP dispatch directives registered in this route table.
     * @return All the HTTP dispatch directives registered in this route table.
     */
    public Set<String> getDirectives() {
        return keySet();
    }

    /**
     * Add an HTTP route instruction to the this routing table.
     * @param route The route instruction to add to the table.
     */
    public void put(HTTPRoute route) {
        String directive = route.getDirective();
        if (!PROHIBITED_DIRECTIVES.contains(directive)) {
            put(directive, HTTPRouteList.add(get(directive), route));
        }
    }

    /**
     * Creates a new HTTP route table from the the route configuration files in each package.
     * @return The new HTTP route table.
     */
    public static HTTPRouteTable newInstance() {
        List<File> files = new ArrayList<File>();
        files.add(new File(ServerAPI.getServerConfigDir(), CONFIGURATION_FILE_NAME));

        String[] packageNames = ServerAPI.getEnabledPackages();
        Arrays.sort(packageNames); // make sure list is in a predicatable order
        for (String packageName : packageNames) {
            files.add(new File(ServerAPI.getPackageConfigDir(packageName), CONFIGURATION_FILE_NAME));
        }

        Map<File, String> contents = new LinkedHashMap<File, String>();
        for (File file : files) {
            try {
                if (file.exists() && file.isFile()) contents.put(file, (String) FileHelper.readToString(file));
            } catch (IOException ex) {
                // do nothing
            }
        }

        return newInstance(contents);
    }

    /**
     * Creates a new HTTP route table from the given list of route configuration file contents.
     * @param contents A list of route configuration file contents.
     * @return         The new HTTP route table.
     */
    protected static HTTPRouteTable newInstance(Map<File, String> contents) {
        HTTPRouteTable table = new HTTPRouteTable();

        if (contents != null) {
            for (java.util.Map.Entry<File, String> entry : contents.entrySet()) {
                File source = entry.getKey();
                String content = entry.getValue();

                java.util.regex.Matcher matcher = ROUTE_PATTERN.matcher(content);
                while (matcher.find()) {
                    String method = matcher.group(1);
                    String uri = matcher.group(2);
                    String target = matcher.group(5);
                    String description = matcher.group(7);
                    if (description != null && description.equals("")) description = null;
                    table.put(new HTTPRoute(method, uri, target, description, source));
                }
            }
        }

        return table;
    }

    /**
     * Returns the matching route for the given HTTP request method and URI, or null if no matching route is found.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       The matching route for the given method and uri, or null if no matching route is found.
     */
    public Map.Entry<HTTPRoute, IData> match(HTTPMethod method, String uri) {
        String directive = HTTPRoute.getDirective(uri);
        HTTPRouteList routes = get(directive);
        Map.Entry<HTTPRoute, IData> result = null;

        if (routes != null) result = routes.match(method, uri);

        return result;
    }

    /**
     * Returns the matching route for the given HTTP request method and URI, or null if no matching route is found.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       The matching route for the given method and uri, or null if no matching route is found.
     */
    public Map.Entry<HTTPRoute, IData> match(String method, String uri) {
        return match(HTTPMethod.normalize(method), uri);
    }

    /**
     * Returns an IData[] representation of this HTTP route table.
     * @return An IData[] representation of this HTTP route table.
     */
    public IData[] toIDataArray() {
        java.util.List<IData> output = new java.util.ArrayList<IData>(size());

        for (String directive : getDirectives()) {
            IData item = IDataFactory.create();
            IDataCursor cursor = item.getCursor();
            IDataUtil.put(cursor, "directive", directive);
            IData[] list = get(directive).toIDataArray();
            if (list != null) {
                IDataUtil.put(cursor, "routes", list);
                IDataUtil.put(cursor, "routes.length", "" + list.length);
            }
            cursor.destroy();

            output.add(item);
        }

        return output.toArray(new IData[output.size()]);
    }
}
