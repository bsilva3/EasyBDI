package prestoComm;

import helper_classes.DBData;
import helper_classes.GlobalTableData;
import helper_classes.TableData;

import java.util.ArrayList;
import java.util.List;

import static prestoComm.Constants.*;

public class MainTest {

    public static void main(String[] args){

        MetaDataManager m = new MetaDataManager("minho dataset");
        //m.deleteTablesToSaveQueries();
        //m.createTables();
        //manager.dropTables();

        /*manager.createDatabaseAndConnectToPresto();
        System.out.println("DB types registered:");
        manager.printQuery("SELECT * FROM "+ DB_TYPE_DATA);*/
        //insert manually DBs
        //dbDataList.add(new DBData("http://localhost:27017", DBModel.MongoDB,""));
        //dbDataList.add(new DBData("http://localhost:3306", DBModel.MYSQL,"", "bruno", "brunosilva"));
        //dbDataList.add(new DBData("http://deti-lei-2.ua.pt:5432/", DBModel.PostgreSQL,"presto2", "bruno", "brunosilva"));
        //dbDataList.add(new DBData("http://deti-lei-2.ua.pt:5432/", DBModel.PostgreSQL,"presto", "bruno", "brunosilva"));
        //dbDataList.add(new DBData("http://localhost:5432/", DBModel.PostgreSQL,"employees_vertical", "postgres", "brunosilva"));
        //dbDataList.add(new DBData("http://localhost:5432/", DBModel.PostgreSQL,"employees_horizontal", "postgres", "brunosilva"));
        //dbDataList.add(new DBData("http://localhost:3306/", DBModel.MYSQL,"employees_horizontal", "bruno", "brunosilva"));
        //dbDataList.add(new DBData("http://localhost:3306/", DBModel.MYSQL, METADATA_VIEW_SCHEMA_NAME, "bruno", "brunosilva"));
        /*boolean success = manager.generatePrestoDBConfigFiles(dbDataList);
        if (!success){
            System.exit(1);
        }
        //manager.prestoMediator.makeQuery("SHOW CATALOGS");
        dbDataList = manager.buildLocalSchema(dbDataList);*/

        /*System.out.println("databases registered:");
        m.makeQueryAndPrint("SELECT * FROM "+ DB_DATA);

        System.out.println("Tables registered:");
        m.makeQueryAndPrint("SELECT * FROM "+ TABLE_DATA);

        System.out.println("Columns registered:");
        m.makeQueryAndPrint("SELECT * FROM "+ COLUMN_DATA);

        //manager.buildGlobalSchemaFromLocalSchema(dbDataList);

        System.out.println("--------------- Global Schema ------------------");
        System.out.println("global tables registered:");
        m.makeQueryAndPrint("SELECT * FROM "+ GLOBAL_TABLE_DATA);

        System.out.println("--------------- Cubes ------------------");
        System.out.println("Cubes created:");
        m.makeQueryAndPrint("SELECT * FROM "+ CUBE_TABLE);*/

        /*m.makeQueryAndPrint("SELECT \n" +
                "    name\n" +
                "FROM \n" +
                "    sqlite_master \n" +
                "WHERE \n" +
                "    type ='table' AND \n" +
                "    name NOT LIKE 'sqlite_%';");*/

        m.makeQueryAndPrint("ALTER TABLE QUERY_FILTERS ADD COLUMN QUERY_COL_FILTERS_OBJ blob;");

        //m.makeQueryAndPrint("SELECT * FROM "+ TABLE_DATA);
        /*m.makeQueryAndPrint("UPDATE "+DB_DATA+"\n" +
                "   SET name = 'Social Media Database' \n" +
                " WHERE id = '1' ;");
        m.makeQueryAndPrint("UPDATE "+DB_DATA+"\n" +
                "   SET name = 'Retail Database' \n" +
                " WHERE id = '2' ;");*/


    }
}
