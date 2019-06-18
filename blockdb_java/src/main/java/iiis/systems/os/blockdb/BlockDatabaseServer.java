package iiis.systems.os.blockdb;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;

public class BlockDatabaseServer {
    private Server server;

    private void start(String address, int port) throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(address, port))
                .addService(new BlockDatabaseImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                BlockDatabaseServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, JSONException, InterruptedException {
        JSONObject config = Util.readJsonFile("config.json");
        config = (JSONObject)config.get("1");
        String address = config.getString("ip");
        int port = Integer.parseInt(config.getString("port"));
        String dataDir = config.getString("dataDir");

        DatabaseEngine.setup(dataDir);

        final BlockDatabaseServer server = new BlockDatabaseServer();
        server.start(address, port);

        System.out.println("Listening on " + address + " port " + Integer.toString(port) + ".....");

        server.blockUntilShutdown();
    }

    static class BlockDatabaseImpl extends BlockDatabaseGrpc.BlockDatabaseImplBase {
        private final DatabaseEngine dbEngine = DatabaseEngine.getInstance();

        @Override
        public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
            int value = dbEngine.get(request.getUserID());
            GetResponse response = GetResponse.newBuilder().setValue(value).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void transfer(Transaction request, StreamObserver<BooleanResponse> responseObserver) {
            boolean success = dbEngine.transfer(request.getFromID(), request.getToID(), request.getValue(), request.getMiningFee(), request.getUUID());
            BooleanResponse response = BooleanResponse.newBuilder().setSuccess(success).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void verify(Transaction request, StreamObserver<VerifyResponse> responseObserver) {
            //Results result = dbEngine.verify().getResult();
            String blockhash = dbEngine.verify().getHash();
            VerifyResponse response = VerifyResponse.newBuilder().setResult(VerifyResponse.Results.FAILED).setBlockHash(blockhash).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getHeight(Null request, StreamObserver<GetHeightResponse> responseObserver){
            int result = dbEngine.getHeight().getResult();
            String leafhash = dbEngine.getHeight().getHash();
            GetHeightResponse response = GetHeightResponse.newBuilder().setHeight(result).setLeafHash(leafhash).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getBlock(GetBlockRequest request, StreamObserver<JsonBlockString> responseObserver){
            String json = dbEngine.getBlock();
            JsonBlockString response = JsonBlockString.newBuilder().setJson(json).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushBlock(JsonBlockString request, StreamObserver<Null> responseObserver){
            dbEngine.pushBlock();
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushTransaction(Transaction request, StreamObserver<Null> responseObserver){
            dbEngine.pushTransaction();
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
