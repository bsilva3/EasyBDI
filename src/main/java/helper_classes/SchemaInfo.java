package helper_classes;

import java.util.List;

//helper class to store for a given schema, the tables and each columns of each table
public class SchemaInfo {
    private String schemaName;
    private List<TableInfo> tables;

    public SchemaInfo(String schemaName, List<TableInfo> tables) {
        this.schemaName = schemaName;
        this.tables = tables;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }
}
