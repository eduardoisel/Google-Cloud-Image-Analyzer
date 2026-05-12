package isel.image.analyzer.server;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.nio.ByteBuffer;

public class StorageOperations {

    private final static String bucketName = "cn_g08_europe"; //todo maybe change to a new one

    private final Storage storage;

    public StorageOperations(Storage storage) {
        this.storage = storage;
    }

    public ChunkUploader uploadImage(String fileName) { //"image/png"

        String contentType = "image/" + fileName.substring(fileName.lastIndexOf('.') + 1);

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

        return new ChunkUploader(blobInfo);

    }

    public class ChunkUploader {

        private final WriteChannel channel;

        public ChunkUploader(BlobInfo blobInfo) {
            channel = storage.writer(blobInfo);
        }

        public void upload(ByteBuffer content) throws Exception {
            channel.write(content);
        }

        public void close() throws Exception {
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
