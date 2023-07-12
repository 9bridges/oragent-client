package net.tbsoft.oragentclient.agent.config;

import lombok.Data;

@Data
public class OragentConfig {
    private String hostName;
    private Integer dbPort;
    private String databaseName;
    private String userName;
    private String password;
    private Integer dataPort;
    private String clientHost;
    private String[] schemaList;
    private String[] tableList;
    private AsmConfig asmConfig;
    private Integer webPort;

    private int srcId=1;
    private int mapId;
    private int mapTgtId;
    private StartupMode startupMode = StartupMode.LATEST_OFFSET;

    public String getSrcLogin() {
        return this.userName + "/" + this.password + "@" + this.hostName + ":" + this.dbPort + "/" + databaseName;
    }
}
