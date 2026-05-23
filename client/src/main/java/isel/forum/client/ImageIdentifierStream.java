package isel.forum.client;


import image.analyzer.ImageIdentifier;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.locks.Lock;


public class ImageIdentifierStream implements StreamObserver<ImageIdentifier> {

    @Override
    public void onNext(ImageIdentifier value) {
        System.out.printf("Received image id %s\n", value.getId());
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.printf("Received error with message %s\n", throwable.getMessage());
    }

    /*
    Should complete right after sending id once
     */
    @Override
    public void onCompleted() {
    }


}
