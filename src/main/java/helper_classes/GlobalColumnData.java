package helper_classes;

import helper_classes.utils_other.Constants;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a global column (in the global schema)
 */
public class GlobalColumnData implements Serializable {
    private int columnID;
    private Set<ColumnData> localColumns;
    private String name;
    private String ogName;
    private String dataType;
    private String ogDataType;
    private boolean isPrimaryKey;
    private String foreignKey; //globalTablename.globalColumnName
    private String orderBy; //just for the query save
    private String fullName; //table.column
    private String aggrOp; //aggrOP
    private boolean originalDatatypeChanged; //aggrOP

    //public static final long serialVersionUID = 3468197598887017481L;
    public static final long serialVersionUID = 4654436492332727651L;
    //public static final long serialVersionUID = -4654436492332727651L;

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> localCols) {
        this.name = name;
        this.ogName = name;
        this.dataType = dataType;
        this.ogDataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = localCols;
        this.aggrOp = "";
        this.originalDatatypeChanged = false;
    }

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, Set<ColumnData> localCols, int id) {
        this.name = name;
        this.ogName = name;
        this.dataType = dataType;
        this.ogDataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = localCols;
        this.columnID = id;
        this.aggrOp = "";
        this.originalDatatypeChanged = false;
    }

    public GlobalColumnData(String name, String dataType, boolean isPrimaryKey, ColumnData localCol) {
        this.name = name;
        this.ogName = name;
        this.dataType = dataType;
        this.ogDataType = dataType;
        this.isPrimaryKey = isPrimaryKey;
        this.localColumns = new HashSet<>();
        this.localColumns.add(localCol);
        this.aggrOp = "";
        this.originalDatatypeChanged = false;
    }

    public GlobalColumnData(ColumnData column) {
        this.name = column.getName();
        this.ogName = name;
        this.dataType = column.getDataType();
        this.ogDataType = dataType;
        this.isPrimaryKey = column.isPrimaryKey();
        this.foreignKey = column.getForeignKey();
        this.localColumns = new HashSet<>();
        this.localColumns.add(column);
        this.aggrOp = "";
        this.originalDatatypeChanged = false;
    }

    public MappingType getMappingType(){
        Iterator iter = localColumns.iterator();
        ColumnData localCol = (ColumnData) iter.next();
        return localCol.getMapping();
    }

    public boolean isNumeric(){
        for (String datatype : Constants.NUMERIC_DATATYPES){
            if (this.getDataType().equalsIgnoreCase(datatype))
                return true;
        }
        return false;
    }

    public boolean isDateTime(){
        for (String datatype : Constants.TIME_DATATYPES){
            if (this.getDataType().equalsIgnoreCase(datatype))
                return true;
        }
        return false;
    }

    public boolean isForeignKey(){
        if (this.foreignKey == null || this.foreignKey.isEmpty())
            return false;
        return true;
    }

    public int getColumnID() {
        return columnID;
    }

    public void setColumnID(int columnID) {
        this.columnID = columnID;
    }

    public Set<ColumnData> getLocalColumns() {
        return localColumns;
    }

    public void setLocalColumns(Set<ColumnData> localColumns) {
        this.localColumns = localColumns;
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

    public String getOgDataType() {
        String datatypeNoLimit =  ogDataType.split("\\(")[0];
        datatypeNoLimit = datatypeNoLimit.replaceAll("\\s+","");
        return datatypeNoLimit;
    }

    public String getDataTypeNoLimit() {
        String datatypeNoLimit =  dataType.split("\\(")[0];
        datatypeNoLimit = datatypeNoLimit.replaceAll("\\s+","");
        return datatypeNoLimit;
    }

    public String getDataTypeCategory() {
        if (Arrays.stream(Constants.NUMERIC_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getDataTypeNoLimit()))){
            return Constants.NUMERIC_DATATYPE;
        }
        if (Arrays.stream(Constants.STRING_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getDataTypeNoLimit()))){
            return Constants.STRING_DATATYPE;
        }
        if (Arrays.stream(Constants.BOOLEAN_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getDataTypeNoLimit()))){
            return Constants.BOOLEAN_DATATYPE;
        }
        if (Arrays.stream(Constants.TIME_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getDataTypeNoLimit()))){
            return Constants.TIME_DATATYPE;
        }
        return null;
    }

    public String getOGDataTypeCategory() {
        if (Arrays.stream(Constants.NUMERIC_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getOgDataType()))){
            return Constants.NUMERIC_DATATYPE;
        }
        if (Arrays.stream(Constants.STRING_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getOgDataType()))){
            return Constants.STRING_DATATYPE;
        }
        if (Arrays.stream(Constants.BOOLEAN_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getOgDataType()))){
            return Constants.BOOLEAN_DATATYPE;
        }
        if (Arrays.stream(Constants.TIME_DATATYPES).anyMatch(d -> d.equalsIgnoreCase(getOgDataType()))){
            return Constants.TIME_DATATYPE;
        }
        return null;
    }

    public void setDataType(String dataType) {
        if(this.dataType == null)
            this.ogDataType = dataType;
        if (!getDataTypeNoLimit().equalsIgnoreCase(dataType)){
            this.originalDatatypeChanged = true;
            this.dataType = dataType;
        }
    }

    public void setOGDataType(String dataType) {
        this.ogDataType = dataType;
        if (!getDataTypeNoLimit().equalsIgnoreCase(ogDataType)){
            this.originalDatatypeChanged = true;
        }
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public Set<TableData> getLocalTables() {
        Set<TableData> tables = new HashSet<>();
        for (ColumnData col : localColumns){
                tables.add(col.getTable());
        }
        return tables;
    }

    public Set<Integer> getLocalTablesIDs(){
        Set<Integer> tableIds = new HashSet<>();
        for (ColumnData c : this.localColumns){
            tableIds.add(c.getTableID());
        }
        return tableIds;
    }

    public boolean correspondenceColumnExist(ColumnData col){
        if (this.localColumns.contains(col))
            return true;
        return false;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullNameEscapped() {
        if (fullName.contains(".")) {
            String[] split = fullName.split("\\.");
            return "\"" + split[0] + "\".\"" + split[1] + "\""; //table."col"
        }
        else
            return "\""+fullName+"\"";
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isOriginalDatatypeChanged() {
        return originalDatatypeChanged;
    }

    public String getOgName() {
        return ogName;
    }

    public String getAggrOp() {
        return aggrOp;
    }

    //aggrOP(tableName.columnName)
    public String getAggrOpFullName() {
        String aggrOpFullName = "";
        if (aggrOp == null || aggrOp.isEmpty())
            return getFullName(); //no aggregation set for this attribute, just return tab.col
        else if (aggrOp.contains("DISTINCT"))
            aggrOpFullName = aggrOp.split(" ")[0]+"(DISTINCT "+getFullName()+")";
        else
            aggrOpFullName = aggrOp+"("+getFullName()+")";
        return aggrOpFullName;
    }

    public String getAggrOpFullNameEscapped() {
        String aggrOpFullName = "";
        if (aggrOp == null || aggrOp.isEmpty())
            return getFullName(); //no aggregation set for this attribute, just return tab.col
        else if (aggrOp.contains("DISTINCT"))
            aggrOpFullName = aggrOp.split(" ")[0]+"(DISTINCT "+getFullNameEscapped()+")";
        else
            aggrOpFullName = aggrOp+"("+getFullNameEscapped()+")";
        return aggrOpFullName;
    }

    //aggrOP(tableName.columnName)
    public String getAggrOpName() {
        String aggrOpFullName = "";
        if (aggrOp == null || aggrOp.isEmpty())
            return name;  //no aggregation set for this attribute, just return colName
        else if (aggrOp.contains("DISTINCT"))
            aggrOpFullName = aggrOp.split(" ")[0]+"(DISTINCT "+name+")";
        else
            aggrOpFullName = aggrOp+"(\""+name+"\")";
        return aggrOpFullName;
    }

    public void setAggrOp(String aggrOp) {
        if (this.aggrOp!=null && this.aggrOp.contains("DISTINCT")){//keep distinct
            this.aggrOp = aggrOp;
            this.aggrOp+=" DISTINCT";
        }
        else
            this.aggrOp = aggrOp;
    }

    public void setAggrOp(String aggrOp, boolean hasDistinct) {
        if (aggrOp.isEmpty() || aggrOp.equalsIgnoreCase("no aggregation")){
            this.aggrOp = "";
        }
        else
            this.aggrOp = aggrOp;
        if (hasDistinct) {
            this.aggrOp += " DISTINCT";
        }
    }

    public void changeDistinct() {
        if (aggrOp.contains("DISTINCT")){
            this.aggrOp = aggrOp.split(" ")[0];
        }
        else
            this.aggrOp+=" DISTINCT";
    }

    public void setDistinct(boolean isDistinct) {
        if (isDistinct){//distinct
            if (aggrOp.contains("DISTINCT")){
                return; //already contains distinct
            }
            else{
                //does not have distinct, add it
                this.aggrOp+=" DISTINCT";
            }
        }
        else{//not distinct
            if (aggrOp.contains("DISTINCT")){
                this.aggrOp = aggrOp.split(" ")[0];; //contains distinct, remove it
            }
            else{
                return; //does not contain distinct
            }
        }
    }

    public boolean hasForeignKey(){
        if (this.foreignKey == null || this.foreignKey.isEmpty())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "GlobalColumnData{" +
                "columnID=" + columnID +
                ", localColumns=" + localColumns +
                ", name='" + name + '\'' +
                ", dataType='" + dataType + '\'' +
                ", isPrimaryKey=" + isPrimaryKey +
                ", foreignKey='" + foreignKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalColumnData)) return false;
        GlobalColumnData that = (GlobalColumnData) o;
        return getColumnID() == that.getColumnID() &&
                isPrimaryKey() == that.isPrimaryKey() &&
                getName().equals(that.getName()) &&
                getDataType().equals(that.getDataType()) &&
                Objects.equals(isForeignKey(), that.isForeignKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColumnID(), getName(), getDataType(), isPrimaryKey(), isForeignKey());
    }
}
