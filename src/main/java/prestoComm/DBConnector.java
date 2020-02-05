package prestoComm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    private String url;
    private String jdbcDriver;
    private Connection conn;
    private String username;
    private String pass;

    public DBConnector (String url, String jdbcDriver){
        this.url = url;
        this.jdbcDriver = jdbcDriver;
    }

    public DBConnector (String url, String jdbcDriver, String username, String pass){
        this.url = url;
        this.jdbcDriver = jdbcDriver;
        this.username = username;
        this.pass = pass;
    }

    public Connection setConnection(){
        String user;
        String password;
        if (username != null) {
            user = username;
            password = pass;
        }
        else {
            user = "";
            password = null;
        }
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void closeConn(){
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
