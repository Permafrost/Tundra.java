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

import com.wm.app.b2b.server.ServiceException;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.xml.sax.SAXException;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.ExceptionHelper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A collection of convenience methods for the canonicalization of XML.
 */
public final class XMLCanonicalizationHelper {
    /**
     * Disallow instantiation of this class.
     */
    private XMLCanonicalizationHelper() {}

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, Charset charset, XMLCanonicalizationAlgorithm algorithm) throws ServiceException {
        byte[] output = null;

        try {
            Init.init();
            input = CharsetHelper.convert(input, CharsetHelper.normalize(charset), CharsetHelper.normalize(Canonicalizer.ENCODING));
            output = Canonicalizer.getInstance(XMLCanonicalizationAlgorithm.normalize(algorithm).getID()).canonicalize(input);
        } catch (XMLSecurityException ex) {
            ExceptionHelper.raise(ex);
        } catch (ParserConfigurationException ex) {
            ExceptionHelper.raise(ex);
        } catch (SAXException ex) {
            ExceptionHelper.raise(ex);
        } catch (IOException ex) {
            ExceptionHelper.raise(ex);
        }

        return output;
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     */
    public static byte[] canonicalize(byte[] input, Charset charset, String algorithm) throws ServiceException {
        return canonicalize(input, charset, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input             The XML content to canonicalize.
     * @param charset           The character set the XML content is encoded with.
     * @param algorithm         The canonicalization algorithm to use.
     * @return                  The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, Charset charset, XMLCanonicalizationAlgorithm algorithm) throws ServiceException, IOException {
        return InputStreamHelper.normalize(canonicalize(BytesHelper.normalize(input), charset, algorithm));
    }

    /**
     * Canonicalizes the given XML content using the given algorithm.
     *
     * @param input     The XML content to canonicalize.
     * @param charset   The character set the XML content is encoded with.
     * @param algorithm The canonicalization algorithm to use.
     * @return The given XML content canonicalized with the specified algorithm.
     * @throws ServiceException If a canonicalization error occurs.
     * @throws IOException      If an I/O error occurs.
     */
    public static InputStream canonicalize(InputStream input, Charset charset, String algorithm) throws ServiceException, IOException {
        return canonicalize(input, charset, XMLCanonicalizationAlgorithm.normalize(algorithm));
    }
}
