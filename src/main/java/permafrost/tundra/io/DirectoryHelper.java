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
import java.io.IOException;
import java.util.Calendar;
import javax.xml.datatype.Duration;

/**
 * A collection of convenience methods for working with file system directories.
 */
public class DirectoryHelper {
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
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(File directory, Duration duration, boolean recurse) throws FileNotFoundException {
        return purge(directory, DateTimeHelper.earlier(duration), recurse);
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  duration                 The age files must be before they are deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(String directory, Duration duration, boolean recurse) throws FileNotFoundException {
        return purge(FileHelper.construct(directory), duration, recurse);
    }

    /**
     * Deletes all files in the given directory, and child directories if recurse is true, older than the given
     * duration.
     *
     * @param  directory                The directory to be purged.
     * @param  olderThan                Only files modified prior to this datetime will be deleted.
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(File directory, Calendar olderThan, boolean recurse) throws FileNotFoundException {
        long count = 0;

        for (String item : list(directory)) {
            File child = new File(directory, item);
            if (child.exists()) {
                if (child.isFile()) {
                    Calendar modified = Calendar.getInstance();
                    modified.setTime(new java.util.Date(child.lastModified()));
                    if (modified.compareTo(olderThan) <= 0 && child.delete()) count += 1;
                } else if (recurse && child.isDirectory()) {
                    count += purge(child, olderThan, recurse);
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
     * @param  recurse                  If true, then child files and directories will also be recursively purged.
     * @return                          The number of files deleted.
     * @throws FileNotFoundException    If the directory does not exist.
     */
    public static long purge(String directory, Calendar olderThan, boolean recurse) throws FileNotFoundException {
        return purge(FileHelper.construct(directory), olderThan, recurse);
    }

    /**
     * Creates a new path given a parent directory and child item.
     *
     * @param  parent   The parent directory.
     * @param  child    The child item.
     * @return          A new path
     */
    public static File join(File parent, String child) {
        if (parent == null) return null;
        if (child == null) return parent;
        return new File(parent, child);
    }

    /**
     * Creates a new path given a parent directory and child item.
     *
     * @param  parent   The parent directory.
     * @param  child    The child item.
     * @return          A new path
     */
    public static String join(String parent, String child) {
        return FileHelper.normalize(join(FileHelper.construct(parent), child));
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
}
