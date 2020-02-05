package helper_classes;

import java.util.List;

//helper class. For a given table, shows its name, columns and information of each column
public class TableInfo {

    private String tableName;
    private List<ColumnInfo> columns;

    public TableInfo(String tableName, List<ColumnInfo> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }
}
