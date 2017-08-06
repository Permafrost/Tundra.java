/*
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2017 Lachlan Dowding
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

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * A FilterInputStream which wraps an existing InputStream and provides transcoding from the given source character set
 * to a given target character set on the fly.
 */
public class TranscodingInputStream extends FilterInputStream {
    /**
     * Whether transcoding should be performed.
     */
    protected boolean transcodingRequired = false;
    /**
     * The character set to use to encode data.
     */
    protected CharsetEncoder encoder;
    /**
     * The buffer used to store unread character data.
     */
    protected CharBuffer buffer;
    /**
     * The reader to read character data from.
     */
    protected Reader reader;

    /**
     * Wraps the given InputStream in a character set transcoding filter.
     *
     * @param in            The InputStream to be transcoded.
     * @param sourceCharset The character set the given InputStream is encoded with.
     * @param targetCharset The character set to transcode the data read from the given InputStream.
     */
    public TranscodingInputStream(InputStream in, Charset sourceCharset, Charset targetCharset) {
        super(in);

        if (sourceCharset == null) throw new NullPointerException("sourceCharset must not be null");
        if (targetCharset == null) throw new NullPointerException("targetCharset must not be null");

        this.transcodingRequired = !sourceCharset.equals(targetCharset);

        if (transcodingRequired) {
            reader = new BufferedReader(new InputStreamReader(in, sourceCharset), InputOutputHelper.DEFAULT_BUFFER_SIZE);
            encoder = targetCharset.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
            buffer = CharBuffer.allocate(InputOutputHelper.DEFAULT_BUFFER_SIZE);
        }
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255.
     * If no byte is available because the end of the stream has been reached, the value -1 is returned.
     *
     * @return              The next byte of data, or -1 if the end of the stream is reached.
     * @throws IOException  If an IO error occurs.
     */
    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[1];
        int count = read(bytes, 0, 1);
        if (count > 0) {
            return bytes[0];
        } else {
            return -1;
        }
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many
     * as len bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer.
     *
     * @param b             The buffer into which the data is read.
     * @param off           The start offset in array b at which the data is written.
     * @param len           The maximum number of bytes to read.
     * @return              The total number of bytes read into the buffer, or -1 if there is no more data because the
     *                      end of the stream has been reached.
     * @throws IOException  If the first byte cannot be read for any reason other than end of file, or if the input
     *                      stream has been closed, or if some other I/O error occurs.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (transcodingRequired) {
            int count = -1;
            while(buffer.hasRemaining()) {
                count = reader.read(buffer);
                if (count < 0) break;
            }

            buffer.flip();
            if (buffer.hasRemaining()) {
                ByteBuffer output = ByteBuffer.wrap(b, off, len);
                CoderResult result = encoder.encode(buffer, output, false);
                if (result.isUnderflow()) {
                    buffer.clear();
                } else if (result.isOverflow()) {
                    buffer.compact();
                } else {
                    result.throwException();
                }
                count = output.flip().remaining();
            }

            return count;
        } else {
            return super.read(b, off, len);
        }
    }

    /**
     * Closes the InputStream.
     *
     * @throws IOException If an IO error occurs.
     */
    @Override
    public void close() throws IOException {
        if (transcodingRequired) {
            reader.close();
        } else {
            super.close();
        }
    }
}