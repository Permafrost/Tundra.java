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
import com.wm.data.IDataCursor;
import com.wm.data.IDataUtil;
import com.wm.util.coder.IDataCodable;
import permafrost.tundra.data.CopyOnWriteIDataMap;
import permafrost.tundra.flow.ConditionEvaluator;
import permafrost.tundra.lang.BooleanHelper;
import permafrost.tundra.xml.namespace.IDataNamespaceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A content format definition object which can be used to recognize arbitrary content.
 */
public class Format implements Comparable<Format>, IDataCodable {
    /**
     * The unique name of this format.
     */
    protected String name;
    /**
     * A conditional statement used to recognize content in this format.
     */
    protected ConditionEvaluator recognitionCondition;
    /**
     * Whether this format is enabled for content recognition.
     */
    protected boolean enabled;
    /**
     * The document with which this format was created.
     */
    protected IData document;

    /**
     * Creates a new content format definition object.
     *
     * @param document The IData document containing the properties of the new format.
     */
    public Format(IData document) {
        if (document == null) throw new NullPointerException("document must not be null");

        this.document = document;

        IDataCursor cursor = document.getCursor();

        String name = IDataUtil.getString(cursor, "name");
        if (name == null) throw new NullPointerException("name must not be null");
        this.name = name;

        String recognitionCondition = IDataUtil.getString(cursor, "recognize");
        if (recognitionCondition == null) throw new NullPointerException("recognize must not be null");
        this.recognitionCondition = new ConditionEvaluator(recognitionCondition, IDataNamespaceContext.of(IDataUtil.getIData(cursor, "namespace")));

        this.enabled = BooleanHelper.parse(IDataUtil.getString(cursor, "enabled?"));
    }

    /**
     * Returns the name of this content format definition.
     *
     * @return The name of this content format definition.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if the given pipeline contains content that is recognized to be of this type.
     *
     * @param pipeline  A pipeline containing content to be recognized.
     * @return          True if the pipeline contains content that is recognized to be of this type.
     */
    public boolean recognize(IData pipeline) {
        return enabled && recognitionCondition.evaluate(pipeline);
    }

    /**
     * Returns true if the given object is equal to this object.
     *
     * @param other The object to compare equality to.
     * @return      True if the given object is equal to this object.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Format)) return false;
        return name.equals(((Format)other).getName());
    }

    /**
     * Returns a hash code for this object.
     *
     * @return A hash code for this object.
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Compares this object to another object.
     *
     * @param other The other object to be compared to.
     * @return      A negative integer, zero, or a positive integer as this object is less than, equal to, or greater
     *              than the specified object.
     */
    public int compareTo(Format other) {
        return name.compareTo(other.getName());
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return The IData document this object wraps.
     */
    @Override
    public IData getIData() {
        return toIData();
    }

    /**
     * This method has not been implemented.
     *
     * @param  document                         An IData document.
     * @throws UnsupportedOperationException    This method has not been implemented.
     */
    public void setIData(IData document) {
        throw new UnsupportedOperationException("setIData(IData) not implemented");
    }

    /**
     * Returns a new Format object given an IData document containing the format's properties.
     *
     * @param document  An IData document containing the format's properties.
     * @return          A new Format object.
     */
    public static Format of(IData document) {
        if (document == null) return null;
        return new Format(document);
    }

    /**
     * Returns a collection of Format objects given a collection of IData documents containing format properties.
     *
     * @param documents A collection of IData documents containing format properties. Nulls are skipped.
     * @return          A collection of Format objects.
     */
    public static Collection<Format> of(Collection<IData> documents) {
        if (documents == null) return null;

        List<Format> output = new ArrayList<Format>(documents.size());
        for (IData item : documents) {
            if (item != null) {
                output.add(of(item));
            }
        }

        return output;
    }

    /**
     * Returns an array of Format objects given an array of IData documents containing format properties.
     *
     * @param documents An array of IData documents containing format properties. Nulls are skipped.
     * @return          An array of Format objects.
     */
    public static Format[] of(IData[] documents) {
        if (documents == null) return null;
        Collection<Format> output = of(Arrays.asList(documents));
        return output.toArray(new Format[output.size()]);
    }

    /**
     * Returns an IData representation of this object.
     *
     * @return The IData document this object wraps.
     */
    public IData toIData() {
        return CopyOnWriteIDataMap.of(this.document);
    }

    /**
     * Returns an IData representation of the given format.
     *
     * @param format The format to convert.
     * @return       An IData representation of the given format.
     */
    public static IData toIData(Format format) {
        if (format == null) return null;
        return format.toIData();
    }

    /**
     * Converts a list of content format definitions to an IData[].
     *
     * @param formats The formats to be converted.
     * @return        An IData[] representation of the given formats.
     */
    public static IData[] toIDataArray(Collection<Format> formats) {
        if (formats == null) return null;
        return toIDataArray(formats.toArray(new Format[formats.size()]));
    }

    /**
     * Converts a list of content format definitions to an IData[].
     *
     * @param formats The formats to be converted.
     * @return        An IData[] representation of the given formats.
     */
    public static IData[] toIDataArray(Format[] formats) {
        if (formats == null) return null;

        IData[] output = new IData[formats.length];
        for (int i = 0; i < formats.length; i++) {
            if (formats[i] != null) {
                output[i] = formats[i].getIData();
            }
        }

        return output;
    }
}
