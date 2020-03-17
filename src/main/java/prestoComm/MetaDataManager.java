package prestoComm;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import helper_classes.ColumnData;
import helper_classes.Correspondence;
import helper_classes.DBData;
import helper_classes.TableData;
import io.prestosql.jdbc.$internal.client.Column;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static prestoComm.Constants.*;

public class MetaDataManager {

    private final String URL = "jdbc:sqlite:" + SQLITE_DB_FOLDER + File.separator + SQLITE_DB;
    private Connection conn;

    public MetaDataManager(){
        this.conn = connect();
    }

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

        // -------------- Local schema
        String sql1 = "CREATE TABLE IF NOT EXISTS "+ DB_TYPE_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    "+ DB_TYPE_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ DB_TYPE_MODEL_FIELD +" text NOT NULL,\n"
                + "    "+ DB_TYPE_QUERY_FIELD +" text, \n"
                +  "    UNIQUE("+ DB_TYPE_NAME_FIELD+") ON CONFLICT IGNORE );";
        //create database table
        String sql2 = "CREATE TABLE IF NOT EXISTS "+ DB_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    "+ DB_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ DB_DATA_SERVER_FIELD +" text NOT NULL,\n"
                + "    "+ DB_DATA_USER_FIELD + " text,\n"
                + "    "+ DB_DATA_PASS_FIELD +" text,\n"
                + "    "+ DB_DATA_TYPE_ID_FIELD +" integer,\n"
                + "    UNIQUE("+ DB_DATA_NAME_FIELD +", "+ DB_DATA_SERVER_FIELD +") ON CONFLICT IGNORE,\n"
                + "    FOREIGN KEY ("+ DB_DATA_TYPE_ID_FIELD+ ") REFERENCES "+DB_TYPE_DATA+"(id));";
        //create table data table
        String sql3 = "CREATE TABLE IF NOT EXISTS "+ TABLE_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    "+ TABLE_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ TABLE_DATA_DB_ID_FIELD + " integer NOT NULL,\n"
                + "    " + TABLE_DATA_SCHEMA_NAME_FIELD +" text NOT NULL,\n"
                + "    UNIQUE("+ TABLE_DATA_DB_ID_FIELD +", " + TABLE_DATA_SCHEMA_NAME_FIELD + ", "+ TABLE_DATA_NAME_FIELD +") ON CONFLICT IGNORE,\n"
                + "    FOREIGN KEY ("+ TABLE_DATA_DB_ID_FIELD +") REFERENCES "+DB_DATA+"(id));";
        //create column table
        String sql4 = "CREATE TABLE IF NOT EXISTS "+ COLUMN_DATA +" (\n"
                + "    id integer PRIMARY KEY,\n"
                + "    " + COLUMN_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    " + COLUMN_DATA_TYPE_FIELD +" text NOT NULL,\n"
                + "    " + COLUMN_DATA_IS_PRIMARY_KEY_FIELD +" boolean NOT NULL,\n"
                + "    " + COLUMN_DATA_FOREIGN_KEY_FIELD + " text,\n"
                + "    " + COLUMN_DATA_TABLE_RELATION_FIELD + " text,\n"
                + "    " + COLUMN_DATA_TABLE_FIELD + " integer,\n"
                + "    UNIQUE("+ COLUMN_DATA_NAME_FIELD +", " + COLUMN_DATA_TABLE_FIELD +") ON CONFLICT IGNORE,\n"
                + "    FOREIGN KEY (" + COLUMN_DATA_TABLE_FIELD + ") REFERENCES "+DB_DATA+"(id));";


        // ------------ Global schema ----------------

        //TODO: cube table and foreign key to cube table in global table data (table would be unique on name AND cube)
        String sql5 = "CREATE TABLE IF NOT EXISTS "+ GLOBAL_TABLE_DATA +" (\n"
                + "    "+ GLOBAL_TABLE_DATA_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ GLOBAL_TABLE_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_TABLE_DATA_MULTI_TYPE_FIELD +" text,\n"
                + "    "+ DB_TYPE_QUERY_FIELD +" text, \n"
                +  "    UNIQUE("+ GLOBAL_TABLE_DATA_NAME_FIELD+") ON CONFLICT IGNORE );";

