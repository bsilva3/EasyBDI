package prestoComm;

import helper_classes.SchemaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args){

        PrestoMediator connector = new PrestoMediator();
        //connector.getTableData("select * from mongodb.products.products");
        //connector.makeQuery("describe mongodb.products.products");
        //connector.getDBData("mongodb");

        TableDataManager tableDataManager = new TableDataManager(Constants.PSQL_USER, Constants.PSQL_PASS);
        //tableDataManager.createSchemaForDBSchema("", "");

        //initial catalogs
        List<String> catalogs = new ArrayList<>();
        catalogs.add("mongodb");

        //for every catalog, get information about their schemas, table and columns and store it in postgres
        Map<String, List<SchemaInfo>> dbsInfo = connector.getDBData(catalogs);

        for (Map.Entry<String, List<SchemaInfo>> entry : dbsInfo.entrySet()){
            tableDataManager.createEntriesForDB(entry.getKey(), entry.getValue());//store metadata about this DB in postgres
        }

    }
}
