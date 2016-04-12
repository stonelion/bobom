package bobom.decoder;

import bobom.packages.MysqlPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * length based package coder.
 */
public class MysqlPackageCoder extends ByteToMessageCodec<MysqlPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //3 bytes package length
        if (in.readableBytes() < 3) {
            return;
        }

        if (in.readableBytes() < in.markReaderIndex().readMediumLE()) {
            in.resetReaderIndex();
            return;
        }

        out.add(in);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, MysqlPacket msg, ByteBuf out) throws Exception {
        //write mysql package
        out.writeMediumLE(msg.payload.readableBytes());
        out.writeByte(msg.packetId);
        out.writeBytes(msg.payload);
        ctx.writeAndFlush(out);
    }
}
