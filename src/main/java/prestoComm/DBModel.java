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
        public String getMetaDataQuery() {
            return "show columns in ";
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schemas", "pg_stats", METADATA_VIEW_SCHEMA_NAME};
        }
    },
    SQLServer {
        @Override
        public String getBDDataModel() {
            return "Relational";
        }
        @Override
        public String getMetaDataQuery() {
            return null;
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schemas", METADATA_VIEW_SCHEMA_NAME};
        }//TODO: fill
    },
    MYSQL {
        @Override
        public String getBDDataModel() {
            return "Relational";
        }

        @Override
        public String getMetaDataQuery() {
            return null;
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"information_schema", "sys", METADATA_VIEW_SCHEMA_NAME};
        }//TODO: fill
    },
    Redis {
        @Override
        public String getBDDataModel() {
            return "Key-Value";
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {};
        }//TODO: fill
    },
    Cassandra {
        @Override
        public String getBDDataModel() {
            return "Column";
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }
        @Override
        public String[] getSchemaExclusions() {
            return new String[] {};
        }//TODO: fill
    },
    MongoDB {
        @Override
        public String getBDDataModel() {
            return "Document";
        }

        @Override
        public String getMetaDataQuery() {
            return "show columns in ";
        }

        @Override
        public String[] getSchemaExclusions() {
            return new String[] {"admin", "config"};
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

    public abstract String[] getSchemaExclusions();

    //public abstract String[] getTableExclusions();

}
