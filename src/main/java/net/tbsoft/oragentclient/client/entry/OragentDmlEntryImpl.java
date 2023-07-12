/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

import io.netty.buffer.ByteBuf;
import net.tbsoft.oragentclient.util.RowidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

public abstract class OragentDmlEntryImpl implements OragentDmlEntry {
    private Logger logger = LoggerFactory.getLogger(OragentDmlEntryImpl.class);
    private Object[] newValues = null;
    private Object[] oldValues = null;
    private String objectOwner = null;
    private String objectName = null;
    private long sourceTime;
    private String[] newColumNames;
    private String[] oldColumnNames;
    private String rowid;

    private int[] newColumnTypes;
    private int[] oldColumntypes;
    private String dataBaseName;
    private long scn = -1;
    private short subScn = -1;
    private String transactionId;
    protected static final int OP_SIZE_LEN = 4;
    protected static final int OP_CODE_LEN = 1;
    protected static final int OBJECT_ID_LEN = 4;
    protected static final int SCN_LEN = 8;
    protected static final int SUB_SCN_LEN = 2;
    protected static final int TRANS_ID_LEN = 8;
    protected static final int OBJECT_PART_ID_LEN = 4;
    protected static final int OBJECT_DATA_ID_LEN = 4;
    protected static final int DSCN_LEN = 8;
    protected static final int DBA_LEN = 4;
    protected static final int OP_SIZE_OFFSET = 0;
    protected static final int OP_CODE_OFFSET = OP_SIZE_OFFSET + OP_SIZE_LEN;
    protected static final int OBJECT_ID_OFFSET = OP_CODE_OFFSET + OP_CODE_LEN;
    protected static final int SCN_OFFSET = OBJECT_ID_OFFSET + OBJECT_ID_LEN;
    protected static final int SUB_SCN_OFFSET = SCN_OFFSET + SCN_LEN;
    protected static final int TRANS_ID_OFFSET = SUB_SCN_OFFSET + SUB_SCN_LEN;
    protected static final int DATA_ID_OFFSET = TRANS_ID_OFFSET + TRANS_ID_LEN + OBJECT_PART_ID_LEN;
    protected static final int DBA_OFFSET = DATA_ID_OFFSET + OBJECT_DATA_ID_LEN + DSCN_LEN;
    protected static final int SLT_OFFSET = DBA_OFFSET + DBA_LEN;

    public void setOldValues(Object[] var1) {
        oldValues = var1;
    }

    public void setNewValues(Object[] var1) {
        newValues = var1;
    }

    public void setOldColumnNames(String[] columnNames) {
        oldColumnNames = columnNames;

    }

    public void setNewColumnNames(String[] columNames) {
        newColumNames = columNames;
    }

    @Override
    public Object[] getOldValues() {
        return oldValues;
    }

    @Override
    public Object[] getNewValues() {
        return newValues;
    }

    public String[] getOldColumnNames() {
        return oldColumnNames;
    }

    public String[] getNewColumnNames() {
        return newColumNames;
    }

    public void setDatabaseName(String var1) {
        dataBaseName = var1;
    }

    public void setObjectName(String name) {
        objectName = name;
    }

    public void setObjectOwner(String name) {
        objectOwner = name;
    }

    @Override
    public void setSourceTime(long var1) {
        sourceTime = var1;
    }

