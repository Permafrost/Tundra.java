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

import permafrost.tundra.exception.BaseException;
import permafrost.tundra.io.filter.*;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Directory lister which supports file name filtering.
 */
public class DirectoryLister {
    protected File directory;
    protected boolean recurse;
    protected FilenameFilter directoryFilter, fileFilter;

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     * @param directory The directory whose contents are to be listed.
     * @param recurse   If true, all child directories will be recursively listed also.
     * @throws BaseException
     */
    public DirectoryLister(String directory, boolean recurse) throws BaseException {
        this(directory, recurse, new FilenameFilter[0]);
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     * @param directory The directory whose contents are to be listed.
     * @param recurse   If true, all child directories will be recursively listed also.
     * @throws BaseException
     */
    public DirectoryLister(File directory, boolean recurse) {
        this(directory, recurse, new FilenameFilter[0]);
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     * @param directory The directory whose contents are to be listed.
     * @param recurse   If true, all child directories will be recursively listed also.
     * @param filters   One or more filename filters which will restrict which files
     *                  and directories are returned in the list results.
     * @throws BaseException
     */
    public DirectoryLister(String directory, boolean recurse, FilenameFilter ...filters) throws BaseException {
        this(FileHelper.construct(directory), recurse, filters);
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     * @param directory The directory whose contents are to be listed.
     * @param recurse   If true, all child directories will be recursively listed also.
     * @param filters   One or more filename filters which will restrict which files
     *                  and directories are returned in the list results.
     * @throws BaseException
     */
    public DirectoryLister(File directory, boolean recurse, FilenameFilter ...filters) {
        if (directory == null) throw new IllegalArgumentException("directory must not be null");

        this.directory = directory;
        this.recurse = recurse;

        if (filters != null && filters.length > 0) {
            this.directoryFilter = new ChainFilter(DirectoryFilter.INSTANCE, new ChainFilter(filters));
            this.fileFilter = new ChainFilter(FileFilter.INSTANCE, new ChainFilter(filters));
        } else {
            this.directoryFilter = DirectoryFilter.INSTANCE;
            this.fileFilter = FileFilter.INSTANCE;
        }
    }

    /**
     * Returns the directory that will be listed.
     * @return The directory that will be listed.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Returns whether the listing will be recursive.
     * @return If true, child directories will be recursively listed also.
     */
    public boolean getRecurse() {
        return recurse;
    }

    /**
     * Lists the directory.
     * @return A list of files and directories that match the specified filters.
     * @throws BaseException
     */
    public DirectoryListing list() throws BaseException {
        return list(directory, recurse);
    }

    /**
     * Lists the given directory.
     * @param directory The directory to be listed.
     * @param recurse   If true, child directories will be recursively listed also.
     * @return          A list of files and directories that match the specified filters.
     * @throws BaseException
     */
    protected DirectoryListing list(File directory, boolean recurse) throws BaseException {
        if (!DirectoryHelper.exists(directory)) throw new FileException("Unable to list directory as it does not exist: " + FileHelper.normalize(directory));

        String[] listing = directory.list();

        // if listing is a reasonable size, just use that for the initial capacity for our list
        // of files and directories; otherwise set capacity to 1000 and let it grow as needed
        int capacity = listing.length > 1000 ? 1000 : listing.length;
        java.util.List<File> files = new java.util.ArrayList<File>(capacity);
        java.util.List<File> directories = new java.util.ArrayList<File>(capacity);

        for (String item : listing) {
            File file = new File(directory, item);
            if (fileFilter.accept(directory, item)) {
                files.add(file);
            } else if (directoryFilter.accept(directory, item)) {
                directories.add(file);
            }

            if (recurse && DirectoryFilter.INSTANCE.accept(directory, item)) {
                DirectoryListing results = list(file, recurse);
                files.addAll(results.listFiles());
                directories.addAll(results.listDirectories());
            }
        }

        return new DirectoryListing(directories, files);
    }
}
