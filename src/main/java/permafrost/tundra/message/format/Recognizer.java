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

package permafrost.tundra.message.format;

import com.wm.data.IData;
import permafrost.tundra.data.IDataHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A content format registry which provides content recognition for arbitrary content.
 */
public class Recognizer {
    /**
     * List of all registered content format definitions.
     */
    private volatile Set<Format> formats;

    /**
     * If content cannot be recognized then this
     */
    private volatile Format defaultFormat;

    /**
     * Create a new Recognizer with no registered formats.
     */
    public Recognizer() {
        formats = new ConcurrentSkipListSet<Format>();
    }

    /**
     * Create a new Recognizer initialized with the given formats.
     *
     * @param collection    A collection of formats to be registered with the new Recognizer.
     */
    public Recognizer(Collection<Format> collection) {
        initialize(collection);
    }

    /**
     * Returns the first recognized ContentDefinition for the content in the given pipeline.
     *
     * @param pipeline  The pipeline containing content to be recognized.
     * @return          Either the recognized ContentDefinition, or null if the content was unrecognized.
     */
    public Format recognize(IData pipeline) {
        Object formatName = IDataHelper.get(pipeline, "$message.format.name");

        if (formatName == null) {
            for (Format format : formats) {
                if (format.recognize(pipeline)) {
                    return format;
                }
            }
        } else {
            Format format = get(formatName.toString());
            if (format != null && format.isEnabled()) return format;
        }

        return null;
    }

    /**
     * Returns the format with the given name, if it was registered.
     *
     * @param formatName    The name of the format.
     * @return              The format with the given name, if it was registered.
     */
    public Format get(String formatName) {
        for (Format format : formats) {
            if (format.getName().equals(formatName)) return format;
        }
        return null;
    }

    /**
     * Replaces all registered formats with the given collection of formats.
     *
     * @param collection A collection of formats to initialize the recognition registry with.
     */
    public synchronized void initialize(Collection<Format> collection) {
        formats = new ConcurrentSkipListSet<Format>(collection);
    }

    /**
     * Registers the given format for recognition.
     *
     * @param format The format to be registered.
     */
    public void register(Format format) {
        formats.add(format);
    }

    /**
     * Unregisters the given format from the registry.
     *
     * @param format The format to be unregistered.
     */
    public void unregister(Format format) {
        formats.remove(format);
    }

    /**
     * Removes all registered formats from the registry.
     */
    public void clear() {
        formats.clear();
    }

    /**
     * Returns a list of all registered content format definitions.
     *
     * @return A list of all registered content format definitions.
     */
    public List<Format> list() {
        return new ArrayList<Format>(formats);
    }
}
