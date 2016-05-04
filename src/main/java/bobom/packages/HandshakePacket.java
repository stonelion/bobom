package bobom.packages;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * <pre>
 *      1              [0a] protocol version
 *      string[NUL]    server version
 *      4              connection id
 *      string[8]      auth-plugin-data-part-1
 *      1              [00] filler
 *      2              capability flags (lower 2 bytes)
 *      if more data in the packet:
 *          1              character set
 *          2              status flags
 *          2              capability flags (upper 2 bytes)
 *      if capabilities & CLIENT_PLUGIN_AUTH {
 *          1              length of auth-plugin-data
 *      } else {
 *          1              [00]
 *      }
 *      string[10]     reserved (all [00])
 *      if capabilities & CLIENT_SECURE_CONNECTION {
 *          string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
 *      if capabilities & CLIENT_PLUGIN_AUTH {
 *          string[NUL]    auth-plugin name
 *      }
 * </pre>
 */
public class HandshakePacket extends MysqlPacket<HandshakePacket> {

    public byte protocolVersion;
    public byte[] serverVersion;
    public int connectionId;
    public byte[] authP1;
    public int capabilityFlagsLower;
    public byte serverCharsetIndex;
    public int serverStatus;
    public int capabilityFlagsUpper;
    public byte[] authP2;

    @Override
    public HandshakePacket read(ByteBuf buf) {
        buf.release();
        return this;
    }

    public void write(ChannelHandlerContext ctx) {
        payload = ctx.alloc().buffer();

        payload.writeByte(protocolVersion);
        payload.writeBytes(serverVersion);
        writeNull();
        payload.writeIntLE(connectionId);
        payload.writeBytes(authP1);
        writeNull();
        payload.writeShortLE(capabilityFlagsLower);
        payload.writeByte(serverCharsetIndex);
        payload.writeShortLE(serverStatus);
        payload.writeShortLE(capabilityFlagsUpper);
        writeNull();
        writeNull(10);
        payload.writeBytes(authP2);
        writeNull();

        ctx.write(this);
    }
}
