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

package permafrost.tundra.server.invoke;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ProtocolInfoIf;
import com.wm.app.b2b.server.ProtocolState;
import com.wm.app.b2b.server.ServerAPI;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.util.ServerException;
import permafrost.tundra.configuration.ConfigurationManager;
import permafrost.tundra.content.ContentParser;
import permafrost.tundra.content.DuplicateException;
import permafrost.tundra.content.MalformedException;
import permafrost.tundra.content.UnsupportedException;
import permafrost.tundra.content.ValidationException;
import permafrost.tundra.content.ValidationResult;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.flow.PipelineHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.SecurityException;
import permafrost.tundra.mime.MIMEClassification;
import permafrost.tundra.mime.MIMETypeHelper;
import permafrost.tundra.mime.MediaRange;
import permafrost.tundra.server.InvokeStateHelper;
import permafrost.tundra.server.ProtocolStateHelper;
import permafrost.tundra.server.ServerLogHelper;
import permafrost.tundra.server.ServerLogLevel;
import permafrost.tundra.server.ServiceHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import javax.activation.MimeType;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Automatically serializes REST service output in the negotiated Content-Type to be returned to client.
 */
public class RestServiceProcessor extends AbstractInvokeChainProcessor {
    protected static class RegistryKey {
        protected Thread thread;
        protected String callStack;

        /**
         * Constructs a new RegistryKey object.
         *
         * @param thread    The thread being registered.
         * @param callStack The callstack being registered.
         */
        public RegistryKey(Thread thread, String callStack) {
            if (thread == null) throw new NullPointerException("thread must not be null");
            if (callStack == null) throw new NullPointerException("callstack must not be null");
            this.thread = thread;
            this.callStack = callStack;
        }

        /**
         * Returns this key's thread.
         *
         * @return this key's thread.
         */
        public Thread getThread() {
            return thread;
        }

        /**
         * Returns this key's call stack.
         *
         * @return this key's call stack.
         */
        public String getCallStack() {
            return callStack;
        }

        /**
         * Returns true if the given object is considered equivalent to this object.
         *
         * @param object    The object to compare for equivalence with.
         * @return          True if the two objects are considered equivalent.
         */
        @Override
        public boolean equals(Object object) {
            boolean result = false;

            if (object instanceof RegistryKey) {
                RegistryKey otherKey = (RegistryKey)object;
                result = this.thread.equals(otherKey.thread) && this.callStack.equals(otherKey.callStack);
            }

            return result;
        }

        /**
         * Returns a hash code for this object.
         *
         * @return A hash code for this object.
         */
        @Override
        public int hashCode() {
            return thread.hashCode() ^ callStack.hashCode();
        }

        /**
         * Returns a string representation of this object.
         *
         * @return a string representation of this object.
         */
        @Override
        public String toString() {
            return thread.toString() + ": " + callStack.toString();
        }
    }

