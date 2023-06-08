package net.tbsoft.oragentclient;

import net.tbsoft.oragentclient.agent.config.OragentConfig;
import net.tbsoft.oragentclient.agent.config.AsmConfig;
import net.tbsoft.oragentclient.agent.config.StartupMode;

public abstract class Oragent {
    protected OragentConfig oragentConfig = new OragentConfig();

    public abstract void start();

    public abstract void stop();

    public abstract boolean isRunning();

    public Oragent hostname(String hostname) {
        oragentConfig.setHostName(hostname);
        return this;
    }

    public Oragent dbPort(int port) {
        oragentConfig.setDbPort(port);
        return this;
    }

    public Oragent database(String database) {
        oragentConfig.setDatabaseName(database);
        return this;
    }

    public Oragent tableList(String... tableList) {
        oragentConfig.setTableList(tableList);
        return this;
    }

    public Oragent schemaList(String... schemaList) {
        oragentConfig.setSchemaList(schemaList);
        return this;
    }

    public Oragent username(String username) {
        oragentConfig.setUserName(username);
        return this;
    }

    public Oragent password(String password) {
        oragentConfig.setPassword(password);
        return this;
    }

    public Oragent dataPort(int dataPort) {
        oragentConfig.setDataPort(dataPort);
        return this;
    }

    public Oragent dataPortOffset(int dataPortOffset) {
        oragentConfig.setDataPortOffset(dataPortOffset);
        return this;
    }

    public Oragent webPort(int webPort) {
        oragentConfig.setWebPort(webPort);
        return this;
    }

    public Oragent clientHost(String host) {
        oragentConfig.setClientHost(host);
        return this;
    }

    public Oragent asm(AsmConfig asmConfig) {
        oragentConfig.setAsmConfig(asmConfig);
        return this;
    }

    public Oragent startupMode(StartupMode startupMode) {
        oragentConfig.setStartupMode(startupMode);
        return this;
    }
}
