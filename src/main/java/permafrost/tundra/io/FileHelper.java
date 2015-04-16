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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import permafrost.tundra.io.filter.RegularExpressionFilter;
import permafrost.tundra.io.filter.WildcardFilter;
import permafrost.tundra.lang.CharsetHelper;
import permafrost.tundra.lang.StringHelper;
import permafrost.tundra.net.uri.URIHelper;

/**
 * A collection of convenience methods for working with files.
 */
public class FileHelper {
    /**
     * Disallow instantiation of this class.
     */
    private FileHelper() {}

    /**
     * @return true if the file system is case insensitive.
     */
    public static boolean isCaseInsensitive() {
        return (new java.io.File("TUNDRA")).equals(new java.io.File("tundra"));
    }

    /**
     * @return true if the file system is case sensitive.
     */
    public static boolean isCaseSensitive() {
        return !isCaseInsensitive();
    }

    /**
     * Returns the MIME media type that describes the content of the given file.
     * @param file The file whose MIME type is to be returned.
     * @return     The MIME media type that describes the content of the given file.
     */
    public static String getMIMEType(File file) {
        String type = null;
        if (file != null) type = com.wm.app.b2b.server.MimeTypes.getTypeFromName(file.getName());
        if (type == null) type = "application/octet-stream";
        return type;
    }

    /**
     * Returns the MIME media type that describes the content of the given file.
     * @param filename  The file whose MIME type is to be returned.
     * @return          The MIME media type that describes the content of the given file.
     */
    public static String getMIMEType(String filename) {
        return getMIMEType(construct(filename));
    }

    /**
     * Returns true if the given file exists and is a file.
     * @param file The file to check existence of.
     * @return     true if the given file exists and is a file.
     */
    public static boolean exists(File file) {
        return file == null ? false : file.exists() && file.isFile();
    }

    /**
     * Returns true if the given file exists and is a file.
     * @param filename  The file to check existence of.
     * @return          true if the given file exists and is a file.
     */
    public static boolean exists(String filename) {
        return exists(construct(filename));
    }

    /**
     * Creates a new, empty file; if file is null, a temporary file is created.
     * @param file          The file to be created.
     * @return              The file which was created.
     * @throws IOException  If the file already exists.
     */
    public static File create(File file) throws IOException {
        if (file == null) {
            file = File.createTempFile("tundra", null);
        } else {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs(); // automatically create directories if required
            if (!file.createNewFile()) throw new IOException("Unable to create file because it already exists: " + normalize(file));
        }
        return file;
    }

    /**
     * Creates a new, empty file; if filename is null, a temporary file is created.
     * @param filename      The name of the file to be created.
     * @return              The name of the file which was created.
     * @throws IOException  If the filename is unparseable or the file already exists.
     */
    public static String create(String filename) throws IOException {
        return normalize(create(construct(filename)));
    }

    /**
     * Deletes the given file.
     * @param file          The file to be deleted.
     * @throws IOException  If the file cannot be deleted.
     */
    public static void remove(File file) throws IOException {
        if (file != null && exists(file) && !file.delete()) {
            throw new IOException("Unable to remove file: " + normalize(file));
        }
    }

    /**
     * Deletes the given file.
     * @param filename      The name of the file to be deleted.
     * @throws IOException  If the filename is unparseable or the file cannot be deleted.
     */
    public static void remove(String filename) throws IOException {
        remove(construct(filename));
    }

    /**
     * Update the modified time of the given file to the current time,
     * or create it if it does not exist.
     * @param file          The file to be touched.
     * @throws IOException  If the file cannot be created.
     */
    public static void touch(File file) throws IOException {
        if (file.exists()) {
            file.setLastModified((new java.util.Date()).getTime());
        } else {
            create(file);
        }
    }

    /**
     * Update the modified time of the given file to the current time,
     * or create it if it does not exist.
     * @param filename      The name of the file to be touched.
     * @throws IOException  If the filename is unparseable.
     */
    public static void touch(String filename) throws IOException {
        touch(construct(filename));
    }

    /**
     * Renames the source file to the target name.
     * @param source        The file to be renamed.
     * @param target        The new name of the file.
     * @throws IOException  If the file cannot be renamed.
     */
    public static void rename(File source, File target) throws IOException {
        if (source != null && target != null) {
            if (!exists(source) || exists(target) || !source.renameTo(target)) {
                throw new IOException("Unable to rename file " + normalize(source) + " to " + normalize(target));
            }
        }
    }

    /**
     * Renames the source file to the target name.
     * @param source        The file to be renamed.
     * @param target        The new name of the file.
     * @throws IOException  If file cannot be renamed.
     */
    public static void rename(String source, String target) throws IOException {
        rename(construct(source), construct(target));
    }

    /**
     * Reads the given file completely, returning the file's content as a byte[].
     * @param filename          The name of the file to be read.
     * @return                  A byte[] containing the file's content.
     * @throws IOException      If there is a problem reading the file.
     */
    public static byte[] readToBytes(String filename) throws IOException {
        return readToBytes(construct(filename));
    }

