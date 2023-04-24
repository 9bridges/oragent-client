/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

import java.io.IOException;
import java.time.Instant;

public interface OragentEntry {
    String getObjectName();

    String getObjectOwner();

    long getSourceTime();

    long getScn();

    String getTransactionId();

    OpCode getEventType();

    String getDatabaseName();

    void setSourceTime(long scnTime);

    void parse(byte[] data) throws IOException;
}
