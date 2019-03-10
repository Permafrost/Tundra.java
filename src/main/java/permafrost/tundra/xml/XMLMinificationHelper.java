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

package permafrost.tundra.xml;

import com.googlecode.htmlcompressor.compressor.XmlCompressor;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.StringHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A collection of convenience methods for minifying XML.
 */
public final class XMLMinificationHelper {
    /**
     * Disallow instantiation of this class.
     */
    private XMLMinificationHelper() {}

    /**
     * Removes extraneous whitespace and comments from the given XML content.
     *
     * @param content               The XML content to be minified.
     * @return                      The minified XML content.
     * @throws IOException          When an IO error occurs.
     */
    public static InputStream minify(InputStream content) throws IOException {
        return minify(content, null);
    }

    /**
     * Removes extraneous whitespace and comments from the given XML content.
     *
     * @param content               The XML content to be minified.
     * @param charset               The character set the character data is encoded with.
     * @return                      The minified XML content.
     * @throws IOException          When an IO error occurs.
     */
    public static InputStream minify(InputStream content, Charset charset) throws IOException {
        return minify(content, charset, true, true);
    }

    /**
     * Removes extraneous whitespace and comments from the given XML content.
     *
     * @param content               The XML content to be minified.
     * @param charset               The character set the character data is encoded with.
     * @param removeComments        Whether XML comments should be removed as part of the minification.
     * @param removeInterTagSpaces  Whether whitespace between tags should be removed as part of the minification.
     * @return                      The minified XML content.
     * @throws IOException          When an IO error occurs.
     */
    public static InputStream minify(InputStream content, Charset charset, boolean removeComments, boolean removeInterTagSpaces) throws IOException {
        if (content == null) return null;

        XmlCompressor compressor = new XmlCompressor();
        compressor.setRemoveComments(removeComments);
        compressor.setRemoveIntertagSpaces(removeInterTagSpaces);

        return InputStreamHelper.normalize(compressor.compress(StringHelper.normalize(content, CharsetHelper.normalize(charset))), CharsetHelper.normalize(charset));
    }
}
