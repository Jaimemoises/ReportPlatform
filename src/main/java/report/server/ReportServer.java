package report.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class ReportServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 50051;

        MongoClient client = MongoClients.create("mongodb://root:root@localhost:27017/");

        Server server = ServerBuilder.forPort(port)
                .addService(new ReportServiceImpl(client))
                .addService(ProtoReflectionService.newInstance())
                .build();

        server.start();
        System.out.println("Server started");
        System.out.println("Port " + port + " is working");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown request");
            server.shutdown();
            client.close();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
