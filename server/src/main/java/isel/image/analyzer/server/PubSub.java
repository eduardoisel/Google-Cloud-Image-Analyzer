package isel.image.analyzer.server;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;

public class PubSub implements AutoCloseable{

    public PubSub(String projectId) throws IOException {
        TopicName topicName = TopicName.ofProjectTopicName(projectId, TOPIC_ID);
        publisher = Publisher.newBuilder(topicName).build();
    }

    @Override
    public void close() {
        publisher.shutdown();
    }

    public record ImageLocation(String bucketName, String blobName, String id) {
    }

    static String TOPIC_ID = "Image";

    Gson gsonMsg = new Gson();

    Publisher publisher;


    public void publishMessage(String imageId) throws Exception {

        String jsonMsg = gsonMsg.toJson(new ImageLocation("cn_g08_europe", imageId, imageId));

        ByteString msgData = ByteString.copyFromUtf8(jsonMsg);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(msgData)
                .build();

        ApiFuture<String> future = publisher.publish(pubsubMessage);
        String msgID = future.get();
        System.out.println("Message Published with ID=" + msgID);
    }

}
