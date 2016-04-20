package bobom.packages;

import bobom.defines.Capabilities;
import io.netty.buffer.ByteBuf;

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
public class HandshakeResponse41Packet extends MysqlPacket {
    int capabilityFlags;
    int maxPacketSize;
    byte characterSet;
    String username;
    String database;
    private byte[] authResponse;

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

        return this;
    }

    public byte[] getAuthResponse() {
        return authResponse;
    }

    public HandshakeResponse41Packet setAuthResponse(byte[] authResponse, byte[] seed) {
        this.authResponse = scramble411(authResponse, seed);
        return this;
    }

    public boolean isAuthResponseSame(byte[] nativeAuthResponse, byte[] encryptedAuthResponse, byte[] seed) {
        byte[] encrypted = scramble411(nativeAuthResponse, seed);
        return Arrays.equals(encrypted, encryptedAuthResponse);
    }

    /**
     * encrypt native password and check encrypted password send from client.
     */
    private byte[] scramble411(byte[] nativePass, byte[] seed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] pass1 = md.digest(nativePass);
            md.reset();
            byte[] pass2 = md.digest(pass1);
            md.reset();
            md.update(seed);
            byte[] pass3 = md.digest(pass2);
            for (int i = 0; i < pass3.length; i++) {
                pass3[i] = (byte) (pass3[i] ^ pass1[i]);
            }
            return pass3;
        } catch (NoSuchAlgorithmException e) {
            //Ignore
        }

        return EMPTY_BYTES;
    }

}
