package prestoComm;

import com.facebook.presto.jdbc.internal.guava.collect.ImmutableSet;
import helper_classes.ColumnInfo;
import helper_classes.SchemaInfo;
import helper_classes.TableInfo;

import java.sql.*;
import java.util.*;

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

    }

    private void createConnection(){
        //Open a connection
        conn = prestoConnector.setConnection();
    }


    public void makeQuery(String query){
        Statement stmt = null;
        createConnection();//or should this be done outside?
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

            }
            //Clean-up environment
            prestoConnector.closeConn();
            res.close();
            stmt.close();
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

    //given a list of catalogs (DBs), returns info about schemas, tables and columns in each one of them
    public Map<String, List<SchemaInfo>> getDBData(List<String> catalogs){
        stmt = null;
        res = null;
        createConnection();//or should this be done outside?
        Set<String> info = null;
        Map<String, List<SchemaInfo>> dbsInfo = new HashMap<>();//for each catalog (aka db), store info about schemas, tables and columns
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // for every catalog, get the schemas
            for (String catalog : catalogs) {
                Set <String> schemasInCatalog = getOneColumnResultQuery("show schemas from "+catalog, true);
                List<SchemaInfo> schemaInfos = new ArrayList<>();
                //for every schema, obtain tables
                for (String schema : schemasInCatalog){
                    Set <String> tablesInSchema = getOneColumnResultQuery("show tables from "+ catalog+"."+schema, true);
                    List<TableInfo> tableInfos = new ArrayList<>();
                    //for every table, obtain column information
                    for (String table : tablesInSchema){
                        //diferent!! more than one column
                        List <Map> columnsInTable = getColumnsResultQuery("describe "+catalog+"."+schema+".\""+table+"\"", true);
                        List<ColumnInfo> columnInfos = new ArrayList<>();
                        for (Map entry : columnsInTable){
                            //a column of the table in the form (column name -> column value)
                            columnInfos.add(new ColumnInfo(entry.get("Column").toString(), entry.get("Type").toString()));
                        }
                        TableInfo tableInfo = new TableInfo(table, columnInfos);
                        tableInfos.add(tableInfo);
                    }
                    SchemaInfo schemaInfo = new SchemaInfo(schema, tableInfos);
                    schemaInfos.add(schemaInfo);
                }
                dbsInfo.put(catalog, schemaInfos);
            }
            //Clean-up environment
            prestoConnector.closeConn();
            res.close();
            stmt.close();
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
        return dbsInfo;
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
        Set<String> info = null;
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



}
