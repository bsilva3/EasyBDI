package helper_classes;

import prestoComm.DBModel;

public class DBData {

    private String dbName;
    private DBModel dbModel;
    private String url;
    private String user;
    private String pass;

    public DBData(String dbName, DBModel dbModel, String url) {
        this.dbModel = dbModel;
        this.url = url;
        //url validations
        this.url = url.replaceFirst("http://", ""); //http:// not needed

        //remove unncessary '/'
        if (this.url.charAt(this.url.length() - 1) != '/'){
            this.url = this.url.substring(0, this.url.length()-1);
        }
        this.dbName = dbName.replaceFirst("/", "");
    }

    public DBData(String dbName, DBModel dbModel, String url, String user, String pass) {
        this(dbName, dbModel, url);
        this.user = user;
        this.pass = pass;
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
}
