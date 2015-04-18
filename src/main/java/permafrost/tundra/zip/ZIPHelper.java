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

package permafrost.tundra.zip;

import permafrost.tundra.io.StreamHelper;
import permafrost.tundra.lang.ObjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A collection of convenience methods for working with ZIP compression.
 */
public class ZIPHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ZIPHelper() {}

    /**
     * Compresses the given contents into a zip archive.
     * @param contents      The contents to be compressed.
     * @return              The zip archive containing the compressed contents.
     * @throws IOException  If an I/O exception occurs reading from the streams.
     */
    public static InputStream compress(NamedContent... contents) throws IOException {
        if (contents == null) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = null;

        try {
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            int i = 0;
            for (NamedContent content : contents) {
                String name = content.getName();
                InputStream inputStream = content.getData();

                try {
                    zipOutputStream.putNextEntry(name == null ? new ZipEntry("Untitled " + (i + 1)) : new ZipEntry(name));
                    StreamHelper.copy(inputStream, zipOutputStream, false);
                } finally {
                    zipOutputStream.closeEntry();
                    StreamHelper.close(inputStream);
                }
                i++;
            }
        } finally {
            StreamHelper.close(zipOutputStream);
        }

        return (InputStream)ObjectHelper.convert(byteArrayOutputStream == null ? null : byteArrayOutputStream.toByteArray(), ObjectHelper.ConvertMode.STREAM);
    }

    /**
     * Decompresses the given zip archive.
     * @param inputStream  The zip archive to decompress.
     * @return             The decompressed contents of the zip archive.
     * @throws IOException If an I/O problem occurs while reading from the stream.
     */
    public static NamedContent[] decompress(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;

        ZipInputStream zipInputStream = null;
        List<NamedContent> contents = new LinkedList<NamedContent>();

        try {
            zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            while((zipEntry = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                StreamHelper.copy(zipInputStream, byteArrayOutputStream, false);
                contents.add(new NamedContent(zipEntry.getName(), byteArrayOutputStream.toByteArray()));
            }
        } finally {
            StreamHelper.close(zipInputStream);
        }

        return contents.toArray(new NamedContent[contents.size()]);
    }
}
