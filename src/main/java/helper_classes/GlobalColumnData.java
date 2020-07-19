package helper_classes;

import java.io.Serializable;
import java.util.*;

public class GlobalColumnData implements Serializable {
    private int columnID;
    private Set<ColumnData> localColumns;
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private String foreignKey; //globalTablename.globalColumnName
    private String orderBy; //just for the query save

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> localCols) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = localCols;
    }

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> localCols, int id) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = localCols;
        this.columnID = id;
    }

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, ColumnData localCol) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = new HashSet<>();
        this.localColumns.add(localCol);
    }

    public GlobalColumnData(ColumnData column) {
        this.name = column.getName();
        this.dataType = column.getDataType();
        this.isPrimaryKey = column.isPrimaryKey();
        this.foreignKey = column.getForeignKey();
        this.localColumns = new HashSet<>();
        this.localColumns.add(column);
    }

    public MappingType getMappingType(){
        Iterator iter = localColumns.iterator();
        ColumnData localCol = (ColumnData) iter.next();
        return localCol.getMapping();
    }

    public boolean isNumeric(){
        String[] numericDatatypes = {"integer", "bigint", "smallint", "tinyint", "double", "real"};
        for (String datatype : numericDatatypes){
            if (this.getDataType().equalsIgnoreCase(datatype))
                return true;
        }
        return false;
    }

    public boolean isForeignKey(){
        if (this.foreignKey == null || this.foreignKey.isEmpty())
            return false;
        return true;
    }

    public int getColumnID() {
        return columnID;
    }

    public void setColumnID(int columnID) {
        this.columnID = columnID;
    }

    public Set<ColumnData> getLocalColumns() {
        return localColumns;
    }

    public void setLocalColumns(Set<ColumnData> localColumns) {
        this.localColumns = localColumns;
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

    public String getDataTypeNoLimit() {
        String datatypeNoLimit =  dataType.split("\\(")[0];
        datatypeNoLimit = datatypeNoLimit.replaceAll("\\s+","");
        return datatypeNoLimit;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Set<TableData> getLocalTables() {
        Set<TableData> tables = new HashSet<>();
        for (ColumnData col : localColumns){
                tables.add(col.getTable());
        }
        return tables;
    }

    public Set<Integer> getLocalTablesIDs(){
        Set<Integer> tableIds = new HashSet<>();
        for (ColumnData c : this.localColumns){
            tableIds.add(c.getTableID());
        }
        return tableIds;
    }

    public boolean correspondenceColumnExist(ColumnData col){
        if (this.localColumns.contains(col))
            return true;
        return false;
    }

    public boolean hasForeignKey(){
        if (this.foreignKey == null || this.foreignKey.isEmpty())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GlobalColumnData{" +
                "columnID=" + columnID +
                ", localColumns=" + localColumns +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", foreignKey='" + foreignKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalColumnData)) return false;
        GlobalColumnData that = (GlobalColumnData) o;
        return getColumnID() == that.getColumnID() &&
                isPrimaryKey() == that.isPrimaryKey() &&
                getName().equals(that.getName()) &&
                getDataType().equals(that.getDataType()) &&
                Objects.equals(isForeignKey(), that.isForeignKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColumnID(), getName(), getDataType(), isPrimaryKey(), isForeignKey());
    }
}
