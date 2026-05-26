package isel.image.analyzer.server.storage;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

public class StorageOperations {

    private final static String bucketName = "cn_g08_europe"; 

    private final Storage storage;

    public StorageOperations(@Nonnull Storage storage) {
        this.storage = storage;
    }

    public ChunkUploader uploadImage(String fileName) { //"image/png"

        String contentType = "image/" + fileName.substring(fileName.lastIndexOf('.') + 1);

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        return new BlobUploader(blobInfo);

    }

    private class BlobUploader implements ChunkUploader {

        private final WriteChannel channel;

        public BlobUploader(BlobInfo blobInfo) {
            channel = storage.writer(blobInfo);
        }

        public void upload(ByteBuffer content) throws IOException {
            channel.write(content);
        }

        public void close() throws IOException {
            channel.close();
        }


    }

    public void updateAllImage(String fileName, byte[] content) throws Exception {

        String contentType = "image/" + fileName.substring(fileName.lastIndexOf('.') + 1);

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        // create the blob in one request.
        storage.create(blobInfo, content);


    }

}
