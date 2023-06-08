package net.tbsoft.oragentclient.agent;


import lombok.Data;

import java.util.Set;

@Data
public class SyncConfig {
    private Integer mapId;
    private String[] mapTables;
    private Integer mapUse;
    private Set<Integer> mapTgtIds;

    public SyncConfig(int mapId, int mapUse, Set<Integer> mapTgtIds, String... mapTables) {
        this.mapId = mapId;
        this.mapUse = mapUse;
        this.mapTables = mapTables;
        this.mapTgtIds = mapTgtIds;
    }
}
