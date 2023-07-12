package net.tbsoft.oragentclient.agent;


import lombok.Data;

@Data
public class NodeConfig {
    private int id;
    private String md5Ip;
    private int port;
    private int md5Port;
    private int webPort;

    public NodeConfig(int id, String md5Ip,int md5Port) {
        this.id = id;
        this.md5Ip = md5Ip;
        this.md5Port = md5Port;
        this.port=md5Port;

    }
}
