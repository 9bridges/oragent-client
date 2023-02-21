/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.entry;

import java.util.ArrayList;
import java.util.List;

public class FzsDmlQmd extends FzsDmlQmi {

    @Override
    public OpCode getEventType() {
        return OpCode.MULIT_DELETE;
    }

    @Override
    public List<FzsEntry> toList() {
        List<FzsEntry> fzsEntries = new ArrayList<>();
        Object[][] datas = getRowDatas();
        for (int i = 0; i < getRowCount(); i++) {
            FzsDmlDrp fzsDmlDrp = new FzsDmlDrp();
            fzsDmlDrp.setDatabaseName(getDatabaseName());
            fzsDmlDrp.setObjectOwner(getObjectOwner());
            fzsDmlDrp.setObjectName(getObjectName());
            fzsDmlDrp.setOldColumnNames(getNewColumnNames());
            fzsDmlDrp.setOldValues(datas[i]);
            fzsDmlDrp.setOldColumnTypes(getNewColumnTypes());
            fzsDmlDrp.setScn(getScn());
            fzsDmlDrp.setTransactionId(getTransactionId());
            fzsDmlDrp.setSourceTime(getSourceTime());
            fzsEntries.add(fzsDmlDrp);
        }
        return fzsEntries;
    }
}
