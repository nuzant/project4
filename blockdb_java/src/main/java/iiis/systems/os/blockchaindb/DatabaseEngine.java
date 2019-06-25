package iiis.systems.os.blockchaindb;

import java.io.FileWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
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
    
    // data structures
    protected class branch implements Comparable<branch> {
    	private int length;
    	private JsonObject last_block;
        
    	public branch(int _length, JsonObject _last_block) {
    		this.length = _length;
    		this.last_block = _last_block;
    	}
    	
    	@Override
    	public int compareTo(branch another) {
    		String hash1 = Hash.getHashString(this.last_block.toString());
    		String hash2 = Hash.getHashString(another.last_block.toString());
    		if (another.length != this.length)
    		    return another.length - this.length;
    		else
    			return hash1.compareTo(hash2);
        }
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

    private HashMap<String, Integer> balances = new HashMap<>();
    private HashMap<String, Integer> balances_block = new HashMap<>();
    private HashMap<String, Lock> locks = new HashMap<>();
    private HashMap<String, JsonObject> blocks = new HashMap<>();
    private PriorityQueue<branch> BlockChain = new PriorityQueue<branch>();
    private int minerId = 0;
    private int logLength = 0;
    private String dataDir;
    private int blockId = 1;
    private volatile JsonArray TxPool_new = new JsonArray();
    private volatile JsonArray TxPool_used = new JsonArray();

    private File logFile = new File(dataDir + "log.json");
    public volatile boolean newBlock = false;
    public volatile boolean computing = false;
    public volatile boolean getBlock = false;
    public volatile boolean recovering = false;
    public HashMap<String, String> remoteBlocks = new HashMap<>();
    public String getBlockHash;

    

    DatabaseEngine(String dataDir) {
        this.dataDir = dataDir;
    }

    //rpc calls 

    public int get(String userId) {
        return getOrInit(userId, balances_block);
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
        if(leaf!=null)
        response.result = leaf.length;
        else
        response.result = 0;
    	response.block = leaf.last_block.toString();
        return response;
    }

    public String getBlock(String hash){
        String response = null;
        if(blocks.containsKey(hash)){
            response = blocks.get(hash).toString();
        }
    	return response;
    }

    public void pushBlock(String block){
        JsonParser parser = new JsonParser();
        JsonObject Block = (JsonObject) parser.parse(block);

        processNewBlock(Block);
        newBlock = true;

        return;
    }

    public void pushTransaction(String fromId, String toId, int value, int miningFee, String UUID){
    	int fromBalance = getOrInit(fromId, balances);
    	int toBalance = getOrInit(toId, balances);
    	if (fromBalance > value) {
    		AddTx(fromId, toId, value, miningFee, UUID);
    		balances.put(fromId, fromBalance - value);
            balances.put(toId, toBalance + value - miningFee);
    	}
        return;
    }

    //rpc calls end

    //server interactions

    //called by server, set miner id
    public void setMinerId(int i){
        this.minerId = i;
    }

    //called by server when a new block is finished computing.
    public void pushComputedBlock(String block){
        JsonParser parser = new JsonParser();
    	JsonObject Block = (JsonObject) parser.parse(block);

        processNewBlock(Block);

    }

    //database wait for client to ask for a remote block
    public String getRemoteBlock(String hash){
        getBlock = true;
        getBlockHash = hash;
        try{
            synchronized(this){
                this.wait();
            }
        } catch(InterruptedException e){
            // nothing happens here
        }
        getBlock = false;
        if(remoteBlocks.containsKey(hash))
            return remoteBlocks.get(hash);
        else{
            return "notfound";
        }
    }

    //get new transaction pool size
    public int getTransSize(){
        return TxPool_new.size();    
    }
    
    //server interactionss end

    //local calls
    private int getOrInit(String userId, HashMap<String, Integer> balance) {
        if (balance.containsKey(userId)) {
            return balance.get(userId);
        } else {
            return 1000;
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
    	
    	TxPool_new.add(Tx);
    }
    
    //used to update mining fee
    public void addValue(JsonObject block, HashMap<String, Integer> balance){
        JsonArray transactions = block.get("Transactions").getAsJsonArray();
        int N = transactions.size();
        String minerId = block.get("MinerID").getAsString();
        getOrInit(minerId, balance);
        for (int i=0; i<N; i++) {
        	JsonObject Tx = transactions.get(i).getAsJsonObject();
        	int current = getOrInit(minerId, balance);
        	balance.put(minerId, current + Tx.get("MiningFee").getAsInt());
        }
        return;
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
    	int N = TxPool_new.size();
    	int i;
    	for (i=0; i<N && i<50; i++) {
    		transactions.add(TxPool_new.get(i));
    		//TxPool_used.add(TxPool_new.get(i));
    	}
    	for (; i<N; i++)
    		newTxPool.add(TxPool_new.get(i));
    	block.add("Transactions", transactions);

    	return block;
    }
    
    public void output_block(JsonObject block) {
         //create blockfile 
        File dir = new File(dataDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        File createBlockFile = new File(dataDir + Hash.getHashString(block.toString()) + ".json");
        //System.out.println(createBlockFile.getName());
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
        try(FileWriter file = new FileWriter(dataDir + Hash.getHashString(block.toString()) + ".json")){
            file.write(block.toString());
            file.flush();
            System.out.println("Writing information to block: " + dataDir + Hash.getHashString(block.toString()) + ".json");
        } catch(IOException e){
            //e.printStackTrace();
            System.out.println("Fail to write block: " + dataDir + Hash.getHashString(block.toString()) + ".json");
        }
    }

    public boolean transfer_balance(String fromId, String toId, int value, int miningFee, String UUID, HashMap<String, Integer> balance) {
        int fromBalance = getOrInit(fromId, balance);
        int toBalance = getOrInit(toId, balance);
        if(fromBalance - value < 0){
            return false;
        }
        
        balance.put(fromId, fromBalance - value);
        balance.put(toId, toBalance + value - miningFee);
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
    	String first_hash = "0000000000000000000000000000000000000000000000000000000000000000";
    	int depth = 0;
    	boolean found_in_BlockChain = false, found_in_TxPool = false;
        
        branch leaf =  BlockChain.peek();
        JsonObject current_block = new JsonObject();
        //try to find the transaction on the longest chain
        if(leaf!=null) {
            current_block = leaf.last_block;
    	    while (true) {
    		    JsonArray transactions = current_block.getAsJsonArray("Transactions");
    		    found_in_BlockChain = find_UUID(transactions, UUID);
    		
    		    if(found_in_BlockChain)
    			    break;
    		
        		String prev_hash = current_block.get("PrevHash").getAsString();
    	    	if(prev_hash.contentEquals(first_hash))
    		    	break;
            
                current_block = blocks.get(prev_hash);
                if(current_block == null) break;
                
                depth++;
            }
        }
        else
            found_in_BlockChain = false;
    	
    	//try to find the transaction in the TxPool
		found_in_TxPool = find_UUID(TxPool_new, UUID);
    	
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
        
        // mining fee should be computed block by block
        addValue(block, balance);
    	return true;
    }

    // compute balance of a chain (from a block)
    public HashMap<String, Integer> compute_balance(JsonObject block) {
    	HashMap<String, Integer> balance = new HashMap<>();
    	Stack<JsonObject> s = new Stack<JsonObject>();
    	String initHash = "0000000000000000000000000000000000000000000000000000000000000000";
    	String prevHash = block.get("PrevHash").getAsString();
    	s.push(block);
    	
    	while (!prevHash.equals(initHash)) {
            JsonObject now = blocks.get(prevHash);
            s.push(now);
            try{
                prevHash = now.get("PrevHash").getAsString();
            } catch (NullPointerException e){
                System.out.println("Null pointer in compute_balance(), prevHash:" + prevHash);
                break;
            }
    	}
    	
    	while(!s.empty()) {
    		JsonObject now = s.pop();
    		Update_by_block(balance, now);
    	}
    	
    	return balance;
    }

    // check if block hash is available
    public boolean checkHash(String block){
        return Hash.checkHash(Hash.getHashString(block));
    }

    // check if the previous block of this block is the end of the chain. return true if it is the first block of the chain and blockchain is empty
    public boolean checkPreviousBlock(JsonObject block){
        String init = "0000000000000000000000000000000000000000000000000000000000000000";

        if(BlockChain.isEmpty()){
            return block.get("PrevHash").getAsString().equals(init);
        } else {
            for(branch b: BlockChain){
                if(block.get("PrevHash").getAsString().equals(Hash.getHashString(b.last_block.toString()))) return true;
            }
            return false;
        }
    }

    // check if the transactions in block is new
    public boolean checkNewTrans(JsonObject block){
    	JsonArray transactions = block.get("Transactions").getAsJsonArray();
    	int N = transactions.size();
    	for (int i=0; i<N; i++) {
    		JsonObject Tx = transactions.get(i).getAsJsonObject();
    		if (TxPool_used.contains(Tx))
    			return false;
        }
        return true;
    }

    // chech if the transactions in the block are legit
    public boolean checkLegitTrans(JsonObject block){
        HashMap<String, Integer> balance = compute_balance(block);
        return Update_by_block(balance, block);
    }

    // put the block in blocks data structure
    public void putBlock(JsonObject block){
        blocks.put(Hash.getHashString(block.toString()), block);
    }

    // delete the block in blocks data structure
    public void deleteBlock(JsonObject block){
        blocks.remove(Hash.getHashString(block.toString()));
    }

    // extend the branch
    public void extendBranch(JsonObject block){
        String init = "0000000000000000000000000000000000000000000000000000000000000000";
        if(block.get("PrevHash").getAsString().equals(init)){
            BlockChain.add(new branch(1, block));
            return;
        }

        branch oldbranch = new branch(0, new JsonObject());
        for(branch old: BlockChain){
            if(block.get("PrevHash").getAsString().equals(Hash.getHashString(old.last_block.toString()))){
                oldbranch = old;
                break;
            }
        }
        branch newbranch = new branch(oldbranch.length + 1, block);
        BlockChain.remove(oldbranch);
        BlockChain.add(newbranch);
    }

    // update the transaction pool (both new and old) w.r.t. block
    public void updateTransPool(JsonObject block){
        JsonArray transactions = block.get("Transactions").getAsJsonArray();
    	int N = transactions.size();
    	for(int i=0; i<N; i++) {
    		JsonObject Tx = transactions.get(i).getAsJsonObject();
    		if(TxPool_new.contains(Tx))
    			TxPool_new.remove(Tx);
    		TxPool_used.add(Tx);
        }
    }
    
    public void print_balance_block(int i) {
    	//create blockfile 
        File dir = new File(dataDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        JsonArray block = new JsonArray();
        for(String key: balances_block.keySet()) {
        	JsonObject b = new JsonObject();
        	b.addProperty("User", key);
        	b.addProperty("Money", balances_block.get(key));
        	block.add(b);
        }
        
        File createBlockFile = new File(dataDir + "balance" + i + ".json");
        //System.out.println(createBlockFile.getName());
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
        try(FileWriter file = new FileWriter(dataDir + "balance" + i + ".json")){
            file.write(block.toString());
            file.flush();
        } catch(IOException e){
            //e.printStackTrace();
            System.out.println("Fail to write block: " + dataDir + Hash.getHashString(block.toString()) + ".json");
        }
    }

    // new block
    public void processNewBlock(JsonObject block){
        if(this.recovering) return;

        if(!checkHash(block.toString())){
            System.out.println("Reject hash: " + Hash.getHashString(block.toString()) + "for chechhash");
            return;
        }

        if(!checkPreviousBlock(block)){
            if(!recover(block.toString())){
                return;
            }
        }

        if(!checkNewTrans(block)){
            System.out.println("Reject hash: " + Hash.getHashString(block.toString()) + "for repeated transaction");
            return;
        }

        putBlock(block);
        extendBranch(block);
        updateTransPool(block);

        if(!checkLegitTrans(block)){
            deleteBlock(block);
            return;
        }

        // everytime we put a block into our memory, compute balance based on the block 
        // (the result of get operation should be based on newest block in memory)
        balances_block = compute_balance(block);
        balances = balances_block;
        output_block(block);
        print_balance_block(blockId);
        blockId++;
    }

    //local calls end

    //recover

    public String fileReadBlock(String path) throws IOException, FileNotFoundException{
        FileInputStream in = new FileInputStream(new File(path));
        int size = in.available();
        byte[] buffer = new byte[size];
        in.read(buffer);
        in.close();
        String block = new String(buffer);
        return block;
    }
        
    public boolean recover(String block){
        this.recovering = true;
        JsonParser parser = new JsonParser();
        JsonObject blockObject = (JsonObject) parser.parse(block);
        HashSet<String> recoveredChain = new HashSet<String>();

        //on an unknown chain
        //blocks.put(Hash.getHashString(block), blockObject);
        recoveredChain.add(block);
        String prevHash = blockObject.get("PrevHash").getAsString();
        int branchLength = 1;

        System.out.println("Starting to recover, from block:" + Hash.getHashString(block));

        while (!prevHash.equals("0000000000000000000000000000000000000000000000000000000000000000")){
            String path = dataDir + prevHash + ".json";
            String prevBlock = new String();
            try{
                prevBlock = fileReadBlock(path);
            } catch (FileNotFoundException e){
                System.err.println("Local file not found, path:" + path);
                long nowtime = System.currentTimeMillis();
                //boolean fail = true;
                while(true){ // try find remote target
                    prevBlock = getRemoteBlock(prevHash);
                    if(!prevBlock.equals("notfound")){
                        break;
                    }
                }
            } catch (IOException e){
            }

            System.out.println("Block recovered:" + prevHash);
            
            try{
                prevHash = ((JsonObject) parser.parse(prevBlock)).get("PrevHash").getAsString();
            } catch (NullPointerException e){
                System.err.println("Null pointer caused by:" + prevBlock + ", recover stopped.");
                this.recovering = false;
                return false;
            }
            if(recoveredChain.contains(prevBlock)){
                this.recovering = false;
                return false;
            } else {
                recoveredChain.add(prevBlock);
            }
            branchLength += 1;
        }

        int oldLength = 0;
        if(!BlockChain.isEmpty()) oldLength = getLongestBranch().length;

        if(branchLength >= oldLength){
            System.out.println("Successfully recovered chain, length = " + Integer.toString(branchLength) 
                                                                            + ", lastblock hash = " + Hash.getHashString(block));
            BlockChain.add(new branch(branchLength, blockObject));

            for(String b: recoveredChain){
                JsonObject bObj = (JsonObject) parser.parse(b);
                blocks.put(Hash.getHashString(b), bObj);
                output_block(bObj);
            }

            balances_block = compute_balance(blockObject);
            balances = balances_block;
            blockId = branchLength;

            this.recovering = false;
            return true;
        }

        System.out.print("Recover discard, not enough length.");
        this.recovering = false;
        return false;
    }
    
    //compute
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
                                            // addValue(block, balances_block);
                                            this.computing = false;
                                            System.out.println("compute_nonce(): Compute completed. Hash = " + Hash.getHashString(block.toString()));
                                            return block;
                                        }
                                        if(newBlock || recovering){
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
        System.out.println("compute_nonce(): Stop computing blocks because received available block, return null.");
        return null;
    }
}
