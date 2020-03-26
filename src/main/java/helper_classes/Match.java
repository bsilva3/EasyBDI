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

    public boolean tableInMatch(TableData t){
        if (t.equals(this.tableData1)){
            return true;
        }
        else if (t.equals(this.tableData2)){
            return true;
        }
        return false;
    }

    /**
     * Given a table, return the other table that make this match object. If user gives as an argument table 1, table 2 is returned. If user Gives table 2, table 1 is returned,
     * If user gives any other table, null is returned. This is usefull to find all matches of a given table.
     * @param t - table
     * @return the other table that matches the given table in this object, or null if the given table does not exist in this object
     */
    public TableData getOtherTable(TableData t){
        if (t.equals(this.tableData1))
            return this.tableData2;
        else if (t.equals(this.tableData2))
            return this.tableData1;
        return null;
    }
}
