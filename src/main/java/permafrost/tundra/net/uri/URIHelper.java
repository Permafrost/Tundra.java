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

package permafrost.tundra.net.uri;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.net.uri.Decoder;
import permafrost.tundra.data.transform.net.uri.Encoder;
import permafrost.tundra.flow.variable.SubstitutionHelper;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.CharsetHelper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of convenience methods for working with URIs.
 */
public final class URIHelper {
    /**
     * The default character set used for URI strings.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("US-ASCII");
    /**
     * The default character set name used for URI strings.
     */
    public static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name();
    /**
     * The character used to delimit the items in a URI's path.
     */
    public static final String URI_PATH_DELIMITER = "/";
    /**
     * The character used to delimit the start of a URI's query string.
     */
    public static final String URI_QUERY_DELIMITER = "?";
    /**
     * The character used to delimit the user from the password in a URI's authority section.
     */
    public static final String URI_USER_PASSWORD_DELIMITER = ":";

    /**
     * Disallow instantiation of this class.
     */
    private URIHelper() {}

    /**
     * Parses a URI string into an IData representation.
     *
     * @param input                 The URI string to be parsed.
     * @return                      An IData representation of the given URI.
     * @throws URISyntaxException   If the given string is an invalid URI.
     */
    public static IData parse(String input) throws URISyntaxException {
        if (input == null) return null;
        return toIData(fromString(input));
    }

    /**
     * Emits a URI given as an IData document as a string.
     *
     * @param input                 An IData containing a specific URI structure to be serialized.
     * @return                      The URI string representing the given IData.
     * @throws URISyntaxException   If the given string is an invalid URI.
     */
    public static String emit(IData input) throws URISyntaxException {
        if (input == null) return null;
        return toString(fromIData(input));
    }

    /**
     * Normalizes a URI string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html#normalize().
     *
     * @param input                 The URI string to be normalized.
     * @return                      The normalized URI string.
     * @throws URISyntaxException   If the given string is an invalid URI.
     */
    public static String normalize(String input) throws URISyntaxException {
        return toString(normalize(fromString(input)));
    }

    /**
     * Normalizes a IData URI structure, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html#normalize().
     *
     * @param input                 The IData document representing a URI structure to be normalized.
     * @return                      The normalized URI IData document.
     * @throws URISyntaxException   If the given string is an invalid URI.
     */
    public static IData normalize(IData input) throws URISyntaxException {
        return toIData(normalize(fromIData(input)));
    }

    /**
     * Normalizes a URI object, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html#normalize().
     *
     * @param uri   The URI object to be normalized.
     * @return      The normalized URI object.
     */
    public static URI normalize(URI uri) {
        if (uri == null) return null;
        return uri.normalize();
    }

    /**
     * URI encodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input The string to be URI encoded.
     * @return      The string after being URI encoded.
     */
    public static String encode(String input) {
        return encode(input, DEFAULT_CHARSET);
    }

    /**
     * URI encodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input         The string to be URI encoded.
     * @param charsetName   The character set to use when URI encoding the string.
     * @return              The string after being URI encoded.
     */
    public static String encode(String input, String charsetName) {
        return encode(input, CharsetHelper.normalize(charsetName, DEFAULT_CHARSET));
    }

    /**
     * URI encodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input     The string to be URI encoded.
     * @param charset   The character set to use when URI encoding the string.
     * @return          The string after being URI encoded.
     */
    public static String encode(String input, Charset charset) {
        if (input == null) return null;

        String output;

        try {
            output = URLEncoder.encode(input, CharsetHelper.normalize(charset, DEFAULT_CHARSET).name()).replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex); // this should never happen
        }

