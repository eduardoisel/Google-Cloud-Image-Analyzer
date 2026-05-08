package isel.forum.client;


import image.analyzer.ImageIdentifier;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.locks.Lock;


public class ImageIdentifierStream implements StreamObserver<ImageIdentifier> {


    private final Lock lock = App.lock;

    @Override
    public void onNext(ImageIdentifier value) {
        lock.lock();
        System.out.printf("Received image id %s\n", value.getId());
        lock.unlock();
    }

    @Override
    public void onError(Throwable throwable) {
        lock.lock();
        System.out.printf("Received error with message %s\n", throwable.getMessage());
        lock.unlock();
    }

    /*
    Should complete right after sending id once
     */
    @Override
    public void onCompleted() {
    }


}
