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

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.util.coder.IDataCodable;
import org.springframework.web.util.UriTemplate;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.io.FileHelper;
import permafrost.tundra.net.http.HTTPMethod;
import permafrost.tundra.net.uri.URIHelper;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An individual routing instruction which maps an HTTP method and URI template to an implementation service.
 */
public class HTTPRoute implements IDataCodable {
    /**
     * A regular expression used to determine if the target is a service or a document.
     */
    protected static final Pattern SERVICE_PATTERN = Pattern.compile("^(([^\\s\\.\\/:]+)(\\.[^\\s\\.\\/:]+)*)(:([^\\s\\.\\/:]+))$");

    /**
     * The HTTP method used for matching against HTTP requests.
     */
    protected HTTPMethod method;

    /**
     * The URI template used for matching against HTTP request URIs.
     */
    protected UriTemplate uri;

    /**
     * Whether this routing instruction target is a service or a document.
     */
    protected boolean isInvoke;

    /**
     * The service to be invoked when this routing instruction matches an HTTP request.
     */
    protected NSName service;

    /**
     * The target which is invoked/redirected to when an HTTP request matching the route's method and URI template is received.
     */
    protected String target;

    /**
     * A description of the routing instruction.
     */

    protected String description;
    /**
     * The source file / package configuration file the routing instruction was loaded from.
     */
    protected File source;

    /**
     * Constructs a new routing instruction.
     * @param method        The HTTP method to be routed.
     * @param uri           The URI to be routed.
     * @param target        The target to be routed.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    public HTTPRoute(HTTPMethod method, String uri, String target, String description, File source) {
        initialize(method, uri, target, description, source);
    }

    /**
     * Constructs a new routing instruction.
     * @param method        The HTTP method to be routed.
     * @param uri           The URI to be routed.
     * @param target        The target to be routed.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    public HTTPRoute(String method, String uri, String target, String description, File source) {
        initialize(method, uri, target, description, source);
    }

    /**
     * Constructs a new routing instruction from the given IData document.
     * @param document  An IData object representing the routing instruction; must contain
     *                  the following keys: method, uri, target, description, source.
     */
    public HTTPRoute(IData document) {
        setIData(document);
    }

    /**
     * Returns the HTTP request method this routing instruction matches.
     * @return The HTTP request method this routing instruction matches.
     */
    public HTTPMethod getMethod() {
        return method;
    }

    /**
     * Returns the URI template this routing instruction matches.
     * @return The URI template this routing instruction matches.
     */
    public UriTemplate getURI() {
        return uri;
    }

    /**
     * Returns the target which is executed for this routing instruction.
     * @return The target which is executed for this routing instruction.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns true if the target is a service invocation.
     * @return True if the target is a service invocation.
     */
    public boolean isInvoke() {
        return isInvoke;
    }

    /**
     * Returns true if the target is a document.
     * @return True if the target is a document.
     */
    public boolean isDocument() {
        return !isInvoke;
    }

    /**
     * Returns the service which is invoked for this routing instruction, or null if target is a document.
     * @return The service which is invoked for this routing instruction, or null if target is a document.
     */
    public NSName getService() {
        return service;
    }

    /**
     * Returns the description of this routing instruction.
     * @return The description of this routing instruction.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the source file for this routing instruction.
     * @return The source file for this routing instruction.
     */
    public File getSource() {
        return source;
    }

    /**
     * Returns the Integration Server HTTP dispatcher directive for this routing instruction.
     * @return    The directive for this routing instruction's URI.
     */
    public String getDirective() {
        return getDirective(uri.toString());
    }

    /**
     * Returns the Integration Server HTTP dispatcher directive for the given URI string.
     * @param uri The URI to return the directive for.
     * @return    The directive for the given URI.
     */
    public static String getDirective(String uri) {
        if (uri == null) return null;

        uri = uri.trim();
        if (uri.startsWith("/")) uri = uri.substring(1, uri.length());
        int index = uri.indexOf("/");
        if (index >= 0) uri = uri.substring(0, index);

        return uri;
    }

    /**
     * Returns true if the given HTTP request method and URI match this routing instruction.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       True if this routing instruction matches the given method and uri.
     */
    public boolean matches(HTTPMethod method, String uri) {
        return this.method != null && this.method == method && this.uri != null && this.uri.matches(uri);
    }

    /**
     * Returns true if the given HTTP request method and URI match this routing instruction.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       True if this routing instruction matches the given method and uri.
     */
    public boolean matches(String method, String uri) {
        return matches(HTTPMethod.valueOf(method.toUpperCase()), uri);
    }

    /**
     * Returns an IData document containing matched URI template parameters extracted from the given URI.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       An IData document containing the matched URI template parameters, or null if not matched.
     */
    public IData match(HTTPMethod method, String uri) {
        return matches(method, uri) ? normalize(this.uri.match(uri)) : null;
    }