        String sql6 = "CREATE TABLE IF NOT EXISTS "+ GLOBAL_COLUMN_DATA +" (\n"
                + "    "+ GLOBAL_COLUMN_DATA_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ GLOBAL_COLUMN_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TYPE_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TABLE_FIELD +" integer NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD +" boolean, \n"
                +  "    UNIQUE("+ GLOBAL_COLUMN_DATA_NAME_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ GLOBAL_COLUMN_DATA_TABLE_FIELD +") REFERENCES "+GLOBAL_TABLE_DATA+"("+GLOBAL_TABLE_DATA_ID_FIELD+"));";

        String sql7 = "CREATE TABLE IF NOT EXISTS "+ CORRESPONDENCES_DATA +" (\n"
                + "    "+ CORRESPONDENCES_GLOBAL_COL_FIELD +" integer,\n"
                + "    "+ CORRESPONDENCES_LOCAL_COL_FIELD +" integer ,\n"
                + "    "+ CORRESPONDENCES_CONVERSION_FIELD +" text,\n"
                + "    "+ CORRESPONDENCES_TYPE_FIELD +" text, \n"
                + "    PRIMARY KEY("+ CORRESPONDENCES_GLOBAL_COL_FIELD+", "+CORRESPONDENCES_LOCAL_COL_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ CORRESPONDENCES_GLOBAL_COL_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ CORRESPONDENCES_LOCAL_COL_FIELD +") REFERENCES "+COLUMN_DATA+"("+ID_FIELD+")); ";
        
        String sql8 = "CREATE TABLE IF NOT EXISTS "+ CORRESPONDENCES_DATA +" (\n"
                + "    "+ CORRESPONDENCES_GLOBAL_COL_FIELD +" integer,\n"
                + "    "+ CORRESPONDENCES_LOCAL_COL_FIELD +" integer ,\n"
                + "    "+ CORRESPONDENCES_CONVERSION_FIELD +" text,\n"
                + "    "+ CORRESPONDENCES_TYPE_FIELD +" text, \n"
                + "    PRIMARY KEY("+ CORRESPONDENCES_GLOBAL_COL_FIELD+", "+CORRESPONDENCES_LOCAL_COL_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ CORRESPONDENCES_GLOBAL_COL_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ CORRESPONDENCES_LOCAL_COL_FIELD +") REFERENCES "+COLUMN_DATA+"("+ID_FIELD+")); ";

