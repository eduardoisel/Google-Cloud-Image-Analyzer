package isel.image.analyzer.server;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.InstanceGroupManagersClient;
import com.google.cloud.compute.v1.Operation;
import com.google.protobuf.Empty;
import image.analyzer.ElasticityGrpc;
import image.analyzer.VmQuantities;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutionException;

public class ElasticityService extends ElasticityGrpc.ElasticityImplBase {

    private final String projectID;

    private final InstanceGroupManagersClient managersClient;

    public ElasticityService(String projectID, InstanceGroupManagersClient managersClient) {
        this.projectID = projectID;
        this.managersClient = managersClient;
    }

    public void setServerAmount(VmQuantities request, StreamObserver<Empty> responseObserver) {

        int amount = request.getQuantity();

        if (amount > 10) {
            responseObserver
                    .onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Too many instances. Not permitted. Maximum is 10")));
            return;
        }

        if (amount < 0) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Cannot choose a negative number of instances.")));
            return;
        }

        try {
            resizeManagedInstanceGroup("europe-southwest1-a", "server-mig", amount);
        } catch (InterruptedException | ExecutionException e) {
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
            return;
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

    }

    public void setLabelAmount(VmQuantities request, StreamObserver<Empty> responseObserver) {

        int amount = request.getQuantity();

        if (amount > 10) {
            responseObserver
                    .onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Too many instances. Not permitted. Maximum is 10")));
            return;
        }

        if (amount < 0) {
            responseObserver.onError(new StatusException(Status.INVALID_ARGUMENT.withDescription("Cannot choose a negative number of instances.")));
            return;
        }

        try {
            resizeManagedInstanceGroup("europe-southwest1-b", "label-app-mig", amount);
        } catch (InterruptedException | ExecutionException e) {
            responseObserver.onError(new StatusException(Status.INTERNAL.withDescription(e.getMessage())));
            return;
        }

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

    }


    void resizeManagedInstanceGroup(String zone, String instanceGroupName, int newSize) throws InterruptedException, ExecutionException {
        OperationFuture<Operation, Operation> result = managersClient.resizeAsync(
                projectID,
                zone,
                instanceGroupName,
                newSize
        );
        Operation oper = result.get();
        System.out.println("Resizing with status " + oper.getStatus());
    }
}
