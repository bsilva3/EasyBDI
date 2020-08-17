package prestoComm.query_ui;

import helper_classes.*;
import helper_classes.Utils;
import io.github.qualtagh.swing.table.model.*;
import io.github.qualtagh.swing.table.view.JBroTable;
import io.github.qualtagh.swing.table.view.JBroTableModel;
import org.joda.time.DateTime;
import prestoComm.DBModel;
import prestoComm.MainMenu;
import prestoComm.MetaDataManager;
import prestoComm.PrestoMediator;
import wizards.global_schema_config.CustomTreeNode;
import wizards.global_schema_config.CustomeTreeCellRenderer;
import wizards.global_schema_config.NodeType;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static prestoComm.query_ui.GlobalTableQuery.MAX_SELECT_COLS;

public class QueryUI extends JPanel{
    private JBroTable queryResultsTableGroupable;
    private JTree schemaTree;
    private JTree filterTree;
    private JList measuresList;
    private JComboBox aggregationOpComboBox;
    private JList rowsList;
    private JPanel mainPanel;
    private JComboBox cubeSelectionComboBox;
    private JButton executeQueryButton;
    private JButton backButton;
    private JList columnsList;
    private JTabbedPane tabbedPane1;
    private JList queryLogList;
    private JButton saveSelectedQueryButton;
    private JButton saveAllQueriesButton;
    private JButton saveQueryButton;
    private JButton loadQueryButton;
    private JButton clearAllFieldsButton;
    private JScrollPane tablePane;
    private Set<String> filters;

    private StarSchema starSchema;
    private GlobalTableQuery globalTableQueries;//used to store all queries for each global table, and their columns

    private DefaultTreeModel schemaTreeModel;
    private DefaultTreeModel filterTreeModel;
    private DefaultListModel measuresListModel;
    private DefaultListModel columnListModel;
    private DefaultListModel rowsListModel;
    private DefaultListModel queryLogModel;
    private JBroTableModel defaultTableModel;

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;
    private final String[] aggregations = { "no aggregation", "count", "sum", "average"};
    private final String[] numberOperations = { "=", "!=", ">", "=>", "<", "<="};
    private final String[] stringOperations = { "=", "!=", "like"};

    private MainMenu mainMenu;

