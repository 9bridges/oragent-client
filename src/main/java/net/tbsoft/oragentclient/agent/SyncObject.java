package net.tbsoft.oragentclient.agent;


import lombok.Data;

@Data
public class SyncObject {
    private int mapId;
    private String[] tableList;
    private int used;
    public SyncObject(int mapId, int used, String... tableList) {
        this.mapId=mapId;
        this.used=used;
        this.tableList=tableList;
    }
}
