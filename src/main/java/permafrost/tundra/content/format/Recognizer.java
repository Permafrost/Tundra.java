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

package permafrost.tundra.content.format;

import com.wm.data.IData;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A content format registry which provides content recognition for arbitrary content.
 */
public class Recognizer {
    /**
     * List of all registered content format definitions.
     */
    private static volatile Set<Format> formats = new ConcurrentSkipListSet<Format>();

    /**
     * Disallow instantiation of this class.
     */
    private Recognizer() {}

    /**
     * Returns the first recognized ContentDefinition for the content in the given pipeline.
     *
     * @param pipeline  The pipeline containing content to be recognized.
     * @return          Either the recognized ContentDefinition, or null if the content was unrecognized.
     */
    public static Format recognize(IData pipeline) {
        for (Format format : formats) {
            if (format.recognize(pipeline)) {
                return format;
            }
        }

        return null;
    }

    /**
     * Returns the format with the given name, if it was registered.
     *
     * @param formatName    The name of the format.
     * @return              The format with the given name, if it was registered.
     */
    public static Format get(String formatName) {
        for (Format format : formats) {
            if (format.getName().equals(formatName)) return format;
        }
        return null;
    }

    /**
     * Replaces all registered formats with the given collection of formats.
     *
     * @param formats A collection of formats to initialize the recognition registry with.
     */
    public static synchronized void initialize(Collection<Format> formats) {
        formats = new ConcurrentSkipListSet<Format>(formats);
    }

    /**
     * Registers the given format for recognition.
     *
     * @param format The format to be registered.
     */
    public static void register(Format format) {
        formats.add(format);
    }

    /**
     * Unregisters the given format from the registry.
     *
     * @param format The format to be unregistered.
     */
    public static void unregister(Format format) {
        formats.remove(format);
    }

    /**
     * Removes all registered formats from the registry.
     */
    public static void clear() {
        formats.clear();
    }

    /**
     * Returns a list of all registered content format definitions.
     *
     * @return A list of all registered content format definitions.
     */
    public static Format[] list() {
        return formats.toArray(new Format[formats.size()]);
    }
}
