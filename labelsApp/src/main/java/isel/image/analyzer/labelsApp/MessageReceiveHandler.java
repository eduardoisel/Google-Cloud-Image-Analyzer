package isel.image.analyzer.labelsApp;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.gson.Gson;
import com.google.pubsub.v1.PubsubMessage;
import isel.image.analyzer.labelsApp.firestore.FirestoreOperations;
import isel.image.analyzer.labelsApp.firestore.ImageInfo;
import java.util.Map;

public class MessageReceiveHandler implements MessageReceiver {

    private FirestoreOperations firestoreOperations;

    public MessageReceiveHandler(FirestoreOperations firestoreOperations) {
        this.firestoreOperations = firestoreOperations;
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {

        System.out.println("Message (Id:" + message.getMessageId() +
                " Data:" + message.getData().toStringUtf8() + ")");

        Gson gsonMsg = new Gson();

        String json = message.getData().toStringUtf8();

        ImageInfo imageInfo = gsonMsg.fromJson(json, ImageInfo.class);

        try {

            firestoreOperations.save(imageInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        consumer.ack();
    }

}