    public void setScn(long scn) {
        this.scn = scn;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String getObjectName() {
        return objectName;
    }

    @Override
    public String getObjectOwner() {
        return objectOwner;
    }

    @Override
    public long getSourceTime() {
        return sourceTime;
    }

    @Override
    public long getScn() {
        return this.scn;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    @Override
    public String getDatabaseName() {
        return dataBaseName;
    }

    public void setNewColumnTypes(int[] newColumnTypes) {
        this.newColumnTypes = newColumnTypes;
    }

    public void setOldColumnTypes(int[] oldColumntypes) {
        this.oldColumntypes = oldColumntypes;
    }

    @Override
    public int[] getNewColumnTypes() {
        return newColumnTypes;
    }

    @Override
    public int[] getOldColumntypes() {
        return oldColumntypes;
    }

    @Override
    public String getRowid() {
        return rowid;
    }

    public void setRowid(ByteBuf byteBuf) {
        long objd = byteBuf.readerIndex(DATA_ID_OFFSET).readUnsignedInt();
        long dba = byteBuf.readerIndex(DBA_OFFSET).readUnsignedInt();
        int slt = byteBuf.readerIndex(SLT_OFFSET).readUnsignedShort();
        setRowid(objd, dba, slt);
    }

    public void setRowid(long objd, long dba, int slt) {
        rowid = RowidUtils.rowidEncode(objd, dba, slt);
    }

    boolean isBinaryLob(int colType) {
        ColumnType columnType = ColumnType.from(colType);
        // Column only define byte val, so if return null, means not bytes type
        if (columnType == null) {
            return false;
        }
        switch (columnType) {
            case ORAGENT_BLOB:
            case ORAGENT_RAW:
            case ORAGENT_LONGRAW:
            case ORAGENT_RAW2:
            case ORAGENT_BYTE1:
            case ORAGENT_BYTE2:
            case ORAGENT_BYTE3:
            case ORAGENT_BFILE:
            case ORAGENT_BYTE5:
                return true;
            default:
                return false;
        }
    }

    boolean isStringLob(int colType) {
        ColumnType columnType = ColumnType.from(colType);
        return columnType == ColumnType.ORAGENT_CLOB;
    }

    boolean isLob(int colType) {
        return isStringLob(colType) || isBinaryLob(colType);
    }

    boolean isZoneTime(int colType) {
        ColumnType columnType = ColumnType.from(colType);
        return columnType == ColumnType.ORAGENT_TIMESTAMP_WITH_LOCAL_TIMEZONE
                || columnType == ColumnType.ORAGENT_TIMESTAMP_WITH_TIMEZONE;
    }

    void setValueByColumnType(Object[] value, int colType, int colLen, byte[] bytes, int index) {
        if (colLen <= 0) {
            return;
        }
        if (isBinaryLob(colType)) {
            value[index] = new byte[colLen];
            System.arraycopy(bytes, 0, value[index], 0, colLen);
            return;
        }
        if (isZoneTime(colType)) {
            value[index] = "TO_TIMESTAMP_TZ('" +
                    new String(bytes) +
                    "')";
            return;
        }
        ColumnType columnType = ColumnType.from(colType);
        if (columnType == ColumnType.ORAGENT_DAY_TO_SECOND) {
            value[index] = "TO_DSINTERVAL('" +
                    new String(bytes) +
                    "')";
            return;
        } else if (columnType == ColumnType.ORAGENT_YEAR_TO_MONTH) {
            value[index] = "TO_YMINTERVAL('" +
                    new String(bytes) +
                    "')";
            return;
        }
        value[index] = new String(bytes);
    }

    @Override
    public String toString() {
        return "OragentDmlEntryImpl{" +
                "eventType=" + getEventType() +
                ", rowid=" + getRowid() +
                ", newValues=" + Arrays.toString(newValues) +
                ", oldValues=" + Arrays.toString(oldValues) +
                ", objectOwner='" + objectOwner + '\'' +
                ", objectName='" + objectName + '\'' +
                ", sourceTime=" + sourceTime +
                ", newColumNames=" + Arrays.toString(newColumNames) +
                ", oldColumnNames=" + Arrays.toString(oldColumnNames) +
                ", newColumnTypes=" + Arrays.toString(newColumnTypes) +
                ", oldColumntypes=" + Arrays.toString(oldColumntypes) +
                ", dataBaseName='" + getDatabaseName() + '\'' +
                ", scn=" + scn +
                ", subScn=" + subScn +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
