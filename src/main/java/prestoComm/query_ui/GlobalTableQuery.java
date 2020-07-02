package prestoComm.query_ui;

import helper_classes.*;
import prestoComm.PrestoMediator;

import java.util.*;

public class GlobalTableQuery {

    private Map<GlobalTableData, List<GlobalColumnData>> selectRows;
    private Map<GlobalTableData, List<GlobalColumnData>> selectColumns;
    private List<GlobalColumnData> measures;
    private GlobalTableData factsTable;
    private PrestoMediator presto;

    public GlobalTableQuery(PrestoMediator presto, GlobalTableData factsTable) {
        this.presto = presto;
        this.factsTable = factsTable;
        selectRows = new HashMap<>();
        selectColumns = new HashMap<>();
    }

    public void addSelectColumn(GlobalTableData table, GlobalColumnData col){
        //table already present, add the column
        if (selectColumns.containsKey(table)){
            List<GlobalColumnData> listCols = selectRows.get(table);
            if (!listCols.contains(col)){
                selectColumns.get(table).add(col);
                return;
            }
        }
        else{
            List <GlobalColumnData> l = new ArrayList<>();
            l.add(col);
            selectColumns.put(table, l);
        }
    }

    public void addSelectRow(GlobalTableData table, GlobalColumnData col){
        //table already present, add the column
        if (selectRows.containsKey(table)){
            List<GlobalColumnData> listCols = selectRows.get(table);
            if (!listCols.contains(col)){
                selectRows.get(table).add(col);
                return;
            }
        }
        else{
            List <GlobalColumnData> l = new ArrayList<>();
            l.add(col);
            selectRows.put(table, l);
        }
    }

    public void addMeasure(GlobalColumnData measure){

    }

    public boolean deleteSelectColumnFromTable(GlobalTableData table, GlobalColumnData columnName){
        boolean success = selectColumns.get(table).remove(columnName);
        if (success && selectColumns.get(table).size()==0){
            selectColumns.remove(table);
        }
        return success;
    }

    public boolean deleteSelectRowFromTable(GlobalTableData table, GlobalColumnData columnName){
        boolean success = selectRows.get(table).remove(columnName);
        if (success && selectRows.get(table).size()==0){
            selectRows.remove(table);
        }
        return success;
    }

    /*public String addPivotQuery(){
        if (selectRowGlobalColumn.isEmpty()){
            return getLocalTableQuery();
        }
        else{
            for (Map.Entry<GlobalTableData, List<GlobalColumnData>> entry: selectRowGlobalColumn.entrySet()){
                GlobalTableData t = entry.getKey();
                List<GlobalColumnData> cols = entry.getValue();
                ResultSet results = presto.getLocalTablesQueries(getLocalTableQuery(t, cols));
            }

        }
    }*/

    public String getLocalTableQuery(GlobalTableData t, List<GlobalColumnData> selectCols){
        MappingType mapping = t.getMappingTypeOfMatches();
        if (mapping == MappingType.Simple)
            return handleSimpleMapping(t, selectCols);
        else if (mapping == MappingType.Horizontal)
            return handleHorizontalMapping(t, selectCols);
        else if (mapping == MappingType.Vertical)
            return handleVerticalMapping(t, selectCols);
        else
            return "Error: Invalid Mapping Type";
    }

    public String getLocalTableQuery(GlobalTableData t){
        MappingType mapping = t.getMappingTypeOfMatches();
        if (mapping == MappingType.Simple)
            return handleSimpleMapping(t, t.getGlobalColumnDataList());
        else if (mapping == MappingType.Horizontal)
            return handleHorizontalMapping(t, t.getGlobalColumnDataList());
        else if (mapping == MappingType.Vertical)
            return handleVerticalMapping(t, t.getGlobalColumnDataList());
        else
            return "Error: Invalid Mapping Type";
    }

    //Creates a SELECT XXX FROM () with the necessary inner queries to get local schema data
    public String buildQuery(){
        String query = "SELECT ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()){//TODO: is iteration correct??
            //for each global table
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();
            for (int i = 0; i < rowsForSelect.size()-1; i++){
                query+= rowsForSelect.get(i).getName()+",";
            }
            query+= rowsForSelect.get(rowsForSelect.size()-1).getName() +" ";//last elemment without comma
            query+= "FROM (";
            String subQueries = getLocalTableQuery(t, rowsForSelect);

            if(subQueries.contains("Error")){
                return subQueries;//propagate error
            }
            query+=subQueries;
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
        String query ="SELECT ";
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

    public Map<GlobalTableData, List<GlobalColumnData>> getSelectRows() {
        return this.selectRows;
    }

    public void setSelectRows(Map<GlobalTableData, List<GlobalColumnData>> selectRows) {
        this.selectRows = selectRows;
    }

    public GlobalTableData getFactsTable() {
        return this.factsTable;
    }

    public void setFactsTable(GlobalTableData factsTable) {
        this.factsTable = factsTable;
    }
}
