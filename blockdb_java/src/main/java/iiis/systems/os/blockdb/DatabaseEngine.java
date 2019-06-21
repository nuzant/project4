package iiis.systems.os.blockdb;

//aaaaa
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
    	private String last_block;
        
    	public branch(int _length, String _last_block) {
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
    private HashMap<Integer, String> blocks = new HashMap<>();
    private PriorityQueue<branch> BlockChain = new PriorityQueue<branch>();
    private int minerId;
    private int logLength = 0;
    private String dataDir;
    private int blockId = 1;
    private JsonArray TxPool = new JsonArray();
    private File logFile = new File(dataDir + "log.json");
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
    
    public void AddTx(String fromId, String toId, int value, int miningFee, String UUID) {
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
    	block.addProperty("PrevHash", Hash.getHashString(getLongestBranch().last_block));
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
        //output_log(4, fromId, toId, value);
        //check_output();
        return true;
    }

    public responseContainer verify(){
        return new responseContainer();
    }

    public responseContainer getHeight(){
        return new responseContainer();
    }

    public String getBlock(){
        return "";
    }

    public void pushBlock(){
        return;
    }

    public void pushTransaction(){
        return;
    }

    public class responseContainer{
        private int result = 0;
        String hash = "";
        responseContainer(){
        }
        
        responseContainer(int result, String hash){
            this.result = result;
            this.hash = hash;
        }

        public int getResult(){
            return this.result;
        }

        public String getHash(){
            return this.hash;
        }
    }

}
