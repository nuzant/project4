package iiis.systems.os.blockdb;

import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import iiis.systems.os.blockdb.hash.Hash;

public class DatabaseEngine {
    private static DatabaseEngine instance = null;

    public static DatabaseEngine getInstance() {
        return instance;
    }

    public static void setup(String dataDir) {
        instance = new DatabaseEngine(dataDir);
        //instance.recover();
        //instance.print_balance();
    }
    
    protected class branch implements Comparable<branch> {
    	private int length;
    	private JsonObject last_block;
        
    	public branch(int _length, JsonObject _last_block) {
    		this.length = _length;
    		this.last_block = _last_block;
    	}
    	
    	@Override
    	public int compareTo(branch another) {
    		return another.length - this.length;
    	}
    }

    private HashMap<String, Integer> balances = new HashMap<>();
    private HashMap<String, Lock> locks = new HashMap<>();
    private HashMap<String, JsonObject> blocks = new HashMap<>();
    private PriorityQueue<branch> BlockChain = new PriorityQueue<branch>();
    private int minerId;
    private int logLength = 0;
    private String dataDir;
    private int blockId = 1;
    private JsonArray TxPool = new JsonArray();
    private File logFile = new File(dataDir + "log.json");
    public boolean stopComputing = true;
    //private FileWriter logWriter;

    DatabaseEngine(String dataDir) {
        this.dataDir = dataDir;
    }

    private int getOrInit(String userId) {
        if (balances.containsKey(userId)) {
            return balances.get(userId);
        } else {
            return 1000;
        }
    }

    private void createLock(String userId){
        if(!locks.containsKey(userId)){
            Lock lock = new ReentrantLock();
            locks.put(userId, lock);
        }
    }
    
    public branch getLongestBranch() {
    	return BlockChain.peek();
    }
    
    public void addTx(String fromId, String toId, int value, int miningFee, String UUID) {
    	JsonObject Tx = new JsonObject();
    	
    	Tx.addProperty("Type", "TRANSFER");
    	Tx.addProperty("FromID", fromId);
    	Tx.addProperty("ToID", toId);
    	Tx.addProperty("Value", value);
    	Tx.addProperty("MiningFee", miningFee);
    	Tx.addProperty("UUID", UUID);
    	
    	TxPool.add(Tx);
    }
    
    public void output_block() {
    	JsonObject block = new JsonObject();
    	block.addProperty("BlockID", blockId);
    	block.addProperty("PrevHash", Hash.getHashString(getLongestBranch().last_block.toString()));
    	block.addProperty("Nonce", "00000000");
    	block.addProperty("MinerID", "Server"+String.format("%02d", minerId));
    	
    	JsonArray newTxPool = new JsonArray();
    	JsonArray transactions = new JsonArray();
    	int N = TxPool.size();
    	int i;
    	for (i=0; i<N && i<50; i++) 
    		transactions.add(TxPool.get(i));
    	for (; i<N; i++)
    		newTxPool.add(TxPool.get(i));
    	block.add("Transactions", transactions);
    	
    	//create blockfile 
        File createBlockFile = new File(dataDir + Integer.toString(blockId) + ".json");
        createBlockFile.delete();
        if(!createBlockFile.exists()){
            try{
                if(createBlockFile.createNewFile()){
                    System.out.println("New block created, blockid:" + Integer.toString(blockId));
                }
            } catch (IOException e){
                e.printStackTrace();
                System.out.println("Creating block failed, blockid:" + Integer.toString(blockId));
            }
        }
        // write new block
        try(FileWriter file = new FileWriter(dataDir + Integer.toString(blockId) + ".json")){
            file.write(block.toString());
            file.flush();

            System.out.println("Writing information to block: " + dataDir + Integer.toString(blockId) + ".json");
        } catch(IOException e){
        //e.printStackTrace();
        System.out.println("Fail to write block: " + dataDir + Integer.toString(blockId) + ".json");
        }
    }

    public int get(String userId) {
        //createLock(userId);
        //logLength++;
        //check_output();
        return getOrInit(userId);
    }

    public boolean transfer(String fromId, String toId, int value, int miningFee, String UUID) {
        createLock(fromId);
        createLock(toId);
        locks.get(fromId).lock();
        locks.get(toId).lock();
        int fromBalance = getOrInit(fromId);
        int toBalance = getOrInit(toId);
        if(fromBalance - value < 0){
            locks.get(fromId).unlock();
            locks.get(toId).unlock();
            return false;
        }
        logLength++;
        balances.put(fromId, fromBalance - value);
        balances.put(toId, toBalance + value);
        locks.get(toId).unlock();
        locks.get(fromId).unlock();
        return true;
    }

