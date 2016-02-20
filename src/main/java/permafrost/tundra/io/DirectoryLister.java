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

import permafrost.tundra.io.filter.AndFilenameFilter;
import permafrost.tundra.io.filter.DirectoryFilenameFilter;
import permafrost.tundra.io.filter.FileFilenameFilter;
import permafrost.tundra.io.filter.NotFilenameFilter;
import permafrost.tundra.io.filter.OrFilenameFilter;
import permafrost.tundra.lang.ArrayHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Directory lister which supports file name filtering.
 */
public class DirectoryLister {
    /**
     * The directory to be listed.
     */
    protected File directory;
    /**
     * Whether the listing will be recursive.
     */
    protected boolean recurse;
    /**
     * The filter used for returning a list of directories.
     */
    protected AndFilenameFilter directoryFilter = new AndFilenameFilter(DirectoryFilenameFilter.getInstance());
    /**
     * The filter used for returning a list of files.
     */
    protected AndFilenameFilter fileFilter = new AndFilenameFilter(FileFilenameFilter.getInstance());

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     *
     * @param directory     The directory whose contents are to be listed.
     * @param recurse       If true, all child directories will be recursively listed also.
     */
    public DirectoryLister(String directory, boolean recurse) {
        this(directory, recurse, null, null);
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     *
     * @param directory     The directory whose contents are to be listed.
     * @param recurse       If true, all child directories will be recursively listed also.
     * @param inclusion     A filters to include filenames from the returned listing.
     */
    public DirectoryLister(String directory, boolean recurse, FilenameFilter inclusion) {
        this(directory, recurse, null, ArrayHelper.arrayify(inclusion));
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     *
     * @param directory     The directory whose contents are to be listed.
     * @param recurse       If true, all child directories will be recursively listed also.
     */
    public DirectoryLister(File directory, boolean recurse) {
        this(directory, recurse, null, null);
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     *
     * @param directory     The directory whose contents are to be listed.
     * @param recurse       If true, all child directories will be recursively listed also.
     * @param exclusions    A list of filters to exclude filenames from the returned listing.
     * @param inclusions    A list of filters to include filenames from the returned listing.
     */
    public DirectoryLister(String directory, boolean recurse, FilenameFilter[] exclusions, FilenameFilter[] inclusions) {
        this(FileHelper.construct(directory), recurse, exclusions == null ? null : Arrays.asList(exclusions), inclusions == null ? null : Arrays.asList(inclusions));
    }

    /**
     * Constructs a new DirectoryLister for listing the contents of a given directory.
     *
     * @param directory     The directory whose contents are to be listed.
     * @param recurse       If true, all child directories will be recursively listed also.
     * @param exclusions    A list of filters to exclude filenames from the returned listing.
     * @param inclusions    A list of filters to include filenames from the returned listing.
     */
    public DirectoryLister(File directory, boolean recurse, Collection<FilenameFilter> exclusions, Collection<FilenameFilter> inclusions) {
        if (directory == null) throw new NullPointerException("directory must not be null");

        this.directory = directory;
        this.recurse = recurse;

        if (exclusions != null && exclusions.size() > 0) {
            FilenameFilter exclusionsFilter = new NotFilenameFilter(new OrFilenameFilter(exclusions));
            this.directoryFilter.add(exclusionsFilter);
            this.fileFilter.add(exclusionsFilter);
        }

        if (inclusions != null && inclusions.size() > 0) {
            FilenameFilter inclusionsFilter = new OrFilenameFilter(inclusions);
            this.directoryFilter.add(inclusionsFilter);
            this.fileFilter.add(inclusionsFilter);
        }
    }

    /**
     * Returns the directory that will be listed.
     *
     * @return The directory that will be listed.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Returns whether the listing will be recursive.
     *
     * @return If true, child directories will be recursively listed also.
     */
    public boolean getRecurse() {
        return recurse;
    }

    /**
     * Lists the directory.
     *
     * @return A list of files and directories that match the specified filters.
     * @throws FileNotFoundException If the directory does not exist.
     */
    public DirectoryListing list() throws FileNotFoundException {
        return list(directory, recurse);
    }

    /**
     * Lists the given directory.
     *
     * @param directory                 The directory to be listed.
     * @param recurse                   If true, child directories will be recursively listed also.
     * @return                          A list of files and directories that match the specified filters.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    protected DirectoryListing list(File directory, boolean recurse) throws FileNotFoundException {
        if (!DirectoryHelper.exists(directory)) {
            throw new FileNotFoundException("Unable to list directory as it does not exist: " + FileHelper.normalize(directory));
        }

        String[] listing = directory.list();

        // if listing is a reasonable size, just use that for the initial capacity for our list
        // of files and directories; otherwise set capacity to 1000 and let it grow as needed
        int capacity = listing.length > 1000 ? 1000 : listing.length;
        List<File> files = new ArrayList<File>(capacity);
        List<File> directories = new ArrayList<File>(capacity);

        for (String item : listing) {
            File file = new File(directory, item);
            if (fileFilter.accept(directory, item)) {
                files.add(file);
            } else if (directoryFilter.accept(directory, item)) {
                directories.add(file);
            }

            if (recurse && DirectoryFilenameFilter.getInstance().accept(directory, item)) {
                DirectoryListing results = list(file, recurse);
                files.addAll(results.listFiles());
                directories.addAll(results.listDirectories());
            }
        }

        return new DirectoryListingImplementation(directory, directories, files);
    }

    /**
     * Encapsulates the results of a directory listing.
     */
    private static class DirectoryListingImplementation extends AbstractDirectoryListing {
        /**
         * The list of all objects, directories only, and files only that are the contents of the listed directory.
         */
        protected List<File> all, directories, files;
        /**
         * The directory which was listed.
         */
        protected File directory;

        /**
         * Constructs a new DirectoryListing given a list of files and directories.
         *
         * @param directory     The directory which produced this listing.
         * @param directoryList A list of directories to include in the listing results.
         * @param fileList      A list of files to include in the listing results.
         */
        public DirectoryListingImplementation(File directory, List<File> directoryList, List<File> fileList) {
            if (directory == null) throw new NullPointerException("directory must not be null");
            if (directoryList == null) throw new NullPointerException("directoryList must not be null");
            if (fileList == null) throw new NullPointerException("fileList must not be null");

            this.directory = directory;
            this.directories = directoryList;
            this.files = fileList;
        }

        /**
         * Returns all the files and directories in this listing.
         *
         * @return All the files and directories in this listing.
         */
        @Override
        public List<File> listAll() {
            if (all == null) {
                all = new ArrayList<File>(directories.size() + files.size());
                all.addAll(directories);
                all.addAll(files);
            }
            return all;
        }

        /**
         * Returns all the directories in this listing.
         *
         * @return All the directories in this listing.
         */
        @Override
        public List<File> listDirectories() {
            return directories;
        }

        /**
         * Returns all the files in this listing.
         *
         * @return All the files in this listing.
         */
        @Override
        public List<File> listFiles() {
            return files;
        }

        /**
         * Returns the directory which produced this listing.
         *
         * @return The directory which produced this listing.
         */
        @Override
        public File getDirectory() {
            return directory;
        }

        /**
         * Returns a string representation of this listing.
         *
         * @return A string representation of this listing.
         */
        @Override
        public String toString() {
            return listAll().toString();
        }
    }
}
