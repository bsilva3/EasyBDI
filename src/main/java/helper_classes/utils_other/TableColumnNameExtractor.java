package helper_classes.utils_other;

import helper_classes.GlobalColumnData;
import helper_classes.GlobalTableData;
import helper_classes.StarSchema;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used in a string to parse table and column names (when using manual mode of the query ui)
 */
public class TableColumnNameExtractor {
    private final String regex = "\\w+\\.\\w+";
    private Pattern pattern;
    private Matcher matcher;

    public static void main (String[] args){
        TableColumnNameExtractor tableColumnExtract = new TableColumnNameExtractor();
        tableColumnExtract.getColumnsFromString("csas dfjs egwq.egsjn  sum(fanjnaf.aefjnui * 0.5(");
    }
    public TableColumnNameExtractor(){
        pattern = Pattern.compile(regex, Pattern.DOTALL);
    }

    public Map<String, List<String>> getColumnsFromString(String text){
        Matcher matcher = pattern.matcher(text);
        Map<String, List<String>> tableColumnStrings = new HashMap<>();
        while (matcher.find()) {
            String tableColumn = matcher.group(0);
            String[] tableColSplit = tableColumn.split("\\.");
            if (tableColSplit[0].matches("[0-9]+") && tableColSplit[1].matches("[0-9]+"))
                continue;
            if (tableColumnStrings.containsKey(tableColSplit[0]) && !tableColumnStrings.get(tableColSplit[0]).contains(tableColSplit[1])){
                tableColumnStrings.get(tableColSplit[0]).add(tableColSplit[1]);
            }
            else {
                List<String> l = new ArrayList<>();
                l.add(tableColSplit[1]);
                tableColumnStrings.put(tableColSplit[0], l);
            }
        }
        return tableColumnStrings;
    }

    public Set<String> getColumnsFromStringSet(String text){
        Matcher matcher = pattern.matcher(text);
        Set<String> tableColumnStrings = new HashSet<>();
        while (matcher.find()) {
            String tableColumn = matcher.group(0);
            tableColumnStrings.add(tableColumn);
        }
        return tableColumnStrings;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getListofTableColObjectsFromStrings(Map<String, List<String>> tableColumnStrings, StarSchema starSchema){
        Map<GlobalTableData, List<GlobalColumnData>> starSchemaTables = new HashMap<>();
        for (Map.Entry<String, List<String>> tableCol : tableColumnStrings.entrySet()){
            String tableName = tableCol.getKey();
            List<String> columnNames = tableCol.getValue();
            GlobalTableData table = null;
            GlobalTableData column = null;
            //get table
            if (starSchema.getFactsTable().getGlobalTable().getTableName().equals(tableName)){
                table = starSchema.getFactsTable().getGlobalTable();
            }
            else{
                for (GlobalTableData t : starSchema.getDimsTables()){
                    if (t.getTableName().equals(tableName)){
                        table = t;
                        break;
                    }
                }
            }
            //get each column of the table
            List<GlobalColumnData> cols = new ArrayList<>();
            for (String columnName : columnNames){
                for (GlobalColumnData c : table.getGlobalColumnDataList()){
                    if (c.getName().equals(columnName)){
                        c.setFullName(tableName+"."+c.getName());
                        cols.add(c);
                        break;
                    }
                }
            }
            //add table with selected columns to the map
            starSchemaTables.put(table, cols);
        }
        return starSchemaTables;
    }

    public Map<GlobalTableData, List<GlobalColumnData>> getSchemaObjectsFromSQLText(String text, StarSchema schema){
        return getListofTableColObjectsFromStrings(getColumnsFromString(text), schema);
    }
}
