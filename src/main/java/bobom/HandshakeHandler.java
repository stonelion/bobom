package bobom;

import bobom.defines.Capabilities;
import bobom.defines.Versions;
import bobom.packages.HandshakePacket;
import bobom.packages.HandshakeResponse41;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Random;

import static bobom.defines.Capabilities.*;

class HandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final int capabilityFlagsLower = Capabilities
            .getCapabilities(CLIENT_LONG_PASSWORD, CLIENT_FOUND_ROWS,
                             CLIENT_LONG_FLAG, CLIENT_CONNECT_WITH_DB,
                             CLIENT_ODBC, CLIENT_IGNORE_SPACE,
                             CLIENT_PROTOCOL_41, CLIENT_INTERACTIVE,
                             CLIENT_IGNORE_SIGPIPE, CLIENT_TRANSACTIONS,
                             CLIENT_SECURE_CONNECTION);

    private static final byte[] seedChar = {
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w', 'e', 'r', 't',
            'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z',
            'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O',
            'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B',
            'N', 'M'
    };

    private static ConnectionIdGenerator idGenerator = new ConnectionIdGenerator();

    private final Random ran = new Random();

    /**
     * attribute of the client connection.
     */
    private byte[] seed;
    private int connectionId;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] rand1 = randomBytes(8);
        byte[] rand2 = randomBytes(12);

        // 保存认证数据
        seed = Bytes.concat(rand1, rand2);
        connectionId = (int) idGenerator.getId();

        // 发送握手数据包
        HandshakePacket hs = new HandshakePacket();
        hs.packetId = 0;
        hs.protocolVersion = Versions.PROTOCOL_VERSION;
        hs.serverVersion = Versions.SERVER_VERSION;
        hs.connectionId = connectionId;
        hs.authP1 = rand1;
        hs.capabilityFlagsLower = capabilityFlagsLower;
        hs.serverCharsetIndex = (byte) (33 & 0xff);
        hs.serverStatus = 2;
        hs.capabilityFlagsUpper = 0;
        hs.authP2 = rand2;
        hs.write(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf message = (ByteBuf) msg;
        HandshakeResponse41 response41 = new HandshakeResponse41();
        response41.read(message);

        if (response41.isAuthResponseSame("admin".getBytes(), response41.getAuthResponse(), seed)) {
            System.out.println("scuress");
        }

    }

    private byte[] randomBytes(int size) {
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = seedChar[(int) (ran.nextDouble() * seedChar.length)];
        }
        return result;
    }

    private static class ConnectionIdGenerator {
        private static final long MAX_VALUE = 0xffffffffL;
        private final Object lock = new Object();
        private long acceptId = 0L;

        private long getId() {
            synchronized (lock) {
                if (acceptId >= MAX_VALUE) {
                    acceptId = 0L;
                }
                return ++acceptId;
            }
        }
    }
}
