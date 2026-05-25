package isel.image.analyzer.server;

import com.google.api.core.ApiFuture;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.longrunning.OperationSnapshot;
import com.google.api.gax.retrying.RetryingFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;
import image.analyzer.ElasticityGrpc;
import image.analyzer.VmQuantities;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ElasticServiceTest {

    private ElasticityGrpc.ElasticityBlockingStub blockingStub;
    private Server inProcessServer;
    private ManagedChannel managedChannel;

    @Mock
    private InstanceGroupManagersClient groupManagersClient;

    @BeforeEach
    void setup() throws IOException {
        String serviceName = InProcessServerBuilder.generateName();

        inProcessServer = InProcessServerBuilder.forName(serviceName)
                .directExecutor()
                .addService(new ElasticityService("", groupManagersClient))
                .build()
                .start();

        managedChannel = InProcessChannelBuilder.forName(serviceName)
                .directExecutor()
                .usePlaintext()
                .build();

        blockingStub = ElasticityGrpc.newBlockingStub(managedChannel);
    }

    @Test
    void onErrorHappensOnBigVmInstances() {

        VmQuantities vmQuantities = VmQuantities.newBuilder()
                .setQuantity(120)
                .build();

        assertThrows(StatusRuntimeException.class, () -> blockingStub.setServerAmount(vmQuantities));

    }

    @Test
    void onErrorHappensVmInstancesAreNegative() {


        VmQuantities vmQuantities = VmQuantities.newBuilder()
                .setQuantity(-1)
                .build();

        assertThrows(StatusRuntimeException.class, () -> blockingStub.setServerAmount(vmQuantities));

    }

    @Test
    void successWithSingleDigitQuantity() {


        int quantity = 3;

        VmQuantities vmQuantities = VmQuantities.newBuilder()
                .setQuantity(quantity)
                .build();

        OperationFuture<Operation, Operation> mockReturn = new OperationFuture<Operation, Operation>() {


            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Operation get() throws InterruptedException, ExecutionException {
                return  Operation.newBuilder().setStatus(Operation.Status.DONE).build();
            }

            @Override
            public Operation get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }

            @Override
            public void addListener(Runnable listener, Executor executor) {

            }

            @Override
            public String getName() throws InterruptedException, ExecutionException {
                return "";
            }

            @Override
            public ApiFuture<OperationSnapshot> getInitialFuture() {
                return null;
            }

            @Override
            public RetryingFuture<OperationSnapshot> getPollingFuture() {
                return null;
            }

            @Override
            public ApiFuture<Operation> peekMetadata() {
                return null;
            }

            @Override
            public ApiFuture<Operation> getMetadata() {
                return null;
            }
        };

        when(groupManagersClient.resizeAsync(anyString(), anyString(), anyString(), eq(quantity)))
                .thenReturn(mockReturn);

        blockingStub.setServerAmount(vmQuantities);

    }

}
