/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

import com.wm.app.b2b.server.HTTPResponse;
import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.ProtocolState;
import com.wm.data.IData;
import com.wm.net.HttpHeader;
import org.glassfish.json.JsonProviderImpl;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.math.IntegerHelper;
import permafrost.tundra.net.http.route.HTTPRouter;
import permafrost.tundra.time.DateTimeHelper;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.xml.datatype.Duration;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Convenience methods for working with ProtocolState objects.
 */
public class ProtocolStateHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ProtocolStateHelper() {}

    /**
     * Implementation class used for JSON parsing and emitting: using the org.glassfish.json implementation directly
     * improves performance by avoiding disk access and thread contention caused by the class loading in the
     * javax.json.spi.JsonProvider.provider() method.
     */
    private static final JsonProvider provider = new JsonProviderImpl();
    /**
     * Factory for created JSON writers.
     */
    private static final JsonWriterFactory jsonWriterFactory = provider.createWriterFactory(new HashMap<String, Object>(0));

    /**
     * Returns the HTTP request represented by the given ProtocolState as a JSON string.
     *
     * @param context           The HTTP request to be logged.
     * @param duration          The measured duration for processing the request in nanoseconds.
     * @param startTime         The datetime the request was received.
     * @param endTime           The datetime the response was generated.
     * @param inputPipeline     The optional input pipeline for the service that handled the request.
     * @param outputPipeline    The optional output pipeline for the service that handled the request.
     */
    public static String serialize(ProtocolState context, long duration, long startTime, long endTime, IData inputPipeline, IData outputPipeline) {
        return serialize(context, DurationHelper.parse(duration, DurationPattern.NANOSECONDS), DateTimeHelper.parse(startTime), DateTimeHelper.parse(endTime), inputPipeline, outputPipeline);
    }

    /**
     * Returns the HTTP request represented by the given ProtocolState as a JSON string.
     *
     * @param context           The HTTP request to be logged.
     * @param duration          The measured duration for processing the request.
     * @param startTime         The datetime the request was received.
     * @param endTime           The datetime the response was generated.
     * @param inputPipeline     The optional input pipeline for the service that handled the request.
     * @param outputPipeline    The optional output pipeline for the service that handled the request.
     */
    public static String serialize(ProtocolState context, Duration duration, Calendar startTime, Calendar endTime, IData inputPipeline, IData outputPipeline) {
        String output = null;

        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter writer = jsonWriterFactory.createWriter(stringWriter);
            JsonObjectBuilder builder = provider.createObjectBuilder();

            builder.add("client", encodeClient(context));
            builder.add("request", encodeRequest(context, startTime));
            builder.add("response", encodeResponse(context, endTime));
            if (inputPipeline != null || outputPipeline != null) builder.add("pipeline", encodePipeline(inputPipeline, outputPipeline));
            if (duration != null) builder.add("duration", DurationHelper.emit(duration, DurationPattern.XML_NANOSECONDS));

            writer.write(builder.build());
            writer.close();

            output = stringWriter.toString();
        } catch(Exception ex) {
            // do nothing
        }

        return output;
    }

    /**
     * Returns a JsonObjectBuilder representing the client of the HTTP request.
     *
     * @param context   The HTTP request context.
     * @return          The JsonObjectBuilder representing the client.
     */
    private static JsonObjectBuilder encodeClient(ProtocolState context) {
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
     * Returns a JsonObjectBuilder representing the client of the HTTP request.
     *
     * @param inputPipeline     The input pipeline.
     * @param outputPipeline    The output pipeline.
     * @return                  The JsonObjectBuilder representing the client.
     */
    private static JsonObjectBuilder encodePipeline(IData inputPipeline, IData outputPipeline) {
        JsonObjectBuilder pipeline = provider.createObjectBuilder();

        try {
            if (inputPipeline != null) pipeline.add("input", IDataJSONParser.toJsonObject(provider, inputPipeline));
            if (outputPipeline != null) pipeline.add("output", IDataJSONParser.toJsonObject(provider, outputPipeline));
        } catch(Exception ex) {
            // do nothing
        }

        return pipeline;
    }

    /**
     * Returns a JsonObjectBuilder representing the HTTP request.
     *
     * @param context   The HTTP request context.
     * @param datetime  The datetime the request was received.
     * @return          The JsonObjectBuilder representing the request.
     */
    private static JsonObjectBuilder encodeRequest(ProtocolState context, Calendar datetime) {
        JsonObjectBuilder request = provider.createObjectBuilder();

        try {
            request.add("datetime", DateTimeHelper.emit(datetime));
            request.add("method", HttpHeader.reqStrType[context.getRequestType()]);
            request.add("uri", HTTPRouter.getRequestURI());

            JsonObjectBuilder headers = provider.createObjectBuilder();
            Map<String, String> map = new TreeMap<String, String>(context.getRequestHeader().getFieldsMap());
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null && !HTTPRouter.REQUEST_URI_HEADER.equalsIgnoreCase(key)) {
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
     * @param datetime  The datetime the response was generated.
     * @return          The JsonObjectBuilder representing the response.
     */
    private static JsonObjectBuilder encodeResponse(ProtocolState context, Calendar datetime) {
        JsonObjectBuilder response = provider.createObjectBuilder();

        try {
            response.add("datetime", DateTimeHelper.emit(datetime));

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
                if (responseSize == 0) {
                    // if response size is zero then the HTTP response might not have been prepared yet, so check the
                    // invoke state private data $msgBytesOut for the size instead.
                    InvokeState invokeState = InvokeStateHelper.current();
                    if (invokeState != null) {
                        byte[] bytes = (byte[])invokeState.getPrivateData("$msgBytesOut");
                        if (bytes != null) {
                            responseSize = bytes.length;
                        }
                    }
                }
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
}
