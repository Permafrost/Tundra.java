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
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
import com.wm.app.b2b.server.ServiceSetupException;
import com.wm.app.b2b.server.ServiceThread;
import com.wm.app.b2b.server.ns.Namespace;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.NSServiceType;
import com.wm.net.HttpHeader;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataMap;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.math.NormalDistributionEstimator;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.net.http.HTTPHelper;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A collection of convenience methods for working with webMethods Integration Server services.
 */
public final class ServiceHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ServiceHelper() {}

    /**
     * Returns a copy of the call stack for the current invocation.
     *
     * @return A copy of the call stack for the current invocation.
     */
    @SuppressWarnings("unchecked")
    public static List<NSService> getCallStack() {
        List<NSService> stack = (List<NSService>)InvokeState.getCurrentState().getCallStack();
        if (stack == null) stack = Collections.emptyList();
        return new ArrayList<NSService>(stack);
    }

    /**
     * Returns true if the calling service is the top-level initiating service of the current thread.
     *
     * @return True if the calling service is the top-level initiating service of the current thread.
     */
    public static boolean isInitiator() {
        return getCallStack().size() <= 1;
    }

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
        for (Map.Entry<String, Object> entry : IDataMap.of(headers)) {
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

    /**
     * Returns true if the given service exists in Integration Server.
     *
     * @param service   The service to check existence of.
     * @return          True if the given service exists in Integration Server.
     */
    public static boolean exists(String service) {
        boolean exists = false;
        try {
            exists = exists(service, false);
        } catch(ServiceException ex) {
            // ignore exceptions
        }
        return exists;
    }

    /**
     * Returns true if the given service exists in Integration Server.
     *
     * @param service           The service to check existence of.
     * @param raise             If true and the service does not exist, an exception will be thrown.
     * @return                  True if the given service exists in Integration Server.
     * @throws ServiceException If raise is true and the given service does not exist.
     */
    public static boolean exists(String service, boolean raise) throws ServiceException {
        boolean exists = NodeHelper.exists(service) && "service".equals(NodeHelper.getNodeType(service).toString());
        if (raise && !exists) ExceptionHelper.raise("Service does not exist: " + service);
        return exists;
    }

    /**
     * Invokes the given service with the given pipeline synchronously.
     *
     * @param service           The service to be invoked.
     * @param pipeline          The input pipeline used when invoking the service.
     * @return                  The output pipeline returned by the service invocation.
     * @throws ServiceException If the service throws an exception while being invoked.
     */
    public static IData invoke(String service, IData pipeline) throws ServiceException {
        return invoke(service, pipeline, true);
    }

    /**
     * Invokes the given service with the given pipeline synchronously.
     *
     * @param service           The service to be invoked.
     * @param pipeline          The input pipeline used when invoking the service.
     * @param raise             If true will rethrow exceptions thrown by the invoked service.
     * @return                  The output pipeline returned by the service invocation.
     * @throws ServiceException If raise is true and the service throws an exception while being invoked.
     */
    public static IData invoke(String service, IData pipeline, boolean raise) throws ServiceException {
        return invoke(service, pipeline, raise, true);
    }

    /**
     * Invokes the given service with the given pipeline synchronously.
     *
     * @param service           The service to be invoked.
     * @param pipeline          The input pipeline used when invoking the service.
     * @param raise             If true will rethrow exceptions thrown by the invoked service.
     * @param clone             If true the pipeline will first be cloned before being used by the invocation.
     * @return                  The output pipeline returned by the service invocation.
     * @throws ServiceException If raise is true and the service throws an exception while being invoked.
     */
    public static IData invoke(String service, IData pipeline, boolean raise, boolean clone) throws ServiceException {
        if (service != null) {
            if (pipeline == null) pipeline = IDataFactory.create();

            try {
                IDataUtil.merge(Service.doInvoke(NSName.create(service), normalize(pipeline, clone)), pipeline);
            } catch (Exception exception) {
                if (raise) {
                    ExceptionHelper.raise(exception);
                } else {
                    pipeline = addExceptionToPipeline(pipeline, exception);
                }
            }
        }

        return pipeline;
    }

    /**
     * Executes a list of services in the order specified.
     *
     * @param services          The list of services to be invoked.
     * @param pipeline          The input pipeline passed to the first service in the chain.
     * @return                  The output pipeline returned by the final service in the chain.
     * @throws ServiceException If an exception is thrown by one of the invoked services.
     */
    public static IData chain(String[] services, IData pipeline) throws ServiceException {
        return chain(services == null ? null : Arrays.asList(services), pipeline);
    }

    /**
     * Executes a list of services in the order specified.
     *
     * @param services          The list of services to be invoked.
     * @param pipeline          The input pipeline passed to the first service in the chain.
     * @return                  The output pipeline returned by the final service in the chain.
     * @throws ServiceException If an exception is thrown by one of the invoked services.
     */
    public static IData chain(Iterable<String> services, IData pipeline) throws ServiceException {
        if (services != null) {
            for (String service : services) {
                pipeline = ServiceHelper.invoke(service, pipeline);
            }
        }
        return pipeline;
    }

    /**
     * Invokes the given service with the given pipeline asynchronously (in another thread).
     *
     * @param service           The service to be invoked.
     * @param pipeline          The input pipeline used when invoking the service.
     * @return                  The thread on which the service is being invoked.
     */
    public static ServiceThread fork(String service, IData pipeline) {
        if (service == null) return null;
        return Service.doThreadInvoke(NSName.create(service), normalize(pipeline));
    }

    /**
     * Waits for an asynchronously invoked service to complete.
     *
     * @param serviceThread     The service thread to wait on to finish.
     * @return                  The output pipeline from the service invocation executed by the given thread.
     * @throws ServiceException If an error occurs when waiting on the thread to finish.
     */
    public static IData join(ServiceThread serviceThread) throws ServiceException {
        return join(serviceThread, true);
    }

    /**
     * Waits for an asynchronously invoked service to complete.
     *
     * @param serviceThread     The service thread to wait on to finish.
     * @param raise             If true rethrows any exception thrown by the invoked service.
     * @return                  The output pipeline from the service invocation executed by the given thread.
     * @throws ServiceException If raise is true and an error occurs when waiting on the thread to finish.
     */
    public static IData join(ServiceThread serviceThread, boolean raise) throws ServiceException {
        IData pipeline = null;

        if (serviceThread != null) {
            try {
                pipeline = serviceThread.getIData();
            } catch (Throwable exception) {
                if (raise) {
                    ExceptionHelper.raise(exception);
                } else {
                    pipeline = addExceptionToPipeline(null, exception);
                }
            }
        }

        return pipeline;
    }

    /**
     * Invokes the given service a given number of times, and returns execution duration statistics. Exceptions thrown
     * by the service are ignored / suppressed.
     *
     * @param service   The service to benchmark.
     * @param pipeline  The input pipeline used when invoking the service.
     * @param count     The sample count, or in other words the number of times to invoke the service.
     * @return          Execution duration statistics generated by benchmarking the given service.
     */
    public static NormalDistributionEstimator benchmark(String service, IData pipeline, int count) {
        NormalDistributionEstimator estimator = null;

        try {
            estimator = benchmark(service, pipeline, count, false);
        } catch(ServiceException ex) {
            // ignore exceptions
        }

        return estimator;
    }

    /**
     * Invokes the given service a given number of times, and returns execution duration statistics.
     *
     * @param service           The service to benchmark.
     * @param pipeline          The input pipeline used when invoking the service.
     * @param count             The sample count, or in other words the number of times to invoke the service.
     * @param raise             If true, exceptions thrown by the service will abort the benchmark.
     * @return                  Execution duration statistics generated by benchmarking the given service.
     * @throws ServiceException If the invoked service throws an exception and raise is true.
     */
    public static NormalDistributionEstimator benchmark(String service, IData pipeline, int count, boolean raise) throws ServiceException {
        if (service == null) throw new NullPointerException("service must not be null");
        if (count <= 0) throw new IllegalArgumentException("count must be greater than zero");
        if (pipeline == null) pipeline = IDataFactory.create();

        NormalDistributionEstimator estimator = new NormalDistributionEstimator("ms");

        exists(service, true);

        for (int i = 0; i < count; i++) {
            IData scope = IDataUtil.clone(pipeline);

            long start = System.currentTimeMillis();
            try {
                invoke(service, scope, raise, false);
            } finally {
                long end = System.currentTimeMillis();
                estimator.add(end - start);
            }
        }

        return estimator;
    }

    /**
     * Provides a try/catch/finally pattern for flow services.
     *
     * @param tryService        The service to be executed in the try clause of the try/catch/finally pattern.
     * @param catchService      The service to be executed in the catch clause of the try/catch/finally pattern.
     * @param finallyService    The service to be executed in the finally clause of the try/catch/finally pattern.
     * @param pipeline          The input pipeline used when invoking the services.
     * @return                  The output pipeline containing the results of the try/catch/finally pattern.
     * @throws ServiceException If the service throws an exception while being invoked, and either no catch service is
     *                          specified, or the catch service rethrows the exception.
     */
    public static IData ensure(String tryService, String catchService, String finallyService, IData pipeline) throws ServiceException {
        try {
            pipeline = invoke(tryService, pipeline);
        } catch (Throwable exception) {
            pipeline = rescue(catchService, pipeline, exception);
        } finally {
            pipeline = invoke(finallyService, pipeline);
        }

        return pipeline;
    }

    /**
     * Handles an exception using the given catch service.
     *
     * @param catchService          The service to invoke to handle the given exception.
     * @param pipeline              The input pipeline for the service.
     * @param exception             The exception to be handled.
     * @return                      The output pipeline returned by invoking the given catchService.
     * @throws ServiceException     If the given catchService encounters an error.
     */
    public static IData rescue(String catchService, IData pipeline, Throwable exception) throws ServiceException {
        if (catchService == null) {
            ExceptionHelper.raise(exception);
        } else {
            pipeline = invoke(catchService, addExceptionToPipeline(pipeline, exception));
        }

        return pipeline;
    }

    /**
     * Adds the given exception and related variables that describe the exception to the given IData pipeline.
     *
     * @param pipeline  The pipeline to add the exception to.
     * @param exception The exception to be added.
     * @return          The pipeline with the added exception.
     */
    private static IData addExceptionToPipeline(IData pipeline, Throwable exception) {
        if (pipeline == null) pipeline = IDataFactory.create();

        if (exception != null) {
            IDataCursor cursor = pipeline.getCursor();

            try {
                IDataUtil.put(cursor, "$exception", exception);
                IDataUtil.put(cursor, "$exception?", "true");
                IDataUtil.put(cursor, "$exception.class", exception.getClass().getName());
                IDataUtil.put(cursor, "$exception.message", exception.getMessage());

                InvokeState invokeState = InvokeState.getCurrentState();
                if (invokeState != null) {
                    IData exceptionInfo = IDataHelper.duplicate(invokeState.getErrorInfoFormatted(), true);
                    if (exceptionInfo != null) {
                        IDataCursor ec = exceptionInfo.getCursor();
                        String exceptionService = IDataUtil.getString(ec, "service");
                        if (exceptionService != null) {
                            IDataUtil.put(cursor, "$exception.service", exceptionService);
                            BaseService baseService = Namespace.getService(NSName.create(exceptionService));
                            if (baseService != null) {
                                String packageName = baseService.getPackageName();
                                if (packageName != null) {
                                    IDataUtil.put(ec, "package", packageName);
                                    IDataUtil.put(cursor, "$exception.package", packageName);
                                }
                            }
                        }
                        ec.destroy();
                        IDataUtil.put(cursor, "$exception.info", exceptionInfo);
                    }
                }

                IDataUtil.put(cursor, "$exception.stack", ExceptionHelper.getStackTrace(exception));
            } finally {
                cursor.destroy();
            }
        }

        return pipeline;
    }

    /**
     * Returns a new IData if the given pipeline is null, otherwise returns a clone of the given pipeline.
     *
     * @param pipeline  The pipeline to be normalized.
     * @return          The normalized pipeline.
     */
    private static IData normalize(IData pipeline) {
        return normalize(pipeline, true);
    }

    /**
     * Returns a new IData if the given pipeline is null, otherwise returns a clone of the given pipeline.
     *
     * @param pipeline  The pipeline to be normalized.
     * @param clone     If true the pipeline will be cloned, otherwise it will be returned as is.
     * @return          The normalized pipeline.
     */
    private static IData normalize(IData pipeline, boolean clone) {
        if (pipeline == null) {
            pipeline = IDataFactory.create();
        } else if (clone) {
            pipeline = IDataUtil.clone(pipeline);
        }
        return pipeline;
    }
}
