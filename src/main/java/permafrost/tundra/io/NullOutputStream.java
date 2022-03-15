package permafrost.tundra.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream which silently ignores all data written to it.
 */
public class NullOutputStream extends OutputStream {
    /**
     * Writes the specified data to this stream. As this stream silently ignores all data written to it, this method
     * does nothing.
     *
     * @param data          The data to be written.
     * @throws IOException  If an IO error occurs.
     */
    @Override
    public void write(int data) throws IOException {
        // do nothing
    }
}
