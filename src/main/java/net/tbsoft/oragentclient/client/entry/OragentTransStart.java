package net.tbsoft.oragentclient.client.entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class OragentTransStart extends OragentDmlEntryImpl {

    @Override
    public void parse(byte[] data) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data, 0, data.length);
        setScn(byteBuf.readerIndex(21).readLong());
        setSourceTime(byteBuf.readerIndex(39).readUnsignedInt());

    }

    @Override
    public OpCode getEventType() {
        return OpCode.START;
    }

}
