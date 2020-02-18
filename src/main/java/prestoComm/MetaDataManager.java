package prestoComm;

import helper_classes.DBData;

import java.awt.*;
import java.sql.*;
import java.util.List;

import static prestoComm.Constants.*;

public class MetaDataManager {

    private final String URL = "jdbc:sqlite:C:/sqlite/db/" + SQLITE_DB;

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            if (conn != null) {
                DatabaseMetaData meta = null;
                System.out.println("Connected to SQLITE DB");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void createTablesAndFillDBModelData(){
        createTables();
        fillDBDataTable();
    }

    public void createTables(){
        // SQLite connection string

        // create the tables if they dont exist already
        //create database types table
        String sql1 = "CREATE TABLE IF NOT EXISTS "+ DB_TYPE_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    db_name text NOT NULL UNIQUE,\n"
                + "    db_model_type text NOT NULL\n"
                + "    catalog_access_query text\n"
                + ");";
        //create database table
        String sql2 = "CREATE TABLE IF NOT EXISTS "+ DB_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    server_url text NOT NULL\n"
                + "    user text\n"
                + "    pass text\n"
                + "    FOREIGN KEY (db_type_id) REFERENCES "+DB_TYPE_DATA+"(id));"
                + ");";
        //create table
        String sql3 = "CREATE TABLE IF NOT EXISTS "+ TABLE_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    FOREIGN KEY (db_id) REFERENCES "+DB_DATA+"(id));"
                + ");";
        //create column table
        String sql4 = "CREATE TABLE IF NOT EXISTS "+ COLUMN_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    name text NOT NULL,\n"
                + "    data_type text NOT NULL\n"
                + "    is_primary_key boolean NOT NULL\n"
                + "    foreign_key text\n"
                + "    table_relation text\n"
                + "    FOREIGN KEY (table_id) REFERENCES "+DB_DATA+"(id));"
                + ");";

        try (Connection conn = this.connect();
            Statement stmt = conn.createStatement()) {
            //
            stmt.execute(sql1);
            stmt.execute(sql2);
            stmt.execute(sql3);
            stmt.execute(sql4);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void fillDBDataTable(){
        String sql = "INSERT INTO "+ DB_TYPE_DATA + "(db_name,db_model_type,catalog_access_query) VALUES(?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (DBModel dbModel : DBModel.values()){
                pstmt.setString(1, dbModel.toString());
                pstmt.setString(2, dbModel.getBDDataModel());
                pstmt.setString(3, dbModel.getMetaDataQuery());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Given a list of databases, fill that info in the appropriate sqlite table.
     * This table contains info regarding the databases, such as url, auth info, and database type (mongo, mysql...)
     */
    public void insertDBData(List<DBData> dbData){
        String sql = "INSERT INTO "+ DB_DATA + "(name, server_url, user, pass, db_type_id) VALUES(?,?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (DBData db : dbData){
                int dbTypeID = getDBTypeIDInTable(db.getDbModel(), conn);
                if (dbTypeID == -1){
                    System.out.println("DB "+db.getDbModel() + " not supported. Won't be inserted in database");
                    continue;
                }
                pstmt.setString(1, db.getDbName());
                pstmt.setString(2, db.getUrl());
                pstmt.setString(3, db.getUser());
                pstmt.setString(4, db.getPass());
                pstmt.setInt(5, getDBTypeIDInTable(db.getDbModel(), conn));
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int getDBTypeIDInTable(DBModel dbModel, Connection conn){
        String query = "SELECT id FROM " + DB_TYPE_DATA + " WHERE db_name = "+dbModel.toString();
        Statement stmt  = null;
        int id = -1;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

}
