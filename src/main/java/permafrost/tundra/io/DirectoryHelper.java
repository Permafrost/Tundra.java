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

import permafrost.tundra.time.DateTimeHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.Duration;

/**
 * A collection of convenience methods for working with file system directories.
 */
public final class DirectoryHelper {
    /**
     * Disallow instantiation of this class;
     */
    private DirectoryHelper() {}

    /**
     * Creates a new directory.
     *
     * @param  directory    The directory to be created.
     * @throws IOException  If the directory already exists or otherwise cannot be created.
     */
    public static void create(File directory) throws IOException {
        create(directory, true);
    }

    /**
     * Creates a new directory.
     *
     * @param  directory    The directory to be created.
     * @throws IOException  If the directory already exists or otherwise cannot be created.
     */
    public static void create(String directory) throws IOException {
        create(directory, true);
    }

    /**
     * Creates a new directory.
     *
     * @param  directory    The directory to be created.
     * @param  raise        If true, will throw an exception if the creation of the directory fails.
     * @throws IOException  If raise is true and the directory already exists or otherwise cannot be created.
     */
    public static void create(File directory, boolean raise) throws IOException {
        if (directory != null) {
            if (raise || !exists(directory)) {
                if (!directory.mkdirs()) {
                    throw new IOException("Unable to create directory: " + FileHelper.normalize(directory));
                }
            }
        }
    }

    /**
     * Creates a new directory.
     *
     * @param  directory    The directory to be created.
     * @param  raise        If true, will throw an exception if the creation of the directory fails.
     * @throws IOException  If raise is true and the directory already exists or otherwise cannot be created.
     */
    public static void create(String directory, boolean raise) throws IOException {
        create(FileHelper.construct(directory), raise);
    }

    /**
     * Returns true if the given directory exists and is a directory.
     *
     * @param  directory The directory to check existence of.
     * @return           True if the directory exists and is a directory.
     */
    public static boolean exists(File directory) {
        if (directory == null) return false;
        return directory.exists() && directory.isDirectory();
    }

    /**
     * Returns true if the given directory exists and is a directory.
     *
     * @param  directory The directory to check existence of.
     * @return           True if the directory exists and is a directory.
     */
    public static boolean exists(String directory) {
        return exists(FileHelper.construct(directory));
    }

    /**
     * Deletes the given directory.
     *
     * @param  directory    The directory to be deleted.
     * @param  recurse      If true, all child directories and files will also be recursively deleted.
     * @throws IOException  If the directory cannot be deleted.
     */
    public static void remove(File directory, boolean recurse) throws IOException {
        if (exists(directory)) {
            if (recurse) {
                for (File item : directory.listFiles()) {
                    if (item.isFile()) {
                        FileHelper.remove(item);
                    } else {
                        remove(item, recurse);
                    }
                }
            }
            if (!directory.delete()) {
                throw new IOException("Unable to remove directory: " + FileHelper.normalize(directory));
            }
        }
    }

    /**
     * Deletes the given directory.
     *
     * @param  directory    The directory to be deleted.
     * @param  recurse      If true, all child directories and files will also be recursively deleted.
     * @throws IOException  If the directory cannot be deleted.
     */
    public static void remove(String directory, boolean recurse) throws IOException {
        remove(FileHelper.construct(directory), recurse);
    }

    /**
     * Renames a directory.
     *
     * @param  source       The directory to be renamed.
     * @param  target       The new name for the directory.
     * @throws IOException  If the directory cannot be renamed.
     */
    public static void rename(File source, File target) throws IOException {
        if (source != null && target != null) {
            if (!exists(source) || exists(target) || !source.renameTo(target)) {
                throw new IOException("Unable to rename directory '" + FileHelper.normalize(source) + "' to '" + FileHelper.normalize(target) + "'");
            }
        }
    }

    /**
     * Renames a directory.
     *
     * @param  source       The directory to be renamed.
     * @param  target       The new name for the directory.
     * @throws IOException  If the directory cannot be renamed.
     */
    public static void rename(String source, String target) throws IOException {
        rename(FileHelper.construct(source), FileHelper.construct(target));
    }

