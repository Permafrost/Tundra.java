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

import java.nio.charset.Charset;

/**
 * A collection of convenience methods for working with Charset objects.
 */
public class CharsetHelper {
    /**
     * The default character set used by Tundra.
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    /**
     * The default character set name used by Tundra.
     */
    public static final String DEFAULT_CHARSET_NAME = DEFAULT_CHARSET.name();

    /**
     * Disallow instantiation of this class.
     */
    private CharsetHelper() {}

    /**
     * Normalizes the given charset name as a Charset object.
     * @param charsetName The character set name to normalize.
     * @return            The Charset representing the given name,
     *                    or a default Charset if the given name is null.
     */
    public static Charset normalize(String charsetName) {
        return normalize(charsetName, DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given charset name as a Charset object.
     * @param charsetName    The character set name to normalize.
     * @param defaultCharset The default character set to return
     *                       if the given name is null.
     * @return               The Charset representing the given name,
     *                       or a default Charset if the given name is null.
     */
    public static Charset normalize(String charsetName, Charset defaultCharset) {
        Charset charset;
        if (charsetName == null) {
            charset = defaultCharset;
        } else {
            charset = Charset.forName(charsetName);
        }
        return charset;
    }

    /**
     * Normalizes the given Charset object.
     * @param charset     The character set to normalize.
     * @return            The normalized character set.
     */
    public static Charset normalize(Charset charset) {
        return normalize(charset, DEFAULT_CHARSET);
    }

    /**
     * Normalizes the given Charset object.
     * @param charset        The character set to normalize.
     * @param defaultCharset The default character set to return
     *                       if the given charset is null.
     * @return               The normalized character set.
     */
    public static Charset normalize(Charset charset, Charset defaultCharset) {
        if (charset == null) charset = defaultCharset;
        return charset;
    }
}
