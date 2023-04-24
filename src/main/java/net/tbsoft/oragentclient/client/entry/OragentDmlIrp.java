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
import java.io.UnsupportedEncodingException;
import java.time.Instant;


public class OragentDmlIrp extends OragentDmlEntryImpl {

    void parseColumnValues(ByteBuf byteBuf) throws UnsupportedEncodingException {
        int columnCount = BytesUtils.getByteOrShort(byteBuf);
        String[] columNames = new String[columnCount];
        int[] columnTypes = new int[columnCount];
        Object[] values = new Object[columnCount];
        for (int index = 0; index < columnCount; index++) {
            int colLen = BytesUtils.getByteOrInt(byteBuf);
            byte[] bytes = null;
            if (colLen > 0) {
                bytes = BytesUtils.readBytes(byteBuf, colLen);
            }
            columNames[index] = BytesUtils.getString(byteBuf);
            columnTypes[index] = BytesUtils.getByteOrShort(byteBuf);
            BytesUtils.getByteOrInt(byteBuf); // col_length_max
            setValueByColumnType(values, columnTypes[index], colLen, bytes, index);
            byteBuf.readerIndex(byteBuf.readerIndex() + 5); // col_unique + col_csform + col_csid + col_null
        }
        setValues(values, columNames, columnTypes);

    }

    protected void setValues(Object[] values, String[] colNames, int[] colTypes) {
        setNewValues(values);
        setNewColumnNames(colNames);
        setNewColumnTypes(colTypes);
    }

    @Override
    public void parse(byte[] data) throws IOException {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data, 0, data.length);
        setScn(byteBuf.readerIndex(SCN_OFFSET).readLong());
        setTransactionId(Long.toString(byteBuf.readerIndex(TRANS_ID_OFFSET).readLong()));
        byteBuf.readerIndex(64);
        setObjectOwner(BytesUtils.getString(byteBuf));
        setObjectName(BytesUtils.getString(byteBuf));
        byteBuf.readerIndex(byteBuf.readerIndex() + 1); // table has pk or uk
        byteBuf.readerIndex(byteBuf.readerIndex() + 8); // scn_time
        parseColumnValues(byteBuf);
    }

    @Override
    public OpCode getEventType() {
        return OpCode.INSERT;
    }
}
