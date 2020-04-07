package helper_classes;

import java.util.HashSet;
import java.util.Set;

public class GlobalColumnData {
    private int columnID;
    private Set<ColumnData> localColumns;
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private String foreignKey;

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> localCols) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = localCols;
    }

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, ColumnData localCol) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = new HashSet<>();
        this.localColumns.add(localCol);
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
}
