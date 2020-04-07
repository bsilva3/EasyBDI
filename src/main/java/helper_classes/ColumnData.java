package helper_classes;

import java.util.*;

//Helper class. Shows info about a column (data type, name..)
public class ColumnData {
    private int columnID;
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private TableData table;
    private String foreignKey;
    private String tableRelation;
    private Set<ColumnData> mergedColumnIds; //used for schema integration
    private int tableID;

    public ColumnData(int id, String name, String dataType, TableData table) {
        this.columnID = id;
        this.name = name;
        this.dataType = dataType;
        this.table = table;
        this.isPrimaryKey = false;
        mergedColumnIds = new HashSet<>();
    }

    public ColumnData(String name, String dataType, boolean isPrimaryKey) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.mergedColumnIds = new HashSet<>();
    }

    public ColumnData(String name, String dataType, TableData table) {
        this.name = name;
        this.dataType = dataType;
        this.table = table;
        this.mergedColumnIds = new HashSet<>();
    }

    public ColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> mergedColumnIds) {
        this(name, dataType, isPrimaryKey);
        this.mergedColumnIds = mergedColumnIds;
    }

    public ColumnData(int columnID, String name, String dataType, boolean isPrimaryKey, TableData table, String foreignKey, String tableRelation) {
        this.columnID = columnID;
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.table = table;
        this.foreignKey = foreignKey;
        this.tableRelation = tableRelation;
        mergedColumnIds = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getColumnID() {
        return columnID;
    }

    public void setColumnID(int columnID) {
        this.columnID = columnID;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public TableData getTable() {
        return table;
    }

    public void setTable(TableData table) {
        this.table = table;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getTableRelation() {
        return tableRelation;
    }

    public void setTableRelation(String tableRelation) {
        this.tableRelation = tableRelation;
    }

    public Set<ColumnData> getMergedColumns() {
        mergedColumnIds.add(this);
        return mergedColumnIds;
    }

    public void setMergedColumnIds(Set<ColumnData> mergedColumns) {
        this.mergedColumnIds = mergedColumns;
    }

    public void addMergedColumnId(ColumnData cols){
        this.mergedColumnIds.add(cols);
    }

    public void addMergedColumnId(Collection<ColumnData> cols){
        this.mergedColumnIds.addAll(cols);
    }

    public int getTableID() {
        return tableID;
    }

    public void setTableID(int tableID) {
        this.tableID = tableID;
    }

    @Override
    public String toString() {
        return "ColumnData{" +
                "columnID=" + columnID +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                //", table=" + table +
                ", foreignKey='" + foreignKey + '\'' +
                ", tableRelation='" + tableRelation + '\'' +
                '}';
    }
}
