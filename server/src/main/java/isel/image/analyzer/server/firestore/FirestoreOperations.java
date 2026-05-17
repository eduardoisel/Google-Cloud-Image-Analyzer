package isel.image.analyzer.server.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

public class FirestoreOperations {

    static String collectionName = "trab"; //todo decide

    CollectionReference collectionReference;

    public FirestoreOperations(@Nonnull Firestore database) {
        this.collectionReference = database.collection(collectionName);
    }

    public List<ImageInfo> search(@Nonnull Timestamp startDate, @Nonnull Timestamp endDate, @Nonnull String characteristic)
            throws ExecutionException, InterruptedException {

        Query query = collectionReference
                .whereArrayContains("labelNames",  characteristic)
                .whereGreaterThanOrEqualTo("processingDate", startDate)
                .whereLessThanOrEqualTo("processingDate", endDate);

        QuerySnapshot querySnapshot = query.get().get();

        return querySnapshot.getDocuments().stream().map(doc -> doc.toObject(ImageInfo.class)).toList();

    }

    public ImageInfo search(String id) throws ExecutionException, InterruptedException {
        return collectionReference.document(id).get().get().toObject(ImageInfo.class);
    }
}
