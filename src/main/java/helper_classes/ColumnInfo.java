package helper_classes;

//Helper class. Shows info about a column (data type, name..)
public class ColumnInfo {
    private String name;
    private String dataType;

    public ColumnInfo(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
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
}