        executeStatements(new String[] {sql1, sql2, sql3, sql4, sql5, sql6, sql7});
    }

    public void deleteTables(){
        String sql1 = "DROP TABLE "+ CORRESPONDENCES_DATA +";";
        String sql2 = "DROP TABLE "+ COLUMN_DATA +";";
        String sql3 = "DROP TABLE "+ TABLE_DATA +";";
        String sql4 = "DROP TABLE "+ DB_DATA +";";
        String sql5 = "DROP TABLE "+ DB_TYPE_DATA +";";
        String sql6 = "DROP TABLE "+ GLOBAL_COLUMN_DATA +";";
        String sql7 = "DROP TABLE "+ GLOBAL_TABLE_DATA +";";
        executeStatements(new String[] {sql1, sql2, sql3, sql4, sql5, sql6, sql7});
    }

    private void executeStatements(String[] statements){
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements){
                stmt.execute(statement);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void fillDBDataTable(){
        String sql = "INSERT INTO "+ DB_TYPE_DATA + "("+ DB_TYPE_NAME_FIELD +","+ DB_TYPE_MODEL_FIELD +","+ DB_TYPE_QUERY_FIELD +") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
    public List<DBData> insertDBData(List<DBData> dbData){
        String sql = "INSERT INTO "+ DB_DATA + "("+DB_DATA_NAME_FIELD+", "+DB_DATA_SERVER_FIELD+", "+DB_DATA_USER_FIELD+", "+DB_DATA_PASS_FIELD+", "+DB_DATA_TYPE_ID_FIELD+") VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < dbData.size(); i++){
                int dbTypeID = getDBTypeIDInTable(dbData.get(i).getDbModel());
                if (dbTypeID == -1){
                    System.out.println("DB "+dbData.get(i).getDbModel() + " not supported. Won't be inserted in database");
                    continue;
                }
                pstmt.setString(1, dbData.get(i).getDbName());
                pstmt.setString(2, dbData.get(i).getUrl());
                pstmt.setString(3, dbData.get(i).getUser());
                pstmt.setString(4, dbData.get(i).getPass());
                pstmt.setInt(5, dbTypeID);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int last_inserted_id = rs.getInt(1);
                    //System.out.println("KEY: "+last_inserted_id);
                    DBData db = dbData.get(i);
                    db.setId(last_inserted_id);
                    dbData.set(i, db);//update the id of the db list in memory
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbData;
    }

    public List<TableData> insertTableData(List<TableData> tables){
        String sql = "INSERT INTO "+ TABLE_DATA + "("+TABLE_DATA_NAME_FIELD+", "+TABLE_DATA_SCHEMA_NAME_FIELD+", "+TABLE_DATA_DB_ID_FIELD+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < tables.size() ; i++){
                int dbID = tables.get(i).getDB().getId();
                if (dbID <= 0){
                    dbID = getDBID(tables.get(i).getDB().getDbName(), tables.get(i).getDB().getUrl());
                    if (dbID == -1){
                        System.out.println("DB "+tables.get(i).getDB().getDbName() + " in server " + tables.get(i).getDB().getUrl() +" not found");
                        continue;
                    }
                }
                pstmt.setString(1, tables.get(i).getTableName());
                pstmt.setString(2, tables.get(i).getSchemaName());
                pstmt.setInt(3, dbID);
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int lastInsertedId = rs.getInt(1);
                    //System.out.println("KEY: "+lastInsertedId);
                    TableData tb = tables.get(i);
                    tb.setId(lastInsertedId);
                    tables.set(i, tb);//update the id of the db list in memory
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tables;
    }

    public List<TableData> insertColumnData(List<TableData> columnsInTables){
        String sql = "INSERT INTO "+ COLUMN_DATA + "("+COLUMN_DATA_NAME_FIELD+", "+COLUMN_DATA_TYPE_FIELD+", "+COLUMN_DATA_IS_PRIMARY_KEY_FIELD+", "+COLUMN_DATA_FOREIGN_KEY_FIELD+", "
                +COLUMN_DATA_TABLE_RELATION_FIELD+", "+ COLUMN_DATA_TABLE_FIELD +") VALUES(?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int j = 0; j < columnsInTables.size(); j++) {
                List<ColumnData> columns = columnsInTables.get(j).getColumnsList();
                for (int i = 0; i < columns.size(); i++){
                    /*int tableID = column.getTable().getId();
                    if (tableID <= 0) {
                        tableID = getTableID(column.getTable().getTableName(), column.getTable().getSchemaName(), column.getTable().getDb());
                        if (tableID == -1) {
                            System.out.println("Table " + column.getTable().getTableName() + " in DB " + column.getTable().getDB().getDbName() + " not found");
                            continue;
                        }
                    }*/
                    pstmt.setString(1, columns.get(i).getName());
                    pstmt.setString(2, columns.get(i).getDataType());
                    pstmt.setBoolean(3, columns.get(i).isPrimaryKey());
                    pstmt.setString(4, columns.get(i).getForeignKey());
                    pstmt.setString(5, columns.get(i).getTableRelation());
                    pstmt.setInt(6, columnsInTables.get(j).getId());
                    pstmt.executeUpdate();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if(rs.next())
                    {
                        int lastInsertedId = rs.getInt(1);
                        //System.out.println("KEY: "+lastInsertedId);
                        ColumnData cd = columns.get(i);
                        cd.setColumnID(lastInsertedId);
                        columns.set(i, cd);//update the id of the db list in memory
                    }
                }
                TableData t = columnsInTables.get(j);
                t.setColumnsList(columns);
                columnsInTables.set(j, t);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return columnsInTables;
    }

    public ResultSet makeQuery(String query){
        ResultSet rs = null;
        try {
            Statement stmt  = conn.createStatement();
            rs    = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rs;
    }

    public void makeQueryAndPrint(String query){
        printQuery(makeQuery(query));
    }

    public void printQuery(ResultSet rs){
        if (rs == null){
            System.out.println("No results to print.");
            return;
        }
        try{
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            // loop through the result set
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    System.out.print( rsmd.getColumnName(i) + ": " + columnValue);
                }
                System.out.println("");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<DBData> getDatabases(){
        List<DBData> dbs = new ArrayList<>();
        try{
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery("SELECT * FROM " + DB_DATA +";");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            // loop through the result set
            while (rs.next()) {
                DBModel dbModel = this.getDBModelFromID(rs.getInt(DB_DATA_TYPE_ID_FIELD));
                dbs.add(new DBData(rs.getString(DB_DATA_SERVER_FIELD), dbModel, rs.getString(DB_DATA_NAME_FIELD),
                        rs.getString(DB_DATA_USER_FIELD), rs.getString(DB_DATA_PASS_FIELD), rs.getInt(ID_FIELD)));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dbs;
    }

    public List<TableData> getTablesInDB(DBData dbData){
        List<TableData> tables = new ArrayList<>();
        try{
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery("SELECT * FROM " + TABLE_DATA +" WHERE " + TABLE_DATA_DB_ID_FIELD + " = " + dbData.getId() + ";");
            // loop through the result set
            while (rs.next()) {
                tables.add(new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), dbData, rs.getInt(ID_FIELD)));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tables;
    }


    private int getDBTypeIDInTable(DBModel dbModel){
        String query = "SELECT id FROM " + DB_TYPE_DATA + " WHERE "+ DB_TYPE_NAME_FIELD +" = '"+dbModel.toString() +"'";
        Statement stmt  = null;
        int id = -1;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return id;
    }

    private int getDBID(String dbName, String server){
        String query = "SELECT id FROM " + DB_DATA + " WHERE "+ DB_DATA_NAME_FIELD +" = '"+ dbName +"' AND " + DB_DATA_SERVER_FIELD +" = '" + server + "'";
        Statement stmt  = null;
        int id = -1;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return id;
    }

    private int getTableID(String tableName, String schemaName, DBData db){
        int dbID = db.getId();
        if (dbID <= 0){
            dbID = getDBID(db.getDbName(), db.getUrl());
        }
        String query = "SELECT id FROM " + TABLE_DATA + " WHERE "+ TABLE_DATA_NAME_FIELD +" = '"+ tableName +"' AND " + TABLE_DATA_SCHEMA_NAME_FIELD +" = '" + schemaName + "'"
                + " AND "+ TABLE_DATA_DB_ID_FIELD +" = "+dbID+"'";
        Statement stmt  = null;
        int id = -1;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return id;
    }

    //returns enum of database type given an id
    private DBModel getDBModelFromID(int id){
        String query = "SELECT "+ DB_TYPE_NAME_FIELD +" FROM " + DB_TYPE_DATA + " WHERE "+ ID_FIELD +" = '"+ id +"'";
        Statement stmt  = null;
        String str = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                str = rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return DBModel.valueOf(str);
    }


    /**
     * Insert a list of tables that did not match into the global tables and global columns schemas. Since the tables had no match, they will each have one table each
     * @param nonMatchedTables
     * @return a map containing table id of a global table and the list of columns belonging to that table
     */
    public Map<Integer, List<ColumnData>> insertGLobalNonMatchedTableData(List<TableData> nonMatchedTables){
        //store the table id of a global table and the list of columns belonging to that table
        Map<Integer, List<ColumnData>> globalTablesForColumns = new HashMap<>();

        String sql = "INSERT INTO "+ GLOBAL_TABLE_DATA + "("+GLOBAL_TABLE_DATA_NAME_FIELD+", "+GLOBAL_TABLE_DATA_MULTI_TYPE_FIELD+", "+GLOBAL_TABLE_DATA_CUBE_FIELD+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < nonMatchedTables.size() ; i++){
                TableData table = nonMatchedTables.get(i);
                int dbID = table.getDB().getId();
                if (dbID <= 0){
                    dbID = getDBID(table.getDB().getDbName(), table.getDB().getUrl());
                    if (dbID == -1){
                        System.out.println("DB "+table.getDB().getDbName() + " in server " + table.getDB().getUrl() +" not found");
                        continue;
                    }
                }
                //TODO: complete insertion with multidimensional data (cube and multidimensional type)
                pstmt.setString(1, table.getTableName());
                pstmt.setString(2, "");
                pstmt.setInt(3, 0);
                pstmt.executeUpdate();

                //get if of inserted global table
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int lastInsertedId = rs.getInt(1);
                    globalTablesForColumns.put(lastInsertedId, table.getColumnsList());
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalTablesForColumns;
    }

    /**
     * Insert a list of tables that matched into the global tables and global columns schemas. Since the tables had no match, they will each have one table each
     * @param nonMatchedTables
     * @return a map containing table id of a global table and the list of columns belonging to that table
     */
    public Map<Integer, List<ColumnData>> insertGLobalMatchedTableData(List<TableData> nonMatchedTables){
        //store the table id of a global table and the list of columns belonging to that table
        Map<Integer, List<ColumnData>> globalTablesForColumns = new HashMap<>();

        String sql = "INSERT INTO "+ GLOBAL_TABLE_DATA + "("+GLOBAL_TABLE_DATA_NAME_FIELD+", "+GLOBAL_TABLE_DATA_MULTI_TYPE_FIELD+", "+GLOBAL_TABLE_DATA_CUBE_FIELD+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < nonMatchedTables.size() ; i++){
                TableData table = nonMatchedTables.get(i);
                int dbID = table.getDB().getId();
                if (dbID <= 0){
                    dbID = getDBID(table.getDB().getDbName(), table.getDB().getUrl());
                    if (dbID == -1){
                        System.out.println("DB "+table.getDB().getDbName() + " in server " + table.getDB().getUrl() +" not found");
                        continue;
                    }
                }
                //TODO: complete insertion with multidimensional data (cube and multidimensional type)
                pstmt.setString(1, table.getTableName());
                pstmt.setString(2, "");
                pstmt.setInt(3, 0);
                pstmt.executeUpdate();

                //get if of inserted global table
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int lastInsertedId = rs.getInt(1);
                    globalTablesForColumns.put(lastInsertedId, table.getColumnsList());
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalTablesForColumns;
    }

    /**
     * Insert Columns of global tables in the global columns table. Also adds the correspondences between the global and the local table columns in the
     * correspondences table.
     * @param columnsInTables
     * @return
     */
    /*public List<TableData> insertGlobalColumnData(Map<Integer, List<ColumnData>> columnsInTables){
        String sql = "INSERT INTO "+ GLOBAL_COLUMN_DATA + "("+GLOBAL_COLUMN_DATA_NAME_FIELD+", "+GLOBAL_COLUMN_DATA_TYPE_FIELD+", "+GLOBAL_COLUMN_DATA_TABLE_FIELD+", "
                +GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Map.Entry<Integer, List<ColumnData>> entry : columnsInTables.entrySet()) {
                int globalTableID = entry.getKey();
                List<ColumnData> columns = entry.getValue();
                for (int i = 0; i < columns.size(); i++){
                    pstmt.setString(1, columns.get(i).getName());
                    pstmt.setString(2, columns.get(i).getDataType());
                    pstmt.setInt(3, globalTableID);
                    pstmt.setBoolean(4, columns.get(i).isPrimaryKey());
                    pstmt.executeUpdate();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if(rs.next()) {
                        int lastInsertedId = rs.getInt(1);

                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return columnsInTables;
    }*/

    /**
     * Insert Columns of global tables in the global columns table. Also adds the correspondences between the global and the local table columns in the
     * correspondences table.
     * @param globalToLocalCorrs
     * @return
     */
    public void insertCorrespondencesData(List<Correspondence> globalToLocalCorrs){
        String sql = "INSERT INTO "+ CORRESPONDENCES_DATA + "("+CORRESPONDENCES_GLOBAL_COL_FIELD+", "+CORRESPONDENCES_LOCAL_COL_FIELD+", "+CORRESPONDENCES_CONVERSION_FIELD+", "
                +CORRESPONDENCES_TYPE_FIELD+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < globalToLocalCorrs.size(); i++) {

                pstmt.setInt(1, globalToLocalCorrs.get(i).getGlobalCol().getColumnID());
                pstmt.setInt(2, globalToLocalCorrs.get(i).getLocalCol().getColumnID());
                pstmt.setString(3, globalToLocalCorrs.get(i).getConversion());
                pstmt.setString(4, "");
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


}
