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

package permafrost.tundra.uri;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import permafrost.tundra.array.ArrayHelper;
import permafrost.tundra.exception.BaseException;
import permafrost.tundra.io.EncodingException;
import permafrost.tundra.io.ParseException;

public class URIHelper {
    /**
     * The default character encoding used for URI strings.
     */
    public static final String DEFAULT_CHARACTER_ENCODING = "US-ASCII";

    /**
     * Disallow instantiation of this class.
     */
    private URIHelper() {}

    /**
     * Parses a URI string into an IData representation.
     * @param input
     * @return
     * @throws BaseException
     */
    public static IData parse(String input) throws BaseException {
        if (input == null) return null;

        // treat Windows UNC file URIs as server-based rather than path-based
        if (input.toLowerCase().startsWith("file:////")) {
            input = "file://" + input.substring(9, input.length());
        }

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        try {
            java.net.URI uri = new java.net.URI(input);
            uri.normalize();

            String scheme = uri.getScheme();
            // schemes are case-insensitive, according to RFC 2396
            if (scheme != null) scheme = scheme.toLowerCase();
            if (scheme != null) IDataUtil.put(cursor, "scheme", scheme);

            IData query = null;
            String ssp = uri.getSchemeSpecificPart();

            if (uri.isOpaque()) {
                if (ssp.contains("?")) {
                    query = QueryHelper.parse(ssp.substring(ssp.indexOf("?") + 1, ssp.length()), false);
                    ssp = ssp.substring(0, ssp.indexOf("?"));
                }
                if (ssp != null) IDataUtil.put(cursor, "body", ssp);
            } else {
                String authority = uri.getAuthority();
                if (authority != null) {
                    IData server = IDataFactory.create();
                    IDataCursor sc = server.getCursor();

                    // parse user-info component according to the format user:[password]
                    String user = uri.getUserInfo();
                    String password = null;
                    String userInfoSeparator = ":";
                    if (user != null && user.contains(userInfoSeparator)) {
                        password = user.substring(user.indexOf(userInfoSeparator) + 1, user.length());
                        user = user.substring(0, user.indexOf(userInfoSeparator));
                    }
                    if (user != null) IDataUtil.put(sc, "user", user);
                    if (password != null) IDataUtil.put(sc, "password", password);

                    // hosts are case-insensitive, according to RFC 2396
                    String host = uri.getHost();
                    if (host != null) IDataUtil.put(sc, "host", host.toLowerCase());

                    // if port is -1, then it wasn't specified in the URI
                    int port = uri.getPort();
                    if (port >= 0) IDataUtil.put(sc, "port", "" + uri.getPort());

                    sc.destroy();

                    // if host is null, then this URI must be registry-based
                    IData authorityDocument = IDataFactory.create();
                    IDataCursor ac = authorityDocument.getCursor();
                    if (host == null) {
                        IDataUtil.put(ac, "registry", authority);
                    } else {
                        IDataUtil.put(ac, "server", server);
                    }
                    ac.destroy();

                    IDataUtil.put(cursor, "authority", authorityDocument);
                }

                String path = uri.getPath();
                String[] paths = PathHelper.parse(path);

                String file = null;
                if (!path.endsWith("/")) {
                    file = ArrayHelper.get(paths, -1);
                    paths = ArrayHelper.drop(paths, -1);
                }

                if (paths != null && paths.length > 0) IDataUtil.put(cursor, "path", paths);
                if (file != null && !file.equals("")) IDataUtil.put(cursor, "file", file);

                query = QueryHelper.parse(uri.getRawQuery(), true);
            }

            if (query != null) IDataUtil.put(cursor, "query", query);

            String fragment = uri.getFragment();
            if (fragment != null) IDataUtil.put(cursor, "fragment", fragment);

            IDataUtil.put(cursor, "absolute?", "" + uri.isAbsolute());
            IDataUtil.put(cursor, "opaque?", "" + uri.isOpaque());
        } catch(java.net.URISyntaxException ex) {
            throw new ParseException(ex);
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Emits a URI given as an IData document as a string.
     * @param input
     * @return
     * @throws BaseException
     */
    public static String emit(IData input) throws BaseException {
        if (input == null) return null;

        String output = null;
        IDataCursor cursor = input.getCursor();

        try {
            java.net.URI uri = null;
            String type = IDataUtil.getString(cursor, "type");

            String scheme = IDataUtil.getString(cursor, "scheme");
            // schemes are case-insensitive, according to RFC 2396
            if (scheme != null) scheme = scheme.toLowerCase();

            String fragment = IDataUtil.getString(cursor, "fragment");
            IData queryDocument = IDataUtil.getIData(cursor, "query");
            String query = QueryHelper.emit(queryDocument, true);

            String body = IDataUtil.getString(cursor, "body");
            IData authority = IDataUtil.getIData(cursor, "authority");
            String[] paths = IDataUtil.getStringArray(cursor, "path");
            String file = IDataUtil.getString(cursor, "file");

            if (body == null && !(authority == null && paths == null && file == null)) {
                // create an hierarchical URI
                String path = "";
                if (paths != null) {
                    path = "/" + ArrayHelper.join(paths, "/");
                }
                path = path + "/";
                if (file != null) {
                    path = path + file;
                }

                if (authority == null) {
                    uri = new java.net.URI(scheme, null, path, null, null);
                } else {
                    IDataCursor ac = authority.getCursor();
                    String registry = IDataUtil.getString(ac, "registry");
                    IData server = IDataUtil.getIData(ac, "server");
                    ac.destroy();

                    if (registry == null) {
                        IDataCursor sc = server.getCursor();
                        String host = IDataUtil.getString(sc, "host");
                        // hosts are case-insensitive, according to RFC 2396
                        if (host != null) host = host.toLowerCase();

                        String portString = IDataUtil.getString(sc, "port");
                        int port = -1;
                        if (portString != null) port = Integer.parseInt(portString);

                        String userinfo = IDataUtil.getString(sc, "user");
                        if (userinfo != null && !(userinfo.equals(""))) {
                            String password = IDataUtil.getString(sc, "password");
                            if (password != null && !(password.equals(""))) userinfo = userinfo + ":" + password;
                        } else {
                            userinfo = null; // ignore empty strings
                        }

                        sc.destroy();
                        uri = new java.net.URI(scheme, userinfo, host, port, path, null, null);
                    } else {
                        uri = new java.net.URI(scheme, registry, path, null, null);
                    }
                }
            } else {
                uri = new java.net.URI(scheme, body, null);
            }
            output = uri.normalize().toASCIIString();
            if (query != null) output = output + "?" + query;
            if (fragment != null && !(fragment.equals(""))) output = output + "#" + encode(fragment);

            // support Windows UNC file URIs, and work-around java bug with
            // file URIs where scheme is followed by ':/' rather than '://'
            if (output.startsWith("file:") && uri.getHost() == null) {
                output = "file://" + output.substring(5, output.length());
            }
        } catch(java.net.URISyntaxException ex) {
            throw new ParseException(ex);
        } finally {
            cursor.destroy();
        }

        return output;
    }

    /**
     * Normalizes a URI string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URI.html#normalize().
     * @param input
     * @return
     * @throws BaseException
     */
    public static String normalize(String input) throws BaseException {
        return emit(parse(input));
    }

    /**
     * URL encodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     * @param input
     * @return
     * @throws BaseException
     */
    public static String encode(String input) throws BaseException {
        return encode(input, DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * URL encodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     * @param input
     * @param encoding
     * @return
     * @throws BaseException
     */
    public static String encode(String input, String encoding) throws BaseException {
        if (input == null) return null;
        if (encoding == null) encoding = DEFAULT_CHARACTER_ENCODING;

        String output;
        try {
            output =  java.net.URLEncoder.encode(input, encoding).replace("+", "%20");
        } catch(java.io.UnsupportedEncodingException ex) {
            throw new EncodingException(ex);
        }
        return output;
    }

    /**
     * URL encodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     * @param input
     * @return
     * @throws BaseException
     */
    public static String[] encode(String[] input) throws BaseException {
        return encode(input, DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * URL encodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLEncoder.html.
     * @param input
     * @param encoding
     * @return
     * @throws BaseException
     */
    public static String[] encode(String[] input, String encoding) throws BaseException {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = encode(input[i], encoding);
        }

        return output;
    }

    /**
     * URL decodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     * @param input
     * @return
     * @throws BaseException
     */
    public static String decode(String input) throws BaseException {
        return decode(input, DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * URL decodes a string, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     * @param input
     * @param encoding
     * @return
     * @throws BaseException
     */
    public static String decode(String input, String encoding) throws BaseException {
        if (input == null) return null;
        if (encoding == null) encoding = DEFAULT_CHARACTER_ENCODING;

        String output = null;
        try {
            output = java.net.URLDecoder.decode(input, encoding);
        } catch(java.io.UnsupportedEncodingException ex) {
            throw new EncodingException(ex);
        }
        return output;
    }

    /**
     * URL decodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     * @param input
     * @return
     * @throws BaseException
     */
    public static String[] decode(String[] input) throws BaseException {
        return decode(input, DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * URL decodes a string list, refer: http://docs.oracle.com/javase/6/docs/api/java/net/URLDecoder.html.
     * @param input
     * @param encoding
     * @return
     * @throws BaseException
     */
    public static String[] decode(String[] input, String encoding) throws BaseException {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = decode(input[i], encoding);
        }

        return output;
    }
}
