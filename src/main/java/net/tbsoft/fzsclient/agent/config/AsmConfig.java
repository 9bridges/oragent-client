package net.tbsoft.fzsclient.agent.config;

import lombok.Data;

@Data
public class AsmConfig {
    private String login;
    private String oracleSid;
    private String oracleHome;
    private AsmMode mode = AsmMode.DB;
    private String[] disk;
    private String[] dev;
}
