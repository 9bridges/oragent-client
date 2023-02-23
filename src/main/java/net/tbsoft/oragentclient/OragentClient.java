/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient;

import net.tbsoft.oragentclient.client.OragentProducer;
import net.tbsoft.oragentclient.client.OragentRecordListener;
import net.tbsoft.oragentclient.client.entry.OragentEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OragentClient {
    private static final Logger logger = LoggerFactory.getLogger(OragentClient.class);
    private Thread processThread;
    private Thread producerThread;
    private OragentRecordListener oragentRecordListener;
    private final OragentProducer oragentProducer;
    private final BlockingQueue<OragentEntry> recordQueue;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public OragentClient(int port) {
        this.recordQueue = new LinkedBlockingQueue<>(20000);
        oragentProducer = new OragentProducer(port, recordQueue);
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
            oragentProducer.stop();
            logger.info("Client stopped successfully");
        }
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            processThread = new Thread(() -> {
                while (isRunning()) {
                    OragentEntry lcr = null;

                    try {
                        lcr = recordQueue.poll(2000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        // ignore exception
                    }
                    if (lcr == null) {
                        continue;
                    }
                    oragentRecordListener.process(lcr);
                }

                triggerStop();
                logger.info("oragent process lcr thread exit");
            });
            producerThread = new Thread(oragentProducer);
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

    public void setListener(OragentRecordListener oragentRecordListener) {
        this.oragentRecordListener = oragentRecordListener;
    }
}
