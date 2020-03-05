/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Lachlan Dowding
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

package permafrost.tundra.flow;

import com.wm.app.b2b.server.InvokeState;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.lang.ns.NSField;
import com.wm.lang.ns.NSRecord;
import com.wm.lang.ns.NSService;
import com.wm.lang.schema.Validator;
import com.wm.lang.xml.WMDocumentException;
import permafrost.tundra.content.ValidationResult;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.IDataJSONParser;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.server.ServiceHelper;
import java.io.IOException;

/**
 * Convenience services for working with the invoke pipeline.
 */
public class PipelineHelper {
    /**
     * Disallow instantiation.
     */
    private PipelineHelper() {}

    /**
     * Validates the given pipeline against the calling service's input or output signature.
     *
     * @param pipeline  The pipeline to be validated.
     * @param direction Whether to validate the input or output signature.
     * @return          Whether the validation succeeded or failed.
     */
    public static ValidationResult validate(IData pipeline, InputOutputSignature direction) {
        return validate(ServiceHelper.self(), pipeline, direction);
    }

    /**
     * Validates the given pipeline against the given service's input or output signature.
     *
     * @param service   The service whose signature is used to validate against.
     * @param pipeline  The pipeline to be validated.
     * @param direction Whether to validate the input or output signature.
     * @return          Whether the validation succeeded or failed.
     */
    public static ValidationResult validate(NSService service, IData pipeline, InputOutputSignature direction) {
        ValidationResult result = ValidationResult.VALID;

        // we can only validate the pipeline when not debugging
        if (!"wm.server.flow:stepFlow".equals(service.getNSName().getFullName())) {
            NSRecord record;
            if (direction == InputOutputSignature.INPUT) {
                record = service.getSignature().getInput();
            } else {
                record = service.getSignature().getOutput();
            }

            if (record != null) {
                Validator validator = Validator.create(pipeline, record, Validator.getDefaultOptions());
                validator.setLocale(InvokeState.getCurrentLocale());
                validator.setMaximumErrors(-1); // return all errors

                IDataCursor cursor = null;
                try {
                    IData scope = validator.validate();
                    cursor = scope.getCursor();

                    boolean isValid = IDataHelper.getOrDefault(cursor, "isValid", Boolean.class, true);

                    if (!isValid) {
                        IData[] errors = IDataHelper.get(cursor, "errors", IData[].class);
                        result = buildResult(direction, isValid, errors);
                    }
                } catch (WMDocumentException ex) {
                    ExceptionHelper.raiseUnchecked(ex);
                } finally {
                    if (cursor != null) cursor.destroy();
                }
            }
        }

        return result;
    }

    /**
     * Specifies whether to use the input or output signature.
     */
    public enum InputOutputSignature {
        INPUT, OUTPUT;
    }

    /**
     * Returns a ValidationResult for the given inputs.
     *
     * @param direction The signature direction that was validated against.
     * @param isValid   Whether the validation succeeded or failed.
     * @param errors    Optional list of errors describing why the validation failed.
     * @return          A ValidationResult object representing the given inputs.
     */
    public static ValidationResult buildResult(InputOutputSignature direction, boolean isValid, IData[] errors) {
        ValidationResult result;

        if (isValid) {
            result = ValidationResult.VALID;
        } else {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Pipeline validation against ")
                    .append(direction.toString().toLowerCase())
                    .append(" signature failed");

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

            result = new ValidationResult(false, errorMessage.toString(), errors);
        }
        return result;
    }
}
