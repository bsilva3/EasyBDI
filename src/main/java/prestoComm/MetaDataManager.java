package prestoComm;

import helper_classes.*;

import java.io.File;
import java.sql.*;
import java.util.*;

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
                +  "   UNIQUE("+ GLOBAL_COLUMN_DATA_NAME_FIELD +", " +GLOBAL_COLUMN_DATA_TABLE_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ GLOBAL_COLUMN_DATA_TABLE_FIELD +") REFERENCES "+GLOBAL_TABLE_DATA+"("+GLOBAL_TABLE_DATA_ID_FIELD+"));";

        String sql7 = "CREATE TABLE IF NOT EXISTS "+ CORRESPONDENCES_DATA +" (\n"
                + "    "+ CORRESPONDENCES_GLOBAL_COL_FIELD +" integer,\n"
                + "    "+ CORRESPONDENCES_LOCAL_COL_FIELD +" integer ,\n"
                + "    "+ CORRESPONDENCES_CONVERSION_FIELD +" text,\n"
                + "    "+ CORRESPONDENCES_TYPE_FIELD +" text, \n"
                + "    PRIMARY KEY("+ CORRESPONDENCES_GLOBAL_COL_FIELD+", "+CORRESPONDENCES_LOCAL_COL_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ CORRESPONDENCES_GLOBAL_COL_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ CORRESPONDENCES_LOCAL_COL_FIELD +") REFERENCES "+COLUMN_DATA+"("+ID_FIELD+")); ";

        String sql8 = "CREATE TABLE IF NOT EXISTS "+ CUBE_TABLE +" (\n"
                + "    "+ CUBE_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ CUBE_NAME +" text UNIQUE,\n"
                + "    "+ CUBE_TYPE_FIELD +" text ); ";

        String sql9 = "CREATE TABLE IF NOT EXISTS "+ MULTIDIM_TABLE +" (\n"
                + "    "+ MULTIDIM_TABLE_ID +" integer PRIMARY KEY,\n"
                + "    "+ MULTIDIM_TABLE_TYPE +" text ,\n"
                + "    "+ CUBE_ID_FIELD +" integer,\n"
                + "    "+ MULTIDIM_TABLE_ISFACTS +" boolean,\n"
                + "    "+ GLOBAL_TABLE_DATA_ID_FIELD +" integer, \n"
                + "    FOREIGN KEY ("+ CUBE_ID_FIELD +") REFERENCES "+CUBE_TABLE+"("+CUBE_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ GLOBAL_TABLE_DATA_ID_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+")); ";

        String sql10 = "CREATE TABLE IF NOT EXISTS "+ MULTIDIM_COLUMN +" (\n"
                + "    "+ CUBE_ID_FIELD +" integer ,\n"
                + "    "+ MULTIDIM_TABLE_ID +" integer ,\n"
                + "    "+ MULTIDIM_COLUMN_MEASURE +" boolean ,\n"
                + "    FOREIGN KEY ("+ CUBE_ID_FIELD +") REFERENCES "+CUBE_TABLE+"("+CUBE_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ MULTIDIM_TABLE_ID +") REFERENCES "+MULTIDIM_TABLE+"("+MULTIDIM_TABLE_ID+")); ";

        executeStatements(new String[] {sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9, sql10});
    }

    public void deleteTables(){
        String sql1 = "DROP TABLE "+ MULTIDIM_COLUMN +";";
        String sql2 = "DROP TABLE "+ MULTIDIM_TABLE +";";
        String sql3 = "DROP TABLE "+ CUBE_TABLE +";";
        String sql4 = "DROP TABLE "+ CORRESPONDENCES_DATA +";";
        String sql5 = "DROP TABLE "+ COLUMN_DATA +";";
        String sql6 = "DROP TABLE "+ TABLE_DATA +";";
        String sql7 = "DROP TABLE "+ DB_DATA +";";
        String sql8 = "DROP TABLE "+ DB_TYPE_DATA +";";
        String sql9 = "DROP TABLE "+ GLOBAL_COLUMN_DATA +";";
        String sql10 = "DROP TABLE "+ GLOBAL_TABLE_DATA +";";
        executeStatements(new String[] {sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9, sql10});
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
                    DBData db = dbData.get(i);
                    if (last_inserted_id == 0) //if db was inserted previously, id will be zero
                        last_inserted_id = getDBID(db.getDbName(), db.getUrl());
                    //System.out.println("KEY: "+last_inserted_id);
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
                    if (lastInsertedId == 0) //if db was inserted previously, id will be zero
                        lastInsertedId = getTableID(tables.get(i).getTableName(), tables.get(i).getSchemaName(), tables.get(i).getDB());
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
                        if (lastInsertedId == 0) //if db was inserted previously, id will be zero
                            lastInsertedId = getColumnID(columns.get(i).getName(), columnsInTables.get(j).getId());
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

    public DBData getDatabaseByID(int id){
        DBData db = null;
        try{
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery("SELECT * FROM " + DB_DATA +" WHERE "+ID_FIELD+" = "+id+";");
            ResultSetMetaData rsmd = rs.getMetaData();
            //
            if (rs.next()) {
                DBModel dbModel = this.getDBModelFromID(rs.getInt(DB_DATA_TYPE_ID_FIELD));
                db = new DBData(rs.getString(DB_DATA_SERVER_FIELD), dbModel, rs.getString(DB_DATA_NAME_FIELD),
                        rs.getString(DB_DATA_USER_FIELD), rs.getString(DB_DATA_PASS_FIELD), rs.getInt(ID_FIELD));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return db;
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

    public int getDBID(String dbName, String server){
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

    private int getColumnID(String columnName, int tableID){
        String query = "SELECT id FROM " + COLUMN_DATA + " WHERE "+ COLUMN_DATA_NAME_FIELD +" = '"+ columnName +"' AND " + COLUMN_DATA_TABLE_FIELD +" = " + tableID + "";
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

        String sql = "INSERT INTO "+ GLOBAL_TABLE_DATA + "("+GLOBAL_TABLE_DATA_NAME_FIELD+") VALUES(?)";
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
                pstmt.setString(1, table.getTableName());
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

        String sql = "INSERT INTO "+ GLOBAL_TABLE_DATA + "("+GLOBAL_TABLE_DATA_NAME_FIELD+") VALUES(?)";
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
                pstmt.setString(1, table.getTableName());
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

    public void insertGlobalSchemaData(List<GlobalTableData> globalTables){
        /*Map<Integer, List<GlobalColumnData>> columnsFromTables = insertGlobalTable(globalTables); //insert the global tables
        Map<Integer, List<Integer>> correspondences = insertGlobalColumn(columnsFromTables); //for each global table (identified by id) insert global columns
        insertGlobalToLocalCorrespondences(correspondences);//*/
        globalTables = insertGlobalTable(globalTables); //insert the global tables
        for (int i = 0; i < globalTables.size(); i++){
            GlobalTableData gt = insertGlobalColumn(globalTables.get(i)); //for each global table (identified by id) insert global columns
            globalTables.set(i, gt);//update global table with the
            insertGlobalToLocalCorrespondences(gt);//
        }
    }

    /**
     * Inserts in the sqlite database the global tables. Returns a map with the id of the inserted tables and their respective global columns
     * @param globalTables
     * @return
     */
    private List<GlobalTableData> insertGlobalTable(List<GlobalTableData> globalTables){
        String sql = "INSERT INTO "+ GLOBAL_TABLE_DATA + "("+GLOBAL_TABLE_DATA_NAME_FIELD+") VALUES(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < globalTables.size() ; i++){
                GlobalTableData globalTableData = globalTables.get(i);
                pstmt.setString(1, globalTableData.getTableName());
                //get id of inserted global table
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int lastInsertedId = rs.getInt(1);
                    globalTableData.setId(lastInsertedId);
                    globalTables.set(i, globalTableData);
                }
            }
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalTables;
    }

    /**
     * Inserts the global columns of global tables
     * @param gt
     * @return
     */
    private GlobalTableData insertGlobalColumn(GlobalTableData gt){
        String sql = "INSERT INTO "+ GLOBAL_COLUMN_DATA + "("+GLOBAL_TABLE_DATA_ID_FIELD+", "+GLOBAL_COLUMN_DATA_NAME_FIELD+
                ", "+GLOBAL_COLUMN_DATA_TYPE_FIELD+", "+GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Iterate through a list of previously inserted global tables and insert, for each table id, the list of its global columns
                List<GlobalColumnData> globalColumns = gt.getGlobalColumnDataList();
                //insert a global column
                for (int i = 0; i < globalColumns.size(); i++){
                    GlobalColumnData column = globalColumns.get(i);
                    pstmt.setInt(1, gt.getId());
                    pstmt.setString(2, column.getName());
                    pstmt.setString(3, column.getDataType());
                    pstmt.setBoolean(4, column.isPrimaryKey());
                    pstmt.executeUpdate();
                    //get id of inserted global column
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if(rs.next())
                    {
                        int lastInsertedId = rs.getInt(1);
                        column.setColumnID(lastInsertedId);
                        globalColumns.set(i, column);
                    }
                }
            gt.setGlobalColumnData(globalColumns);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return gt;
    }

    private void insertGlobalToLocalCorrespondences(GlobalTableData globalTable){
        String sql = "INSERT INTO "+ CORRESPONDENCES_DATA + "("+CORRESPONDENCES_GLOBAL_COL_FIELD+", "+
                CORRESPONDENCES_LOCAL_COL_FIELD+ ", " + CORRESPONDENCES_TYPE_FIELD+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Iterate through a list of previously inserted global tables and insert, for each table id, the list of its global columns
            List<GlobalColumnData> localCols = globalTable.getGlobalColumnDataList();
            for (GlobalColumnData globalColumn : localCols){
                for (ColumnData localColumn : globalColumn.getLocalColumns()) {
                    pstmt.setInt(1, globalColumn.getColumnID());
                    pstmt.setInt(2, localColumn.getColumnID());
                    pstmt.setString(3, localColumn.getMapping().toString());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Insert a star schema in the database. It is assumed that the global table objects have their database id's set up
     * @param starSchema
     */
    public void insertStarSchema(StarSchema starSchema){
        int cubeId = getOrcreateCube(starSchema.getSchemaName());
        insertFactsTable(cubeId, starSchema.getFactsTable());
        insertDimsTables(cubeId, starSchema.getDimsTables());
        //TODO: insert tables's columns in cube
    }

    private int insertFactsTable(int cubeID, FactsTable factsTable){
        int factsTableID = -1;
        String sql = "INSERT INTO "+ MULTIDIM_TABLE + "("+GLOBAL_TABLE_DATA_ID_FIELD+", "+MULTIDIM_TABLE_CUBE_ID+", "+
                MULTIDIM_TABLE_ISFACTS+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, factsTable.getGlobalTable().getId());
            pstmt.setInt(2, cubeID);
            pstmt.setBoolean(2, true);
            pstmt.executeUpdate();
            //get id of inserted global column
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next())
            {
                factsTableID = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return factsTableID;
    }

    private int insertDimsTables(int cubeID, List<GlobalTableData> dimsTables){
        int dimsTableID = -1;
        String sql = "INSERT INTO "+ MULTIDIM_TABLE + "("+GLOBAL_TABLE_DATA_ID_FIELD+", "+MULTIDIM_TABLE_CUBE_ID+", "+
                MULTIDIM_TABLE_ISFACTS+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (GlobalTableData gt : dimsTables){
                pstmt.setInt(1, gt.getId());
                pstmt.setInt(2, cubeID);
                pstmt.setBoolean(2, false);
                pstmt.executeUpdate();
                //get id of inserted global column
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    dimsTableID = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dimsTableID;
    }

    private int getOrcreateCube(String cubeName){
        int cubeId = getCubeID(cubeName);
        if (cubeId != -1){
            return cubeId;
        }
        //cube with that name does not exist,  create it
        String sql = "INSERT INTO "+ CUBE_TABLE + "("+CUBE_NAME+") VALUES(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cubeName);
            pstmt.executeUpdate();
            //get id of inserted cube
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next())
            {
                cubeId = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cubeId;
    }

    /**
     * given a Multidimensional cube id, get its id. returns -1 if does not exist
     * @param cubeName
     * @return
     */
    private int getCubeID(String cubeName){
        String sql = "SELECT * FROM "+ CUBE_TABLE + " WHERE "+CUBE_NAME+"= '"+cubeName+"'";
        int cubeId = -1;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                cubeId = rs.getInt(CUBE_NAME);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cubeId;
    }

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

    public List<TableData> getLocalTablesByID(Set<Integer> tableIDs){
        List<TableData> tables = new ArrayList<>();
        for (Integer id : tableIDs) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT "+ TABLE_DATA_NAME_FIELD +", "+TABLE_DATA_SCHEMA_NAME_FIELD+
                        ", "+TABLE_DATA_DB_ID_FIELD+" FROM " + TABLE_DATA + " where "+ID_FIELD+"="+id+";");
                // loop through the result set
                while (rs.next()) {
                    DBData db = this.getDatabaseByID(rs.getInt(TABLE_DATA_DB_ID_FIELD));
                    TableData table = new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), db, id);
                    table.setColumnsList(getColumnsInTable(table));
                    tables.add(table);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return tables;
    }

    public TableData getTableByID(int tableID){
        TableData table = null;
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT "+ TABLE_DATA_NAME_FIELD +", "+TABLE_DATA_SCHEMA_NAME_FIELD+
                        ", "+TABLE_DATA_DB_ID_FIELD+" FROM " + TABLE_DATA + " where "+ID_FIELD+"="+tableID+";");
                // loop through the result set
                while (rs.next()) {
                    DBData db = this.getDatabaseByID(rs.getInt(TABLE_DATA_DB_ID_FIELD));
                    table = new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), db, tableID);
                    table.setColumnsList(getColumnsInTable(table));
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        return table;
    }

    public List<ColumnData> getColumnsInTable(TableData table){
        List<ColumnData> cols = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT "+ COLUMN_DATA_NAME_FIELD+ ", " + COLUMN_DATA_TYPE_FIELD+", " + COLUMN_DATA_IS_PRIMARY_KEY_FIELD
                    +", "+ COLUMN_DATA_FOREIGN_KEY_FIELD +", "+ ID_FIELD +" FROM " + COLUMN_DATA + " where "+COLUMN_DATA_TABLE_FIELD+"="+table.getId()+";");
            // loop through the result set
            while (rs.next()) {
                ColumnData col = new ColumnData.Builder(rs.getString(COLUMN_DATA_NAME_FIELD), rs.getString(COLUMN_DATA_TYPE_FIELD), rs.getBoolean(COLUMN_DATA_IS_PRIMARY_KEY_FIELD))
                        .withForeignKey(rs.getString(COLUMN_DATA_FOREIGN_KEY_FIELD)).withID(rs.getInt(ID_FIELD)).withTable(table).build();
                cols.add(col);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cols;
    }

    /**
     * return a column
     * @param
     * @param schemaName
     * @param tableName
     * @param columnName
     * @return
     */
    public ColumnData getColumn(DBData db, String schemaName, String tableName, String columnName){
        ColumnData col = null;
        TableData t = getTable(db, schemaName, tableName);
        if (t == null)
            return null;
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ COLUMN_DATA_TYPE_FIELD+", " + COLUMN_DATA_IS_PRIMARY_KEY_FIELD
                    +", "+ COLUMN_DATA_FOREIGN_KEY_FIELD +", "+ ID_FIELD +" FROM " + COLUMN_DATA + " where "+COLUMN_DATA_TABLE_FIELD+"="+t.getId()
                    +" and "+COLUMN_DATA_NAME_FIELD + " = '"+columnName+"';";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                col = new ColumnData.Builder(columnName, rs.getString(COLUMN_DATA_TYPE_FIELD), rs.getBoolean(COLUMN_DATA_IS_PRIMARY_KEY_FIELD))
                        .withForeignKey(rs.getString(COLUMN_DATA_FOREIGN_KEY_FIELD)).withID(rs.getInt(ID_FIELD)).withTable(t).build();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return col;
    }

    public TableData getTable(DBData db, String schemaName, String tableName){
        TableData t = null;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT "+ ID_FIELD+" FROM " + TABLE_DATA + " where "+TABLE_DATA_DB_ID_FIELD+"="+db.getId()+" and "
                    +TABLE_DATA_NAME_FIELD +" ='" + tableName +"' and "+TABLE_DATA_SCHEMA_NAME_FIELD+" = '"+schemaName+"';");
            // loop through the result set
            while (rs.next()) {
                t = new TableData(tableName, schemaName,db, rs.getInt(ID_FIELD));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return t;
    }


}
