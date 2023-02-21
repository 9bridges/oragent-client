/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.entry;

import net.tbsoft.fzsclient.util.BytesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimpleFzsParser implements FzsParser {
    private final Logger logger = LoggerFactory.getLogger(SimpleFzsParser.class);
    private final int DEFAULT_PARSER_THREAD_COUNT = 8;
    private final ExecutorService parseTheadPool;
    private final ArrayList<Future<List<FzsEntry>>> parseFutures;

    public SimpleFzsParser() {
        parseTheadPool = Executors.newFixedThreadPool(DEFAULT_PARSER_THREAD_COUNT);
        parseFutures = new ArrayList<>();
    }

    void dealResultList(BlockingQueue<FzsEntry> outQueue) {
        if (parseFutures.isEmpty()) {
            return;
        }
        parseFutures.forEach(result -> {
            try {
                List<FzsEntry> fzsEntries = result.get();
                fzsEntries.forEach(fzsEntry -> {
                    try {
                        if (fzsEntry != null) {
                            outQueue.put(fzsEntry);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        parseFutures.clear();
    }

    @Override
    public void parser(byte[] bytes, BlockingQueue<FzsEntry> outQueue) {
        if (bytes == null) {
            logger.warn("parser bytes is null, do nothing and just return.");
            return;
        }
        int offset = 0;
        while (offset < bytes.length) {
            int dataLength = (int) BytesUtils.toUnsignedInt(BytesUtils.copyBytesByPos(bytes, offset, 4));
            if (dataLength > 0) {
                byte[] data = BytesUtils.copyBytesByPos(bytes, offset, dataLength);
                Future<List<FzsEntry>> submit = parseTheadPool.submit(new ParseTask(data));
                parseFutures.add(submit);
                if (parseFutures.size() >= DEFAULT_PARSER_THREAD_COUNT) {
                    dealResultList(outQueue);
                }
            }
            offset += dataLength + 4;
        }
        dealResultList(outQueue);
    }
}

class ParseTask implements Callable<List<FzsEntry>> {
    private final Logger logger = LoggerFactory.getLogger(SimpleFzsParser.class);
    private static final int ENTRY_TYPE_OFFSET = 4;
    private final byte[] data;

    ParseTask(byte[] data) {
        this.data = data;
    }

    @Override
    public List<FzsEntry> call() throws IOException {
        List<FzsEntry> fzsEntries = new ArrayList<>();
        FzsEntry fzsEntry = null;

        OpCode code = OpCode.from(data[ENTRY_TYPE_OFFSET] & 0xff);
        switch (code) {
            case INSERT:
                fzsEntry = new FzsDmlIrp();
                break;
            case DELETE:
                fzsEntry = new FzsDmlDrp();
                break;
            case UPDATE:
                fzsEntry = new FzsDmlUrp();
                break;
            case MULIT_INSERT:
                fzsEntry = new FzsDmlQmi();
                break;
            case MULIT_DELETE:
                fzsEntry = new FzsDmlQmd();
                break;
            case COMMIT:
                fzsEntry = new FzsTransCommit();
                break;
            case DDL: // todo deal ddl
                /*
                 * fzsEntry = new FzsDdlEntryImpl();
                 * break;
                 */
            case UNSUPPORTED:
                logger.warn("unsupport opcode: {}", data[ENTRY_TYPE_OFFSET] & 0xff);
        }
        if (fzsEntry != null) {
            fzsEntry.parse(data);
            switch (code) {
                case MULIT_DELETE:
                case MULIT_INSERT:
                    List<FzsEntry> fzsDmlMulitList = ((FzsDmlQmi) fzsEntry).toList();
                    fzsEntries.addAll(fzsDmlMulitList);
                    break;
                default:
                    fzsEntries.add(fzsEntry);
                    break;
            }
        }

        return fzsEntries;
    }
}
