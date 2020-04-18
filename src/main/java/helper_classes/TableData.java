package helper_classes;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

//helper class. For a given table, shows its name, columns and information of each column
public class TableData {

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

    @Override
    public String toString() {
        return "TableData{" +
                "tableName='" + tableName + '\'' +
                ", schemaName='" + schemaName + '\'' +
                ", db=" + db +
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
