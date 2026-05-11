package isel.image.analyzer.labelsApp.firestore;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ImageInfo {

    /**
     * Enunciado menciona identificador do pedido
     */
    public String id;

    public Date processingDate;

    /**
     * Saves in portuguese a map of characteristics as keys to its corresponding odds of being that characteristic
     *
     * Odds could probably be a float (or similar) and it should be necessary if one is to use number comparations
     */
    public Map<String, String> characteristicsToOddsMap;
}
