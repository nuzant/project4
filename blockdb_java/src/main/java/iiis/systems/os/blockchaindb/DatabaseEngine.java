package iiis.systems.os.blockchaindb;

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
import java.util.Stack;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

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
    private HashMap<String, Integer> balances_block = new HashMap<>();
    private HashMap<String, Lock> locks = new HashMap<>();
    private HashMap<String, JsonObject> blocks = new HashMap<>();
    private PriorityQueue<branch> BlockChain = new PriorityQueue<branch>();
    private int minerId;
    private int logLength = 0;
    private String dataDir;
    private int blockId = 1;
    private volatile JsonArray TxPool = new JsonArray();
    private File logFile = new File(dataDir + "log.json");
    public boolean newBlock = false;
    public boolean computing = false;
    public boolean firstRun = true;

    DatabaseEngine(String dataDir) {
        this.dataDir = dataDir;
    }

    //rpc calls 

    public int get(String userId) {
        return getOrInit(userId, balances);
    }

    public boolean transfer(String fromId, String toId, int value, int miningFee, String UUID) {
    	boolean flag = transfer_balance(fromId, toId, value, miningFee, UUID, balances);
    	if (flag) {
    		AddTx(fromId, toId, value, miningFee, UUID);
    	}
    	return flag;
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

    public void pushBlock(String block){
    	JsonParser parser = new JsonParser();
    	JsonObject Block = (JsonObject) parser.parse(block);
    	JsonObject block_chosen = CheckBlock(Block);
    	if (block_chosen != null) {
    		PutBlock(Block, block_chosen);
    		Update_by_block(balances_block, Block);
        }
        newBlock = true;
        //addValue(Block.get("MinerID").getAsString(), Block.get("MiningFee").getAsInt());

        return;
    }

    public void pushTransaction(String fromId, String toId, int value, int miningFee, String UUID){
    	int fromBalance = getOrInit(fromId, balances);
    	int toBalance = getOrInit(toId, balances);
    	if (value > miningFee && fromBalance > value) {
    		AddTx(fromId, toId, value, miningFee, UUID);
    		balances.put(fromId, fromBalance - value);
            balances.put(toId, toBalance + value);
    	}
        return;
    }

    //rpc calls end
    //util functions

    public int getTransSize(){
        return TxPool.size();    
    }

    private int getOrInit(String userId, HashMap<String, Integer> balance) {
        if (balance.containsKey(userId)) {
            return balance.get(userId);
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

    public void addValue(String id, int value){
        int oldvalue = getOrInit(id, balances);
        int oldvalue_block = getOrInit(id, balances_block);
        balances.put(id, oldvalue + value);
        balances_block.put(id, oldvalue_block + value);
    }
    
    //starts a new round of computing by creating a raw block
    public JsonObject raw_block() {
    	JsonObject block = new JsonObject();
    	block.addProperty("BlockID", blockId);
    	if(!BlockChain.isEmpty())
    	    block.addProperty("PrevHash", Hash.getHashString(getLongestBranch().last_block.toString()));
    	else {
    		block.addProperty("PrevHash", "0000000000000000000000000000000000000000000000000000000000000000");
    	}
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
        
        TxPool = newTxPool;
    	//String nonce = compute_nonce(block);
    	//block.addProperty("Nonce", nonce);
    	return block;
    }
    
    public void output_block(JsonObject block) {
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

    public boolean transfer_balance(String fromId, String toId, int value, int miningFee, String UUID, HashMap<String, Integer> balance) {
        int fromBalance = getOrInit(fromId, balance);
        int toBalance = getOrInit(toId, balance);
        if(fromBalance - value < 0){
            locks.get(fromId).unlock();
            locks.get(toId).unlock();
            return false;
        }
        
        balance.put(fromId, fromBalance - value);
        balance.put(toId, toBalance + value);

        //output_log(4, fromId, toId, value);
        //check_output();
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
    
    public HashMap<String, Integer> compute_balance(JsonObject block) {
    	HashMap<String, Integer> balance = new HashMap<>();
    	Stack<JsonObject> s = new Stack<JsonObject>();
    	String initHash = "0000000000000000000000000000000000000000000000000000000000000000";
    	String prevHash = block.get("PrevHash").getAsString();
    	s.push(block);
    	
    	while (prevHash.equals(initHash)) {
    		JsonObject now = blocks.get(prevHash);
    		s.push(now);
    		prevHash = now.get("PrevHash").getAsString();
    	}
    	
    	while(s.empty()) {
    		JsonObject now = s.pop();
    		Update_by_block(balance, now);
    	}
    	
    	return balance;
    }
    
    public boolean Update_by_block(HashMap<String, Integer> balance, JsonObject block) {
    	JsonArray transactions = block.get("Transactions").getAsJsonArray();
		int N = transactions.size();
		
		for(int i=0;i<N;i++) {
			JsonObject Tx = transactions.get(i).getAsJsonObject();
			String fromId = Tx.get("FromID").getAsString();
			String toId = Tx.get("ToID").getAsString();
			int value = Tx.get("Value").getAsInt();
			int miningFee = Tx.get("MiningFee").getAsInt();
			String UUID = Tx.get("UUID").getAsString();
			if (!transfer_balance(fromId, toId, value, miningFee, UUID, balance))
				return false;
		}
    	
    	return true;
    }

    public JsonObject CheckBlock(JsonObject block) {
    	boolean check1 = true;
    	//check if the block’s string hash is legitimate
    	
    	//check if the block’s hash to its previous block is indeed a block on the longest branch
    	JsonObject block_chosen = null;
    	boolean check2 = false;
    	PriorityQueue<branch> newBlockChain = new PriorityQueue<branch>();
    	String prevHash = block.get("PrevHash").getAsString();
    	int length;
    	if(!BlockChain.isEmpty()) {
    	    length = BlockChain.peek().length;
    	    while (!BlockChain.isEmpty()) {
    		    branch now = BlockChain.peek();
    		    String hash = Hash.getHashString(now.last_block.toString());
    		    if (prevHash.contentEquals(hash) && now.length == length && !check2) {
    			    check2 = true;
    			    block_chosen = now.last_block;
    		    }
    		    BlockChain.remove();
    		    newBlockChain.add(now);
    	    }
    	    BlockChain = newBlockChain;
    	}
    	else
    		check2 = true;
    	
    	//check if the block’s transactions are new transactions
    	boolean check3 = true;
    	JsonArray transactions = block.get("Transactions").getAsJsonArray();
    	int N = transactions.size();
    	for (int i=0; i<N; i++) {
    		JsonObject Tx = transactions.get(i).getAsJsonObject();
    		if (!TxPool.contains(Tx))
    			check3 = false;
    	}
    	
    	//check if the block’s transactions are all legitimate
    	boolean check4 = true;
    	if (check2) {
    		HashMap<String, Integer> balance;
    		if (block_chosen != null)
    	         balance = compute_balance(block_chosen);
    		else
    			 balance = new HashMap<String, Integer>();
    	    if (!Update_by_block(balance, block))
    	    	check4 = false;
    	}
    	
    	if (check1 && check2 && check3 && check4)
            return block_chosen;
    	else
    		return null;
    }
    
    public void PutBlock(JsonObject block, JsonObject block_chosen) {
    	PriorityQueue<branch> newBlockChain = new PriorityQueue<branch>();
    	if (block_chosen != null) {
    	while (BlockChain.isEmpty()) {
    		branch now = BlockChain.peek();
    		if (now.last_block.equals(block_chosen))
    			newBlockChain.add(new branch(now.length+1, block));
    		else
    			newBlockChain.add(now);
    	}
    	}
    	else
    		newBlockChain.add(new branch(1, block));
    	BlockChain = newBlockChain;
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
    public String intToHex(int i){ // i < 128 here
		return Integer.toHexString(i);
	}

    public JsonObject compute_nonce(JsonObject block){
        //compute
        System.out.println("starting compute_nonce()");
        this.computing = true;
        if (block == null)
        	System.out.println("1");
        //block = (JsonObject)block.remove("Nonce");
        if (block == null)
        	System.out.println("2");
        jumpOut:
        for(int i1 = 0; i1 < 16; i1++){
            for(int i2 = 0; i2 < 16; i2++){
                for(int i3 = 0; i3 < 16; i3++){
                    for(int i4 = 0; i4 < 16; i4++){
                        for(int i5 = 0; i5 < 16; i5++){
                            for(int i6 = 0; i6 < 16; i6++){
                                for(int i7 = 0; i7 < 16; i7++){
                                    for(int i8 = 0; i8 < 16; i8++){
                                        String nonce = intToHex(i1) + intToHex(i2) + intToHex(i3) + intToHex(i4) 
                                                     + intToHex(i5) + intToHex(i6) + intToHex(i7) + intToHex(i8);
                                        block.addProperty("Nonce",  nonce);
                                        if(Hash.checkHash(Hash.getHashString(block.toString()))){
                                            System.out.println("compute_nonce(): Compute completed.");
                                            //addValue(block.get("MinerID").getAsString(), block.get("MiningFee").getAsInt());
                                            return block;
                                        }
                                        if(newBlock){
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
        this.newBlock = false;
        this.computing = false;
        System.out.println("compute_nonce(): Stop computing blocks because received available block, return empty string.");
        return block;
    }
}