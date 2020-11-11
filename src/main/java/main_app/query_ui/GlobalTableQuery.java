package main_app.query_ui;

import helper_classes.*;
import helper_classes.utils_other.Utils;
import main_app.presto_com.PrestoMediator;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;


public class GlobalTableQuery {

    private Map<GlobalTableData, List<GlobalColumnData>> selectRows;
    private Map<GlobalTableData, List<GlobalColumnData>> selectColumns;
    private Map<GlobalTableData, List<GlobalColumnData>> manualRowsAggr;
    private List<GlobalColumnData> measures;
    private List<GlobalColumnData> manualMeasures;
    private List<String> orderBy;
    private String filterQuery;
    private String colFilterQuery;
    private String filterAggrQuery;
    private String manualAggregationsStr;
    private Set<String> filters;
    private Set<String> colFilters;
    private boolean hasCountAll;
    private FactsTable factsTable;
    private List<GlobalTableData> dimensions;
    private PrestoMediator presto;

    private List<List<String>> pivotValues;

    private final static int LIMIT_NUMBER = 50000;
    public final static int MAX_SELECT_COLS = 3;
    public final static String MULTI_HEADER_SEPARATOR = "-";

    public GlobalTableQuery(PrestoMediator presto, FactsTable factsTable, List<GlobalTableData> dimensions) {
        this.presto = presto;
        this.factsTable = factsTable;
        this.dimensions = dimensions;
        selectRows = new HashMap<>();
        selectColumns = new HashMap<>();
        manualRowsAggr = new HashMap<>();
        measures = new ArrayList<>();
        manualMeasures = new ArrayList<>();
        orderBy = new ArrayList<>();
        pivotValues = new ArrayList<>();
        filters = new HashSet<>();
        colFilters = new HashSet<>();
        filterQuery = "";
        colFilterQuery = "";
        filterAggrQuery = "";
        hasCountAll = false;
        manualAggregationsStr = "";
    }

    public void addOrderByRow(String groupByRow){
        orderBy.add(groupByRow);
    }

