package permafrost.tundra.net.http;

import permafrost.tundra.content.Content;
import permafrost.tundra.lang.TransportException;
import java.io.Serializable;

/**
 * Represents an HTTP server error response.
 */
public class HTTPServerException extends TransportException implements Serializable {
    /**
     * The serialization identity of this class version.
     */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a new HTTPServerException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     */
    public HTTPServerException(int code, String message, Content content) {
        this(code, message, content, null);
    }

    /**
     * Constructs a new HTTPServerException.
     *
     * @param code      The response code.
     * @param message   The response message.
     * @param content   The response body.
     * @param cause     The cause of this exception.
     */
    public HTTPServerException(int code, String message, Content content, Throwable cause) {
        super(HTTPHelper.getExceptionMessage(code, message, content), content, cause);
    }
}
