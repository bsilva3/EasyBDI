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

import java.io.File;
import java.io.IOException;


public class SchemaMatcher {

    public static void main(String[] args){
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        schemaMatcher.instanceMatch();
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

        /*for(Attribute data : data1.getSchema().get()) {
            System.out.println(data.getName());
        }*/


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
