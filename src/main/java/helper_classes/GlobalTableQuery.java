package helper_classes;

import java.util.*;

public class GlobalTableQuery {

    private Map<GlobalTableData, List<GlobalColumnData>> selectGlobalColumn;

    public GlobalTableQuery() {
        selectGlobalColumn = new HashMap<>();
    }

    public void addSelectColumn(GlobalTableData table, GlobalColumnData col){
        //table already present, add the column
        if (selectGlobalColumn.containsKey(table)){
            List<GlobalColumnData> listCols = selectGlobalColumn.get(table);
            if (!listCols.contains(col)){
                selectGlobalColumn.get(table).add(col);
                return;
            }
        }
        else{
            List <GlobalColumnData> l = new ArrayList<>();
            l.add(col);
            selectGlobalColumn.put(table, l);
        }
    }

    public boolean deleteSelectColumnFromTable(GlobalTableData table, GlobalColumnData columnName){
        boolean success = selectGlobalColumn.get(table).remove(columnName);
        if (success && selectGlobalColumn.get(table).size()==0){
            selectGlobalColumn.remove(table);
        }
        return success;
    }

    //Creates a SELECT XXX FROM () with the necessary inner queries to get local schema data
    public String getLocalTableQuery(){
        String query = "SELECT ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectColumns : selectGlobalColumn.entrySet()){//TODO: is iteration correct??
            //for each global table
            GlobalTableData t = tableSelectColumns.getKey();
            List<GlobalColumnData> selectCols = tableSelectColumns.getValue();
            for (int i = 0; i < selectCols.size()-1; i++){
                query+= selectCols.get(i).getName()+",";
            }
            query+= selectCols.get(selectCols.size()-1).getName() +" ";//last elemment without comma
            query+= "FROM (";
            MappingType mapping = t.getMappingTypeOfMatches();
            if (mapping == MappingType.Simple)
                query+=handleSimpleMapping(t, selectCols);
            else if (mapping == MappingType.Horizontal)
                query+=handleHorizontalMapping(t, selectCols);
            else if (mapping == MappingType.Vertical)
                query+=handleVerticalMapping(t, selectCols);
            else
                return "Error: Invalid Mapping Type";
            query+= ")";
        }
        return query;
    }

    private String handleSimpleMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        //for each local table that matches with this global table
        String query = " SELECT ";
        Set<TableData> localTables = t.getLocalTablesFromCols(selectCols);
        for (TableData localTable : localTables){
            List<ColumnData> localCols = localTable.getColumnsList();
            for (int i = 0; i < localCols.size()-1; i++){
                query+=localCols.get(i).getCompletePrestoColumnName() +", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
        }
        return query;
    }

    private String handleHorizontalMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        String query ="";
        String tableUnionString = "UNION SELECT ";
        //for each local table that matches with this global table
        Set<TableData> localTables = t.getLocalTablesFromCols(selectCols);
        for (TableData localTable : localTables){
            List<ColumnData> localCols = localTable.getColumnsList();
            for (int i = 0; i < localCols.size()-1; i++){
                query+=localCols.get(i).getCompletePrestoColumnName() +", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
            query+=tableUnionString;
        }
        if (query.endsWith(tableUnionString)) {
            return query.substring(0, query.length() - tableUnionString.length());
        }
        return query;
    }

    private String handleVerticalMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        //for each local table that matches with this global table
        String query = " SELECT ";
        Set<TableData> localTables = t.getLocalTablesFromCols(selectCols);
        String tableJoinString = "INNER JOIN ";
        ColumnData primaryKeyCol = null;
        List<ColumnData> foreignKeyCols = new ArrayList<>();
        for (TableData localTable : localTables){
            List<ColumnData> localCols = localTable.getColumnsList();
            for (int i = 0; i < localCols.size(); i++){
                ColumnData col = localCols.get(i);
                if (col.isPrimaryKey() && !col.hasForeignKey()){
                    primaryKeyCol = col;
                }
                else if(col.isPrimaryKey() && col.hasForeignKey()) {//skip, this column is the same as primary key
                    foreignKeyCols.add(col);
                    continue;
                }
                query+=col.getCompletePrestoColumnName() +", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
        }
        if (query.endsWith(", ")) {
            query = query.substring(0, query.length() - ", ".length());//last column is whithout a comma
        }
        query+= " FROM "+primaryKeyCol.getTable().getCompletePrestoTableName()+" ";
        //add joins
        for (ColumnData col : foreignKeyCols){
            query+= tableJoinString +" "+col.getTable().getCompletePrestoTableName()+ " ON "+primaryKeyCol.getCompletePrestoColumnName()+ " = "+col.getCompletePrestoColumnName();
        }
        return query;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getSelectGlobalColumn() {
        return this.selectGlobalColumn;
    }

    public void setSelectGlobalColumn(Map<GlobalTableData, List<GlobalColumnData>> selectGlobalColumn) {
        this.selectGlobalColumn = selectGlobalColumn;
    }
}
