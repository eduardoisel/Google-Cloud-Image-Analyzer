package isel.forum.client;

import com.google.cloud.Timestamp;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import image.analyzer.SearchByDateIntervalAndLabel;
import image.analyzer.ImageAnalyserGrpc;
import image.analyzer.ImageCharacteristics;
import image.analyzer.ImageIdentifier;
import image.analyzer.ImageNames;
import image.analyzer.ImageSend;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.jspecify.annotations.NonNull;

public class App {


    private static String svcIP;   // "localhost"
    private static int svcPort = 8000;
    private static ImageAnalyserGrpc.ImageAnalyserBlockingStub blockingStub;
    private static ImageAnalyserGrpc.ImageAnalyserStub noBlockStub;

    private static Comparator<EndpointInfo> comparator = endpointInfoComparator();


    public static VirtualMachineInstances search() throws IOException, InterruptedException {
        String cfURL = "https://europe-southwest1-cn2526-t4-g08.cloudfunctions.net/serverLookup?zone=europe-southwest1-a";
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cfURL))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) System.out.println(response.body());

        else System.out.println("Endpoint failed, status code " + response.statusCode());

        Gson gson = new Gson();

        try {
            return gson.fromJson(response.body(), VirtualMachineInstances.class);
        } catch (Exception e) {
            throw e;
        }

    }

    private static void getServer() {
        try {
            VirtualMachineInstances virtualMachineInstances = search();

            if (virtualMachineInstances == null || virtualMachineInstances.list().isEmpty()) {
                System.out.println("Virtual machine instances not found");
                System.exit(1);
            }

            EndpointInfo youngest = virtualMachineInstances.list().stream().max(comparator).get();
            svcIP = youngest.IpAddress();


        } catch (Exception e) {
            System.out.println("Unhandled exception");
            throw new RuntimeException(e);
        }
    }


    static void main() {
//            if (args.length == 2) {
//                svcIP = args[0];
//                svcPort = Integer.parseInt(args[1]);
//            }


        getServer();


        System.out.println("connect to " + svcIP + ":" + svcPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(svcIP, svcPort)
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
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode().equals(Status.Code.UNAVAILABLE)) {
                    getServer();
                } else {
                    System.out.println("Unhandled exception");
                    System.out.println(e.getStatus());
                }

            } catch (Exception ex) {
                System.out.println("Execution call Error  !");
                ex.printStackTrace();
            }
        }

    }

    // https://stackoverflow.com/questions/289311/output-rfc-3339-timestamp-in-java
    // https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html#iso8601timezone
    private static @NonNull Comparator<EndpointInfo> endpointInfoComparator() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        //ignore possible case of being equal
        Comparator<EndpointInfo> comparator = (o1, o2) -> {
            try {
                Date date = sdf.parse(o1.startTimestamp());
                Date date2 = sdf.parse(o2.startTimestamp());

                if (date.after(date2))
                    return 1;
                else
                    return -1;

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        };
        return comparator;
    }

    /**
     * todo decide max size
     */
    static int ARR_SIZE = 1_000_000;

    /**
     * Probably better to add here a check on type of file
     *
     */
    static void sendImage() {

        String fileName = read("Insert absolute path for image file", new Scanner(System.in));

        Path path = Paths.get(fileName);
        File selectedFile = path.toFile();
        System.out.println("Selected file: " + selectedFile.getAbsolutePath());

        /*
         * Warning:Since it is a stream, as is required, should probably not send all at once
         *
         * to decide a reasonable size
         */
        try (FileInputStream fileInputStream = new FileInputStream(selectedFile)) {

            //long size = selectedFile.length();

            byte[] arr = new byte[ARR_SIZE];

            ImageIdentifierStream imageIdentifierStream = new ImageIdentifierStream();

            StreamObserver<ImageSend> imageSendStreamObserver = noBlockStub.publishImage(imageIdentifierStream);

            while (fileInputStream.read(arr) != -1) {

                imageSendStreamObserver
                        .onNext(ImageSend.newBuilder().setName(selectedFile.getName()).setChunkData(ByteString.copyFrom(arr)).build());

            }

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

        System.out.print("Image characteristics: ");
        for (int i = 0; i < count; i++) {
            System.out.print(characteristics.getCharacteristic(i) + "; ");
        }
        System.out.println();

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

        System.out.print("Image names: ");
        for (int i = 0; i < count; i++) {
            System.out.print(imageNames.getName(i) + "; ");
        }
        System.out.println();

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
            System.out.println();
            System.out.println("    MENU");
            System.out.println(" 1 - Send image");
            System.out.println(" 2 - Get labels of image");
            System.out.println(" 3 - Get images by date interval and label");
            System.out.println("99 - Exit");
            System.out.println();
            System.out.println("Choose an Option?");
            op = scan.nextInt();
        } while (!((op >= 1 && op <= 3) || op == 99));
        return op;
    }

    private static String read(String msg, Scanner input) {
        System.out.println(msg);
        return input.nextLine();
    }


}
