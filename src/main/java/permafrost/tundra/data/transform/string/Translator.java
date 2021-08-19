/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Lachlan Dowding
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

package permafrost.tundra.data.transform.string;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import permafrost.tundra.content.TranslationException;
import permafrost.tundra.data.IDataHelper;
import permafrost.tundra.data.transform.Transformer;
import permafrost.tundra.data.transform.TransformerMode;

/**
 * Translates an input value to an output value using a translation table.
 */
public class Translator extends Transformer<String, Object> {
    /**
     * The translations used when translating values.
     */
    private final IData translations;
    /**
     * The default value used when translating a value with no translation, and the value used when translating a null
     * value.
     */
    private final Object defaultValue, nullValue;
    /**
     * Whether to exclude missing translation values from translated documents.
     */
    private final boolean exclude;
    /**
     * Whether to throw an exception for missing translation values.
     */
    private final boolean raise;

    /**
     * Creates a new Translator object.
     *
     * @param translations  The translation table used to translate values.
     * @param reverse       If true, the translation table is flipped so keys become values and vice versa.
     * @param exclude       If true, missing translation values will be excluded from the translated document.
     * @param raise         If true, missing translation values will result in an exception being thrown.
     */
    public Translator(IData translations, boolean reverse, boolean exclude, boolean raise) {
        super(String.class, Object.class, TransformerMode.VALUES, true, !exclude, true, true);
        this.exclude = exclude;
        this.raise = raise;
        if (translations == null) {
            this.translations = IDataFactory.create();
            defaultValue = null;
            nullValue = null;
        } else {
            translations = IDataHelper.duplicate(translations, true);
            Object defaultValueForward = IDataHelper.remove(translations, "$default");
            Object defaultValueReverse = IDataHelper.remove(translations, "$default.reverse");
            Object nullValueForward = IDataHelper.remove(translations, "$null");
            Object nullValueReverse = IDataHelper.remove(translations, "$null.reverse");
            if (reverse) {
                this.translations = IDataHelper.flip(translations);
                this.defaultValue = defaultValueReverse;
                this.nullValue = nullValueReverse;
            } else {
                this.translations = translations;
                this.defaultValue = defaultValueForward;
                this.nullValue = nullValueForward;
            }
        }
    }

    /**
     * Transforms the given value.
     *
     * @param key   The key associated with the value being transformed.
     * @param value The value to be transformed.
     * @return      The transformed value.
     */
    @Override
    protected Object transformValue(String key, String value) {
        IDataCursor cursor = translations.getCursor();
        try {
            Object defaultValue;
            if (this.defaultValue == null) {
                if (exclude || raise) {
                    defaultValue = null;
                } else {
                    defaultValue = value;
                }
            } else {
                defaultValue = this.defaultValue;
            }
            Object translatedValue = IDataHelper.getOrDefault(cursor, value, Object.class, defaultValue);
            if (raise && translatedValue == null) {
                throw new TranslationException("Translation missing for value: " + value);
            }
            return translatedValue;
        } finally {
            cursor.destroy();
        }
    }

    /**
     * Translates a null value.
     *
     * @param key   The key associated with the null value being transformed.
     * @return      The transformed null value.
     */
    @Override
    protected Object transformNull(String key) {
        return nullValue;
    }
}
