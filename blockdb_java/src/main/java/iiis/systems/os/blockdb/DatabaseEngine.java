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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private HashMap<String, Integer> balances = new HashMap<>();
    private HashMap<String, Lock> locks = new HashMap<>();
    private int logLength = 0;
    private String dataDir;
    private int blockId = 1;
    private JsonArray blockTransRecord = new JsonArray();
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
