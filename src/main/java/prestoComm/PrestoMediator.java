package prestoComm;

import helper_classes.ColumnData;
import helper_classes.DBData;
import helper_classes.TableData;
import io.prestosql.jdbc.$internal.guava.collect.ImmutableSet;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import static prestoComm.Constants.*;

public class PrestoMediator {

    private String url = "jdbc:presto://127.0.0.1:8080";
    final String JDBC_DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
    private Connection conn;
    private String user = "test";
    private String pass = null;
    private DBConnector prestoConnector;
    private ResultSet res;
    private Statement stmt;

    public PrestoMediator(){
        prestoConnector = new DBConnector(url, JDBC_DRIVER, user, pass);
        this.createConnection();
    }

    public static void main (String[] args){
        PrestoMediator connector = new PrestoMediator();
        connector.createConnection();
        //connector.getTableData("select * from mongodb.products.products");
        System.out.println(connector.makeQuery("show schemas from postgresql_localhost_5432_employees_vertical"));
        /*try {
            connector.getOneColumnResultQuery("show tables from mysql_test.sales_schema", true);
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        /*if (connector.showRestartPrompt() == true){
            System.out.println("Presto restarted succesfully");
        }
        else{
            System.out.println("Presto failed to restart");
        }*/
        //connector.getDBData("mongodb");
        //connector.makeQuery("describe prestodb.public.catalog_page");
    }

    public boolean createConnection(){
        //Open a connection
        conn = prestoConnector.setConnection();
        if (conn == null)
            return false;
        return true;
    }


