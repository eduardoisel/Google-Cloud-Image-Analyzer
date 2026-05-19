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
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;


// gcloud functions deploy funcPubSub --project=cn2526-t4-g08 --region=europe-west1 --entry-point=functionpubsub.Entrypoint --allow-unauthenticated --gen2 --runtime=java25 --trigger-topic cf-topic-pubsub-base --source=target/deployment --service-account=firestore@cn2526-geral.iam.gserviceaccount.com
public class Entrypoint implements BackgroundFunction<PSMessage> {

    static String projectID = "cn2526-t4-g08";

    static String managedInstanceGroupName = "image-label-Servers";

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
        logger.info("original message " + message.data);
        String dataAsString = new String(Base64.getDecoder().decode(message.data));
        logger.info(dataAsString);
        CollectionReference colRef = db.collection("CFPubSubMessages");
        // O message ID vem no eventID
        DocumentReference docRef = colRef.document(context.eventId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg-data", message.data);
        map.put("data", dataAsString);
        if (dataAsString.compareTo("error") == 0)
            throw new Exception("error forced from data");
        map.put("ctx-messageId", context.eventId());
        map.put("ctx-pubTime", context.timestamp());
        ApiFuture<WriteResult> result = docRef.set(map);
        result.get();
        logger.info("Event was written to Firestore.");

    }


}