    public boolean find_UUID(JsonArray transactions, String UUID) {
    	int N = transactions.size();
    	boolean found = false;
    	for(int i=0; i<N; i++) {
			JsonObject Tx = transactions.get(i).getAsJsonObject();
			String uuid = Tx.get("UUID").getAsString();
			if (uuid.contentEquals(UUID))
				found = true;
		}
        return found;
    }
    
    public responseContainer verify(String UUID){
    	JsonObject current_block = BlockChain.peek().last_block;
    	String current_hash = Hash.getHashString(current_block.toString());
    	String first_hash = "0000000000000000000000000000000000000000000000000000000000000000";
    	int depth = 0;
    	boolean found_in_BlockChain = false, found_in_TxPool = false;
    	
    	//try to find the transaction on the longest chain
    	while (true) {
    		JsonArray transactions = current_block.getAsJsonArray("Transactions");
    		found_in_BlockChain = find_UUID(transactions, UUID);
    		
    		if(found_in_BlockChain)
    			break;
    		
    		String prev_hash = current_block.get("PrevHash").getAsString();
    		if(prev_hash.contentEquals(first_hash))
    			break;
    		
    		current_block = blocks.get(prev_hash);
    		current_hash = Hash.getHashString(current_block.toString());
    		depth++;
    	}
    	
    	//try to find the transaction in the TxPool
		found_in_TxPool = find_UUID(TxPool, UUID);
    	
    	responseContainer response = new responseContainer();
    	if (found_in_BlockChain && depth >= 6) {
    		response.verify_result = 0;
    		response.block = current_block.toString();
    	}
    	else if (found_in_BlockChain || found_in_TxPool) {
    		response.verify_result = 1;
    		if (found_in_BlockChain)
    			response.block = current_block.toString();
    	}
    	else
    		response.verify_result = 2;
    	
        return response;
    }

    public responseContainer getHeight(){
        branch leaf = BlockChain.peek();
    	responseContainer response = new responseContainer();
    	response.result = leaf.length;
    	response.block = leaf.last_block.toString();
        return response;
    }

    public String getBlock(String hash){
    	String response = null;
    	response = blocks.get(hash).toString();
    	return response;
    }

    public void pushBlock(){
        return;
    }

    public void pushTransaction(){
        return;
    }

    public class responseContainer{
        private int result = 0;
        private String block = "";
        private int verify_result = 0;
        responseContainer(){
        }
        
        responseContainer(int result, String block, int verify_result){
            this.result = result;
            this.block = block;
            this.verify_result = verify_result;
        }

        public int getResult(){
            return this.result;
        }

        public String getBlock(){
            return this.block;
        }
        
        public int getVerifyResult() {
        	return this.verify_result;
        }
    }


    /*
    This method will compute one satisfiable nonce, which is a 8-digit String
    for the block, and fill the "nonce" field with the nonce.
    Return nothing. If not a single nonce has been solved by the function,
    it will throw an exception.
    */
    public byte intToByte(int i){ // i < 128 here
		return (byte)(i & 0xff);
	}

    public String compute_nonce(JsonObject block){
        //compute
        block = (JsonObject)block.remove("nonce");
        jumpOut:
        for(int i1 = 0; i1 < 128; i1++){
            for(int i2 = 0; i2 < 128; i2++){
                for(int i3 = 0; i3 < 128; i3++){
                    for(int i4 = 0; i4 < 128; i4++){
                        for(int i5 = 0; i5 < 128; i5++){
                            for(int i6 = 0; i6 < 128; i6++){
                                for(int i7 = 0; i7 < 128; i7++){
                                    for(int i8 = 0; i8 < 128; i8++){
                                        byte[] nonceByte = {intToByte(i1), intToByte(i2), intToByte(i3), intToByte(i4),
                                                                intToByte(i5), intToByte(i6), intToByte(i7), intToByte(i8)};
                                        block.addProperty("nonce",  new String(nonceByte));
                                        if(Hash.checkHash(Hash.getHashString(block.toString()))){
                                            System.out.println("compute_nonce(): Compute completed.");
                                            return new String(nonceByte);
                                        }
                                        if(stopComputing){
                                            break jumpOut;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("compute_nonce(): Stop computing blocks because received available block, return empty string.");
        return new String();
    }
}