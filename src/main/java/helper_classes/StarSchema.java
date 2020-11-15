package helper_classes;

import helper_classes.FactsTable;
import helper_classes.GlobalTableData;

import java.util.List;

/**
 * Represents a star schema, with a name, id, a facts table and list of dimensions
 */
public class StarSchema {

    private int cubeID;
    private String schemaName;
    private FactsTable factsTable;
    private List<GlobalTableData> dimsTables;

    public StarSchema(String schemaName, FactsTable factsTable, List<GlobalTableData> dimsTables) {
        this.schemaName = schemaName;
        this.factsTable = factsTable;
        this.dimsTables = dimsTables;
    }

    public StarSchema(String schemaName, int cubeID, FactsTable factsTable, List<GlobalTableData> dimsTables) {
        this.schemaName = schemaName;
        this.cubeID = cubeID;
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

    public int getCubeID() {
        return this.cubeID;
    }

    public void setCubeID(int cubeID) {
        this.cubeID = cubeID;
    }
}
