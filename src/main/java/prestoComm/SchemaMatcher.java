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
    private final double similarityThreshold = 0.6;

    public static void main(String[] args){
        List<TableData> tables = new ArrayList<>();
        //Table 1 -----------
        TableData sales = new TableData("sales", "sales_schema", null);
        List<ColumnData> cols = new ArrayList<>();
        cols.add(new ColumnData(1, "sale_id", "integer", true, sales, "", ""));
        cols.add(new ColumnData(2, "ammount", "double", false, sales, "", ""));
        cols.add(new ColumnData(3, "market", "varchar", false, sales, "", ""));
        cols.add(new ColumnData(4, "sale_date", "date", false, sales, "", ""));
        sales.setColumnsList(cols);

        //Table 2 -----------
        TableData employees = new TableData("employees", "sales_schema", null);
        cols = new ArrayList<>();
        cols.add(new ColumnData(5, "employeeID", "integer", true, sales, "", ""));
        cols.add(new ColumnData(6, "fullName", "varchar", false, sales, "", ""));
        cols.add(new ColumnData(7, "badge", "char", false, sales, "", ""));
        cols.add(new ColumnData(8, "hired_date", "date", false, sales, "", ""));
        employees.setColumnsList(cols);

        //Table 3 -----------
        TableData product = new TableData("product", "sales_schema", null);
        cols = new ArrayList<>();
        cols.add(new ColumnData(9, "prod_id", "integer", true, sales, "", ""));
        cols.add(new ColumnData(10, "price", "double", false, sales, "", ""));
        cols.add(new ColumnData(11, "category", "varchar", false, sales, "", ""));
        product.setColumnsList(cols);

        //Table 4 ----------- similar to 2
        TableData employees2 = new TableData("info_employees", "sales_schema", null);
        cols = new ArrayList<>();
        cols.add(new ColumnData(12, "id", "integer", true, sales, "", ""));
        cols.add(new ColumnData(13, "first_name", "double", false, sales, "", ""));
        cols.add(new ColumnData(14, "second_name", "varchar", false, sales, "", ""));
        cols.add(new ColumnData(15, "badge_code", "integer", false, sales, "", ""));
        cols.add(new ColumnData(16, "time_hired", "timestamp", false, sales, "", ""));
        employees2.setColumnsList(cols);

        tables.add(sales);
        tables.add(employees);
        tables.add(employees2);
        tables.add(product);
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(tables);
        for (GlobalTableData globalTableData: globalTables){
            System.out.println(globalTableData);
        }
    }

    public List<GlobalTableData> schemaIntegration(List<TableData> tables){
        List<Match> matches = labelSchemaMatchingTables(tables);
        //matches = labelTypeSchemaMatchColumns(matches); // column matching (should it be here)
        //get the tables that did not match
        List<TableData> nonMatchedTables = getNonMatchedTables(matches, tables);
        //group tables that match to same global table
        List<GlobalTableData> globalTables = groupMatchedTables(matches);
        globalTables = mergeGlobalTableAttributes(globalTables);

        return globalTables;
    }

    /**
     * Get all tables that did not matched and therefore are not in the list of matches
     * @param matches
     * @param tables
     * @return a list of tables that didnt had any matches
     */
    public List<TableData> getNonMatchedTables(List<Match> matches, List<TableData> tables){
        List<TableData> nonMatched = new ArrayList<>();
        for (TableData t : tables){
            boolean tableMatched = false;
            for (Match m : matches){
                if (m.tableInMatch(t)){
                    tableMatched = true;
                    break;
                }
            }
            if (!tableMatched){
                nonMatched.add(t);
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
                    if (sim >= similarityThreshold){
                        //match between the tables
                        tableMatches.add(new Match(tables.get(i), tables.get(j)));
                    }
                }
            }
        }
        return tableMatches;
    }

    public List<GlobalTableData> groupMatchedTables(List<Match> matches){
        List<GlobalTableData> groupedTables = new ArrayList<>();
        if (matches.size() == 0){
            return groupedTables;
        }
        List<TableData> matchedTables = new ArrayList<>();
        List<Integer> indexesMatchesAdded = new ArrayList<>();
        GlobalTableData globalTable = null;
        TableData localTable = null;
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
                }
            }
            //search for all tables that will match to the same global table
            for (int i = 0; i < matches.size(); i++){
                TableData otherMatchedTable = matches.get(i).getOtherTable(localTable);
                if (otherMatchedTable != null){
                    //table exists in this match and the other matched table is added to the same global table
                    globalTable.addLocalTable(otherMatchedTable);
                    indexesMatchesAdded.add(i);
                    localTable = otherMatchedTable;
                }
            }
            Collections.sort(indexesMatchesAdded);
            for (int i = indexesMatchesAdded.size() -1; i > 0; i-- ){
                int index = indexesMatchesAdded.get(i-1);
                matches.remove(index);
            }
            indexesMatchesAdded.clear();
            groupedTables.add(globalTable);
        }
        return groupedTables;
    }

    /**
     * For each global table merge all tables's atributes for the global table
     * @param globalTables
     * @return
     */
    private List<GlobalTableData> mergeGlobalTableAttributes(List<GlobalTableData> globalTables){
        //to check if the datatypes between two columns is compatible
        Map<String, String> convertibleDataTypes = loadConvertibleDataTypeFile();
        for (int i = 0; i < globalTables.size(); i++){
            List<TableData> localTables = globalTables.get(i).getLocalTables();
            if (localTables.size() < 2){
                continue;//nothing to merge
            }
            //schema matching between table columns and merging columns between tables for this global table
            List<GlobalColumnData> columnsForGlobalTable = getColumnsForGlobalTable(globalTables.get(i), localTables, convertibleDataTypes);
            GlobalTableData globalTableData = globalTables.get(i);
            globalTableData.setGlobalColumnData(columnsForGlobalTable);
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
    public List<GlobalColumnData> getColumnsForGlobalTable(GlobalTableData globalTableData, List<TableData> localTables, Map<String, String> convertibleDataTypes){
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
                    mergedCols.add(new ColumnData(table1MatchedCols.getName(), col1DataType, table1MatchedCols.isPrimaryKey(), ids));
                }
                else{
                    //different data types, select one
                    String dataType = chooseGenerableDataType(col1DataType, col2DataType);
                    //globalTableCols.add(new GlobalColumnData(table1MatchedCols.getName(), dataType, table1MatchedCols.isPrimaryKey(), globalTableData));
                    mergedCols.add(new ColumnData(table1MatchedCols.getName(), dataType, table1MatchedCols.isPrimaryKey(), ids));
                }
            }

            //finished merging 2 tables

        }
        //finished merging all table into one. Convert to global Column
        for (ColumnData col : mergedCols){
            globalTableCols.add(new GlobalColumnData(col.getName(), col.getDataType(), col.isPrimaryKey(), globalTableData, col.getMergedColumns()));
        }
        return globalTableCols;
    }

    /**
     * Given 2 datatype, choose the one that is more generable. For example, varchar e is more generable than integer
     * @param dataType1
     * @param dataType2
     * @return
     */
    private String chooseGenerableDataType(String dataType1, String dataType2){
        if (dataType1.equalsIgnoreCase("varchar") || dataType2.equalsIgnoreCase("varchar"))
            return "varchar";
        else if (dataType1.equalsIgnoreCase("char") || dataType2.equalsIgnoreCase("char"))
            return "varchar";
        else if (dataType1.equalsIgnoreCase("integer") || dataType2.equalsIgnoreCase("integer"))
            return "integer";
        else if (dataType1.equalsIgnoreCase("double") || dataType2.equalsIgnoreCase("double"))
            return "double";
        //time, date, timestamp...?
        else
            return "varchar";
    }


    public double getNameSimilarityLevenshtein(String name1, String name2){
        LevenshteinDistance distance = new LevenshteinDistance();
        double dist = distance.apply(name1, name2);
        //convert the number of substituitions to a percentage
        double bigger = max(name1.length(), name2.length());
        //double sim = (bigger - dist) / bigger;
        double sim = (1 - dist/bigger);
        return sim;
    }

    //
    public List<Match> labelTypeSchemaMatchColumns(List<Match> tableMatches){
        //to check if the datatypes between two columns is compatible
        Map<String, String> convertibleDataTypes = loadConvertibleDataTypeFile();
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

    private Map<ColumnData, ColumnData> schemaMatchingColumn(List<ColumnData> cd1, List<ColumnData> cd2, Map<String, String> convertibleDataTypes){
        //to check if the datatypes between two columns is compatible
        Map<ColumnData, ColumnData> columnMappings = new HashMap<>();
        for (int i = 0; i < cd1.size(); i++) {
            //avoid inverse permutations ( (col1, col2) and (col2, col1) should not happen)
            ColumnData c1 = cd1.get(i);
            for (int j = i; j < cd2.size(); j++) {
                ColumnData c2 = cd2.get(j);
                if ( getNameSimilarityLevenshtein(c1.getName(), c2.getName()) >= similarityThreshold){
                    String dataType = convertibleDataTypes.get(c1.getDataType());
                    //the map contains, for each datatype supported by presto, an associated datatype that is convertable or
                    //similar (numeric, string, etc..)
                    if (dataType != null && dataType.equalsIgnoreCase(c2.getDataType())){
                        //its a match, these columns have similar datatypes and similar names
                        columnMappings.put(c1, c2);
                    }
                }
            }
        }
        //all matches between columns in 2 tables are finished. Add these mappings
        return columnMappings;
    }

    /**
     * Load into memory a list of convertible data types from presto to use for column schema matching
     */
    private Map<String, String> loadConvertibleDataTypeFile(){
        String line = "";
        String csvSplitBy = ",";
        Map<String, String> convertipleDataTypes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(DATATYPE_CONVERTIBLES_DICT))) {
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(csvSplitBy);
                convertipleDataTypes.put(elements[0], elements[1]);
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
}
