package permafrost.tundra.io;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TranscodingInputStreamTest {
    @Test
    public void testRead() throws Exception {
        String testString = "abc";

        Charset sourceCharset = Charset.forName("UTF-8");
        Charset targetCharset = Charset.forName("UTF-32");

        byte[] sourceBytes = testString.getBytes(sourceCharset);
        InputStream in = InputStreamHelper.normalize(sourceBytes);

        TranscodingInputStream transcodingInputStream = new TranscodingInputStream(in, sourceCharset, targetCharset);

        byte[] targetBytes = InputStreamHelper.read(transcodingInputStream);

        assertArrayEquals(testString.getBytes(targetCharset), targetBytes);
    }
}