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

    public static class Builder {
        private int columnID;
        private String name;
        private String dataType;
        private boolean isPrimaryKey;
        private TableData table;
        private String foreignKey;
        private Set<ColumnData> mergedColumnIds; //used for schema integration
        private int tableID;

        public Builder (String name, String dataType, boolean isPrimaryKey){
            this.name = name;
            this.dataType = dataType;
            this.isPrimaryKey = isPrimaryKey;
        }

        public Builder (String name, String dataType){
            this.name = name;
            this.dataType = dataType;
        }

        public Builder withPrimaryKey(boolean isPrimaryKey){
            this.isPrimaryKey = isPrimaryKey;
            return this;
        }


        public Builder withID(int id){
            this.columnID = id;
            return this;
        }

        public Builder withTable(TableData table){
            this.table = table;
            return this;
        }

        public Builder withTableID(int tableID){
            this.tableID = tableID;
            return this;
        }

        public Builder withForeignKey(String foreignKey){
            this.foreignKey = foreignKey;
            return this;
        }

        public Builder withMergedCols(Set<ColumnData> mergedColumnIds){
            this.mergedColumnIds = mergedColumnIds;
            return this;
        }

        public ColumnData build(){
            ColumnData col = new ColumnData();
            col.columnID = this.columnID;
            col.dataType = this.dataType;
            col.isPrimaryKey = this.isPrimaryKey;
            col.name = this.name;
            col.table = this.table;
            col.tableID = this.tableID;
            col.foreignKey = this.foreignKey;
            col.mergedColumnIds = this.mergedColumnIds;
            return col;
        }

    }

    public ColumnData (String name, String dataType, boolean isPrimaryKey){
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
    }

    private ColumnData() {
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
