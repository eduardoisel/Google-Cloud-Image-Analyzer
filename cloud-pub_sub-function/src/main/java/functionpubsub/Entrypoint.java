package functionpubsub;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class Entrypoint implements BackgroundFunction<ImageLocation> {

    Logger logger = Logger.getLogger(Entrypoint.class.getName());


    private static final Firestore db = initFirestore();

    private static Firestore initFirestore() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            FirestoreOptions options = FirestoreOptions.newBuilder()
                    .setDatabaseId("public-spaces-standard").setCredentials(credentials).build();
            Firestore db = options.getService();
            return db;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void accept(ImageLocation message, Context context) throws Exception {
        if (db == null) {
            logger.info("Error connecting to Firestore. Exiting function.");
            throw new RuntimeException("Error connecting to Firestore");
        }

        logger.info("original message " + message.toString());
        CollectionReference colRef = db.collection("CFPubSubMessages");

        // O message ID vem no eventID
        DocumentReference docRef = colRef.document(context.eventId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("image-location", message.bucketName());
        map.put("image-name", message.blobName());
        map.put("id", message.id());
        map.put("ctx-messageId", context.eventId());
        map.put("ctx-pubTime", context.timestamp());
        ApiFuture<WriteResult> result = docRef.set(map);
        result.get();
        logger.info("Event was written to Firestore.");

    }


}
