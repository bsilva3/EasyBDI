package prestoComm;

import java.sql.*;

import static prestoComm.Constants.*;

public class MetaDataManager {

    private final String URL = "jdbc:sqlite:C:/sqlite/db/" + SQLITE_DB;
    private Connection conn;


    public void connect() {
        try {
            conn = DriverManager.getConnection(URL);
            if (conn != null) {
                DatabaseMetaData meta = null;
                System.out.println("Connected to SQLITE DB");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTables(){
        // SQLite connection string

        // create the 3 tables if they dont exist already
        String sql1 = "CREATE TABLE IF NOT EXISTS "+ DB_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    db_type text NOT NULL\n"
                + "    server text NOT NULL\n"
                + "    user text\n"
                + "    pass text\n"
                + ");";

        String sql2 = "CREATE TABLE IF NOT EXISTS "+ TABLE_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    db_type text NOT NULL\n"
                + "    server text NOT NULL\n"
                + "    user text\n"
                + "    pass text\n"
                + ");";

        String sql3 = "CREATE TABLE IF NOT EXISTS "+ COLUMN_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    db_type text NOT NULL\n"
                + "    server text NOT NULL\n"
                + "    user text\n"
                + "    pass text\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
