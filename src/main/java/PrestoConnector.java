import java.sql.*;

public class PrestoConnector {

    private String url = "jdbc:presto://127.0.0.1:8080";
    final String JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    private Connection conn;

    public Connection setConnection(String url){
        try {
            conn = DriverManager.getConnection(url, "test", null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void makeQuery(String query){
        Statement stmt = null;
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            //Open a connection
            conn = DriverManager.getConnection(url, "user", null);
            //Execute a query
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            ResultSetMetaData rsmd = res.getMetaData();
            //Extract data from result set
            while (res.next()) {
                //Retrieve by column name
                //String name = res.getString("name");
                //Display values
                //System.out.println("name : " + name);
                for (int i = 1; i < rsmd.getColumnCount(); i++){
                    String name = rsmd.getColumnName(i);
                    System.out.print(name+": " + res.getString(name)+", ");
                }
                System.out.println("");
            }
        res.close();
        stmt.close();
        conn.close();
            //Clean-up environment
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }


}
