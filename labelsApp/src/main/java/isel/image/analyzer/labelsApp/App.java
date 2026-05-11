package isel.image.analyzer.labelsApp;

import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.pubsub.v1.ProjectSubscriptionName;
import isel.image.analyzer.labelsApp.firestore.FirestoreOperations;
import java.io.IOException;

public class App {

//    static void main() throws IOException {
//
//        DetectLabels.detectLabels();
//    }

//    public static void main(String... args) {
//        // Create a service object
//        //
//        // If no explicit credentials or API key are set, requests are authenticated using Application
//        // Default Credentials if available; otherwise, using an API key from the GOOGLE_API_KEY
//        // environment variable
//        Translate translate = TranslateOptions.getDefaultInstance().getService();
//
//        // Text of an "unknown" language to detect and then translate into English
//        final String mysteriousText = "Hola Mundo";
//
//        // Detect the language of the mysterious text
//        Detection detection = translate.detect(mysteriousText);
//        String detectedLanguage = detection.getLanguage();
//
//        // Translate the mysterious text to English
//        Translation translation =
//                translate.translate(
//                        mysteriousText,
//                        Translate.TranslateOption.sourceLanguage(detectedLanguage),
//                        Translate.TranslateOption.targetLanguage("en"));
//
//        System.out.println(translation.getTranslatedText());
//    }

    static String subscriptionId; //todo decide
    static String projectId = "cn2526-t4-g08";

    static FirestoreOperations firestoreOperations;

    static void main(String[] args) throws IOException {


        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

        FirestoreOptions options = FirestoreOptions
                .newBuilder().setDatabaseId("public-spaces-standard").setCredentials(credentials)
                .build();

        firestoreOperations = new FirestoreOperations(options.getService());





    }


    public static void subscribe(String subscriptionID) throws IOException {

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("id", subscriptionID);

        ExecutorProvider executorProvider = InstantiatingExecutorProvider
                .newBuilder()
                .setExecutorThreadCount(1) // ensures only 1 message is processed
                .build();
        Subscriber subscriber =
                Subscriber.newBuilder(subscriptionName, new MessageReceiveHandler(firestoreOperations))
                        .setExecutorProvider(executorProvider)
                        .build();
        subscriber.startAsync().awaitRunning();

    }
}
