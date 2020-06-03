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

    public String getLocalTableQuery(){
        String query = "SELECT ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectColumns : selectGlobalColumn.entrySet()){
            //for each global table
            GlobalTableData t = tableSelectColumns.getKey();
            List<GlobalColumnData> selectCols = tableSelectColumns.getValue();
            MappingType mapping = t.getMappingTypeOfMatches();
            if (mapping == MappingType.Simple)
                query+=handleSimpleMapping(t, selectCols);
            else if (mapping == MappingType.Horizontal)
                query+=handleHorizontalMapping(t, selectCols);
            else if (mapping == MappingType.Vertical)
                query+=handleVerticalMapping(t, selectCols);
            else
                return "Error: Invalid Mapping Type";
        }
        return query;
    }

    private String handleSimpleMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        //for each local table that matches with this global table
        String query = "";
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
        String query ="";
        //for each local table that matches with this global table
        Set<TableData> localTables = t.getLocalTablesFromCols();
        for (TableData localTable : localTables){
            List<ColumnData> localCols = localTable.getColumnsList();
            for (int i = 0; i < localCols.size()-1; i++){
                query+=localCols.get(i).getCompletePrestoColumnName() +", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
            query+="JOIN ";
        }
        if (query.endsWith("JOIN ")) {
            return query.substring(0, query.length() - "UNION ".length());
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
