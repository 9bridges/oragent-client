/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.entry;

import java.io.IOException;
import java.time.Instant;

public interface FzsEntry {
    String getObjectName();

    String getObjectOwner();

    Instant getSourceTime();

    long getScn();

    String getTransactionId();

    OpCode getEventType();

    String getDatabaseName();

    void parse(byte[] data) throws IOException;
}