    public void addSelectColumn(GlobalTableData table, GlobalColumnData col){
        if (selectColumns.size() >= MAX_SELECT_COLS){
            return;
        }
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
            if (listCols.contains(col)){
                //column present, compare aggregation operations (must all be different if column is duplicated)
                if( !listCols.get(listCols.indexOf(col)).getAggrOp().equals(col.getAggrOp()) ){
                    selectRows.get(table).add(col);
                }
            }
            else{ //column not present, add it
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

    public boolean updateSelectRowFromTable(GlobalTableData table, GlobalColumnData columnName, String oldAggr, String newAggr){
        List<GlobalColumnData> cols = selectRows.get(table);
        for (GlobalColumnData c : cols){
            if (c.equals(columnName)){
                if ( ((c.getAggrOpFullName() == null && c.getAggrOpFullName().isEmpty()) && oldAggr.isEmpty())
                        || c.getAggrOpFullName().equals(oldAggr)){//check column with same aggregation and update it
                    c.setAggrOp(newAggr);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean rowExists(GlobalTableData table, GlobalColumnData columnName, String aggr){
        List<GlobalColumnData> cols = selectRows.get(table);
        for (GlobalColumnData c : cols){
            if (c.equals(columnName)){
                if ( ((c.getAggrOpFullName() == null && c.getAggrOpFullName().isEmpty()) && aggr.isEmpty())
                        || c.getAggrOpFullName().equals(aggr)){//column with same name, same aggreate
                    return true;
                }
            }
        }
        return false;
    }

    //Adds measure, but checks if measures with same name and aggrOP exist, in this case it does not add
    public boolean addMeasure(GlobalColumnData measure){
        for (GlobalColumnData c : measures){
            if (c.equals(measure) && ((c.getAggrOp() == null && c.getAggrOp().isEmpty()) && measure.getAggrOp().isEmpty()
                    || c.getAggrOp().equals(measure.getAggrOp())) ){
                return false; //exact same column exists, return and do not add anything
            }
        }
        measures.add(measure);
        return true;
    }

    //searches for measure, and sets the aggrop given
    public void addAndReplaceMeasureAggrOP(GlobalColumnData measure, String olAggr, String newAggr){
        for (GlobalColumnData c : measures){
            if (c.equals(measure) &&  (((c.getAggrOp() == null && c.getAggrOp().isEmpty()) && olAggr.isEmpty() )
                    || c.getAggrOp().equals(olAggr)) ){
                c.setAggrOp(newAggr); //change aggr op of measure
                return;
            }
        }
    }

    public boolean measureExists(GlobalColumnData measure){
        for (GlobalColumnData c : measures){
            if (c.equals(measure) && ((c.getAggrOp() == null && c.getAggrOp().isEmpty()) && measure.getAggrOp().isEmpty()
                    || c.getAggrOp().equals(measure.getAggrOp())) ){
                return true; //exact same column exists, return and do not add anything
            }
        }
        return false;
    }

    public boolean measureExists(GlobalColumnData measure, String aggrOP){
        for (GlobalColumnData c : measures){
            if (c.equals(measure) && ((c.getAggrOp() == null && c.getAggrOp().isEmpty()) && aggrOP.isEmpty()
                    || c.getAggrOp().equals(aggrOP)) ){
                return true; //exact same column exists, return and do not add anything
            }
        }
        return false;
    }

    public boolean deleteSelectColumnFromTable(GlobalTableData table, GlobalColumnData columnName){
        boolean success = selectColumns.get(table).remove(columnName);
        if (success && selectColumns.get(table).size()==0){
            selectColumns.remove(table);
        }
        return success;
    }

    public boolean deleteSelectRowFromTable(GlobalTableData table, GlobalColumnData columnName){
        if (selectRows.isEmpty())
            return true;
        boolean success = selectRows.get(table).remove(columnName);
        List<GlobalColumnData> cols = selectRows.get(table);
        for (GlobalColumnData c : cols){
            if (c.equals(columnName)){
                if ( (c.getAggrOpFullName() == null && columnName.getAggrOpFullName() == null)
                        || c.getAggrOpFullName().equals(columnName.getAggrOpFullName())){//2 columns can exist, one with aggregation and other without
                    success = selectRows.get(table).remove(c);
                    break;
                }
            }
        }
        if (success && selectRows.get(table).size()==0){
            selectRows.remove(table);
        }
        removeOrderByIfPresent(table.getTableName()+"."+columnName.getName());
        return success;
    }

    public void deleteAllRowsWithAggregations(){
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> table : selectRows.entrySet()){
            List<GlobalColumnData> cols = table.getValue();
            for (int i = 0; i < cols.size(); i++ ){
                if (cols.get(i).getAggrOp()!=null && !cols.get(i).getAggrOp().isEmpty()){
                    cols.remove(i);
                }
            }
            if (cols.isEmpty())
                selectRows.remove(table.getKey());
        }
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

    public void addFilter(String column){
        if (column == null && column.isEmpty())
            return;
        filters.add(column);
    }

    public void addColFilter(String column){
        if (column == null && column.isEmpty())
            return;
        colFilters.add(column);
    }

    public Map<GlobalTableData, List<GlobalColumnData>> addRowsToListOfRows(Map<GlobalTableData, List<GlobalColumnData>> rows, Map<GlobalTableData, List<GlobalColumnData>> rowsToAdd){
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> table : rowsToAdd.entrySet()){
            GlobalTableData t = new GlobalTableData(table.getKey());
            if (t.getTableName().equals(factsTable.getGlobalTable().getTableName()))
                continue;
            List<GlobalColumnData> cs = rows.get(t);
            if (cs == null)
                continue;
            List<GlobalColumnData> cols = new ArrayList<>(cs);
            if (rows.containsKey(t)){
                for (GlobalColumnData col : table.getValue()){
                    if (!rows.get(t).contains(col)){
                        cols.add(col);
                    }
                }
                rows.put(table.getKey(), cols);
            }
            else{
                //add table and rows
                rows.put(t, new ArrayList<>(table.getValue()));
            }
        }
        return rows;
    }


    public void removeMeasure(GlobalColumnData column){
        for (GlobalColumnData c : measures){
            if (c.equals(column)){
                if ( (c.getAggrOpFullName() == null && column.getAggrOpFullName() == null)
                        || c.getAggrOpName().equals(column.getAggrOpName())){//2 columns can exist, one with aggregation and other without
                    measures.remove(c);
                    break;
                }
            }
        }
        removeOrderByIfPresent(column.getName());
    }

    public void removeFilter(String column){
        filters.remove(column);
    }

    public void removeColFilter(String column){
        colFilters.remove(column);
    }

    public String getLocalTableQuery(GlobalTableData t, List<GlobalColumnData> selectCols){
        MappingType mapping = t.getMappingType();
        if (mapping == MappingType.Simple)
            return handleSimpleMapping(t, selectCols);
        else if (mapping == MappingType.Horizontal)
            return handleHorizontalMapping(t, selectCols);
        else if (mapping == MappingType.Vertical)
            return handleVerticalMapping(t, selectCols);
        else
            return "Error: Invalid Mapping Type";
    }

    //add measure attributes on filters that are not on select clause
    private Set<GlobalColumnData> addFilterColumnIfNeededToMeasures(List<GlobalColumnData> measureAttrs) {
        Set<GlobalColumnData> measuresAttrsWithFiltersSelection = new HashSet<>(measureAttrs);
        //for each filter  measure, add the measures to the list of selected measures if not present in the list given as parameter
        if (filterQuery.length() > 0 && filters.size() > 0) {
            List<String> measureFilters = new ArrayList<>();
            for (String filterCol : filters) {
                String[] filterSplit = filterCol.split("\\.");
                String tableName = filterSplit[0];
                if (tableName.equals(factsTable.getGlobalTable().getTableName())) //only cols from facts table (measures)
                    measureFilters.add(filterCol);
            }
            for (String filterCol : measureFilters) {
                String[] filterSplit = filterCol.split("\\.");
                String colName = filterSplit[1];
                boolean isSelected = false;
                for (GlobalColumnData measure : measureAttrs) {
                    if (measure.getName().equals(colName)) {
                        isSelected = true; //already present, no need to add to the list of selected measures
                        break;
                    }
                }
                if (!isSelected){
                    //column in filter not in user selection, add it
                    GlobalColumnData c = getMeasureFromName(colName);
                    if (c!= null)
                        measuresAttrsWithFiltersSelection.add(c);
                }
            }
        }
        return measuresAttrsWithFiltersSelection;
    }

    private GlobalColumnData getMeasureFromName(String measureName){
        for (Map.Entry<GlobalColumnData, Boolean> factCols : factsTable.getColumns().entrySet()){
            if (factCols.getValue() == true && factCols.getKey().getName().equals(measureName))
                return factCols.getKey();
        }
        return null;
    }

    private Map<GlobalTableData, List<GlobalColumnData>> addFilterColumnIfNeeded(Set<String> filters, Map<GlobalTableData, List<GlobalColumnData>> selectAttrs){
        Map<GlobalTableData, List<GlobalColumnData>> attrs = new HashMap<>();
        attrs.putAll(selectAttrs);
        if (filters.size() > 0){
            for (String filterCol : filters){
                String [] filterSplit = filterCol.split("\\.");
                String tableName = filterSplit[0];
                String colName = filterSplit[1];
                boolean isSelected = false;
                for (Map.Entry<GlobalTableData, List<GlobalColumnData>> selectedTable : attrs.entrySet()){
                    GlobalTableData gt = selectedTable.getKey();
                    if (gt.getTableName().equals(tableName)) {
                        List<GlobalColumnData> cols = selectedTable.getValue();
                        for (GlobalTableData dim : dimensions) {
                            if (dim.getTableName().equals(gt.getTableName())) {
                                for (GlobalColumnData gc : selectedTable.getValue()) {
                                    if (gc.getName().equals(colName)) {
                                        isSelected = true;
                                        break;                  //this filter is already present in user selection
                                    }
                                }
                                if (!isSelected){
                                    //column in filter not in user selection, add it
                                    cols.add(dim.getGlobalColumnData(colName));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return attrs;
    }

    public String getLocalTableQuery(GlobalTableData t){
        MappingType mapping = t.getMappingType();
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
    public String buildQuerySelectRowsOnly(boolean includeInnerQueries){
        String query = "SELECT ";
        boolean hasAggregations = false;
        //add manual aggregations typed by user
        if (!manualAggregationsStr.isEmpty()) {
            hasAggregations = true;
            query += manualAggregationsStr.trim();
            if (query.charAt(query.length()-1) != ',' )
                query+=',';

        }
        //add "count(*)" if selected
        if (hasCountAll)
            query+="Count(*),";
        //first add to the select the dimensions columns
        String selectColsNoAggr = "";
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> dimTable : selectRows.entrySet()){
            List<GlobalColumnData> cols = dimTable.getValue();
            GlobalTableData t = dimTable.getKey();
            for (GlobalColumnData c : cols){
                if (c.getAggrOp()!=null && !c.getAggrOp().isEmpty()  &&!c.getAggrOp().equalsIgnoreCase("Group By")){
                    if (c.isOriginalDatatypeChanged())
                        query += c.getAggrOp()+"(CAST( "+c.getFullNameEscapped()+"AS "+c.getDataType()+")) AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\","; //aggrOP("CAST( table.column as datatype)as "aggr of table.column"
                    else
                        query += c.getAggrOpFullNameEscapped()+" AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\",";// aggregationOP (table.column) as "aggrOP of table.column"
                    hasAggregations = true;
                }
                else {
                    if (c.isOriginalDatatypeChanged())
                        query += "CAST("+c.getFullNameEscapped()+" AS "+c.getDataType()+")AS \""+c.getFullName()+"\",";// or c.getFullname();
                    else
                        query += c.getFullNameEscapped()+",";// or c.getFullname();
                    selectColsNoAggr += t.getTableName() + "." + c.getName() +",";
                    if (c.getAggrOp().equalsIgnoreCase("Group By"))
                        hasAggregations = true;
                }
            }
        }

        query = query.substring(0, query.length() - 1);//last elemment without comma
        if (selectColsNoAggr.length()> 0)
            selectColsNoAggr = selectColsNoAggr.substring(0, selectColsNoAggr.length() - 1);//last elemment without comma

        query+= " FROM ";

        //check if all tables and columns of filters are selected
        Map<GlobalTableData, List<GlobalColumnData>> selectRowsCopy = addFilterColumnIfNeeded(filters, selectRows);
        selectRowsCopy = addRowsToListOfRows(selectRowsCopy, manualRowsAggr);//add any manual aggregation rows added

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRowsCopy.entrySet()){
            //for each global table
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();
            //query+= "FROM (";
            if (includeInnerQueries) {
                String subQueries = "(" + getLocalTableQuery(t, rowsForSelect);

                if (subQueries.contains("Error")) {
                    return subQueries;//propagate error
                }
                query += subQueries;
                query += ") AS \"" + t.getTableName() + "\",";
            }
            else{
                query += t.getTableName() + ",";
            }
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma

        if (filterQuery.length() > 0){
            query += " WHERE " + filterQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        //add group by if aggregations exist and non aggregate columns exist as well
        if (hasAggregations && selectColsNoAggr.length() > 0){
            query += " GROUP BY ("+selectColsNoAggr+")"; //group columns that do not have aggregations
        }

        //add having clause if exist
        if (filterAggrQuery.length() > 0){
            query += " HAVING " + filterAggrQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        return query;
    }


    //Creates a 'SELECT XXX FROM ( )  with the necessary inner queries to get local schema data and join facts foreign keys with dims. Also
    //performs aggregations on the measures and groups the dimensions rows

    public String buildQuerySelectRowsAndMeasures(boolean selectMeasure, boolean includeInnerQueries) {
        String query = "SELECT ";

        boolean hasAggregations = false;
        //add manual aggregations typed by user
        if (!manualAggregationsStr.isEmpty()) {
            hasAggregations = true;
            query += manualAggregationsStr.trim();
            if (query.charAt(query.length()-1) != ',' )
                query+=',';
        }

        //add "count(*)" if selected
        if (hasCountAll)
            query+="Count(*),";

        Map<GlobalTableData, List<GlobalColumnData>> tableSelectRowsWithPrimKeys = new HashMap<>();
        Set<String> selectColsNoAggr = new HashSet<>(); //elements that need to be added to group by because there are other elements containing aggregate functions
        Set<String> selectColsGroupBy = new HashSet<>(); //elements that need to be added to group by because user selected them to be part of there are other elements containing aggregate functions
        //first add to the select the dimensions columns
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> dimTable : selectRows.entrySet()){
            List<GlobalColumnData> cols = dimTable.getValue();
            GlobalTableData t = dimTable.getKey();
            GlobalTableData newt = new GlobalTableData(t.getTableName());
            newt.setId(t.getId());
            List<GlobalColumnData> newCols = new ArrayList<>();
            boolean primKeyIsSelected = false;
            for (GlobalColumnData c : cols){
                if (c.getAggrOp()!=null && !c.getAggrOp().isEmpty()  && !c.getAggrOp().equalsIgnoreCase("Group By")){ //agregations
                    hasAggregations = true;
                    if (c.isOriginalDatatypeChanged())
                        query += c.getAggrOp()+"CAST( "+c.getFullNameEscapped() + "AS "+c.getDataType()+") AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\","; //aggrOP("CAST( table.column as datatype)as "aggr of table.column"
                    else
                        query += c.getAggrOpFullNameEscapped()+" AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\",";// aggregationOP (table.column) as "aggrOP of table.column"
                    //query += c.getAggrOpFullName()+" AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\",";// aggregationOP (table.column) as "aggrOP of table.column"
                }
                else {
                    if (c.isOriginalDatatypeChanged())
                        query += "CAST("+c.getFullNameEscapped()+" AS "+c.getDataType()+")AS \""+c.getFullName()+"\",";// CAST(table.col as datatype) as table.col
                    else
                        query += ""+c.getFullNameEscapped()+ ",";
                    selectColsNoAggr.add(c.getFullNameEscapped());
                    if (c.getAggrOp().equalsIgnoreCase("Group By")) {//no aggregate but selected to be added to group by
                        selectColsGroupBy.add(c.getFullNameEscapped());
                    }
                }
                //get primary keys of dimensions
                GlobalColumnData newC = new GlobalColumnData(c.getName(), c.getDataType(), c.isPrimaryKey(), c.getLocalColumns());
                newCols.add(newC);
                if (c.isPrimaryKey()) {
                    primKeyIsSelected = true;
                }
            }
            if (!primKeyIsSelected){
                List<GlobalColumnData> cps = dimTable.getKey().getPrimaryKeyColumns(); //primary key column missing
                for (GlobalColumnData cp : cps)
                    newCols.add(new GlobalColumnData(cp.getName(), cp.getDataType(), cp.isPrimaryKey(), cp.getLocalColumns()));
            }
            newt.setGlobalColumnData(newCols);
            tableSelectRowsWithPrimKeys.put(newt, newCols);
        }


        //list of measures that are not aggregated, and therefore must appear on the group by clause
        if (selectMeasure) {
            List<String> measureNames = new ArrayList<>();
            for (GlobalColumnData measureCol : measures) {
                String measureName = measureCol.getName();
                if (measureNames.contains(measureName)){
                    break;
                }
                measureNames.add(measureName);
            }
            //add to the select the measures with the aggregation operation (in the form 'aggr(measureName)'). This a string taken from the drop area in the interface.
            for (GlobalColumnData measureCol : measures) {
                if (measureCol.getAggrOp()!=null && !measureCol.getAggrOp().isEmpty() && !measureCol.getAggrOp().equalsIgnoreCase("Group By")){//no  aggregation, only add measure name
                    //query += measureName + " AS " + measureAlias + ",";
                    String measureAlias = "\"" + measureCol.getAggrOp() + " of " + measureCol.getName() + "\"";
                    if (measureCol.isOriginalDatatypeChanged())
                        query += measureCol.getAggrOp()+"(CAST(\""+measureCol.getName()+"\" AS "+measureCol.getDataType()+")) AS " + measureAlias + ",";//aggr(cast col as datatype) as "agrop of col"
                    else
                        query += "\""+measureCol.getAggrOpName() + "\" AS " + measureAlias + ",";
                    hasAggregations = true;
                }
                else{
                    if (measureCol.isOriginalDatatypeChanged())
                        query += "CAST(\""+measureCol.getName() + "\" AS "+measureCol.getDataType()+") AS \""+measureCol.getName()+"\",";
                    else
                        query += "\""+measureCol.getName() + "\",";
                    if( measureCol.getAggrOp() != null && measureCol.getAggrOp().equalsIgnoreCase("Group By"))
                        selectColsGroupBy.add("\""+factsTable.getGlobalTable().getTableName() + "\".\"" + measureCol.getName()+"\"");
                    else
                        selectColsNoAggr.add("\""+factsTable.getGlobalTable().getTableName() + "\".\"" + measureCol.getName()+"\"");
                }
            }
        }

        query = query.substring(0, query.length() - 1);//last elemment without comma

        query+= " FROM ";

        //check if all tables and columns of filters are selected
        tableSelectRowsWithPrimKeys = addFilterColumnIfNeeded(filters, tableSelectRowsWithPrimKeys);
        tableSelectRowsWithPrimKeys = addRowsToListOfRows(tableSelectRowsWithPrimKeys, manualRowsAggr);

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : tableSelectRowsWithPrimKeys.entrySet()){
            //for each global column create inner queries in the 'From' clause
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();
            if (includeInnerQueries) {

                String subQueries = getLocalTableQuery(t, rowsForSelect);

                if (subQueries.contains("Error")) {
                    return subQueries;//propagate error
                }
                query += "(" + subQueries;
                //Join on facts table
                query += ") AS \"" + t.getTableName() + "\",";
            }
            else{
                query += t.getTableName() + ",";
            }
        }

        List <GlobalColumnData> measuresCopy = new ArrayList<>();
        if (measures.size() > 0)
            measuresCopy = new ArrayList<>(measures);
        else if (manualMeasures.size() > 0) {
            measuresCopy = new ArrayList<>(manualMeasures);
        }
        measuresCopy = new ArrayList<>(addFilterColumnIfNeededToMeasures(measuresCopy));
        //get facts table in from clause
        //get necessary attributes of facts table
        List<GlobalColumnData> factsCols = new ArrayList<>();
        for (Map.Entry<GlobalColumnData, Boolean> factsCol : factsTable.getColumns().entrySet()){
            //for each column, if it's measure, check if it was selected and addit. For foreign keys, check if they are needed and add them for selection
            Boolean isMeasure = factsCol.getValue();
            GlobalColumnData col = factsCol.getKey();
            if (isMeasure){
                for (GlobalColumnData m : measuresCopy) {
                    if (col.getName().equals(m.getName())) {
                        factsCols.add(col);
                        break;
                    }
                }
            }
            else if (col.isForeignKey()){
                String fk = col.getForeignKey();//globTable.globCol
                String tableName = fk.split("\\.")[0];
                for (Map.Entry<GlobalTableData, List<GlobalColumnData>> selectRow : selectRows.entrySet()){
                    if (selectRow.getKey().getTableName().equals(tableName)){
                        factsCols.add(col);
                        break;
                    }
                }
            }
        }
        if (includeInnerQueries) {
            String subQueries = "(" + getLocalTableQuery(factsTable.getGlobalTable(), factsCols);
            if (subQueries.contains("Error")) {
                return subQueries;//propagate error
            }
            query += subQueries;
            query += ") AS \"" + factsTable.getGlobalTable().getTableName() + "\" ";
        }
        else{
            query += factsTable.getGlobalTable().getTableName() + " ";
        }

        //perform Where on facts foreign key fields = dimensions referenced primary keys
        if (tableSelectRowsWithPrimKeys.size() > 0) {
            query += " WHERE (";

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
                                String dimCol = "";
                                if (referencedCol.isOriginalDatatypeChanged())
                                    dimCol = "CAST ("+"\""+tableDim.getTableName() + "\".\"" + referencedCol.getName() + "\" AS "+referencedCol.getDataType()+")"; //changed data type, perform cast
                                else
                                    dimCol = "\""+tableDim.getTableName() + "\".\"" + referencedCol.getName() + "\""; //no change in data type

                                String factColStr = "";
                                if (factsCol.isOriginalDatatypeChanged())
                                    factColStr = "CAST (\""+factsTable.getGlobalTable().getTableName()+"\".\""+ factsCol.getName()+"\" AS "+factsCol.getDataType()+")"; //changed data type, perform cast
                                else
                                    factColStr = "\""+factsTable.getGlobalTable().getTableName()+"\".\""+ factsCol.getName()+"\""; //no change in data type

                                query += dimCol + " = " + factColStr;
                                query += " AND ";
                                break;
                            }
                        }
                    }
                }
            }

            query = query.substring(0, query.length() - "AND ".length());//last column is whithout a comma
            query += ")"; //close where clause
        }

        //aditional filters set by user here
        if (filterQuery.length() > 0){
            query += " AND ( " + filterQuery +")";//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        //groupby for each dimension column (only if aggregation operation is made)
        if ( (hasAggregations && selectColsNoAggr.size() > 0) || selectColsGroupBy.size()>0) {
            query += " GROUP BY ( ";

            if (hasAggregations){
                //add to groupby columns that do not have aggregation and other specified to be on group by
                selectColsNoAggr.addAll(selectColsGroupBy); //combine all in order to remove duplicates
                for (String s : selectColsNoAggr)
                    query += s +",";
            }
            else { //add specified to be on group by (if any)
                for (String s : selectColsGroupBy)
                    query += s + ",";
            }
            query = query.substring(0, query.length() - 1);//last elemment without comma
            query += ")"; //close group by
        }

        //add having clause if exist
        if (filterAggrQuery.length() > 0){
            query += " HAVING " + filterAggrQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        return query;
    }

    public String buildQuerySelectRowsColsAndMeasures(boolean includeInnerQueries) {

        String query = "SELECT ";
        //add "count(*)" if selected
        if (hasCountAll)
            query+="Count(*),";
        int nSelectCols = 0;
        //get distinct values of columns. If multiple columns, get all distinct combinations
        List<List<String>> valuesByGlobalCol = new ArrayList<>();
        if (includeInnerQueries)
            valuesByGlobalCol = getAllDifferentValuesOfColumn();

        boolean hasAggregations = false;
        Set<String> selectColsNoAggr = new HashSet<>(); //elements that need to be added to group by because there are other elements containing aggregate functions
        Set<String> selectColsGroupBy = new HashSet<>(); //elements that need to be added to group by because user selected them to be part of there are other elements containing aggregate functions


        //add to select atributes in the 'rows' area
        Map<GlobalTableData, List<GlobalColumnData>> tableSelectRowsWithPrimKeys = new HashMap<>();
        //first add to the select the dimensions columns
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> dimTable : selectRows.entrySet()){
            List<GlobalColumnData> cols = dimTable.getValue();
            GlobalTableData t = dimTable.getKey();
            GlobalTableData newt = new GlobalTableData(t.getTableName());
            newt.setId(t.getId());
            List<GlobalColumnData> newCols = new ArrayList<>();
            boolean primKeyIsSelected = false;
            for (GlobalColumnData c : cols){
                if (c.getAggrOp()!=null && !c.getAggrOp().isEmpty()  && !c.getAggrOp().equalsIgnoreCase("Group By")){ //agregations
                    hasAggregations = true;
                    query += c.getAggrOpFullNameEscapped()+" AS \""+c.getAggrOp().toLowerCase() +" of "+c.getFullName()+"\",";// aggregationOP (table.column) as "aggrOP of table.column"
                }
                else {
                    query += c.getFullNameEscapped()+",";// no aggregation
                    selectColsNoAggr.add(c.getFullNameEscapped());
                    if (c.getAggrOp().equalsIgnoreCase("Group By")) {//no aggregate but selected to be added to group by
                        selectColsGroupBy.add(c.getFullNameEscapped());
                    }
                }
                nSelectCols++;
                GlobalColumnData newC = new GlobalColumnData(c.getName(), c.getDataType(), c.isPrimaryKey(), c.getLocalColumns());//new references so that the originals are unnaltered
                newCols.add(newC);
                if (c.isPrimaryKey()) {
                    primKeyIsSelected = true;
                }
            }

            if (!primKeyIsSelected){
                List<GlobalColumnData> cps = dimTable.getKey().getPrimaryKeyColumns(); //primary key column missing
                for (GlobalColumnData cp : cps)
                    newCols.add(new GlobalColumnData(cp.getName(), cp.getDataType(), cp.isPrimaryKey(), cp.getLocalColumns(), cp.getColumnID()));
            }
            newt.setGlobalColumnData(newCols);
            tableSelectRowsWithPrimKeys.put(newt, newCols);
        }

        query+=" ";
        //get ordered list of columns's fullnames:

        List<String> colNames = new ArrayList<>();
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> colSelect : selectColumns.entrySet()){
            GlobalTableData t = colSelect.getKey();
            List<GlobalColumnData> cs = colSelect.getValue();
            for (GlobalColumnData c : cs){
                colNames.add(t.getTableName()+"."+c.getName());
            }
        }

        //clauses to create columns
        //for each measure, iterate
        List<Integer> groupByPivotCols = new ArrayList<>();
        if (includeInnerQueries) {
            if (valuesByGlobalCol.size() > 0 && colNames.size() != valuesByGlobalCol.get(0).size()){//list of columns of values must be same length as list of values of columns
                return "Error";
            }

            GlobalColumnData measure = measures.get(0); //only one measure is used here.
            String pivotStatement = "";
            if (measure.getAggrOp().equalsIgnoreCase("COUNT")) {
                pivotStatement = " SUM(CASE WHEN %s AND " + measure.getName() + " IS NOT NULL THEN 1 ELSE 0 END) AS %s, ";
                hasAggregations = true;
            } else if (measure.getAggrOp().equalsIgnoreCase("SUM")) {
                pivotStatement = " SUM(CASE WHEN %s THEN " + measure.getName() + " ELSE 0 END) AS %s, ";
                hasAggregations = true;
            } else if (measure.getAggrOp().equalsIgnoreCase("AVG")) {
                pivotStatement = " AVG(CASE WHEN %s THEN " + measure.getName() + " ELSE NULL END) AS %s, ";
                hasAggregations = true;
            } else if (measure.getAggrOp().equalsIgnoreCase("SIMPLE") || measure.getAggrOp().isEmpty() || measure.getAggrOp().equalsIgnoreCase("Group By")) {
                pivotStatement = " (CASE WHEN %s THEN " + measure.getName() + " ELSE 0 END) AS %s, ";
                groupByPivotCols.add(nSelectCols);
            }
            for (List<String> pairs : valuesByGlobalCol) {//for each list of value of each column
                String valueColEnumeration = ""; //colName = value AND ColName = value etc...
                String valueAlias = ""; //as aliasName
                List<String> valuesRaw = new ArrayList<>();
                for (int i = 0; i < pairs.size(); i++) {//v is already escaped with ''
                    String v = pairs.get(i);
                    String valueRaw = ""; //value with no singe quote
                    if (v.charAt(0) == '\'' && v.charAt(v.length() - 1) == '\'') {
                        valueRaw = v.substring(1, v.length() - 1);//remove the ' at the beginning and end of string
                    } else
                        valueRaw = v;
                    valueRaw = valueRaw.replaceAll("''", "'");//all ' were replaced by '' to be escaped when used as column anmes for presto. Convert back to single '
                    //valueRaw = v.replaceAll("'", "");
                    valuesRaw.add(valueRaw);
                    //if (valueRaw.isEmpty())
                    //valueRaw ="''";
                    valueColEnumeration += colNames.get(i) + " = " + v + " AND ";//building a colName = value AND ColName = value etc...
                    valueAlias += valueRaw + MULTI_HEADER_SEPARATOR;
                }
                pivotValues.add(valuesRaw);
                //remove last AND from enumerations and last _ from alias
                valueColEnumeration = valueColEnumeration.substring(0, valueColEnumeration.length() - " AND ".length());//remove last AND from enumeration
                valueAlias = valueAlias.substring(0, valueAlias.length() - MULTI_HEADER_SEPARATOR.length());//remove last - from alias
                if (valueAlias.trim().isEmpty()) {//empty alias name, add a new name
                    valueAlias = "empty";
                }
                valueAlias = "\"" + valueAlias + "\"";//space between chars in alias is not allowed, add double quotes
                nSelectCols++;
                query += String.format(pivotStatement, valueColEnumeration, valueAlias);
            }
        }
        else{
            GlobalColumnData measure = measures.get(0);
            if ((measure.getAggrOp() == null && measure.getAggrOp().isEmpty()) || measure.getAggrOp().equals("Group By"))
                query+= measure.getName();
            else
                query+= measure.getAggrOpName() + "AS \""+measure.getAggrOp() +" of "+measure.getName()+"\"";
            for (String colName : colNames)
                query+=",PIVOT("+colName+"),";
        }
        query = query.substring(0, query.length() - ", ".length());//last elemment without comma
        query+= " FROM ";

        tableSelectRowsWithPrimKeys = addFilterColumnIfNeeded(filters, tableSelectRowsWithPrimKeys);

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : tableSelectRowsWithPrimKeys.entrySet()){
            //for each global column create inner queries in the 'From' clause
            GlobalTableData t = tableSelectRows.getKey();
            List<GlobalColumnData> rowsForSelect = tableSelectRows.getValue();
            if (selectColumns.containsKey(t)){
                List<GlobalColumnData> colsForSelect = selectColumns.get(t);
                for (GlobalColumnData colForSelect : colsForSelect) {
                    if (!rowsForSelect.contains(colForSelect)) { //in the subqueries, must also fetch the attributes in the 'columns' area
                        rowsForSelect.add(colForSelect);
                    }
                }
            }

            if (includeInnerQueries) {
                String subQueries = getLocalTableQuery(t, rowsForSelect);

                if (subQueries.contains("Error")) {
                    return subQueries;//propagate error
                }
                query += "(" + subQueries;
                query += ") AS \"" + t.getTableName() + "\",";
            }
            else{
                query += t.getTableName() + ",";
            }
        }

        Map<GlobalTableData, List<GlobalColumnData>> selectColsCopy = new HashMap<>(selectColumns);
        //do the same for columns selected but with tables that were not previously included, if any
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectCols : selectColumns.entrySet()){
            GlobalTableData gt = tableSelectCols.getKey();
            if (!selectRows.keySet().contains(gt) && dimensions.contains(gt)){//search in rows if table is selected (if so, it has been added)
                //table not added to from clause, add the table and also the primk key to selected attributes
                GlobalTableData dimTable = dimensions.get(dimensions.indexOf(gt));
                List<GlobalColumnData> cs = selectColsCopy.get(gt);
                cs.addAll(dimTable.getPrimaryKeyColumns());
                selectColsCopy.put(gt, cs);
            }
            else{
                selectColsCopy.remove(gt);//remove and do not add table to for, already added in the rows
            }
        }

        //selectColsCopy = addFilterColumnIfNeeded(selectColsCopy);

        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectCols : selectColsCopy.entrySet()){
            //for each global column create inner queries in the 'From' clause
            GlobalTableData t = tableSelectCols.getKey();
            List<GlobalColumnData> colsForSelect = tableSelectCols.getValue();

            if (includeInnerQueries) {
                String subQueries = getLocalTableQuery(t, colsForSelect);

                if (subQueries.contains("Error")) {
                    return subQueries;//propagate error
                }
                query += "(" + subQueries;
                //Join on facts table
                query += ") AS \"" + t.getTableName() + "\",";
            }
            else{
                query += t.getTableName() + ",";
            }
        }

        //get facts table in from clause
        //get necessary attributes of facts table
        List<GlobalColumnData> factsCols = new ArrayList<>();
        for (Map.Entry<GlobalColumnData, Boolean> factsCol : factsTable.getColumns().entrySet()){
            //for each column, if it's measure, check if it was selected and addit. For foreign keys, check if they are needed and add them for selection
            Boolean isMeasure = factsCol.getValue();
            GlobalColumnData col = factsCol.getKey();
            if (isMeasure){
                for (GlobalColumnData m : measures) {
                    if (col.getName().equals(m.getName())) {
                        factsCols.add(col);
                        break;
                    }
                }
            }
            else if (col.isForeignKey()){
                String fk = col.getForeignKey();//globTable.globCol
                String tableName = fk.split("\\.")[0];
                for (Map.Entry<GlobalTableData, List<GlobalColumnData>> selectRow : selectRows.entrySet()){
                    if (selectRow.getKey().getTableName().equals(tableName)){
                        factsCols.add(col);
                        break;
                    }
                }
            }
        }

        if (includeInnerQueries) {
            String subQueries = "(" + getLocalTableQuery(factsTable.getGlobalTable(), factsCols);
            if (subQueries.contains("Error")) {
                return subQueries;//propagate error
            }
            query += subQueries;
            query += ") AS " + factsTable.getGlobalTable().getTableName() + " ";
        }
        else{
            query += factsTable.getGlobalTable().getTableName() + " ";
        }

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
                            String dimCol = "";
                            if (referencedCol.isOriginalDatatypeChanged())
                                dimCol = "CAST ("+"\""+tableDim.getTableName() + "\".\"" + referencedCol.getName() + "\" AS "+referencedCol.getDataType()+")"; //changed data type, perform cast
                            else
                                dimCol = "\""+tableDim.getTableName() + "\".\"" + referencedCol.getName() + "\""; //no change in data type

                            String factColStr = "";
                            if (factsCol.isOriginalDatatypeChanged())
                                factColStr = "CAST (\""+factsTable.getGlobalTable().getTableName()+"\".\""+ factsCol.getName()+"\" AS "+factsCol.getDataType()+")"; //changed data type, perform cast
                            else
                                factColStr = "\""+factsTable.getGlobalTable().getTableName()+"\".\""+ factsCol.getName()+"\""; //no change in data type


                            query += dimCol + " = " + factColStr;
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
        if ( (hasAggregations && selectColsNoAggr.size() > 0) || selectColsGroupBy.size()>0) {
            query += " GROUP BY ( ";

            if (hasAggregations){
                //add to groupby columns that do not have aggregation and other specified to be on group by
                selectColsNoAggr.addAll(selectColsGroupBy); //combine all in order to remove duplicates
                for (String s : selectColsNoAggr)
                    query += s +",";
                for (Integer pivotColNumber : groupByPivotCols) {
                    query += pivotColNumber + ",";
                }
            }
            else { //add specified to be on group by (if any)
                for (String s : selectColsGroupBy)
                    query += s + ",";
            }
            query = query.substring(0, query.length() - 1);//last elemment without comma
            query += ")"; //close group by
        }
        /*if (hasAggregations && selectColsNoAggr.size() > 0 || selectColsGroupBy.size() > 0) {
            query += " GROUP BY ( ";
            for (Map.Entry<GlobalTableData, List<GlobalColumnData>> tableSelectRows : selectRows.entrySet()) {
                GlobalTableData table = tableSelectRows.getKey();
                List<GlobalColumnData> columns = tableSelectRows.getValue();
                for (GlobalColumnData col : columns) {
                    if (col.getAggrOp() == null || col.getAggrOp().isEmpty())
                        query += table.getTableName() + "." + col.getName() + ",";
                }
            }
            //add any pivoted columns without the aggregation
            for (Integer pivotColNumber : groupByPivotCols) {
                query += pivotColNumber + ",";
            }
            query = query.substring(0, query.length() - 1);//last elemment without comma
            query += ")"; //close group by
        }*/

        //add having clause if exist
        if (filterAggrQuery.length() > 0){
            query += " HAVING " + filterAggrQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }

        return query;
    }


    public List<List<String>>  getAllDifferentValuesOfColumn(){
        List<GlobalColumnData> allCols = new ArrayList<>();
        List<GlobalTableData> allTables = new ArrayList<>();
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> colSelect : selectColumns.entrySet()){
            GlobalTableData t = colSelect.getKey();
            allTables.add(t);
            List<GlobalColumnData> cs = colSelect.getValue();
            for (GlobalColumnData c : cs){
                c.setFullName(t.getTableName()+"."+c.getName());
                allCols.add(c);
            }
        }
        String query = "SELECT DISTINCT (";
        for (GlobalColumnData c : allCols){
            query+=c.getFullName()+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma

        Map<GlobalTableData, List<GlobalColumnData>> selectColsCopy = addFilterColumnIfNeeded(colFilters, selectColumns);
        query+=") FROM ";
        for (GlobalTableData t : allTables){
            String subQueries = getLocalTableQuery(t, selectColsCopy.get(t));

            if(subQueries.contains("Error")){
                return null;//propagate error
            }
            query+="("+subQueries;
            //Join on facts table
            query+= ") AS " + t.getTableName()+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma

        //where, if any filter
        if (colFilters.size() > 0 && colFilterQuery.length() > 0){
            query+=" WHERE " + colFilterQuery;
        }
        query+=" ORDER BY (";
        for (GlobalColumnData c : allCols){
            query+=c.getFullName()+",";
        }
        query = query.substring(0, query.length() - 1);//last elemment without comma
        query+=")";
        System.out.println("value query: "+query);
        ResultSet results = presto.getLocalTablesQueries(query);
        List<List<String>> values = new ArrayList<>();//tablename.columnname; List of values
        try {
            //results.beforeFirst(); //return to begining
            ResultSetMetaData rsmd = results.getMetaData();
            while(results.next()){
                //Fetch each rows from the ResultSet, and add to ArrayList of different values
                // ResultSet column indices start at 1
                List<String> valuePairs = new ArrayList<>();
                if (allCols.size() == 1) {
                    String value = results.getString(1);
                    if (!Utils.stringIsNumericOrBoolean(value))
                        value = "'" + value.replaceAll("'", "''") + "'";//2 single quotes to escape any single quotes in the string ('Women's dress' -> ''Women''s dress'
                    valuePairs.add(value);
                    values.add(valuePairs);//add list of values
                }
                else if (allCols.size() > 1){
                    Map<String, String> valueFields = (Map<String, String>) results.getObject(1);//returns only one columns with a map with fieldx=value, fieldy=value...
                    for (Map.Entry<String, String> valueField : valueFields.entrySet()){//mapp with key=fieldName, value=value from select
                        String value = valueField.getValue();
                        if (!Utils.stringIsNumericOrBoolean(value))//if string, add single quotes as this value will be used to create queries
                            value = "'"+value.replaceAll("'", "''")+"'";//2 single quotes to escape any single quotes in the string ('Women's dress' -> ''Women''s dress'
                        valuePairs.add(value);
                    }
                    values.add(valuePairs);//add the list of pair values.
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        colFilterQuery = "";
        colFilters.clear();
        return values;
    }

    public String buildQuery(boolean includeInnerQueries){
        this.pivotValues.clear();//reset elements
        String query = "";
        if ((selectColumns.size() == 0 || manualRowsAggr.size()>0) && (measures.size() == 0 && manualMeasures.size() == 0) && selectRows.size() > 0){
            query = buildQuerySelectRowsOnly(includeInnerQueries);
        }
        else if (selectColumns.size() == 0 && (measures.size() > 0 || manualMeasures.size() > 0) && selectRows.size() >= 0){
            query = buildQuerySelectRowsAndMeasures(true, includeInnerQueries);
        }
        else if (selectColumns.size() > 0 && (measures.size() == 1 || manualMeasures.size() == 1) && selectRows.size() > 0){
            query = buildQuerySelectRowsColsAndMeasures(includeInnerQueries);
        }
        else {
            //error, query cannot be executed
            if (selectColumns.size() > 0 && (measures.size() != 1 || manualMeasures.size() != 1))
                return "One measure, and only one, must be selected when submiting queries containing pivoted attributes.";
            else
                return "Error invalid query elements given";
        }
        //add order by elements if any are selected
        if (orderBy.size() > 0){
            query += " ORDER BY ";
            for (String s : orderBy){
                query+=s +",";
            }
            query = query.substring(0, query.length() - 1);//last elemment without comma
        }
        //Add limit of max lines
        if (includeInnerQueries)
            query += " LIMIT "+LIMIT_NUMBER;

        //clear manual query data structures if any has data
        manualAggregationsStr = "";
        filters.clear();
        filterQuery = "";
        filterAggrQuery = "";
        manualRowsAggr.clear();
        manualMeasures.clear();
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
                ColumnData c = localCols.get(i);
                if (localTable.hasViewUsingSQL())
                    query+= "\""+c.getName()+"\"";
                else
                    query+=c.getCompletePrestoColumnNameEscaped();
                if (!c.getName().equals(c.getGlobalColumnName())){//if this local column's name is not the same as the matched global column, add an alias
                    query+=" AS \""+c.getGlobalColumnName()+"\"";
                }
                query+=", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma
            //from clause, select local tables, or apply sql code if available
            if (localTable.getSqlCode() == null)
                query+= "FROM "+localTable.getCompletePrestoTableNameEscapped()+" ";
            else
                query+= "FROM ("+localTable.getSqlCode()+") ";
        }
        return query;
    }
    private String handleSimpleMapping(GlobalTableData t){
        //for each local table that matches with this global table
        String query = " SELECT * ";
        Set<TableData> localTables = t.getAllLocalTablesFromCols(t.getGlobalColumnDataList());
        //from clause, select local tables, or apply sql code if available
        for (TableData localTable : localTables){
            if (localTable.getSqlCode() == null)
                query+= "FROM "+localTable.getCompletePrestoTableNameEscapped()+" ";
            else
                query+= "FROM ("+localTable.getSqlCode()+") ";
        }
        return query;
    }

    private String handleHorizontalMapping(GlobalTableData t, List<GlobalColumnData> selectCols){
        String query ="SELECT ";
        String tableUnionString = "UNION SELECT ";
        //for each local table that matches with this global table
        Set<TableData> localTables = t.getAllLocalTablesFromCols(selectCols);
        List<ColumnData> localCols = localTables.iterator().next().getColumnsList();
        List<ColumnData> localColNames = new ArrayList<>();
        for (ColumnData c : localCols){
            localColNames.add(c);
        }
        for (TableData localTable : localTables){
            /*List<ColumnData> localCols = localTable.getColumnsList();
            for (int i = 0; i < localCols.size()-1; i++){
                query+=localCols.get(i).getCompletePrestoColumnName() +", ";
            }
            query+=localCols.get(localCols.size()-1).getCompletePrestoColumnName()+" ";//last column is whithout a comma*/
            for (ColumnData col : localColNames){
                //select column names
                if (localTable.hasViewUsingSQL())
                    query+= "\""+col.getName()+"\"";
                else
                    query+=col.getCompletePrestoColumnNameEscaped();
                if (!col.getName().equals(col.getGlobalColumnName())){//if this local column's name is not the same as the matched global column, add an alias
                    query+=" AS \""+col.getGlobalColumnName()+"\"";
                }
                query+=", ";
            }
            query = query.substring(0, query.length() - ", ".length());
            //from clause, select local tables, or apply sql code if available
            if (localTable.getSqlCode() == null)
                query+= "FROM "+localTable.getCompletePrestoTableNameEscapped()+" ";
            else
                query+= "FROM ("+localTable.getSqlCode()+") ";
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
        //from clause, select local tables, or apply sql code if available
        for (TableData localTable : localTables){
            if (localTable.getSqlCode() == null)
                query+= "FROM "+localTable.getCompletePrestoTableNameEscapped()+" ";
            else
                query+= "FROM ("+localTable.getSqlCode()+") ";
            query+=tableUnionString;
        }
        if (query.endsWith(tableUnionString)) {
            return query.substring(0, query.length() - tableUnionString.length());
        }
        return query;
    }

    private String handleVerticalMapping(GlobalTableData t, List<GlobalColumnData> selectCols) {
        //for each local table that matches with this global table
        String query = " SELECT ";
        String tableJoinString = "INNER JOIN ";
        List<GlobalColumnData> primKeyCols = t.getPrimaryKeyColumns();
        Map<GlobalColumnData, Set<ColumnData>> pkWithLocalCols = new HashMap<>();
        for (GlobalColumnData pk : primKeyCols){
            pkWithLocalCols.put(pk, pk.getLocalColumns());
        }
        //get all local tables WITH ONLY LOCAL COLS SELECTED BY USER for the select clause
        Set<TableData> localTablesFromSelectedCols = t.getLocalTablesFromColsVerticalMap(selectCols);//contains the tables in the inner query for select clause
        //select clause
        for (TableData localTable : localTablesFromSelectedCols){
            Set<ColumnData> localCols = new HashSet<>(localTable.getColumnsList());
            for (ColumnData col: localCols){
                if (localTable.hasViewUsingSQL())
                    query+= "\""+col.getName()+"\"";
                else
                    query+=col.getCompletePrestoColumnNameEscaped();
                if (!col.getName().equals(col.getGlobalColumnName())){//if this local column's name is not the same as the matched global column, add an alias
                    query+=" AS \""+col.getGlobalColumnName()+"\"";
                }
                query+=", ";
            }
        }
        if (query.endsWith(", ")) {
            query = query.substring(0, query.length() - ", ".length());//last column is whithout a comma
        }
        //if only one table on the correspondences of the selected global columns, no need to perform joins)
        if (localTablesFromSelectedCols.size() == 1) {
            TableData localtable = localTablesFromSelectedCols.iterator().next();
            if (localtable.getSqlCode() == null)
                query+= " FROM " + localtable.getCompletePrestoTableNameEscapped();
            else
                query+= "FROM ("+localtable.getSqlCode()+") ";
        }
        else{
            //columns were selected that correspond to more than one local table, apply joins
            Set<TableData> localTablesComplete = t.getAllLocalTablesFromCols(t.getGlobalColumnDataList());//tables that have columns mappig to the selected cols. The local tables have all columns
            TableData originalTable = null;
            for (TableData tb : localTablesComplete){
                if (!tb.hasForeignKeys())
                    originalTable = tb;
            }
            if (originalTable == null)
                return null; //there MUST be a table with no foreign keys (the original table)
            localTablesComplete.remove(originalTable);
            if (originalTable.getSqlCode() == null)
                query += " FROM " + originalTable.getCompletePrestoTableNameEscapped();
            else
                query+= "FROM ("+originalTable.getSqlCode()+") ";
            List<ColumnData> pkOriginalColumns = originalTable.getPrimaryKeyColumns();
            Set<TableData> localTablesSelected = getAllDiferentLocalTablesInGlobalColumns(selectCols);//tables with complete columns, but only those that have cols mapped to the selected global cols
            localTablesSelected.remove(originalTable);
            for (TableData tb : localTablesSelected) {
                query += " "+tableJoinString + " " + tb.getCompletePrestoTableNameEscapped();
                List<ColumnData> pkColumns = tb.getPrimaryKeyColumns();
                for (ColumnData pk : pkColumns){
                    ColumnData fk = getForeignKeyRef(pkOriginalColumns, pk);//get wich of the pk originals are referenced by the pk of this pk of the current table
                    if (fk == null){
                        continue;
                    }
                    query+= " ON " + pk.getCompletePrestoColumnNameEscaped() + " = " + fk.getCompletePrestoColumnNameEscaped() +" AND ";
                }
            }
        }
        query = query.substring(0, query.length() - " AND ".length());//last column is whithout a comma

        return query;
    }

    private Set<TableData> getAllDiferentLocalTablesInGlobalColumns(List<GlobalColumnData> gcs){
        Set<TableData> localTablesSelected = new HashSet<>();
        for (GlobalColumnData gc : gcs){
            for (ColumnData lc : gc.getLocalColumns()){
                localTablesSelected.add(lc.getTable());
            }
        }
        return localTablesSelected;
    }

    private ColumnData getForeignKeyRef(List<ColumnData> pkOriginal, ColumnData fk){
        for (ColumnData c : pkOriginal) {
            if (fk.getForeignKeySimplified().equals(c.getTable().getTableName() + "." + c.getName()));
            return c;
        }
        return null;
    }

    public void clearMeasures(){
        measures.clear();
    }

    public void clearNormalFilters(){
        filters.clear();
        filterQuery = "";
    }

    public void clearCollFilters(){
        colFilters.clear();
        colFilterQuery = "";
    }

    public void clearAllFilters(){
        filters.clear();
        colFilters.clear();
        filterQuery = "";
        colFilterQuery = "";
        filterAggrQuery = "";
    }

    public void clearAllElements(){
        selectRows.clear();
        selectColumns.clear();
        measures.clear();
        orderBy.clear();
        filters.clear();
        colFilters.clear();
        filterQuery = "";
        colFilterQuery = "";
        filterAggrQuery = "";
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getSelectRows() {
        return this.selectRows;
    }

    public void setSelectRows(Map<GlobalTableData, List<GlobalColumnData>> selectRows) {
        this.selectRows = selectRows;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getManualRowsAggr() {
        return this.manualRowsAggr;
    }

    public void setManualRowsAndMeasuresAggr(Map<GlobalTableData, List<GlobalColumnData>> manualRowsAggr) {
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rowsAgg : manualRowsAggr.entrySet()){
            if (rowsAgg.getKey().getTableName().equals(factsTable.getGlobalTable().getTableName())){
                manualMeasures.addAll(rowsAgg.getValue());
            }
            else{
                this.manualRowsAggr.put(rowsAgg.getKey(), rowsAgg.getValue());
            }
        }
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

    public List<GlobalColumnData> getMeasures() {
        return this.measures;
    }

    public void setMeasures(List<GlobalColumnData> measures) {
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

    public String getColFilterQuery() {
        return colFilterQuery;
    }

    public void setColFilterQuery(String colFilterQuery) {
        this.colFilterQuery = colFilterQuery;
    }

    public List<List<String>> getPivotValues() {
        return pivotValues;
    }

    public void setPivotValues(List<List<String>> pivotValues) {
        this.pivotValues = pivotValues;
    }

    public Set<String> getFilters() {
        return filters;
    }

    public Set<String> getColFilters() {
        return colFilters;
    }

    public void setFilters(Set<String> filters) {
        this.filters = filters;
    }

    public void setColFilters(Set<String> filters) {
        this.colFilters = filters;
    }

    public String getFilterAggrQuery() {
        return filterAggrQuery;
    }

    public void setFilterAggrQuery(String filterAggrQuery) {
        this.filterAggrQuery = filterAggrQuery;
    }

    public void setCountAll(boolean countAll){
        hasCountAll = countAll;
    }

    public boolean getCountAll(){
        return hasCountAll;
    }

    public String getManualAggregationsStr() {
        return manualAggregationsStr;
    }

    public void setManualAggregationsStr(String manualAggregationsStr) {
        this.manualAggregationsStr = manualAggregationsStr;
    }

    public List<GlobalColumnData> getManualMeasures() {
        return manualMeasures;
    }

    public void setManualMeasures(List<GlobalColumnData> manualMeasures) {
        this.manualMeasures = manualMeasures;
    }


}
