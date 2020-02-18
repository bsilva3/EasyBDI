package prestoComm;

import helper_classes.DBData;
import helper_classes.SchemaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args){

        ManagerFacade manager = new ManagerFacade();
        //register databases
        List<DBData> dbDataList = new ArrayList<>();
        manager.beginSetup();
        dbDataList.add(new DBData("http://localhost:27017", DBModel.MongoDB,""));
        dbDataList.add(new DBData("http://localhost:3306", DBModel.MYSQL,"", "bruno", "brunosilva"));
        dbDataList.add(new DBData("http://localhost:deti-lei-2.ua.pt:5432/", DBModel.PostgresSQL,"presto2", "bruno", "brunosilva"));
        dbDataList.add(new DBData("http://localhost:deti-lei-2.ua.pt:5432/", DBModel.PostgresSQL,"presto", "bruno", "brunosilva"));
        manager.setupUpDatabases(dbDataList);

    }
}
