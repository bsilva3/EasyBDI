package helper_classes;

import java.util.Map;

public class Match {

    private TableData tableData1;
    private TableData tableData2;
    private Map<ColumnData, ColumnData> columnMatches; //key is columns from first table, value is columns from second table

    public Match(TableData tableData1, TableData tableData2){
        this.tableData1 = tableData1;
        this.tableData2 = tableData2;
    }

    public TableData getTableData1() {
        return tableData1;
    }

    public void setTableData1(TableData tableData1) {
        this.tableData1 = tableData1;
    }

    public TableData getTableData2() {
        return tableData2;
    }

    public void setTableData2(TableData tableData2) {
        this.tableData2 = tableData2;
    }

    public Map<ColumnData, ColumnData> getColumnMatches() {
        return columnMatches;
    }

    public void setColumnMatches(Map<ColumnData, ColumnData> columnMatches) {
        this.columnMatches = columnMatches;
    }
}
