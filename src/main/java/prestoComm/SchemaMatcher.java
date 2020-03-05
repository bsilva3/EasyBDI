package prestoComm;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.VotingAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.InstanceBasedSchemaBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.*;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.CSVRecordReader;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Record;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.comparators.LabelComparatorLevenshtein;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import helper_classes.ColumnData;
import helper_classes.Match;
import helper_classes.TableData;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.max;
import static prestoComm.Constants.DATATYPE_CONVERTIBLES_DICT;


public class SchemaMatcher {
    private final double similarityThreshold = 0.7;

    public static void main(String[] args){
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        schemaMatcher.labelMatch(null);
    }

    public List<Match> schemaIntegration(List<TableData> tables){
        List<Match> matches = fillTableColumnDataForSchemaMatching(tables);
        //get the tables that did not match
        List<TableData> nonMatchedTables = new ArrayList<>();

        return matches;
    }



    public List<Match> fillTableColumnDataForSchemaMatching(List<TableData> tables){
        List<Match> tableMatches = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++){
            //avoid inverse permutations ( (table1, table2) and (table2, table1) should not happen)
            for (int j = i; j < tables.size(); j++){
                if (!tables.get(i).equals(tables.get(j))){
                    //use Levenshtein distance to get the name similarity
                    double sim = getNameSimilarityLevenshtein(tables.get(i).getTableName(), tables.get(j).getTableName());
                    System.out.println("Levenshtein sim between " + tables.get(i).getTableName() + " and "+ tables.get(j).getTableName() +" = "+sim);
                    if (sim >= similarityThreshold){
                        //possible match between the tables
                        tableMatches.add(new Match(tables.get(i), tables.get(j)));
                    }
                }
            }
        }
        return tableMatches;
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
    public List<Match> labelMatch(List<Match> tableMatches){
        //to check if the datatypes between two columns is compatible
        Map<String, String> convertibleDataTypes = loadConvertibleDataTypeFile();
        for (int k = 0; k < tableMatches.size(); k++){
            Match m = tableMatches.get(k);
            TableData t1 = m.getTableData1();
            TableData t2 = m.getTableData2();
            List<ColumnData> cd1 = t1.getColumnsList();
            List<ColumnData> cd2 = t2.getColumnsList();
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
            m.setColumnMatches(columnMappings);
            tableMatches.set(k, m);
        }
        return tableMatches;
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


    public void labelMatch(){
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

    public void instanceMatch(){
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
