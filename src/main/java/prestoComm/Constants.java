package prestoComm;

import java.io.File;

public class Constants {

    //Presto main dir location Static for now
    //TODO: add a config file that shows the location of presto instalation
    public static final String PRESTO_DIR = "/home/bruno/Desktop/presto-server-330";
    public static final String PRESTO_BIN = PRESTO_DIR + File.separator + "bin";
    public static final String PRESTO_PROPERTIES_FOLDER = PRESTO_DIR + File.separator + "etc" + File.separator + "catalog"+File.separator;

    //files
    public static final String FILES_DIR = "files";
    public static final String DATATYPE_CONVERTIBLES_DICT = FILES_DIR + File.separator + "convertible_datatypes.csv";

    //SQLITE
    public static final String SQLITE_DB = "metadataBD";
    public static final String SQLITE_DB_FOLDER = "metadata";
    // ---------tables and columns for SQLITE--------------

    public static final String ID_FIELD = "id";

    //Table to store info about types of databases
    public static final String DB_TYPE_DATA = "db_type_data";
    //Columns
    public static final String DB_TYPE_NAME_FIELD = "db_name";
    public static final String DB_TYPE_MODEL_FIELD = "db_model_type";
    public static final String DB_TYPE_QUERY_FIELD = "catalog_access_query";


    //Table for the database data storage
    public static final String DB_DATA = "db_data";
    //columns
    public static final String DB_DATA_NAME_FIELD = "name";
    public static final String DB_DATA_SERVER_FIELD = "server_url";
    public static final String DB_DATA_USER_FIELD = "user";
    public static final String DB_DATA_PASS_FIELD = "pass";
    public static final String DB_DATA_TYPE_ID_FIELD = "db_type_id";


    //table for the table data storage
    public static final String TABLE_DATA = "table_data";
    //columns
    public static final String TABLE_DATA_NAME_FIELD = "name";
    public static final String TABLE_DATA_SCHEMA_NAME_FIELD = "schema_name";
    public static final String TABLE_DATA_DB_ID_FIELD = "db_id";

    //table for the column data storage
    public static final String COLUMN_DATA = "column_data";
    //columns in this table
    public static final String COLUMN_DATA_NAME_FIELD = "name";
    public static final String COLUMN_DATA_TYPE_FIELD = "data_type";
    public static final String COLUMN_DATA_IS_PRIMARY_KEY_FIELD = "is_primary_key";
    public static final String COLUMN_DATA_FOREIGN_KEY_FIELD = "foreign_key_reference";
    public static final String COLUMN_DATA_TABLE_RELATION_FIELD = "table_relation";
    public static final String COLUMN_DATA_TABLE_FIELD = "table_id";

    //Presto SHOW COLUMNS FROM column names of query result
    public static final String SHOW_COLS_COLUMN = "Column";
    public static final String SHOW_COLS_TYPE = "Type";

    //Schema name that contains the views with info regarding table constraints
    public static final String METADATA_VIEW_SCHEMA_NAME = "constraint_data_schema";
    //View name that must be created in each database to get information about foreign key relations
    public static final String METADATA_VIEW_FOREIGN_KEY_NAME = "foreign_keys_relations";
    public static final String METADATA_VIEW_SCHEMA = "TABLE_SCHEMA";
    public static final String METADATA_VIEW_TABLE = "TABLE_NAME";
    public static final String METADATA_VIEW_COLUMN = "COLUMN_NAME";
    public static final String METADATA_VIEW_REFERENCED_SCHEMA = "REFERENCED_TABLE_SCHEMA";
    public static final String METADATA_VIEW_REFERENCED_TABLE = "REFERENCED_TABLE_NAME";
    public static final String METADATA_VIEW_REFERENCED_COLUMN = "REFERENCED_COLUMN_NAME";

    public static final String METADATA_VIEW_PRIMARY_KEY_NAME = "primary_keys";
    public static final String METADATA_VIEW_PRIMARY_SCHEMA = "TABLE_SCHEMA";
    public static final String METADATA_VIEW_PRIMARY_TABLE = "TABLE_NAME";
    public static final String METADATA_VIEW_PRIMARY_COLUMN = "COLUMN_NAME";



    //SCHEMA exclusions that are database characteristic and have no interest in a business analytics context
    public static final String[] PSQL_SCHEMA_EXCL = new String[] {"schema_info", "pg_catalog"};
    public static final String[] MYSQL_SCHEMA_EXCL = new String[] {"schema_info", "pg_catalog"};
    public static final String[] SQLSERVER_SCHEMA_EXCL = new String[] {"schema_info", "pg_catalog"};
    public static final String[] MONGODB_SCHEMA_EXCL = new String[] {"admin"};

}
