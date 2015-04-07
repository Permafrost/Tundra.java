package permafrost.tundra.io;

import permafrost.tundra.exception.BaseException;

public class FileException extends BaseException {
    /**
     * Constructs a new FileException.
     */
    public FileException() {
        super();
    }

    /**
     * Constructs a new FileException with the given message.
     *
     * @param message A message describing why the FileException was thrown.
     */
    public FileException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileException with the given cause.
     *
     * @param cause The cause of this FileException.
     */
    public FileException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new FileException with the given message and cause.
     *
     * @param message A message describing why the FileException was thrown.
     * @param cause The cause of this StreamException.
     */
    public FileException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
