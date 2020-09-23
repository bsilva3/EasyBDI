package helper_classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableColumnNameExtractor {
    private final String regex = "\\w+\\.\\w+";
    private Pattern pattern;
    private Matcher matcher;

    public static void main (String[] args){
        TableColumnNameExtractor tableColumnExtract = new TableColumnNameExtractor();
        tableColumnExtract.getColumnsFromString("csas dfjs egwq.egsjn  sum(fanjnaf.aefjnui * sajnf(");
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
            if (tableColumnStrings.containsKey(tableColSplit[0])){
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
