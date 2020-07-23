package prestoComm;

import helper_classes.*;

import java.util.List;

public class ManagerFacade {

    PrestoMediator prestoMediator;
    MetaDataManager metaDataManager;

    public ManagerFacade(){
        prestoMediator = new PrestoMediator();
        metaDataManager = new MetaDataManager("my project");
    }

    public void createDatabaseAndConnectToPresto(){
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
    public List<DBData> buildLocalSchema(List<DBData> dbs){
        //insert DB Data
        dbs = metaDataManager.insertDBData(dbs);
        //get information about tables
        for (DBData db : dbs) {
            List<TableData> dbTables = prestoMediator.getTablesInDatabase(db);
            dbTables = metaDataManager.insertTableData(dbTables); //tables updated with their id
            //get information about columns (for each table check information about their columns)
            for (int i = 0; i < dbTables.size(); i++){
                TableData tableUpdatedWithColumns = prestoMediator.getColumnsInTable(dbTables.get(i));
                dbTables.set(i, tableUpdatedWithColumns);
            }
            dbTables = metaDataManager.insertColumnData(dbTables);//columns in tables updates with their id
            db.setTableList(dbTables);
        }

        return dbs;
    }

   /*public void buildGlobalSchemaFromLocalSchema(List<DBData> dbs){
        //tables in SQLITE for global schema already created
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        //Generate the global schema from the local schemas
        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(dbs);
        GlobalSchemaConfigurationV2 schemaConfigurationV2 = new GlobalSchemaConfigurationV2(dbs, globalTables);
        //insert the global tables, global columns in the database and correspondences between local and global columns
        //metaDataManager.insertGlobalSchemaData(globalTables);
    }*/

    public void buildStarSchema(GlobalTableData factTable, List<GlobalTableData> dimTables, List<GlobalColumnData> measures){

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
