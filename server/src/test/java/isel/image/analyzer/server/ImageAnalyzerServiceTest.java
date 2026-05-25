package isel.image.analyzer.server;

import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import image.analyzer.ElasticityGrpc;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageAnalyzer;
import image.analyzer.ImageCharacteristics;
import image.analyzer.ImageIdentifier;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import isel.image.analyzer.server.firestore.FirestoreOperations;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void saveImageTest(){


        String path = ImageAnalyzerServiceTest.class.getResource("/birdie.jpg").getPath();


        System.out.println(path);

    }

    @Test
    public void retrieveCharacteristicsOfNonExistentImage() throws ExecutionException, InterruptedException {

        String id = UUID.randomUUID().toString();


        when(firestoreOperations.search(id)).thenReturn(null);

        Assert.assertThrows(StatusRuntimeException.class, () ->blockingStub.retrieveImageCharacteristics(ImageIdentifier.newBuilder().setId(id).build()));


    }

}