    /**
     * Reads the given file completely, returning the file's content as a byte[].
     * @param file          The file to be read.
     * @return              A byte[] containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static byte[] readToBytes(File file) throws IOException {
        byte[] content = null;

        if (file != null) {
            InputStream inputStream = null;
            ByteArrayOutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(file);
                outputStream = new ByteArrayOutputStream(StreamHelper.DEFAULT_BUFFER_SIZE);

                StreamHelper.copy(inputStream, outputStream);

                content = outputStream.toByteArray();
            } finally {
                StreamHelper.close(inputStream, outputStream);
            }
        }

        return content;
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param filename      The name of the file to be read.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(String filename) throws IOException {
        return readToString(filename, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param filename      The name of the file to be read.
     * @param charsetName      The character set the file's content is encoded with.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(String filename, String charsetName) throws IOException {
        return readToString(filename, CharsetHelper.normalize(charsetName));
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param filename      The name of the file to be read.
     * @param charset       The character set the file's content is encoded with.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(String filename, Charset charset) throws IOException {
        return readToString(construct(filename), CharsetHelper.normalize(charset));
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param file          The file to be read.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(File file) throws IOException {
        return readToString(file, CharsetHelper.DEFAULT_CHARSET);
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param file          The file to be read.
     * @param charsetName   The character set the file's content is encoded with.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(File file, String charsetName) throws IOException {
        return readToString(file, CharsetHelper.normalize(charsetName));
    }

    /**
     * Reads the given file completely, returning the file's content as a String.
     * @param file          The file to be read.
     * @param charset       The character set the file's content is encoded with.
     * @return              A String containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static String readToString(File file, Charset charset) throws IOException {
        return StringHelper.normalize(readToBytes(file), CharsetHelper.normalize(charset));
    }

    /**
     * Reads the given file completely, returning the file's content as a java.io.InputStream.
     * @param filename      The name of the file to be read.
     * @return              A java.io.InputStream containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static InputStream readToStream(String filename) throws IOException {
        return readToStream(construct(filename));
    }

    /**
     * Reads the given file completely, returning the file's content as a java.io.InputStream.
     * @param file          The file to be read.
     * @return              A java.io.InputStream containing the file's content.
     * @throws IOException  If there is a problem reading the file.
     */
    public static InputStream readToStream(File file) throws IOException {
        return new ByteArrayInputStream(readToBytes(file));
    }

    /**
     * Writes content to a file; if the given file is null, a new temporary
     * file is automatically created.
     * @param file          The file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static File writeFromStream(File file, InputStream content, boolean append) throws IOException {
        if (file == null || !exists(file)) file = create(file);
        InputStream input = StreamHelper.normalize(content);
        OutputStream output = new FileOutputStream(file, append);
        if (input != null) StreamHelper.copy(input, output);
        return file;
    }

    /**
     * Writes content to a file; if the given filename is null, a new temporary
     * file is automatically created.
     * @param filename      The name of the file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The name of the file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static String writeFromStream(String filename, InputStream content, boolean append) throws IOException {
        return normalize(writeFromStream(construct(filename), content, append));
    }

    /**
     * Writes content to a file; if the given file is null, a new temporary
     * file is automatically created.
     * @param file          The file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static File writeFromBytes(File file, byte[] content, boolean append) throws IOException {
        return writeFromStream(file, StreamHelper.normalize(content), append);
    }

    /**
     * Writes content to a file; if the given filename is null, a new temporary
     * file is automatically created.
     * @param filename      The name of the file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The name of the file which the content was written to.
     * @throws IOException  If the filename is unparseable or there is a problem writing to the file.
     */
    public static String writeFromBytes(String filename, byte[] content, boolean append) throws IOException {
        return normalize(writeFromBytes(construct(filename), content, append));
    }

    /**
     * Writes content to a file; if the given file is null, a new temporary
     * file is automatically created.
     * @param file          The file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param charsetName   The character set to encode the content with.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static File writeFromString(File file, String content, String charsetName, boolean append) throws IOException {
        return writeFromString(file, content, CharsetHelper.normalize(charsetName), append);
    }

    /**
     * Writes content to a file; if the given file is null, a new temporary
     * file is automatically created.
     * @param file          The file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param charset       The character set to encode the content with.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static File writeFromString(File file, String content, Charset charset, boolean append) throws IOException {
        return writeFromStream(file, StreamHelper.normalize(content, charset), append);
    }

    /**
     * Writes content to a file; if the given filename is null, a new temporary
     * file is automatically created.
     * @param filename      The name of the file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param charsetName   The character set to encode the content with.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The name of the file which the content was written to.
     * @throws IOException  If the filename is unparseable or there is a problem writing to the file.
     */
    public static String writeFromString(String filename, String content, String charsetName, boolean append) throws IOException {
        return writeFromString(filename, content, CharsetHelper.normalize(charsetName), append);
    }

