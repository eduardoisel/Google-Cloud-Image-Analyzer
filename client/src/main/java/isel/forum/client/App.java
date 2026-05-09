package isel.forum.client;

import com.google.cloud.Timestamp;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import image.analyzer.SearchByDateIntervalAndLabel;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageCharacteristics;
import image.analyzer.ImageIdentifier;
import image.analyzer.ImageNames;
import image.analyzer.ImageSend;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.*;

/**
 * mvn exec:java -Dexec.mainClass="grpcclientapp.Client"
 * Start on command line with line above
 * <p>
 * Lock probably good to be removed, left only because if necessary adding it all in would be more tiresome
 */
public class App {

    public static Lock lock = new ReentrantLock();

    // generic ClientApp for Calling a grpc Service
    private static String svcIP = "localhost";
    private static int svcPort = 8000;
    private static ImageAnalyserGrpc.ImageAnalyserBlockingStub blockingStub;
    private static ImageAnalyserGrpc.ImageAnalyserStub noBlockStub;

    public static void main(String[] args) {


        try {
            System.out.println();
            if (args.length == 2) {
                svcIP = args[0];
                svcPort = Integer.parseInt(args[1]);
            }


            System.out.println("connect to " + svcIP + ":" + svcPort);
            // Channels are secure by default (via SSL/TLS).
            // For the example we disable TLS to avoid
            // needing certificates.
            ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
                    // Channels are secure by default (via SSL/TLS).
                    // For the example we disable TLS to avoid
                    // needing certificates.
                    .usePlaintext()
                    .build();
            blockingStub = ImageAnalyserGrpc.newBlockingStub(channel);
            noBlockStub = ImageAnalyserGrpc.newStub(channel);


            while (true) {
                try {
                    int option = Menu();
                    switch (option) {
                        case 1:
                            sendImage();
                            break;
                        case 2:
                            getLabelsOfImage();
                            break;
                        case 3:
                            getImagesByDateIntervalAndLabel();
                            break;
                        case 99:
                            System.exit(0);
                        default:
                            System.out.printf("Option %d is not available", option);
                            break;
                    }
                } catch (Exception ex) {
                    System.out.println("Execution call Error  !");
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            System.out.println("Unhandled exception");
            ex.printStackTrace();
        }
    }

    /**
     * Probably better to add here a check on type of file
     *
     */
    static void sendImage() {

        String fileName = read("Insert absolute path for image file; not used", new Scanner(System.in));


//        System.out.println("AAAAAA");
//
//        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setVisible(true);
//        int returnValue = fileChooser.showOpenDialog(null);
//        File selectedFile;
//        if (returnValue != JFileChooser.APPROVE_OPTION) {
//            return;
//        }
//
//        selectedFile = fileChooser.getSelectedFile();

        Path path = Paths.get(fileName);
        File selectedFile = path.toFile();
        System.out.println("Selected file: " + selectedFile.getAbsolutePath());

        /*
         * Warning:Since it is a stream, as is required, should probably not send all at once
         *
         * to decide a reasonable size
         */
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {

            byte[] arr = new byte[(int) selectedFile.length()];

            fileInputStream.read(arr);

            ImageIdentifierStream imageIdentifierStream = new ImageIdentifierStream();

            StreamObserver<ImageSend> imageSendStreamObserver = noBlockStub.publishImage(imageIdentifierStream);


            imageSendStreamObserver.onNext(ImageSend.newBuilder().setChunkData(ByteString.copyFrom(arr)).build());

            imageSendStreamObserver.onCompleted();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    static void getLabelsOfImage() {
        String id = read("Choose the image id", new Scanner(System.in));

        ImageIdentifier identifier = ImageIdentifier.newBuilder().setId(id).build();

        ImageCharacteristics characteristics = blockingStub
                .retrieveImageCharacteristics(identifier);

        int count = characteristics.getCharacteristicCount();

        lock.lock();
        System.out.print("Image characteristics: ");
        for (int i = 0; i < count; i++) {
            System.out.print(characteristics.getCharacteristic(i) + "; ");
        }
        System.out.println();
        lock.unlock();

    }

    /**
     * See precision of dates. Left to day specification
     */
    static void getImagesByDateIntervalAndLabel() {
        String label = read("Choose the label of images", new Scanner(System.in));


        String[] startDate = read("Choose the starting date; dd/mm/yyyy format", new Scanner(System.in)).split("/");

        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(startDate[2]), Integer.parseInt(startDate[1]) - 1, Integer.parseInt(startDate[0]), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Timestamp startTime = Timestamp.of(cal.getTime());

        String[] endDate = read("Choose the end date; dd/mm/yyyy format", new Scanner(System.in)).split("/");

        cal.set(Integer.parseInt(endDate[2]), Integer.parseInt(endDate[1]) - 1, Integer.parseInt(endDate[0]), 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Timestamp endTime = Timestamp.of(cal.getTime());

        ImageNames imageNames = blockingStub.imagesSearch(SearchByDateIntervalAndLabel.newBuilder().setStartDate(startTime.toProto()).setEndDate(endTime.toProto()).setLabel(label).build());

        int count = imageNames.getNameCount();

        lock.lock();
        System.out.print("Image names: ");
        for (int i = 0; i < count; i++) {
            System.out.print(imageNames.getName(i) + "; ");
        }
        System.out.println();
        lock.unlock();

    }

    /**
     * print options and collect chosen one
     *
     * @return the number given by user
     */
    private static int Menu() {
        int op;
        Scanner scan = new Scanner(System.in);
        do {
            lock.lock();
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Send image");
            System.out.println(" 2 - Get labels of image");
            System.out.println(" 3 - Get images by date interval and label");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            lock.unlock();
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 3) || op == 99));
        return op;
    }

    private static String read(String msg, Scanner input) {
        lock.lock();
        System.out.println(msg);
        lock.unlock();

        return input.nextLine();
    }


}
