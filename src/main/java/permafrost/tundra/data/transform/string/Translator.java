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
     * Creates a new Translator object.
     *
     * @param translations  The translation table used to translate values.
     * @param reverse       If true, the translation table is flipped so keys become values and vice versa.
     */
    public Translator(IData translations, boolean reverse) {
        super(String.class, Object.class, TransformerMode.VALUES, true, true, true, true);
        if (translations == null) {
            this.translations = IDataFactory.create();
            defaultValue = null;
            nullValue = null;
        } else {
            translations = IDataHelper.duplicate(translations, true);
            defaultValue = IDataHelper.remove(translations, "$default");
            nullValue = IDataHelper.remove(translations, "$null");

            if (reverse) {
                this.translations = IDataHelper.flip(translations);
            } else {
                this.translations = translations;
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
            Object defaultValue = this.defaultValue == null ? value : this.defaultValue;
            return IDataHelper.getOrDefault(cursor, value, Object.class, defaultValue);
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
