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

package permafrost.tundra.io;

import java.io.File;
import java.util.Comparator;

/**
 * Compares files by their last modified time.
 */
public class FileModificationComparator implements Comparator<File> {
    protected boolean ascending = true;

    /**
     * Constructs a new FileModificationComparator.
     *
     * @param ascending If true, files will be compared in ascending last modified time order, otherwise they
     *                  will be compared in descending last modified time order.
     */
    FileModificationComparator(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Compares two file's last modified times.
     *
     * @param firstFile     The first file to be compared.
     * @param secondFile    The second file to be compared.
     * @return              0 if the files were modified at the same time, 1 if ascending order and -1 if descending
     *                      order and the first file was modified after the second file, -1 if ascending order and 1
     *                      if descending order and the first file was modified before the second file.
     */
    public int compare(File firstFile, File secondFile) {
        if (firstFile == null || secondFile == null) throw new NullPointerException("cannot compare null references");

        long lastModifiedDifference = firstFile.lastModified() - secondFile.lastModified();

        int comparison = 0;
        if (lastModifiedDifference < 0) {
            comparison = -1;
        } else if (lastModifiedDifference > 0) {
            comparison = 1;
        }

        return ascending ? comparison : comparison * -1;
    }
}
