package prestoComm;

import helper_classes.ColumnInfo;
import helper_classes.SchemaInfo;
import helper_classes.TableInfo;

import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * Connects to postgres to save data about tables
 */

public class TableDataManager {

    private String url = "jdbc:postgresql://localhost/";
    final String JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    private Properties props;
    private Connection conn;
    private DBConnector prestoConnector;
    private String user;
    private String pass;

    public TableDataManager(String user, String pass){
        props = new Properties();
        this.user = user;
        this.pass = pass;
        props.setProperty("user", user);
        props.setProperty("password", pass);
    }

    private void connectPostgres() throws SQLException {
        conn = DriverManager.getConnection(url, props);
    }

    public void storeTableData(String tableData){

    }

    //for each DB schema, a new schema in psql is created to store info of each in that schema
    public void createEntriesForDB(String DBName, List<SchemaInfo> schemas){
        try {
            connectPostgres();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Statement statement = null;
        try {
            // first create database if does not exist

            PreparedStatement ps = conn.prepareStatement("CREATE DATABASE "+ DBName);
            ps.executeUpdate();

            //switch do DB??
            //ps = conn.prepareStatement("CREATE DATABASE "+ DBName);
            //ps.executeUpdate();

            for (SchemaInfo schema : schemas){

                //create schema
                ps = conn.prepareStatement("CREATE SCHEMA IF NOT EXISTS "+schema.getSchemaName()+";");
                ps.executeUpdate();

                //create tables
                for (TableInfo table : schema.getTables()) {
                    //each row of the table contains info about a column of a table (name, data type, is primary key?)...
                    ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + table +" (name varchar(225) NOT NULL PRIMARY KEY, dataType varchar(225) NOT NULL , isPrimaryKey boolean, foreignKey varchar(255));");
                    ps.executeUpdate();

                    //insert on the table the data regarding the table info
                    for (ColumnInfo column : table.getColumns() ){
                        ps = conn.prepareStatement("INSERT INTO "+ table +"(name, dataType, isPrimaryKey, foreignKey) VALUES ("+ column.getName()+", "+column.getDataType()+", false, NULL);");
                        ps.executeUpdate();
                    }
                }
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
