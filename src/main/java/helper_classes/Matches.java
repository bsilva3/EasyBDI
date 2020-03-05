package helper_classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Matches {

    private List<Match> matches;

    public Matches (){
        matches = new ArrayList<>();
    }

    public void add (Match match){
        matches.add(match);
    }


    /**
     * Given a match pair of tables, get all matching pairs of those two elements
     * @param initialMatch
     * @return
     */
    public List<Match> getPairsCorrespondences(Match initialMatch){
        List<Match> matchedTables = new ArrayList<>();
        matchedTables.add(initialMatch);
        TableData tb1 = initialMatch.getTableData1();

        boolean allFound = false;
        while (!allFound){
            for (Match match : matches){
                if (match.getTableData1().equals(tb1)){

                }
                else if (match.getTableData2().equals(tb1)){

                }
            }
        }
        return matchedTables;
    }
}
