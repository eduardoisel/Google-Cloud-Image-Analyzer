package isel.image.analyzer.labelsApp.firestore;

import java.util.Date;
import java.util.List;

/**
 * @param id     Enunciado menciona identificador do pedido
 * @param labels Saves in portuguese
 */
public record ImageInfo(String id, Date processingDate, List<LabelInfo> labels) {


}
