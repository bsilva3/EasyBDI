package helper_classes;

import prestoComm.Constants;
import prestoComm.DBModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimpleDBData {
    private String dbName;
    private DBModel dbModel;
    private String url;
    private String user;
    private String pass;


    public SimpleDBData(String url, DBModel dbModel, String dbName, String user, String pass) {
        this.url = url;
        this.dbName = dbName;
        this.dbModel = dbModel;
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

    @Override
    public String toString() {
        String s =  dbName + " (" + dbModel + ") at " + url ;
        //if (user!= null || !user.isEmpty())
            //s+= " (Authentication required)";
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleDBData)) return false;
        SimpleDBData that = (SimpleDBData) o;
        return this.getDbName().equals(that.getDbName()) &&
                this.getDbModel() == that.getDbModel() &&
                this.getUrl().equals(that.getUrl()) &&
                Objects.equals(this.getUser(), that.getUser()) &&
                Objects.equals(this.getPass(), that.getPass());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getDbName(), this.getDbModel(), this.getUrl(), this.getUser(), this.getPass());
    }
}
