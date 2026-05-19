package functionhttp;

import com.google.cloud.compute.v1.Instance;
import com.google.cloud.compute.v1.InstancesClient;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.LinkedList;


//see path on --source. absolute path works, to relative maybe not
// gcloud functions deploy serverLookup --project=cn2526-t4-g08 --allow-unauthenticated --entry-point=functionhttp.Entrypoint --gen2 --runtime=java25 --trigger-http --region=europe-southwest1 --source=C:\Users\Edu\Desktop\CN\trab\cloud-Image-Analyzer\cloud-http-function\target\deployment
public class Entrypoint implements HttpFunction {

    static String projectID = "cn2526-t4-g08";

    static String managedInstanceGroupName = "image-label-Servers";

//    @Override
//    public void service(HttpRequest request, HttpResponse response) throws Exception {
//        BufferedWriter writer = response.getWriter();
//
//        String zone = request.getFirstQueryParameter("zone").orElse("europe-southwest1");
//        writer.write("List running Vms in zone=" + zone + "\n");
//
//        ListManagedInstancesInstanceGroupManagersRequest instanceGroup =
//                ListManagedInstancesInstanceGroupManagersRequest.newBuilder()
//                        .setInstanceGroupManager(managedInstanceGroupName)
//                        .setProject(projectID)
//                        .setReturnPartialSuccess(true)
//                        .setZone(zone)
//                        .build();
//
//        try (InstanceGroupManagersClient client = InstanceGroupManagersClient.create()) {
//            for (ManagedInstance managedInstance : client.listManagedInstances(instanceGroup).iterateAll()) {
//
//
//                if (managedInstance.getInstanceStatus().compareTo("RUNNING") == 0) {
//                    writer.write("Name: " + managedInstance.getName() + "\n");
//                    String ip = managedInstance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
//                    writer.write(" Last Start time: " + managedInstance.getLastStartTimestamp() + "\n");
//                    writer.write(" IP: " + ip + "\n");
//                }
//
//            }
//        }
//    }
//
//    static void listManagedInstanceGroupVMs(String zone, String grpName) throws IOException {
//
//        ListManagedInstancesInstanceGroupManagersRequest request =
//                ListManagedInstancesInstanceGroupManagersRequest.newBuilder()
//                        .setInstanceGroupManager(grpName)
//                        .setProject(projectID)
//                        .setReturnPartialSuccess(true)
//                        .setZone(zone)
//                        .build();
//
//        InstanceGroupManagersClient managersClient = InstanceGroupManagersClient.create();
//
//        System.out.println("Instances of instance group: " + grpName);
//        for (ManagedInstance instance :
//                managersClient.listManagedInstances(request).iterateAll()) {
//            System.out.println(instance.getInstance() + " with STATUS = " + instance.getInstanceStatus());
//        }
//    }


    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        BufferedWriter writer = response.getWriter();
        String zone = request.getFirstQueryParameter("zone").orElse("europe-southwest1-a");
        try (InstancesClient client = InstancesClient.create()) {

            Gson gson = new Gson();

            LinkedList<EndpointInfo> endpointInfo = new LinkedList<>();

            for (Instance instance : client.list(projectID, zone).iterateAll()) {
                if (instance.getStatus().compareTo("RUNNING") == 0) {
                    String ip = instance.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    String startTime = instance.getLastStartTimestamp();
                    endpointInfo.add(new EndpointInfo(ip, startTime));
                }
                writer.write(gson.toJson(endpointInfo));
            }
        } catch (Exception e) {
            writer.write(Arrays.toString(e.getStackTrace()));
        }
    }


}
