package report.client;

import com.google.protobuf.Empty;
import com.mongodb.internal.VisibleForTesting;
import com.proto.report.Report;
import com.proto.report.ReportId;
import com.proto.report.ReportServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;




public class ReportClient {

    public static ReportId createReport(ReportServiceGrpc.ReportServiceBlockingStub stub){
        try{
            ReportId createResponse = stub.createReport(
                    Report.newBuilder()
                            .setEmployee("Jaime")
                            .setTitle("Emergency number 0001")
                            .setContent("Some content here")
                            .build()
            );

            System.out.println("Report created" + createResponse.getId());
            return createResponse;
        }catch (StatusRuntimeException e){
            System.out.println("Could not create the report");
            e.printStackTrace();
            return null;
        }
    }

    public static void readReport(ReportServiceGrpc.ReportServiceBlockingStub stub, ReportId reportId){
        try{
            Report readResponse = stub.readReport(reportId);

            System.out.println("Report read: " + readResponse);
        }catch (StatusRuntimeException e){
            System.out.println("Could not read the report");
            e.printStackTrace();
        }
    }

    public static void updateReport(ReportServiceGrpc.ReportServiceBlockingStub stub, ReportId reportId){
        try{
            Report newReport = Report.newBuilder()
                    .setId(reportId.getId())
                    .setEmployee("Jaime")
                    .setTitle("Report 0001 (Changed)")
                    .setContent("This is the report 0001 altered")
                    .build();

            stub.updateReport(newReport);
            System.out.println("Report Updated: " + newReport);
        }catch (StatusRuntimeException e){
            System.out.println("Could not update the report");
            e.printStackTrace();
        }

    }


    public static void listReports(ReportServiceGrpc.ReportServiceBlockingStub stub){
        stub.listReports(Empty.getDefaultInstance()).forEachRemaining(System.out::print);
    }

    public static void deleteBlog(ReportServiceGrpc.ReportServiceBlockingStub stub, ReportId reportId){
        try{
            stub.deleteReport(reportId);
            System.out.println("Report deleted: " + reportId.getId());
        }catch (StatusRuntimeException e){
            System.out.println("The report could not be deleted");
            e.printStackTrace();
        }
    }


    public static void run (ManagedChannel channel) {
        ReportServiceGrpc.ReportServiceBlockingStub stub = ReportServiceGrpc.newBlockingStub(channel);

        ReportId reportId = createReport(stub);

        if (reportId == null)
            return;

        readReport(stub, reportId);
        updateReport(stub, reportId);
        listReports(stub);
        deleteBlog(stub, reportId);
    }


    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        run(channel);

        System.out.println("Shutting down");
        channel.shutdown();
    }
}