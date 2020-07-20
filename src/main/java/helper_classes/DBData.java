package helper_classes;

import prestoComm.Constants;
import prestoComm.DBModel;

import java.io.Serializable;
import java.util.*;

public class DBData implements Serializable {

    private String dbName;
    private DBModel dbModel;
    private String url;
    private String user;
    private String pass;
    private String catalogName; //for presto file name
    private int id;
    List<TableData> tableList;

    public DBData(String url, DBModel dbModel, String dbName) {
        if (dbName == null || dbName.length() == 0){
            String[] modelSplit = url.split("/");
            this.dbName = modelSplit[modelSplit.length-1];
        }
        else{
            this.dbName = dbName.replaceAll("/", "");//replaceFirst?
        }

        this.dbModel = dbModel;
        //this.url = url;
        //url validations
        this.url = url.replaceFirst("http://", ""); //http:// not needed
        //remove unncessary '/' at the end if any
        if (!this.url.isEmpty() && this.url.charAt(this.url.length() - 1) == '/'){
            this.url = this.url.substring(0, this.url.length()-1);
        }
        this.catalogName = dbModel+"_"+this.url+"_"+this.dbName;
        this.catalogName = this.catalogName.toLowerCase().replaceAll("[\\:\\-\\.()]", "_");
    }


    public DBData(String url, DBModel dbModel, String dbName, String user, String pass) {
        this(url, dbModel, dbName);
        this.user = user;
        this.pass = pass;
    }

    public DBData(String url, DBModel dbModel, String dbName, String user, String pass, int id) {
        this(url, dbModel, dbName, user, pass);
        this.id = id;
    }

    public SimpleDBData convertToSimpleDB(){
        return new SimpleDBData(this.getUrl(), this.getDbModel(), this.getDbName(), this.getUser(), this.getPass());
    }

    public List<TableData> getTableList() {
        return this.tableList;
    }

    public Map<String, List<TableData>> getTableBySchemaMap() {
        Map<String, List<TableData>> tablesInSchmeas = new HashMap<>();
        for (TableData t : tableList){
            if (tablesInSchmeas.containsKey(t.getSchemaName())){//add table to schema
                List<TableData> tables = tablesInSchmeas.get(t.getSchemaName());
                tables.add(t);
                tablesInSchmeas.put(t.getSchemaName(), tables);
            }
            else{
                //add new schema
                List<TableData> tables = new ArrayList<>();
                tables.add(t);
                tablesInSchmeas.put(t.getSchemaName(), tables);
            }
        }
        return tablesInSchmeas;
    }

    public void setTableList(List<TableData> tableList) {
        this.tableList = tableList;
    }

    public void addTable(TableData table){
        if (this.tableList == null)
            tableList = new ArrayList<>();
        tableList.add(table);
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public DBModel getDbModel() {
        return dbModel;
    }

    public void setDbModel(DBModel dbModel) {
        this.dbModel = dbModel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getFullFilePath(){
        return Constants.PRESTO_PROPERTIES_FOLDER + this.catalogName+".properties";
    }

    public String getFileName(){
        return this.catalogName+".properties";
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void clearTables(){
        tableList.clear();
    }

    @Override
    public String toString() {
        return "DBData{" +
                "dbName='" + dbName + '\'' +
                ", dbModel=" + dbModel +
                ", url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", catalogName='" + catalogName + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        DBData dbData = (DBData) o;
        return this.id == dbData.id &&
                this.dbName.equals(dbData.dbName) &&
                this.dbModel == dbData.dbModel &&
                this.url.equals(dbData.url) &&
                Objects.equals(this.user, dbData.user) &&
                Objects.equals(this.pass, dbData.pass) &&
                Objects.equals(this.catalogName, dbData.catalogName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dbName, this.dbModel, this.url, this.user, this.pass, this.catalogName, this.id);
    }
}
