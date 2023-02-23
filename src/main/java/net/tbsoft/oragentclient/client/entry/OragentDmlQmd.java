/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

import java.util.ArrayList;
import java.util.List;

public class OragentDmlQmd extends OragentDmlQmi {

    @Override
    public OpCode getEventType() {
        return OpCode.MULIT_DELETE;
    }

    @Override
    public List<OragentEntry> toList() {
        List<OragentEntry> oragentEntries = new ArrayList<>();
        Object[][] datas = getRowDatas();
        for (int i = 0; i < getRowCount(); i++) {
            OragentDmlDrp oragentDmlDrp = new OragentDmlDrp();
            oragentDmlDrp.setDatabaseName(getDatabaseName());
            oragentDmlDrp.setObjectOwner(getObjectOwner());
            oragentDmlDrp.setObjectName(getObjectName());
            oragentDmlDrp.setOldColumnNames(getNewColumnNames());
            oragentDmlDrp.setOldValues(datas[i]);
            oragentDmlDrp.setOldColumnTypes(getNewColumnTypes());
            oragentDmlDrp.setScn(getScn());
            oragentDmlDrp.setTransactionId(getTransactionId());
            oragentDmlDrp.setSourceTime(getSourceTime());
            oragentEntries.add(oragentDmlDrp);
        }
        return oragentEntries;
    }
}
