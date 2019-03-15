/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

package permafrost.tundra.server.http.handler;

import com.wm.app.b2b.server.AccessException;
import com.wm.app.b2b.server.HTTPResponse;
import com.wm.app.b2b.server.ProtocolState;
import com.wm.net.HttpHeader;
import org.glassfish.json.JsonProviderImpl;
import permafrost.tundra.math.IntegerHelper;
import permafrost.tundra.server.ConcurrentLogWriter;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Logs HTTP requests and responses to a log file.
 */
public class Logger extends StartableHandler {
    /**
     * The logger to use when logging requests.
     */
    protected ConcurrentLogWriter logger;
    /**
     * Factory for created JSON writers.
     */
    private JsonWriterFactory jsonWriterFactory;
    /**
     * Implementation class used for JSON parsing and emitting.
     */
    private JsonProvider provider;
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final Logger INSTANCE = new Logger();
    }

    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class.
     */
    public static Logger getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Creates a new Logger.
     */
    private Logger() {
        // using the org.glassfish.json implementation directly improves performance by avoiding disk access and thread
        // contention caused by the class loading in the javax.json.spi.JsonProvider.provider() method
        provider = new JsonProviderImpl();
        jsonWriterFactory = provider.createWriterFactory(new HashMap<String, Object>(0));
    }

    /**
     * Processes an HTTP request.
     *
     * @param context           The HTTP request context.
     * @param handlers          The queue of subsequent handlers to be called to handle the request.
     * @return                  True if the request was processed.
     * @throws IOException      If an IO error occurs.
     * @throws AccessException  If a security error occurs.
     */
    @Override
    public boolean handle(ProtocolState context, Iterator<Handler> handlers) throws IOException, AccessException {
        boolean result;

        if (started) {
            long startTime = System.nanoTime();
            try {
                result = next(context, handlers);
            } finally {
                long endTime = System.nanoTime();
                log(context, endTime - startTime);
            }
        } else {
            result = next(context, handlers);
        }

        return result;
    }

    /**
     * Logs the HTTP request.
     *
     * @param context   The HTTP request to be logged.
     * @param duration  The measured duration for processing the request in nanoseconds.
     */
    protected void log(ProtocolState context, long duration) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = jsonWriterFactory.createWriter(stringWriter);
            JsonObjectBuilder builder = provider.createObjectBuilder();

            builder.add("duration", DurationHelper.format(duration / 1000000000.0, DurationPattern.XML_NANOSECONDS));
            builder.add("client", encodeClient(context));
            builder.add("request", encodeRequest(context));
            builder.add("response", encodeResponse(context));

            writer.write(builder.build());
            writer.close();

            logger.log(stringWriter.toString());
        } catch(Exception ex) {
            // do nothing
        }
    }

    /**
     * Returns a JsonObjectBuilder representing the client of the HTTP request.
     *
     * @param context   The HTTP request context.
     * @return          The JsonObjectBuilder representing the client.
     */
    protected JsonObjectBuilder encodeClient(ProtocolState context) {
        JsonObjectBuilder client = provider.createObjectBuilder();

        try {
            client.add("host", context.getRemoteHost());
            client.add("port", context.getRemotePort());
            client.add("user", context.getInvokeState().getUser().toString());
        } catch(Exception ex) {
            // do nothing
        }

        return client;
    }

    /**
     * Returns a JsonObjectBuilder representing the HTTP request.
     *
     * @param context   The HTTP request context.
     * @return          The JsonObjectBuilder representing the request.
     */
    protected JsonObjectBuilder encodeRequest(ProtocolState context) {
        JsonObjectBuilder request = provider.createObjectBuilder();

        try {
            request.add("method", HttpHeader.reqStrType[context.getRequestType()]);
            request.add("uri", context.getRequestUrl());

            JsonObjectBuilder headers = provider.createObjectBuilder();
            Map<String, String> map = new TreeMap<String, String>(context.getRequestHeader().getFieldsMap());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    if (key.equalsIgnoreCase("Authorization")) {
                        value = "REDACTED";
                    }
                    headers.add(key, value);
                }
            }

            request.add("headers", headers);
        } catch(Exception ex) {
            // do nothing
        }

        return request;
    }

    /**
     * Returns a JsonObjectBuilder representing the response to the HTTP request.
     *
     * @param context   The HTTP request context.
     * @return          The JsonObjectBuilder representing the response.
     */
    protected JsonObjectBuilder encodeResponse(ProtocolState context) {
        JsonObjectBuilder response = provider.createObjectBuilder();

        try {
            JsonObjectBuilder status = provider.createObjectBuilder();
            status.add("code", context.getResponseCode());
            status.add("message", context.getResponseMessage());

            response.add("status", status);

            JsonObjectBuilder headers = provider.createObjectBuilder();
            Map<String, String> map = new TreeMap<String, String>(context.getResponseHeader().getFieldsMap());

            if (!map.containsKey("Content-Length")) {
                // add content length, if it hasn't been added to the response headers yet
                HTTPResponse httpResponse = context.getResponse();
                int responseSize = httpResponse.getOutputSize();
                map.put("Content-Length", IntegerHelper.emit(responseSize));
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    headers.add(entry.getKey(), entry.getValue());
                }
            }

            response.add("headers", headers);
        } catch(Exception ex) {
            // do nothing
        }

        return response;
    }

    /**
     * Starts this object.
     */
    @Override
    public synchronized void start() {
        if (!started) {
            logger = new ConcurrentLogWriter("tundra-http.log");
        }
        super.start();
    }

    /**
     * Stops this object.
     */
    @Override
    public synchronized void stop() {
        if (started) {
            logger.stop();
            logger = null;
        }
        super.stop();
    }
}
