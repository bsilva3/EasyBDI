package helper_classes;

import prestoComm.MetaDataManager;

import java.io.Serializable;
import java.util.*;

//Helper class. Shows info about a column (data type, name..)
public class ColumnData implements Serializable {
    private int columnID;
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private TableData table;
    private String foreignKey; //in the type "catalogName.schemaName.TableName.ColumnName"
    private int tableID;
    private MappingType mapping;

    public static class Builder {
        private int columnID;
        private String name;
        private String dataType;
        private boolean isPrimaryKey;
        private TableData table;
        private String foreignKey;
        private int tableID;
        private MappingType mapping;

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

        public Builder withMappingType(MappingType mapping){
            this.mapping = mapping;
            return this;
        }

        public Builder withMergedCols(Set<ColumnData> mergedColumnIds){
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
            col.mapping = this.mapping;
            return col;
        }

    }

    public ColumnData (String name, String dataType, boolean isPrimaryKey){
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
    }

    private ColumnData() {
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

    public int getTableID() {
        return getTable().getId();
    }

    public void setTableID(int tableID) {
        this.tableID = tableID;
    }

    public MappingType getMapping() {
        return mapping;
    }

    public void setMapping(MappingType mapping) {
        this.mapping = mapping;
    }

    public ColumnData getForeignKeyColumn(String projectName){
        if (!hasForeignKey())
            return null;
        MetaDataManager m = new MetaDataManager(projectName);
        String[] foreignKeySplit = this.getForeignKey().split("\\."); //"catalogName.schemaName.TableName.ColumnName"
        ColumnData c = m.getColumn(this.getTable().getDB(), foreignKeySplit[1], foreignKeySplit[2], foreignKeySplit[3]); //need schemaname, table name and column as well as the dbID
        return c;
    }

    public ColumnData getForeignKeyColumn(MetaDataManager m){
        if (!hasForeignKey())
            return null;
        String[] foreignKeySplit = this.getForeignKey().split("\\."); //"catalogName.schemaName.TableName.ColumnName"
        ColumnData c = m.getColumn(this.getTable().getDB(), foreignKeySplit[1], foreignKeySplit[2], foreignKeySplit[3]); //need schemaname, table name and column as well as the dbID
        return c;
    }

    public boolean hasForeignKey(){
        if (this.foreignKey == null || this.foreignKey.isEmpty())
            return false;
        return true;
    }

    public String getCompletePrestoColumnName(){
        return table.getCompletePrestoTableName()+"."+name;
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnData that = (ColumnData) o;
        return columnID == that.columnID &&
                isPrimaryKey == that.isPrimaryKey &&
                name.equals(that.name) &&
                dataType.equals(that.dataType) &&
                Objects.equals(table, that.table) &&
                Objects.equals(foreignKey, that.foreignKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnID, name, dataType, isPrimaryKey, table, foreignKey);
    }
}
