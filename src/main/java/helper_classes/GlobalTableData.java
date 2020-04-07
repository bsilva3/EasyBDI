package helper_classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalTableData {

    private String tableName;
    private int id;
    //private List<TableData> localTables;
    private List<GlobalColumnData> globalColumnData;
    private List<TableData> localTables;

    public GlobalTableData(String tableName) {
        this.tableName = tableName;
        localTables = new ArrayList<>();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<TableData> getLocalTables() {
        return localTables;
    }

    public List<GlobalColumnData> getGlobalColumnData() {
        return globalColumnData;
    }

    public void setGlobalColumnData(List<GlobalColumnData> globalColumnData) {
        this.globalColumnData = globalColumnData;
    }

    public void setGlobalColumnDataFromLocalColumns(List<ColumnData> columnData) {
        globalColumnData = new ArrayList<>();
        for (ColumnData col: columnData){
            globalColumnData.add(new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), col));
        }
    }

    public void setLocalTables(List<TableData> localTables) {
        this.localTables = localTables;
    }

    public void addLocalTable(TableData table){
        this.localTables.add(table);
    }

    @Override
    public String toString() {
        return "GlobalTableData{" +
                "tableName='" + tableName + '\'' +
                ", id=" + id +
                ", localTables=" + localTables +
                ", globalColumnData=" + globalColumnData +
                '}';
    }
}
