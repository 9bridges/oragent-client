package net.tbsoft.fzsclient.client;

public interface FzsConnection extends Runnable {
    void boundPort(int port);

    byte[] poll();

    void stop();
}
