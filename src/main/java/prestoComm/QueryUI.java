package prestoComm;

import helper_classes.*;
import jdk.nashorn.internal.objects.Global;
import wizards.global_schema_config.CustomTreeNode;
import wizards.global_schema_config.CustomeTreeCellRenderer;
import wizards.global_schema_config.GlobalSchemaConfigurationV2;
import wizards.global_schema_config.NodeType;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class QueryUI extends JPanel{
    private JTable queryResultsTable;
    private JTree schemaTree;
    private JList filterList;
    private JList aggregationList;
    private JComboBox aggregationOpComboBox;
    private JList columnsList;
    private JPanel mainPanel;
    private JComboBox cubeSelectionComboBox;
    private JButton executeQueryButton;
    private JButton backButton;
    private String project;

    private StarSchema starSchema;
    private GlobalTableQuery globalTableQueries;//used to store all queries for each global table, and their columns

    private DefaultTreeModel schemaTreeModel;
    private DefaultListModel fliterListModel;
    private DefaultListModel aggreListModel;
    private DefaultListModel columnListModel;
    private DefaultTableModel defaultTableModel;

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;
    private String projectName;
    private final String[] aggregations = { "count", "sum", "average", "min", "max"};
    private final String[] numberOperations = { "=", ">", "=>", "<", "<="};
    private final String[] stringOperations = { "=", "like"};
    //TODO: checkbox disntinct?

    private MainMenu mainMenu;

    public QueryUI(String projectName, MainMenu mainMenu){
        this.mainMenu = mainMenu;
        this.projectName = projectName;
        this.metaDataManager = new MetaDataManager(projectName);
        this.prestoMediator = new PrestoMediator();
        this.globalTableQueries = new GlobalTableQuery();

        List<String> starSchemas =  metaDataManager.getStarSchemaNames();
        if (starSchemas.isEmpty()){
            JOptionPane.showMessageDialog(null, "There are no star schemas in this project.", "No Star schemas found", JOptionPane.ERROR_MESSAGE);
            mainMenu.returnToMainMenu();
        }
        cubeSelectionComboBox.setModel(new DefaultComboBoxModel(starSchemas.toArray(new String[starSchemas.size()])));

        aggregationOpComboBox.setModel(new DefaultComboBoxModel(aggregations));

        this.starSchema = metaDataManager.getStarSchema(cubeSelectionComboBox.getSelectedItem().toString());
        schemaTreeModel = setStarSchemaTree();
        schemaTree.setModel(schemaTreeModel);
        schemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        schemaTree.setTransferHandler(new TreeTransferHandler());
        schemaTree.setDragEnabled(true);
        schemaTree.setRootVisible(false);

        fliterListModel = new DefaultListModel();
        aggreListModel = new DefaultListModel();
        columnListModel = new DefaultListModel();
        filterList.setModel(fliterListModel);
        aggregationList.setModel(aggreListModel);
        columnsList.setModel(columnListModel);
        filterList.setTransferHandler(new TreeTransferHandler());
        aggregationList.setTransferHandler(new TreeTransferHandler());
        columnsList.setTransferHandler(new TreeTransferHandler());

        //jtable
        this.defaultTableModel = new DefaultTableModel();
        this.queryResultsTable.setModel(defaultTableModel);

        backButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open wizard and edit current project
                mainMenu.returnToMainMenu();
            }
        });

        executeQueryButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open wizard and edit current project
                executeQuery();
            }
        });


        cubeSelectionComboBox.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                starSchema = metaDataManager.getStarSchema(cubeSelectionComboBox.getSelectedItem().toString());
                schemaTreeModel = setStarSchemaTree();
                schemaTree.setModel(schemaTreeModel);
                schemaTree.revalidate();
                schemaTree.updateUI();
            }
        });

        //pop up menus for each list
        //COLUMNS JLIST
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Delete");
        //item1.addActionListener(getRemoveActionListener());
        menu.add(item1);
        columnsList.setComponentPopupMenu(menu);

        add(mainPanel);
        this.setVisible(true);
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
        CustomTreeNode factsNode = new CustomTreeNode("Measures of "+facts.getGlobalTable().getTableName(), NodeType.GLOBAL_TABLES);
        //set columns that are measures ONLY
        Map<GlobalColumnData, Boolean> cols = facts.getColumns();
        for (Map.Entry<GlobalColumnData, Boolean> col : cols.entrySet()){
            if (col.getValue() == true){
                //is measure, add
                GlobalColumnData measure = col.getKey();
                factsNode.add(new CustomTreeNode(measure.getName(), measure, NodeType.GLOBAL_COLUMN));
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

    private MouseListener getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem item = new JMenuItem("Say hello");
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JOptionPane.showMessageDialog(null, "Hello "
                                    + columnsList.getSelectedValue());
                        }
                    });
                    menu.add(item);
                    menu.show(null, 5, columnsList.getCellBounds(
                            columnsList.getSelectedIndex() + 1,
                            columnsList.getSelectedIndex() + 1).y);
                }
                super.mousePressed(arg0);
            }
        };
    }



    public void executeQuery(){
        String localQuery = globalTableQueries.getLocalTableQuery();
        System.out.println(localQuery);
        if (localQuery.contains("Error")){
            JOptionPane.showMessageDialog(null, "Could not execute query:\n"+localQuery, "Query Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ResultSet results = prestoMediator.getLocalTablesQueries(localQuery);
        String[] cols = null;
        //insert column results in JTable
        try {
            ResultSetMetaData rsmd = results.getMetaData();
            int columnCount = rsmd.getColumnCount();
            cols = new String[columnCount];
            for (int i = 1; i <= columnCount; i++ ) {
                String name = rsmd.getColumnName(i);
                cols[i - 1] = name;
            }
            defaultTableModel.setColumnIdentifiers(cols);

            //place rows
            //results.beforeFirst(); //return to begining
            while(results.next()){
                //Fetch each row from the ResultSet, and add to ArrayList of rows
                String[] currentRow = new String[columnCount];
                for(int i = 0; i < columnCount; i++){
                    //Again, note that ResultSet column indecies start at 1
                    currentRow[i] = results.getString(i+1);
                }
                defaultTableModel.addRow(currentRow);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        queryResultsTable.revalidate();
    }

    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        private int[] indices = null;
        private int addIndex = -1; //Location where items were added
        private int addCount = 0;  //Number of items added.

        public TreeTransferHandler() {
            nodesFlavor = new DataFlavor(DefaultMutableTreeNode.class, "custom node");//TODO!! CustomTreeNode
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

            JList list = (JList) info.getComponent();
            DefaultListModel listModel = (DefaultListModel) list.getModel();
            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            int index = dl.getIndex();
            if (index == -1)
                index = 0;
            // Get the string that is being dropped.
            Transferable t = info.getTransferable();
            CustomTreeNode data;
            try {
                data = (CustomTreeNode) t.getTransferData(nodesFlavor);
            } catch (Exception e) {
                return false;
            }

            if (data.getNodeType() != NodeType.GLOBAL_COLUMN){
                JOptionPane.showMessageDialog(null, "You can only drag and drop columns.",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Wherever there is a newline in the incoming data,
            // break it into a separate item in the list.
            //String[] values = data.split("\n");

            addIndex = index;
            //addCount = values.length;

            // Perform the actual import.
            /*for (int i = 0; i < values.length; i++) {
                if (insert) {
                    listModel.add(index++, values[i]);
                } else {
                    // If the items go beyond the end of the current
                    // list, add them in.
                    if (index < listModel.getSize()) {
                        listModel.set(index++, values[i]);
                    } else {
                        listModel.add(index++, values[i]);
                    }
                }
            }*/
            GlobalColumnData column = (GlobalColumnData) data.getObj();
            String lisElem = data.getUserObject().toString();
            if (list.equals(filterList)){
                //user drops in the filter list
                //filter operations depende on data type (<, >, <= only for numeric OR date)
                String[] filterOps;
                if (column.isNumeric()) //TODO: also accept date time datatypes
                    filterOps = numberOperations;
                else
                    filterOps = stringOperations;
                JComboBox filter = new JComboBox(filterOps);
                JTextField value = new JTextField();
                final JComponent[] inputs = new JComponent[] {
                        new JLabel("Select Filter Operation"),
                        filter,
                        new JLabel("Filter value selection"),
                        value,
                };
                int result = JOptionPane.showConfirmDialog(
                        null,
                         inputs,
                        "Filter operation and value selection",
                        JOptionPane.PLAIN_MESSAGE);
                String filterValue = "";
                if (result == JOptionPane.OK_OPTION) {
                    filterValue = value.getText();
                    lisElem+= " = " + filterValue;
                }
                else{
                    JOptionPane.showMessageDialog(null, "Please select a filter operation and insert a filter value with same data type",
                            "Operation Failed", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                listModel.add(index++, lisElem);
            }
            else if (list.equals(columnsList)){
                CustomTreeNode parentNode = (CustomTreeNode) data.getParent();//global table
                addColumnsToColumnsList(listModel, (GlobalColumnData)data.getObj(), (GlobalTableData) parentNode.getObj()) ;
            }
            else if (list.equals(aggregationList)){
                lisElem+= " ("+ aggregationOpComboBox.getSelectedItem().toString() +")";
                listModel.add(index++, lisElem);
            }
            return true;
        }

        private void addColumnsToColumnsList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
            String[] s = null;
            //check if table name of this column exists. If true then inserted here
            if (columnListModel.contains(globalTable.getTableName())){
                int index = columnListModel.indexOf(globalTable.getTableName());
                index++;
                //iterate the columns of this tables. insert a new one
                for (int i = index; i < columnListModel.getSize(); i++) {
                    if (!String.valueOf(columnListModel.getElementAt(i)).contains("    ")){
                        listModel.add(i, "    "+globalCol.getName());//add column
                        globalTableQueries.addSelectColumn(globalTable, globalCol);
                        return;
                    }
                }
                //maybe this table is the last one, insert at last position
                listModel.addElement("    "+globalCol.getName());//add column
                return;
            }
            else{
                listModel.addElement(globalTable.getTableName()); //add table name
                listModel.addElement("    "+globalCol.getName());//add column
                globalTableQueries.addSelectColumn(globalTable, globalCol);
            }
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
}
