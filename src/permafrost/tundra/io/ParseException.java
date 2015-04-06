package permafrost.tundra.io;

import permafrost.tundra.exception.BaseException;

public class ParseException extends BaseException {
    /**
     * Constructs a new ParseException.
     */
    public ParseException() {
        super();
    }

    /**
     * Constructs a new ParseException with the given message.
     *
     * @param message A message describing why the ParseException was thrown.
     */
    public ParseException(java.lang.String message) {
        super(message);
    }

    /**
     * Constructs a new ParseException with the given cause.
     *
     * @param cause The cause of this ParseException.
     */
    public ParseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ParseException with the given message and cause.
     *
     * @param message A message describing why the ParseException was thrown.
     * @param cause The cause of this ParseException.
     */
    public ParseException(java.lang.String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