    /**
     * Returns a raw directory listing with no additional processing: useful for when performance takes priority over
     * ease of use; for example, when the directory contains hundreds of thousands or more files.
     *
     * @param  directory                The directory to list.
     * @return                          The list of item names in the given directory.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static String[] list(String directory) throws FileNotFoundException {
        return list(FileHelper.construct(directory));
    }

    /**
     * Returns a raw directory listing with no additional processing: useful for when performance takes priority over
     * ease of use; for example, when the directory contains hundreds of thousands or more files.
     *
     * @param  directory                The directory to list.
     * @return                          The list of item names in the given directory.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static String[] list(File directory) throws FileNotFoundException {
        if (!exists(directory)) {
            throw new FileNotFoundException("Unable to list directory as it does not exist: " + FileHelper.normalize(directory));
        }
        return directory.list();
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  duration                 The age files must be before they are deleted.
     * @param  filter                   An optional FilenameFilter used to filter which files are deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(File directory, Duration duration, FilenameFilter filter, boolean recurse) throws FileNotFoundException {
        return purge(directory, DateTimeHelper.earlier(duration), filter, recurse);
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  duration                 The age files must be before they are deleted.
     * @param  filter                   An optional FilenameFilter used to filter which files are deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(String directory, Duration duration, FilenameFilter filter, boolean recurse) throws FileNotFoundException {
        return purge(FileHelper.construct(directory), duration, filter, recurse);
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  olderThan                Only files modified prior to this datetime will be deleted.
     * @param  filter                   An optional FilenameFilter used to filter which files are deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(File directory, Calendar olderThan, FilenameFilter filter, boolean recurse) throws FileNotFoundException {
        long count = 0;

        for (String item : list(directory)) {
            File child = new File(directory, item);
            if (child.exists()) {
                if (child.isFile() && (filter == null || filter.accept(directory, item))) {
                    Calendar modified = Calendar.getInstance();
                    modified.setTime(new java.util.Date(child.lastModified()));
                    if (modified.compareTo(olderThan) <= 0 && child.delete()) count += 1;
                } else if (recurse && child.isDirectory()) {
                    count += purge(child, olderThan, filter, recurse);
                }
            }
        }

        return count;
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  olderThan                Only files modified prior to this datetime will be deleted.
     * @param  filter                   An optional FilenameFilter used to filter which files are deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(String directory, Calendar olderThan, FilenameFilter filter, boolean recurse) throws FileNotFoundException {
        return purge(FileHelper.construct(directory), olderThan, filter, recurse);
    }

    /**
     * Creates a new path given a parent directory and children.
     *
     * @param  parent   The parent directory.
     * @param  children The child path items.
     * @return          A new path.
     */
    public static File join(File parent, String... children) {
        File path = null;

        if (parent != null || (children != null && children.length > 0)) {
            if (parent != null) path = parent;

            if (children != null) {
                for (String child : children) {
                    if (path == null) {
                        path = FileHelper.construct(child);
                    } else {
                        path = new File(path, child);
                    }
                }
            }
        }

        return path;
    }

    /**
     * Creates a new path given a list of path items.
     *
     * @param  path     The path items.
     * @return          A new path.
     */
    public static String join(String... path) {
        return FileHelper.normalize(join(null, path));
    }

    /**
     * Deletes all empty child directories in the given directory, and
     * optionally deletes the given directory itself if empty.
     *
     * @param  directory    The directory to be compacted.
     * @param  deleteSelf   If true the given directory will also be deleted if empty
     * @throws IOException  If the directory could not be deleted.
     */
    public static void compact(File directory, boolean deleteSelf) throws IOException {
        if (directory == null || !directory.isDirectory()) return;

        File[] children = directory.listFiles();
        Throwable cause = null;

        if (children != null) {
            // compact all child directories recursively
            for (File child : children) {
                if (child.isDirectory()) {
                    try {
                        compact(child, true);
                    } catch (IOException ex) {
                        cause = ex;
                    }
                }
            }

            // after compacting children, delete this directory if required
            if (deleteSelf) {
                // re-list files after compacting children
                if (children.length > 0) children = directory.listFiles();

                // delete this directory if it is empty
                if (children != null && children.length == 0 && !directory.delete()) {
                    throw new IOException("Directory could not be deleted: " + FileHelper.normalize(directory));
                } else if (cause != null) {
                    throw new IOException("Directory could not be deleted: " + FileHelper.normalize(directory), cause);
                }
            }
        }
    }

