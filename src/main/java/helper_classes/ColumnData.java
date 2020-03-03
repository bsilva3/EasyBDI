package helper_classes;

//Helper class. Shows info about a column (data type, name..)
public class ColumnData {
    private int columnID;
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private TableData table;
    private String foreignKey;
    private String tableRelation;

    public ColumnData(String name, String dataType, TableData table) {
        this.name = name;
        this.dataType = dataType;
        this.table = table;
        this.isPrimaryKey = false;
    }

    public ColumnData(int columnID, String name, String dataType, boolean isPrimaryKey, TableData table, String foreignKey, String tableRelation) {
        this.columnID = columnID;
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.table = table;
        this.foreignKey = foreignKey;
        this.tableRelation = tableRelation;
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

    @Override
    public String toString() {
        return "ColumnData{" +
                "columnID=" + columnID +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", table=" + table +
                ", foreignKey='" + foreignKey + '\'' +
                ", tableRelation='" + tableRelation + '\'' +
                '}';
    }
}