        return output;
    }

    /**
     * URI encodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input A list of strings to be URI encoded.
     * @return      The new copy of the list of strings after being URI encoded.
     */
    public static String[] encode(String[] input) {
        return encode(input, DEFAULT_CHARSET);
    }

    /**
     * URI encodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input         A list of strings to be URI encoded.
     * @param charsetName   The character set to use when URI encoding the strings.
     * @return              The new copy of the list of strings after being URI encoded.
     */
    public static String[] encode(String[] input, String charsetName) {
        return encode(input, CharsetHelper.normalize(charsetName, DEFAULT_CHARSET));
    }

    /**
     * URI encodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     *
     * @param input     A list of strings to be URI encoded.
     * @param charset   The character set to use when URI encoding the strings.
     * @return          The new copy of the list of strings after being URI encoded.
     */
    public static String[] encode(String[] input, Charset charset) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = encode(input[i], charset);
        }

        return output;
    }

    /**
     * URI encodes an IData document, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param document  The IData document to be encoded.
     * @param charset   The character set to use when URI encoding the strings.
     * @return          The encoded IData document.
     */
    public static IData encode(IData document, Charset charset) {
        return Transformer.transform(document, new Encoder(charset));
    }

    /**
     * URI decodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input The string to be URI decoded.
     * @return      The string after being URI decoded.
     */
    public static String decode(String input) {
        return decode(input, DEFAULT_CHARSET);
    }

    /**
     * URI decodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input         The string to be URI decoded.
     * @param charsetName   The character set to use when URI decoding the string.
     * @return              The string after it has been URI decoded.
     */
    public static String decode(String input, String charsetName) {
        return decode(input, CharsetHelper.normalize(charsetName, DEFAULT_CHARSET));
    }

    /**
     * URI decodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input     The string to be URI decoded.
     * @param charset   The character set to use when URI decoding the string.
     * @return          The string after it has been URI decoded.
     */
    public static String decode(String input, Charset charset) {
        if (input == null) return null;

        String output;

        try {
            output = URLDecoder.decode(input, CharsetHelper.normalize(charset, DEFAULT_CHARSET).name());
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException(ex); // this should never happen
        }

        return output;
    }

    /**
     * URI decodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input     The list of strings to be URI decoded.
     * @return          A new copy of the list of strings after being URI decoded.
     */
    public static String[] decode(String[] input) {
        return decode(input, DEFAULT_CHARSET);
    }

    /**
     * URI decodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input         The list of strings to be URI decoded.
     * @param charsetName   The character set to use when URI decoding the strings.
     * @return              A new copy of the list of strings after being URI decoded.
     */
    public static String[] decode(String[] input, String charsetName) {
        return decode(input, CharsetHelper.normalize(charsetName, DEFAULT_CHARSET));
    }


    /**
     * URI decodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param input     The list of strings to be URI decoded.
     * @param charset   The character set to use when URI decoding the strings.
     * @return          A new copy of the list of strings after being URI decoded.
     */
    public static String[] decode(String[] input, Charset charset) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = decode(input[i], charset);
        }

        return output;
    }

    /**
     * URI decodes an IData document, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     *
     * @param document  The IData document to be decoded.
     * @param charset   The character set to use when URI decoding the strings.
     * @return          The decoded IData document.
     */
    public static IData decode(IData document, Charset charset) {
        return Transformer.transform(document, new Decoder(charset));
    }

    /**
     * Performs variable substitution on the components of the given URI string.
     *
     * @param uri                   The URI string to perform variable substitution on.
     * @param scope                 The scope variables are resolved against.
     * @return                      The resulting URI string after variable substitution.
     * @throws URISyntaxException   If the given string is not a valid URI.
     */
    public static String substitute(String uri, IData scope) throws URISyntaxException {
        // possible URI template
        if (uri.contains("{")) {
            uri = expandTemplate(uri, scope);
        }

        IData parsedURI = parse(uri);

        // possible variable substitution strings
        if (uri.contains("%25")) {
            parsedURI = SubstitutionHelper.substitute(parsedURI, null, true, false, null, scope);
        }

        return emit(parsedURI);
    }

    /**
     * Regular expression pattern to match URI template style variables.
     */
    private static final Pattern URI_TEMPLATE_VARIABLE = Pattern.compile("\\{([^\\{\\}]+)\\}");

    /**
     * Expands the given URI template using the given variable scope.
     *
     * @param template  The URI template to expand.
     * @param scope     The scope against which variable references in the template are resolved.
     * @return          The expanded URI template where variable references have been resolved against the given scope.
     */
    private static String expandTemplate(String template, IData scope) {
        if (template != null) {
            StringBuilder builder = new StringBuilder();
            Matcher matcher = URI_TEMPLATE_VARIABLE.matcher(template);
            int index = 0;
            while (matcher.find()) {
                builder.append(template, index, matcher.start());
                builder.append(encode(SubstitutionHelper.resolve(matcher.group(1), String.class, matcher.group(0), null, scope)));
                index = matcher.end();
            }
            if (index > 0) {
                builder.append(template.substring(index));
                template = builder.toString();
            }
        }
        return template;
    }

    /**
     * Converts a URI object to an IData representation.
     *
     * @param uri The URI object to be converted.
     * @return    An IData representation of the given URI.
     */
    public static IData toIData(URI uri) {
        if (uri == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        try {
            String scheme = uri.getScheme();
            // schemes are case-insensitive, according to RFC 2396
            if (scheme != null) scheme = scheme.toLowerCase();
            IDataHelper.put(cursor, "scheme", scheme, false);

            IData query = null;

            if (uri.isOpaque()) {
                String schemeSpecificPart = uri.getRawSchemeSpecificPart();
                if (schemeSpecificPart != null) {
                    if (schemeSpecificPart.contains(URI_QUERY_DELIMITER)) {
                        query = URIQueryHelper.parse(schemeSpecificPart.substring(schemeSpecificPart.indexOf(URI_QUERY_DELIMITER) + 1), true);
                        schemeSpecificPart = schemeSpecificPart.substring(0, schemeSpecificPart.indexOf(URI_QUERY_DELIMITER));
                    }
                    IDataHelper.put(cursor, "body", decode(schemeSpecificPart));
                }
            } else {
                String authority = uri.getAuthority();

                if (authority != null) {
                    // hosts are case-insensitive, according to RFC 2396, but we will preserve the case to be safe
                    String host = uri.getHost();

                    IData authorityDocument = IDataFactory.create();
                    IDataCursor ac = authorityDocument.getCursor();

                    try {
                        if (host == null) {
                            // if host is null, then this URI must be registry-based
                            IDataHelper.put(ac, "registry", authority);
                        } else {
                            IData server = IDataFactory.create();
                            IDataCursor sc = server.getCursor();
                            try {
                                // parse user-info component according to the format user:[password]
                                String user = uri.getUserInfo();
                                String password = null;
                                if (user != null && user.contains(URI_USER_PASSWORD_DELIMITER)) {
                                    password = user.substring(user.indexOf(URI_USER_PASSWORD_DELIMITER) + 1);
                                    user = user.substring(0, user.indexOf(URI_USER_PASSWORD_DELIMITER));
                                }
                                IDataHelper.put(sc, "user", user, false);
                                IDataHelper.put(sc, "password", password, false);
                                IDataHelper.put(sc, "host", host, false);

                                // if port is -1, then it wasn't specified in the URI
                                int port = uri.getPort();
                                if (port >= 0) IDataHelper.put(sc, "port", port, String.class);
                            } finally {
                                sc.destroy();
                            }

                            IDataHelper.put(ac, "server", server);
                        }

                        IDataUtil.put(cursor, "authority", authorityDocument);
                    } finally {
                        ac.destroy();
                    }
                }

                String path = uri.getPath();
                if (path != null && !path.equals("")) {
                    String[] paths = URIPathHelper.parse(path);

                    if (paths != null && paths.length > 0) {
                        String file = null;
                        if (!path.endsWith(URI_PATH_DELIMITER)) {
                            file = ArrayHelper.get(paths, -1);
                            paths = ArrayHelper.drop(paths, -1);
                        }

                        IDataHelper.put(cursor, "path", paths, false);
                        IDataHelper.put(cursor, "path.absolute?", path.startsWith(URI_PATH_DELIMITER), String.class);
                        IDataHelper.put(cursor, "file", file, false);
                    }
                }

                query = URIQueryHelper.parse(uri.getRawQuery(), true);
            }

            IDataHelper.put(cursor, "query", query, false);
            IDataHelper.put(cursor, "fragment", uri.getFragment(), false);
            IDataHelper.put(cursor, "absolute?", uri.isAbsolute(), String.class);
            IDataHelper.put(cursor, "opaque?", uri.isOpaque(), String.class);
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Converts an IData document to a URI object.
     *
     * @param input                 An IData containing a specific URI structure.
     * @return                      The resulting URI object representing the given IData.
     * @throws URISyntaxException   If the given IData document represents an invalid URI.
     */
    public static URI fromIData(IData input) throws URISyntaxException {
        if (input == null) return null;

        URI uri;
        IDataCursor cursor = input.getCursor();

        try {
            String scheme = IDataUtil.getString(cursor, "scheme");
            // schemes are case-insensitive, according to RFC 2396
            if (scheme != null) scheme = scheme.toLowerCase();

            String fragment = IDataHelper.get(cursor, "fragment", String.class);
            IData queryDocument = IDataHelper.get(cursor, "query", IData.class);
            String query = URIQueryHelper.emit(queryDocument, false);

            String body = IDataHelper.get(cursor, "body", String.class);
            IData authority = IDataHelper.get(cursor, "authority", IData.class);
            String[] paths = IDataHelper.get(cursor, "path", String[].class);
            boolean absolutePath = IDataHelper.getOrDefault(cursor, "path.absolute?", Boolean.class, true);
            String file = IDataHelper.get(cursor, "file", String.class);

            if (body == null && !(authority == null && paths == null && file == null)) {
                // create an hierarchical URI
                String path;

                if ((paths != null || file != null) && (absolutePath || authority != null)) {
                    path = URI_PATH_DELIMITER;
                } else {
                    path = null;
                }

                if (paths != null && paths.length > 0) {
                    path = (path == null ? "" : path) + ArrayHelper.join(paths, URI_PATH_DELIMITER);
                }

                if (file == null) {
                    if (path != null && !path.equals(URI_PATH_DELIMITER)) {
                        path = path + URI_PATH_DELIMITER;
                    }
                } else {
                    if (path == null) {
                        path = file;
                    } else if (path.equals(URI_PATH_DELIMITER)) {
                        path = path + file;
                    } else {
                        path = path + URI_PATH_DELIMITER + file;
                    }
                }

                if (authority == null) {
                    uri = new URI(scheme, null, path, query, fragment);
                } else {
                    IDataCursor ac = authority.getCursor();
                    String registry = IDataHelper.get(ac, "registry", String.class);
                    IData server = IDataHelper.get(ac, "server", IData.class);
                    ac.destroy();

                    if (registry == null) {
                        IDataCursor sc = server.getCursor();

                        // hosts are case-insensitive, according to RFC 2396, but we will preserve the case to be safe
                        String host = IDataHelper.get(sc, "host", String.class);
                        int port = IDataHelper.getOrDefault(sc, "port", Integer.class, -1);
                        // remove the explicit port if it is the default port for the scheme
                        if (port != -1 && isDefaultPort(scheme, port)) {
                            port = -1;
                        }

                        String userinfo = IDataHelper.get(sc, "user", String.class);
                        if (userinfo != null) {
                            if (userinfo.equals("")) {
                                userinfo = null; // ignore empty strings
                            } else {
                                String password = IDataHelper.get(sc, "password", String.class);
                                if (password != null && !(password.equals(""))) userinfo = userinfo + ":" + password;
                            }
                        }

                        sc.destroy();
                        uri = new URI(scheme, userinfo, host, port, path, query, fragment);
                    } else {
                        uri = new URI(scheme, registry, path, query, fragment);
                    }
                }
            } else if (query != null) {
                uri = new URI(scheme, (body == null ? "" : body) + URI_QUERY_DELIMITER + query, fragment);
            } else if (body != null) {
                uri = new URI(scheme, body, fragment);
            } else {
                uri = new URI("");
            }
        } finally {
            cursor.destroy();
        }

        return uri;
    }

    /**
     * Parses a given string to a URI object.
     *
     * @param input                 A URI string to be parsed.
     * @return                      The parsed URI object.
     */
    public static URI fromString(String input) throws URISyntaxException {
        if (input == null) return null;

        // treat Windows UNC file URIs as server-based rather than path-based
        if (input.toLowerCase().startsWith("file:////")) {
            input = "file://" + input.substring(9, input.length());
        }

        return new URI(input);
    }

    /**
     * Emits a URI given as an IData document as a string.
     *
     * @param uri                   A URI object to be serialized.
     * @return                      The serialized URI.
     */
    public static String toString(URI uri) {
        if (uri == null) return null;

        String output = uri.toASCIIString();

        // support Windows UNC file URIs, and work-around Java bug with
        // file URIs where scheme is followed by ':/' rather than '://'
        if (output.startsWith("file:") && uri.getHost() == null) {
            output = "file://" + output.substring(5, output.length());
        }

        return output;
    }

    /**
     * Returns the default port for the given scheme.
     *
     * @param scheme    The URI scheme.
     * @return          The default port for the given scheme, if defined.
     */
    public static Integer getDefaultPort(String scheme) {
        Integer defaultPort = null;
        if (scheme != null) {
            defaultPort = DEFAULT_PORTS.get(scheme.toLowerCase());
        }
        return defaultPort;
    }

    /**
     * Returns true if the given port is the default port for the given scheme.
     *
     * @param scheme    The URI scheme.
     * @param port      The port to check.
     * @return          True if the given port is the default port for the given scheme.
     */
    public static boolean isDefaultPort(String scheme, int port) {
        boolean isDefaultPort = false;
        if (scheme != null) {
            String key = scheme.toLowerCase();
            isDefaultPort = DEFAULT_PORTS.containsKey(key) && DEFAULT_PORTS.get(key) == port;
        }
        return isDefaultPort;
    }

    /**
     * A map of schemes and their respective default ports, seeded with some well-known schemes sourced from the IANA
     * [Uniform Resource Identifier (URI) Schemes][1] and [Service Name and Transport Protocol Port Number Registry][2]
     * via Mahmoud Hashemi's [scheme_port_map.json][3].
     *
     * [1]: https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml
     * [2]: https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml
     * [3]: https://gist.github.com/mahmoud/2fe281a8daaff26cfe9c15d2c5bf5c8b
     */
    private static final Map<String, Integer> DEFAULT_PORTS = new TreeMap<String, Integer>();
    static {
        DEFAULT_PORTS.put("acap", 674);
        DEFAULT_PORTS.put("afp", 548);
        DEFAULT_PORTS.put("dict", 2628);
        DEFAULT_PORTS.put("dns", 53);
        DEFAULT_PORTS.put("ftp", 21);
        DEFAULT_PORTS.put("ftps", 990);
        DEFAULT_PORTS.put("git", 9418);
        DEFAULT_PORTS.put("gopher", 70);
        DEFAULT_PORTS.put("http", 80);
        DEFAULT_PORTS.put("https", 443);
        DEFAULT_PORTS.put("imap", 143);
        DEFAULT_PORTS.put("ipp", 631);
        DEFAULT_PORTS.put("ipps", 631);
        DEFAULT_PORTS.put("irc", 194);
        DEFAULT_PORTS.put("ircs", 6697);
        DEFAULT_PORTS.put("ldap", 389);
        DEFAULT_PORTS.put("ldaps", 636);
        DEFAULT_PORTS.put("mms", 1755);
        DEFAULT_PORTS.put("msrp", 2855);
        DEFAULT_PORTS.put("mtqp", 1038);
        DEFAULT_PORTS.put("nfs", 111);
        DEFAULT_PORTS.put("nntp", 119);
        DEFAULT_PORTS.put("nntps", 563);
        DEFAULT_PORTS.put("pop", 110);
        DEFAULT_PORTS.put("prospero", 1525);
        DEFAULT_PORTS.put("redis", 6379);
        DEFAULT_PORTS.put("rsync", 873);
        DEFAULT_PORTS.put("rtsp", 554);
        DEFAULT_PORTS.put("rtsps", 322);
        DEFAULT_PORTS.put("rtspu", 5005);
        DEFAULT_PORTS.put("scp", 22);
        DEFAULT_PORTS.put("sftp", 22);
        DEFAULT_PORTS.put("smb", 445);
        DEFAULT_PORTS.put("snmp", 161);
        DEFAULT_PORTS.put("ssh", 22);
        DEFAULT_PORTS.put("svn", 3690);
        DEFAULT_PORTS.put("telnet", 23);
        DEFAULT_PORTS.put("ventrilo", 3784);
        DEFAULT_PORTS.put("vnc", 5900);
        DEFAULT_PORTS.put("wais", 210);
        DEFAULT_PORTS.put("ws", 80);
        DEFAULT_PORTS.put("wss", 443);
    }
}
