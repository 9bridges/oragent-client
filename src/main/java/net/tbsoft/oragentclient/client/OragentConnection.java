package net.tbsoft.oragentclient.client;

public interface OragentConnection extends Runnable {
    void boundPort(int port);

    byte[] poll();

    void stop();
}
