package isel.image.analyzer.server;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.grpc.ServerBuilder;

import isel.image.analyzer.server.firestore.FirestoreOperations;
import java.io.IOException;

public class App {

    private static int svcPort = 8000;

    static void main(String[] args) throws InterruptedException, IOException {

        //does not check if it has right permissions
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
            System.exit(-1);
        }


        StorageOptions storageOptions = StorageOptions.getDefaultInstance();
        Storage storage = storageOptions.getService(); //actually used for  bucket operations, see lab3

        //extra check of GOOGLE_APPLICATION_CREDENTIALS keys, left to ease debugging as (i think) it will be needed to run on cloud virtual machine
        String projID = storageOptions.getProjectId();
        if (projID != null) System.out.println("Current Project ID:" + projID);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
            System.exit(-1);
        }

        if (args.length > 0) svcPort = Integer.parseInt(args[0]);

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("public-spaces-standard").setCredentials(credentials)
                .build();

        Firestore db = options.getService();

        io.grpc.Server svc = ServerBuilder.forPort(svcPort)
                // Add one or more services.
                // The Server can host many services in same TCP/IP port
                .addService(new ServiceImpl(new StorageOperations(storage), new PubSub(projID), new FirestoreOperations(db)))
                .build();

        svc.start();
        System.out.println("Server started on port " + svcPort);
        // Java virtual machine shutdown hook
        // to capture normal or abnormal exits
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(svc));
        // Waits for the server to become terminated
        svc.awaitTermination();
    }
}