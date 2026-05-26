package isel.image.analyzer.server;

import com.google.cloud.Timestamp;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageCharacteristics;
import image.analyzer.ImageIdentifier;
import image.analyzer.ImageNames;
import image.analyzer.ImageSend;
import image.analyzer.SearchByDateIntervalAndLabel;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import isel.image.analyzer.server.firestore.FirestoreOperations;
import isel.image.analyzer.server.firestore.ImageInfo;
import isel.image.analyzer.server.storage.ChunkUploader;
import isel.image.analyzer.server.storage.StorageOperations;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Making use of {@link ServerCallStreamObserver} casting to add on cancel guardrails, if users stops responding before
 * unsubscribing. There may be other issues such as when user loses internet connection, but avoids terminal app
 * simply being closed without all topic unsubscribe
 */
public class ImageAnalyzerService extends ImageAnalyserGrpc.ImageAnalyserImplBase {

    private final StorageOperations storageOperations;

    private final PubSub pubSub;

    private final FirestoreOperations firestoreOperations;

    public ImageAnalyzerService(StorageOperations storageOperations, PubSub pubSub, FirestoreOperations firestoreOperations) {
        this.storageOperations = storageOperations;
        this.pubSub = pubSub;
        this.firestoreOperations = firestoreOperations;
    }

    @Override
    public StreamObserver<ImageSend> publishImage(StreamObserver<ImageIdentifier> responseObserver) {

        return new StreamObserver<ImageSend>() {

            final String id = UUID.randomUUID().toString();

            String fileName = null;

            ChunkUploader chunkUploader;


            @Override
            public void onNext(ImageSend value) {

                try {

                    if (fileName == null) {
                        fileName = value.getName();
                        chunkUploader = storageOperations.uploadImage(id);
                    }

                    chunkUploader.upload(value.getChunkData().asReadOnlyByteBuffer());


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {


                responseObserver.onNext(ImageIdentifier.newBuilder().setId(id).build());
                try {
                    chunkUploader.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                responseObserver.onCompleted();

                try {
                    pubSub.publishMessage(id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        };

    }

    @Override
    public void retrieveImageCharacteristics(ImageIdentifier request, StreamObserver<ImageCharacteristics> responseObserver) {

        try {
            ImageInfo imageInfo = firestoreOperations.search(request.getId());

            if (imageInfo == null) {
                responseObserver.onError(new StatusException(Status.NOT_FOUND));
                return;
            }

            //only sending labels without certainties
            responseObserver.onNext(ImageCharacteristics.newBuilder().addAllCharacteristic(imageInfo.labelNames()).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusException(Status.ABORTED));
        }


    }

    @Override
    public void imagesSearch(SearchByDateIntervalAndLabel request, StreamObserver<ImageNames> responseObserver) {


        try {
            List<ImageInfo> imageInfo = firestoreOperations.search(
                    Timestamp.fromProto(request.getStartDate()),
                    Timestamp.fromProto(request.getEndDate()),
                    request.getLabel());

            //only sending ids
            responseObserver
                    .onNext(ImageNames.newBuilder().addAllName(imageInfo.stream().map(ImageInfo::id).toList()).build());

            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(new StatusException(Status.ABORTED));
        }

    }


}
