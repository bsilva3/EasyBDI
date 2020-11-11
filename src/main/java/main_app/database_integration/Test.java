package main_app.database_integration;

import org.apache.commons.text.similarity.LevenshteinDistance;

import static java.lang.Integer.max;

public class Test {

    public static void main(String[] args){
        System.out.println(Test.getNameSimilarityLevenshtein("emp", "employee_info"));
    }

    private static double getNameSimilarityLevenshtein(String name1, String name2){
        LevenshteinDistance distance = new LevenshteinDistance();
        double dist = distance.apply(name1.toLowerCase(), name2.toLowerCase());
        //convert the number of substituitions to a percentage
        double bigger = max(name1.length(), name2.length());
        //double sim = (bigger - dist) / bigger;
        double sim = (1 - dist/bigger);
        return sim;
    }
}
