package helper_classes;

import jdk.nashorn.internal.objects.Global;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalTableData implements Serializable {

    private String tableName;
    private int id;
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

    public Set<TableData> getLocalTablesFromCols() {
        Set<TableData> tables = new HashSet<>();
        for (GlobalColumnData gc : this.globalColumnData){
            for (ColumnData c : gc.getLocalColumns())
                tables.add(c.getTable());
        }
        return tables;
    }

    public MappingType getMappingTypeOfMatches(){//same for all matches, therefore, check only a column
        return globalColumnData.get(0).getLocalColumns().iterator().next().getMapping();
    }

    /**
     * Get all local tables that match with this global table, but return only local columns in the local table that match with the
     * specified global cols
     * @param cols
     * @return
     */
    public Set<TableData> getLocalTablesFromColsSelectedCols(List<GlobalColumnData> cols) {
        Set<TableData> tables = new HashSet<>();
        for (GlobalColumnData gc : this.globalColumnData){
            if (cols.contains(gc)) {
                for (ColumnData c : gc.getLocalColumns())
                    tables.add(c.getTable());
            }
        }
        return tables;
    }

    public Set<Integer> getLocalTablesIDs() {
        Set<Integer> tablesIDS = new HashSet<>();
        for (GlobalColumnData gc : this.globalColumnData){
            for (ColumnData c : gc.getLocalColumns())
                tablesIDS.add(c.getTableID());
        }

        return tablesIDS;
    }


    public List<GlobalColumnData> getGlobalColumnDataList() {
        return globalColumnData;
    }

    public GlobalColumnData getGlobalColumnData(String colName) {
        for (GlobalColumnData globalColumn: globalColumnData){
            if (globalColumn.getName().equals(colName)){
                return globalColumn;
            }
        }
        return null;
    }

    public void setGlobalColumnData(List<GlobalColumnData> globalColumnData) {
        this.globalColumnData = globalColumnData;
    }

    public void setGlobalColumnDataFromLocalColumns(List<ColumnData> columnData) {
        globalColumnData = new ArrayList<>();
        for (ColumnData col: columnData){
            GlobalColumnData globCol = new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), col);
            globCol.setForeignKey(col.getForeignKey());
            globalColumnData.add(globCol);
        }
    }

    public List<ColumnData> getFullListColumnsCorrespondences(){
        List<ColumnData> fullLocalCols = new ArrayList<>();
        for (GlobalColumnData globalCol : this.globalColumnData){
            fullLocalCols.addAll(globalCol.getLocalColumns());
        }
        return fullLocalCols;
    }

    public void setLocalTables(List<TableData> localTables) {
        this.localTables = localTables;
    }

    public GlobalColumnData getGlobalColContainingLocalColAsCorrespondence(ColumnData col){
        for (GlobalColumnData gc : this.globalColumnData){
            if (gc.getLocalColumns().contains(col))
                return gc;
        }
        return null;
    }

    public void addLocalTable(TableData table){
        this.localTables.add(table);
    }

    public void addGlobalColumn(GlobalColumnData col){
        try {
            this.globalColumnData.add(col);
        } catch (NullPointerException e){
            this.globalColumnData = new ArrayList<>();
            this.globalColumnData.add(col);
        }
    }

    public int getNCols (){
        return this.globalColumnData.size();
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
