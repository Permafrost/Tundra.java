/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.content;

import com.wm.data.IData;

/**
 * Convenience class for returning the result from validating content.
 */
public class ValidationResult {
    /**
     * The singleton instance representing a VALID result.
     */
    public static final ValidationResult VALID = new ValidationResult(true);
    /**
     * Whether the content is valid.
     */
    protected boolean isValid;
    /**
     * Message describing why the content is invalid.
     */
    protected String message;
    /**
     * The validation error details describing why the content is invalid.
     */
    protected IData[] errors;

    /**
     * Constructs a new ValdiationResult object.
     *
     * @param isValid   Whether the content is valid.
     */
    public ValidationResult(boolean isValid) {
        this(isValid, null);
    }

    /**
     * Constructs a new ValdiationResult object.
     *
     * @param isValid   Whether the content is valid.
     * @param message   Message describing why the content is invalid.
     */
    public ValidationResult(boolean isValid, String message) {
        this(isValid, message, null);
    }

    /**
     * Constructs a new ValdiationResult object.
     *
     * @param isValid   Whether the content is valid.
     * @param message   Message describing why the content is invalid.
     * @param errors    The validation error details describing why the content is invalid.
     */
    public ValidationResult(boolean isValid, String message, IData[] errors) {
        this.isValid = isValid;
        this.message = message;
        this.errors = errors;
    }

    /**
     * Returns whether the content is valid.
     *
     * @return whether the content is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Returns a message describing why the content is invalid.
     *
     * @return a message describing why the content is invalid.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the validation error details describing why the content is invalid.
     *
     * @return the validation error details describing why the content is invalid.
     */
    public IData[] getErrors() {
        return errors;
    }

    /**
     * Throws an exception if the content is invalid.
     *
     * @throws ValidationException  If the content is invalid.
     */
    public void raiseIfInvalid() throws ValidationException {
        raiseIfInvalid(true);
    }

    /**
     * Throws an exception if the content is invalid.
     *
     * @param raise                 Whether to throw an exception or not.
     * @throws ValidationException  If raise is true and the content is invalid.
     */
    public void raiseIfInvalid(boolean raise) throws ValidationException {
        if (raise && !isValid()) {
            throw new ValidationException(getMessage());
        }
    }
}
