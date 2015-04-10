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

public class HTTPHelper {
    private static final java.util.Map<Integer, String> RESPONSE_STATUSES = new java.util.TreeMap<Integer, String>();
    private static final String DEFAULT_RESPONSE_MESSAGE = "Unassigned";

    // creates a map of the standard HTTP status codes
    // <http://www.iana.org/assignments/http-status-codes/http-status-codes.txt>
    static {
        RESPONSE_STATUSES.put(100, "Continue");
        RESPONSE_STATUSES.put(101, "Switching Protocols");
        RESPONSE_STATUSES.put(102, "Processing");
        RESPONSE_STATUSES.put(200, "OK");
        RESPONSE_STATUSES.put(201, "Created");
        RESPONSE_STATUSES.put(202, "Accepted");
        RESPONSE_STATUSES.put(203, "Non-Authoritative Information");
        RESPONSE_STATUSES.put(204, "No Content");
        RESPONSE_STATUSES.put(205, "Reset Content");
        RESPONSE_STATUSES.put(206, "Partial Content");
        RESPONSE_STATUSES.put(207, "Multi-Status");
        RESPONSE_STATUSES.put(208, "Already Reported");
        RESPONSE_STATUSES.put(226, "IM Used");
        RESPONSE_STATUSES.put(300, "Multiple Choices");
        RESPONSE_STATUSES.put(301, "Moved Permanently");
        RESPONSE_STATUSES.put(302, "Found");
        RESPONSE_STATUSES.put(303, "See Other");
        RESPONSE_STATUSES.put(304, "Not Modified");
        RESPONSE_STATUSES.put(305, "Use Proxy");
        RESPONSE_STATUSES.put(306, "Reserved");
        RESPONSE_STATUSES.put(307, "Temporary Redirect");
        RESPONSE_STATUSES.put(308, "Permanent Redirect");
        RESPONSE_STATUSES.put(400, "Bad Request");
        RESPONSE_STATUSES.put(401, "Unauthorized");
        RESPONSE_STATUSES.put(402, "Payment Required");
        RESPONSE_STATUSES.put(403, "Forbidden");
        RESPONSE_STATUSES.put(404, "Not Found");
        RESPONSE_STATUSES.put(405, "Method Not Allowed");
        RESPONSE_STATUSES.put(406, "Not Acceptable");
        RESPONSE_STATUSES.put(407, "Proxy Authentication Required");
        RESPONSE_STATUSES.put(408, "Request Timeout");
        RESPONSE_STATUSES.put(409, "Conflict");
        RESPONSE_STATUSES.put(410, "Gone");
        RESPONSE_STATUSES.put(411, "Length Required");
        RESPONSE_STATUSES.put(412, "Precondition Failed");
        RESPONSE_STATUSES.put(413, "Request Entity Too Large");
        RESPONSE_STATUSES.put(414, "Request-URI Too Long");
        RESPONSE_STATUSES.put(415, "Unsupported Media Type");
        RESPONSE_STATUSES.put(416, "Requested Range Not Satisfiable");
        RESPONSE_STATUSES.put(417, "Expectation Failed");
        RESPONSE_STATUSES.put(422, "Unprocessable Entity");
        RESPONSE_STATUSES.put(423, "Locked");
        RESPONSE_STATUSES.put(424, "Failed Dependency");
        RESPONSE_STATUSES.put(425, "Unassigned");
        RESPONSE_STATUSES.put(426, "Upgrade Required");
        RESPONSE_STATUSES.put(427, "Unassigned");
        RESPONSE_STATUSES.put(428, "Precondition Required");
        RESPONSE_STATUSES.put(429, "Too Many Requests");
        RESPONSE_STATUSES.put(430, "Unassigned");
        RESPONSE_STATUSES.put(431, "Request Header Fields Too Large");
        RESPONSE_STATUSES.put(500, "Internal Server Error");
        RESPONSE_STATUSES.put(501, "Not Implemented");
        RESPONSE_STATUSES.put(502, "Bad Gateway");
        RESPONSE_STATUSES.put(503, "Service Unavailable");
        RESPONSE_STATUSES.put(504, "Gateway Timeout");
        RESPONSE_STATUSES.put(505, "HTTP Version Not Supported");
        RESPONSE_STATUSES.put(506, "Variant Also Negotiates");
        RESPONSE_STATUSES.put(507, "Insufficient Storage");
        RESPONSE_STATUSES.put(508, "Loop Detected");
        RESPONSE_STATUSES.put(509, "Unassigned");
        RESPONSE_STATUSES.put(510, "Not Extended");
        RESPONSE_STATUSES.put(511, "Network Authentication Required");
    }

    /**
     * Disallow instantiation of this class.
     */
    private HTTPHelper() {}

    /**
     * Returns the standard message associated with the given HTTP response status code.
     * @param code An HTTP response status code.
     * @return     The standard message associated with the given code, or "Unassigned" if
     *             given code is not standard.
     */
    public static String getResponseStatusMessage(int code) {
        String message = RESPONSE_STATUSES.get(code);
        if (message == null) message = "Unassigned";
        return message;
    }
}
