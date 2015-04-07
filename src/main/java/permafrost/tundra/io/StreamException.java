package permafrost.tundra.io;

import permafrost.tundra.exception.BaseException;

public class StreamException extends BaseException {
    /**
     * Constructs a new StreamException.
     */
    public StreamException() {
        super();
    }

    /**
     * Constructs a new StreamException with the given message.
     *
     * @param message A message describing why the StreamException was thrown.
     */
    public StreamException(String message) {
        super(message);
    }

    /**
     * Constructs a new StreamException with the given cause.
     *
     * @param cause The cause of this StreamException.
     */
    public StreamException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new StreamException with the given message and cause.
     *
     * @param message A message describing why the StreamException was thrown.
     * @param cause The cause of this StreamException.
     */
    public StreamException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
