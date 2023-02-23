package net.tbsoft.oragentclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomOragentConnection implements OragentConnection {
    private final static Logger log = LoggerFactory.getLogger(CustomOragentConnection.class);
    private int port;
    private BlockingQueue<byte[]> outQueue;
    private boolean isRunning = false;
    private String serverID;
    private ServerSocket serverSocket;

    private ThreadPoolExecutor executor;

    CustomOragentConnection() {
        outQueue = new LinkedBlockingQueue<>(20);
    }

    public static byte[] file2byte(String path) {
        try {
            FileInputStream in = new FileInputStream(new File(path));
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ThreadPoolExecutor createThreadPoolExecutor() {
        int poolCoreSize = 10;
        int maximumSize = 100;
        int poolKeepAlive = 5000;
        int queueCapacity = 10;

        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(queueCapacity);

        return new ThreadPoolExecutor(poolCoreSize, maximumSize, poolKeepAlive, TimeUnit.MILLISECONDS, blockingQueue);
    }

    @Override
    public void boundPort(int port) {
        this.port = port;
        outQueue = new LinkedBlockingQueue<>(20);
    }

    @Override
    public byte[] poll() {
        log.info("OragentConnection begin poll");
        byte[] bytes = null;
        try {
            bytes = outQueue.take();
        } catch (InterruptedException ignored) {
            // do nothing
        }
        return bytes;
    }

    @Override
    public void stop() {
        try {
            isRunning = false;
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        serverID = "SER-" + UUID.randomUUID().toString().substring(0, 6);
        executor = createThreadPoolExecutor();
        isRunning = true;
        int index = 0;
        while (isRunning) {
            if (serverSocket == null || serverSocket.isClosed()) {
                try {
                    serverSocket = new ServerSocket(port, 10);
                    serverSocket.setReuseAddress(true);
                    index = 0;
                    log.info("[{}] create ServerSocket at port={}", serverID, port);
                } catch (IOException e) {
                    log.error("[{}] create ServerSocket failed {} port={}", serverID, e.getMessage(), port);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignore) {
                    }
                    continue;
                }
            }
            try {
                //System.out.printf("[%s]%s wait for accept socket...\n", tName, serverName);
                log.info("[{}] wait socket connect...", serverID);
                RequestJob requestJob = new RequestJob("JOB-" + index + "-" + UUID.randomUUID().toString().substring(0, 4), serverID, serverSocket.accept(), outQueue);
                executor.execute(requestJob);
                index++;
            } catch (IOException e) {
                log.warn("[{}] accept socket failed {}", serverID, e.getMessage());
            }
        }
        //System.out.printf("[%s]%s exit...\n", tName, serverName);
        log.info("[{}] exit...", serverID);
    }
}
