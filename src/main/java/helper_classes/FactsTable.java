package helper_classes;

import java.util.Map;

public class FactsTable {

    private int id;
    private int cubeID;
    private GlobalTableData globalTable;
    private Map<GlobalColumnData, Boolean> columns;

    public FactsTable(GlobalTableData globalTable, Map<GlobalColumnData, Boolean> columns) {
        this.globalTable = globalTable;
        this.columns = columns;
    }

    public FactsTable(int id, int cubeID, GlobalTableData globalTable, Map<GlobalColumnData, Boolean> columns) {
        this.id = id;
        this.cubeID = cubeID;
        this.globalTable = globalTable;
        this.columns = columns;
    }

    public boolean isColumnMeasurement(GlobalColumnData colToCheck){
        for (Map.Entry<GlobalColumnData,Boolean> entry : columns.entrySet()){
            GlobalColumnData col = entry.getKey();
            if (col.equals(colToCheck))
                return entry.getValue();//return if this column is or not a measurement
        }
        return false;
    }

    public GlobalTableData getGlobalTable() {
        return this.globalTable;
    }

    public void setGlobalTable(GlobalTableData globalTable) {
        this.globalTable = globalTable;
    }

    public Map<GlobalColumnData, Boolean> getColumns() {
        return this.columns;
    }

    public void setColumns(Map<GlobalColumnData, Boolean> columns) {
        this.columns = columns;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
