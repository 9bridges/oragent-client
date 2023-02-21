/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.client;

import net.tbsoft.fzsclient.client.entry.FzsEntry;

public interface FzsRecordListener {
    void process(FzsEntry lcr);
}
