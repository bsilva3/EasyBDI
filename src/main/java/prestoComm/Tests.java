package prestoComm;

import helper_classes.*;
import wizards.global_schema_config.GlobalSchemaConfiguration;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.*;

import static prestoComm.Constants.PRESTO_BIN;

public class Tests {

    public static void main(String[] args) {
        /*GlobalTableData globTab = generateVertSchema();
        globTab = defineDistributionType(globTab);
        System.out.println(globTab.getMappingType());*/

        executeSudoCommand();
    }

    public static void executeSudoCommand(){
        InputStreamReader input;
        OutputStreamWriter output;

        try {
            //Create the process and start it.
            Process pb = new ProcessBuilder("/bin/bash", "-c", "/usr/bin/sudo -S ls  2>&1").start();
            //ProcessBuilder processBuilder = new ProcessBuilder();
            //processBuilder.command("/bin/bash", "-c", "sudo -S ls");
            //Process pb = processBuilder.start();
            output = new OutputStreamWriter(pb.getOutputStream());
            input = new InputStreamReader(pb.getInputStream());

            int bytes, tryies = 0;
            char buffer[] = new char[1024];
            while ((bytes = input.read(buffer, 0, 1024)) != -1) {
                if(bytes == 0)
                    continue;
                //Output the data to console, for debug purposes
                String data = String.valueOf(buffer, 0, bytes);
                System.out.println(data);
                // Check for password request
                if (data.contains("[sudo] password")) {
                    // Here you can request the password to user using JOPtionPane or System.console().readPassword();
                    JPasswordField pwd = new JPasswordField(30);
                    String labelText = "";
                    if (tryies > 0)
                        labelText = "Wrong password, please try again";
                    else
                        labelText = "Please, enter the system password.";
                    JComponent[]inputs = new JComponent[]{
                            new JLabel(labelText),
                            pwd
                    };

                    int action = JOptionPane.showConfirmDialog(null, inputs,"Please, enter password",JOptionPane.OK_CANCEL_OPTION);
                    if (action < 0 || action == 2) { //user canceled
                        pb.destroy();
                        return;
                    }
                    else if (action == 0) {
                        char password[] = pwd.getPassword();
                        output.write(password);
                        output.write('\n');
                        output.flush();
                        // erase password data, to avoid security issues.
                        Arrays.fill(password, '\0');
                        tryies++;
                    }

                }
            }

        } catch (IOException ex) { System.err.println(ex); }
        //wait and check if connection with presto is possible every 4 seconds, 3 times
        int nTries = 0;
        /*while (nTries < 3) {
            try {
                conn.getMetaData();
                return true;
            } catch (SQLException e) {
                nTries++;
            }
        }
        if (nTries >= 3)
            return false;
        else
            return true;*/
    }

    public static GlobalTableData defineDistributionType(GlobalTableData globalTable){
        //test if its a simple mapping 1 - 1 and only 1 local table
        MappingType mappingType = MappingType.Undefined;
        if (isSimpleMapping(globalTable)){
            mappingType = MappingType.Simple;
        }
        else if (isHorizontalMapping(globalTable)){
            mappingType = MappingType.Horizontal;
        }
        else if (isVerticalMapping(globalTable)){
            mappingType = MappingType.Vertical;
        }
        for (GlobalColumnData gc : globalTable.getGlobalColumnDataList()){
            Set<ColumnData> localCols = gc.getLocalColumns();
            Set<ColumnData> updatedLocalCols = new HashSet<>();
            //for each local table corresponding to this global column, set the mapping, simple mapping in this case
            for (ColumnData c : localCols){
                c.setMapping(mappingType);
                updatedLocalCols.add(c);
            }
            gc.setLocalColumns(updatedLocalCols);
        }
        return globalTable;
    }

    /**
     * GIven  a global table, and the local tables that have correspondences, check to see if there is a simple mapping between them.
     * A simple mapping means that the local table is constituted by one unique local table, whose attributes (columns) are the same or less then in the local table
     * @param globalTable
     * @return
     */
    private static boolean isSimpleMapping(GlobalTableData globalTable) {
        Set<TableData> completeLocalTables = globalTable.getLocalTablesFromCols();
        if (completeLocalTables.size() == 1) {
            return true;
        }
        return false;
    }

