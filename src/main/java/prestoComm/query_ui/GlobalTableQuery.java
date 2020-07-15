package prestoComm.query_ui;

import helper_classes.*;
import prestoComm.PrestoMediator;

import java.util.*;

public class GlobalTableQuery {

    private Map<GlobalTableData, List<GlobalColumnData>> selectRows;
    private Map<GlobalTableData, List<GlobalColumnData>> selectColumns;
    private List<String> measures;
    private List<String> orderBy;
    private String filterQuery;
    private FactsTable factsTable;
    private PrestoMediator presto;

    public GlobalTableQuery(PrestoMediator presto, FactsTable factsTable) {
        this.presto = presto;
        this.factsTable = factsTable;
        selectRows = new HashMap<>();
        selectColumns = new HashMap<>();
        measures = new ArrayList<>();
        orderBy = new ArrayList<>();
    }

    public void addOrderByRow(String groupByRow){
        orderBy.add(groupByRow);
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

    public void addMeasure(String measure){
        this.measures.add(measure);
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
        removeOrderByIfPresent(table.getTableName()+"."+columnName.getName());
        return success;
    }

    /**
     * Removes an order by element if present. Searches for strings in the form 'tablename.columnname'
     * @param orderByElem
     */
    public void removeOrderByIfPresent(String orderByElem){
        for (String s : orderBy){
            if (s.contains(orderByElem)){
                orderBy.remove(s);
                return;
            }
        }
    }

    public void removeMeasure(String measure){
        this.measures.remove(measure);
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
            return handleSimpleMapping(t);
        else if (mapping == MappingType.Horizontal)
            return handleHorizontalMapping(t);
        else if (mapping == MappingType.Vertical)
            return handleVerticalMapping(t, t.getGlobalColumnDataList());
        else
            return "Error: Invalid Mapping Type";
    }

    //Creates a SELECT XXX FROM () with the necessary inner queries to get local schema data
    public String buildQuerySelectRowsOnly(){
        String query = "SELECT ";
        //first add to the select the dimensions columns

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> dimTable : selectRows.entrySet()){
            List<GlobalColumnData> cols = dimTable.getValue();
            GlobalTableData t = dimTable.getKey();
            for (GlobalColumnData c : cols){
                query+= t.getTableName()+"."+c.getName()+",";

            }
        }

        query = query.substring(0, query.length() - 1);//last elemment without comma
        query+= " FROM ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()){
            //for each global table
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();
            //query+= "FROM (";
            String subQueries = "("+getLocalTableQuery(t, rowsForSelect);

            if(subQueries.contains("Error")){
                return subQueries;//propagate error
            }
            query+=subQueries;
            query+= ") AS "+t.getTableName()+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma

        if (filterQuery.length() > 0){
            query += " WHERE " + filterQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        return query;
    }

    //Creates a 'SELECT XXX FROM ( ) join fatcs with dims foreign keys' with the necessary inner queries to get local schema data and join facts foreign keys with dims. Also
    //performs aggregations on the measures and groups the dimensions rows
    public String buildQuerySelectRowsAndMeasures() {
        String query = "SELECT ";
        Map<GlobalTableData, List<GlobalColumnData>> tableSelectRowsWithPrimKeys = new HashMap<>();
        //first add to the select the dimensions columns
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> dimTable : selectRows.entrySet()){
            List<GlobalColumnData> cols = dimTable.getValue();
            GlobalTableData t = dimTable.getKey();
            GlobalTableData newt = new GlobalTableData(t.getTableName());
            List<GlobalColumnData> newCols = new ArrayList<>();
            boolean primKeyIsSelected = false;
            for (GlobalColumnData c : cols){
                query+= t.getTableName()+"."+c.getName()+",";
                GlobalColumnData newC = new GlobalColumnData(c.getName(), c.getDataType(), c.isPrimaryKey(), c.getLocalColumns());
                newCols.add(newC);
                if (c.isPrimaryKey()) {
                    primKeyIsSelected = true;
                }
            }
            if (!primKeyIsSelected){
                GlobalColumnData cp = dimTable.getKey().getPrimaryKeyColumn(); //primary key column missing
                newCols.add(new GlobalColumnData(cp.getName(), cp.getDataType(), cp.isPrimaryKey(), cp.getLocalColumns()));
            }
            newt.setGlobalColumnData(newCols);
            tableSelectRowsWithPrimKeys.put(newt, newCols);
        }
        //if primary keys of dims are not in the query, add them now
        /*for (String s : dimKeysStr){
            query+= s+", ";
        }*/
        //add to the select the measures with the aggregation operation (in the form 'aggr(measureName)'). This a string taken from the drop are in the interface.
        for (String measureCol : measures) {
            String measureName = measureCol.split("[()]")[1]; //split on first space to the measure name (its in the form "aggr(measureName)" )
            query += measureCol + " AS " + measureName + ",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma
        query+= " FROM ";

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : tableSelectRowsWithPrimKeys.entrySet()){
            //for each global column create inner queries in the 'From' clause
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();

            String subQueries = getLocalTableQuery(t, rowsForSelect);

            if(subQueries.contains("Error")){
                return subQueries;//propagate error
            }
            query+="("+subQueries;
            //Join on facts table
            query+= ") AS " + t.getTableName()+",";
        }
        //get facts table in from clause
        String subQueries = "("+getLocalTableQuery(factsTable.getGlobalTable());
        if(subQueries.contains("Error")){
            return subQueries;//propagate error
        }
        query+=subQueries;
        query+= ") AS "+factsTable.getGlobalTable().getTableName()+" ";

        //perform Where on facts foreign key fields = dimensions referenced primary keys
        query+= " WHERE (";

        Map<GlobalColumnData, Boolean> factColumns = factsTable.getColumns();
        //foreign key of facts = prim key of the dimensions
        for (Map.Entry<GlobalColumnData, Boolean> factColumn : factColumns.entrySet()) {
            boolean isMeasure = factColumn.getValue();
            if (!isMeasure) { //it must be a foreign key, check if it references a dim table
                for (GlobalTableData tableDim : tableSelectRowsWithPrimKeys.keySet()) {

                    GlobalColumnData factsCol = factColumn.getKey();
                    if (factsCol.hasForeignKey()) {
                        GlobalColumnData referencedCol = isFactsColReferencingDimTable(factsCol.getForeignKey(), tableDim);
                        if (referencedCol != null) {
                            query += tableDim.getTableName() + "." + referencedCol.getName() + " = " + factsTable.getGlobalTable().getTableName() + "." + factsCol.getName();
                            query += " AND ";
                            break;
                        }
                    }
                }
            }
        }

        query = query.substring(0, query.length() - "AND ".length());//last column is whithout a comma
        query+=")"; //close where clause

        //aditional filters set by user here
        if (filterQuery.length() > 0){
            query += " AND ( " + filterQuery +")";//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        //groupby for each dimension column (only if aggregation operation is made)
        query += " GROUP BY ( ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()) {
            //for each global column create inner queries in the 'From' clause
            GlobalTableData table = tableSelectRows.getKey();
            List<GlobalColumnData> columns = tableSelectRows.getValue();
            for (GlobalColumnData col : columns) {
                query +=table.getTableName()+"."+col.getName()+",";
            }
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma
        query += ")"; //close group by

        return query;
    }

    //Creates a 'SELECT XXX FROM ( ) join fatcs with dims foreign keys' with the necessary inner queries to get local schema data and join facts foreign keys with dims. Also
    //performs aggregations on the measures and groups the dimensions rows
    /*public String buildQuerySelectRowsAndMeasures(){
        String query = "SELECT ";
        Collection<List<GlobalColumnData>> dimsRows = (Collection<List<GlobalColumnData>>) selectRows.values(); //list with a list of rows of each dim table
        //first add to the select the dimensions columns
        for (List<GlobalColumnData> dimsRowsOfTable : dimsRows){
            for (GlobalColumnData c : dimsRowsOfTable){
                query+= c.getName()+",";
            }
        }
        //add to the select the measures with the aggregation operation (in the form 'aggr(measureName)'). This a string taken from the drop are in the interface.
        for (String measureCol : measures){
            String measureName = measureCol.split("[()]")[1]; //split on first space to the measure name (its in the form "aggr(measureName)" )
            query+= measureCol+" AS "+measureName+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma
        query+= " FROM ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()){
            //for each global column create inner queries in the 'From' clause
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();

            String subQueries = getLocalTableQuery(t, rowsForSelect);

            if(subQueries.contains("Error")){
                return subQueries;//propagate error
            }
            query+="("+subQueries;
            //Join on facts table
            query+= ") AS " + t.getTableName()+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma

        Map<GlobalColumnData, Boolean> factColumns = factsTable.getColumns();
        String factsLocalTableQuery = getLocalTableQuery(factsTable.getGlobalTable()); //not efficient (repeats for every join..)
        if (factsLocalTableQuery.contains("Error")){
            return factsLocalTableQuery;
        }
        //foreign key of facts = prim key of the dimensions
        for (Map.Entry<GlobalColumnData, Boolean> factColumn : factColumns.entrySet()){
            boolean isMeasure = factColumn.getValue();
            if (!isMeasure){ //it must be a foreign key, check if it references a dim table
                for (GlobalTableData tableDim : selectRows.keySet()){

                    GlobalColumnData factsCol = factColumn.getKey();
                    if (factsCol.hasForeignKey()){
                        GlobalColumnData referencedCol = isFactsColReferencingDimTable(factsCol.getForeignKey(), tableDim);
                        if (referencedCol != null){
                            query+= " JOIN (" + factsLocalTableQuery +") AS "+factsTable.getGlobalTable().getTableName()+" ON " ;
                            query+= tableDim.getTableName()+"."+referencedCol.getName() +" = "+factsTable.getGlobalTable().getTableName()+"."+factsCol.getName();
                            break;
                        }
                    }
                }
            }
        }
        //validate query so far
        if (!query.contains("JOIN"))
            return "Error: Query not correctly formulated";
        //group by for each dim column
        query += " GROUP BY ( ";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()) {
            //for each global column create inner queries in the 'From' clause
            GlobalTableData table = tableSelectRows.getKey();
            List<GlobalColumnData> columns = tableSelectRows.getValue();
            for (GlobalColumnData col : columns) {
                query +=table.getTableName()+"."+col.getName()+",";
            }
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma
        query += ")"; //close group by

        //Oder by here if (any)...
        return query;
    }*/

    private String getMeasureName(String measureAndOP){
        return measureAndOP.split("[()]")[1]; //split on first space to the measure name (its in the form "aggr(measureName)" )
    }
    private String getMeasureOP(String measureAndOP){
        return measureAndOP.split("[\\(]")[0]; //split on first space to the measure name (its in the form "aggr(measureName)" )
    }

    public String buildQuery(){
        String query = "";
        if (selectColumns.size() == 0 && measures.size() == 0 && selectRows.size() > 0){
            query = buildQuerySelectRowsOnly();
        }
        else if (selectColumns.size() == 0 && measures.size() > 0 && selectRows.size() > 0){
            query = buildQuerySelectRowsAndMeasures();
        }
        else
            return "Error invalid query elements given";
        //add order by elements if any are selected
        if (orderBy.size() > 0){
            query += " ORDER BY ";
            for (String s : orderBy){
                query+=s +",";
            }
            query = query.substring(0, query.length() - 1);//last elemment without comma
        }
        return query;
    }

    private List<GlobalColumnData> getSelectedMeasureCols(List<String> measuresString){
        List<GlobalColumnData> selectedMeasures = new ArrayList<>();
        Set<GlobalColumnData> measureCols = factsTable.getColumns().keySet();
        for (String measureString : measuresString){
            String measureName = measureString.split("[()]")[1]; //split on first space to the measure name (its in the form "aggr(measureName)" )
            for (GlobalColumnData measureCol : measureCols){
                if (measureCol.getName().equals(measureName)){
                    selectedMeasures.add(measureCol);
                }
            }
        }
        return selectedMeasures;
    }

    private GlobalColumnData isFactsColReferencingDimTable(String foreignKey, GlobalTableData dimTable){
        String[] splitStr = foreignKey.split("\\.");
        String tableName = splitStr[0];
        String columnName = splitStr[1];
        if (dimTable.getTableName().equals(tableName)){
            List<GlobalColumnData> globalColsDims = dimTable.getGlobalColumnDataList();
            for (GlobalColumnData c : globalColsDims){
                if (c.getName().equals(columnName))
                    return c;
            }
        }
        return null;
    }

    private String handleSimpleMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        //for each local table that matches with this global table
        String query = " SELECT ";
        Set<TableData> localTables = t.getAllLocalTablesFromCols(selectCols);
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
    private String handleSimpleMapping(GlobalTableData t){
        //for each local table that matches with this global table
        String query = " SELECT * ";
        Set<TableData> localTables = t.getAllLocalTablesFromCols(t.getGlobalColumnDataList());
        for (TableData localTable : localTables){
            query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
        }
        return query;
    }

    private String handleHorizontalMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        String query ="SELECT ";
        String tableUnionString = "UNION SELECT ";
        //for each local table that matches with this global table
        Set<TableData> localTables = t.getAllLocalTablesFromCols(selectCols);
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

    private String handleHorizontalMapping(GlobalTableData t){
        String query ="SELECT * ";
        String tableUnionString = "UNION SELECT ";
        //for each local table that matches with this global table
        Set<TableData> localTables = t.getAllLocalTablesFromCols(t.getGlobalColumnDataList());
        for (TableData localTable : localTables){
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
        String tableJoinString = "INNER JOIN ";

        Set<TableData> localTables = t.getAllLocalTablesFromCols(t.getGlobalColumnDataList());
        ColumnData primaryKeyCol = null;
        List<ColumnData> foreignKeyCols = new ArrayList<>();
        //all columns  from local tables (get primary key)
        for (TableData localTable : localTables){
            Set<ColumnData> localCols = new HashSet<>(localTable.getColumnsList());
            for (ColumnData col: localCols){
                if (col.isPrimaryKey() && !col.hasForeignKey()){
                    primaryKeyCol = col;
                }
                else if (col.isPrimaryKey() && col.hasForeignKey() && localTableHasOneColumnInSelect(selectCols, localTable)) {//the foreign key table must have at least one column in the select clause
                    foreignKeyCols.add(col);
                }
            }
            //query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            //query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
        }
        Set<TableData> localTablesSelectCols = t.getLocalTablesFromCols_v(selectCols);
        //get all local tables in the selected
        for (TableData localTable : localTablesSelectCols){
            Set<ColumnData> localCols = new HashSet<>(localTable.getColumnsList());
            for (ColumnData col: localCols){
                query+=col.getCompletePrestoColumnName() +", ";
            }
            //query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            //query+= "FROM "+localTable.getCompletePrestoTableName()+" ";
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

    private boolean localTableCorrespondsToOneOfTheGlobalTables(List<GlobalColumnData> globalCols, ColumnData localCol){
        for (GlobalColumnData c : globalCols){
            if (c.getLocalColumns().contains(localCol)){
                return true;
            }
        }
        return false;
    }

    private boolean localTableHasOneColumnInSelect(List<GlobalColumnData> globalCols, TableData localTable){
        for (GlobalColumnData c : globalCols){
            for (ColumnData localCol : c.getLocalColumns()){
                if (localCol.getTable().equals(localTable))
                    return true;
            }
        }
        return false;
    }

    public void clearAllElements(){
        selectRows.clear();
        selectColumns.clear();
        measures.clear();
        orderBy.clear();
    }

    public Map<Integer, String> getMeasuresWithID() {
        Map<Integer, String> measuresWithOP = new HashMap<>();
        factsTable.getColumns().keySet();
        for (String m : measures){
            String measureName = getMeasureName(m);
            String measureOP = getMeasureOP(m);
            int id = getIDofMeasure(measureName);
            measuresWithOP.put(id, measureOP);
        }
        return measuresWithOP;
    }

    private int getIDofMeasure(String measureName){
        Set<GlobalColumnData> cols = factsTable.getColumns().keySet();
        for (GlobalColumnData c : cols){
            if (c.getName().equals(measureName))
                return c.getColumnID();
        }
        return -1;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getSelectRows() {
        return this.selectRows;
    }

    public void setSelectRows(Map<GlobalTableData, List<GlobalColumnData>> selectRows) {
        this.selectRows = selectRows;
    }

    public FactsTable getFactsTable() {
        return this.factsTable;
    }

    public void setFactsTable(FactsTable factsTable) {
        this.factsTable = factsTable;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getSelectColumns() {
        return this.selectColumns;
    }

    public void setSelectColumns(Map<GlobalTableData, List<GlobalColumnData>> selectColumns) {
        this.selectColumns = selectColumns;
    }

    public List<String> getMeasures() {
        return this.measures;
    }

    public void setMeasures(List<String> measures) {
        this.measures = measures;
    }

    public List<String> getOrderBy() {
        return this.orderBy;
    }

    public void setOrderBy(List<String> orderBy) {
        this.orderBy = orderBy;
    }

    public String getFilterQuery() {
        return filterQuery;
    }

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }
}
