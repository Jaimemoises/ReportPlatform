package report.server;

import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.report.Report;
import com.proto.report.ReportId;
import com.proto.report.ReportServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class ReportServiceImpl extends ReportServiceGrpc.ReportServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    ReportServiceImpl(MongoClient client){
        MongoDatabase db = client.getDatabase("reportdb");
        mongoCollection = db.getCollection("report");
    }

    @Override
    public void createReport(Report request, StreamObserver<ReportId> responseObserver) {
        Document doc = new Document("employee", request.getEmployee())
                .append("title", request.getTitle())
                .append("content", request.getContent());

        InsertOneResult result;

        try{
            result = mongoCollection.insertOne(doc);
        }catch (MongoException e){
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }

        if (!result.wasAcknowledged() || result.getInsertedId() == null){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Report could not be created")
                    .asRuntimeException());
            return;
        }

        String id = result.getInsertedId().asObjectId().getValue().toString();

        responseObserver.onNext(ReportId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void readReport(ReportId request, StreamObserver<Report> responseObserver) {
        if(request.getId().isEmpty()){
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The report ID cannot be empty")
                    .asRuntimeException());
            return;
        }

        String id = request.getId();
        Document result = mongoCollection.find(eq("_id", new ObjectId(id))).first();

        if(result == null){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Report was not found")
                    .augmentDescription("ReportId: " + id)
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Report.newBuilder()
                .setEmployee(result.getString("employee"))
                .setTitle(result.getString("title"))
                .setContent(result.getString("content"))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateReport(Report request, StreamObserver<Empty> responseObserver) {
        if (request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The report ID cannot be empty")
                    .asRuntimeException());
            return;
        }

        String id = request.getId();
        Document result = mongoCollection.findOneAndUpdate(
                eq("_id", new ObjectId(id)),
                combine (
                        set("employee", request.getEmployee()),
                        set("title", request.getTitle()),
                        set("content", request.getContent())
                )
        );

        if(result == null){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The report was not found")
                    .augmentDescription("ReportId: " + id)
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listReports(Empty request, StreamObserver<Report> responseObserver) {
        for (Document document : mongoCollection.find()){
            responseObserver.onNext(Report.newBuilder()
                    .setId(document.getObjectId("_id").toString())
                    .setEmployee(document.getString("employee"))
                    .setTitle(document.getString("title"))
                    .setContent(document.getString("content"))
                    .build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void deleteReport(ReportId request, StreamObserver<Empty> responseObserver) {
        if (request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The report ID cannot be empty")
                    .asRuntimeException());
            return;
        }

        String id = request.getId();
        DeleteResult result;

        try{
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        }catch (MongoException e){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The report could not be deleted")
                    .asRuntimeException());
            return;
        }

        if(!result.wasAcknowledged()){
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The report could not be deleted")
                    .asRuntimeException());
            return;
        }

        if(result.getDeletedCount() == 0){
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("The report was not fould")
                    .augmentDescription("ReportId: " + id)
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
