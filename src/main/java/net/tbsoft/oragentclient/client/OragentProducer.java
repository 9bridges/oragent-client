/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client;

import net.tbsoft.oragentclient.client.entry.OragentEntry;
import net.tbsoft.oragentclient.client.entry.OragentParser;
import net.tbsoft.oragentclient.client.entry.SimpleOragentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class OragentProducer implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(OragentProducer.class);
    private final OragentConnection oragentConnection;
    private final Thread oragentConnectionTask;
    private final OragentParser oragentParser;
    private final BlockingQueue<OragentEntry> outQueue;
    private final AtomicBoolean started = new AtomicBoolean(false);
    public static final Object UNAVAILABLE_VALUE = new Object();

    public OragentProducer(int port, BlockingQueue<OragentEntry> outQueue) {
        oragentConnection = new CustomOragentConnection();
        oragentConnection.boundPort(port);
        oragentParser = new SimpleOragentParser();
        this.outQueue = outQueue;
        oragentConnectionTask = new Thread(oragentConnection);
    }

    private boolean isRunning() {
        return started.get();
    }

    public void stop() {
        started.compareAndSet(true, false);
        oragentConnection.stop();
        logger.info("OragentProducer begin stop.");
    }

    @Override
    public void run() {
        if (started.compareAndSet(false, true)) {
            logger.info("OragentProducer started.");
            oragentConnectionTask.start();
            while (isRunning()) {
                oragentParser.parser(oragentConnection.poll(), outQueue);
            }
            logger.info("OragentProducer stopped.");
        }
    }
}