    /**
     * Regular expression pattern for matching key=value URI template constants
     */
    private static final Pattern KEY_EQUALS_VALUE_PATTERN = Pattern.compile("([^=]+?)=(.*)");

    /**
     * Converts a UriTemplate result set to an IData document. Supports converting URI template
     * variables specified in the form {key=value} to key value tuples in resulting IData.
     * @param results   The UriTemplate matched result set.
     * @return          An IData representing the matched results.
     */
    private static IData normalize(Map<String, String> results) {
        if (results == null) return null;

        IDataMap inputMap = IDataMap.of(results);
        IDataMap outputMap = new IDataMap();

        for (Map.Entry<String, Object> entry : inputMap) {
            String key = entry.getKey();
            String value = (String)entry.getValue();

            // support URI templates that contain {key=value} constants
            Matcher matcher = KEY_EQUALS_VALUE_PATTERN.matcher(key);
            if (matcher.matches()) {
                outputMap.put(URIHelper.decode(matcher.group(1)), URIHelper.decode(matcher.group(2)));
            }
            outputMap.put(key, value);
        }

        return outputMap;
    }

    /**
     * Returns an IData document containing matched URI template parameters extracted from the given URI.
     * @param method The HTTP method to match.
     * @param uri    The URI to match.
     * @return       An IData document containing the matched URI template parameters, or null if not matched.
     */
    public IData match(String method, String uri) {
        return match(HTTPMethod.normalize(method), uri);
    }

    /**
     * Returns an IData representation of this routing instruction.
     * @return An IData representation of this routing instruction.
     */
    public IData getIData() {
        IData output = IDataFactory.create();

        IDataCursor cursor = output.getCursor();
        IDataUtil.put(cursor, "method", method.name().toLowerCase());
        IDataUtil.put(cursor, "uri", uri.toString());
        IDataUtil.put(cursor, "target", target);
        if (description != null) IDataUtil.put(cursor, "description", description);
        if (source != null) {
            IDataUtil.put(cursor, "source", FileHelper.normalize(source));
        }
        cursor.destroy();

        return output;
    }

    /**
     * Initializes this HTTP routing instruction.
     * @param document  An IData object representing the routing instruction; must contain
     *                  the following keys: method, uri, target, description, source.
     */
    public void setIData(IData document) {
        if (document == null) return;

        IDataCursor cursor = document.getCursor();
        String method = IDataUtil.getString(cursor, "method");
        String uri = IDataUtil.getString(cursor, "uri");
        String target = IDataUtil.getString(cursor, "target");
        String description = IDataUtil.getString(cursor, "description");
        String source = IDataUtil.getString(cursor, "source");
        cursor.destroy();

        initialize(method, uri, target, description, source);
    }

    /**
     * Initializes this HTTP routing instruction.
     * @param method        The HTTP method for the instruction.
     * @param uri           The URI for the instruction.
     * @param target        The target for the instruction.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    protected void initialize(String method, String uri, String target, String description, String source) {
        initialize(method, uri, target, description, FileHelper.construct(source));
    }

    /**
     * Initializes a new routing instruction.
     * @param method        The HTTP method to be routed.
     * @param uri           The URI to be routed.
     * @param target        The target to be routed.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    protected void initialize(String method, String uri, String target, String description, File source) {
        initialize(HTTPMethod.normalize(method), uri, target, description, source);
    }

    /**
     * Initializes a new routing instruction.
     * @param method        The HTTP method for the instruction.
     * @param uri           The URI for the instruction.
     * @param target        The target for the instruction.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    protected void initialize(HTTPMethod method, String uri, String target, String description, File source) {
        initialize(method, new UriTemplate(uri), target, description, source);
    }

    /**
     * Initializes a new routing instruction.
     * @param method        The HTTP method for the instruction.
     * @param uri           The URI for the instruction.
     * @param target        The target for the instruction.
     * @param description   The description of the instruction.
     * @param source        The source file of the instruction.
     */
    protected void initialize(HTTPMethod method, UriTemplate uri, String target, String description, File source) {
        this.method = method;
        this.uri = uri;
        this.target = target;

        java.util.regex.Matcher matcher = SERVICE_PATTERN.matcher(target);
        isInvoke = matcher.matches();

        if (isInvoke) {
            service = NSName.create(target);
        }

        this.description = description;
        this.source = source;
    }

    /**
     * Compares this object with another object for equality.
     * @param other The object to compare with.
     * @return      True if the two objects are equal.
     */
    public boolean equals(Object other) {
        if (!(other instanceof HTTPRoute)) return false;

        HTTPRoute route = (HTTPRoute)other;
        return this.getMethod() == route.getMethod() && this.getURI().toString().equals(route.getURI().toString()) && this.getTarget().equals(route.getTarget());
    }
}
