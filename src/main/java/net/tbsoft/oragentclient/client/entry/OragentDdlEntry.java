/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

public interface OragentDdlEntry extends OragentEntry {

    String getDDLString();

    String getObjectType();
}
