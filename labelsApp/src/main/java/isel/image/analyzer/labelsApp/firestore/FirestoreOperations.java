package isel.image.analyzer.labelsApp.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;

public class FirestoreOperations {

    static String collectionName = "trab"; //todo decide

    CollectionReference collectionReference;

    Firestore database;

    public FirestoreOperations(Firestore database) {
        this.database = database;
        this.collectionReference = database.collection(collectionName);
    }


    public void save(ImageInfo imageInfo) throws ExecutionException, InterruptedException {
        DocumentReference docRef = collectionReference.document(imageInfo.id());
        ApiFuture<WriteResult> resultFut = docRef.set(imageInfo);
        WriteResult result = resultFut.get();
        System.out.println("Update time : " + result.getUpdateTime());
    }

    public List<ImageInfo> search(@Nonnull Date startDate, @Nonnull Date endDate, @Nonnull String characteristic)
            throws ExecutionException, InterruptedException {

        Query query = collectionReference
                //.whereEqualTo("", startDate) //to see how
                .whereGreaterThan("processingDate", startDate)
                .whereLessThan("processingDate", endDate);

        QuerySnapshot querySnapshot = query.get().get();

        return querySnapshot.getDocuments().stream().map(doc -> doc.toObject(ImageInfo.class)).toList();

    }

}
