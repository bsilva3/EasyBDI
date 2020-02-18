package prestoComm;

import java.io.File;

public class Constants {

    //Presto main dir location Static for now
    //TODO: add a config file that shows the location of presto instalation
    public static final String PRESTO_DIR = "/home/bruno/Desktop/presto-server-0.329";
    public static final String PRESTO_PROPERTIES_FOLDER = PRESTO_DIR + File.separator + "etc" + File.separator + "catalog"+File.separator;

    //PostgresSQL user details
    public static final String PSQL_USER = "postgres";
    public static final String PSQL_PASS = "brunosilva";

    //SQLITE
    public static final String SQLITE_DB = "metadataBD";
    //tables for SQLITE
    public static final String DB_DATA = "db_data";
    public static final String DB_TYPE_DATA = "db_type_data";
    public static final String TABLE_DATA = "table_data";
    public static final String COLUMN_DATA = "column_data";
}
