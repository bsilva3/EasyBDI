package prestoComm;

public enum DBModel {
    PostgresSQL(){
        @Override
        public String getBDDataModel() {
            return "Relational";
        }
        @Override
        public String getMetaDataQuery() {
            return null;
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
    },
    Redis {
        @Override
        public String getBDDataModel() {
            return "Key-Value";
        }

        @Override
        public String getMetaDataQuery() {
            return "Key-Value";
        }
    },
    Cassandra {
        @Override
        public String getBDDataModel() {
            return "Column";
        }

        @Override
        public String getMetaDataQuery() {
            return null;
        }
    },
    MongoDB {
        @Override
        public String getBDDataModel() {
            return "Document";
        }

        @Override
        public String getMetaDataQuery() {
            return null;
        }
    };

    public abstract String getBDDataModel();

    public abstract String getMetaDataQuery();

}
