package prestoComm;

import helper_classes.DBData;

import java.util.List;

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

    /*
    Given a database information (url, auth parameters) provided by the user, create presto config files to connect to those databases
    and store this info in SQLite
     */
    public void setupUpDatabases(List<DBData> dbDataList){
        for (DBData db : dbDataList){
            prestoMediator.createDBFileProperties(db);
        }
        metaDataManager.insertDBData(dbDataList);
    }

    public void getAllRegisteredDatabases(){

    }
}
