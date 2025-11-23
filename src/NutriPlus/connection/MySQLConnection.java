package NutriPlus.connection;

import java.sql.*;

public class MySQLConnection {
    private static Connection singleton;

    public static Connection get() throws SQLException {
        if (singleton != null && !singleton.isClosed())
            return singleton;

        String host = getenv("NP_DB_HOST", "127.0.0.1");
        String port = getenv("NP_DB_PORT", "3306");
        String db = getenv("NP_DB_NAME", "nutriplus");
        String user = getenv("NP_DB_USER", "nutriplus");
        String pass = getenv("NP_DB_PASSWORD", "nutri123");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
        }
        singleton = DriverManager.getConnection(url, user, pass);
        singleton.setAutoCommit(true);
        return singleton;
    }

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank())
            return def;
        return v;
    }
}
