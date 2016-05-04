package bobom.packages;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class MysqlPacket<T> {
    private static final byte[] EMPTY_BYTES = {};
    private static final long NULL_LENGTH = -1;
    private static final byte NULL = (byte) 0;

    public byte packetId;
    public ByteBuf payload;

    public abstract T read(ByteBuf buf);

    public abstract void write(ChannelHandlerContext ctx);

    void writeNull() {
        payload.writeByte(0);
    }

    void writeNull(int times) {
        for (int i = 0; i < times; i++) {
            payload.writeByte(0);
        }
    }

    void readPacketId(ByteBuf buf) {
        packetId = buf.readByte();
    }

    byte[] readThroughEOF(ByteBuf buf) {
        if (!buf.isReadable()) {
            return EMPTY_BYTES;
        }

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        buf.release();

        return bytes;
    }

    byte[] readEndWithNull(ByteBuf buf) {
        if (!buf.isReadable()) {
            return EMPTY_BYTES;
        }

        byte[] bytes = new byte[buf.bytesBefore(NULL)];
        buf.readBytes(bytes);

        if (buf.isReadable()) {
            //skip null.
            buf.skipBytes(1);
        }

        return bytes;
    }

    /**
     * read bytes based on prefix length
     */
    byte[] readWithLength(ByteBuf buf) {
        int length = (int) readLengthEncoded(buf);

        if (length == NULL_LENGTH) {
            return null;
        }

        if (length <= 0) {
            return EMPTY_BYTES;
        }

        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return bytes;
    }

    /**
     * read long based on lenenc length
     */
    long readLengthEncoded(ByteBuf buf) {
        int length = buf.readByte();
        switch (length) {
        case 0xfb:
            return NULL_LENGTH;
        case 0xfc:
            return buf.readShortLE();
        case 0xfd:
            return buf.readMediumLE();
        case 0xfe:
            return buf.readLongLE();
        default:
            return length;
        }
    }
}
