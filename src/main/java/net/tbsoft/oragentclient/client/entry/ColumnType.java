/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.oragentclient.client.entry;

public enum ColumnType {
    // those not all types, only define byte, lob, zone time value in oragent
    ORAGENT_CLOB(112),
    ORAGENT_BLOB(113),
    ORAGENT_RAW(23),
    ORAGENT_LONGRAW(24),
    ORAGENT_RAW2(95),
    ORAGENT_BYTE1(105),
    ORAGENT_BYTE2(106),
    ORAGENT_BYTE3(111),
    ORAGENT_BFILE(114),
    ORAGENT_BYTE5(115),
    ORAGENT_TIMESTAMP_WITH_TIMEZONE(181),
    ORAGENT_TIMESTAMP_WITH_LOCAL_TIMEZONE(231),
    ORAGENT_YEAR_TO_MONTH(182),
    ORAGENT_DAY_TO_SECOND(183),
    UNSUPPORTED(255);

    private static final ColumnType[] types = new ColumnType[256];

    static {
        for (ColumnType option : ColumnType.values()) {
            types[option.getValue()] = option;
        }
    }

    private final int value;

    ColumnType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ColumnType from(int value) {
        return value < types.length ? types[value] : ColumnType.UNSUPPORTED;
    }

}