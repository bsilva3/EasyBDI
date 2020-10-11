package helper_classes.elements;

import java.io.Serializable;
import java.util.*;

//helper class. For a given table, shows its name, columns and information of each column
public class TableData implements Serializable {

    private String tableName;
    private String schemaName;
    private DBData db;
    private int id;
    private List<ColumnData> columnsList;

    public TableData(String tableName, String schemaName, DBData db) {
        this.tableName = tableName;
        this.db = db;
        this.schemaName = schemaName;
        id = -1;
    }

    public TableData(String tableName){
        this.tableName = tableName;
    }

    public TableData(String tableName, String schemaName, DBData db, int id) {
        this.tableName = tableName;
        this.db = db;
        this.schemaName = schemaName;
        this.id = id;
    }

    public void addColumn(ColumnData col){
        if (this.columnsList == null)
            columnsList = new ArrayList<>();
        if (columnsList.contains(col))
            return;
        columnsList.add(col);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DBData getDB() {
        return db;
    }

    public void setDB(DBData db) {
        this.db = db;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public DBData getDb() {
        return db;
    }

    public void setDb(DBData db) {
        this.db = db;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ColumnData> getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(List<ColumnData> columnsList) {
        this.columnsList = columnsList;
    }

    /**
     * Search for a column in this table whose name, data type and primary key constraint is the same
     * @param name
     * @param dataType
     * @param isPrimaryKey
     * @return true if there is at least one table with same name, datatype, and primary key constraint. false otherwise
     */
    public boolean columnExists(String name, String dataType, boolean isPrimaryKey){
        for (ColumnData c : this.columnsList){
            if (c.getName().equals(name) && c.getDataTypeNoLimit().equals(dataType) && c.isPrimaryKey() == isPrimaryKey){
                return true;
            }
        }
        return false;
    }

    public List<ColumnData> getPrimaryKeyColumns(){
        List<ColumnData> primKeys = new ArrayList<>();
        for (ColumnData c : columnsList){
            if (c.isPrimaryKey())
                primKeys.add(c);
        }
        return primKeys;
    }

    public boolean hasForeignKeys(){
        for (ColumnData c : columnsList){
            if (c.hasForeignKey())
                return true;
        }
        return false;
    }

    public String getCompletePrestoTableName(){
        return db.getCatalogName()+"."+schemaName+"."+tableName;
    }

    //Used for queries in order to select only certain columns
    public void keepOnlySpecifiedColumnsIfExist(Collection<ColumnData> columnDataSet){
        List<ColumnData> colsSelected = new ArrayList<>();
        for (ColumnData c : columnsList){
            if (columnDataSet.contains(c)){
                colsSelected.add(c);
            }
        }
        this.columnsList = colsSelected;
    }

    public int getNCols (){
        return this.columnsList.size();
    }

    @Override
    public String toString() {
        return "TableData{" +
                "tableName='" + tableName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", db=" + db.getDbName() +
                ", id=" + id +
                ", columnsList=" + columnsList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableData tableData = (TableData) o;
        return id == tableData.id &&
                tableName.equals(tableData.tableName) &&
                Objects.equals(schemaName, tableData.schemaName) &&
                Objects.equals(db, tableData.db);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, schemaName, db, id);
    }
}
