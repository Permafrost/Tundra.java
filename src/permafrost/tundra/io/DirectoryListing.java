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

package permafrost.tundra.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the results of a directory listing.
 */
public class DirectoryListing {
    protected Collection<File> all, directories, files;

    /**
     * Constructs a new DirectoryListing given a list of files and directories.
     * @param directories A list of directories to include in the listing results.
     * @param files       A list of files to include in the listing results.
     */
    public DirectoryListing(File[] directories, File[] files) {
        if (directories == null) throw new IllegalArgumentException("directories must not be null");
        if (files == null) throw new IllegalArgumentException("files must not be null");

        this.directories = Arrays.asList(directories);
        this.files = Arrays.asList(files);
    }

    /**
     * Constructs a new DirectoryListing given a list of files and directories.
     * @param directories A list of directories to include in the listing results.
     * @param files       A list of files to include in the listing results.
     */
    public DirectoryListing(Collection<File> directories, Collection<File> files) {
        if (directories == null) throw new IllegalArgumentException("directories must not be null");
        if (files == null) throw new IllegalArgumentException("files must not be null");

        this.directories = directories;
        this.files = files;
    }

    /**
     * Returns all the files and directories in this listing.
     * @return All the files and directories in this listing.
     */
    public Collection<File> list() {
        if (all == null) {
            all = new ArrayList<File>(directories.size() + files.size());
            all.addAll(directories);
            all.addAll(files);
        }
        return all;
    }

    /**
     * Returns all the directories in this listing.
     * @return All the directories in this listing.
     */
    public Collection<File> listDirectories() {
        return directories;
    }

    /**
     * Returns all the files in this listing.
     * @return All the files in this listing.
     */
    public Collection<File> listFiles() {
        return files;
    }
}