    /**
     * The service invocation instances which have registered as REST services.
     */
    protected final ConcurrentMap<RegistryKey, IData> registry = new ConcurrentHashMap<RegistryKey, IData>();
    /**
     * The logging level to use when logging.
     */
    protected volatile ServerLogLevel logLevel = ServerLogLevel.DEFAULT_LOG_LEVEL;

    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final RestServiceProcessor INSTANCE = new RestServiceProcessor();
    }

    /**
     * Disallow instantiation of this class.
     */
    private RestServiceProcessor() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static RestServiceProcessor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Registers the current service for processing by this processor.
     *
     * @param pipeline  The pipeline to be registered.
     */
    public void register(IData pipeline) {
        if (started) {
            registry.putIfAbsent(new RegistryKey(Thread.currentThread(), ServiceHelper.getCallStackString()), IDataHelper.duplicate(pipeline));
        }
    }

    /**
     * List of automatically supported response body content types.
     */
    private static final List<MimeType> SUPPORTED_RESPONSE_CONTENT_TYPES = Arrays.asList(
        MIMETypeHelper.of("application/json"),
        MIMETypeHelper.of("text/json"),
        MIMETypeHelper.of("application/xml"),
        MIMETypeHelper.of("text/xml"),
        MIMETypeHelper.of("application/yaml"),
        MIMETypeHelper.of("text/yaml"),
        MIMETypeHelper.of("application/x-www-form-urlencoded"),
        MIMETypeHelper.of("text/html")
    );

    /**
     * Returns the negotiated content type to use for an HTTP response from the current HTTP request Accept header.
     *
     * @return the negotiated content type to use for an HTTP response from the current HTTP request Accept header.
     */
    protected static MimeType negotiateResponseContentType() {
        // determine response content type using Accept header
        String acceptedTypes = Service.getHttpHeaderField("Accept", Service.getHttpRequestHeader());
        MimeType responseContentType = MediaRange.resolve(MediaRange.parse(acceptedTypes), SUPPORTED_RESPONSE_CONTENT_TYPES);
        if (responseContentType == null) responseContentType = SUPPORTED_RESPONSE_CONTENT_TYPES.get(0);
        return responseContentType;
    }

    /**
     * Processes a service invocation:
     *
     * @param iterator          Invocation chain.
     * @param baseService       The invoked service.
     * @param pipeline          The input pipeline for the service.
     * @param serviceStatus     The status of the service invocation.
     * @throws ServerException  If the service invocation fails.
     */
    @Override
    public void process(Iterator iterator, BaseService baseService, IData pipeline, ServiceStatus serviceStatus) throws ServerException {
        long monotonicStartTime = System.nanoTime(), startTime = System.currentTimeMillis();
        boolean isRestful = false;
        RegistryKey registryKey = new RegistryKey(Thread.currentThread(), ServiceHelper.getCallStackString(false));
        Throwable exception = null;

        try {
            // note that the following line is already executing at the time the service self-registers as restful
            super.process(iterator, baseService, pipeline, serviceStatus);

            isRestful = registry.containsKey(registryKey);

            // serialize response body from output pipeline if not already explicitly set by the service
            if (isRestful && !InvokeStateHelper.hasResponseBody(InvokeStateHelper.current())) {
                IDataCursor cursor = pipeline.getCursor();
                try {
                    IData response = IDataHelper.remove(cursor, "$httpResponse", IData.class);
                    ServerLogHelper.log(this.getClass().getName(), logLevel, null, pipeline, true);
                    if (response == null) {
                        PipelineHelper.sanitize(baseService, pipeline, PipelineHelper.InputOutputSignature.OUTPUT, false);
                        ValidationResult result = PipelineHelper.validate(baseService, pipeline, PipelineHelper.InputOutputSignature.OUTPUT);
                        result.raiseIfInvalid();
                        respond(200, pipeline);
                    } else {
                        IDataCursor responseCursor = response.getCursor();
                        try {
                            IData responseHeaders = IDataHelper.get(responseCursor, "headers", IData.class);
                            int responseStatus = IDataHelper.getOrDefault(responseCursor, "responseCode", Integer.class, 200);
                            String responseReason = IDataHelper.get(responseCursor, "reasonPhrase", String.class);
                            Object responseBody = IDataHelper.first(responseCursor, Object.class, "responseString", "responseBytes", "responseStream");

                            ServiceHelper.respond(responseStatus, responseReason, responseHeaders, InputStreamHelper.normalize(responseBody), (MimeType) null, null);
                        } finally {
                            responseCursor.destroy();
                        }
                    }
                } finally {
                    cursor.destroy();
                }
            }
        } catch(Throwable ex) {
            exception = ex;

            if (isRestful || registry.containsKey(registryKey)) {
                ServerAPI.logError(exception);
                respond(ex, isRestful);
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            } else if (ex instanceof ServerException) {
                throw (ServerException)ex;
            } else {
                throw new ServerException(ex);
            }
        } finally {
            // clean up registry whether or not an exception was thrown
            IData inputPipeline = registry.remove(registryKey);
            if (inputPipeline != null) {
                if (!ServerLogLevel.OFF.equals(logLevel)) {
                    InvokeState invokeState = InvokeStateHelper.current();
                    if (invokeState != null) {
                        ProtocolInfoIf protocolInfo = invokeState.getProtocolInfoIf();
                        if (protocolInfo instanceof ProtocolState) {
                            long duration = System.nanoTime() - monotonicStartTime;
                            long endTime = System.currentTimeMillis();
                            String context = ProtocolStateHelper.serialize((ProtocolState)protocolInfo, duration, startTime, endTime, inputPipeline, pipeline);
                            ServerLogHelper.log(this.getClass().getName(), logLevel, MessageFormat.format("{0} {1} -- {2}", DurationHelper.format(duration, DurationPattern.NANOSECONDS, DurationPattern.XML_MILLISECONDS), exception == null ? "COMPLETED" : "FAILED: " + ExceptionHelper.getMessage(exception), context), null, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets an appropriate error HTTP response status code and body for the given exception.
     *
     * @param exception         The exception that was caught to be serialized.
     * @throws ServerException  If an error occurs.
     */
    protected void respond(Throwable exception, boolean isServerSide) throws ServerException {
        try {
            int responseCode = 500;

            if (!isServerSide) {
                if (exception instanceof SecurityException || "permafrost.tundra.lang.SecurityException".equals(exception.getClass().getName())) {
                    responseCode = 403;
                } else if (exception instanceof UnsupportedException || "permafrost.tundra.content.UnsupportedException".equals(exception.getClass().getName())) {
                    responseCode = 406;
                } else if (exception instanceof DuplicateException || "permafrost.tundra.content.DuplicateException".equals(exception.getClass().getName())) {
                    responseCode = 409;
                } else if (exception instanceof ValidationException || "permafrost.tundra.content.ValidationException".equals(exception.getClass().getName())) {
                    responseCode = 422;
                } else if (exception instanceof MalformedException || "permafrost.tundra.content.MalformedException".equals(exception.getClass().getName())) {
                    responseCode = 400;
                }
            }

            IData responseBody = null;

            String errorMessage = exception.getMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = ExceptionHelper.getMessage(exception);
            }
            if (!"".equals(errorMessage)) {
                responseBody = IDataHelper.create();
                IDataHelper.put(responseBody, "error/message", errorMessage);
            }

            respond(responseCode, responseBody);
        } catch(Throwable ex) {
            respond(500, null);
        }
    }

    /**
     * Sets HTTP response status code and body.
     *
     * @param statusCode        The response status code to use.
     * @param pipeline          The pipeline to be serialized as the response body.
     * @throws ServerException  If an error occurs.
     */
    protected void respond(int statusCode, IData pipeline) throws ServerException {
        try {
            MimeType contentType = negotiateResponseContentType();
            Charset charset = CharsetHelper.DEFAULT_CHARSET;
            InputStream responseBody = null;

            int pipelineSize = IDataHelper.size(pipeline);
            if (pipelineSize > 0) {
                MIMEClassification classification = MIMETypeHelper.classify(contentType);
                if (classification == MIMEClassification.XML && pipelineSize > 1) {
                    // for XML, because it requires a single root node, only serialize the first value in the pipeline
                    IData document = IDataFactory.create();
                    IDataCursor cursor = pipeline.getCursor();
                    try {
                        while(cursor.next()) {
                            String key = cursor.getKey();
                            Object value = cursor.getValue();
                            if (value != null) {
                                IDataHelper.put(document, key, value);
                                pipeline = document;
                                break;
                            }
                        }
                    } finally {
                        cursor.destroy();
                    }
                }

                if (IDataHelper.size(pipeline) > 0) {
                    ContentParser parser = new ContentParser(contentType, null, null, null, false, null);
                    charset = parser.getCharset();
                    responseBody = parser.emit(pipeline);
                }
            }

            ServiceHelper.respond(statusCode, null, null, responseBody, contentType, charset);
        } catch(Throwable ex) {
            respond(ex, true);
        }
    }

    /**
     * Registers this class as an invocation handler and starts saving pipelines.
     */
    public synchronized void start() {
        if (!started) {
            try {
                logLevel = IDataHelper.get(ConfigurationManager.get("Tundra"), "feature/service/restful/logging", ServerLogLevel.class);
            } catch(Exception ex) {
                // do nothing
            }
            registry.clear();
            super.start();
        }
    }

    /**
     * Unregisters this class as an invocation handler and stops saving pipelines.
     */
    public synchronized void stop() {
        if (started) {
            super.stop();
            registry.clear();
        }
    }
}
