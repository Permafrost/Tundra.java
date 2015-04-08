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

package permafrost.tundra.idata;

/**
 * Compares two IData objects using all the keys and values in each document.
 */
public class IDataDefaultComparator implements IDataComparator {
    /**
     * Construct a new IDataDefaultComparator.
     */
    public IDataDefaultComparator() {}

    /**
     * Compares two IData documents.
     *
     * @param firstDocument  The first IData document to be compared.
     * @param secondDocument The second IData document to be compared.
     * @return               A value less than zero if the first document
     *                       comes before the second document, a value of
     *                       zero if they are equal, or a value of greater
     *                       than zero if the first document comes after the
     *                       second document according to the comparison
     *                       of all the keys and values in each document.
     */
    public int compare(com.wm.data.IData firstDocument, com.wm.data.IData secondDocument) {
        int result = 0;
        // TODO: implement comparison of all keys and values in each document
        return result;
    }
}
