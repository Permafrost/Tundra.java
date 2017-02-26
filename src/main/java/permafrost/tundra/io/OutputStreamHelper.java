/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2016 Lachlan Dowding
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package permafrost.tundra.io;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;

/**
 * A collection of convenience methods for working with OutputStream objects.
 */
public final class OutputStreamHelper {
    /**
     * Disallow instantiation of this class.
     */
    private OutputStreamHelper() {}

    /**
     * Normalizes the given OutputStream, by wrapping it in a BufferedOutputStream where appropriate.
     *
     * @param outputStream  A OutputStream to be normalized.
     * @return              The normalized OutputStream.
     */
    public static OutputStream normalize(OutputStream outputStream) {
        if (outputStream == null) return null;

        if (!(outputStream instanceof FilterOutputStream || outputStream instanceof ByteArrayOutputStream)) {
            outputStream = new BufferedOutputStream(outputStream, InputOutputHelper.DEFAULT_BUFFER_SIZE);
        }

        return outputStream;
    }
}
