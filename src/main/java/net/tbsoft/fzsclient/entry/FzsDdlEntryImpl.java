/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.entry;

import java.time.Instant;

public class FzsDdlEntryImpl implements FzsDdlEntry {
    public FzsDdlEntryImpl() {
    }

    public void parse(byte[] bytes) {
        // TODO: parse ddl entry
    }

    public void setDDLString(String ddlString) {

    }

    public void setObjectType(String objectType) {

    }

    public String getDDLString() {
        return null;
    }

    @Override
    public String getObjectType() {
        return null;
    }

    public void setDatabaseName(String var1) {

    }

    public void setObjectName(String name) {

    }

    public void setObjectOwner(String name) {

    }

    public void setSourceTime(Instant var1) {

    }

    public void setScn(long scn) {

    }

    public void setTransactionId(String var1) {

    }

    public void setEventType(OpCode var1) {

    }

    @Override
    public String getObjectName() {
        return null;
    }

    @Override
    public String getObjectOwner() {
        return null;
    }

    @Override
    public Instant getSourceTime() {
        return null;
    }

    @Override
    public long getScn() {
        return 0;
    }

    @Override
    public String getTransactionId() {
        return null;
    }

    @Override
    public OpCode getEventType() {
        return null;
    }

    @Override
    public String getDatabaseName() {
        return null;
    }
}
