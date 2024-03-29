package iiis.systems.os.blockchaindb;

import com.google.gson.JsonObject;

public class Computer extends Thread{
    Thread t;
    private String name = "computer";

    private String result = "default";
    private DatabaseEngine dbEngine = null;

    private volatile JsonObject block = null; 

    public volatile boolean finished = false;

    Computer(DatabaseEngine dbEngine){
        this.dbEngine = dbEngine;
    }

    public void run(){
        while(true){
            try{
                synchronized(this){
                    this.wait();
                    block = dbEngine.compute_nonce(block);
                    if(block != null){
                        this.finished = true;
                    }
                }
            } catch(InterruptedException e) {
                //i dont actually know in what situation this can happen
                System.err.println("Computer thread interrupted.");
                Thread.currentThread().interrupt();
            }
            //this.finished = false;
        }
    }

    public void setFinished(boolean b){
        this.finished = b;
    }

    public void start(){
        System.out.println("Start to run computer.");
        if (t == null){
            t = new Thread(this, name);
            t.start();
        }
    }

    public void clearBlock(){
        this.block = null;
    }

    public void setBlock(JsonObject block){
        this.block = block;
    }

    public String getBlock(){
        return this.block.toString();
    }

    public String getResult(){
        return result;
    }
}

