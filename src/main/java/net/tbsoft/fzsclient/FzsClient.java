/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient;

import net.tbsoft.fzsclient.client.FzsProducer;
import net.tbsoft.fzsclient.client.FzsRecordListener;
import net.tbsoft.fzsclient.client.entry.FzsEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class FzsClient {
    private static final Logger logger = LoggerFactory.getLogger(FzsClient.class);
    private Thread processThread;
    private Thread producerThread;
    private FzsRecordListener fzsRecordListener;
    private final FzsProducer fzsProducer;
    private final BlockingQueue<FzsEntry> recordQueue;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public FzsClient(int port) {
        this.recordQueue = new LinkedBlockingQueue<>(20000);
        fzsProducer = new FzsProducer(port, recordQueue);
    }

    public void join() {
        if (processThread != null) {
            try {
                processThread.join();
            } catch (InterruptedException e) {
                logger.warn("Waits for process thread failed : {}", e.getMessage());
                triggerStop();
            }
        }
    }

    public void stop() {
        if (started.compareAndSet(true, false)) {
            logger.info("Try to stop this client");
            join();
            fzsProducer.stop();
            logger.info("Client stopped successfully");
        }
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            processThread = new Thread(() -> {
                while (isRunning()) {
                    FzsEntry lcr = null;

                    try {
                        lcr = recordQueue.poll(2000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        // ignore exception
                    }
                    if (lcr == null) {
                        continue;
                    }
                    fzsRecordListener.process(lcr);
                }

                triggerStop();
                logger.info("fzs process lcr thread exit");
            });
            producerThread = new Thread(fzsProducer);
            processThread.start();
            producerThread.start();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public boolean isRunning() {
        return started.get();
    }

    public void triggerStop() {
        new Thread(this::stop).start();
    }

    public void setListener(FzsRecordListener fzsRecordListener) {
        this.fzsRecordListener = fzsRecordListener;
    }
}
