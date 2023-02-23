/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client;

import net.tbsoft.oragentclient.client.entry.OragentEntry;

public interface OragentRecordListener {
    void process(OragentEntry lcr);
}
