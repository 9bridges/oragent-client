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

    //fixme 以下默认参数值
    private int fullCnt = 1;
    private int srcId = 1;
    private int tgtId = 2;

    private int mapId = 1;
    private StartupMode startupMode = StartupMode.INITIAL;

    public String getSrcLogin() {
        return this.userName + "/" + this.password + "@" + this.hostName + ":" + this.dbPort + "/" + databaseName;
    }
}
