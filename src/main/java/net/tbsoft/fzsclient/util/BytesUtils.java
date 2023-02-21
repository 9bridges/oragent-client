/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package net.tbsoft.fzsclient.util;

import io.netty.buffer.ByteBuf;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.InflaterInputStream;

public final class BytesUtils {

    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream iis = new InflaterInputStream(bais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        byte[] buf = new byte[4096];
        int readSize = 0;
        while ((readSize = iis.read(buf, 0, 4096)) > 0) {
            baos.write(buf, 0, readSize);
        }
        baos.flush();
        bais.close();
        return baos.toByteArray();
    }

    public static int readBytes(InputStream inputStream, byte[] content, int size) throws IOException {
        int read;
        int readed = 0;
        while (readed < size) {
            read = inputStream.read(content, readed, size - readed);
            readed += read;
        }
        return readed;
    }


    public static void writeBytes(OutputStream outputStream, byte[] content, int offset, int size) throws IOException {
        outputStream.write(content, offset, size);
        outputStream.flush();
    }

    /*
     * byte[4] to int
     */
    public static int toInt(byte[] bytes) {

        int ret = 0;
        for (int i = 0; i < 4; i++) {
            byte b = bytes[i];
            ret |= ((int) b & 0xff) << 8 * (3 - i);
        }
        return ret;
    }

    /*
     * byte[4] to long,uint
     */
    public static long toUnsignedInt(byte[] bytes) {
        return ((long) toInt(bytes)) & 0xffffffffL;
    }

    public static String toString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }

    public static String toString(byte[] bytes) {
        String value = toString(bytes, "GBK");
        if (value.endsWith("\0")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static String toString(byte[] bytes, int offset, int len) {
        byte[] data = copyBytesByPos(bytes, offset, len);
        return toString(data);
    }

    public static byte[] copyBytesByPos(byte[] data, int pos, int size) {
        byte[] bytes = new byte[size];
        System.arraycopy(data, pos, bytes, 0, bytes.length);
        return bytes;
    }

    public static byte[] readBytes(ByteBuf byteBuf, int size) {
        byte[] t = new byte[size];
        System.arraycopy(byteBuf.array(), byteBuf.readerIndex(), t, 0, size);
        byteBuf.readerIndex(byteBuf.readerIndex() + size);
        return t;
    }

    public static String getString(ByteBuf data) throws UnsupportedEncodingException {
        return getString(data, "UTF-8");
    }

    public static String getString(ByteBuf data, String encoding) throws UnsupportedEncodingException {
        int length = data.readByte();
        String rs = null;
        if (length > 0) {
            rs = new String(data.array(), data.readerIndex(), length - 1, encoding);
        }
        data.readerIndex(data.readerIndex() + length);
        return rs;
    }

    public static int getByteOrShort(ByteBuf byteBuf) {
        int val = byteBuf.readByte() & 0xff;
        if (val == 0xff) {
            val = byteBuf.readShort();
        }
        return val;
    }

    public static int getByteOrInt(ByteBuf byteBuf) {
        int val = byteBuf.readByte() & 0xff;
        if (val == 0xff) {
            val = byteBuf.readInt();
        }
        return val;
    }
}
