package main_app.metadata_storage;

import helper_classes.*;
import main_app.query_ui.FilterNode;

import java.io.*;
import java.sql.*;
import java.util.*;

import static helper_classes.utils_other.Constants.*;

public class MetaDataManager {

    private static final String URL = "jdbc:sqlite:" + PROJECT_DIR;
    private String DB_FILE_URL = URL;//will have the database name to connect to
    private Connection conn;
    private String databaseName;

    public MetaDataManager(String databaseName){
        this.databaseName = databaseName;
        DB_FILE_URL = DB_FILE_URL + databaseName;
        this.conn = connect();
    }

    public Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_FILE_URL);
            if (conn != null) {
                DatabaseMetaData meta = null;
                System.out.println("Connected to SQLITE DB");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static File[] listAllProjectFiles(){
        File folder = new File(PROJECT_DIR);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null || listOfFiles.length == 0){
            return new File[]{};
        }
        return listOfFiles;
    }

    public static boolean deleteProject(String projectName){
        File folder = new File(PROJECT_DIR);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().equals(projectName)) {
                return listOfFiles[i].delete();
            }
        }
        return false;
    }

    public static String[] listAllProjectNames(){
        File[] listOfFiles = listAllProjectFiles();
        String[] dbNames = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName().split("\\.")[0];
                dbNames[i] = fileName;
            }
        }
        return dbNames;
    }

    public static Set<SimpleDBData> getAllDatabasesInProjects(){
        Set<SimpleDBData> dbs = new HashSet<>();
        String[] projects = listAllProjectNames();
        for (String project : projects){
            MetaDataManager m = new MetaDataManager(project);
            List<DBData> dbsInProjects = m.getDatabases();
            for (DBData db : dbsInProjects){
                dbs.add(db.convertToSimpleDB());
            }
            m.close();
        }
        return dbs;
    }

    /**
     * Check if db file exists
     * @param dbName
     * @return
     */
    public static boolean dbExists(String dbName){
        File[] listOfFiles = listAllProjectFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName().split("\\.")[0];
                if (fileName.equals(dbName))
                    return true;
            }
        }
        return false;
    }

    public boolean removeDB(String dbName){
        File[] listOfFiles = listAllProjectFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName().split("\\.")[0];
                if (fileName.equals(dbName))
                    return listOfFiles[i].delete();
            }
        }
        return false;
    }

    public void createTablesAndFillDBModelData(){
        createTables();
        fillDBDataTable();
    }

    public void close(){
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) { e.printStackTrace();}
        }
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
                + "    "+ TABLE_CODE_FIELD +" text,\n"
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
                + "    " + COLUMN_DATA_TABLE_FIELD + " integer,\n"
                + "    UNIQUE("+ COLUMN_DATA_NAME_FIELD +", " + COLUMN_DATA_TABLE_FIELD +") ON CONFLICT IGNORE,\n"
                + "    FOREIGN KEY (" + COLUMN_DATA_TABLE_FIELD + ") REFERENCES "+DB_DATA+"(id));";


        // ------------ Global schema ----------------
        String sql5 = "CREATE TABLE IF NOT EXISTS "+ GLOBAL_TABLE_DATA +" (\n"
                + "    "+ GLOBAL_TABLE_DATA_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ GLOBAL_TABLE_DATA_NAME_FIELD +" text NOT NULL,\n"
               // + "    "+ GLOBAL_TABLE_DATA_MULTI_TYPE_FIELD +" text,\n"
                + "    "+ DB_TYPE_QUERY_FIELD +" text, \n"
                +  "    UNIQUE("+ GLOBAL_TABLE_DATA_NAME_FIELD+") ON CONFLICT IGNORE );";

        String sql6 = "CREATE TABLE IF NOT EXISTS "+ GLOBAL_COLUMN_DATA +" (\n"
                + "    "+ GLOBAL_COLUMN_DATA_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ GLOBAL_COLUMN_DATA_NAME_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TYPE_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TYPEOG_FIELD +" text NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TYPE_CHANGE_FIELD +" boolean NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_TABLE_FIELD +" integer NOT NULL,\n"
                + "    "+ GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD +" boolean, \n"
                + "    "+ GLOBAL_COLUMN_DATA_FOREIGN_KEY_FIELD +" text, \n"
                +  "   UNIQUE("+ GLOBAL_COLUMN_DATA_NAME_FIELD +", " +GLOBAL_COLUMN_DATA_TABLE_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ GLOBAL_COLUMN_DATA_TABLE_FIELD +") REFERENCES "+GLOBAL_TABLE_DATA+"("+GLOBAL_TABLE_DATA_ID_FIELD+"));";

        String sql7 = "CREATE TABLE IF NOT EXISTS "+ CORRESPONDENCES_DATA +" (\n"
                + "    "+ CORRESPONDENCES_GLOBAL_COL_FIELD +" integer,\n"
                + "    "+ CORRESPONDENCES_LOCAL_COL_FIELD +" integer ,\n"
                //+ "    "+ CORRESPONDENCES_CONVERSION_FIELD +" text,\n"
                + "    "+ CORRESPONDENCES_TYPE_FIELD +" integer, \n"
                + "    PRIMARY KEY("+ CORRESPONDENCES_GLOBAL_COL_FIELD+", "+CORRESPONDENCES_LOCAL_COL_FIELD+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ CORRESPONDENCES_GLOBAL_COL_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ CORRESPONDENCES_LOCAL_COL_FIELD +") REFERENCES "+COLUMN_DATA+"("+ID_FIELD+")); ";

        String sql8 = "CREATE TABLE IF NOT EXISTS "+ CUBE_TABLE +" (\n"
                + "    "+ CUBE_ID_FIELD +" integer PRIMARY KEY,\n"
                + "    "+ CUBE_NAME +" text UNIQUE);";
               // + "    "+ CUBE_TYPE_FIELD +" text ); ";

        String sql9 = "CREATE TABLE IF NOT EXISTS "+ MULTIDIM_TABLE +" (\n"
                + "    "+ MULTIDIM_TABLE_ID +" integer PRIMARY KEY,\n"
                + "    "+ MULTIDIM_TABLE_TYPE +" text ,\n"
                + "    "+ CUBE_ID_FIELD +" integer,\n"
                + "    "+ MULTIDIM_TABLE_ISFACTS +" boolean,\n"
                + "    "+ GLOBAL_TABLE_DATA_ID_FIELD +" integer, \n"
                + "    FOREIGN KEY ("+ CUBE_ID_FIELD +") REFERENCES "+CUBE_TABLE+"("+CUBE_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ GLOBAL_TABLE_DATA_ID_FIELD +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+")); ";

        String sql10 = "CREATE TABLE IF NOT EXISTS "+ MULTIDIM_COLUMN +" (\n"
                + "    "+ CUBE_ID_FIELD +" integer,\n"
                + "    "+ MULTIDIM_TABLE_ID +" integer ,\n"
                + "    "+ MULTIDIM_COL_GLOBAL_COLUMN_ID +" integer ,\n"
                + "    "+ MULTIDIM_COLUMN_MEASURE +" boolean ,\n"
                + "    "+ "PRIMARY KEY("+CUBE_ID_FIELD+", "+MULTIDIM_TABLE_ID +", "+MULTIDIM_COL_GLOBAL_COLUMN_ID+") ON CONFLICT ABORT,\n"
                + "    FOREIGN KEY ("+ CUBE_ID_FIELD +") REFERENCES "+CUBE_TABLE+"("+CUBE_ID_FIELD+"), "
                + "    FOREIGN KEY ("+ MULTIDIM_TABLE_ID +") REFERENCES "+MULTIDIM_TABLE+"("+MULTIDIM_TABLE_ID+"), "
                + "    FOREIGN KEY ("+ MULTIDIM_COL_GLOBAL_COLUMN_ID +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_ID_FIELD+")); ";


        //tables to store queries
        String sql11 = "CREATE TABLE IF NOT EXISTS "+ QUERY_SAVE +" (\n"
                + "    "+ QUERY_ID +" integer PRIMARY KEY,\n"
                + "    "+ QUERY_NAME +" text NOT NULL ,\n"
                + "    "+ QUERY_CUBE_ID +" integer ,\n"
                + "    UNIQUE("+ QUERY_NAME +", "+QUERY_CUBE_ID+") ON CONFLICT IGNORE ,\n"
                + "    FOREIGN KEY ("+ QUERY_CUBE_ID +") REFERENCES "+CUBE_TABLE+"("+CUBE_ID_FIELD+")); ";

        String sql12 = "CREATE TABLE IF NOT EXISTS "+ QUERY_ROW +" (\n"
                + "    "+ QUERY_ROW_ID +" integer,\n"
                + "    "+ QUERY_ID +" integer,\n"
                + "    "+ QUERY_GLOBAL_TABLE_ID +" integer ,\n"
                + "    "+ QUERY_GLOBAL_ROW_OBJ +" blob ,\n"
                + "    "+ "PRIMARY KEY("+ QUERY_ID+", "+QUERY_ROW_ID+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ QUERY_GLOBAL_TABLE_ID +") REFERENCES "+GLOBAL_TABLE_DATA+"("+GLOBAL_TABLE_DATA_ID_FIELD+"), "
                //+ "    FOREIGN KEY ("+ QUERY_GLOBAL_COLUMN_ID +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_TABLE_FIELD+"), "
                + "    FOREIGN KEY ("+ QUERY_ID +") REFERENCES "+QUERY_SAVE+"("+QUERY_ID+")); ";

        String sql13 = "CREATE TABLE IF NOT EXISTS "+ QUERY_COLS +" (\n"
                + "    "+ QUERY_COLS_ID +" integer,\n"
                + "    "+ QUERY_ID +" integer,\n"
                + "    "+ QUERY_GLOBAL_TABLE_ID +" integer ,\n"
                + "    "+ QUERY_GLOBAL_COLUMN_OBJ +" blob ,\n"
                + "    "+ "PRIMARY KEY("+ QUERY_ID+", "+QUERY_COLS_ID+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ QUERY_GLOBAL_TABLE_ID +") REFERENCES "+GLOBAL_TABLE_DATA+"("+GLOBAL_TABLE_DATA_ID_FIELD+"), "
                //+ "    FOREIGN KEY ("+ QUERY_GLOBAL_COLUMN_ID +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_TABLE_FIELD+"), "
                + "    FOREIGN KEY ("+ QUERY_ID +") REFERENCES "+QUERY_SAVE+"("+QUERY_ID+")); ";

        String sql14 = "CREATE TABLE IF NOT EXISTS "+ QUERY_MEASURES +" (\n"
                + "    "+ QUERY_MEASURES_ID +" integer,\n"
                + "    "+ QUERY_ID +" integer,\n"
                + "    "+ QUERY_MEASURE_OBJ +" blob ,\n"
                + "    "+ "PRIMARY KEY("+ QUERY_ID+", "+ QUERY_MEASURES_ID+") ON CONFLICT IGNORE, \n"
                //+ "    FOREIGN KEY ("+ QUERY_GLOBAL_COLUMN_ID +") REFERENCES "+GLOBAL_COLUMN_DATA+"("+GLOBAL_COLUMN_DATA_TABLE_FIELD+"), "
                + "    FOREIGN KEY ("+ QUERY_ID +") REFERENCES "+QUERY_SAVE+"("+QUERY_ID+")); ";

        String sql15 = "CREATE TABLE IF NOT EXISTS "+ QUERY_FILTERS +" (\n"
                + "    "+ QUERY_FILTER_ID +" integer ,\n"
                + "    "+ QUERY_ID +" integer ,\n"
                + "    "+ QUERY_FILTERS_OBJ +" blob ,\n"
                + "    "+ QUERY_AGGR_FILTERS_OBJ +" blob ,\n"
                + "    "+ QUERY_COL_FILTERS_OBJ +" blob ,\n"
                + "    "+ "PRIMARY KEY("+ QUERY_ID+", "+ QUERY_FILTER_ID+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ QUERY_ID +") REFERENCES "+QUERY_SAVE+"("+QUERY_ID+")); ";

        String sql16 = "CREATE TABLE IF NOT EXISTS "+ QUERY_MANUAL +" (\n"
                + "    "+ QUERY_MANUAL_ID +" integer ,\n"
                + "    "+ QUERY_ID +" integer ,\n"
                + "    "+ QUERY_AGGR_STR +" text ,\n"
                + "    "+ QUERY_FILTER_STR +" text ,\n"
                + "    "+ QUERY_COL_FILTER_STR +" text ,\n"
                + "    "+ QUERY_AGGR_FILTER_STR +" text ,\n"
                + "    "+ "PRIMARY KEY("+ QUERY_ID+", "+ QUERY_MANUAL_ID+") ON CONFLICT IGNORE, \n"
                + "    FOREIGN KEY ("+ QUERY_ID +") REFERENCES "+QUERY_SAVE+"("+QUERY_ID+")); ";


        executeStatements(new String[] {sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9, sql10, sql11, sql12, sql13, sql14, sql15, sql16});
    }

    public void deleteTables(){
        String sql16 = "DROP TABLE "+ QUERY_MANUAL +";";
        String sql1 = "DROP TABLE "+ QUERY_FILTERS +";";
        String sql2 = "DROP TABLE "+ QUERY_MEASURES +";";
        String sql3 = "DROP TABLE "+ QUERY_COLS +";";
        String sql4 = "DROP TABLE "+ QUERY_ROW +";";
        String sql5 = "DROP TABLE "+ QUERY_SAVE +";";
        String sql6 = "DROP TABLE "+ MULTIDIM_COLUMN +";";
        String sql7 = "DROP TABLE "+ MULTIDIM_TABLE +";";
        String sql8 = "DROP TABLE "+ CUBE_TABLE +";";
        String sql9 = "DROP TABLE "+ CORRESPONDENCES_DATA +";";
        String sql10 = "DROP TABLE "+ COLUMN_DATA +";";
        String sql11 = "DROP TABLE "+ TABLE_DATA +";";
        String sql12 = "DROP TABLE "+ DB_DATA +";";
        String sql13 = "DROP TABLE "+ DB_TYPE_DATA +";";
        String sql14 = "DROP TABLE "+ GLOBAL_COLUMN_DATA +";";
        String sql15 = "DROP TABLE "+ GLOBAL_TABLE_DATA +";";
        executeStatements(new String[] {sql16, sql1, sql2, sql3, sql4, sql5, sql6, sql7, sql8, sql9, sql10, sql11, sql12, sql13, sql14, sql15});
    }

    public void deleteTablesToSaveQueries(){
        String sql6 = "DROP TABLE "+ QUERY_MANUAL +";";
        String sql1 = "DROP TABLE "+ QUERY_FILTERS +";";
        String sql2 = "DROP TABLE "+ QUERY_MEASURES +";";
        String sql3 = "DROP TABLE "+ QUERY_COLS +";";
        String sql4 = "DROP TABLE "+ QUERY_ROW +";";
        String sql5 = "DROP TABLE "+ QUERY_SAVE +";";
        executeStatements(new String[] {sql6, sql1, sql2, sql3, sql4, sql5});
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

    public void printLocalSchema(){
        System.out.println("databases registered:");
        makeQueryAndPrint("SELECT * FROM "+ DB_DATA);

        System.out.println("Tables registered:");
        makeQueryAndPrint("SELECT * FROM "+ TABLE_DATA);

        System.out.println("Columns registered:");
        makeQueryAndPrint("SELECT * FROM "+ COLUMN_DATA);
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

    public TableData insertTableData(TableData table){
        String sql = "INSERT INTO "+ TABLE_DATA + "("+TABLE_DATA_NAME_FIELD+", "+TABLE_DATA_SCHEMA_NAME_FIELD+", "+TABLE_DATA_DB_ID_FIELD+","+TABLE_CODE_FIELD+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int dbID = table.getDB().getId();
            if (dbID <= 0){
                dbID = getDBID(table.getDB().getDbName(), table.getDB().getUrl());
                if (dbID == -1){
                    System.out.println("DB "+table.getDB().getDbName() + " in server " + table.getDB().getUrl() +" not found");
                    return null;
                }
            }
            pstmt.setString(1, table.getTableName());
            pstmt.setString(2, table.getSchemaName());
            pstmt.setInt(3, dbID);
            pstmt.setString(4, table.getSqlCode());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next())
            {
                int lastInsertedId = rs.getInt(1);
                //System.out.println("KEY: "+lastInsertedId);
                if (lastInsertedId == 0) //if db was inserted previously, id will be zero
                    lastInsertedId = getTableID(table.getTableName(), table.getSchemaName(), table.getDB());
                table.setId(lastInsertedId);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return table;
    }

    public List<TableData> insertTableData(List<TableData> tables){
        String sql = "INSERT INTO "+ TABLE_DATA + "("+TABLE_DATA_NAME_FIELD+", "+TABLE_DATA_SCHEMA_NAME_FIELD+", "+TABLE_DATA_DB_ID_FIELD+","+TABLE_CODE_FIELD+") VALUES(?,?,?,?)";
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
                pstmt.setString(4, tables.get(i).getSqlCode());
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

    public TableData insertColumnData(TableData columnsInTable){
        String sql = "INSERT INTO "+ COLUMN_DATA + "("+COLUMN_DATA_NAME_FIELD+", "+COLUMN_DATA_TYPE_FIELD+", "+COLUMN_DATA_IS_PRIMARY_KEY_FIELD+", "+COLUMN_DATA_FOREIGN_KEY_FIELD+", "
                + COLUMN_DATA_TABLE_FIELD +") VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                List<ColumnData> columns = columnsInTable.getColumnsList();
                for (int i = 0; i < columns.size(); i++){
                    pstmt.setString(1, columns.get(i).getName());
                    pstmt.setString(2, columns.get(i).getDataType());
                    pstmt.setBoolean(3, columns.get(i).isPrimaryKey());
                    pstmt.setString(4, columns.get(i).getForeignKey());
                    pstmt.setInt(5, columnsInTable.getId());
                    pstmt.executeUpdate();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if(rs.next())
                    {
                        int lastInsertedId = rs.getInt(1);
                        //System.out.println("KEY: "+lastInsertedId);
                        ColumnData cd = columns.get(i);
                        if (lastInsertedId == 0) //if db was inserted previously, id will be zero
                            lastInsertedId = getColumnID(columns.get(i).getName(), columnsInTable.getId());
                        cd.setColumnID(lastInsertedId);
                        columns.set(i, cd);//update the id of the db list in memory
                    }
                TableData t = columnsInTable;
                t.setColumnsList(columns);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return columnsInTable;
    }

    public List<TableData> insertColumnData(List<TableData> columnsInTables){
        String sql = "INSERT INTO "+ COLUMN_DATA + "("+COLUMN_DATA_NAME_FIELD+", "+COLUMN_DATA_TYPE_FIELD+", "+COLUMN_DATA_IS_PRIMARY_KEY_FIELD+", "+COLUMN_DATA_FOREIGN_KEY_FIELD+", "
                + COLUMN_DATA_TABLE_FIELD +") VALUES(?,?,?,?,?)";
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
                    pstmt.setInt(5, columnsInTables.get(j).getId());
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

    public boolean isLocalSchemaCreated(){
        if (getDatabaseCount() > 0)
            return true;
        return false;
    }

    public boolean isGlobalSchemaCreated(){
        if (getGlobalTablesCount() > 0)
            return true;
        return false;
    }

    public int getDatabaseCount(){
        int nDBs = 0;
        try{
            Statement stmt  = conn.createStatement();
            ResultSet res    = stmt.executeQuery("SELECT COUNT(*) FROM " + DB_DATA +";");
            while (res.next()){
                nDBs = res.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return nDBs;
    }

    public int getGlobalTablesCount(){
        int nDBs = 0;
        try{
            Statement stmt  = conn.createStatement();
            ResultSet res    = stmt.executeQuery("SELECT COUNT(*) FROM " + GLOBAL_TABLE_DATA +";");
            while (res.next()){
                nDBs = res.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return nDBs;
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
                TableData t = new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), dbData, rs.getInt(ID_FIELD), rs.getString(TABLE_CODE_FIELD));
                t.setColumnsList(getColumnsInTable(t));
                tables.add(t);
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

    public boolean dbExists(String dbName, String server){
        String query = "SELECT "+DB_DATA_NAME_FIELD+", "+DB_DATA_SERVER_FIELD+" FROM " + DB_DATA + " WHERE "+ DB_DATA_NAME_FIELD +" = '"+ dbName +"' AND " + DB_DATA_SERVER_FIELD +" = '" + server + "'";
        Statement stmt  = null;
        boolean exists = false;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {//has a result, db exists
                exists = true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return exists;
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
                + " AND "+ TABLE_DATA_DB_ID_FIELD +" = "+dbID+"";
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

    public boolean tableExists(String tableName, String schema, String dbName, String server){
        int dbID = this.getDBID(dbName, server);
        if (dbID < 1)
            return false;
        String query = "SELECT id FROM " + TABLE_DATA + " WHERE "+ TABLE_DATA_NAME_FIELD +" = '"+ tableName +"' AND " + TABLE_DATA_SCHEMA_NAME_FIELD +" = '" + schema + "'"
                + " AND "+ TABLE_DATA_DB_ID_FIELD +" = "+dbID+"";
        Statement stmt  = null;
        boolean exists = false;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return exists;
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

    public boolean columnExists(String columnName, String tableName, String schema, DBData db){
        int tableID = this.getTableID(tableName, schema, db);
        String query = "SELECT id FROM " + COLUMN_DATA + " WHERE "+ COLUMN_DATA_NAME_FIELD +" = '"+ columnName +"' AND " + COLUMN_DATA_TABLE_FIELD +" = " + tableID + "";
        Statement stmt  = null;
        if (tableID < 1)
            return false;
        boolean exists = false;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
                exists = true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return exists;
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

    public void deleteGlobalSchemaAndCubes(){
        String sql1 = "DELETE FROM "+ MULTIDIM_COLUMN +";";
        String sql2 = "DELETE FROM "+ MULTIDIM_TABLE +";";
        String sql3 = "DELETE FROM "+ CUBE_TABLE +";";
        String sql4 = "DELETE FROM "+ CORRESPONDENCES_DATA +";";
        String sql5 = "DELETE FROM "+ GLOBAL_COLUMN_DATA +";";
        String sql6 = "DELETE FROM "+ GLOBAL_TABLE_DATA +";";
        executeStatements(new String[]{sql1, sql2, sql3, sql4, sql5, sql6});
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
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    int lastInsertedId = rs.getInt(1);
                    globalTableData.setId(lastInsertedId);
                    globalTables.set(i, globalTableData);
                }
            }

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
                ", "+GLOBAL_COLUMN_DATA_TYPE_FIELD+", "+GLOBAL_COLUMN_DATA_TYPEOG_FIELD+", "+GLOBAL_COLUMN_DATA_TYPE_CHANGE_FIELD+", "+
                GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD+ ", "+GLOBAL_COLUMN_DATA_FOREIGN_KEY_FIELD+") VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Iterate through a list of previously inserted global tables and insert, for each table id, the list of its global columns
                List<GlobalColumnData> globalColumns = gt.getGlobalColumnDataList();
                //insert a global column
                for (int i = 0; i < globalColumns.size(); i++){
                    GlobalColumnData column = globalColumns.get(i);
                    pstmt.setInt(1, gt.getId());
                    pstmt.setString(2, column.getName());
                    pstmt.setString(3, column.getDataType());
                    pstmt.setString(4, column.getOgDataTypeNoLimit());
                    pstmt.setBoolean(5, column.isOriginalDatatypeChanged());
                    pstmt.setBoolean(6, column.isPrimaryKey());
                    pstmt.setString(7, column.getForeignKey());
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
                    pstmt.setInt(3, localColumn.getMapping().getNumber());
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
    public boolean insertStarSchema(StarSchema starSchema){

        int cubeId = getOrcreateCube(starSchema.getSchemaName());
        if (cubeId < 1){
            return false;
        }
        int factsID = insertFactsTable(cubeId, starSchema.getFactsTable());
        if (factsID < 1){
            return false;
        }
        insertDimsTables(cubeId, starSchema.getDimsTables());
        return true;
    }

    private int insertFactsTable(int cubeID, FactsTable factsTable){
        int factsTableID = -1;
        String sql = "INSERT INTO "+ MULTIDIM_TABLE + "("+GLOBAL_TABLE_DATA_ID_FIELD+", "+MULTIDIM_TABLE_CUBE_ID+", "+
                MULTIDIM_TABLE_ISFACTS+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, factsTable.getGlobalTable().getId());
            pstmt.setInt(2, cubeID);
            pstmt.setBoolean(3, true);
            pstmt.executeUpdate();
            //get id of inserted global column
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next())
            {
                factsTableID = rs.getInt(1);
                factsTable.setId(factsTableID);
            }
            //insert the columns
            insertMultiDimColumn(cubeID, factsTable);

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
                pstmt.setBoolean(3, false);
                pstmt.executeUpdate();
                //get id of inserted global column
                ResultSet rs = pstmt.getGeneratedKeys();
                if(rs.next())
                {
                    dimsTableID = rs.getInt(1);
                }
                insertMultiDimColumn(cubeID, dimsTableID, gt);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dimsTableID;
    }

    private void insertMultiDimColumn(int cubeID, FactsTable facts){
        String sql = "INSERT INTO "+ MULTIDIM_COLUMN + "(" + CUBE_ID_FIELD+", "+MULTIDIM_TABLE_ID+", "+MULTIDIM_COLUMN_MEASURE+", "+
                MULTIDIM_COL_GLOBAL_COLUMN_ID+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<GlobalColumnData, Boolean> gc : facts.getColumns().entrySet()){
                pstmt.setInt(1, cubeID);
                pstmt.setInt(2, facts.getId());//id of multidimensional table (facts table in this case)
                pstmt.setBoolean(3, gc.getValue()); //is or not a measure
                pstmt.setInt(4, gc.getKey().getColumnID());
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Inserts columns of dimension tables of a star schema Table info
     * @param cubeID
     * @param dimTableID
     * @param dimTable
     * @return
     */
    private void insertMultiDimColumn(int cubeID, int dimTableID,  GlobalTableData dimTable){
        String sql = "INSERT INTO "+ MULTIDIM_COLUMN + "(" + CUBE_ID_FIELD+", "+MULTIDIM_TABLE_ID+", "+MULTIDIM_COLUMN_MEASURE+", "+
                MULTIDIM_COL_GLOBAL_COLUMN_ID+") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (GlobalColumnData gc : dimTable.getGlobalColumnDataList()){
                pstmt.setInt(1, cubeID);
                pstmt.setInt(2, dimTableID);//id of multidimensional table (dim table in this case)
                pstmt.setBoolean(3, false); //is or not a measure (this column belongs to a dimension table, so it is never a measure)
                pstmt.setInt(4, gc.getColumnID());
                pstmt.executeUpdate();
                //get id of inserted global column
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean cubeExists(String cubeName){
        String sql = "SELECT "+CUBE_ID_FIELD+" FROM "+ CUBE_TABLE +" WHERE "+CUBE_NAME+" = '"+cubeName+"'";
        boolean exists = false;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                int cubeID = rs.getInt(CUBE_ID_FIELD);
                if (cubeID > 1)
                    exists = true;
                System.out.println(cubeID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return exists;
    }

    public int getOrcreateCube(String cubeName){
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
    public int getCubeID(String cubeName){
        String sql = "SELECT * FROM "+ CUBE_TABLE + " WHERE "+CUBE_NAME+"= '"+cubeName+"'";
        int cubeId = -1;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                cubeId = rs.getInt(CUBE_ID_FIELD);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return cubeId;
    }

    public List<String> getStarSchemaNames(){
        List<String> starSchemas = new ArrayList<>();
        String sql = "SELECT "+CUBE_NAME+" FROM "+ CUBE_TABLE;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                starSchemas.add(rs.getString(CUBE_NAME));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return starSchemas;
    }

    public StarSchema getStarSchema(String cubeName){
        int cubeID = getCubeID(cubeName);
        if (cubeID < 1)
            return null;
        FactsTable facts = null;
        List<GlobalTableData> dimsTables = new ArrayList<>();
        String sql = "SELECT "+MULTIDIM_TABLE_ID+", " +MULTIDIM_TABLE_ISFACTS +", "+ GLOBAL_TABLE_DATA_ID_FIELD+" FROM "+ MULTIDIM_TABLE + " WHERE "+MULTIDIM_TABLE_CUBE_ID+"= "+cubeID+"";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int multiDimTableID = rs.getInt(MULTIDIM_TABLE_ID);
                boolean isFacts = rs.getBoolean(MULTIDIM_TABLE_ISFACTS);
                int globalTableID = rs.getInt(GLOBAL_TABLE_DATA_ID_FIELD);
                GlobalTableData globalTable = getGlobalTableFromID(globalTableID);
                if (isFacts){
                    Map<GlobalColumnData, Boolean> measures = getMeasuresFromMultiDimCol(cubeID, multiDimTableID, globalTable.getGlobalColumnDataList());
                    facts = new FactsTable(multiDimTableID, cubeID, globalTable, measures);
                }
                else{
                    dimsTables.add(globalTable);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return new StarSchema(cubeName, cubeID, facts, dimsTables);
    }

    public List<GlobalTableData> getGlobalSchema(){
        List<GlobalTableData> globalTables = new ArrayList<>();
        String sql = "SELECT "+GLOBAL_TABLE_DATA_ID_FIELD +" FROM "+ GLOBAL_TABLE_DATA;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int tableID = rs.getInt(GLOBAL_TABLE_DATA_ID_FIELD);
                globalTables.add(getGlobalTableFromID(tableID));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalTables;
    }

    public GlobalTableData getGlobalTableFromID(int globalTableID){
        GlobalTableData globalTableData = null;
        String sql = "SELECT "+GLOBAL_TABLE_DATA_NAME_FIELD +" FROM "+ GLOBAL_TABLE_DATA + " WHERE "+GLOBAL_TABLE_DATA_ID_FIELD+" = "+globalTableID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                String tableName = rs.getString(GLOBAL_TABLE_DATA_NAME_FIELD);
                globalTableData = new GlobalTableData(tableName);
                globalTableData.setId(globalTableID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        if (globalTableData != null){
            //get its columns and correspondences
            globalTableData.setGlobalColumnData(getGlobalColumnsInGlobalTable(globalTableData.getId()));
        }
        return globalTableData;
    }

    private List<GlobalColumnData> getGlobalColumnsInGlobalTable(int globalTableID){
        List<GlobalColumnData> globalCols = new ArrayList<>();
        String sql = "SELECT * FROM "+ GLOBAL_COLUMN_DATA + " WHERE "+GLOBAL_COLUMN_DATA_TABLE_FIELD+" = "+globalTableID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int globalColID = rs.getInt(GLOBAL_COLUMN_DATA_ID_FIELD);
                Set<ColumnData> globalColsCorrs = getCorrespondencesFromGlobalColumn(globalColID);
                GlobalColumnData globalCol = new GlobalColumnData(rs.getString(GLOBAL_COLUMN_DATA_NAME_FIELD), rs.getString(GLOBAL_COLUMN_DATA_TYPE_FIELD),
                        rs.getBoolean(GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD), globalColsCorrs);
                globalCol.setOGDataType(rs.getString(GLOBAL_COLUMN_DATA_TYPEOG_FIELD));
                globalCol.setColumnID(rs.getInt(GLOBAL_COLUMN_DATA_ID_FIELD));
                globalCol.setForeignKey(rs.getString(GLOBAL_COLUMN_DATA_FOREIGN_KEY_FIELD));
                globalCols.add(globalCol);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalCols;
    }

    private GlobalColumnData getGlobalColumnByID(int globalColID){
        GlobalColumnData globalCol = null;
        String sql = "SELECT * FROM "+ GLOBAL_COLUMN_DATA + " WHERE "+GLOBAL_COLUMN_DATA_ID_FIELD+" = "+globalColID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                Set<ColumnData> globalColsCorrs = getCorrespondencesFromGlobalColumn(globalColID);
                globalCol = new GlobalColumnData(rs.getString(GLOBAL_COLUMN_DATA_NAME_FIELD), rs.getString(GLOBAL_COLUMN_DATA_TYPE_FIELD),
                        rs.getBoolean(GLOBAL_COLUMN_DATA_PRIMARY_KEY_FIELD), globalColsCorrs);
                globalCol.setOGDataType(rs.getString(GLOBAL_COLUMN_DATA_TYPEOG_FIELD));
                globalCol.setColumnID(rs.getInt(GLOBAL_COLUMN_DATA_ID_FIELD));
                globalCol.setForeignKey(rs.getString(GLOBAL_COLUMN_DATA_FOREIGN_KEY_FIELD));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return globalCol;
    }

    /**
     * Query the correspondences table and get all local columns that pair with the given global column id.
     * @param globalColID
     * @return
     */
    private Set<ColumnData> getCorrespondencesFromGlobalColumn(int globalColID){
        Set<ColumnData> corrs = new HashSet<>();

        String sql = "SELECT * FROM "+ CORRESPONDENCES_DATA + " WHERE "+CORRESPONDENCES_GLOBAL_COL_FIELD+" = "+globalColID;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int localColID = rs.getInt(CORRESPONDENCES_LOCAL_COL_FIELD);
                //get local column
                MappingType mapping = MappingType.getMapping(rs.getInt(CORRESPONDENCES_TYPE_FIELD));
                corrs.add(getLocalColumnByID(localColID, mapping));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return corrs;

    }

    /**
     * Used mainly by facts table to get know if global columns are or not measures
     * @param cubeID
     * @param multiDimTableID
     * @return
     */
    public Map<GlobalColumnData, Boolean> getMeasuresFromMultiDimCol (int cubeID, int multiDimTableID, List<GlobalColumnData> globalCols){
        Map<GlobalColumnData, Boolean> measures = new HashMap<>();

        for (GlobalColumnData globalCol : globalCols) {
            String sql = "SELECT " + MULTIDIM_COLUMN_MEASURE + " FROM " + MULTIDIM_COLUMN + " WHERE " + CUBE_ID_FIELD + " = " + cubeID +
                    " AND " + MULTIDIM_TABLE_ID + " = " + multiDimTableID + " AND " + MULTIDIM_COL_GLOBAL_COLUMN_ID + " = " + globalCol.getColumnID();
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                // loop through the result set
                while (rs.next()) {
                    boolean isMeasure = rs.getBoolean(MULTIDIM_COLUMN_MEASURE);
                    measures.put(globalCol, isMeasure);
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return measures;
    }

    public List<TableData> getLocalTablesByID(Set<Integer> tableIDs){
        List<TableData> tables = new ArrayList<>();
        for (Integer id : tableIDs) {
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT "+ TABLE_DATA_NAME_FIELD +", "+TABLE_DATA_SCHEMA_NAME_FIELD+
                        ", "+TABLE_DATA_DB_ID_FIELD+", "+ TABLE_CODE_FIELD+" FROM " + TABLE_DATA + " where "+ID_FIELD+"="+id+";");
                // loop through the result set
                while (rs.next()) {
                    DBData db = this.getDatabaseByID(rs.getInt(TABLE_DATA_DB_ID_FIELD));
                    TableData table = new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), db, id, rs.getString(TABLE_CODE_FIELD));
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
                        ", "+TABLE_DATA_DB_ID_FIELD+", "+TABLE_CODE_FIELD+" FROM " + TABLE_DATA + " where "+ID_FIELD+"="+tableID+";");
                // loop through the result set
                while (rs.next()) {
                    DBData db = this.getDatabaseByID(rs.getInt(TABLE_DATA_DB_ID_FIELD));
                    table = new TableData(rs.getString(TABLE_DATA_NAME_FIELD), rs.getString(TABLE_DATA_SCHEMA_NAME_FIELD), db, tableID, rs.getString(TABLE_CODE_FIELD));
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

    public ColumnData getLocalColumnByID(int localID, MappingType mapping){
        ColumnData col = null;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + COLUMN_DATA + " where "+ID_FIELD+"="+localID+";");
            // loop through the result set
            while (rs.next()) {
                int tableID = rs.getInt(COLUMN_DATA_TABLE_FIELD);
                TableData table = getTableByID(tableID);
                col = new ColumnData.Builder(rs.getString(COLUMN_DATA_NAME_FIELD), rs.getString(COLUMN_DATA_TYPE_FIELD), rs.getBoolean(COLUMN_DATA_IS_PRIMARY_KEY_FIELD))
                        .withForeignKey(rs.getString(COLUMN_DATA_FOREIGN_KEY_FIELD)).withID(rs.getInt(ID_FIELD)).withTable(table).withMappingType(mapping).build();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return col;
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
            ResultSet rs = stmt.executeQuery("SELECT "+ ID_FIELD+", "+TABLE_CODE_FIELD+" FROM " + TABLE_DATA + " where "+TABLE_DATA_DB_ID_FIELD+"="+db.getId()+" and "
                    +TABLE_DATA_NAME_FIELD +" ='" + tableName +"' and "+TABLE_DATA_SCHEMA_NAME_FIELD+" = '"+schemaName+"';");
            // loop through the result set
            while (rs.next()) {
                t = new TableData(tableName, schemaName,db, rs.getInt(ID_FIELD), rs.getString(TABLE_CODE_FIELD));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return t;
    }

    public boolean insertNewQuerySave(String queryName, String cubeName, Map<GlobalTableData, List<GlobalColumnData>> rows,
                                   Map<GlobalTableData, List<GlobalColumnData>> columns, List<GlobalColumnData> measures,
                                      FilterNode filters, FilterNode colFilterRoot, FilterNode aggrFilters, String aggrManualStr,
                                      String filtersManualStr, String colFiltersManualStr, String AggrFiltersManualEdit){
        int cubeID = getOrcreateCube(cubeName);
        String sql = "INSERT INTO "+ QUERY_SAVE + "("+QUERY_CUBE_ID+", "+QUERY_NAME+") VALUES(?,?)";
        int queryID = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cubeID);
            pstmt.setString(2, queryName);
            pstmt.executeUpdate();
            //get id of this query
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next())
            {
                queryID = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        if (queryID == -1){
            System.err.println("Query Save: problem inserting in database");
            return false;
        }

        //insert manual edition elements (if any)
        sql = "INSERT INTO "+ QUERY_MANUAL + "("+QUERY_ID+", "+QUERY_AGGR_STR+", "+QUERY_FILTER_STR+", "+QUERY_COL_FILTER_STR+", "+QUERY_AGGR_FILTER_STR+") VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, queryID);
            pstmt.setString(2, aggrManualStr);
            pstmt.setString(3, filtersManualStr);
            pstmt.setString(4, colFiltersManualStr);
            pstmt.setString(5, AggrFiltersManualEdit);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        //insert rows
        sql = "INSERT INTO "+ QUERY_ROW + "("+QUERY_ID+", "+QUERY_GLOBAL_TABLE_ID+", "+QUERY_GLOBAL_ROW_OBJ+") VALUES(?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelects : rows.entrySet()){
                int tableID = tableSelects.getKey().getId();
                for (GlobalColumnData c : tableSelects.getValue()){
                    byte[] rowByte = makebyteFromGlobalCol(c);

                    pstmt.setInt(1, queryID);
                    pstmt.setInt(2, tableID);
                    pstmt.setBytes(3, rowByte);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
        //insert cols (if any)
        if (columns != null && columns.size() > 0) {
            sql = "INSERT INTO " + QUERY_COLS + "(" + QUERY_ID + ", " + QUERY_GLOBAL_TABLE_ID + ", " + QUERY_GLOBAL_COLUMN_OBJ + ") VALUES(?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelects : columns.entrySet()) {
                    int tableID = tableSelects.getKey().getId();
                    for (GlobalColumnData c : tableSelects.getValue()) {
                        byte[] columnByte = makebyteFromGlobalCol(c);

                        pstmt.setInt(1, queryID);
                        pstmt.setInt(2, tableID);
                        pstmt.setBytes(3, columnByte);
                        pstmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }

        //insert measures (if any)
        if (measures != null && measures.size() > 0) {
            sql = "INSERT INTO " + QUERY_MEASURES + "(" + QUERY_ID + ", " + QUERY_MEASURE_OBJ + ") VALUES(?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (GlobalColumnData c : measures) {
                    pstmt.setInt(1, queryID);
                    byte[] measureByte = makebyteFromGlobalCol(c);
                    pstmt.setBytes(2,  measureByte);
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }

        //insert filters (if any)
        byte[] filtersByte = new byte[0];
        byte[] colFiltersByte = new byte[0];
        byte[] aggrFiltersByte = new byte[0];
        if (filters != null)
            filtersByte = makebyteFromTreeModel(filters);
        if (colFilterRoot != null)
            colFiltersByte = makebyteFromTreeModel(colFilterRoot);
        if (aggrFilters != null)
            aggrFiltersByte = makebyteFromTreeModel(aggrFilters);


        sql = "INSERT INTO " + QUERY_FILTERS + "(" + QUERY_ID + ", " + QUERY_FILTERS_OBJ + ", " + QUERY_COL_FILTERS_OBJ+
                ", "+ QUERY_AGGR_FILTERS_OBJ +") VALUES(?,?,?,?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, queryID);
            pstmt.setBytes(2, filtersByte);
            pstmt.setBytes(3, colFiltersByte);
            pstmt.setBytes(4, aggrFiltersByte);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public void deleteQuery(String queryName, String cubeName){
        int cubeID = getOrcreateCube(cubeName);
        int queryID = getQueryID(queryName, cubeID);

        String sql = "DELETE FROM "+QUERY_FILTERS +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM "+QUERY_MANUAL +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM "+QUERY_MEASURES +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM "+QUERY_ROW +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM "+QUERY_COLS +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        sql = "DELETE FROM "+QUERY_SAVE +" WHERE "+QUERY_ID +" = "+queryID+";";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<String> getListOfQueriesByCube(String cubeName){
        int cubeID = getOrcreateCube(cubeName);
        List<String> queryNames = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_NAME+" FROM " + QUERY_SAVE + " WHERE "+QUERY_CUBE_ID+" = " +cubeID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                queryNames.add(rs.getString(QUERY_NAME));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return queryNames;
    }

    public int getQueryID(String queryName, int cubeID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_ID+" FROM " + QUERY_SAVE + " WHERE "+QUERY_CUBE_ID+" = " +cubeID+" AND "+QUERY_NAME+" = '"+queryName+"';";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return rs.getInt(QUERY_ID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getQueryRows(int queryID){
        Map<GlobalTableData, List<GlobalColumnData>> rows = new HashMap<>();
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_GLOBAL_TABLE_ID +", "+QUERY_GLOBAL_ROW_OBJ+" FROM " + QUERY_ROW + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int tableID = rs.getInt(QUERY_GLOBAL_TABLE_ID);
                GlobalColumnData c = readGlobalCol(rs.getBytes(QUERY_GLOBAL_ROW_OBJ));
                GlobalTableData t = this.getGlobalTableFromID(tableID);
                if (rows.containsKey(t)){
                    List<GlobalColumnData> cols = rows.get(t);
                    cols.add(c);
                    rows.put(t, cols);
                }
                else{
                    List<GlobalColumnData> cols = new ArrayList<>();
                    cols.add(c);
                    rows.put(t, cols);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return rows;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getQueryColumns(int queryID){
        Map<GlobalTableData, List<GlobalColumnData>> colums = new HashMap<>();
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_GLOBAL_TABLE_ID +", "+QUERY_GLOBAL_COLUMN_OBJ+" FROM " + QUERY_COLS + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                int tableID = rs.getInt(QUERY_GLOBAL_TABLE_ID);
                GlobalColumnData c = readGlobalCol(rs.getBytes(QUERY_GLOBAL_COLUMN_OBJ));
                GlobalTableData t = this.getGlobalTableFromID(tableID);
                if (colums.containsKey(t)){
                    List<GlobalColumnData> cols = colums.get(t);
                    cols.add(c);
                    colums.put(t, cols);
                }
                else{
                    List<GlobalColumnData> cols = new ArrayList<>();
                    cols.add(c);
                    colums.put(t, cols);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return colums;
    }

    public List<GlobalColumnData> getQueryMeasures(int queryID){
        List<GlobalColumnData> measures = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_MEASURE_OBJ+" FROM " + QUERY_MEASURES + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                GlobalColumnData measure = readGlobalCol(rs.getBytes(QUERY_MEASURE_OBJ));
                measures.add(measure);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return measures;
    }

    public String getManualAggr(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_AGGR_STR +" FROM "+QUERY_MANUAL+" WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return rs.getString(QUERY_AGGR_STR);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String getManualFilter(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_FILTER_STR +" FROM "+QUERY_MANUAL+" WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return rs.getString(QUERY_FILTER_STR);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String getManualColFilter(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_COL_FILTER_STR +" FROM "+QUERY_MANUAL+" WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return rs.getString(QUERY_COL_FILTER_STR);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String getManualFilterAggr(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_AGGR_FILTER_STR+" FROM "+QUERY_MANUAL +" WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return rs.getString(QUERY_AGGR_FILTER_STR);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public FilterNode getQueryFilters(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_FILTERS_OBJ +" FROM " + QUERY_FILTERS + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return readTreeModel(rs.getBytes(QUERY_FILTERS_OBJ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public FilterNode getQueryColFilters(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_COL_FILTERS_OBJ+" FROM " + QUERY_FILTERS + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return readTreeModel(rs.getBytes(QUERY_COL_FILTERS_OBJ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public FilterNode getQueryAggrFilters(int queryID){
        try {
            Statement stmt = conn.createStatement();
            String sql = "SELECT "+ QUERY_AGGR_FILTERS_OBJ +" FROM " + QUERY_FILTERS + " WHERE "+QUERY_ID+" = " +queryID+";";
            ResultSet rs = stmt.executeQuery(sql);
            // loop through the result set
            if (rs.next()) {
                return readTreeModel(rs.getBytes(QUERY_AGGR_FILTERS_OBJ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    //adapted from https://stackoverflow.com/a/55487151
    private byte[] makebyteFromTreeModel(FilterNode modeldata) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(modeldata);
            byte[] bytes = baos.toByteArray();
            //ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    //adpated from: https://stackoverflow.com/a/55487151
    public FilterNode readTreeModel(byte[] data) {
        if (data.length == 0)
            return null;
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            FilterNode filterNode = (FilterNode ) ois.readObject();
            return filterNode ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] makebyteFromGlobalCol(GlobalColumnData columnData) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(columnData);
            byte[] bytes = baos.toByteArray();
            //ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public GlobalColumnData readGlobalCol(byte[] data) {
        if (data.length == 0)
            return null;
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            GlobalColumnData globalCol = (GlobalColumnData) ois.readObject();
            return globalCol ;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


}
