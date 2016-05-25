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

import permafrost.tundra.io.CloseableHelper;
import permafrost.tundra.io.InputOutputHelper;
import permafrost.tundra.io.InputStreamHelper;
import permafrost.tundra.lang.BytesHelper;
import permafrost.tundra.lang.ObjectConvertMode;
import permafrost.tundra.lang.ObjectHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A collection of convenience methods for working with ZIP compression.
 */
public final class ZipHelper {
    /**
     * Disallow instantiation of this class.
     */
    private ZipHelper() {}

    /**
     * Compresses the given contents into a zip archive.
     *
     * @param contents The contents to be compressed.
     * @return The zip archive containing the compressed contents.
     * @throws IOException If an I/O exception occurs reading from the streams.
     */
    public static InputStream compress(ZipEntryWithData... contents) throws IOException {
        if (contents == null) return null;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = null;

        try {
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

            for (int i = 0; i < contents.length; i++) {
                if (contents[i] != null) {
                    String name = contents[i].getName();
                    if (name == null) name = "Untitled " + (i + 1);
                    InputStream inputStream = InputStreamHelper.normalize(contents[i].getData());

                    try {
                        zipOutputStream.putNextEntry(new ZipEntry(name));
                        InputOutputHelper.copy(inputStream, zipOutputStream, false);
                    } finally {
                        CloseableHelper.close(inputStream);
                    }
                }
            }
        } finally {
            if (zipOutputStream != null) zipOutputStream.closeEntry();
            CloseableHelper.close(zipOutputStream);
        }

        return (InputStream)ObjectHelper.convert(byteArrayOutputStream.toByteArray(), ObjectConvertMode.STREAM);
    }

    /**
     * Decompresses the given zip archive.
     *
     * @param inputStream The zip archive to decompress.
     * @return The decompressed contents of the zip archive.
     * @throws IOException If an I/O problem occurs while reading from the stream.
     */
    public static ZipEntryWithData[] decompress(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;

        ZipInputStream zipInputStream = null;
        List<ZipEntryWithData> contents = new ArrayList<ZipEntryWithData>();
        byte[] buffer = new byte[InputOutputHelper.DEFAULT_BUFFER_SIZE];

        try {
            zipInputStream = new ZipInputStream(InputStreamHelper.normalize(inputStream));
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //InputStreamHelper.copy(zipInputStream, byteArrayOutputStream, false);

                int count;
                while ((count = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, count);
                }
                byteArrayOutputStream.close();

                contents.add(new ZipEntryWithData(zipEntry.getName(), byteArrayOutputStream.toByteArray()));
            }
        } finally {
            CloseableHelper.close(zipInputStream);
        }

        return contents.toArray(new ZipEntryWithData[contents.size()]);
    }

    /**
     * Decompresses the given zip archive.
     *
     * @param bytes The zip archive to decompress.
     * @return The decompressed contents of the zip archive.
     * @throws IOException If an I/O problem occurs while reading from the stream.
     */
    public static ZipEntryWithData[] decompress(byte[] bytes) throws IOException {
        return decompress(InputStreamHelper.normalize(bytes));
    }

    /**
     * Decompresses the given zip archive.
     *
     * @param base64 The zip archive as a base64-encoded string.
     * @return The decompressed contents of the zip archive.
     * @throws IOException If an I/O problem occurs while reading from the stream.
     */
    public static ZipEntryWithData[] decompress(String base64) throws IOException {
        return decompress(BytesHelper.base64Decode(base64));
    }
}
