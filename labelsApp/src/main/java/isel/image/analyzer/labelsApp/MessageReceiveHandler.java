package isel.image.analyzer.labelsApp;


import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.gson.Gson;
import com.google.pubsub.v1.PubsubMessage;
import isel.image.analyzer.labelsApp.firestore.FirestoreOperations;
import isel.image.analyzer.labelsApp.firestore.ImageInfo;
import isel.image.analyzer.labelsApp.firestore.ImageLocation;
import isel.image.analyzer.labelsApp.firestore.LabelInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Receive just name/id as a way retrieve from cloud storage and analyze/translate
 */
public class MessageReceiveHandler implements MessageReceiver {

    private FirestoreOperations firestoreOperations;

    private Translate translate;

    public MessageReceiveHandler(FirestoreOperations firestoreOperations, Translate translate) {
        this.firestoreOperations = firestoreOperations;
        this.translate = translate;
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {

        System.out.println("Message (Id:" + message.getMessageId() +
                " Data:" + message.getData().toStringUtf8() + ")");

        Gson gsonMsg = new Gson();

        String json = message.getData().toStringUtf8();

        ImageLocation imageLocation = gsonMsg.fromJson(json, ImageLocation.class);

        try {

            List<EntityAnnotation> entityAnnotations =
                    DetectLabelsGcs.getEntityAnnotations(imageLocation.bucketName(), imageLocation.blobName());

            if (entityAnnotations == null) {
                firestoreOperations.save(new ImageInfo(imageLocation.id(), Timestamp.now().toDate(), Collections.emptyList()));
                return;
            }

            List<LabelInfo> labelInfo = entityAnnotations
                    .stream()
                    .map(LabelInfo::new)
                    .map(label -> {
                        Translation translation =
                                translate.translate(
                                        label.name(),
                                        Translate.TranslateOption.sourceLanguage("en"),
                                        Translate.TranslateOption.targetLanguage("pt"));

                        return new LabelInfo(translation.getTranslatedText(), label.score(), label.topicality());
                    }).toList();


            firestoreOperations.save(new ImageInfo(imageLocation.id(), Timestamp.now().toDate(), labelInfo));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        consumer.ack();
    }

}

