/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Lachlan Dowding
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

package permafrost.tundra.data;

import permafrost.tundra.mime.ImmutableMimeType;
import permafrost.tundra.mime.MIMETypeHelper;
import java.util.concurrent.ConcurrentHashMap;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

/**
 * A manager for resolving a MIME type or filename to an IDataTextParser that can handle content in that format.
 */
public class IDataTextParsers {
    private static ConcurrentHashMap<String, IDataTextParser> fileExtensionParsers = new ConcurrentHashMap<String, IDataTextParser>();
    private static ConcurrentHashMap<ImmutableMimeType, IDataTextParser> mimeTypeParsers = new ConcurrentHashMap<ImmutableMimeType, IDataTextParser>();

    /**
     * Disallow instantiation of this class.
     */
    private IDataTextParsers() {}

    /**
     * Registers an IDataTextParser object as a handler of files with the given extensions.
     *
     * @param parser        The parser to be registered.
     * @param extensions    The filename extensions the given parser can handle.
     */
    public static void registerFileExtensions(IDataTextParser parser, String ...extensions) {
        if (parser != null && extensions != null) {
            for (String fileExtension : extensions) {
                if (fileExtension != null) {
                    fileExtensionParsers.put(fileExtension, parser);
                }
            }
        }
    }

    /**
     * Unregisters an IDataTextParser object as a handler of files with the given extensions.
     *
     * @param parser        The parser to be unregistered.
     * @param extensions    The filename extensions the given parser was previously registered as a handler for.
     */
    public static void unregisterFileExtensions(IDataTextParser parser, String ...extensions) {
        if (parser != null && extensions != null) {
            for (String fileExtension : extensions) {
                if (fileExtension != null) {
                    fileExtensionParsers.remove(fileExtension, parser);
                }
            }
        }
    }

    /**
     * Registers an IDataTextParser object as a handler of content with the given MIME types.
     *
     * @param parser                    The parser to be registered.
     * @param mimeTypes                 The MIME types the given parser can handle.
     * @throws MimeTypeParseException   If a MIME type is incorrectly formatted.
     */
    public static void registerMimeTypes(IDataTextParser parser, String ...mimeTypes) throws MimeTypeParseException {
        registerMimeTypes(parser, ImmutableMimeType.of(mimeTypes));
    }

    /**
     * Registers an IDataTextParser object as a handler of content with the given MIME type.
     *
     * @param parser                    The parser to be registered.
     * @param mimeTypes                 The MIME types the given parser can handle.
     * @throws MimeTypeParseException   If a MIME type is incorrectly formatted.
     */
    public static void registerMimeTypes(IDataTextParser parser, MimeType ...mimeTypes) throws MimeTypeParseException {
        if (parser != null && mimeTypes != null) {
            for (MimeType mimeType : mimeTypes) {
                if (mimeType != null) {
                    mimeTypeParsers.put(ImmutableMimeType.of(mimeType), parser);
                }
            }
        }
    }

    /**
     * Unregisters an IDataTextParser object as a handler of the given MIME types.
     *
     * @param parser                    The parser to be unregistered.
     * @param mimeTypes                 The MIME types the given parser was previously registered as a handler for.
     * @throws MimeTypeParseException   If a MIME type string is incorrectly formatted.
     */
    public static void unregisterMimeTypes(IDataTextParser parser, String ...mimeTypes) throws MimeTypeParseException {
        unregisterMimeTypes(parser, ImmutableMimeType.of(mimeTypes));
    }

    /**
     * Unregisters an IDataTextParser object as a handler of the given MIME types.
     *
     * @param parser                    The parser to be unregistered.
     * @param mimeTypes                 The MIME types the given parser was previously registered as a handler for.
     */
    public static void unregisterMimeTypes(IDataTextParser parser, ImmutableMimeType...mimeTypes) {
        if (parser != null && mimeTypes != null) {
            for (ImmutableMimeType mimeType : mimeTypes) {
                if (mimeType != null) {
                    mimeTypeParsers.remove(mimeType, parser);
                }
            }
        }
    }

    /**
     * Returns an IDataTextParser which can handle files with the given filename extension.
     *
     * @param fileExtension             The filename extension.
     * @return                          An IDataTextParser that can handle the given filename extension, or null if no
     *                                  parser is registered as a handler for the given extension.
     * @throws MimeTypeParseException   If the MIME type string is incorrectly formatted.
     */
    public static IDataTextParser resolveFileExtension(String fileExtension) throws MimeTypeParseException {
        IDataTextParser parser = fileExtensionParsers.get(fileExtension);

        if (parser == null) {
            // try resolving using MIME type associated with this file extension
            parser = resolveMimeType(MIMETypeHelper.fromExtension(fileExtension));
        }

        return parser;
    }

    /**
     * Returns an IDataTextParser which can handle content of the given MIME type.
     *
     * @param mimeType                  The MIME type string.
     * @return                          An IDataTextParser that can handle content of the given MIME type, or null if
     *                                  no parser is registered as a handler for the given MIME type.
     * @throws MimeTypeParseException   If the MIME type string is incorrectly formatted.
     */
    public static IDataTextParser resolveMimeType(String mimeType) throws MimeTypeParseException {
        return resolveMimeType(ImmutableMimeType.of(mimeType));
    }

    /**
     * Returns an IDataTextParser which can handle content of the given MIME type.
     *
     * @param mimeType                  The MIME type string.
     * @return                          An IDataTextParser that can handle content of the given MIME type, or null if
     *                                  no parser is registered as a handler for the given MIME type.
     * @throws MimeTypeParseException   If the MIME type string is incorrectly formatted.
     */
    public static IDataTextParser resolveMimeType(MimeType mimeType) throws MimeTypeParseException {
        return mimeTypeParsers.get(ImmutableMimeType.of(mimeType));
    }
}
