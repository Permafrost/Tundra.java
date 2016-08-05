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

package permafrost.tundra.server;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.ServiceSetupException;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSServiceType;
import com.wm.net.HttpHeader;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.net.http.HTTPHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * A collection of convenience methods for working with webMethods Integration Server services.
 */
public final class ServiceHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ServiceHelper() {}

    /**
     * Creates a new service in the given package with the given name.
     *
     * @param packageName The name of the package to create the service in.
     * @param serviceName The fully-qualified name of the service to be created.
     * @param type        The type of service to be created.
     * @param subtype     The subtype of service to be created.
     * @throws ServiceException If an error creating the service occurs.
     */
    private static void create(String packageName, String serviceName, String type, String subtype) throws ServiceException {
        if (!PackageHelper.exists(packageName)) {
            throw new IllegalArgumentException("package does not exist: " + packageName);
        }
        if (NodeHelper.exists(serviceName)) throw new IllegalArgumentException("node already exists: " + serviceName);

        NSName service = NSName.create(serviceName);

        if (type == null) type = NSServiceType.SVC_FLOW;
        if (subtype == null) subtype = NSServiceType.SVCSUB_UNKNOWN;
        NSServiceType serviceType = NSServiceType.create(type, subtype);

        try {
            ServerAPI.registerService(packageName, service, true, serviceType, null, null, null);
        } catch (ServiceSetupException ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Creates a new flow service in the given package with the given name.
     *
     * @param packageName The name of the package to create the service in.
     * @param serviceName The fully-qualified name of the service to be created.
     * @throws ServiceException If an error creating the service occurs.
     */
    public static void create(String packageName, String serviceName) throws ServiceException {
        create(packageName, serviceName, null, null);
    }

    /**
     * Returns information about the service with the given name.
     *
     * @param serviceName   The name of the service to be reflected on.
     * @return              An IData document containing information about the service.
     */
    public static IData reflect(String serviceName) {
        if (serviceName == null) return null;

        BaseService service = Namespace.getService(NSName.create(serviceName));
        if (service == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        IDataUtil.put(cursor, "name", serviceName);
        IDataUtil.put(cursor, "type", service.getServiceType().getType());
        IDataUtil.put(cursor, "package", service.getPackageName());

        String description = service.getComment();
        if (description != null) IDataUtil.put(cursor, "description", description);

        cursor.destroy();

        return output;
    }

    /**
     * Returns the invoking service.
     *
     * @return The invoking service.
     */
    public static NSService self() {
        return Service.getCallingService();
    }

    /**
     * Sets the HTTP response status, headers, and body for the current service invocation.
     *
     * @param code        The HTTP response status code to be returned.
     * @param message     The HTTP response status message to be returned; if null, the standard message for the given
     *                    code will be used.
     * @param headers     The HTTP headers to be returned; if null, no custom headers will be added to the response.
     * @param content     The HTTP response body to be returned.
     * @param contentType The MIME content type of the response body being returned.
     * @param charset     The character set used if a text response is being returned.
     * @throws ServiceException If an I/O error occurs.
     */
    public static void respond(int code, String message, IData headers, InputStream content, String contentType, Charset charset) throws ServiceException {
        try {
            HttpHeader response = Service.getHttpResponseHeader();

            if (response == null) {
                // service was not invoked via HTTP, so throw an exception for HTTP statuses >= 400
                if (code >= 400) ExceptionHelper.raise(StringHelper.normalize(content, charset));
            } else {
                setResponseStatus(response, code, message);
                setContentType(response, contentType, charset);
                setHeaders(response, headers);
                setResponseBody(response, content);
            }
        } catch (IOException ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Sets the response body in the given HTTP response.
     *
     * @param response The HTTP response to set the response body in.
     * @param content  The content to set the response body to.
     * @throws ServiceException If an I/O error occurs.
     */
    private static void setResponseBody(HttpHeader response, InputStream content) throws ServiceException {
        if (response == null) return;

        try {
            if (content == null) content = new ByteArrayInputStream(new byte[0]);
            Service.setResponse(BytesHelper.normalize(content));
        } catch (IOException ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Sets the response status code and message in the given HTTP response.
     *
     * @param response The HTTP response to set the response status in.
     * @param code     The response status code.
     * @param message  The response status message.
     */
    private static void setResponseStatus(HttpHeader response, int code, String message) {
        if (response != null) {
            if (message == null) message = HTTPHelper.getResponseStatusMessage(code);
            response.setResponse(code, message);
        }
    }

    /**
     * Sets the Content-Type header in the given HTTP response.
     *
     * @param response    The HTTP response to set the header in.
     * @param contentType The MIME content type.
     * @param charset     The character set used by the content, or null if not applicable.
     * @throws ServiceException If the MIME content type is malformed.
     */
    private static void setContentType(HttpHeader response, String contentType, Charset charset) throws ServiceException {
        if (contentType == null) contentType = MIMETypeHelper.DEFAULT_MIME_TYPE_STRING;

        try {
            MimeType mimeType = new MimeType(contentType);
            if (charset == null) mimeType.setParameter("charset", charset.displayName());

            setHeader(response, "Content-Type", mimeType);
        } catch (MimeTypeParseException ex) {
            ExceptionHelper.raise(ex);
        }
    }

    /**
     * Sets all HTTP header with the given keys to their associated values from the given IData document in the given
     * HTTP response.
     *
     * @param response The HTTP response to add the header to.
     * @param headers  An IData document containing key value pairs to be set as headers in the given response.
     */
    private static void setHeaders(HttpHeader response, IData headers) {
        IDataMap map = new IDataMap(headers);

        for (Map.Entry<String, Object> entry : map) {
            setHeader(response, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the HTTP header with the given key to the given value in the given HTTP response.
     *
     * @param response The HTTP response to add the header to.
     * @param key      The header's key.
     * @param value    The header's value.
     */
    private static void setHeader(HttpHeader response, String key, Object value) {
        if (response != null && key != null) {
            response.clearField(key);
            if (value != null) response.addField(key, value.toString());
        }
    }
}