    /**
     * Writes content to a file; if the given filename is null, a new temporary
     * file is automatically created.
     * @param filename      The name of the file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param charset       The character set to encode the content with.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The name of the file which the content was written to.
     * @throws IOException  If the filename is unparseable or there is a problem writing to the file.
     */
    public static String writeFromString(String filename, String content, Charset charset, boolean append) throws IOException {
        return normalize(writeFromString(construct(filename), content, charset, append));
    }

    /**
     * Writes content to a file; if the given file is null, a new temporary
     * file is automatically created.
     * @param file          The file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static File writeFromString(File file, String content, boolean append) throws IOException {
        return writeFromString(file, content, CharsetHelper.DEFAULT_CHARSET, append);
    }

    /**
     * Writes content to a file; if the given filename is null, a new temporary
     * file is automatically created.
     * @param filename      The name of the file to be written to; if null, a new temporary
     *                      file is automatically created.
     * @param content       The content to be written.
     * @param append        If true, the content will be appended to the file,
     *                      otherwise the content will overwrite any previous
     *                      content in the file.
     * @return              The name of the file which the content was written to.
     * @throws IOException  If there is a problem writing to the file.
     */
    public static String writeFromString(String filename, String content, boolean append) throws IOException {
        return normalize(writeFromString(construct(filename), content, append));
    }

    /**
     * Copies the content in the source file to the target file.
     * @param source        The file from which content will be copied.
     * @param target        The file to which content will be copied.
     * @param append        If true, the target content will be appended to,
     *                      otherwise any previous content will be overwritten.
     * @throws IOException  If the source file does not exist or there is a problem copying it.
     */
    public static void copy(File source, File target, boolean append) throws IOException {
        if (source != null && target != null) {
            InputStream input = new FileInputStream(source);
            OutputStream output = new FileOutputStream(target, append);
            StreamHelper.copy(input, output);
        }
    }

    /**
     * Copies the content in the source file to the target file.
     * @param source        The file from which content will be copied.
     * @param target        The file to which content will be copied.
     * @param append        If true, the target content will be appended to,
     *                      otherwise any previous content will be overwritten.
     * @throws IOException  If the source file does not exist or there is a
     *                      problem copying it.
     */
    public static void copy(String source, String target, boolean append) throws IOException {
        copy(construct(source), construct(target), append);
    }

    /**
     * Returns a java.io.File object given a file name.
     * @param filename  The name of the file, specified as a path or file:// URI.
     * @return          The file representing the given name.
     */
    public static File construct(String filename) {
        File file = null;

        if (filename != null) {
            if (filename.toLowerCase().startsWith("file:")) {
                try {
                    file = new File(new URI(filename));
                } catch(IllegalArgumentException ex) {
                    // work around java's weird handling of file://server/path style URIs on Windows, by changing the URI
                    // to be file:////server/path
                    if (filename.toLowerCase().startsWith("file://") && !filename.toLowerCase().startsWith("file:///")) {
                        file = construct("file:////" + filename.substring(6, filename.length()));
                    } else {
                        throw ex;
                    }
                } catch(URISyntaxException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else {
                file = new File(filename);
            }
        }

        return file;
    }

    /**
     * Returns the canonical file:// URI representation of the given file.
     * @param file                  The file to be normalized.
     * @return                      The canonical file:// URI representation of the given file.
     */
    public static String normalize(File file) {
        String filename = null;
        try {
            if (file != null) filename = URIHelper.normalize(file.getCanonicalFile().toURI().toString());
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        } catch(URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return filename;
    }

    /**
     * Returns the canonical file:// URI representation of the given file.
     * @param filename  The name of the file to be normalized.
     * @return          The canonical file:// URI representation of the given file.
     */
    public static String normalize(String filename) {
        return normalize(construct(filename));
    }

    /**
     * Returns true if the given file matches the given pattern.
     * @param filename                   The name of the file to check against the pattern.
     * @param pattern                    Either a regular expression or wildcard pattern.
     * @param patternIsRegularExpression Boolean indicating if the given pattern is a
     *                                   regular expression or wildcard pattern.
     * @return                           True if the given file matches the given pattern.
     */
    public static boolean match(String filename, String pattern, boolean patternIsRegularExpression) {
        return match(construct(filename), pattern, patternIsRegularExpression);
    }

    /**
     * Returns true if the given file matches the given pattern.
     * @param file                       The file to check against the pattern.
     * @param pattern                    Either a regular expression or wildcard pattern.
     * @param patternIsRegularExpression Boolean indicating if the given pattern is a
     *                                   regular expression or wildcard pattern.
     * @return                           True if the given file matches the given pattern.
     */
    public static boolean match(File file, String pattern, boolean patternIsRegularExpression) {
        boolean match = false;

        if (file != null && pattern != null) {
            java.io.FilenameFilter filter;
            if (patternIsRegularExpression) {
                filter = new RegularExpressionFilter(pattern);
            } else {
                filter = new WildcardFilter(pattern);
            }
            match = filter.accept(file.getParentFile(), file.getName());
        }

        return match;
    }
}
