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

package permafrost.tundra.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import permafrost.tundra.lang.StringHelper;
import java.io.File;

public class DeleteOnCloseFileInputStreamTest {

    @Test
    public void testClose() throws Exception {
        String content = "this is a test";
        File tempFile = FileHelper.create();
        FileHelper.writeFromString(tempFile, content, false);
        assertTrue(FileHelper.exists(tempFile));

        DeleteOnCloseFileInputStream in = new DeleteOnCloseFileInputStream(tempFile);

        // make sure the input stream can be read from without error
        assertEquals(content, StringHelper.normalize(InputStreamHelper.read(in, false)));

        // make sure closing the first time deletes the file
        in.close();
        assertTrue(!FileHelper.exists(tempFile));

        // make sure closing more than once doesn't cause an error
        in.close();
    }
}