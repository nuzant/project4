package iiis.systems.os.blockdb;

public class ServerInfo{
    public String host;
    public int port;
    public String dataDir;

    ServerInfo(String host, int port, String dataDir){
        this.host = host;
        this.port = port;
        this.dataDir = dataDir;
    }
}