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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import permafrost.tundra.lang.ArrayHelper;
import permafrost.tundra.lang.ExceptionHelper;
import permafrost.tundra.lang.IterableHelper;
import permafrost.tundra.time.DateTimeHelper;
import javax.xml.datatype.Duration;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * Deletes the given directory.
     *
     * @param  directory    The directory to be deleted.
     * @param  recurse      If true, all child directories and files will also be recursively deleted.
     * @throws IOException  If the directory cannot be deleted.
     */
    public static void remove(File directory, boolean recurse) throws IOException {
        if (exists(directory)) {
            if (recurse) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File item : files) {
                        if (item.isFile()) {
                            FileHelper.remove(item);
                        } else {
                            remove(item, recurse);
                        }
                    }
                }
            }
            if (!directory.delete()) {
                throw new IOException("Unable to remove directory: " + FileHelper.normalize(directory));
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
    public static void rename(File source, File target) throws IOException {
        if (source != null && target != null) {
            if (!exists(source) || exists(target) || !source.renameTo(target)) {
                throw new IOException("Unable to rename directory '" + FileHelper.normalize(source) + "' to '" + FileHelper.normalize(target) + "'");
            }
        }
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
        String[] listing;
        if (!exists(directory) || (listing = directory.list()) == null) {
            throw new FileNotFoundException("Unable to list directory as it either does not exist, access is denied, or an IO error occurred: " + FileHelper.normalize(directory));
        }
        return ArrayHelper.sort(listing);
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
        return purge(directory, duration == null ? null : DateTimeHelper.earlier(duration), filter, recurse);
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

        List<Throwable> exceptions = new ArrayList<Throwable>();

        for (String item : list(directory)) {
            try {
                File child = new File(directory, item);
                if (child.exists()) {
                    if (child.isFile() && (filter == null || filter.accept(directory, item))) {
                        boolean shouldPurge = true;

                        if (olderThan != null) {
                            Calendar modified = Calendar.getInstance();
                            modified.setTime(new Date(child.lastModified()));
                            shouldPurge = modified.compareTo(olderThan) <= 0;
                        }

                        if (shouldPurge && child.delete()) count += 1;
                    } else if (recurse && child.isDirectory()) {
                        count += purge(child, olderThan, filter, recurse);
                    }
                }
            } catch(IOException ex) {
                exceptions.add(ex);
            }
        }

        if (exceptions.size() > 0) {
            ExceptionHelper.raiseUnchecked(exceptions);
        }

        return count;
    }

    /**
     * Compresses the files in the given directory into a zip archive.
     *
     * @param directory             The directory to be compressed.
     * @param filter                An optional filter to determine which files to include in the archive.
     * @param recurse               Whether to also recursively archive subdirectories.
     * @param includeParentInPath   Whether the parent directory should be included in the filename paths.
     * @return                      An input stream from which the resulting zip archive can be read.
     * @throws IOException          If an IO error occurs.
     */
    public static InputStream zip(File directory, FilenameFilter filter, boolean recurse, boolean includeParentInPath) throws IOException {
        File temporaryFile = FileHelper.create();
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(temporaryFile), InputOutputHelper.DEFAULT_BUFFER_SIZE));

        Deque<String> path = new ArrayDeque<String>();
        if (includeParentInPath) path.add(directory.getName() + "/");

        try {
            zip(zipOutputStream, path, directory, filter, recurse);
        } finally {
            CloseableHelper.close(zipOutputStream);
        }

        return new BufferedInputStream(new DeleteOnCloseFileInputStream(temporaryFile), InputOutputHelper.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Compresses the files in the given directory into a zip archive.
     *
     * @param zipOutputStream       The zip stream to write the directory's contents to.
     * @param path                  The path prefix for all file names.
     * @param directory             The directory to compress.
     * @param filter                An optional filter to determine which files to include in the archive.
     * @param recurse               Whether to also recursively archive subdirectories.
     * @throws IOException          If an IO error occurs.
     */
    private static void zip(ZipOutputStream zipOutputStream, Deque<String> path, File directory, FilenameFilter filter, boolean recurse) throws IOException {
        if (zipOutputStream == null || directory == null) return;

        for (String item : list(directory)) {
            File child = new File(directory, item);
            if (child.isFile() && (filter == null || filter.accept(directory, item))) {
                zipOutputStream.putNextEntry(new ZipEntry(IterableHelper.join(path, null, false) + item));
                InputStream childInputStream = new BufferedInputStream(new FileInputStream(child), InputOutputHelper.DEFAULT_BUFFER_SIZE);
                try {
                    InputOutputHelper.copy(childInputStream, zipOutputStream, false);
                } finally {
                    CloseableHelper.close(childInputStream);
                    zipOutputStream.closeEntry();
                }
            } else if (recurse && child.isDirectory()) {
                path.add(child.getName() + "/");
                zip(zipOutputStream, path, child, filter, recurse);
                path.removeLast();
            }
        }
    }

    /**
     * Adds the files in the given directory to a tar archive.
     *
     * @param directory             The directory to be archived.
     * @param filter                An optional filter to determine which files to include in the archive.
     * @param recurse               Whether to also recursively archive subdirectories.
     * @param includeParentInPath   Whether the parent directory should be included in the filename paths.
     * @param gzip                  Whether to gzip the resulting tar archive.
     * @return                      An input stream from which the resulting tar archive can be read.
     * @throws IOException          If an IO error occurs.
     */
    public static InputStream tar(File directory, FilenameFilter filter, boolean recurse, boolean includeParentInPath, boolean gzip) throws IOException {
        File temporaryFile = FileHelper.create();

        OutputStream outputStream = new FileOutputStream(temporaryFile);
        if (gzip) outputStream = new GZIPOutputStream(outputStream);
        TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(new BufferedOutputStream(outputStream, InputOutputHelper.DEFAULT_BUFFER_SIZE));

        tarOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        tarOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        Deque<String> path = new ArrayDeque<String>();
        if (includeParentInPath) path.add(directory.getName() + "/");

        try {
            tar(tarOutputStream, path, directory, filter, recurse);
        } finally {
            CloseableHelper.close(tarOutputStream);
        }

        return new BufferedInputStream(new DeleteOnCloseFileInputStream(temporaryFile), InputOutputHelper.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Adds the files in the given directory into a tar archive.
     *
     * @param tarOutputStream       The tar stream to write the directory's contents to.
     * @param path                  The path prefix for all file names.
     * @param directory             The directory to compress.
     * @param filter                An optional filter to determine which files to include in the archive.
     * @param recurse               Whether to also recursively archive subdirectories.
     * @throws IOException          If an IO error occurs.
     */
    private static void tar(TarArchiveOutputStream tarOutputStream, Deque<String> path, File directory, FilenameFilter filter, boolean recurse) throws IOException {
        if (tarOutputStream == null || directory == null) return;

        for (String item : list(directory)) {
            File child = new File(directory, item);
            if (child.isFile() && (filter == null || filter.accept(directory, item))) {
                TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(child, IterableHelper.join(path, null, false) + item);
                tarOutputStream.putArchiveEntry(tarArchiveEntry);
                try {
                    tarOutputStream.write(InputStreamHelper.read(new FileInputStream(child)));
                } finally {
                    tarOutputStream.closeArchiveEntry();
                }
            } else if (recurse && child.isDirectory()) {
                path.add(child.getName() + "/");
                tar(tarOutputStream, path, child, filter, recurse);
                path.removeLast();
            }
        }
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
    public static File join(String... path) {
        return join(null, path);
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
    public static BigInteger size(File directory, boolean recurse) throws IOException {
        if (!exists(directory)) throw new FileNotFoundException("Unable to calculate size of directory as it does not exist: " + FileHelper.normalize(directory));

        BigInteger totalSize = BigInteger.ZERO;
        File[] children = directory.listFiles();

        if (children != null) {
            for (File child : children) {
                if (FileHelper.exists(child)) {
                    totalSize = totalSize.add(BigInteger.valueOf(child.length()));
                } else if (recurse && exists(child)) {
                    totalSize = totalSize.add(size(child, recurse));
                }
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

    /**
     * Compresses files in the given directory, and child directories if recurse is true, older than the given
     * duration using gzip.
     *
     * @param directory     The directory whose files are to be compressed.
     * @param olderThan     Only files modified prior to this datetime will be compressed.
     * @param filter        An optional FilenameFilter used to filter which files are compressed.
     * @param recurse       If true, then child files and directories will also be recursively compressed.
     * @param replace       Whether the original file should be deleted once compressed.
     * @return              The number of files compressed.
     * @throws IOException  If an IO error occurs.
     */
    public static long gzip(File directory, Calendar olderThan, FilenameFilter filter, boolean recurse, boolean replace) throws IOException {
        long count = 0;

        List<Throwable> exceptions = new ArrayList<Throwable>();

        for (String item : list(directory)) {
            try {
                File child = new File(directory, item);
                if (child.exists()) {
                    if (child.isFile() && (filter == null || filter.accept(directory, item))) {
                        boolean shouldCompress = true;

                        if (olderThan != null) {
                            Calendar modified = Calendar.getInstance();
                            modified.setTime(new Date(child.lastModified()));
                            shouldCompress = modified.compareTo(olderThan) <= 0;
                        }

                        if (shouldCompress) {
                            FileHelper.gzip(child, replace);
                            count += 1;
                        }
                    } else if (recurse && child.isDirectory()) {
                        count += gzip(child, olderThan, filter, recurse, replace);
                    }
                }
            } catch(IOException ex) {
                exceptions.add(ex);
            }
        }

        if (exceptions.size() > 0) {
            ExceptionHelper.raiseUnchecked(exceptions);
        }

        return count;
    }

    /**
     * Compresses files in the given directory, and child directories if recurse is true, older than the given
     * duration using gzip.
     *
     * @param directory     The directory whose files are to be compressed.
     * @param duration      The age files must be before they are deleted.
     * @param filter        An optional FilenameFilter used to filter which files are compressed.
     * @param recurse       If true, then child files and directories will also be recursively compressed.
     * @param replace       Whether the original file should be deleted once compressed.
     * @return              The number of files compressed.
     * @throws IOException  If an IO error occurs.
     */
    public static long gzip(File directory, Duration duration, FilenameFilter filter, boolean recurse, boolean replace) throws IOException {
        return gzip(directory, duration == null ? null : DateTimeHelper.earlier(duration), filter, recurse, replace);
    }
}
