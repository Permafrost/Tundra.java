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

package permafrost.tundra.data;

import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataPortable;
import com.wm.util.Table;
import com.wm.util.coder.IDataCodable;
import com.wm.util.coder.ValuesCodable;
import permafrost.tundra.lang.ObjectArrayDefaultComparator;
import permafrost.tundra.lang.ObjectDefaultComparator;

/**
 * Compares two IData objects using all the keys and values in each document.
 */
public enum IDataDefaultComparator implements IDataComparator {
    /**
     * The singleton instance of this class.
     */
    INSTANCE;

    /**
     * Compares two IData documents.
     *
     * @param document1  The first IData document to be compared.
     * @param document2 The second IData document to be compared.
     * @return               A value less than zero if the first document
     *                       comes before the second document, a value of
     *                       zero if they are equal, or a value of greater
     *                       than zero if the first document comes after the
     *                       second document according to the comparison
     *                       of all the keys and values in each document.
     */
    public int compare(IData document1, IData document2) {
        int result = 0;

        if (document1 == null || document2 == null) {
            if (document1 != null) {
                result = 1;
            } else if (document2 != null){
                result = -1;
            }
        } else {
            int size1 = IDataHelper.size(document1);
            int size2 = IDataHelper.size(document2);

            if (size1 < size2) {
                result = -1;
            } else if (size2 > size1) {
                result = 1;
            } else {
                // compare all keys and values in order
                IDataCursor cursor1 = document1.getCursor();
                IDataCursor cursor2 = document2.getCursor();

                boolean next1 = cursor1.next(), next2 = cursor2.next();

                while (next1 && next2) {
                    String key1 = cursor1.getKey();
                    String key2 = cursor2.getKey();

                    result = key1.compareTo(key2);

                    if (result == 0) {
                        Object value1 = cursor1.getValue();
                        Object value2 = cursor2.getValue();

                        if (value1 == null || value2 == null) {
                            if (value1 != null) {
                                result = 1;
                            } else if (value2 != null) {
                                result = -1;
                            }
                        } else if ((value1 instanceof IData || value1 instanceof IDataCodable || value1 instanceof IDataPortable || value1 instanceof ValuesCodable) &&
                                   (value2 instanceof IData || value2 instanceof IDataCodable || value2 instanceof IDataPortable || value2 instanceof ValuesCodable)) {
                            result = compare(IDataHelper.toIData(value1), IDataHelper.toIData(value2));
                        } else if ((value1 instanceof IData[] || value1 instanceof Table || value1 instanceof IDataCodable[] || value1 instanceof IDataPortable[] || value1 instanceof ValuesCodable[]) &&
                                   (value2 instanceof IData[] || value2 instanceof Table || value2 instanceof IDataCodable[] || value2 instanceof IDataPortable[] || value2 instanceof ValuesCodable[])) {
                            result = IDataArrayDefaultComparator.INSTANCE.compare(IDataHelper.toIDataArray(value1), IDataHelper.toIDataArray(value2));
                        } else if (value1 instanceof Object[] && value2 instanceof Object[]) {
                            result = ObjectArrayDefaultComparator.INSTANCE.compare((Object[]) value1, (Object[]) value2);
                        } else {
                            result = ObjectDefaultComparator.INSTANCE.compare(value1, value2);
                        }
                    }

                    if (result != 0) {
                        break;
                    }

                    next1 = cursor1.next();
                    next2 = cursor2.next();
                }

                if (next1 && !next2) {
                    result = 1;
                } else if (!next1 && next2) {
                    result = -1;
                }
            }
        }

        return result;
    }
}
