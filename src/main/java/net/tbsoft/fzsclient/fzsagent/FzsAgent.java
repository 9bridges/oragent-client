package net.tbsoft.fzsclient.fzsagent;

import net.tbsoft.fzsclient.fzsagent.config.FzsConfig;
import net.tbsoft.fzsclient.fzsagent.config.AsmConfig;

public abstract class FzsAgent {
    protected FzsConfig fzsConfig = new FzsConfig();

    public abstract void start();

    public abstract void stop();

    public abstract boolean isRunning();

    public FzsAgent hostname(String hostname) {
        fzsConfig.setHostName(hostname);
        return this;
    }

    public FzsAgent dbPort(int port) {
        fzsConfig.setDbPort(port);
        return this;
    }

    public FzsAgent database(String database) {
        fzsConfig.setDatabaseName(database);
        return this;
    }

    public FzsAgent tableList(String... tableList) {
        fzsConfig.setTableList(tableList);
        return this;
    }

    public FzsAgent schemaList(String... schemaList) {
        fzsConfig.setSchemaList(schemaList);
        return this;
    }

    public FzsAgent username(String username) {
        fzsConfig.setUserName(username);
        return this;
    }

    public FzsAgent password(String password) {
        fzsConfig.setPassword(password);
        return this;
    }

    public FzsAgent dataPort(int dataPort) {
        fzsConfig.setDataPort(dataPort);
        return this;
    }

    public FzsAgent webPort(int webPort) {
        fzsConfig.setWebPort(webPort);
        return this;
    }

    public FzsAgent clientHost(String host) {
        fzsConfig.setClientHost(host);
        return this;
    }

    public FzsAgent asm(AsmConfig asmConfig) {
        fzsConfig.setAsmConfig(asmConfig);
        return this;
    }
}
