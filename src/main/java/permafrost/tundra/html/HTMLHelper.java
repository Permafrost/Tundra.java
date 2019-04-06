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

package permafrost.tundra.html;

import com.wm.data.IData;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.org.springframework.web.util.HtmlUtils;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.html.Decoder;
import permafrost.tundra.data.transform.html.Encoder;

/**
 * A collection of convenience methods for working with HTML.
 */
public final class HTMLHelper {
    /**
     * Disallow instantiation of this class.
     */
    private HTMLHelper() {}

    /**
     * HTML decodes the given string.
     *
     * @param input The string to be decoded.
     * @return The decoded string.
     */
    public static String decode(String input) {
        if (input == null) return null;
        return HtmlUtils.htmlUnescape(input);
    }

    /**
     * HTML decodes the given strings.
     *
     * @param input The strings to be decoded.
     * @return The decoded strings.
     */
    public static String[] decode(String[] input) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = decode(input[i]);
        }

        return output;
    }

    /**
     * HTML decodes string values in the given IData document.
     *
     * @param document  The IData document to decode.
     * @return          A new IData document with string values decoded.
     */
    public static IData decode(IData document) {
        return Transformer.transform(document, new Decoder());
    }

    /**
     * HTML encodes the given string.
     *
     * @param input The string to be encoded.
     * @return The encoded string.
     */
    public static String encode(String input) {
        if (input == null) return null;
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * HTML encodes the given strings.
     *
     * @param input The strings to be encoded.
     * @return The encoded strings.
     */
    public static String[] encode(String[] input) {
        if (input == null) return null;

        String[] output = new String[input.length];

        for (int i = 0; i < input.length; i++) {
            output[i] = encode(input[i]);
        }

        return output;
    }

    /**
     * HTML encodes string values in the given IData document.
     *
     * @param document  The IData document to decode.
     * @return          A new IData document with string values encoded.
     */
    public static IData encode(IData document) {
        return Transformer.transform(document, new Encoder());
    }
}