    /**
     * GIven  a global table, and the local tables that have correspondences, check to see if there is a vertical partioning mapping between them.
     * A vertical mapping means that the local table is constituted by tables that contain primary keys that reference one table's primary key
     * (columns of one table was distributed to multiple tables)
     * NOTE: considering that there is only one table that does not have a foreign key and primary key
     * @param globalTable
     * @return
     */
    private static boolean isVerticalMapping(GlobalTableData globalTable) {
        Set<TableData> completeLocalTables = globalTable.getLocalTablesFromCols();
        if (completeLocalTables.size() > 1) {
            ColumnData primKeyOriginalTable = null;
            Set<ColumnData> primCols = new HashSet<>();
            //get the original prim key that other foreign keys prim keys reference
            for (GlobalColumnData c : globalTable.getPrimaryKeyColumns()){
                primCols.addAll(c.getLocalColumns());
            }
            //Set<ColumnData> primCols = globalTable.getPrimaryKeyColumns().getLocalColumns();
            for (ColumnData c : primCols){
                if (c.isPrimaryKey() && !c.hasForeignKey()){
                    primKeyOriginalTable = c;
                    completeLocalTables.remove(c.getTable());
                    break;
                }
            }
            for (TableData  localTable : completeLocalTables) {
                for (ColumnData localColumn : localTable.getColumnsList()){
                    //check for columns that are both primary and foreign keys
                    if (localColumn.isPrimaryKey() && localColumn.hasForeignKey()){
                        ColumnData referencedCol = localColumn.getForeignKeyColumn();
                        if (referencedCol != null && !referencedCol.equals(primKeyOriginalTable))
                            return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Given  a global table, and the local tables that have correspondences with the global table, check to see if there is a horizontal partioning
     * mapping between them. This means that the global table is constituted by several equal local tables,
     * whose attributes (columns) are the same in all local and global tables.
     * @param globalTable
     * @return
     */
    private static boolean isHorizontalMapping(GlobalTableData globalTable) {
        Set<TableData> completeLocalTables = globalTable.getLocalTablesFromCols();
        if (completeLocalTables.size() > 1) {
            for (TableData localTable : completeLocalTables) {
                //all local and global tables have same nº of columns if horizontal partioning
                if (localTable.getNCols() != globalTable.getNCols())
                    return false;
                for (GlobalColumnData gc : globalTable.getGlobalColumnDataList()){
                    //if all columns in all local tables are the same as the cols in global table, then there is vertical partiotioning
                    if (!localTable.columnExists(gc.getName(), gc.getDataTypeNoLimit(), gc.isPrimaryKey()))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    public static GlobalTableData generateVertSchema(){
        java.util.List<DBData> dbs = new ArrayList<>();
        java.util.List<TableData> tables = new ArrayList<>();
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.MYSQL, "parisDB");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees_info", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        java.util.List<ColumnData> colsForTable1 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable2 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable3 = new ArrayList<>();
        ColumnData c11 = new ColumnData.Builder("id", "integer", true).withTable(table1).build();
        colsForTable1.add(c11);
        ColumnData c12 = new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build();
        colsForTable1.add(c12);

        ColumnData c21 = new ColumnData.Builder("employee_id", "integer", true).withTable(table2).withForeignKey("employees.id").build();
        c21.setfk(c11);
        colsForTable2.add(c21);
        ColumnData c22 = new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build();
        colsForTable1.add(c22);
        ColumnData c23 = new ColumnData.Builder("email", "varchar", false).withTable(table1).build();
        colsForTable1.add(c23);

        ColumnData c31 = new ColumnData.Builder("employee_id", "integer", true).withTable(table3)
                .withForeignKey("employees.id").build();
        c31.setfk(c11);
        colsForTable3.add(c31);
        ColumnData c32 = new ColumnData.Builder("sales_ammount", "double", false).withTable(table3).build();
        colsForTable3.add(c32);
        ColumnData c33 = new ColumnData.Builder("date", "varchar", false).withTable(table3).build();
        colsForTable3.add(c33);

        table1.setColumnsList(colsForTable1);
        table2.setColumnsList(colsForTable2);
        table3.setColumnsList(colsForTable3);
        dbData1.addTable(table1);
        dbData2.addTable(table2);
        dbData2.addTable(table3);
        dbs.add(dbData1);
        dbs.add(dbData2);

        GlobalTableData g1 = new GlobalTableData("employees");
        Set <ColumnData> c = new HashSet<>();
        c.add(c11);
        c.add(c21);
        c.add(c31);
        GlobalColumnData globalColA = new GlobalColumnData("id", "integer", true, c);
        GlobalColumnData globalColB = new GlobalColumnData("full_name", "varchar", true, c12);
        GlobalColumnData globalColC = new GlobalColumnData("phone_number", "integer", true, c22);
        GlobalColumnData globalColD = new GlobalColumnData("email", "varchar", false, c23);
        GlobalColumnData globalColE = new GlobalColumnData("sales_ammount", "double", false, c32);
        GlobalColumnData globalColF = new GlobalColumnData("date", "varchar", false, c33);
        java.util.List<GlobalColumnData> globalCols = new ArrayList<>();
        globalCols.add(globalColA);
        globalCols.add(globalColB);
        globalCols.add(globalColC);
        globalCols.add(globalColD);
        /*globalCols.add(globalColMongo1);
        globalCols.add(globalColMongo2);
        globalCols.add(globalColMongo3);
        globalCols.add(globalColMongo4);*/

        g1.setGlobalColumnData(Arrays.asList(globalColA, globalColB, globalColC, globalColD));
        return g1;
    }

}
