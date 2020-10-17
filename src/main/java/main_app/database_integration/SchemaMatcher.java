package main_app.database_integration;

import helper_classes.*;
import main_app.metadata_storage.MetaDataManager;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.max;
import static helper_classes.utils_other.Constants.DATATYPE_CONVERTIBLES_DICT;


public class SchemaMatcher {
    private final double tableNameSimilarityThreshold = 0.6;
    private final double columnSimilarityThreshold = 0.75;

    private MetaDataManager metaDataManager;

    public static void main(String[] args){

        SchemaMatcher schemaMatcher = new SchemaMatcher("my project");
        List<DBData> dbs = schemaMatcher.generateLocalSchema();
        List<TableData> tables = new ArrayList<>();

        for (DBData db: dbs)
            tables.addAll(db.getTableList());

        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(dbs);
        schemaMatcher.printGlobalTables(globalTables);

        //GlobalSchemaConfigurationV2 schemaConfigurationV2 = new GlobalSchemaConfigurationV2("my project", schemaMatcher.generateLocalSchema(), globalTables);
    }

    public void printGlobalTables(List<GlobalTableData> globalTables){
        for (GlobalTableData globalTableData: globalTables){
            System.out.println("----------Global table: " + globalTableData.getTableName() +"-----------");
            System.out.println("- local tables -");
            for (TableData t : globalTableData.getLocalTables()){
                System.out.println(t.getTableName());
            }
            System.out.println("- columns -");
            for (GlobalColumnData gc :globalTableData.getGlobalColumnDataList()){
                System.out.print(gc.getName()+": "+gc.getDataType() +" -> ");
                for (ColumnData c : gc.getLocalColumns()){
                    System.out.print(c.getName() +": "+c.getDataType()+", mapping type: "+c.getMapping() +"("+c.getTable().getTableName()+"), ");
                }
                System.out.println();
            }
        }
    }

    public SchemaMatcher(String projectName){
        metaDataManager = new MetaDataManager(projectName);
    }

    public List<DBData> generateLocalSchema(){
        DBData db1 = new DBData("http://example", DBModel.MYSQL, "sales");
        DBData db2 = new DBData("http://example", DBModel.MYSQL, "business");
        DBData db3 = new DBData("http://example", DBModel.PostgreSQL, "employees");
        List<TableData> tables = new ArrayList<>();
        //Table 1 -----------
        TableData sales = new TableData("sales", "sales_schema", db1);
        sales.setId(1);
        List<ColumnData> cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("sale_id", "integer", true).withID(1).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("ammount", "double", false).withID(2).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("market", "varchar", false).withID(3).withTable(sales).withForeignKey("").build());
        cols.add(new ColumnData.Builder("sale_date", "date", false).withID(4).withTable(sales).withForeignKey("").build());
        sales.setColumnsList(cols);

        //Table 2 -----------
        TableData employees = new TableData("employees", "sales_schema", db1);
        employees.setId(2);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(5).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("fullName", "varchar", false).withID(6).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge", "char", false).withID(7).withTable(employees).withForeignKey("").build());
        cols.add(new ColumnData.Builder("hired_date", "date", false).withID(8).withTable(employees).withForeignKey("").build());
        employees.setColumnsList(cols);

