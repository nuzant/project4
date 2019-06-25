package iiis.systems.os.blockchaindb;

import iiis.systems.os.blockchaindb.BlockChainMinerGrpc.BlockChainMinerBlockingStub;
import iiis.systems.os.blockchaindb.BlockChainMinerGrpc.BlockChainMinerStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
//import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.HashSet;


/** 
    * class and methods that used to communicate with other servers
 */

public class BlockChainMinerClient extends Thread{
    private final ManagedChannel channel;
    private final BlockChainMinerStub asyncStub;
    //private final BlockChainMinerBlockingStub blockingStub;
    private final String host;
    private final int port;
    private String name;
    private Thread t;

    private String block;
    private volatile boolean sendBlock = false;
    private Transaction trans;
    private volatile boolean sendTrans = false;

    public Set<String> blocksReceived = new HashSet<String>();
    private String hash;
    private boolean sendGetBlock = false;

    public volatile boolean notice = false;

    /** construct channel at host, port */
    BlockChainMinerClient(String host, int port){
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true);
        System.out.println("Building channel to " + host + ":" + Integer.toString(port));
        channel = channelBuilder.build();
        //blockingStub = BlockChainMinerGrpc.newBlockingStub(channel);
        asyncStub = BlockChainMinerGrpc.newStub(channel);
        this.host = host;
        this.port = port;
        this.name = host + ":" + Integer.toString(port);
    }

    public void run(){
        //logic of client thread
        while(true){
            try{
                synchronized(this){
                    this.wait();
                }
            } catch(InterruptedException e) {
                //i dont actually know in what situation this can happen
                System.err.println("Client thread interrupted.");
                Thread.currentThread().interrupt();
            }
            if(sendBlock){
                pushBlock(block);
            }
            if(sendTrans){
                pushTransaction(trans);
            }
            if(sendGetBlock){
                getBlock(hash);
            }
        }
    }

    public void start(){
        System.out.println("Starting client thread talking to " + name);
        if(t == null){
            t = new Thread(this, name);
            t.start();
        }
    }

    public void setBlock(String block){
        this.sendBlock = true;
        this.block = block;
    }

    public void setTransaction(Transaction trans){
        this.sendTrans = true;
        this.trans = trans;
    }

    public void setGetBlock(String hash){
        this.sendGetBlock = true;
        this.hash = hash;
    }

    public void getBlock(String hash){

        GetBlockRequest request = GetBlockRequest.newBuilder().setBlockHash(hash).build();
        StreamObserver<JsonBlockString> observer = new StreamObserver<JsonBlockString>(){
            //stream observer used to receive new block from other servers
            @Override
            public void onNext(JsonBlockString json){
                if(json != null){
                    String b = json.getJson();
                    blocksReceived.add(b);
                    notice = true;
                    System.out.println("Getting block from: " + host + ":" + Integer.toString(port) + ", hash = " + Hash.getHashString(b));
                } else {
                    System.out.println("Getting block from: " + host + ":" + Integer.toString(port) + ", get NOTHING!");
                }
                return;
            }

            @Override
            public void onError(Throwable t){
                return;
            }

            @Override
            public void onCompleted(){
                return;
            }
        };

        try{
            asyncStub.getBlock(request, observer);
            this.sendGetBlock = false;
        } catch (RuntimeException e){
            // fail to deliever?
            System.out.println("Failed sending getblock request to host: " + host + ", port:" + Integer.toString(port));
            return;
        }
    }

    public void pushBlock(String json){
        System.out.println("Sending block to host: " + host + ", port:" + Integer.toString(port));

        JsonBlockString request = JsonBlockString.newBuilder().setJson(json).build();
        StreamObserver<Null> nothing = new StreamObserver<Null>(){
            // this is an empty StreamObserver, Only used for compatibility of function pushBlock()
            // never used
            @Override
            public void onNext(Null n){
                return;
            }

            @Override
            public void onError(Throwable t){
                return;
            }

            @Override
            public void onCompleted(){
                return;
            }
        };

        try{
            asyncStub.pushBlock(request, nothing);
            this.sendBlock = false;
        } catch (RuntimeException e){
            // fail to deliever?
            System.out.println("Failed sending block to host: " + host + ", port:" + Integer.toString(port));
            return;
        }

        nothing.onCompleted();
    }

    public void pushTransaction(Transaction trans){
        System.out.println("Sending transaction to host: " + host + ", port:" + Integer.toString(port));

        Transaction request = trans;
        StreamObserver<Null> nothing = new StreamObserver<Null>(){
            // this is an empty StreamObserver, Only used for compatibility of function pushBlock()
            // never used
            @Override
            public void onNext(Null n){
                return;
            }

            @Override
            public void onError(Throwable t){
                return;
            }

            @Override
            public void onCompleted(){
                return;
            }
        };

        try{
            asyncStub.pushTransaction(request, nothing);
            this.sendTrans = false;
        } catch (RuntimeException e){
            // fail to deliever?
            System.out.println("Failed sending transaction to host: " + host + ", port:" + Integer.toString(port));
            return;
        }

        nothing.onCompleted();
    }
}