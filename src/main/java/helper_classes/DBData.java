package helper_classes;

import prestoComm.Constants;
import prestoComm.DBModel;

public class DBData {

    private String dbName;
    private DBModel dbModel;
    private String url;
    private String user;
    private String pass;
    private String catalogName; //for presto file name
    private int id;

    public DBData(String url, DBModel dbModel, String dbName) {
        this.dbModel = dbModel;
        this.url = url;
        //url validations
        this.url = url.replaceFirst("http://", ""); //http:// not needed
        //remove unncessary '/'
        if (!this.url.isEmpty() && this.url.charAt(this.url.length() - 1) == '/'){
            this.url = this.url.substring(0, this.url.length()-1);
        }
        this.dbName = dbName.replaceFirst("/", "");
        this.catalogName = dbModel+"_"+this.url+"_"+this.dbName;
        this.catalogName = this.catalogName.toLowerCase().replaceAll("[\\:\\-\\.]", "_");
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

    @Override
    public String toString() {
        return "DBData{" +
                "dbName='" + dbName + '\'' +
                ", dbModel=" + dbModel +
                ", url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", pass='" + pass + '\'' +
                ", catalogName='" + catalogName + '\'' +
                ", id=" + id +
                '}';
    }
}
