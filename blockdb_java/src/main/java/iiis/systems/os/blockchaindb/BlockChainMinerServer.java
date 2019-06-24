package iiis.systems.os.blockchaindb;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.HashSet;

public class BlockChainMinerServer {
    private Server server;
    private static Set<BlockChainMinerClient> clients = new HashSet<BlockChainMinerClient>();
    private static Computer computer;
    private static DatabaseEngine dbEngine;

    private void start(String address, int port) throws IOException {
        server = NettyServerBuilder.forAddress(new InetSocketAddress(address, port))
                .addService(new BlockChainMinerImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                BlockChainMinerServer.this.stop();
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

    private static ServerInfo myInfo;
    private static Set<ServerInfo> othersInfo = new HashSet<ServerInfo>();

    static private void initialize(String[] args) throws IOException{
        int serverid = 0;

        if(args.length == 2){
            if(args[0].compareTo("--id") == 0){
                try{
                    serverid = Integer.parseInt(args[1]);
                    //return serverid;
                } catch (NumberFormatException e){
                    System.err.println("Server id should be an integer.");
                    System.exit(1);
                }
            } else {
                System.err.println("Missing argument, \"--id [server id]\" 01");
                System.exit(1);
            }
        } else {
            System.err.println("Missing argument, \"--id [server id]\" 02");
            System.exit(1);
        }

        JSONObject config = Util.readJsonFile("config.json");
        int nservers = config.getInt("nservers");

        if(nservers < serverid){
            System.err.println("Server id not availiable.");
            System.exit(1);
        }

        String serveridString = Integer.toString(serverid);
        //read my info
        JSONObject myconfig = (JSONObject)config.get(serveridString);
        myInfo = new ServerInfo(myconfig.getString("ip"), Integer.parseInt(myconfig.getString("port")), myconfig.getString("dataDir"));

        JSONObject otherconfig;
        ServerInfo otherinfo;

        for(int i = 1; i <= nservers; i++){
            if(i != serverid){
                otherconfig = (JSONObject)config.get(Integer.toString(i));
                othersInfo.add(new ServerInfo(otherconfig.getString("ip")
                                    , Integer.parseInt(otherconfig.getString("port")), otherconfig.getString("dataDir")));
            }
        }

    }

    public static void main(String[] args) throws IOException, JSONException, InterruptedException {
        //read arguments
        initialize(args);
        //start server (thread?)
        DatabaseEngine.setup(myInfo.dataDir);
        dbEngine = DatabaseEngine.getInstance();
        
        final BlockChainMinerServer server = new BlockChainMinerServer();
        server.start(myInfo.host, myInfo.port);

        System.out.println("Listening on " + myInfo.host + " port " + Integer.toString(myInfo.port) + ".....");

        //start client thread
        for(ServerInfo info: othersInfo){
            clients.add(new BlockChainMinerClient(info.host, info.port));
        }

        for(BlockChainMinerClient client: clients){
            client.start();
        }

        //start computer
        computer = new Computer(dbEngine);
        computer.start();
        int debugcount = 0;

        while(true){
            //server running logic
            //System.out.print("?");
            if(debugcount % 9999999 == 0){
                System.out.println(dbEngine.getTransSize());
            }
            if(!dbEngine.newBlock && dbEngine.getTransSize() > 0 && !dbEngine.computing){
                                                      //  && !(dbEngine.firstRun && dbEngine.getTransSize() < 50)){
                System.out.println("???");
                computer.setBlock(dbEngine.raw_block());
                synchronized(computer){
                    computer.notify();
                }
            }

            if(computer.finished){
                String newblock = computer.getBlock();
                for(BlockChainMinerClient client: clients){
                    client.setBlock(newblock);
                    synchronized(client){
                        client.notify();
                    }
                }
                computer.setFinished(false);
            }

            debugcount ++;
        }


        //server.blockUntilShutdown();
    }

    static class BlockChainMinerImpl extends BlockChainMinerGrpc.BlockChainMinerImplBase {
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
            int counter = 0;
            for(BlockChainMinerClient client:clients){
                synchronized(client){
                    client.setTransaction(request);
                    client.notify();
                }
                counter ++;
            }
            BooleanResponse response = BooleanResponse.newBuilder().setSuccess(success).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void verify(Transaction request, StreamObserver<VerifyResponse> responseObserver) {
            //Results result = dbEngine.verify().getResult();
        	DatabaseEngine.responseContainer r = dbEngine.verify(request.getUUID());
        	VerifyResponse.Results results = VerifyResponse.Results.FAILED;
        	switch(r.getVerifyResult()) {
        	    case 0: results = VerifyResponse.Results.SUCCEEDED;break;
        	    case 1: results = VerifyResponse.Results.PENDING;break;
        	    case 2: results = VerifyResponse.Results.FAILED;break;
        	}
            VerifyResponse response = VerifyResponse.newBuilder().setResult(results).setBlockHash(r.getBlock()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getHeight(Null request, StreamObserver<GetHeightResponse> responseObserver){
            int result = dbEngine.getHeight().getResult();
            DatabaseEngine.responseContainer r = dbEngine.getHeight();
            GetHeightResponse response = GetHeightResponse.newBuilder().setHeight(r.getResult()).setLeafHash(r.getBlock()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getBlock(GetBlockRequest request, StreamObserver<JsonBlockString> responseObserver){
            String json = dbEngine.getBlock(request.getBlockHash());
            JsonBlockString response = JsonBlockString.newBuilder().setJson(json).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushBlock(JsonBlockString request, StreamObserver<Null> responseObserver){
            dbEngine.pushBlock(request.getJson());
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void pushTransaction(Transaction request, StreamObserver<Null> responseObserver){
            dbEngine.pushTransaction(request.getFromID(), request.getToID(), request.getValue(), request.getMiningFee(), request.getUUID());
            Null response = Null.newBuilder().build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
