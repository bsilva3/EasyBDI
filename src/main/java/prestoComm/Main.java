package prestoComm;

import helper_classes.DBData;
import helper_classes.TableData;

import java.util.ArrayList;
import java.util.List;

import static prestoComm.Constants.*;

public class Main {

    public static void main(String[] args){

        ManagerFacade manager = new ManagerFacade();
        //register databases
        List<DBData> dbDataList = new ArrayList<>();
        //manager.dropTables();

        manager.beginSetup();
        System.out.println("DB types registered:");
        manager.printQuery("SELECT * FROM "+ DB_TYPE_DATA);
        //insert manually DBs
        //dbDataList.add(new DBData("http://localhost:27017", DBModel.MongoDB,""));
        dbDataList.add(new DBData("http://localhost:3306", DBModel.MYSQL,"", "bruno", "brunosilva"));
        dbDataList.add(new DBData("http://deti-lei-2.ua.pt:5432/", DBModel.PostgreSQL,"presto2", "bruno", "brunosilva"));
        dbDataList.add(new DBData("http://deti-lei-2.ua.pt:5432/", DBModel.PostgreSQL,"presto", "bruno", "brunosilva"));
        boolean success = manager.generatePrestoDBConfigFiles(dbDataList);
        if (!success){
            System.exit(1);
        }
        //manager.prestoMediator.makeQuery("SHOW CATALOGS");
        List<TableData> tables = manager.buildLocalSchema(dbDataList);

        SchemaMatcher schemaMatcher = new SchemaMatcher();
        schemaMatcher.fillTableColumnDataForSchemaMatching(tables);

        System.out.println("databases registered:");
        manager.printQuery("SELECT * FROM "+ DB_DATA);

        System.out.println("Tables registered:");
        manager.printQuery("SELECT * FROM "+ TABLE_DATA);

        System.out.println("Columns registered:");
        manager.printQuery("SELECT * FROM "+ COLUMN_DATA);
    }
}
