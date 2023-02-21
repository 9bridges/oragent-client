/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.entry;

public interface FzsDmlEntry extends FzsEntry {

    Object[] getOldValues();

    Object[] getNewValues();

    String[] getOldColumnNames();

    String[] getNewColumnNames();

    int[] getNewColumnTypes();

    int[] getOldColumntypes();
}
