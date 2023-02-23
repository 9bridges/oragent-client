/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.tbsoft.oragentclient.util.BytesUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OragentDmlQmi extends OragentDmlEntryImpl {
    private int rowCount;
    private Object[][] rowDatas;

    @Override
    public OpCode getEventType() {
        return OpCode.MULIT_INSERT;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data, 0, data.length);
        setScn(byteBuf.readerIndex(SCN_OFFSET).readLong());
        setTransactionId(Long.toString(byteBuf.readerIndex(TRANS_ID_OFFSET).readLong()));
        byteBuf.readerIndex(52);
        setObjectOwner(BytesUtils.getString(byteBuf));
        setObjectName(BytesUtils.getString(byteBuf));
        BytesUtils.getString(byteBuf); // part
        setSourceTime(Instant.now());
        byteBuf.readerIndex(byteBuf.readerIndex() + 12); // table has pk or uk, scn_time, is_full, if_can_dp, queue_id
        parseColumnValues(byteBuf);
    }

    void parseColumnValues(ByteBuf byteBuf) throws IOException {
        rowCount = byteBuf.readShort();
        rowDatas = new Object[rowCount][];
        for (int row = 0; row < rowCount; row++) {
            BytesUtils.getByteOrShort(byteBuf); // slt
            byteBuf.readerIndex(byteBuf.readerIndex() + 2); // flag, bit_flag
            int columnCount = BytesUtils.getByteOrShort(byteBuf);
            rowDatas[row] = new Object[columnCount];
            String[] colmnNames = new String[columnCount];
            int[] columnTypes = new int[columnCount];
            for (int col = 0; col < columnCount; col++) {
                int colLen = BytesUtils.getByteOrShort(byteBuf);
                byte[] bytes = null;
                if (colLen > 0) {
                    bytes = BytesUtils.readBytes(byteBuf, colLen);
                }
                if (row == 0) {
                    colmnNames[col] = BytesUtils.getString(byteBuf);
                    columnTypes[col] = BytesUtils.getByteOrShort(byteBuf);
                    BytesUtils.getByteOrInt(byteBuf); // col_len_max
                    byteBuf.readerIndex(byteBuf.readerIndex() + 5); // col_unique, col_dsform, col_csid, col_null

                }
                setValueByColumnType(rowDatas[row], columnTypes[col], colLen, bytes, col);
            }
            if (row == 0) {
                setNewColumnNames(colmnNames);
                setNewColumnTypes(columnTypes);
            }
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public Object[][] getRowDatas() {
        return rowDatas;
    }

    public List<OragentEntry> toList() {
        List<OragentEntry> oragentEntries = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            OragentDmlIrp oragentDmlIrp = new OragentDmlIrp();
            oragentDmlIrp.setDatabaseName(getDatabaseName());
            oragentDmlIrp.setObjectOwner(getObjectOwner());
            oragentDmlIrp.setObjectName(getObjectName());
            oragentDmlIrp.setNewColumnNames(getNewColumnNames());
            oragentDmlIrp.setNewValues(rowDatas[i]);
            oragentDmlIrp.setNewColumnTypes(getNewColumnTypes());
            oragentDmlIrp.setScn(getScn());
            oragentDmlIrp.setTransactionId(getTransactionId());
            oragentDmlIrp.setSourceTime(getSourceTime());
            oragentEntries.add(oragentDmlIrp);
        }
        return oragentEntries;
    }
}