    public String makeQuery(String query){
        Statement stmt = null;
        String queryStateMessage = "";
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            //Execute a query
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);
            ResultSetMetaData rsmd = res.getMetaData();
            if (rsmd.getColumnCount() > 1){
                //get all columns
                while (res.next()) {
                    for (int i = 1; i < rsmd.getColumnCount(); i++){
                        String name = rsmd.getColumnName(i);
                        System.out.print(name+": " + res.getString(name)+", ");
                    }
                    System.out.println("");
                }
                queryStateMessage = SUCCESS_STR;
            }
            else{
                //only one column
                ImmutableSet.Builder<String> set = ImmutableSet.builder();
                while (res.next()) {
                    set.add(res.getString(1));
                }
                Set setFinal = set.build();
                setFinal.size();
                Iterator<String> it = setFinal.iterator();
                while(it.hasNext()){
                    System.out.println(it.next());
                }
                queryStateMessage = SUCCESS_STR;
            }
            //Clean-up environment
            res.close();
            stmt.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            queryStateMessage = se.getCause().getLocalizedMessage();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            queryStateMessage = e.getCause().getLocalizedMessage();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            } finally {
                return queryStateMessage;
            }
        }
    }

    public void closeConection(){
        prestoConnector.closeConn();
    }

    /**
     * Given information about a database, create a .properties file for presto to be able to access that database
     * @param dbModel - database used identified by DBModel enum
     * @param serverUrl - the url to access the db. Must be in the form url:port
     * @param user
     * @param pass
     */
    public void createDBFileProperties(DBModel dbModel, String serverUrl, String user, String pass, String dbName){
        DBData dbData = new DBData(dbName, dbModel, serverUrl, user, pass);
        createDBFileProperties(dbData);
    }

    /**
     * Given information about a database, create a .properties file for presto to be able to access that database
     * @param dbData - database information (url, auth data, db type...)
     */
    public void createDBFileProperties(DBData dbData){
        //delete any possible http:// on server url or incorrect '/'
        Writer writer = null;
        String configFileName = dbData.getFullFilePath();
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFileName), "utf-8"));
            writer.write("connector.name = " + dbData.getDbModel().toString().toLowerCase()+"\n");
            writer.write(writeDBTypePropertiesFile(dbData));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given a database, add config file and check to see if presto can connect to it
     * @param db
     * @return
     */
    public String testDBConnection(DBData db){
        createDBFileProperties(db);
        showRestartPrompt();
        String state = this.makeQuery("show schemas from "+db.getCatalogName());
        if (!state.equals(SUCCESS_STR))
            removeDBFile(db.getFullFilePath()); //remove config file that points to DB with incorrect permitions or data
        return state;
    }

    private boolean removeDBFile(String configFile){
        File f = new File(configFile);
        return f.delete();
    }

    //returns a string with the proper config text for the .properties file for a specific DB
    private String writeDBTypePropertiesFile(DBData dbData){
        String config = "";
        final boolean authNeeded = ( dbData.getUser() != null && !dbData.getUser().isEmpty()) && ( dbData.getPass() != null && !dbData.getPass().isEmpty());
        if (dbData.getDbModel().equals(DBModel.MongoDB)){
            config += "mongodb.seeds = "+dbData.getUrl()+"\n";
            if (authNeeded) {
                config += "mongodb.credentials = " + dbData.getUser() + ":" + dbData.getPass() + "@" + dbData.getDbName() + "\n";
            }
        }
        else if (dbData.getDbModel().equals(DBModel.Redis)){
            config += "redis.table-names="+dbData.getDbName()+"\n";//list of all tables in the database (schema1.table1,schema1.table2)
            config += "redis.nodes="+dbData.getUrl()+"\n";
            if ( dbData.getPass() != null && !dbData.getPass().isEmpty()) {
                config += "redis.password="+dbData.getPass()+"\n";
            }
        }
        else if (dbData.getDbModel().equals(DBModel.Cassandra)){//TODO: if port is not default (9042), cassandra.native-protocol-port must be defined with the port
            config += "cassandra.contact-points="+dbData.getUrl()+"\n";
            if (authNeeded) {
                config += "cassandra.username="+dbData.getUser()+"\n";
                config += "cassandra.password="+dbData.getPass()+"\n";
            }
        }

        else {
            //relational dbs (mysql, sql server or postgresql) have identical .properties files
            if (dbData.getDbModel() == DBModel.MYSQL)
                config += "connection-url=jdbc:" + dbData.getDbModel().toString().toLowerCase() + "://"+ dbData.getUrl() +"\n"; //mysql connector throws exception if database is specified. DBs are separed in schemas
            else
                config += "connection-url=jdbc:" + dbData.getDbModel().toString().toLowerCase() + "://"+ dbData.getUrl() +"/"+dbData.getDbName()+"\n";
            config += "connection-user="+dbData.getUser()+"\n";
            config += "connection-password="+dbData.getPass()+"\n";
        }
        return config;
    }

    public List<TableData> getTablesInDatabase(DBData db){
        List<TableData> tablesInDB= new ArrayList<>();
        try {
            //for each schema, get tables
            Set<String> schemaNames = getOneColumnResultQuery("show schemas from " +  db.getCatalogName()+"", false);
            //exclude irrelevant schemas (administration or metadata of the DBMS)
            schemaNames = removeIrrelevantSchemas(schemaNames, db.getDbModel());

            //for each schema, get the tables and store them
            for (String schema : schemaNames){
                Set<String> tableNames = getOneColumnResultQuery("show tables from " +  db.getCatalogName()+"."+schema, false);
                tableNames = removeIrrelevantTables(tableNames, db.getDbModel());
                //remove unwanted tables
                for (String tableName : tableNames){
                    tablesInDB.add(new TableData(tableName, schema, db));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tablesInDB;
    }

    public TableData getColumnsInTable(TableData table){
        List<ColumnData> columnsInTable = new ArrayList<>();
        try {
            //for each schema, get tables
            List<Map> tableColumns = getColumnsResultQuery("show columns from " +  table.getDB().getCatalogName()+"."+table.getSchemaName()+"."+table.getTableName(), false);

            //for each column, store its info.
            for (Map tableColumn : tableColumns){
                String columnName = (String) tableColumn.get(SHOW_COLS_COLUMN);
                String columnDataType = (String) tableColumn.get(SHOW_COLS_TYPE);
                columnsInTable.add(new ColumnData.Builder(columnName, columnDataType).withTable(table).build());
            }
            //if this table belongs to a relational DB, get information about primary keys and foreign key constraints
            if(table.getDB().getDbModel().isRelational()){
                columnsInTable = updateTableConstraints(table, columnsInTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        table.setColumnsList(columnsInTable);
        return table;
    }

    private Set<String> removeIrrelevantSchemas(Set<String> schemas, DBModel dbModel){
        List<String> schemaList = new ArrayList<>();
        schemaList.addAll(schemas);
        schemaList.removeAll(Arrays.asList(dbModel.getSchemaExclusions()));
        return new HashSet<>(schemaList);
    }

    private Set<String> removeIrrelevantTables(Set<String> tables, DBModel dbModel){
        List<String> tableList = new ArrayList<>();
        tableList.addAll(tables);
        tableList.removeAll(Arrays.asList(dbModel.getSchemaExclusions()));
        return new HashSet<>(tableList);
    }

    /**
     * Completes data regarding primary keys and foreign keys in a table
     * @param table
     * @param columns
     * @return List of columns with updated info regarding primary keys and foreign keys
     */
    private List<ColumnData> updateTableConstraints(TableData table, List<ColumnData> columns){
        //update with foreign key constraints
        columns = updateForeignKeyInfo(table, columns);

        // update with  primary key(s) constraints
        columns = updatePrimaryKeyInfo(table, columns);
        return columns;
    }

    private List<ColumnData> updateForeignKeyInfo(TableData table, List<ColumnData> columns){
        String query = "select * from "+ table.getDB().getCatalogName()+"."+METADATA_VIEW_SCHEMA_NAME+"."+METADATA_VIEW_FOREIGN_KEY_NAME + " where " + METADATA_VIEW_SCHEMA + " = '"+table.getSchemaName()
                + "' and " + METADATA_VIEW_TABLE +" = '" + table.getTableName() + "'";
        try {
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);

            //each row is a column in the table with a foreign key referencing another table
            while (res.next()) {
                //get the column of this table that is a foreign key
                String columnName = res.getString(METADATA_VIEW_COLUMN);
                ColumnData columnToUpdate = null;
                for (ColumnData col : columns){
                    if (col.getName().equals(columnName)){
                        columnToUpdate = col;
                        break;
                    }
                }
                if (columnToUpdate == null){
                    //there is no column
                    return columns;
                }
                else{
                    columns.remove(columnToUpdate);
                }
                String referencedSchema = res.getString(METADATA_VIEW_REFERENCED_SCHEMA);
                String referencedTable = res.getString(METADATA_VIEW_REFERENCED_TABLE);
                String referencedColumn = res.getString(METADATA_VIEW_REFERENCED_COLUMN);
                //TODO: review. It is assumed that referenced table is in same database!
                //in the form database.schema.table.column
                String referencedKeyFullPath = table.getDB().getCatalogName()+"."+referencedSchema+"."+referencedTable+"."+referencedColumn;//could use id, but would be tricky because of order of insertion (referenced table might not have been insetrted)
                columnToUpdate.setForeignKey(referencedKeyFullPath);
                columns.add(columnToUpdate);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Schema "+ METADATA_VIEW_SCHEMA_NAME+" does not exist")){
                JOptionPane.showMessageDialog(null,
                        "The necessary schema '"+METADATA_VIEW_FOREIGN_KEY_NAME+"' is not created in the database "+table.getDB().getDbName()+". Please create it before moving on.",
                        "Schema "+METADATA_VIEW_FOREIGN_KEY_NAME+" not found",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }
        return columns;
    }

    private List<ColumnData> updatePrimaryKeyInfo(TableData table, List<ColumnData> columns){
        String query = "select * from "+ table.getDB().getCatalogName()+"."+METADATA_VIEW_SCHEMA_NAME+"."+METADATA_VIEW_PRIMARY_KEY_NAME + " where " + METADATA_VIEW_PRIMARY_SCHEMA + " = '"+table.getSchemaName()
                + "' and " + METADATA_VIEW_PRIMARY_TABLE +" = '" + table.getTableName() + "'";
        try {
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(query);

            //each row is a primary key in this table
            while (res.next()) {
                //get the column of this table that is a foreign key
                String columnName = res.getString(METADATA_VIEW_COLUMN);
                ColumnData columnToUpdate = null;
                for (ColumnData col : columns){
                    if (col.getName().equals(columnName)){
                        columnToUpdate = col;
                    }
                }
                if (columnToUpdate == null){
                    //there is no column
                    return columns;
                }
                else{
                    columns.remove(columnToUpdate);
                }
                columnToUpdate.setPrimaryKey(true);
                columns.add(columnToUpdate);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Schema "+ METADATA_VIEW_SCHEMA_NAME+" does not exist")){
                JOptionPane.showMessageDialog(null,
                        "The necessary schema '"+METADATA_VIEW_PRIMARY_KEY_NAME+"' is not created in the database "+table.getDB().getDbName()+". Please create it before moving on.",
                        "Schema "+METADATA_VIEW_PRIMARY_KEY_NAME+" not found",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        return columns;
    }

    private Set<String> getOneColumnResultQuery(String query, boolean print) throws SQLException {
        Set<String> info = null;
        stmt = conn.createStatement();
        res = stmt.executeQuery(query);

        ImmutableSet.Builder<String> set = ImmutableSet.builder();
        //Extract data from result set
        while (res.next()) {
            set.add(res.getString(1));
        }
        info = set.build();
        Iterator<String> it = info.iterator();
        if (print){
            System.out.println("query: "+query +"\n Results:");
            while (it.hasNext()) {
                System.out.println(it.next());
            }
        }
        return info;
    }

    private List<Map> getColumnsResultQuery(String query, boolean print) throws SQLException {
        stmt = conn.createStatement();
        ResultSet res = stmt.executeQuery(query);
        ResultSetMetaData rsmd = res.getMetaData();
        List<Map> records = new ArrayList<>();
        while (res.next()) {
            Map <String, String> row = new HashMap<>();//store a row from the records. Stores each column in the form (column name -> column value)
            for (int i = 1; i < rsmd.getColumnCount(); i++){
                String name = rsmd.getColumnName(i);
                row.put(name,res.getString(name));
                if (print)
                    System.out.print(name+": " + res.getString(name)+", ");
            }
            records.add(row);
            if (print)
                System.out.println("");
        }
        return records;
    }


    //NOT WORKING!!
    public boolean restartPresto(){
        InputStreamReader input;
        OutputStreamWriter output;

        try {
            //Create the process and start it.
            Process pb = new ProcessBuilder("sudo",  "bash", "-c", PRESTO_BIN+"/launcher" + " -S /bin/cat /etc/sudoers 2>&1").start();
            output = new OutputStreamWriter(pb.getOutputStream());
            input = new InputStreamReader(pb.getInputStream());

            int bytes, tryies = 0;
            char buffer[] = new char[1024];
            while ((bytes = input.read(buffer, 0, 1024)) != -1) {
                if(bytes == 0)
                    continue;
                //Output the data to console, for debug purposes
                String data = String.valueOf(buffer, 0, bytes);
                System.out.println(data);
                // Check for password request
                if (data.contains("[sudo] password")) {
                    // Here you can request the password to user using JOPtionPane or System.console().readPassword();
                    JPasswordField pwd = new JPasswordField(20);
                    int action = JOptionPane.showConfirmDialog(null, pwd,"Enter Password",JOptionPane.OK_CANCEL_OPTION);
                    if(action < 0)JOptionPane.showMessageDialog(null,"Cancel, X or escape key selected");
                    else JOptionPane.showMessageDialog(null,"Your password is "+new String(pwd.getPassword()));
                    char password[] = pwd.getPassword();
                    output.write(password);
                    output.write('\n');
                    output.flush();
                    // erase password data, to avoid security issues.
                    Arrays.fill(password, '\0');
                    tryies++;
                }
            }
            if (tryies > 3){
                return false;
            }
        } catch (IOException ex) { System.err.println(ex); }
        //wait and check if connection with presto is possible every 4 seconds, 3 times
        int nTries = 0;
        while (nTries < 3) {
            try {
                conn.getMetaData();
                return true;
            } catch (SQLException e) {
                nTries++;
            }
        }
        if (nTries >= 3)
            return false;
        else
            return true;
    }

    /**
     *
     * @return true if user confirmed presto restart; false otherwise
     */
    public boolean showRestartPrompt(){
        int n = JOptionPane.showOptionDialog(new JFrame(), "Please, restart presto server, then after it restarted select 'OK'",
                "Presto required to restart", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object[] {"OK", "Cancel"}, JOptionPane.OK_CANCEL_OPTION);

        if (n == JOptionPane.OK_OPTION) {
            return true;
        } else {
            return false;
        }
    }

    public void getLocalTablesQueries(List<TableData> localTables){

    }

}