        //Table 3 -----------
        TableData product = new TableData("product", "sales_schema", db1);
        product.setId(3);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("prod_id", "integer", true).withID(9).withTable(product).withForeignKey("").build());
        cols.add(new ColumnData.Builder("price", "double", false).withID(10).withTable(product).withForeignKey("").build());
        cols.add(new ColumnData.Builder("category", "varchar", false).withID(11).withTable(product).withForeignKey("").build());

        product.setColumnsList(cols);

        db1.addTable(sales);
        db1.addTable(employees);
        db1.addTable(product);

        //Table 4 ----------- similar to 2
        TableData employees2 = new TableData("info_employees", "sales_schema", db2);
        employees2.setId(4);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(12).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(13).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(14).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(15).withTable(employees2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(16).withTable(employees2).withForeignKey("").build());
        employees2.setColumnsList(cols);

        //Table 5 ----------- similar to 4 and 2
        TableData employees3 = new TableData("infoEmployees", "sales", db2);
        employees3.setId(5);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(17).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(18).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(19).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(20).withTable(employees3).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(21).withTable(employees3).withForeignKey("").build());
        employees3.setColumnsList(cols);


        //Table 6 ----------- similar to 5, 4 and 2
        TableData employees4 = new TableData("employees6", "sales", db2);
        employees4.setId(6);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(22).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(23).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(24).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("badge_code", "integer", false).withID(25).withTable(employees4).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(26).withTable(employees4).withForeignKey("").build());
        employees4.setColumnsList(cols);

        db2.addTable(employees2);
        db2.addTable(employees3);
        db2.addTable(employees4);

        //Table 7 ----------- similar to 6, 5, 4 and 2
        TableData employees5 = new TableData("employees7", "sales", db3);
        employees5.setId(7);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("id", "integer", true).withID(27).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("first_name", "varchar", false).withID(28).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("second_name", "varchar", false).withID(29).withTable(employees5).withForeignKey("").build());
        cols.add(new ColumnData.Builder("time_hired", "timestamp", false).withID(30).withTable(employees5).withForeignKey("").build());
        employees5.setColumnsList(cols);

        //Table 8 ----------- similar to 3
        TableData product2 = new TableData("products", "sales_schema", db3);
        product2.setId(8);
        cols = new ArrayList<>();
        cols.add(new ColumnData.Builder("prod_id", "integer", true).withID(31).withTable(product2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("price", "double", false).withID(32).withTable(product2).withForeignKey("").build());
        cols.add(new ColumnData.Builder("category", "varchar", true).withID(33).withTable(product2).withForeignKey("").build());
        product2.setColumnsList(cols);

        db3.addTable(employees5);
        db3.addTable(product2);
        List<DBData> dbs = new ArrayList<>();
        dbs.add(db1);
        dbs.add(db2);
        dbs.add(db3);
        return dbs;
    }

    public List<TableData> getAllTablesInDB(List<DBData> dbs){
        List<TableData> tables = new ArrayList<>();

        for (DBData db: dbs)
            tables.addAll(db.getTableList());
        return tables;
    }

    public List<GlobalTableData> schemaIntegration(List<DBData> dbs){
        List<TableData> tables = getAllTablesInDB(dbs);
        List<Match> matches = labelSchemaMatchingTables(tables);
        //matches = labelTypeSchemaMatchColumns(matches); // column matching (should it be here)
        //get the tables that did not match
        if (matches.size() == 0){ //no matches, simply convert each local table into a global table
            List<GlobalTableData> globalTables = new ArrayList<>();
            for (TableData t : tables){
                GlobalTableData gt = new GlobalTableData(t.getTableName());
                gt.addLocalTable(t);
                gt.setGlobalColumnDataFromLocalColumns(t.getColumnsList());
                globalTables.add(gt);

            }
            return globalTables;
        }
        List<GlobalTableData> nonMatchedTables = getNonMatchedTables(matches, tables);
        //group tables that match to same global table
        List<GlobalTableData> globalTables = groupMatchedTables(matches);
        globalTables = mergeGlobalTableAttributes(globalTables);

        nonMatchedTables = validateForeignKeys(nonMatchedTables, globalTables);
        globalTables.addAll(nonMatchedTables);
        printGlobalTables(globalTables);
        return globalTables;
    }

    /**
     * Get all tables that did not matched and therefore are not in the list of matches and create global tables with them.
     * It also performs correspondence from the original local table
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
    private List<Match> labelSchemaMatchingTables(List<TableData> tables){
        List<Match> tableMatches = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++){
            //avoid inverse permutations ( (table1, table2) and (table2, table1) should not happen)
            for (int j = i+1; j < tables.size(); j++){
                if (!tables.get(i).equals(tables.get(j))){
                    //use Levenshtein distance to get the name similarity
                    double sim = getNameSimilarityLevenshtein(tables.get(i).getTableName(), tables.get(j).getTableName());
                    //System.out.println("Levenshtein sim between " + tables.get(i).getTableName() + " and "+ tables.get(j).getTableName() +" = "+sim);
                    if (sim >= tableNameSimilarityThreshold){
                        //match between the tables
                        tableMatches.add(new Match(tables.get(i), tables.get(j)));
                    }
                }
            }
        }
        return tableMatches;
    }

    private List<GlobalTableData> groupMatchedTables(List<Match> matches){
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

    private GlobalTableData getColumnsForGlobalTableV2(GlobalTableData globalTableData, Map<DatatypePair, String> convertibleDataTypes){
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
            List<ColumnData> columnsCurrentLocalTable = new ArrayList<>(localTables.get(i).getColumnsList());
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
                //primary key
                boolean isPrimaryKey = false;
                if (colFromMergedTables.isPrimaryKey() || colFromNewTable.isPrimaryKey())
                    isPrimaryKey = true;
                //create a new column, the merge of both columns
                ColumnData newColumn = new ColumnData.Builder(colFromMergedTables.getName(), datatype).withPrimaryKey(isPrimaryKey).build();
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


    private double getNameSimilarityLevenshtein(String name1, String name2){
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

    /**
     * Validate the foreign keys of global tables that have simple mapping, by referencing the correct global column.
     * @param nonMatchedTables
     * @param globalTables
     * @return
     */
    private List<GlobalTableData> validateForeignKeys(List<GlobalTableData> nonMatchedTables, List<GlobalTableData> globalTables){
        for (int i = 0; i < nonMatchedTables.size(); i++){
            GlobalTableData gt = nonMatchedTables.get(i);
            List<GlobalColumnData> globCols = gt.getGlobalColumnDataList();
            for (int j = 0; j <globCols.size(); j++ ){
                GlobalColumnData gc = gt.getGlobalColumnDataList().get(j);
                ColumnData c = gc.getLocalColumns().iterator().next();//single match, only one local column
                //check if this column is foreign key (assigned by previously).
                // if it is it references a local column. Change that to reference the corresponding global column
                if (c.hasForeignKey()){
                    ColumnData referencedCol = c.getForeignKeyColumn(metaDataManager);
                    String key = getReferencesGlobalCol(nonMatchedTables, referencedCol);
                    if (key == null)
                        key = getReferencesGlobalCol(globalTables, referencedCol);
                    gc.setForeignKey(key);
                    globCols.set(j, gc);
                }
            }
            gt.setGlobalColumnData(globCols);
            nonMatchedTables.set(i, gt);
        }
        return nonMatchedTables;
    }

    /**
     * given a referenced local column, get the global column that has this column as correspondence
     * @param globTables
     * @param referencedCol
     * @return
     */
    private String getReferencesGlobalCol(List<GlobalTableData> globTables, ColumnData referencedCol){
        for (GlobalTableData gt : globTables){
            GlobalColumnData globCol = gt.getGlobalColContainingLocalColAsCorrespondence(referencedCol);
            if (globCol != null){
                return gt.getTableName()+"."+globCol.getName();
            }
        }
        return null;
    }
}
