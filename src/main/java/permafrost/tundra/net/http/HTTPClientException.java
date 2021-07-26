package permafrost.tundra.net.http;

import permafrost.tundra.content.Content;
import permafrost.tundra.lang.UnrecoverableTransportException;

/**
 * Represents an HTTP client error response.
 */
public class HTTPClientException extends UnrecoverableTransportException {
    /**
     * Constructs a new HTTPClientException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     */
    public HTTPClientException(int code, String message, Content content) {
        this(code, message, content, null);
    }

    /**
     * Constructs a new HTTPClientException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     * @param cause     The cause of this exception.
     */
    public HTTPClientException(int code, String message, Content content, Throwable cause) {
        super(HTTPHelper.getExceptionMessage(code, message, content), content, cause);
    }
}
