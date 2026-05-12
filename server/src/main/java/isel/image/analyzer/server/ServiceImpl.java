package isel.image.analyzer.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.storage.Storage;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageCharacteristics;
import image.analyzer.ImageIdentifier;
import image.analyzer.ImageNames;
import image.analyzer.ImageSend;
import image.analyzer.SearchByDateIntervalAndLabel;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Making use of {@link ServerCallStreamObserver} casting to add on cancel guardrails, if users stops responding before
 * unsubscribing. There may be other issues such as when user loses internet connection, but avoids terminal app
 * simply being closed without all topic unsubscribe
 */
public class ServiceImpl extends ImageAnalyserGrpc.ImageAnalyserImplBase {

    private final StorageOperations storageOperations;

    public  ServiceImpl(StorageOperations storageOperations) {
        this.storageOperations = storageOperations;
    }

    @Override
    public StreamObserver<ImageSend> publishImage(StreamObserver<ImageIdentifier> responseObserver) {

        //testing "reconstruction" of file sent by client

        return new StreamObserver<ImageSend>() {

            final Path download = Path.of(System.getProperty("user.home"), "Downloads");

            final String id = UUID.randomUUID().toString();

            final FileOutputStream fileOutputStream;

            {
                try {
                    fileOutputStream = new FileOutputStream(download.resolve(id + ".png").toFile());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNext(ImageSend value) {


                try {

                    byte[] arr = value.getChunkData().toByteArray(); value.getChunkData().asReadOnlyByteBuffer();

                    fileOutputStream.write(arr);


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                responseObserver.onNext(ImageIdentifier.newBuilder().setId(id).build());
                responseObserver.onCompleted();

            }
        };

    }

    @Override
    public void retrieveImageCharacteristics(ImageIdentifier request, StreamObserver<ImageCharacteristics> responseObserver) {

        List<String> list = new LinkedList<>();

        list.add("mock"); list.add("fake_result");

        responseObserver.onNext(ImageCharacteristics.newBuilder().addAllCharacteristic(list).build());
        responseObserver.onCompleted();

    }

    @Override
    public void imagesSearch(SearchByDateIntervalAndLabel request, StreamObserver<ImageNames> responseObserver) {

        List<String> list = new LinkedList<>();

        list.add("mock"); list.add("fake_result");

        responseObserver.onNext(ImageNames.newBuilder().addAllName(list).build());
        responseObserver.onCompleted();

    }


}
