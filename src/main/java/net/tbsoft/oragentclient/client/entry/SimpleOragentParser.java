/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

import net.tbsoft.oragentclient.util.BytesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SimpleOragentParser implements OragentParser {
    private final Logger logger = LoggerFactory.getLogger(SimpleOragentParser.class);
    private final int DEFAULT_PARSER_THREAD_COUNT = 8;
    private final ExecutorService parseTheadPool;
    private final ArrayList<Future<List<OragentEntry>>> parseFutures;
    private static long currentInstant = 0;

    public SimpleOragentParser() {
        parseTheadPool = Executors.newFixedThreadPool(DEFAULT_PARSER_THREAD_COUNT);
        parseFutures = new ArrayList<>();
    }

    void dealResultList(BlockingQueue<OragentEntry> outQueue) {
        if (parseFutures.isEmpty()) {
            return;
        }
        parseFutures.forEach(result -> {
            try {
                List<OragentEntry> oragentEntries = result.get();
                oragentEntries.forEach(oragentEntry -> {
                    try {
                        if (oragentEntry != null) {
                            if (oragentEntry instanceof OragentTransStart) {
                                OragentTransStart oragentTransStart = (OragentTransStart) oragentEntry;
                                currentInstant = oragentTransStart.getSourceTime();
                            }
                            oragentEntry.setSourceTime(currentInstant);
                            outQueue.put(oragentEntry);
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
    public void parser(byte[] bytes, BlockingQueue<OragentEntry> outQueue) {
        if (bytes == null) {
            logger.warn("parser bytes is null, do nothing and just return.");
            return;
        }
        int offset = 0;
        while (offset < bytes.length) {
            int dataLength = (int) BytesUtils.toUnsignedInt(BytesUtils.copyBytesByPos(bytes, offset, 4));
            if (dataLength > 0) {
                byte[] data = BytesUtils.copyBytesByPos(bytes, offset, dataLength);
                Future<List<OragentEntry>> submit = parseTheadPool.submit(new ParseTask(data));
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

class ParseTask implements Callable<List<OragentEntry>> {
    private final Logger logger = LoggerFactory.getLogger(SimpleOragentParser.class);
    private static final int ENTRY_TYPE_OFFSET = 4;
    private final byte[] data;

    ParseTask(byte[] data) {
        this.data = data;
    }

    @Override
    public List<OragentEntry> call() throws IOException {
        List<OragentEntry> oragentEntries = new ArrayList<>();
        OragentEntry oragentEntry = null;

        OpCode code = OpCode.from(data[ENTRY_TYPE_OFFSET] & 0xff);
        switch (code) {
            case INSERT:
                oragentEntry = new OragentDmlIrp();
                break;
            case DELETE:
                oragentEntry = new OragentDmlDrp();
                break;
            case UPDATE:
                oragentEntry = new OragentDmlUrp();
                break;
            case MULIT_INSERT:
                oragentEntry = new OragentDmlQmi();
                break;
            case MULIT_DELETE:
                oragentEntry = new OragentDmlQmd();
                break;
            case COMMIT:
                oragentEntry = new OragentTransCommit();
                break;
            case START:
                oragentEntry = new OragentTransStart();
                break;
            case DDL: // todo deal ddl
                /*
                 * oragentEntry = new OragentDdlEntryImpl();
                 * break;
                 */
            case UNSUPPORTED:
                logger.warn("unsupport opcode: {}", data[ENTRY_TYPE_OFFSET] & 0xff);
        }
        if (oragentEntry != null) {
            oragentEntry.parse(data);
            switch (code) {
                case MULIT_DELETE:
                case MULIT_INSERT:
                    List<OragentEntry> oragentDmlMulitList = ((OragentDmlQmi) oragentEntry).toList();
                    oragentEntries.addAll(oragentDmlMulitList);
                    break;
                default:
                    oragentEntries.add(oragentEntry);
                    break;
            }
        }

        return oragentEntries;
    }
}
