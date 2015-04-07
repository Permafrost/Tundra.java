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

package permafrost.tundra.net;

public class HTTPResponseHelper {
    private static final java.util.Map<Integer, String> statuses = new java.util.TreeMap<Integer, String>();
    private static final String DEFAULT_RESPONSE_MESSAGE = "Unassigned";

    // creates a map of the standard HTTP status codes
    // <http://www.iana.org/assignments/http-status-codes/http-status-codes.txt>
    static {
        statuses.put(100, "Continue");
        statuses.put(101, "Switching Protocols");
        statuses.put(102, "Processing");
        statuses.put(200, "OK");
        statuses.put(201, "Created");
        statuses.put(202, "Accepted");
        statuses.put(203, "Non-Authoritative Information");
        statuses.put(204, "No Content");
        statuses.put(205, "Reset Content");
        statuses.put(206, "Partial Content");
        statuses.put(207, "Multi-Status");
        statuses.put(208, "Already Reported");
        statuses.put(226, "IM Used");
        statuses.put(300, "Multiple Choices");
        statuses.put(301, "Moved Permanently");
        statuses.put(302, "Found");
        statuses.put(303, "See Other");
        statuses.put(304, "Not Modified");
        statuses.put(305, "Use Proxy");
        statuses.put(306, "Reserved");
        statuses.put(307, "Temporary Redirect");
        statuses.put(308, "Permanent Redirect");
        statuses.put(400, "Bad Request");
        statuses.put(401, "Unauthorized");
        statuses.put(402, "Payment Required");
        statuses.put(403, "Forbidden");
        statuses.put(404, "Not Found");
        statuses.put(405, "Method Not Allowed");
        statuses.put(406, "Not Acceptable");
        statuses.put(407, "Proxy Authentication Required");
        statuses.put(408, "Request Timeout");
        statuses.put(409, "Conflict");
        statuses.put(410, "Gone");
        statuses.put(411, "Length Required");
        statuses.put(412, "Precondition Failed");
        statuses.put(413, "Request Entity Too Large");
        statuses.put(414, "Request-URI Too Long");
        statuses.put(415, "Unsupported Media Type");
        statuses.put(416, "Requested Range Not Satisfiable");
        statuses.put(417, "Expectation Failed");
        statuses.put(422, "Unprocessable Entity");
        statuses.put(423, "Locked");
        statuses.put(424, "Failed Dependency");
        statuses.put(425, "Unassigned");
        statuses.put(426, "Upgrade Required");
        statuses.put(427, "Unassigned");
        statuses.put(428, "Precondition Required");
        statuses.put(429, "Too Many Requests");
        statuses.put(430, "Unassigned");
        statuses.put(431, "Request Header Fields Too Large");
        statuses.put(500, "Internal Server Error");
        statuses.put(501, "Not Implemented");
        statuses.put(502, "Bad Gateway");
        statuses.put(503, "Service Unavailable");
        statuses.put(504, "Gateway Timeout");
        statuses.put(505, "HTTP Version Not Supported");
        statuses.put(506, "Variant Also Negotiates");
        statuses.put(507, "Insufficient Storage");
        statuses.put(508, "Loop Detected");
        statuses.put(509, "Unassigned");
        statuses.put(510, "Not Extended");
        statuses.put(511, "Network Authentication Required");
    }

    /**
     * Disallow instantiation of this class.
     */
    private HTTPResponseHelper() {}

    /**
     * Returns the standard message associated with the given HTTP response status code.
     * @param code An HTTP response status code.
     * @return     The standard message associated with the given code, or "Unassigned" if
     *             given code is not standard.
     */
    public static String getStatusMessage(int code) {
        String message = statuses.get(code);
        if (message == null) message = "Unassigned";
        return message;
    }
}
