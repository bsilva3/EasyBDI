package prestoComm;

import helper_classes.ColumnData;
import helper_classes.DBData;
import helper_classes.TableData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static prestoComm.Constants.DB_DATA;

public class ManagerFacade {

    PrestoMediator prestoMediator;
    MetaDataManager metaDataManager;

    public ManagerFacade(){
        prestoMediator = new PrestoMediator();
        metaDataManager = new MetaDataManager();
    }

    public void beginSetup(){
        //create local table models
        metaDataManager.createTablesAndFillDBModelData();
        //start presto connector
        boolean isConnected = prestoMediator.createConnection();
        if (isConnected){
            System.out.println("Connection Succesfull to presto");
        }
        else{
            System.out.println("Could not connect to presto.");
            System.exit(0);
        }
    }

    /**using presto, retrieve information about tables in each db, and columns in each table
    * and store in sqlite.
    * It is assumed that DB connection info for presto has already been set up.
     * Will fetch DB Data and store it on SQLITE
     **/
    /*public void buildLocalSchemas(){
        //get databases registered
        prestoMediator.
        prestoMediator.getTablesInDatabase();
    }*/

    /**using presto, retrieve information about tables in each db, and columns in each table
     * and store in sqlite.
     * It is assumed that DB connection info for presto has already been set up
     * AND that database info has already been retrieved
     * return - List of table data, with info regarding its columns and database.
     **/
    public List<TableData> buildLocalSchema(List<DBData> dbs){
        //insert DB Data
        dbs = metaDataManager.insertDBData(dbs);
        //get information about tables
        List<TableData> tables = new ArrayList<>();
        for (DBData db : dbs)
            tables.addAll(prestoMediator.getTablesInDatabase(db));
        dbs.clear();
        //insert tables in database
        tables = metaDataManager.insertTableData(tables);

        //get information about columns (for each table check information about theyr columns)
        for (int i = 0; i < tables.size(); i++){
            TableData tableUpdatedWithColumns = prestoMediator.getColumnsInTable(tables.get(i));
            tables.set(i, tableUpdatedWithColumns);
        }
        tables = metaDataManager.insertColumnData(tables);
        return tables;
    }


    /**
     * Given a database information (url, auth parameters) provided by the user, create presto config files to connect to those databases
     * return - false if presto was not restarted in order to use the new catalogs. True otherwise
     */
    public boolean generatePrestoDBConfigFiles(List<DBData> dbDataList){
        for (DBData db : dbDataList){
            prestoMediator.createDBFileProperties(db);
        }
        return prestoMediator.showRestartPrompt();
    }

    public List<DBData> getAllRegisteredDatabases(){
        return metaDataManager.getDatabases();
    }

    public void printQuery(String query){
        metaDataManager.makeQueryAndPrint(query);
    }
    public void dropTables(){
        metaDataManager.deleteTables();
    }
}
