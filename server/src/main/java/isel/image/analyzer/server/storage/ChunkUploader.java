package isel.image.analyzer.server.storage;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface ChunkUploader extends Closeable {

    /**
     * Uploads the next chunk
     * @param content the chunk in question
     * @throws IOException
     */
    void upload(ByteBuffer content) throws IOException;

}
