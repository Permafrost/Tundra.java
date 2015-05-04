/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lachlan Dowding
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

package permafrost.tundra.lang;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;

/**
 * A collection of convenience methods for working with StackTraceElement objects.
 */
public class StackTraceElementHelper {
    /**
     * Disallow instantiation of this class.
     */
    private StackTraceElementHelper() {}

    /**
     * Converts a StackTraceElement[] to an IData[] representation.
     *
     * @param array The StackTraceElement[] to be converted.
     * @return      The IData[] representation of the given StackTraceElement[].
     */
    public static IData[] toIDataArray(StackTraceElement[] array) {
        if (array == null) return null;

        IData[] output = new IData[array.length];

        for (int i = 0; i < array.length; i++) {
            output[i] = toIData(array[i]);
        }

        return output;
    }

    /**
     * Converts a StackTraceElement to an IData representation.
     *
     * @param stackTraceElement The StackTraceElement to be converted.
     * @return                  The IData representation of the given StackTraceElement.
     */
    public static IData toIData(StackTraceElement stackTraceElement) {
        if (stackTraceElement == null) return null;

        IData output = IDataFactory.create();
        IDataCursor cursor = output.getCursor();

        IDataUtil.put(cursor, "description", stackTraceElement.toString());

        String file = stackTraceElement.getFileName();
        if (file != null) IDataUtil.put(cursor, "file", file);

        IDataUtil.put(cursor, "class", stackTraceElement.getClassName());
        IDataUtil.put(cursor, "method", stackTraceElement.getMethodName());

        int line = stackTraceElement.getLineNumber();
        if (line >= 0) IDataUtil.put(cursor, "line", "" + line);

        IDataUtil.put(cursor, "native?", "" + stackTraceElement.isNativeMethod());

        cursor.destroy();

        return output;
    }
}
