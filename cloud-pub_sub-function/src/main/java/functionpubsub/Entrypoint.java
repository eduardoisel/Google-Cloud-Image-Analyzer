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
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entrypoint implements BackgroundFunction<PSMessage> {

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
    public void accept(PSMessage message, Context context) throws Exception {
        if (db == null) {
            logger.info("Error connecting to Firestore. Exiting function.");
            throw new RuntimeException("Error connecting to Firestore");
        }

        String dataAsString = new String(Base64.getDecoder().decode(message.data));
        logger.log(Level.WARNING, dataAsString);
        CollectionReference colRef = db.collection("CFPubSubMessages");

        logger.info("Using dataAsString");

        Gson gson = new Gson();

         ImageLocation imageLocation = gson.fromJson(dataAsString, ImageLocation.class);

        // O message ID vem no eventID
        DocumentReference docRef = colRef.document(context.eventId());
        HashMap<String, Object> map = new HashMap<>();

        map.put("ctx-messageId", context.eventId());
        map.put("ctx-pubTime", context.timestamp());

        map.put("id", imageLocation.id());
        map.put("blobName", imageLocation.blobName());
        map.put("bucketName", imageLocation.bucketName());

        ApiFuture<WriteResult> result = docRef.set(map);
        result.get();
        logger.info("Event was written to Firestore.");

    }


}
