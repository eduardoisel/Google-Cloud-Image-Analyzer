package isel.image.analyzer.server;

import com.google.protobuf.ByteString;
import com.google.cloud.Timestamp;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageIdentifier;
import image.analyzer.ImageNames;
import image.analyzer.ImageSend;
import image.analyzer.SearchByDateIntervalAndLabel;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import isel.image.analyzer.server.firestore.FirestoreOperations;
import isel.image.analyzer.server.firestore.ImageInfo;
import isel.image.analyzer.server.firestore.LabelInfo;
import isel.image.analyzer.server.storage.ChunkUploader;
import isel.image.analyzer.server.storage.StorageOperations;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageAnalyzerServiceTest {

    private ImageAnalyserGrpc.ImageAnalyserBlockingStub blockingStub;
    private ImageAnalyserGrpc.ImageAnalyserStub noBlockingStub;

    private Server inProcessServer;
    private ManagedChannel managedChannel;

    @Mock
    StorageOperations storageOperations;

    @Mock
    PubSub pubSub;

    @Mock
    FirestoreOperations firestoreOperations;

    @BeforeEach
    void setup() throws IOException {
        String serviceName = InProcessServerBuilder.generateName();

        inProcessServer = InProcessServerBuilder.forName(serviceName)
                .directExecutor()
                .addService(new ImageAnalyzerService(storageOperations, pubSub, firestoreOperations))
                .build()
                .start();

        managedChannel = InProcessChannelBuilder.forName(serviceName)
                .directExecutor()
                .usePlaintext()
                .build();

        blockingStub = ImageAnalyserGrpc.newBlockingStub(managedChannel);
        noBlockingStub = ImageAnalyserGrpc.newStub(managedChannel);
    }

    @Test
    void saveImageTest() {

        String path = ImageAnalyzerServiceTest.class.getResource("/birdie.jpg").getPath();
        File file = new File(path);


        StreamObserver<ImageIdentifier> identifierStreamObserver = new StreamObserver<ImageIdentifier>() {
            @Override
            public void onNext(ImageIdentifier value) {

            }
            @Override
            public void onError(Throwable t) {

            }
            @Override
            public void onCompleted() {

            }
        };

        when(storageOperations.uploadImage(any())).thenReturn(new ChunkUploader() {
            @Override
            public void upload(ByteBuffer content) throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        });

        int ARR_SIZE = 1_000_000;

        try(FileInputStream fileInputStream = new FileInputStream(file);) {

            byte[] arr = new byte[ARR_SIZE];

            StreamObserver<ImageSend> sendStreamObserver = noBlockingStub.publishImage(identifierStreamObserver);

            while (fileInputStream.read(arr) != -1) {

                sendStreamObserver
                        .onNext(ImageSend.newBuilder().setName(file.getName()).setChunkData(ByteString.copyFrom(arr)).build());

            }

            sendStreamObserver.onCompleted();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void retrieveCharacteristicsOfNonExistentImage() throws ExecutionException, InterruptedException {

        String id = UUID.randomUUID().toString();

        when(firestoreOperations.search(id)).thenReturn(null);

       StatusRuntimeException exception =
               assertThrows(StatusRuntimeException.class, () -> blockingStub.retrieveImageCharacteristics(ImageIdentifier.newBuilder().setId(id).build()));

       assertEquals(Status.Code.NOT_FOUND, exception.getStatus().getCode());

    }

    @Test
    public void setBlockingStub() throws ExecutionException, InterruptedException {

        List<LabelInfo> dogLabel = Arrays.stream(new LabelInfo[]{
                new LabelInfo("cao", 0.99F, 0.96F),
                new LabelInfo("canidae", 0.97F, 0.96F),
        }).toList();

        String id = UUID.randomUUID().toString();

        List<ImageInfo> returnedInfo = Arrays.stream(
                new ImageInfo[]{new ImageInfo(id, new Date(), dogLabel)}
        ).toList();

        String label = "cao";

        when(firestoreOperations.search(any(), any(), eq(label))).thenReturn(returnedInfo);

        SearchByDateIntervalAndLabel search =
                SearchByDateIntervalAndLabel.newBuilder()
                        .setLabel(label)
                        .setStartDate(Timestamp.of(new Date()).toProto())
                        .setEndDate(Timestamp.of(new Date()).toProto())
                        .build();

        ImageNames result = blockingStub.imagesSearch(search);

        result.getNameCount();
        assertEquals(returnedInfo.size(), result.getNameCount());

    }
}
