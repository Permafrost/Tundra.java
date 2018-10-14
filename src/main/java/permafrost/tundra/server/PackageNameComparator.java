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

package permafrost.tundra.server;

import com.wm.lang.ns.NSPackage;
import java.util.Comparator;

/**
 * Compares two Package objects using the package name.
 */
public class PackageNameComparator implements Comparator<NSPackage> {
    /**
     * Initialization on demand holder idiom.
     */
    private static class Holder {
        /**
         * The singleton instance of the class.
         */
        private static final PackageNameComparator INSTANCE = new PackageNameComparator();
    }

    /**
     * Disallow instantiation of this class;
     */
    private PackageNameComparator() {}

    /**
     * Returns the singleton instance of this class.
     *
     * @return The singleton instance of this class.
     */
    public static PackageNameComparator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Compares two Package objects using the package name.
     *
     * @param package1 The first package to be compared.
     * @param package2 The second package to be compared.
     * @return 0 if the two package names are equal, -1 if the first package is ordered by name before the second
     * package, +1 if the first package is ordered by name after the second package.
     */
    public int compare(NSPackage package1, NSPackage package2) {
        int result = 0;

        if (package1 == null && package2 != null) result = -1;
        if (package1 != null && package2 == null) result = 1;
        if (package1 != null && package2 != null) result = package1.getName().compareTo(package2.getName());

        return result;
    }
}
