package net.tbsoft.oragentclient.util;

public class RowidUtils {
    private static final String base64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    private static String ubyte4ToBase64(long n, long v) {
        int i;
        short c;
        StringBuilder rowid = new StringBuilder();
        for (i = 0; i < n; i++) {
            c = (short) (v & 0x3f);
            v >>= 6;
            rowid.append(base64, c, c + 1);
        }
        return rowid.reverse().toString();
    }

    public static String rowidEncode(long objd, long dba, int slt) {
        long f_no;
        long blk;
        StringBuilder rowid = new StringBuilder();
        f_no = dba >> 22;
        blk = (int)(dba << 10) >> 10;

        rowid.append(ubyte4ToBase64(6, objd));
        rowid.append(ubyte4ToBase64(3, f_no));
        rowid.append(ubyte4ToBase64(6, blk));
        rowid.append(ubyte4ToBase64(3, slt));
        return rowid.toString();
    }
}
