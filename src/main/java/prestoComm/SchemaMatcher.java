package prestoComm;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.VotingAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.LabelComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import helper_classes.*;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.max;
import static prestoComm.Constants.DATATYPE_CONVERTIBLES_DICT;


public class SchemaMatcher {
    private final double tableNameSimilarityThreshold = 0.6;
    private final double columnSimilarityThreshold = 0.75;

    public static void main(String[] args){
        List<TableData> tables = new ArrayList<>();
        //Table 1 -----------
        TableData sales = new TableData("sales", "sales_schema", null);
        sales.setId(1);
        List<ColumnData> cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("sale_id", "integer", true).withID(1).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("ammount", "double", false).withID(2).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("market", "varchar", false).withID(3).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("sale_date", "date", false).withID(4).withTable(sales).withForeignKey("").build());
        sales.setColumnsList(cols);

        //Table 2 -----------
        TableData employees = new TableData("employees", "sales_schema", null);
        employees.setId(2);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(5).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("fullName", "varchar", false).withID(6).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge", "char", false).withID(7).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("hired_date", "date", false).withID(8).withTable(employees).withForeignKey("").build());
        employees.setColumnsList(cols);

        //Table 3 -----------
        TableData product = new TableData("product", "sales_schema", null);
        product.setId(3);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("prod_id", "integer", true).withID(9).withTable(product).withForeignKey("").build());
        cols.add(new ColumnData.Builder("price", "double", false).withID(10).withTable(product).withForeignKey("").build());
        cols.add(new ColumnData.Builder("category", "varchar", false).withID(11).withTable(product).withForeignKey("").build());

        product.setColumnsList(cols);

        //Table 4 ----------- similar to 2
        TableData employees2 = new TableData("info_employees", "sales_schema", null);
        employees2.setId(4);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(12).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(13).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(14).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(15).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(16).withTable(employees2).withForeignKey("").build());
        employees2.setColumnsList(cols);

        //Table 5 ----------- similar to 4 and 2
        TableData employees3 = new TableData("infoEmployees", "sales", null);
        employees3.setId(5);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(17).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(18).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(19).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(20).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(21).withTable(employees3).withForeignKey("").build());
        employees3.setColumnsList(cols);


        //Table 6 ----------- similar to 5, 4 and 2
        TableData employees4 = new TableData("employees6", "sales", null);
        employees4.setId(6);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(22).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(23).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(24).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(25).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(26).withTable(employees4).withForeignKey("").build());
        employees4.setColumnsList(cols);

        //Table 7 ----------- similar to 6, 5, 4 and 2
        TableData employees5 = new TableData("employees7", "sales", null);
        employees5.setId(7);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(27).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(28).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(29).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(30).withTable(employees5).withForeignKey("").build());
        employees5.setColumnsList(cols);

        //Table 8 ----------- similar to 3
        TableData product2 = new TableData("products", "sales_schema", null);
        product2.setId(8);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("prod_id", "integer", true).withID(31).withTable(product2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("price", "double", false).withID(32).withTable(product2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("category", "varchar", true).withID(33).withTable(product2).withForeignKey("").build());
        product2.setColumnsList(cols);

        tables.add(sales);
        tables.add(employees);
        tables.add(employees2);
        tables.add(employees3);
        tables.add(employees4);
        tables.add(employees5);
        tables.add(product);
        tables.add(product2);
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(tables);
        for (GlobalTableData globalTableData: globalTables){
            System.out.println("----------Global table: " + globalTableData.getTableName() +"-----------");
            System.out.println("- local tables -");
            for (TableData t : globalTableData.getLocalTables()){
                System.out.println(t.getTableName());
            }
            System.out.println("- columns -");
            for (GlobalColumnData gc :globalTableData.getGlobalColumnData()){
                System.out.print(gc.getName()+": "+gc.getDataType() +" -> ");
                for (ColumnData c : gc.getLocalColumns()){
                    System.out.print(c.getName() +": "+c.getDataType() +"("+c.getTable().getTableName()+"), ");
                }
                System.out.println();
            }
        }
    }

    public List<GlobalTableData> schemaIntegration(List<TableData> tables){
        List<Match> matches = labelSchemaMatchingTables(tables);
        //matches = labelTypeSchemaMatchColumns(matches); // column matching (should it be here)
        //get the tables that did not match
        List<GlobalTableData> nonMatchedTables = getNonMatchedTables(matches, tables); //NOTE: non matched tables can become matched after user intervention, and vice versa
        //group tables that match to same global table
        List<GlobalTableData> globalTables = groupMatchedTables(matches);
        //TODO: IN UI show user the global tables, and edit them
        //TODO: add the non matched single tables (iddealy, one function should add both the matched and non matched)
        globalTables = mergeGlobalTableAttributes(globalTables);
        // In UI, show the result, and edit as user edits them

        //ASSUMING USER ALREADY EDITED EVERYTHING
        globalTables.addAll(nonMatchedTables);
        nonMatchedTables.clear();
        return globalTables;
    }

    /**
     * Get all tables that did not matched and therefore are not in the list of matches
     * @param matches
     * @param tables
     * @return a list of tables that didnt had any matches
     */
    public List<GlobalTableData> getNonMatchedTables(List<Match> matches, List<TableData> tables){
        List<GlobalTableData> nonMatched = new ArrayList<>();
        for (TableData t : tables){
            boolean tableMatched = false;
            for (Match m : matches){
                if (m.tableInMatch(t)){
                    tableMatched = true;
                    break;
                }
            }
            if (!tableMatched){
                //this single table is going to form a global table
                GlobalTableData gt = new GlobalTableData(t.getTableName());
                gt.addLocalTable(t);
                gt.setGlobalColumnDataFromLocalColumns(t.getColumnsList());
                nonMatched.add(gt);
            }
        }
        return nonMatched;
    }

    /**
     * Matches tables using label based schema matching by table name
     * @param tables
     * @return List of table matches and list of table
     */
    public List<Match> labelSchemaMatchingTables(List<TableData> tables){
        List<Match> tableMatches = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++){
            //avoid inverse permutations ( (table1, table2) and (table2, table1) should not happen)
            for (int j = i+1; j < tables.size(); j++){
                if (!tables.get(i).equals(tables.get(j))){
                    //use Levenshtein distance to get the name similarity
                    double sim = getNameSimilarityLevenshtein(tables.get(i).getTableName(), tables.get(j).getTableName());
                    System.out.println("Levenshtein sim between " + tables.get(i).getTableName() + " and "+ tables.get(j).getTableName() +" = "+sim);
                    if (sim >= tableNameSimilarityThreshold){
                        //match between the tables
                        tableMatches.add(new Match(tables.get(i), tables.get(j)));
                    }
                }
            }
        }
        return tableMatches;
    }

    //TODO: fix, not grouping all local tables
    /*public List<GlobalTableData> groupMatchedTables(List<Match> matches){
        List<GlobalTableData> groupedTables = new ArrayList<>();
        if (matches.size() == 0){
            return groupedTables;
        }
        List<TableData> matchedTables = new ArrayList<>();
        List<Integer> indexesMatchesAdded = new ArrayList<>();
        GlobalTableData globalTable = null;
        TableData localTable = null;
        TableData localTable2 = null;
        while (matches.size() > 0){
            //check if this table exists in global schema (start from first)
            // each iteration, the match pairs already added to a global table will be deleted
            if (!matchedTables.contains(matches.get(0).getTableData1())){
                if (globalTable == null){
                    globalTable =  new GlobalTableData(matches.get(0).getTableData1().getTableName());
                    globalTable.addLocalTable(matches.get(0).getTableData1());
                    globalTable.addLocalTable(matches.get(0).getTableData2());
                    localTable = matches.get(0).getTableData2();
                    indexesMatchesAdded.add(0);
                    matches.remove(0); //remove the match tables that were already added
                }
            }
            //search for all tables that will match to the same global table
            for (int i = 0; i < matches.size(); i++){
                TableData otherMatchedTable = matches.get(i).getOtherTable(localTable); //Get the matched table of the previous table. If there is no more matches for this table, null is returned
                if (otherMatchedTable != null){
                    //table exists in this match and the other matched table is added to the same global table
                    globalTable.addLocalTable(otherMatchedTable);
                    indexesMatchesAdded.add(i);
                    localTable = otherMatchedTable;
                }
            }
            Collections.sort(indexesMatchesAdded);
            for (int i = indexesMatchesAdded.size() -1; i >= 0; i-- ){
                int index = indexesMatchesAdded.get(i);
                matches.remove(index);
            }
            indexesMatchesAdded.clear();
            groupedTables.add(globalTable);
        }
        return groupedTables;
    }*/

    public List<GlobalTableData> groupMatchedTables(List<Match> matches){
        List<GlobalTableData> groupedTables = new ArrayList<>();
        if (matches.size() == 0){
            return groupedTables;
        }
        while (matches.size() > 0){
            List<TableData> tables = new ArrayList<>();
            Match match = matches.get(0);
            matches.remove(0);
            int j = 0;
            tables.add(match.getTableData1());
            tables.add(match.getTableData2());
            do{
                //get all other tables that match with one of these tables. Those other pairs will also have their pairs searched
                List<Match> matchesToRemove = new ArrayList<>();
                for (int i = 0; i < matches.size(); i++){
                    TableData t = matches.get(i).getOtherTable(tables.get(j));
                    /*if (t == null){
                        t = matches.get(i).getOtherTable(match.getTableData2());
                    }*/
                    if (t != null){ //if t is null, this table did not matched any other tables
                        matchesToRemove.add(matches.get(i));
                        if (!tables.contains(t))
                            tables.add(t);
                    }
                }
                //remove match pairs already matched
                for (Match m : matchesToRemove){
                    matches.remove(m);
                }
                j++;
            } while (j != tables.size());
            //create a global table
            GlobalTableData globalTable = new GlobalTableData(tables.get(0).getTableName());
            for (TableData table : tables){
                globalTable.addLocalTable(table);
            }
            groupedTables.add(globalTable);
        }
        return groupedTables;
    }

    private List<TableData> getTablesInAllPairs(List<Match> matches, Match match){
        List<TableData> tables = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++){
            TableData t = matches.get(i).getOtherTable(match.getTableData1());
            if (t == null){
                t = matches.get(i).getOtherTable(match.getTableData2());
            }
            if (t != null){
                matches.remove(i);
                tables.add(t);
            }
        }
        return tables;
    }

    /**
     * For each global table merge all tables's atributes for the global table
     * @param globalTables
     * @return
     */
    private List<GlobalTableData> mergeGlobalTableAttributes(List<GlobalTableData> globalTables){
        //to check if the datatypes between two columns is compatible
        Map<DatatypePair, String> convertibleDataTypes = loadConvertibleDataTypeFile();
        for (int i = 0; i < globalTables.size(); i++){
            List<TableData> localTables = globalTables.get(i).getLocalTables();
            if (localTables.size() < 2){
                continue;//nothing to merge
            }
            //schema matching between table columns and merging columns between tables for this global table
            /*List<GlobalColumnData> columnsForGlobalTable = getColumnsForGlobalTable(globalTables.get(i), localTables, convertibleDataTypes);
            GlobalTableData globalTableData = globalTables.get(i);
            globalTableData.setGlobalColumnData(columnsForGlobalTable);*/
            GlobalTableData globalTableData = getColumnsForGlobalTableV2(globalTables.get(i), convertibleDataTypes);
            globalTables.set(i, globalTableData);
        }
        return globalTables;
    }

    /**
     * Performs schema match on 2 tables's columns given a list of tables and merges them together into one table.
     * Iterates the list, merges the first 2 tables and generates a merged table which is used to be merged with the
     * 3rd table if there are more than 2 tables and so on.
     * @param localTables
     * @param convertibleDataTypes
     * @return
     */
    public List<GlobalColumnData> getColumnsForGlobalTable(GlobalTableData globalTableData, List<TableData> localTables, Map<DatatypePair, String> convertibleDataTypes){
        List<GlobalColumnData> globalTableCols = new ArrayList<>();//columns of all local tables merged and belonging to the global table
        List <ColumnData> mergedCols = localTables.get(0).getColumnsList();
        //iterate through all tables, and merge the tables into one list of columns for a global table
        for (int i = 1; i < localTables.size(); i++){
            //perform column table schema match to match pairs of columns
            Map<ColumnData, ColumnData> columnMatches = schemaMatchingColumn(mergedCols, localTables.get(i).getColumnsList(), convertibleDataTypes);

            //add columns that did not had any match
            List<ColumnData> previousMergedCols = mergedCols;
            mergedCols.clear();//clear the previous merged tables and create a new one
            for(ColumnData col : previousMergedCols){
                if (!columnMatches.keySet().contains(col)){
                    //does not belong to match list
                    //globalTableCols.add(new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), globalTableData));
                    mergedCols.add(col);
                }
            }
            for(ColumnData col : localTables.get(i).getColumnsList()){
                if (!columnMatches.values().contains(col)){
                    //does not belong to match list
                    //globalTableCols.add(new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), globalTableData));
                    mergedCols.add(col);
                }
            }
            //merge tables
            for (Map.Entry<ColumnData, ColumnData> entry : columnMatches.entrySet()) { //iterate through the column matches for this local table
                ColumnData table1MatchedCols = entry.getKey(); //column from the first table
                ColumnData table2MatchedCols = entry.getValue(); //column from 2nd table
                //TODO: Do nothing with primary key and foreign key for now
                //remove possible (x) in the type defining fixed or max length
                String col1DataType = table1MatchedCols.getDataType().split("\\(")[0];
                String col2DataType = table2MatchedCols.getDataType().split("\\(")[0];
                Set<ColumnData> ids = table1MatchedCols.getMergedColumns(); //get list of columns previously used to merge and create this column
                ids.addAll(table2MatchedCols.getMergedColumns());
                if (table1MatchedCols.getDataType().equals(table2MatchedCols.getDataType())){
                    //same data type
                    //globalTableCols.add(new GlobalColumnData(table1MatchedCols.getName(), col1DataType, table1MatchedCols.isPrimaryKey(), globalTableData));
                    mergedCols.add(new ColumnData.Builder(table1MatchedCols.getName(), col1DataType, table1MatchedCols.isPrimaryKey()).withMergedCols(ids).build());
                }
                else{
                    //different data types, select one
                    String dataType = chooseGenericDataType(col1DataType, col2DataType, convertibleDataTypes);
                    //globalTableCols.add(new GlobalColumnData(table1MatchedCols.getName(), dataType, table1MatchedCols.isPrimaryKey(), globalTableData));
                    mergedCols.add(new ColumnData.Builder(table1MatchedCols.getName(), dataType, table1MatchedCols.isPrimaryKey()).withMergedCols(ids).build());
                }
            }

            //finished merging 2 tables

        }
        //finished merging all table into one. Convert to global Column
        for (ColumnData col : mergedCols){
            globalTableCols.add(new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), col.getMergedColumns()));
        }
        return globalTableCols;
    }

    public GlobalTableData getColumnsForGlobalTableV2(GlobalTableData globalTableData, Map<DatatypePair, String> convertibleDataTypes){
        List<TableData> localTables = globalTableData.getLocalTables();
        List<ColumnData> initialCols = localTables.get(0).getColumnsList(); //the result of the merging of tables. Starts from the first table
        Map<ColumnData, Set<ColumnData>> correspondences = new HashMap<>(); //for each merged column, contains a list of tables and columns in those tables that make the merge column
        for (ColumnData c : initialCols){
            correspondences.put(c, new HashSet<>(Arrays.asList(c)));
        }
        //Map<ColumnData, List<TableData>>
        for (int i = 1; i < localTables.size(); i++){
            //iterate each local table in a binary way (table A +  Table B = Table AB; Table AB + Table C = Table ABC...)
            Map<ColumnData, Set<ColumnData>> previousMergedColumns = new HashMap<>();
            previousMergedColumns.putAll(correspondences);
            List<ColumnData> columnsPreviousMergedTables = new ArrayList<>();
            columnsPreviousMergedTables.addAll(correspondences.keySet());
            List<ColumnData> columnsCurrentLocalTable = localTables.get(i).getColumnsList();
            correspondences.clear();

            //perform column table schema match to match pairs of columns
            Map<ColumnData, ColumnData> columnMatches = schemaMatchingColumn(columnsPreviousMergedTables, localTables.get(i).getColumnsList(), convertibleDataTypes);
            //if (columnMatches.isEmpty())
                //continue;//if there are no matches, move on, this table is MAYBE not part of the semantic domain
            //for each pair of mathes, create a new column that is combination of both similar columns
            for (Map.Entry<ColumnData, ColumnData> entry : columnMatches.entrySet()) {
                ColumnData colFromMergedTables = entry.getKey();
                ColumnData colFromNewTable = entry.getValue();
                //remove from each tables to merge the columns that matched. The remaining columns have no match
                columnsPreviousMergedTables.remove(colFromMergedTables);
                columnsCurrentLocalTable.remove(colFromNewTable);

                // ---- handle data type conflicts
                String datatype = "";
                //remove possible <datatype>(x) (such as char(7), only the 'char' part is relevant) in the type defining fixed or max length
                String col1DataType = colFromMergedTables.getDataTypeNoLimit();
                String col2DataType = colFromNewTable.getDataTypeNoLimit();
                if (col1DataType.equalsIgnoreCase(col2DataType)){
                    //same datatypes
                    datatype = colFromMergedTables.getDataType();
                }
                else{
                    //different data types, choose the most generic one
                    datatype = chooseGenericDataType(col1DataType, col2DataType, convertibleDataTypes);
                }
                //create a new column, the merge of both columns
                ColumnData newColumn = new ColumnData.Builder(colFromMergedTables.getName(), datatype).build();
                //mergedColumns.add(newColumn);
                Set<ColumnData> previousCorrs = previousMergedColumns.get(colFromMergedTables);//get previous local columns
                previousCorrs.add(colFromNewTable);// add the new local column merged
                correspondences.put(newColumn, previousCorrs);
                //associate with the merged cols the local columns and their tables
            }
            // add unique columns in each table to the new table
            for (ColumnData localCol : columnsPreviousMergedTables) {
                Set<ColumnData> localCols = previousMergedColumns.get(localCol);
                if (localCols == null || localCols.isEmpty()) //add possible previously detected correspondences of  merged tables
                    localCols = new HashSet<ColumnData>(Arrays.asList(localCol));
                correspondences.put(localCol, new HashSet<ColumnData>(localCols));
            }
            for (ColumnData localCol : columnsCurrentLocalTable) {
                correspondences.put(localCol, new HashSet<ColumnData>(Arrays.asList(localCol))); //no correspondences, these are new columns from the new local table to be merged. Corrs are the column itself
            }
        }
        //finished marging all tables into one global table.
        for (Map.Entry<ColumnData, Set<ColumnData>> entry : correspondences.entrySet()){
            ColumnData mergedCol = entry.getKey();
            globalTableData.addGlobalColumn(new GlobalColumnData(mergedCol.getName(), mergedCol.getDataType(), mergedCol.isPrimaryKey(), entry.getValue()));
        }
        return globalTableData;
    }

    /**
     * Given 2 datatype, choose the one that is more generic. For example, varchar e is more generic than integer
     * @param dataType1
     * @param dataType2
     * @return
     */
    private String chooseGenericDataType(String dataType1, String dataType2, Map<DatatypePair, String> convertibleDataTypes){
        String datatype = convertibleDataTypes.get(new DatatypePair(dataType1, dataType2));
        if (datatype == null || datatype.isEmpty()){
            datatype = convertibleDataTypes.get(new DatatypePair(dataType2, dataType1));
        }
        return datatype;
    }


    public double getNameSimilarityLevenshtein(String name1, String name2){
        LevenshteinDistance distance = new LevenshteinDistance();
        double dist = distance.apply(name1.toLowerCase(), name2.toLowerCase());
        //convert the number of substituitions to a percentage
        double bigger = max(name1.length(), name2.length());
        //double sim = (bigger - dist) / bigger;
        double sim = (1 - dist/bigger);
        return sim;
    }

    //
    public List<Match> labelTypeSchemaMatchColumns(List<Match> tableMatches){
        //to check if the datatypes between two columns is compatible
        Map<DatatypePair, String> convertibleDataTypes = loadConvertibleDataTypeFile();
        for (int k = 0; k < tableMatches.size(); k++){
            Match m = tableMatches.get(k);
            TableData t1 = m.getTableData1();
            TableData t2 = m.getTableData2();
            List<ColumnData> cd1 = t1.getColumnsList();
            List<ColumnData> cd2 = t2.getColumnsList();
            Map<ColumnData, ColumnData> columnMappings = schemaMatchingColumn(cd1, cd2, convertibleDataTypes);

            //all matches between columns in 2 tables are finished. Add these mappings
            m.setColumnMatches(columnMappings);
            tableMatches.set(k, m);
        }
        return tableMatches;
    }

    /**
     * Given two lists of columns from two tables, perform schema matching on te columns.
     * To determine if 2 columns are similar, name similarity, data type similarity and if it is or not primary key are used.
     * The following formula is used:
     * nameSim * 0.4 + datatypeSim * 0.4 + isPrimKey - 0.2
     * @param cd1
     * @param cd2
     * @param convertibleDataTypes
     * @return
     */
    private Map<ColumnData, ColumnData> schemaMatchingColumn(List<ColumnData> cd1, List<ColumnData> cd2, Map<DatatypePair, String> convertibleDataTypes){
        //to check if the datatypes between two columns is compatible
        Map<ColumnData, ColumnData> columnMappings = new HashMap<>();
        for (int i = 0; i < cd1.size(); i++) {
            //avoid inverse permutations ( (col1, col2) and (col2, col1) should not happen)
            ColumnData c1 = cd1.get(i);
            for (int j = 0; j < cd2.size(); j++) {
                ColumnData c2 = cd2.get(j);
                double datatypeSim = 0.0;
                if (c1.getDataTypeNoLimit().equalsIgnoreCase(c2.getDataTypeNoLimit()))
                    datatypeSim = 1.0; //same datatype
                else {
                    //check to see if the 2 datatypes are present in the list of convertable data types. If not, the data types are considered to be too diferent (double and boolean for example)
                    if (convertibleDataTypes.containsKey(new DatatypePair(c1.getDataTypeNoLimit(), c2.getDataTypeNoLimit())))
                        datatypeSim = 0.5;
                    else if (convertibleDataTypes.containsKey(new DatatypePair(c2.getDataTypeNoLimit(), c1.getDataTypeNoLimit())))//switch order to find in the map if not yet found
                        datatypeSim = 0.5;
                    else
                        continue; //if data types are not convertible, then do not consider as match
                }
                double nameSim = getNameSimilarityLevenshtein(c1.getName().toLowerCase(), c2.getName().toLowerCase());
                // if not present, types are too diferent, datatypeSim remains at 0.0

                // ---- check primary keys: if both have or dont have, chance of being similar increases
                double primaryKeySim = 0.0;
                if (c1.isPrimaryKey() == c2.isPrimaryKey())
                    primaryKeySim = 1.0;

                double columnSim = nameSim * 0.4 + datatypeSim * 0.4 + primaryKeySim * 0.2;
                if (columnSim >= columnSimilarityThreshold){
                    columnMappings.put(c1, c2); //considered to be semantically similar
                }
            }
        }
        //all matches between columns in 2 tables are finished. Add these mappings
        return columnMappings;
    }

    /**
     * Load into memory a list of convertible data types from presto to use for column schema matching
     */
    private Map<DatatypePair, String> loadConvertibleDataTypeFile(){
        String line = "";
        String csvSplitBy = ",";
        Map<DatatypePair, String> convertipleDataTypes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATATYPE_CONVERTIBLES_DICT))) {
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(csvSplitBy);
                convertipleDataTypes.put(new DatatypePair(elements[0], elements[1]), elements[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertipleDataTypes;
    }

    //used for testing with the Winte.r library ------------------------

    private void labelTypeSchemaMatchColumns(){
        DataSet<Record, Attribute> data1 = new HashedDataSet<>();
        DataSet<Record, Attribute> data2 = new HashedDataSet<>();

        Attribute attribute = new Attribute();
        try {
            new CSVRecordReader(0).loadFromCSV(new File("/home/bruno/Desktop/datasetTest.csv"), data1);
            new CSVRecordReader(0).loadFromCSV(new File("/home/bruno/Desktop/datasetTest2.csv"), data2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(data1.getRandomRecord());
        //RECORD -> a row with data
        //ATRIBUTE: Uma coluna
        for(Attribute data : data1.getSchema().get()) {
            System.out.println(data.getName() +", prov: "+data.getProvenance() + ", identifier: "+data.getIdentifier());
        }


        // Initialize Matching Engine
        MatchingEngine engine = new MatchingEngine<>();
        Processable<Correspondence<Attribute, Attribute>> correspondences = null;
        // run the matching
        try {
            correspondences = engine.runLabelBasedSchemaMatching(data1.getSchema(), data2.getSchema(), new LabelComparatorLevenshtein(), 0.5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // print results
        for(Correspondence<Attribute, Attribute> cor : correspondences.get()) {
            System.out.println(String.format("'%s' <-> '%s' (%.4f)",
                    cor.getFirstRecord().getName(),
                    cor.getSecondRecord().getName(),
                    cor.getSimilarityScore()));
        }
    }

    private void instanceMatch(){
        DataSet<Record, Attribute> data1 = new HashedDataSet<>();
        DataSet<Record, Attribute> data2 = new HashedDataSet<>();
        Attribute attribute = new Attribute();
        try {
            new CSVRecordReader(-1).loadFromCSV(new File("/home/bruno/Desktop/datasetTest.csv"), data1);
            new CSVRecordReader(-1).loadFromCSV(new File("/home/bruno/Desktop/datasetTest2.csv"), data2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // define a blocker that uses the attribute values to generate pairs
        BlockingKeyGenerator<Record, Attribute, MatchableValue> blockingKeyGenerator = new BlockingKeyGenerator<Record, Attribute, MatchableValue>() {
            @Override
            public void generateBlockingKeys(Record record, Processable<Correspondence<Attribute, Matchable>> processable, DataIterator<Pair<String, MatchableValue>> dataIterator) {

            }
        };
        // define a blocker that uses the attribute values to generate pairs
        /*InstanceBasedSchemaBlocker<Record, Attribute> blocker = new InstanceBasedSchemaBlocker<>(
                new AttributeValueGenerator(data1.getSchema()),
                new AttributeValueGenerator(data2.getSchema()));*/

        // to calculate the similarity score, aggregate the pairs by counting
        // and normalise with the number of record in the smaller dataset
        // (= the maximum number of records that can match)
        VotingAggregator<Attribute, MatchableValue> aggregator
                = new VotingAggregator<>(
                false,
                Math.min(data1.size(), data2.size()),
                0.0);

        // Initialize Matching Engine
        MatchingEngine<Record, Attribute> engine = new MatchingEngine<>();
        // run the matching
        Processable<Correspondence<Attribute, MatchableValue>> correspondences
                = engine.runInstanceBasedSchemaMatching(data1, data2, null, aggregator);

        // print results
        for(Correspondence<Attribute, MatchableValue> cor : correspondences.get()) {
            System.out.println(String.format("'%s' <-> '%s' (%.4f)",
                    cor.getFirstRecord().getName(),
                    cor.getSecondRecord().getName(),
                    cor.getSimilarityScore()));
        }
    }

    /**
     * Define if between the type of mapping between the global and local table(s). Local tables can be only one (0), be vertically partioned (1),
     * be horizontally partitioned (2) or...
     * @param globalTable
     * @return
     */
    private int defineDistributionType(GlobalTableData globalTable){
        //List<TableData> tables = globalTable.getLocalTablesFromLocalColumns();
        return 0;
    }
}
