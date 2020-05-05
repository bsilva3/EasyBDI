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
            return new String[] {"information_schema", "pg_stats", "pg_catalog", METADATA_VIEW_SCHEMA_NAME};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
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
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
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
            return new String[] {"information_schema", "sys", METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
        @Override
        public String[] getTableExclusions() {
            return new String[] {METADATA_VIEW_SCHEMA_NAME, METADATA_VIEW_FOREIGN_KEY_NAME, METADATA_VIEW_PRIMARY_KEY_NAME};
        }
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

        @Override
        public String[] getTableExclusions() {
            return new String[] {};
        }
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
        @Override
        public String[] getTableExclusions() {
            return new String[] {};
        }
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
        @Override
        public String[] getTableExclusions() {
            return new String[] {};
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

    public abstract String[] getTableExclusions();

    //public abstract String[] getTableExclusions();

}
