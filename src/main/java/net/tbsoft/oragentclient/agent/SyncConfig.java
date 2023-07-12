package net.tbsoft.oragentclient.agent;


import lombok.Data;

@Data
public class SyncConfig {
    private Integer mapId;
    private String[] mapTables;
    private Integer mapUse;
    private int mapTgtId;

    public SyncConfig(int mapId, int mapUse, String... mapTables) {
        this.mapId = mapId;
        this.mapUse = mapUse;
        this.mapTables = mapTables;
    }
}
