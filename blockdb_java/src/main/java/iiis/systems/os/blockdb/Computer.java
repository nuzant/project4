package iiis.systems.os.blockdb;

public class Computer extends Thread{
    Thread t;
    private String name = "computer";

    private String result = "default";
    private DatabaseEngine dbEngine = null;

    Computer(DatabaseEngine dbEngine){
        this.dbEngine = dbEngine;
    }

    public void run(){
        
    }

    public void start(){
        System.out.println("Start to run computer.");
        if (t == null){
            t = new Thread(this, name);
            t.start();
        }
    }

    public String getResult(){
        return result;
    }
}

