package net.tbsoft.oragentclient.agent.config;


import lombok.Data;
import net.tbsoft.oragentclient.agent.NodeConfig;
import net.tbsoft.oragentclient.agent.SyncConfig;

import java.util.List;

@Data
public class StoreCaches {
    private List<NodeConfig> nodeConfig;
    private List<SyncConfig> syncConfigs;
}
