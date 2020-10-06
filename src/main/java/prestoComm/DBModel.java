package prestoComm;

import java.util.Set;

import static prestoComm.Constants.*;

public enum DBModel {
    PostgreSQL(){
        @Override
        public String getBDDataModel() {
            return "Relational";
        }
        @Override
        public String getConnectorName() {
            return "postgresql";
        }
        @Override
        public int getDefaultPort() {
            return 5432;
        }
        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schema", "pg_stats", "pg_catalog", METADATA_VIEW_SCHEMA_NAME};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return false;
        }
    },
    SQLServer {
        @Override
        public String getBDDataModel() {
            return "Relational";
        }
        @Override
        public String getConnectorName() {
            return "sqlserver";
        }
        @Override
        public int getDefaultPort() {
            return 1433;
        }
        @Override
        public String getMetaDataQuery() {
            return null;
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schemas", METADATA_VIEW_SCHEMA_NAME};
        }//TODO: fill
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return false;
        }
    },
    MYSQL {
        @Override
        public String getBDDataModel() {
            return "Relational";
        }

        @Override
        public String getConnectorName() {
            return "mysql";
        }

        @Override
        public int getDefaultPort() {
            return 3306;
        }

        @Override
        public String getMetaDataQuery() {
            return null;
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schema", "sys", METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return true;
        }
    },
    Redis {
        @Override
        public String getBDDataModel() {
            return "Key-Value";
        }

        @Override
        public String getConnectorName() {
            return "redis";
        }

        @Override
        public int getDefaultPort() {
            return 6379;
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {};
        }//TODO: fill

        @Override
        public String[] getTableExclusions() {
            return new String[] {};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return true;
        }
    },
    Cassandra {
        @Override
        public String getBDDataModel() {
            return "Column";
        }
        @Override
        public String getConnectorName() {
            return "cassandra";
        }
        @Override
        public int getDefaultPort() {
            return 9042;
        }
        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schema","system","system_auth","system_distributed","system_schema","system_traces"};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return true;
        }
    },
    MongoDB {
        @Override
        public String getBDDataModel() {
            return "Document";
        }

        @Override
        public String getConnectorName() {
            return "mongodb";
        }

        @Override
        public int getDefaultPort() {
            return 27017;
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"admin", "config"};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {"views", "tables", "table_privileges", "schemata", "columns", "roles", "enabled_roles", "vies", "applicable_roles", "startup_log"};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return true;
        }
    },

    HIVE {
        @Override
        public String getBDDataModel() {
            return "HDFS";
        }

        @Override
        public String getConnectorName() {
            return "hive-hadoop2";
        }

        @Override
        public int getDefaultPort() {
            return 9083;
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schema"};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {};
        }
        @Override
        public boolean isSingleServerOneDatabase(){
            return true;
        }
    };

    public boolean isRelational(){
        if (this.getBDDataModel().equalsIgnoreCase("relational")){
            return true;
        }
        return false;
    }

    public abstract String getBDDataModel();

    public abstract String getMetaDataQuery();

    public abstract String getConnectorName();

    public abstract int getDefaultPort();

    public abstract String[] getSchemaExclusions();

    public abstract String[] getTableExclusions();

    public abstract boolean isSingleServerOneDatabase();

    //public abstract String[] getTableExclusions();

}
