package isel.image.analyzer.labelsApp.firestore;

import com.google.cloud.vision.v1.EntityAnnotation;

public record LabelInfo(String name, float score, float topicality) {

    public LabelInfo(EntityAnnotation annotation) {
        this(annotation.getDescription(), annotation.getScore(), annotation.getTopicality());
    }


}
