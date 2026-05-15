package isel.image.analyzer.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

public class PubSub {

    private final String projectId;

    public PubSub(String projectId) {
        this.projectId = projectId;
    }

    public record ImageLocation(String bucketName, String blobName, String id) {
    }

    static String TOPIC_ID = "Image";


    public void publishMessage(String imageId) throws Exception {

        Gson gsonMsg = new Gson();
        String jsonMsg = gsonMsg.toJson(new ImageLocation("cn_g08_europe", imageId, imageId));

        TopicName topicName = TopicName.ofProjectTopicName(projectId, TOPIC_ID);
        Publisher publisher = Publisher.newBuilder(topicName).build();
        ByteString msgData = ByteString.copyFromUtf8(jsonMsg);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(msgData)
                .build();

        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
        publisher.shutdown();
    }
}
