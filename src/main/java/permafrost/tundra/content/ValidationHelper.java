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
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.NodeHelper;
import permafrost.tundra.server.ServiceHelper;
import java.io.IOException;

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

    public static ValidationResult buildResult(String contentSchema, boolean isValid, IData[] errors) {
        ValidationResult result = ValidationResult.VALID;

        if (!isValid) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Content failed validation against schema ").append(contentSchema);

            if (errors != null && errors.length > 0) {
                errorMessage.append(": ");

                IData errorDetails = IDataFactory.create();
                IDataHelper.put(errorDetails, "recordWithNoID", errors);

                IDataJSONParser parser = new IDataJSONParser(false);
                try {
                    parser.emit(errorMessage, errorDetails);
                } catch(IOException ex) {
                    // ignore exception
                }
            }

            result = new ValidationResult(false, errorMessage.toString());
        }
        return result;
    }
}