    /**
     * Returns the total size in bytes of all files in the given directory.
     *
     * @param directory     The directory to calculate the size of.
     * @param recurse       If true, will recursively calculate the size of the entire directory tree including all
     *                      child directories.
     * @return              The total size in bytes of all files in the given directory.
     * @throws IOException  If the given directory does not exist or is a file.
     */
    public static BigInteger size(String directory, boolean recurse) throws IOException {
        return size(FileHelper.construct(directory), recurse);
    }

    /**
     * Returns the total size in bytes of all files in the given directory.
     *
     * @param directory     The directory to calculate the size of.
     * @param recurse       If true, will recursively calculate the size of the entire directory tree including all
     *                      child directories.
     * @return              The total size in bytes of all files in the given directory.
     * @throws IOException  If the given directory does not exist or is a file.
     */
    public static BigInteger size(File directory, boolean recurse) throws IOException {
        if (!exists(directory)) throw new FileNotFoundException("Unable to calculate size of directory as it does not exist: " + FileHelper.normalize(directory));

        BigInteger totalSize = BigInteger.ZERO;
        File[] children = directory.listFiles();

        for (File child : children) {
            if (FileHelper.exists(child)) {
                totalSize = totalSize.add(BigInteger.valueOf(child.length()));
            } else if (recurse && exists(child)) {
                totalSize = totalSize.add(size(child, recurse));
            }
        }

        return totalSize;
    }

    /**
     * Reduces the size in bytes of a directory to an allowable size by deleting the least recently used
     * files.
     *
     * @param directory     The directory to be squeezed.
     * @param allowedSize   The allowable size of the directory in bytes.
     * @param filter        An optional FilenameFilter used to filter which files are deleted.
     * @param recurse       If true, child directories will be included in the total size and their files
     *                      may be deleted when reducing the total size of the parent.
     * @return              The resulting total size in bytes of all files in the given directory.
     * @throws IOException  If the given directory does not exist or is not a file.
     */
    public static BigInteger squeeze(String directory, BigInteger allowedSize, FilenameFilter filter, boolean recurse) throws IOException {
        return squeeze(FileHelper.construct(directory), allowedSize, filter, recurse);
    }

    /**
     * Reduces the size in bytes of a directory to an allowable size by deleting the least recently used
     * files.
     *
     * @param directory     The directory to be squeezed.
     * @param allowedSize   The allowable size of the directory in bytes.
     * @param filter        An optional FilenameFilter used to filter which files are deleted.
     * @param recurse       If true, child directories will be included in the total size and their files
     *                      may be deleted when reducing the total size of the parent.
     * @return              The resulting total size in bytes of all files in the given directory.
     * @throws IOException  If the given directory does not exist or is not a file.
     */
    public static BigInteger squeeze(File directory, BigInteger allowedSize, FilenameFilter filter, boolean recurse) throws IOException {
        BigInteger totalSize = size(directory, recurse);

        if (allowedSize != null && totalSize.compareTo(allowedSize) > 0) {
            BigInteger requiredReductionSize = totalSize.subtract(allowedSize);

            DirectoryLister directoryLister = new DirectoryLister(directory, filter, recurse);
            DirectoryListing directoryListing = directoryLister.list();
            List<File> files = directoryListing.listFiles();

            Collections.sort(files, new FileModificationComparator(true));

            BigInteger totalReductionSize = BigInteger.ZERO;

            for (File file : files) {
                if (FileHelper.exists(file)) {
                    long fileSize = file.length();
                    if (file.delete()) totalReductionSize = totalReductionSize.add(BigInteger.valueOf(fileSize));
                }

                if (totalReductionSize.compareTo(requiredReductionSize) > 0) {
                    break;
                }
            }
        }

        return size(directory, recurse);
    }
}
