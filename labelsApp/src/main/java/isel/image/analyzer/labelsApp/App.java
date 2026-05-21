package isel.image.analyzer.labelsApp;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.pubsub.v1.ProjectSubscriptionName;
import isel.image.analyzer.labelsApp.firestore.FirestoreOperations;
import java.io.IOException;

public class App {

    static String subscriptionId = "Image-sub";

    static String projectId;
    static Translate translate;
    static FirestoreOperations firestoreOperations;

    static void main() throws IOException {

        StorageOptions storageOptions = StorageOptions.getDefaultInstance();

        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            System.out.println("GOOGLE_APPLICATION_CREDENTIALS is not set");
            System.exit(-1);
        }

        projectId = storageOptions.getProjectId();
        if (projectId != null) System.out.println("Current Project ID:" + projectId);
        else {
            System.out.println("The environment variable GOOGLE_APPLICATION_CREDENTIALS isn't well defined!!");
            System.exit(-1);
        }


        translate = TranslateOptions.getDefaultInstance().getService();

        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("public-spaces-standard").setCredentials(credentials)
                .build();

        firestoreOperations = new FirestoreOperations(options.getService());

        subscribe();

    }


    public static void subscribe() {

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1) // ensures only 1 message is processed
                .build();
        Subscriber subscriber =
                Subscriber.newBuilder(subscriptionName, new MessageReceiveHandler(firestoreOperations, translate))
                        .setExecutorProvider(executorProvider)
                        .build();
        subscriber.startAsync().awaitRunning();

        subscriber.awaitTerminated();

    }
}
