package permafrost.tundra.content;

import permafrost.tundra.lang.UnrecoverableRuntimeException;

/**
 * Throw a ValidationException when data translation fails.
 */
public class TranslationException extends UnrecoverableRuntimeException {
    /**
     * Constructs a new TranslationException with the given message.
     *
     * @param message A message describing why the TranslationException was thrown.
     */
    public TranslationException(String message) {
        super(message);
    }
}
