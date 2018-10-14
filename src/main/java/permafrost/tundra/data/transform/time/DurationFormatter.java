/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Lachlan Dowding
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

package permafrost.tundra.data.transform.time;

import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;
import permafrost.tundra.time.DurationHelper;
import permafrost.tundra.time.DurationPattern;
import java.util.Date;

/**
 * Reformats duration strings in IData documents and IData[] document lists.
 */
public class DurationFormatter extends Transformer<String, String> {
    protected DurationPattern inPattern;
    protected DurationPattern[] inPatterns;
    protected DurationPattern outPattern;
    protected Date instant;

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPattern     The pattern the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     */
    public DurationFormatter(DurationPattern inPattern, DurationPattern outPattern) {
        this(inPattern, outPattern, null);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPattern     The pattern the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     * @param instant       Used as a starting instant to resolve indeterminate values.
     */
    public DurationFormatter(DurationPattern inPattern, DurationPattern outPattern, Date instant) {
        this(inPattern, outPattern, instant, true);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPattern     The pattern the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     * @param instant       Used as a starting instant to resolve indeterminate values.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public DurationFormatter(DurationPattern inPattern, DurationPattern outPattern, Date instant, boolean recurse) {
        this(inPattern, null, outPattern, instant, recurse);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPatterns    The patterns the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     */
    public DurationFormatter(DurationPattern[] inPatterns, DurationPattern outPattern) {
        this(inPatterns, outPattern, null);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPatterns    The patterns the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     * @param instant       Used as a starting instant to resolve indeterminate values.
     */
    public DurationFormatter(DurationPattern[] inPatterns, DurationPattern outPattern, Date instant) {
        this(inPatterns, outPattern, instant, true);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPatterns    The patterns the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     * @param instant       Used as a starting instant to resolve indeterminate values.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    public DurationFormatter(DurationPattern[] inPatterns, DurationPattern outPattern, Date instant, boolean recurse) {
        this(null, inPatterns, outPattern, instant, recurse);
    }

    /**
     * Creates a new DateTimeFormatter object.
     *
     * @param inPattern     The pattern the given input duration string adheres to.
     * @param inPatterns    The patterns the given input duration string adheres to.
     * @param outPattern    The pattern the duration string should be reformatted as.
     * @param instant       Used as a starting instant to resolve indeterminate values.
     * @param recurse       Whether to recursively transform child IData documents and IData[] document lists.
     */
    protected DurationFormatter(DurationPattern inPattern, DurationPattern[] inPatterns, DurationPattern outPattern, Date instant, boolean recurse) {
        super(String.class, String.class, TransformerMode.VALUES, recurse, true, true, true);
        this.inPattern = inPattern;
        this.inPatterns = inPatterns;
        this.outPattern = outPattern;
        this.instant = instant;
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    @Override
    protected String transformValue(String key, String value) {
        String result;
        if (inPatterns == null) {
            result = DurationHelper.format(value, inPattern, outPattern, instant);
        } else {
            result = DurationHelper.format(value, inPatterns, outPattern, instant);
        }
        return result;
    }
}
