package isel.image.analyzer.server.firestore;

import java.util.Date;
import java.util.List;

public record ImageInfo(String id, Date processingDate, List<LabelInfo> labels, List<String> labelNames) {

    public ImageInfo(String id, Date processingDate, List<LabelInfo> labels) {
        this(id, processingDate, labels, labels.stream().map(LabelInfo::name).toList());
    }

}