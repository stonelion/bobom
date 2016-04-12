package bobom.defines;

/**
 * 处理能力标识定义 server capabilities
 * <pre>
 * @see https://dev.mysql.com/doc/internals/en/capability-flags.html#packet-Protocol::CapabilityFlags
 * </pre>
 */
public enum Capabilities {
    // new more secure passwords
    CLIENT_LONG_PASSWORD(0),

    // Send found rows instead of affected rows in EOF_Packet.
    // 返回找到（匹配）的行数，而不是改变了的行数。
    CLIENT_FOUND_ROWS(1),

    // Get all column flags
    // Longer flags in Protocol::ColumnDefinition320.
    CLIENT_LONG_FLAG(2),

    //Database (schema) name can be specified on connect in Handshake Response Packet.
    CLIENT_CONNECT_WITH_DB(3),

    // Do not permit database.table.column.
    // 不允许“数据库名.表名.列名”这样的语法。这是对于ODBC的设置。
    // 当使用这样的语法时解析器会产生一个错误，这对于一些ODBC的程序限制bug来说是有用的。
    CLIENT_NO_SCHEMA(4),

    // Compression protocol supported.
    // 使用压缩协议
    CLIENT_COMPRESS(5),

    //CLIENT_ODBC
    //Special handling of ODBC behavior.
    CLIENT_ODBC(6),

    //Can use LOAD DATA LOCAL.
    CLIENT_LOCAL_FILES(7),

    // Parser can ignore spaces before '('.
    // 允许在函数名后使用空格。所有函数名可以预留字。
    CLIENT_IGNORE_SPACE(8),

    // Supports the 4.1 protocol.
    CLIENT_PROTOCOL_41(9),

    // Supports interactive and noninteractive clients.
    // wait_timeout versus wait_interactive_timeout
    // 允许使用关闭连接之前的不活动交互超时的描述，而不是等待超时秒数。
    // 客户端的会话等待超时变量变为交互超时变量。
    CLIENT_INTERACTIVE(10),

    // Supports SSL
    // 使用SSL。这个设置不应该被应用程序设置，他应该是在客户端库内部是设置的。
    // 可以在调用mysql_real_connect()之前调用mysql_ssl_set()来代替设置。
    CLIENT_SSL(11),

    // IGNORE sigpipes
    // 阻止客户端库安装一个SIGPIPE信号处理器。
    // 这个可以用于当应用程序已经安装该处理器的时候避免与其发生冲突。
    CLIENT_IGNORE_SIGPIPE(12),

    // Can send status flags in EOF_Packet.
    CLIENT_TRANSACTIONS(13),

    //CLIENT_RESERVED
    //Unused
    CLIENT_RESERVED(14),

    // New 4.1 authentication
    // Supports Authentication::Native41.
    CLIENT_SECURE_CONNECTION(15),

    // CLIENT_MULTI_STATEMENTS
    //Can handle multiple statements per COM_QUERY and COM_STMT_PREPARE.
    // 通知服务器客户端可以发送多条语句（由分号分隔）。如果该标志为没有被设置，多条语句执行。
    CLIENT_MULTI_STATEMENTS(16),

    // Enable/disable multi-results
    // Can send multiple resultsets for COM_QUERY.
    // 通知服务器客户端可以处理由多语句或者存储过程执行生成的多结果集。
    // 当打开CLIENT_MULTI_STATEMENTS时，这个标志自动的被打开。
    CLIENT_MULTI_RESULTS(17),

    //Can send multiple resultsets for COM_STMT_EXECUTE.
    CLIENT_PS_MULTI_RESULTS(18),

    //Sends extra data in Initial Handshake Packet and supports the pluggable authentication protocol.
    CLIENT_PLUGIN_AUTH(19),

    //Permits connection attributes in Protocol::HandshakeResponse41.
    CLIENT_CONNECT_ATTRS(20),

    //Understands length-encoded integer for auth response data in Protocol::HandshakeResponse41.
    CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA(21),

    //Announces support for expired password extension.
    CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS(22),

    //Can set SERVER_SESSION_STATE_CHANGED in the Status Flags and send session-state change data after a OK packet.
    CLIENT_SESSION_TRACK(23),

    //Can send OK after a Text ResultSet.
    CLIENT_DEPRECATE_EOF(24);

    int value;

    Capabilities(int offset) {
        value = 1 << offset;
    }

    public static int getCapabilities(Capabilities... capabilities) {
        int flag = 0;
        for (Capabilities capability : capabilities) {
            flag |= capability.value;
        }
        return flag;
    }

    public int getValue() {
        return value;
    }
}
