/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.client;

import net.tbsoft.fzsclient.entry.FzsEntry;
import net.tbsoft.fzsclient.entry.FzsParser;
import net.tbsoft.fzsclient.entry.SimpleFzsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class FzsProducer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(FzsProducer.class);
    private final FzsConnection fzsConnection;
    private final Thread fzsConnectionTask;
    private final FzsParser fzsParser;
    private final BlockingQueue<FzsEntry> outQueue;
    private final AtomicBoolean started = new AtomicBoolean(false);
    public static final Object UNAVAILABLE_VALUE = new Object();

    public FzsProducer(int port, BlockingQueue<FzsEntry> outQueue) {
        fzsConnection = new CustomFzsConnection();
        fzsConnection.boundPort(port);
        fzsParser = new SimpleFzsParser();
        this.outQueue = outQueue;
        fzsConnectionTask = new Thread(fzsConnection);
    }

    private boolean isRunning() {
        return started.get();
    }

    public void stop() {
        started.compareAndSet(true, false);
        fzsConnection.stop();
        logger.info("FzsProducer begin stop.");
    }

    @Override
    public void run() {
        if (started.compareAndSet(false, true)) {
            logger.info("FzsProducer started.");
            fzsConnectionTask.start();
            while (isRunning()) {
                fzsParser.parser(fzsConnection.poll(), outQueue);
            }
            logger.info("FzsProducer stopped.");
        }
    }
}
