package bobom.packages;

import bobom.defines.Capabilities;
import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <pre>
 * 4              capability flags, CLIENT_PROTOCOL_41 always set
 * 4              max-packet size
 * 1              character set
 * string[23]     reserved (all [0])
 * string[NUL]    username
 *
 * if capabilities & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA {
 *      lenenc-int     length of auth-response
 *      string[n]      auth-response
 * } else if capabilities & CLIENT_SECURE_CONNECTION {
 *      1              length of auth-response
 *      string[n]      auth-response
 * } else {
 *      string[NUL]    auth-response
 * }
 *
 * if capabilities & CLIENT_CONNECT_WITH_DB {
 *      string[NUL]    database
 * }
 *
 * if capabilities & CLIENT_PLUGIN_AUTH {
 *      string[NUL]    auth plugin name
 * }
 *
 * if capabilities & CLIENT_CONNECT_ATTRS {
 *      lenenc-int     length of all key-values
 *      lenenc-str     key
 *      lenenc-str     value
 *      if-more data in 'length of all key-values', more keys and value pairs
 * }
 * </pre>
 */
public class HandshakeResponse41Packet extends MysqlPacket<HandshakeResponse41Packet> {
    int capabilityFlags;
    int maxPacketSize;
    byte characterSet;
    String username;
    String database;
    private byte[] authResponse;

    @Override
    public void write(ChannelHandlerContext ctx) {

    }

    public HandshakeResponse41Packet read(ByteBuf buf) {
        super.readPacketId(buf);
        capabilityFlags = buf.readIntLE();
        maxPacketSize = buf.readIntLE();
        characterSet = buf.readByte();
        buf.skipBytes(23);
        username = new String(readEndWithNull(buf));
        authResponse = readWithLength(buf);

        if ((capabilityFlags & Capabilities.CLIENT_CONNECT_WITH_DB.getValue()) != 0) {
            database = new String(readEndWithNull(buf));
        }

        buf.release();
        return this;
    }

    public byte[] getAuthResponse() {
        return authResponse;
    }

    public HandshakeResponse41Packet setAuthResponse(byte[] authResponse, byte[] seed) {
        this.authResponse = scramble411(authResponse, seed);
        return this;
    }

    public boolean isAuthResponseSame(byte[] nativeAuthResponse, byte[] encryptedAuthResponse, byte[] salt) {
        byte[] encrypted = scramble411(nativeAuthResponse, salt);
        return Arrays.equals(encrypted, encryptedAuthResponse);
    }

    /**
     * encrypt native password.
     */
    private byte[] scramble411(byte[] nativePass, byte[] salt) {
        MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] passwordHash = sha.digest(nativePass);
        return Bytes.concat(salt,sha.digest(passwordHash));
    }


    private static byte[] xor(byte[] a, byte[] b) {
        byte[] r = new byte[a.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) (a[i] ^ b[i]);
        }
        return r;
    }



}
