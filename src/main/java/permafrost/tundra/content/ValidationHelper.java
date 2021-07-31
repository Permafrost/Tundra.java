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

import com.wm.app.b2b.server.ServiceException;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.flow.InputOutputSignature;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.server.NodeHelper;
import permafrost.tundra.server.ServiceHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for content validation.
 */
public class ValidationHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ValidationHelper() {}

    /**
     * Validates the given IData document against the given document reference or flat file schema.
     *
     * @param document          The IData document to be validated.
     * @param contentSchema     The document reference or flat file schema to validate against.
     * @return                  The validation result.
     * @throws ServiceException If an unexpected error occurs during validation.
     */
    public static ValidationResult validate(IData document, String contentSchema) throws ServiceException {
        ValidationResult result = ValidationResult.VALID;

        if (document != null && contentSchema != null) {
            if (NodeHelper.exists(contentSchema)) {
                String schemaType = NodeHelper.getNodeType(contentSchema).toString();
                if ("Flat File Schema".equals(schemaType)) {
                    IData scope = IDataFactory.create();
                    IDataCursor scopeCursor = scope.getCursor();
                    try {
                        IDataHelper.put(scopeCursor, "ffValues", document);
                        IDataHelper.put(scopeCursor, "ffSchema", contentSchema);
                        IDataHelper.put(scopeCursor, "returnAsBytes", "false");
                        scopeCursor.destroy();

                        scope = ServiceHelper.invoke("pub.flatFile:convertToString", scope);

                        scopeCursor = scope.getCursor();
                        String content = IDataHelper.get(scopeCursor, "string", String.class);

                        result = validate(content, contentSchema);
                    } finally {
                        scopeCursor.destroy();
                    }
                } else {
                    IData scope = IDataFactory.create();
                    IDataCursor scopeCursor = scope.getCursor();
                    try {
                        IDataHelper.put(scopeCursor, "object", document);
                        IDataHelper.put(scopeCursor, "conformsTo", contentSchema);
                        IDataHelper.put(scopeCursor, "maxErrors", "-1");
                        IDataHelper.put(scopeCursor, "ignoreContent", "false");
                        IDataHelper.put(scopeCursor, "failIfInvalid", "false");
                        scopeCursor.destroy();

                        scope = ServiceHelper.invoke("pub.schema:validate", scope);

                        scopeCursor = scope.getCursor();
                        boolean isValid = IDataHelper.getOrDefault(scopeCursor, "isValid", Boolean.class, true);
                        IData[] errors = IDataHelper.get(scopeCursor, "errors", IData[].class);

                        result = buildResult(contentSchema, isValid, errors);
                    } finally {
                        scopeCursor.destroy();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Validates the given flat file content against the given flat file schema.
     *
     * @param content           The flat file content to validate.
     * @param contentSchema     The flat file schema to validate against.
     * @return                  The validation result.
     * @throws ServiceException If an unexpected error occurs during validation.
     */
    public static ValidationResult validate(String content, String contentSchema) throws ServiceException {
        ValidationResult result = ValidationResult.VALID;

        if (content != null && contentSchema != null) {
            if (NodeHelper.exists(contentSchema)) {
                String schemaType = NodeHelper.getNodeType(contentSchema).toString();
                if ("Flat File Schema".equals(schemaType)) {
                    IData scope = IDataFactory.create();
                    IDataCursor scopeCursor = scope.getCursor();
                    try {
                        IDataHelper.put(scopeCursor, "ffData", content);
                        IDataHelper.put(scopeCursor, "ffSchema", contentSchema);
                        IDataHelper.put(scopeCursor, "keepResults", "false");
                        IDataHelper.put(scopeCursor, "validate", "true");
                        IDataHelper.put(scopeCursor, "returnErrors", "asArray");
                        scopeCursor.destroy();

                        scope = ServiceHelper.invoke("pub.flatFile:convertToValues", scope);

                        scopeCursor = scope.getCursor();
                        boolean isValid = IDataHelper.getOrDefault(scopeCursor, "isValid", Boolean.class, true);
                        IData[] errors = IDataHelper.get(scopeCursor, "errors", IData[].class);

                        result = buildResult(contentSchema, isValid, errors);
                    } catch(Exception ex) {
                        ExceptionHelper.raise(ex);
                    } finally {
                        scopeCursor.destroy();
                    }
                } else {
                    throw new UnsupportedOperationException("Non-flat file content validation is not supported");
                }
            }
        }

        return result;
    }

    /**
     * Returns a ValidationResult for the given inputs.
     *
     * @param contentSchema The schema against which the content was validated.
     * @param isValid       Whether the validation succeeded or failed.
     * @param errors        Optional list of errors describing why the validation failed.
     * @return              A ValidationResult object representing the given inputs.
     */
    public static ValidationResult buildResult(String contentSchema, boolean isValid, IData[] errors) {
        ValidationResult result = ValidationResult.VALID;

        if (!isValid) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Validation against schema ")
                    .append(contentSchema)
                    .append(" failed");

            if (errors != null && errors.length > 0) {
                errorMessage.append(": ").append(errorsToString(errors));
            }

            result = new ValidationResult(false, errorMessage.toString(), normalize(errors));
        }
        return result;
    }

    /**
     * Returns a ValidationResult for the given inputs.
     *
     * @param direction The signature direction that was validated against.
     * @param isValid   Whether the validation succeeded or failed.
     * @param errors    Optional list of errors describing why the validation failed.
     * @param pipeline  The pipeline against which error keys are resolved.
     * @return          A ValidationResult object representing the given inputs.
     */
    public static ValidationResult buildResult(InputOutputSignature direction, boolean isValid, IData[] errors, IData pipeline) {
        ValidationResult result;

        if (isValid) {
            result = ValidationResult.VALID;
        } else {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Validation against ")
                    .append(direction.toString().toLowerCase())
                    .append(" signature failed");

            if (errors != null && errors.length > 0) {
                errorMessage.append(": ").append(errorsToString(errors));
            }

            result = new ValidationResult(false, errorMessage.toString(), normalize(errors, pipeline));
        }
        return result;
    }

    /**
     * Converts the given errors array to a string.
     *
     * @param errors    The errors to be converted to a string.
     * @return          A string representation of the given errors.
     */
    private static String errorsToString(IData[] errors) {
        if (errors == null) return null;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < errors.length; i++) {
            if (errors[i] != null) {
                IDataCursor errorCursor = errors[i].getCursor();

                try {
                    String pathName = IDataHelper.get(errorCursor, "pathName", String.class);
                    String errorMessage = IDataHelper.get(errorCursor, "errorMessage", String.class);

                    if (builder.length() > 0) {
                        builder.append(", ");
                        if (i == errors.length - 1) builder.append("and ");
                    }

                    if (errors.length > 1) {
                        builder.append("(")
                               .append(i + 1)
                               .append(") ");
                    }

                    builder.append("`")
                           .append(StringHelper.slice(pathName, 1, pathName.length()))
                           .append("` ")
                           .append(StringHelper.slice(errorMessage, 16, errorMessage.length()).toLowerCase().trim());
                } finally {
                    errorCursor.destroy();
                }
            }
        }

        return builder.toString();
    }

    /**
     * Normalize the errors array to include the key as well as the error message.
     *
     * @param errors    The errors to normalize.
     * @return          The normalized errors.
     */
    private static IData[] normalize(IData[] errors) {
        return normalize(errors, null);
    }

    /**
     * Normalize the errors array to include the key and value as well as the error message.
     *
     * @param errors    The errors to normalize.
     * @param pipeline  The pipeline against which keys will be resolved.
     * @return          The normalized errors.
     */
    private static IData[] normalize(IData[] errors, IData pipeline) {
        if (errors == null || errors.length == 0) return errors;

        List<IData> normalizedErrors = new ArrayList<IData>(errors.length);

        for (IData error : errors) {
            if (error != null) {
                IData normalizedError = IDataFactory.create();

                IDataCursor errorCursor = error.getCursor();
                IDataCursor normalizedErrorCursor = normalizedError.getCursor();

                try {
                    String pathName = IDataHelper.get(errorCursor, "pathName", String.class);
                    String errorCode = IDataHelper.get(errorCursor, "errorCode", String.class);
                    String errorMessage = IDataHelper.get(errorCursor, "errorMessage", String.class);

                    if (pathName != null && errorCode != null && errorMessage != null) {
                        String key = StringHelper.slice(pathName, 1, pathName.length());
                        normalizedErrorCursor.insertAfter("code", errorCode);
                        normalizedErrorCursor.insertAfter("message", errorMessage);
                        normalizedErrorCursor.insertAfter("key", key);

                        if  (pipeline != null) {
                            Object value = IDataHelper.get(pipeline, key);
                            if (value != null) normalizedErrorCursor.insertAfter("value", value);
                        }

                        normalizedErrors.add(normalizedError);
                    }
                } finally {
                    errorCursor.destroy();
                    normalizedErrorCursor.destroy();
                }

            }
        }

        return normalizedErrors.toArray(new IData[0]);
    }
}
