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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A content format registry which provides content recognition for arbitrary content.
 */
public class Recognizer {
    /**
     * List of all registered content format definitions by name.
     */
    private volatile Map<String, Format> formats = Collections.emptyMap();
    /**
     * List of publishable registered content format definitions by publishable document type.
     */
    private volatile Map<String, Format> formatsByPublishableDocumentType = Collections.emptyMap();

    /**
     * Create a new Recognizer with no registered formats.
     */
    public Recognizer() {}

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
        String formatName = formatName = IDataHelper.get(pipeline, "$message.format.name", true, String.class);
        if (formatName == null) IDataHelper.get(pipeline, "$message.format/name", String.class);

        if (formatName != null) {
            return get(formatName, true);
        } else {
            for (Format format : formats.values()) {
                if (format.recognize(pipeline)) {
                    return format;
                }
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
    public Format get(String formatName) {
        return get(formatName, false);
    }

    /**
     * Returns the format with the given name, if it was registered.
     *
     * @param formatName    The name of the format.
     * @return              The format with the given name, if it was registered.
     */
    public Format get(String formatName, boolean enabledOnly) {
        Format format = formats.get(formatName);
        return format == null || (enabledOnly && !format.isEnabled()) ? null : format;
    }

    /**
     * Returns the format with the publishable document type, if registered.
     *
     * @param publishableDocumentType   The publishable document type of the format.
     * @return                          The format with the given publishable document type, if registered.
     */
    public Format getByPublishableDocumentType(String publishableDocumentType) {
        Format format = formatsByPublishableDocumentType.get(publishableDocumentType);
        return format == null || !format.isEnabled() ? null : format;
    }
    
    /**
     * Replaces all registered formats with the given collection of formats.
     *
     * @param collection A collection of formats to initialize the recognition registry with.
     */
    public synchronized void initialize(Collection<Format> collection) {
        Map<String, Format> newFormats = new TreeMap<String, Format>();
        Map<String, Format> newFormatsByPublishableDocumentType = new TreeMap<String, Format>();

        for (Format format : collection) {
            if (format != null) {
                newFormats.put(format.getName(), format);
                if ("publish".equals(format.getRouteType()) && format.getRouteRef() != null) {
                    newFormatsByPublishableDocumentType.put(format.getRouteRef(), format);
                }
            }
        }

        formats = newFormats;
        formatsByPublishableDocumentType = newFormatsByPublishableDocumentType;
    }

    /**
     * Removes all registered formats from the registry.
     */
    public synchronized void clear() {
        formats = Collections.emptyMap();
        formatsByPublishableDocumentType = Collections.emptyMap();
    }

    /**
     * Returns a list of all registered content format definitions.
     *
     * @return A list of all registered content format definitions.
     */
    public List<Format> list() {
        return new ArrayList<Format>(formats.values());
    }
}