    public QueryUI(String projectName, final MainMenu mainMenu){
        this.mainMenu = mainMenu;
        this.metaDataManager = new MetaDataManager(projectName);
        this.prestoMediator = new PrestoMediator();
        filters = new HashSet<>();

        mainPanel.setSize(mainMenu.getSize());

        List<String> starSchemas =  metaDataManager.getStarSchemaNames();
        if (starSchemas.isEmpty()){
            JOptionPane.showMessageDialog(null, "There are no star schemas in this project.", "No Star schemas found", JOptionPane.ERROR_MESSAGE);
            //close db
            metaDataManager.close();
            mainMenu.returnToMainMenu();
        }
        else {
            cubeSelectionComboBox.setModel(new DefaultComboBoxModel(starSchemas.toArray(new String[starSchemas.size()])));

            aggregationOpComboBox.setModel(new DefaultComboBoxModel(aggregations));

            this.starSchema = metaDataManager.getStarSchema(cubeSelectionComboBox.getSelectedItem().toString());
            schemaTreeModel = setStarSchemaTree();
            schemaTree.setModel(schemaTreeModel);
            CustomTreeNode root = (CustomTreeNode) schemaTreeModel.getRoot();
            expandAllStarSchema(new TreePath(root), true);
            schemaTree.setCellRenderer(new CustomeTreeCellRenderer());
            schemaTree.setTransferHandler(new TreeTransferHandler());
            schemaTree.setDragEnabled(true);
            schemaTree.setRootVisible(false);

            filterTreeModel = null;

            measuresListModel = new DefaultListModel();
            columnListModel = new DefaultListModel();
            rowsListModel = new DefaultListModel();
            queryLogModel = new DefaultListModel();
            filterTree.setModel(filterTreeModel);
            filterTree.setCellRenderer(new FilterNodeCellRenderer());
            filterTree.addMouseListener(getMouseListenerForFilterTree());
            measuresList.setModel(measuresListModel);
            rowsList.setModel(rowsListModel);
            columnsList.setModel(columnListModel);
            queryLogList.setModel(queryLogModel);

            filterTree.setTransferHandler(new TreeTransferHandler());
            measuresList.setTransferHandler(new TreeTransferHandler());
            rowsList.setTransferHandler(new TreeTransferHandler());
            columnsList.setTransferHandler(new TreeTransferHandler());

            //jtable
            //this.defaultTableModel = new JBroTableModel(new ModelData());
            //queryResultsTableGroupable = new JBroTable();
            //tablePane.add(queryResultsTableGroupable);
            //this.queryResultsTable.setModel(defaultTableModel);
            //queryResultsTableGroupable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//maintain column width

            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //open wizard and edit current project
                    metaDataManager.close();
                    mainMenu.returnToMainMenu();
                }
            });

            executeQueryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //open wizard and edit current project
                    executeQueryAndShowResults();
                }
            });


            cubeSelectionComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (cubeSelectionComboBox.getSelectedItem().toString().equalsIgnoreCase(starSchema.getSchemaName()))//user selected same project
                        return;
                    clearAllFieldsAndQueryElements();//new project selected, clear all ui and data structures
                    starSchema = metaDataManager.getStarSchema(cubeSelectionComboBox.getSelectedItem().toString());
                    schemaTreeModel = setStarSchemaTree();
                    schemaTree.setModel(schemaTreeModel);
                    schemaTree.revalidate();
                    schemaTree.updateUI();
                }
            });

            //listeners for lists left click to open menus
            columnsList.addMouseListener(getMouseListenerForColumnList());
            rowsList.addMouseListener(getMouseListenerForRowsList());
            measuresList.addMouseListener(getMouseListenerForMeasuresList());

            queryLogList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 2) {
                        // Double-click on list log item: show full message
                        int index = list.locationToIndex(evt.getPoint());
                        if (index < 0){
                            return;
                        }
                        QueryLog queryLog = (QueryLog) queryLogModel.get(index);
                        JOptionPane optionPane = new NarrowOptionPane();
                        optionPane.setMessage(queryLog.toString());
                        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        JDialog dialog = optionPane.createDialog(null, "Query Log");
                        dialog.setVisible(true);
                    }
                }
            });

            this.globalTableQueries = new GlobalTableQuery(prestoMediator, starSchema.getFactsTable(), starSchema.getDimsTables());
            add(mainPanel);
            this.setVisible(true);
        }
        saveAllQueriesButton.addActionListener(e -> {
            saveToFile(true);
        });
        saveSelectedQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile(false);
            }
        });
        saveQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveQueryState();
            }
        });

        loadQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectQueryToLoad();
            }
        });
        clearAllFieldsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFieldsAndQueryElements();
            }
        });
    }

    private void clearAllFieldsAndQueryElements(){
        //clear all fields in ui
        rowsListModel.clear();
        columnListModel.clear();
        measuresListModel.clear();
        filterTreeModel = null;

        rowsList.revalidate();
        rowsList.updateUI();
        columnsList.revalidate();
        columnsList.updateUI();
        measuresList.revalidate();
        measuresList.updateUI();
        filterTree.revalidate();
        filterTree.updateUI();

        //clear all query elements in structures
        globalTableQueries.clearAllElements();
    }

    private void selectQueryToLoad(){
        List<String> queryNames = metaDataManager.getListOfQueriesByCube(cubeSelectionComboBox.getSelectedItem().toString());
        new QuerySelector(queryNames, this);
    }

    public void loadSelectedQuery(String queryName){
        int cubeID = metaDataManager.getOrcreateCube(cubeSelectionComboBox.getSelectedItem().toString());
        int queryID = metaDataManager.getQueryID(queryName, cubeID);
        if (queryID == -1){
            JOptionPane.showMessageDialog(mainMenu, "Could not load query: does not exist", "Error loading query", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Map<GlobalTableData, List<GlobalColumnData>> rows = metaDataManager.getQueryRows(queryID);
        Map<GlobalTableData, List<GlobalColumnData>> cols = metaDataManager.getQueryColumns(queryID);
        Map<GlobalColumnData, String> measures = metaDataManager.getQueryMeasures(queryID);
        //clear all fields in ui
        clearAllFieldsAndQueryElements();

        //place rows in UI and in data structures and add group by's (if any)
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rowSelect : rows.entrySet()){
            for (GlobalColumnData c : rowSelect.getValue()){
                addRowsToList(rowsListModel, c, rowSelect.getKey());
                if (c.getOrderBy() != null && c.getOrderBy().equalsIgnoreCase("ASC")){
                    this.addGroupBy(rowsListModel.getSize()-1, true);
                }
                else if (c.getOrderBy() != null && c.getOrderBy().equalsIgnoreCase("DESC")){
                    this.addGroupBy(rowsListModel.getSize()-1, false);
                }
            }
        }

        //place columns in UI (if any) and in data structures
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> colSelect : cols.entrySet()){
            for (GlobalColumnData c : colSelect.getValue()){
                addColumnsToList(columnListModel, c, colSelect.getKey());
            }
        }

        //place measures in UI (if any) and in data structures
        for (Map.Entry<GlobalColumnData, String> measure : measures.entrySet()) {
            String measureItem = measure.getValue()+"("+measure.getKey().getName()+")";
            addMeasure(measuresListModel, measureItem);
        }

        this.filterTreeModel = new DefaultTreeModel(metaDataManager.getQueryFilters(queryID));
        this.filterTree.setModel(filterTreeModel);

    }

    private void saveQueryState(){
        //user must name the query
        JTextField nameTxt = new JTextField();
        JComponent[] inputs = new JComponent[]{
                new JLabel("Please, insert a name for this query."),
                nameTxt};
        int result = JOptionPane.showConfirmDialog(
            null,
            inputs,
            "Save Query",
            JOptionPane.PLAIN_MESSAGE);
        if (nameTxt.getText().length() == 0) {
            JOptionPane.showConfirmDialog(
                    null,
                    "You must name your query to save it.",
                    "Save Query Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        //get all select rows:
        Map<GlobalTableData, List<GlobalColumnData>> rows = globalTableQueries.getSelectRows();
        //get all select cols:
        Map<GlobalTableData, List<GlobalColumnData>> columns = globalTableQueries.getSelectColumns();
        Map<Integer, String> measures = globalTableQueries.getMeasuresWithID();
        List<String> orderBy = globalTableQueries.getOrderBy(); //each order by is in the form 'tableName.column (ASC/DESC)'
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> r : rows.entrySet()){
            String tName = r.getKey().getTableName();
            for (GlobalColumnData c : r.getValue()){
                String cName = c.getName();
                for (String s : orderBy){
                    String [] splitted = s.split(" ");
                    String name = splitted[0];
                    String type = splitted[1];
                    if (name.equals(tName+"."+cName)){
                        c.setOrderBy(type);
                        orderBy.remove(s);
                        break;
                    }
                }
            }
        }
        boolean success = metaDataManager.insertNewQuerySave(nameTxt.getText(), cubeSelectionComboBox.getSelectedItem().toString(), rows, columns, measures, (FilterNode) filterTreeModel.getRoot() );
        if (success){
            JOptionPane.showMessageDialog(mainMenu, "Query "+nameTxt.getText()+" save successfully!", "Query saved", JOptionPane.PLAIN_MESSAGE);
        }
        else{
            JOptionPane.showMessageDialog(mainMenu, "Query "+nameTxt.getText()+" could not be saved.", "Query not saved", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteQuery(String queryName){
        metaDataManager.deleteQuery(queryName, cubeSelectionComboBox.getSelectedItem().toString());
    }

    private void saveToFile(boolean multiline){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {

            public String getDescription() {
                return "txt file (*.txt)";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    String filename = f.getName().toLowerCase();
                    return filename.endsWith(".txt");
                }
            }
        });
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(mainMenu);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            //validate
            String[] fileSplit = fileToSave.toString().split(".");
            if (fileSplit.length == 2 && fileSplit[1].equals("txt")) {
                // filename is OK as-is
            } else {
                fileToSave = new File(fileToSave.toString() + ".txt");  // append .xml if "foo.jpg.xml" is OK
            }
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            boolean success = false;
            if (multiline)
                success = saveAllQueriesLog(fileToSave);
            else
                success = saveLogToFile(queryLogModel.get(queryLogList.getSelectedIndex()).toString(), fileToSave);
            if (success){
                JOptionPane.showMessageDialog(mainMenu, "Query Log Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(mainMenu, "Failed to save query log. Select an apropriate folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean saveAllQueriesLog(File file){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < queryLogModel.getSize(); i++) {
                //saveLogToFile(queryLogModel.get(i).toString(), folder);
                List<String> limitLine = textLimiter(queryLogModel.get(i).toString(), 80);
                for (String s : limitLine)
                    writer.write(s+"\n");
                writer.write("\n ------------------------------------------------------------------------------- \n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean saveLogToFile(String log, File folder){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(folder));
            List<String> limitLine = textLimiter(log, 80);
            for (String s : limitLine)
                writer.write(s+"\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Limit the ammount of chars by line when saving to file
     * @param input
     * @param limit
     * @return
     */
    private List<String> textLimiter(String input, int limit) {
        List<String> returnList = new ArrayList<>();
        String[] parts = input.split("[ ,\n]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() + part.length() > limit) {
                returnList.add(sb.toString().substring(0, sb.toString().length() - 1));
                sb = new StringBuilder();
            }
            sb.append(part + " ");
        }
        if (sb.length() > 0) {
            returnList.add(sb.toString());
        }
        return returnList;
    }

    public static void main(String[] args){
        QueryUI m = new QueryUI("My Project", null);
        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(950, 800));
        frame.setContentPane(m);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Data source configuration wizard");
        frame.pack();
        frame.setVisible(true);
    }

    public static List<GlobalTableData> generateGlobalSchema(){
        java.util.List<GlobalTableData> globalTableDataList = new ArrayList<>();
        GlobalTableData g1 = new GlobalTableData("employees");
        //GlobalTableData g2 = new GlobalTableData("inventory");
        //TableData table4 = new TableData("products", "schema", dbData3, 4);
        Set<ColumnData> colsA = new HashSet<>();
        Set<ColumnData> colsB = new HashSet<>();
        Set<ColumnData> colsC = new HashSet<>();
        Set<ColumnData> colsD = new HashSet<>();
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.PostgreSQL, "parisDB");
        DBData dbData3 = new DBData("http://192.168.23.5", DBModel.MongoDB, "inventory");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        TableData table4 = new TableData("products", "schema", dbData3, 4);
        colsA.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table1).build());
        colsB.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build());
        colsC.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build());
        colsD.add(new ColumnData.Builder("email", "varchar", false).withTable(table1).build());
        colsA.add(new ColumnData.Builder("id", "integer", true).withTable(table2).build());
        colsB.add(new ColumnData.Builder("name", "varchar", false).withTable(table2).build());
        colsA.add(new ColumnData.Builder("id", "integer", true).withTable(table3).build());
        colsC.add(new ColumnData.Builder("phone", "integer", false).withTable(table3).build());
        colsD.add(new ColumnData.Builder("email", "varchar", false).withTable(table3).build());

        GlobalColumnData globalColA = new GlobalColumnData("id", "integer", true, colsA);
        GlobalColumnData globalColB = new GlobalColumnData("name", "varchar", true, colsB);
        GlobalColumnData globalColC = new GlobalColumnData("phone_number", "varchar", false, colsC);
        GlobalColumnData globalColD = new GlobalColumnData("email", "varchar", false, colsD);

        /*GlobalColumnData globalColMongo1 = new GlobalColumnData("product_id", "integer", true, new ColumnData.Builder("product_id", "integer", false).withTable(table4).build());
        GlobalColumnData globalColMongo2 = new GlobalColumnData("product_name", "varchar", false, new ColumnData.Builder("product_name", "varchar", false).withTable(table4).build());
        GlobalColumnData globalColMongo3 = new GlobalColumnData("price", "double", false, new ColumnData.Builder("price", "double", false).withTable(table4).build());
        GlobalColumnData globalColMongo4 = new GlobalColumnData("UnitsInStock", "integer", false, new ColumnData.Builder("UnitsInStock", "integer", false).withTable(table4).build());*/
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
        //g2.setGlobalColumnData(Arrays.asList(globalColMongo1, globalColMongo2, globalColMongo3, globalColMongo4));
        globalTableDataList.add(g1);
        //globalTableDataList.add(g2);
        return globalTableDataList;
    }

    public DefaultTreeModel setStarSchemaTree(){
        if (this.starSchema == null)
            return null;
        FactsTable facts = starSchema.getFactsTable();
        CustomTreeNode root = new CustomTreeNode("root", NodeType.GLOBAL_TABLES);
        CustomTreeNode factsNode = new CustomTreeNode("Measures of "+facts.getGlobalTable().getTableName(),facts.getGlobalTable(), NodeType.FACTS_TABLE);
        //set columns that are measures ONLY
        Map<GlobalColumnData, Boolean> cols = facts.getColumns();
        for (Map.Entry<GlobalColumnData, Boolean> col : cols.entrySet()){
            if (col.getValue() == true){
                //is measure, add
                GlobalColumnData measure = col.getKey();
                factsNode.add(new CustomTreeNode(measure.getName(), measure, NodeType.MEASURE));
            }
        }
        //dimension tables
        CustomTreeNode dimensionsNode = new CustomTreeNode("Dimensions", NodeType.GLOBAL_TABLES);
        for (GlobalTableData gt : starSchema.getDimsTables() ) {
            CustomTreeNode tables = new CustomTreeNode(gt.getTableName(), gt, NodeType.GLOBAL_TABLE);
            //global cols
            for (GlobalColumnData col : gt.getGlobalColumnDataList()) {
                CustomTreeNode column = new CustomTreeNode(col.getName(), col, NodeType.GLOBAL_COLUMN);
                column.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                if (col.isPrimaryKey())
                    column.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                tables.add(column);
            }
            dimensionsNode.add(tables);
        }
        root.add(factsNode);
        root.add(dimensionsNode);
        return new DefaultTreeModel(root);
    }

    private ActionListener getRemoveActionListenerForColumnList(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                ListElementWrapper elem = (ListElementWrapper)columnListModel.get(index);
                GlobalColumnData col = null;
                if (elem.getType() == ListElementType.GLOBAL_COLUMN){
                    col = (GlobalColumnData) elem.getObj();
                }
                else{
                    return;
                }
                GlobalTableData table = getTableInRowIndex(columnListModel, index);
                boolean success = globalTableQueries.deleteSelectColumnFromTable(table, col);
                if (success){
                    int indexBefore = index - 1;
                    int indexAfter = index + 1;
                    if (columnListModel.size()==2){
                        //remove the table name from the list (if only one table was present and all its columns was deleted)
                        columnListModel.remove(index);
                        columnListModel.remove(0);
                    }
                    else if (!columnListModel.get(indexBefore).toString().contains("    ") && indexAfter < columnListModel.size()-1 && !columnListModel.get(indexAfter).toString().contains("    ")){
                        columnListModel.remove(index);
                        columnListModel.remove(indexBefore);
                    }
                    else{
                        columnListModel.remove(index);
                    }
                    columnsList.revalidate();
                }
            }
        };
    }

    private ActionListener getRemoveActionListenerForRowsList(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                ListElementWrapper elem = (ListElementWrapper)rowsListModel.get(index);
                GlobalColumnData col = null;
                if (elem.getType() == ListElementType.GLOBAL_COLUMN){
                    col = (GlobalColumnData) elem.getObj();
                }
                else{
                    return;
                }
                GlobalTableData table = getTableInRowIndex(rowsListModel, index);
                boolean success = globalTableQueries.deleteSelectRowFromTable(table, col);
                if (success){
                    int indexBefore = index - 1;
                    int indexAfter = index + 1;
                    if (rowsListModel.size()==2){
                        //remove the table name from the list (if only one table was present and all its columns was deleted)
                        rowsListModel.remove(index);
                        rowsListModel.remove(0);
                    }
                    else if (!rowsListModel.get(indexBefore).toString().contains("    ") && indexAfter < rowsListModel.size()-1 && !rowsListModel.get(indexAfter).toString().contains("    ")){
                        rowsListModel.remove(index);
                        rowsListModel.remove(indexBefore);
                    }
                    else{
                        rowsListModel.remove(index);
                    }
                    rowsList.revalidate();
                }
            }
        };
    }

    private ActionListener getAddGroupByListenerRows(int index, boolean isAsc) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                addGroupBy(index, isAsc);
            }
        };
    }

    public void addGroupBy(int index, boolean isAsc){
            if (index < 0)
                return;
            ListElementWrapper elem = (ListElementWrapper)rowsListModel.get(index);
            if (elem.getType() != ListElementType.GLOBAL_COLUMN){
                return;
            }
            String colName = rowsListModel.getElementAt(index).toString();
            colName = colName.replaceAll("\\s+", "");
            String tableName = getTableNameOfColumnInList(rowsListModel, index);
            if (tableName == null)
                return;
            //add to the list (ASC or DESC)
            String order = "";
            if (isAsc)
                order = "ASC";
            else
                order = "DESC";
            rowsListModel.setElementAt(rowsListModel.getElementAt(index).toString()+ " ("+order+")", index);//update element with ASC or DESC to signal it will be ordered
            globalTableQueries.addOrderByRow(tableName+"."+colName+" "+order);
            rowsList.revalidate();
            rowsList.updateUI();
    }

    private String getTableNameOfColumnInList(DefaultListModel listModel, int colIndex) {//must be either column or row list
        for (int i = colIndex-1; i >= 0; i--){
            String name = listModel.getElementAt(i).toString();
            if (!name.contains("    ")){
                return name;
            }
        }
        return null;
    }

    private ActionListener getRemoveGroupByListenerRows(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;

                String colName = rowsListModel.getElementAt(index).toString();
                colName = colName.split(" \\(")[0]; //keep element in list, but remove (ASC) or (DESC)

                String colNameOnly = colName;
                colNameOnly.replaceAll("\\s+", "");
                String tableName = getTableNameOfColumnInList(rowsListModel, index);
                if (tableName == null)
                    return;
                rowsListModel.setElementAt(colName, index);//update element with ASC or DESC to signal it will be ordered
                globalTableQueries.removeOrderByIfPresent(tableName+"."+colNameOnly);
                rowsList.revalidate();
            }
        };
    }

    private ActionListener getRemoveActionListenerForMeasuresList(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                globalTableQueries.removeMeasure(measuresListModel.getElementAt(index).toString());
                measuresListModel.remove(index);
                measuresList.updateUI();
                measuresList.revalidate();
            }
        };
    }

    private ActionListener getAddNOTActionListenerOnNestedExprssion(FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    FilterNode notNode = new FilterNode("NOT", "NOT", FilterNodeType.BOOLEAN_OPERATION);
                    List <FilterNode> childNodes = getAllChildNodes(node);
                    //remove all childs, make a new child called not and have all childs be child of not node
                    for (FilterNode n : childNodes)
                        node.remove(n);
                    node.add(notNode);
                    for (FilterNode n : childNodes)
                        notNode.add(n);
                    filterTree.expandPath(new TreePath(notNode.getPath()));
                    filterTree.repaint();
                    filterTree.updateUI();
                }
            }
        };
    }

    private ActionListener getAddNOTActionListener(FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    parent.remove(node);
                    FilterNode notNode = new FilterNode("NOT", "NOT", FilterNodeType.BOOLEAN_OPERATION);
                    notNode.add(node);
                    parent.insert(notNode, index);
                    filterTree.expandPath(new TreePath(notNode.getPath()));
                    filterTree.repaint();
                    filterTree.updateUI();
                }
            }
        };
    }


    private ActionListener getRemoveFilterNodActionListener(FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {//TODO: bug when removing first filter, boolean op must be deleted and is not (what if a sub condition exists..)
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    int childCount = parent.getChildCount();
                    if (index > 0){
                        //
                        FilterNode nodeAbove = (FilterNode) parent.getChildAt(index-1);
                        if (nodeAbove.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            filterTreeModel.removeNodeFromParent(nodeAbove);
                        }
                    }
                    else if (index == 0 && childCount > 1){
                        //if there is a boolean operation below, remove it
                        FilterNode nodeBellow = (FilterNode) parent.getChildAt(index+1);
                        if (nodeBellow.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            filterTreeModel.removeNodeFromParent(nodeBellow);
                        }
                    }
                    filterTreeModel.removeNodeFromParent(node);
                    GlobalColumnData c = (GlobalColumnData) node.getObj();
                    globalTableQueries.removeFilter(c.getFullName());
                    System.out.println(filters);
                    filterTree.repaint();
                    filterTree.updateUI();
                }
            }
        };
    }

    private ActionListener changeBooleanOperation(String booleanOp, FilterNode selectedNode) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //FilterNode node = (FilterNode) parentNode.getChildAt(index);
                selectedNode.setUserObject(booleanOp);
                filterTree.repaint();
                filterTree.updateUI();
            }
        };
    }
    /**
     * In the list of columns, returns the table name that contains the column with the index specified
     * @param index
     * @return
     */
    private GlobalTableData getTableInColumnIndex(int index){
        for (int i = index; i >=0; i--){
            ListElementWrapper elem = (ListElementWrapper) columnListModel.get(i);
            if (elem.getType() == ListElementType.GLOBAL_TABLE){//first table to appear belongs to this
                return (GlobalTableData) elem.getObj();
            }
        }
        return null;
    }

    private List<FilterNode> getAllChildNodes(FilterNode node){
        List<FilterNode> nodes = new ArrayList<>();
        int nChilds = node.getChildCount();
        for (int i = 0; i < nChilds; i++){
            FilterNode child = (FilterNode) node.getChildAt(i);
            nodes.add(child);
        }

        return nodes;
    }

                                              /**
     * In the list of rows, returns the table name that contains the column with the index specified
     * @param index
     * @return
     */
    private GlobalTableData getTableInRowIndex(DefaultListModel model, int index){
        for (int i = index; i >=0; i--){
            ListElementWrapper elem = (ListElementWrapper) model.get(i);
            if (elem.getType() == ListElementType.GLOBAL_TABLE){//first table to appear belongs to this
                return (GlobalTableData) elem.getObj();
            }
        }
        return null;
    }

    private void expandAllStarSchema(TreePath path, boolean expand) {
        CustomTreeNode node = (CustomTreeNode) path.getLastPathComponent();
        if (node.getNodeType() == NodeType.GLOBAL_COLUMN )
            return;
        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                CustomTreeNode n = (CustomTreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);

                expandAllStarSchema(p, expand);
            }
        }

        if (expand) {
            schemaTree.expandPath(path);
        } else {
            schemaTree.collapsePath(path);
        }
    }

    public String getFilterQuery(){
        if (this.filterTreeModel == null) //easy fix
            return "";
        FilterNode root = (FilterNode) this.filterTreeModel.getRoot();
        int nChilds = root.getChildCount();
        if (nChilds <= 0){
            return "";
        }
        String query = "";
        for (int i = 0 ; i < nChilds; i++){
            FilterNode filterNode = (FilterNode) root.getChildAt(i);
            query += filterNode.getUserObject().toString() +" ";
            query += processInnerExpressions(filterNode);
        }
        System.out.println("Filter query: "+query);
        return query;
    }

    private String processInnerExpressions(FilterNode filterNode){
        String query = "";
        if (filterNode.getChildCount() > 0){
            query +="(";
            for (int j = 0 ; j < filterNode.getChildCount(); j++){
                FilterNode innerFilterNode = (FilterNode) filterNode.getChildAt(j);
                /*String c = innerFilterNode.getUserObject().toString();
                if (innerFilterNode.getNodeType() == FilterNodeType.CONDITION && !Utils.stringIsNumericOrBoolean(c)){
                    c = "'"+c+"'";
                }
                query += c +" ";*/
                query += innerFilterNode.getUserObject().toString()+" ";
                query += processInnerExpressions(innerFilterNode);
            }
            query +=")";
        }
        return query;
    }

    private boolean filterColumnExistsInRows(){
        for (String s : filters){
            String tableName = s.split("\\.")[0];
            String columnName = s.split("\\.")[1];
            boolean isInRows = false;
            for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rows : globalTableQueries.getSelectRows().entrySet()){
                GlobalTableData gt = rows.getKey();
                if (gt.getTableName().equals(tableName)){
                    List<GlobalColumnData> gcs = rows.getValue();
                    for (GlobalColumnData gc : gcs){
                        if (gc.getName().equals(columnName)){
                            isInRows = true;
                        }
                    }
                }
            }
            if (!isInRows){//this filter column is not selected in the rows
                return false;
            }
        }
    return true;//all filter column are seleced in the rows
    }

    public void executeQueryAndShowResults(){
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                DateTime beginTime = new DateTime();
                //validate query string
                /*if (!filterColumnExistsInRows()){
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                    backButton.setEnabled(true);
                    JOptionPane.showMessageDialog(mainMenu, "There is one or more columns in filters not selected\n in the rows area.", "Invalid Query", JOptionPane.ERROR_MESSAGE);
                    return null;
                }*/
                //buld query string
                globalTableQueries.setFilterQuery(getFilterQuery());

                String localQuery = globalTableQueries.buildQuery();//create query with inner query to get local table data
                System.out.println(localQuery);
                if (localQuery.contains("Error")){
                    JOptionPane.showMessageDialog(null, "Could not execute query:\n"+localQuery, "Query Error", JOptionPane.ERROR_MESSAGE);
                    queryLogModel.addElement(new QueryLog(localQuery, beginTime, null, 0));
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                    backButton.setEnabled(true);
                    return null;
                }
                //execute query by presto
                firePropertyChange("querying", null, null);
                ResultSet results = prestoMediator.getLocalTablesQueries(localQuery);

                //process query results
                firePropertyChange("results_processing", null, null);
                if (results == null){
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                    backButton.setEnabled(true);
                    JOptionPane.showMessageDialog(mainMenu, "Query returned with no results. Check if Presto is running and\nthat the data source is also available.",
                            "Query empty", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                setResultsAndCreateLog(results, localQuery, beginTime);
                LoadingScreenAnimator.closeGeneralLoadingAnimation();
                backButton.setEnabled(true);
                return null;
            }

            protected void done() {
                try {
                    //System.out.println("Done");
                    get();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    String msg = String.format("Unexpected problem: %s",
                            e.getCause().toString());
                    //JOptionPane.showMessageDialog(mainMenu,
                     //       msg, "Error", JOptionPane.ERROR_MESSAGE);
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                } catch (InterruptedException e) {
                    // Process e here
                }
            }
        };
        LoadingScreenAnimator.openGeneralLoadingAnimation(mainMenu, "Constructing Query...");
        worker.execute();
        worker.addPropertyChangeListener( new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("build_query".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Constructing Query...");
                }
                else if ("querying".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Executing Query...");
                }
                else if ("results_processing".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Processing Query Results...");
                }
            }
        });
        backButton.setEnabled(false);
    }

    private void setResultsAndCreateLog(ResultSet results, String localQuery, DateTime beginTime){
        if (results == null)
            return;
        //defaultTableModel.setColumnCount(0);
        //defaultTableModel.setRowCount(0);//clear any previous results

        IModelFieldGroup[] cols = null;
        ModelData data = null;

        //insert column results in JTable
        int nRows = 0;
        try {
            ResultSetMetaData rsmd = results.getMetaData();
            int columnCount = rsmd.getColumnCount() + 1;
            ;//+1 for line col

            List<List<String>> pivots = globalTableQueries.getPivotValues();
            //pivots.clear();
            List<ModelRow> rows = new ArrayList<>();
            if (pivots.size() > 0 && pivots.get(0).size() > 1) {//if there are pivoted columns and the number of columns that were pivot is biggger than one, then it is necessary to group multiple column headers
                data = createMultiHeaders(pivots, rsmd, columnCount, results);

            } else { //no pivoted columns or only one pivoted column, there is only one level of column headers
                cols = new IModelFieldGroup[columnCount];
                cols[0] = new ModelField(" ", " ");
                for (int i = 1; i < columnCount; i++) {
                    String name = rsmd.getColumnName(i);
                    cols[i] = new ModelField(name, name);
                }
                data = new ModelData(cols);
                //place rows
                //results.beforeFirst(); //return to begining

                while (results.next()) {
                    //Fetch each row from the ResultSet, and add to ArrayList of rows
                    rows.add(new ModelRow(columnCount));
                    rows.get(nRows).setValue(0, (nRows + 1) + "");
                    for (int i = 0; i < columnCount - 1; i++) {
                        //Again, note that ResultSet column indices start at 1
                        //currentRow[i+1] = results.getString(i+1);//first column (index 0) is the line number)
                        rows.get(nRows).setValue(i + 1, results.getString(i + 1));//first column (index 0) is the line number)
                    }
                    nRows++;
                    //defaultTableModel.addRow(rows);
                }

                ModelRow[] rowsArray = new ModelRow[rows.size()];
                rowsArray = rows.toArray(rowsArray);
                data.setRows(rowsArray);
            }
            //add elements to table and add table to scrollpane
            queryResultsTableGroupable = new JBroTable(data);
            queryResultsTableGroupable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//maintain column width
            tablePane.setViewportView(queryResultsTableGroupable);
            DefaultTableCellRenderer rendar1 = new DefaultTableCellRenderer();
            rendar1.setBackground(new Color(238, 238, 238));//same color on line rows as header
            queryResultsTableGroupable.getColumnModel().getColumn(0).setCellRenderer(rendar1);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        DateTime endTime = new DateTime();
        queryLogModel.addElement(new QueryLog(localQuery, beginTime, endTime, nRows));
        queryResultsTableGroupable.revalidate();
    }

    private ModelData createMultiHeaders(List<List<String>> pivotValues, ResultSetMetaData rsmd, int columnCount, ResultSet results) throws SQLException {
        List<IModelFieldGroup> cols = new ArrayList<>();
        cols.add(new ModelField( " ", " " ));
        int nNonPivotTables = columnCount - (pivotValues.size() +1);//get number of columns from elements that are not pivoted columns
        int nNonPivotTablesAndLineCOl = nNonPivotTables+1;
        //nNonPivotTables++;//add the column with line numbers
        //add column names of non pivoted columns
        for (int i = 1; i <= nNonPivotTables; i++) {//start iterating on first pivot column
            String name = rsmd.getColumnName(i);
            cols.add(new ModelField( name, name ));
        }

        int nLevels =  pivotValues.get(0).size();

        //for all list of values group similar values at header 0 add only different values
        cols.add(new ModelFieldGroup(pivotValues.get(0).get(0), pivotValues.get(0).get(0)));
        for (int i = 1; i < pivotValues.size(); i++){//start on the second values list, fisrt already inserted
            String value = pivotValues.get(i).get(0);
            if (!cols.get(cols.size()-1).getCaption().equals(value)){//check of previous value has same value. if it does not, add new value
                cols.add(new ModelFieldGroup(value, value));
            }
        }

        //2nd level of values if there are 3 levels if it exists
        if (nLevels == 3) {
            for (int i = 0; i < pivotValues.size(); i++) {//start on the second values list, fisrt already inserted
                String value = pivotValues.get(i).get(1);//2nd value of each list
                String parentValue = pivotValues.get(i).get(0);//value that appears on same list, one level up
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking for the parent after the 'one level columns'
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    //fieldParent = (ModelFieldGroup) fieldParent.getChild(parentValue);
                    if (fieldParent == null){
                        continue;
                    }
                    if (fieldParent.getCaption().equals(parentValue)) {
                        ModelFieldGroup childField = (ModelFieldGroup) fieldParent.getChild(value);
                        if (childField == null){
                            ModelFieldGroup secondLevelField = new ModelFieldGroup(value+i, value);
                            fieldParent.withChild(secondLevelField);//add this value as child
                            break;
                        }
                        String child = childField.getCaption();
                        if (child != null && child.equals(value)){//this value is already child, move to next value
                            break;
                        }
                    }
                }
            }
        }

        int lastLevelIndex = nLevels-1;
        //do the same but for last elements, (leaf headers) and put them in respective parents. These are either the 3rd or 2nd level of headers
        for (int i = 0; i < pivotValues.size(); i++) {
            String value = pivotValues.get(i).get(lastLevelIndex);
            ModelField leafField = new ModelField(value + i, value);
            if (nLevels == 3) {//3 header levels
                String parentSecondHeaderValue = pivotValues.get(i).get(1);
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking after the 'one level columns'
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    String valueTopHeader = pivotValues.get(i).get(0);
                    if (fieldParent.getCaption().equals(valueTopHeader)){
                        fieldParent = (ModelFieldGroup) fieldParent.getChild(parentSecondHeaderValue+i);//get child requires IDENTIFIER AND NOT CAPTION!!
                        if (fieldParent != null) {
                            fieldParent.withChild(leafField);
                            break;
                        }
                    }
                }
            } else if (nLevels == 2) {
                String parentValue = pivotValues.get(i).get(lastLevelIndex - 1);
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking after the 'one level columns'
                    //only 2 column headers
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    if (fieldParent.getCaption().equals(parentValue)) {
                        fieldParent.withChild(leafField);
                    }
                }
            }
        }

        IModelFieldGroup[] colsArray = new IModelFieldGroup[cols.size()];
        colsArray = cols.toArray(colsArray);

        List<ModelRow> rows = new ArrayList<>();
        ModelData data = new ModelData(colsArray);

        int nRows = 0;

        while (results.next()) {
            //Fetch each row from the ResultSet, and add to ArrayList of rows
            rows.add(new ModelRow(columnCount));
            rows.get(nRows).setValue(0, (nRows + 1) + "");
            for (int i = 0; i < columnCount - 1; i++) {
                //Again, note that ResultSet column indices start at 1
                //currentRow[i+1] = results.getString(i+1);//first column (index 0) is the line number)
                rows.get(nRows).setValue(i + 1, results.getString(i + 1));//first column (index 0) is the line number)
            }
            nRows++;
            //defaultTableModel.addRow(rows);
            ModelRow[] rowsArray = new ModelRow[rows.size()];
            rowsArray = rows.toArray(rowsArray);
            data.setRows(rowsArray);
        }

        return data;
    }

    private MouseListener getMouseListenerForColumnList() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    int index = rowsList.locationToIndex(arg0.getPoint());

                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem item1 = new JMenuItem("Delete");
                    item1.addActionListener(getRemoveActionListenerForColumnList(index));
                    //item1.addActionListener(getRemoveActionListener());
                    menu.add(item1);
                    columnsList.setComponentPopupMenu(menu);
                }
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForRowsList() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                //if (SwingUtilities.isRightMouseButton(arg0)){
                int index = rowsList.locationToIndex(arg0.getPoint());
                if (index < 0)
                    return;
                if (!rowsListModel.getElementAt(index).toString().contains("    ")){
                    rowsList.setComponentPopupMenu(null);
                    return;
                }
                rowsList.setSelectedIndex(index);
                JPopupMenu menu = new JPopupMenu();
                JMenuItem item1 = new JMenuItem("Delete");
                JMenu subMenu = new JMenu("Order by");
                JMenuItem subItem = new JMenuItem("Ascending");
                JMenuItem subItem2 = new JMenuItem("Descending");
                JMenuItem subItem3 = new JMenuItem("No order");
                subMenu.add(subItem);
                subMenu.add(subItem2);
                subMenu.add(subItem3);
                item1.addActionListener(getRemoveActionListenerForRowsList(index));
                subItem.addActionListener(getAddGroupByListenerRows(index, true));
                subItem2.addActionListener(getAddGroupByListenerRows(index, false));
                subItem3.addActionListener(getRemoveGroupByListenerRows(index));
                //item1.addActionListener(getRemoveActionListener());
                menu.add(item1);
                menu.add(subMenu);
                rowsList.setComponentPopupMenu(menu);
                //}
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForMeasuresList() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    int index = measuresList.locationToIndex(arg0.getPoint());

                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem item1 = new JMenuItem("Delete");
                    item1.addActionListener(getRemoveActionListenerForMeasuresList(index));
                    //item1.addActionListener(getRemoveActionListener());
                    menu.add(item1);
                    measuresList.setComponentPopupMenu(menu);
                }
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForFilterTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                //if (SwingUtilities.isRightMouseButton(arg0)){
                    TreePath pathForLocation = filterTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    filterTree.setSelectionPath(pathForLocation);
                    FilterNode selectedNode = null;
                    if(pathForLocation != null) {
                        selectedNode = (FilterNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == FilterNodeType.CONDITION){
                            //menu for a condition
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem item1 = new JMenuItem("Delete");
                            JMenuItem item2 = new JMenuItem("NOT");
                            item1.addActionListener(getRemoveFilterNodActionListener(selectedNode));
                            item2.addActionListener(getAddNOTActionListener(selectedNode));
                            menu.add(item1);
                            menu.add(item2);
                            filterTree.setComponentPopupMenu(menu);
                        }
                        else if (selectedNode.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            //menu for a boolean op
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem item1 = new JMenuItem("AND");
                            JMenuItem item2 = new JMenuItem("OR");
                            JMenuItem item3 = new JMenuItem("NOT on nested expression");
                            item1.addActionListener(changeBooleanOperation("AND", selectedNode));
                            item2.addActionListener(changeBooleanOperation("OR", selectedNode));
                            item3.addActionListener(getAddNOTActionListenerOnNestedExprssion(selectedNode));
                            menu.add(item1);
                            menu.add(item2);
                            if (selectedNode.getChildCount()>0)
                                menu.add(item3);
                            filterTree.setComponentPopupMenu(menu);
                        }
                    }
                //}
                super.mousePressed(arg0);
            }
        };
    }

    private void addColumnsToList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
        //first iterate to check if maximum value of columns is achieved:
        int nCols = 0;
        for (int i = 0; i < listModel.getSize(); i++) {
            if (String.valueOf(listModel.getElementAt(i)).contains("    ")){
                nCols++;
            }
        }
        if (nCols >= MAX_SELECT_COLS){
            JOptionPane.showMessageDialog(mainMenu, "Maximum number of columns Reached", "Maximum number of columns Reached.\nDelete columns to add new ones.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String[] s = null;
        //check if table name of this column exists. If true then inserted here
        ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);
        if (listModel.contains(elemtTosearch)){
            int index = listModel.indexOf(elemtTosearch);
            index++;
            //iterate the columns of this tables. insert a new one
            for (int i = index; i < listModel.getSize(); i++) {
                if (!String.valueOf(listModel.getElementAt(i)).contains("    ")){
                    listModel.add(i, new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
                    globalTableQueries.addSelectColumn(globalTable, globalCol);
                    return;
                }
            }
            //maybe this table is the last one, insert at last position
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
            globalTableQueries.addSelectColumn(globalTable, globalCol);
            return;
        }
        else{
            listModel.addElement(new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE)); //add table name
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
            globalTableQueries.addSelectColumn(globalTable, globalCol);
        }
    }

    private void addRowsToList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
        //check if table name of this column exists. If true then inserted here
        ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);
        if (listModel.contains(elemtTosearch)){
            int index = listModel.indexOf(elemtTosearch);
            index++;
            //iterate the columns of this tables. insert a new one
            for (int i = index; i < listModel.getSize(); i++) {
                if (!String.valueOf(listModel.getElementAt(i)).contains("    ")){
                    listModel.add(i, new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
                    globalTableQueries.addSelectRow(globalTable, globalCol);
                    return;
                }
            }
            //maybe this table is the last one, insert at last position
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
            globalTableQueries.addSelectRow(globalTable, globalCol);
            return;
        }
        else{
            listModel.addElement(new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE)); //add table name
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
            globalTableQueries.addSelectRow(globalTable, globalCol);
        }
    }

    private void addMeasure(DefaultListModel listModel, String measureStr){
        //make sure this measure is not added already
        for (int i = 0; i < listModel.size(); i++){
            if (listModel.get(i).toString().equals(measureStr)){
                JOptionPane.showMessageDialog(mainMenu, "Measure already present. Cannot add repeated Measure.", "Cannot add measure", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        listModel.add(listModel.size(), measureStr);
        String[] splitres = measureStr.split("[(]"); //in the form "aggr(measureName)"
        if (splitres.length == 1){
            measureStr = "SIMPLE ("+measureStr+")";//case in which user does not use any operation
        }
        globalTableQueries.addMeasure(measureStr);
    }

    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        private int[] indices = null;
        private int addIndex = -1; //Location where items were added
        private int addCount = 0;  //Number of items added.

        public TreeTransferHandler() {
            nodesFlavor = new DataFlavor(CustomTreeNode.class, "custom node");
            flavors[0] = nodesFlavor;
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) {
                return false;
            }

            support.setShowDropLocation(true);
            if(!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            return true;
        }

        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree)c;
            TreePath[] paths = tree.getSelectionPaths();
            if(paths != null) {
                // exportDone after a successful drop.
                CustomTreeNode node =
                        (CustomTreeNode)paths[0].getLastPathComponent();
                return new TreeTransferHandler.NodesTransferable(node);
            }
            return null;
        }

        /** Defensive copy used in createTransferable. */
        private CustomTreeNode copy(TreeNode node) {
            return new CustomTreeNode(node);
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if((action & MOVE) == MOVE) {
                JTree tree = (JTree)source;
                DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            }
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            Transferable t = info.getTransferable();
            CustomTreeNode data;
            try {
                data = (CustomTreeNode) t.getTransferData(nodesFlavor);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (data.getNodeType() != NodeType.GLOBAL_COLUMN && data.getNodeType() != NodeType.MEASURE){
                JOptionPane.showMessageDialog(null, "You can only drag and drop columns.",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            GlobalColumnData column = (GlobalColumnData) data.getObj();
            CustomTreeNode globalTable = (CustomTreeNode) data.getParent();
            GlobalTableData gt = (GlobalTableData) globalTable.getObj();
            column.setFullName(gt.getTableName()+"."+column.getName());

            if (info.getComponent() instanceof JList){
                JList list = (JList) info.getComponent();
                DefaultListModel listModel = (DefaultListModel) list.getModel();
                JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
                int index = dl.getIndex();
                if (index == -1)
                    index = 0;

                addIndex = index;

                if (list.equals(columnsList)){
                    //select columns list
                    CustomTreeNode parentNode = (CustomTreeNode) data.getParent();//global table
                    addColumnsToList(listModel, (GlobalColumnData)data.getObj(), (GlobalTableData) parentNode.getObj()) ;
                }
                else if (list.equals(rowsList)){
                    //select rows list
                    CustomTreeNode parentNode = (CustomTreeNode) data.getParent();//global table
                    addRowsToList(listModel, (GlobalColumnData)data.getObj(), (GlobalTableData) parentNode.getObj()) ;
                }
                else if (list.equals(measuresList)){
                    if (data.getNodeType() != NodeType.MEASURE){
                        JOptionPane.showMessageDialog(mainMenu, "You can only drag measures to this area.", "Measures only", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    String measureStr = "";
                    if (aggregationOpComboBox.getSelectedIndex() == 0){//user selected no aggregation operation
                        measureStr = column.getName();
                    }
                    else
                        measureStr = aggregationOpComboBox.getSelectedItem().toString() +"("+ column.getName() +")";
                    addMeasure(listModel, measureStr);
                }
                return true;
            }
            //user drops on jtree
            else if (info.getComponent() instanceof JTree){
                JTree tree = (JTree) info.getComponent();
                if (tree.equals(filterTree)){
                    String s[] = null;
                    //user drops in the filter tree
                    if (filterTreeModel == null){//filters dropped for the first time (bug if root added on jtree creation, thats why there's two ifs..)
                        while (s == null){
                            s = createFilterStringOperation(column, true);
                        }
                        if (s.length == 0)
                            return false;
                        //no filters added yet
                        FilterNode root = new FilterNode("", null, null);
                        root.add(new FilterNode(s[1], column, FilterNodeType.CONDITION));
                        globalTableQueries.addFilter(column.getFullName());//for validation purposes
                        filterTreeModel = new DefaultTreeModel(root);
                        filterTree.setModel(filterTreeModel);
                        filterTree.setRootVisible(false);
                        filterTree.revalidate();
                        filterTree.updateUI();
                        return true;
                    }
                    FilterNode root = (FilterNode) filterTreeModel.getRoot();
                    //decide where to drop
                    JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
                    TreePath dest = dl.getPath();
                    FilterNode parent;
                    if (dest == null)
                        parent = root;
                    else
                        parent = (FilterNode)dest.getLastPathComponent();

                    TreePath path = null;
                    if (parent.getNodeType() == null && parent.getChildCount() == 0){
                        while (s == null){
                            s = createFilterStringOperation(column, true);//0 - boolean operation if any, 1 - condition
                        }
                        if (s.length == 0)
                            return false;
                        //filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                        filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                        filters.add(column.getFullName());//for validation purposes
                        path = new TreePath(parent.getPath());
                    }
                    else if (parent.getNodeType() == null && parent.getChildCount() > 0){
                        while (s == null){
                            s = createFilterStringOperation(column, false);//0 - boolean operation if any, 1 - condition
                        }
                        if (s.length == 0)
                            return false;
                        filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                        filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                        filters.add(column.getFullName());//for validation purposes
                        path = new TreePath(parent.getPath());
                    }
                    else if (parent.getNodeType() == FilterNodeType.CONDITION ){
                        //user wants to create an inner expression OR to add content to inner expression and dragg it to a condition
                        while (s == null){
                            s = createFilterStringOperation(column, false);//0 - boolean operation if any, 1 - condition
                        }
                        if (s.length == 0)
                            return false;
                        //get the boolean operator next to it and check if it has childs
                        FilterNode boleanNodeParent = (FilterNode) parent.getNextNode();

                        if (boleanNodeParent == null || boleanNodeParent.getChildCount() == 0){ //no boolean operator, create it and add child
                            //if creating a new expression nested in a codition, add a boolean operator between the condition and a boolean operator. Make the inner expression child of the boolean operator
                            FilterNode parentOfParent = (FilterNode) parent.getParent();
                            int indexOfParent = parentOfParent.getIndex(parent);
                            FilterNode booleanNode = new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION);
                            filterTreeModel.insertNodeInto(booleanNode, parentOfParent, indexOfParent+1);// create this condition between the condition and the inner expression
                            filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), booleanNode, booleanNode.getChildCount());//Must be child of the bolean operator
                            globalTableQueries.addFilter(column.getFullName());//for validation purposes
                            path = new TreePath(booleanNode.getPath());
                        }
                        else {
                            //inserting on an already existent boolean node with inner expr. IF it has an inner expr, add the operator and cond, else only the cond as childs
                            filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), boleanNodeParent, boleanNodeParent.getChildCount());
                            filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), boleanNodeParent, boleanNodeParent.getChildCount());
                            globalTableQueries.addFilter(column.getFullName());//for validation purposes
                            path = new TreePath(boleanNodeParent.getPath());
                        }
                    }
                    else if (parent.getNodeType() == FilterNodeType.BOOLEAN_OPERATION && parent.getChildCount()>0){
                        //user wants to add content to inner expression and dragg it to the outer boolean operator
                        while (s == null){
                            s = createFilterStringOperation(column, false);//0 - boolean operation if any, 1 - condition
                        }
                        if (s.length == 0)
                            return false;
                        //get the boolean operator next to it and check if it has childs
                        filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                        filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                        globalTableQueries.addFilter(column.getFullName());//for validation purposes
                        path = new TreePath(parent.getPath());
                    }
                    System.out.println(filters);
                    filterTree.expandPath(path);

                    filterTree.revalidate();
                    filterTree.updateUI();
                    return true;
                }
            }
            return false;
        }

        private String[] createFilterStringOperation(GlobalColumnData droppedCol, boolean isFirst){
            String s[] = new String [2];
            String elem = droppedCol.getFullName();
            //filter operations depende on data type (<, >, <= only for numeric OR date)
            String[] filterOps;
            if (droppedCol.isNumeric()) //TODO: also accept date time datatypes
                filterOps = numberOperations;
            else
                filterOps = stringOperations;
            JComboBox filter = new JComboBox(filterOps);
            JComboBox logicOperation = new JComboBox(LogicOperation.getOpList());
            JTextField value = new JTextField();
            JComponent[] inputs = null;
            if (isFirst) {
                inputs = new JComponent[]{
                        new JLabel("Select Filter Operation"),
                        filter,
                        new JLabel("Filter value selection"),
                        value,
                };
            }
            else{
                //Need to add logic operation
                inputs = new JComponent[]{
                        new JLabel("Select Filter Operation"),
                        logicOperation,
                        new JLabel("Select Filter Operation"),
                        filter,
                        new JLabel("Filter value selection"),
                        value,
                };
            }
            int result = JOptionPane.showConfirmDialog(
                    null,
                    inputs,
                    "Filter operation and value selection",
                    JOptionPane.PLAIN_MESSAGE);
            String filterValue = "";
            if (result == JOptionPane.OK_OPTION) {
                filterValue = value.getText();
                if (!Utils.stringIsNumericOrBoolean(filterValue)){
                    filterValue = "'"+filterValue+"'";
                }
                if (filterValue.length() == 0){
                    JOptionPane.showMessageDialog(null, "Please insert a filter value with same data type",
                            "Operation Failed", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                elem+= " "+filter.getSelectedItem().toString() +" "+ filterValue;
            }
            else{
                JOptionPane.showMessageDialog(null, "Please select a filter operation and insert a filter value with same data type",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return new String[0];
            }
            if (!isFirst){
                s[0] = logicOperation.getSelectedItem().toString();
            }
            s[1] = elem;
            return s;
        }

        public String toString() {
            return getClass().getName();
        }

        public class NodesTransferable implements Transferable {
            CustomTreeNode nodes;

            public NodesTransferable(CustomTreeNode nodes) {
                this.nodes = nodes;
            }

            public CustomTreeNode getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return nodes;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return nodesFlavor.equals(flavor);
            }
        }
    }

    class NarrowOptionPane extends JOptionPane {

        NarrowOptionPane() {
        }

        public int getMaxCharactersPerLineCount() {
            return 100;
        }
    }
}
