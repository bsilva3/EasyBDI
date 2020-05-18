package helper_classes;

import java.util.List;

public class StarSchema {

    private String schemaName;
    private FactsTable factsTable;
    private List<GlobalTableData> dimsTables;

    public StarSchema(String schemaName, FactsTable factsTable, List<GlobalTableData> dimsTables) {
        this.schemaName = schemaName;
        this.factsTable = factsTable;
        this.dimsTables = dimsTables;
    }

    public String getSchemaName() {
        return this.schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public FactsTable getFactsTable() {
        return this.factsTable;
    }

    public void setFactsTable(FactsTable factsTable) {
        this.factsTable = factsTable;
    }

    public List<GlobalTableData> getDimsTables() {
        return this.dimsTables;
    }

    public void setDimsTables(List<GlobalTableData> dimsTables) {
        this.dimsTables = dimsTables;
    }
}
