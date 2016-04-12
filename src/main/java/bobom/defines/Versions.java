package bobom.defines;

public interface Versions {

    byte PROTOCOL_VERSION = 10;

    byte[] SERVER_VERSION = "5.6.0-UDAL-DBPROXY-1.2.0-dev-20160406-1039".getBytes();

    byte[] SCM_VERSION = "GIT: 953a5a76".getBytes();

    byte[] BUILD_TIMESTAMP = "20160406-1039".getBytes();
}
