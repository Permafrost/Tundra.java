package permafrost.tundra.net.http;

import permafrost.tundra.content.Content;
import permafrost.tundra.lang.TransportException;

/**
 * Represents an HTTP client error response that can be retried automatically, such as 429 Too Many Requests.
 */
public class HTTPClientRecoverableException extends TransportException {
    /**
     * Constructs a new HTTPClientRecoverableException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     */
    public HTTPClientRecoverableException(int code, String message, Content content) {
        this(code, message, content, null);
    }

    /**
     * Constructs a new HTTPClientRecoverableException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     * @param cause     The cause of this exception.
     */
    public HTTPClientRecoverableException(int code, String message, Content content, Throwable cause) {
        super(HTTPHelper.getExceptionMessage(code, message, content), content, cause);
    }
}
