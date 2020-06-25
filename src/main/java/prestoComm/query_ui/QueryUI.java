package prestoComm.query_ui;

import helper_classes.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import prestoComm.DBModel;
import prestoComm.MainMenu;
import prestoComm.MetaDataManager;
import prestoComm.PrestoMediator;
import wizards.global_schema_config.CustomTreeNode;
import wizards.global_schema_config.CustomeTreeCellRenderer;
import wizards.global_schema_config.NodeType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class QueryUI extends JPanel{
    private JTable queryResultsTable;
    private JTree schemaTree;
    private JTree filterTree;
    private JList aggregationList;
    private JComboBox aggregationOpComboBox;
    private JList columnsList;
    private JPanel mainPanel;
    private JComboBox cubeSelectionComboBox;
    private JButton executeQueryButton;
    private JButton backButton;
    private JList measuresList;
    private JTabbedPane tabbedPane1;
    private JList queryLogList;
    private String project;

    private StarSchema starSchema;
    private GlobalTableQuery globalTableQueries;//used to store all queries for each global table, and their columns

    private DefaultTreeModel schemaTreeModel;
    private DefaultTreeModel filterTreeModel;
    private DefaultListModel aggreListModel;
    private DefaultListModel columnListModel;
    private DefaultListModel queryLogModel;
    private DefaultTableModel defaultTableModel;

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;
    private String projectName;
    private final String[] aggregations = { "count", "sum", "average", "min", "max"};
    private final String[] numberOperations = { "=", "!=", ">", "=>", "<", "<="};
    private final String[] stringOperations = { "=", "!=", "like"};
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

            aggreListModel = new DefaultListModel();
            columnListModel = new DefaultListModel();
            queryLogModel = new DefaultListModel();
            filterTree.setModel(filterTreeModel);
            filterTree.setCellRenderer(new FilterNodeCellRenderer());
            filterTree.addMouseListener(getMouseListenerForFilterTree());
            aggregationList.setModel(aggreListModel);
            columnsList.setModel(columnListModel);
            queryLogList.setModel(queryLogModel);

            filterTree.setTransferHandler(new TreeTransferHandler());
            aggregationList.setTransferHandler(new TreeTransferHandler());
            columnsList.setTransferHandler(new TreeTransferHandler());

            //jtable
            this.defaultTableModel = new DefaultTableModel();
            this.queryResultsTable.setModel(defaultTableModel);

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
                    executeQuery();
                }
            });


            cubeSelectionComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    starSchema = metaDataManager.getStarSchema(cubeSelectionComboBox.getSelectedItem().toString());
                    schemaTreeModel = setStarSchemaTree();
                    schemaTree.setModel(schemaTreeModel);
                    schemaTree.revalidate();
                    schemaTree.updateUI();
                }
            });

            //listeners for lists left click to open menus
            columnsList.addMouseListener(getMouseListenerForColumnList());

            queryLogList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 2) {
                        // Double-click on list log item: show full message
                        int index = list.locationToIndex(evt.getPoint());
                        String queryLog = (String) queryLogModel.get(index);
                        JOptionPane optionPane = new NarrowOptionPane();
                        optionPane.setMessage(queryLog);
                        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        JDialog dialog = optionPane.createDialog(null, "Query Log");
                        dialog.setVisible(true);
                    }
                }
            });

            add(mainPanel);
            this.setVisible(true);
        }
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
                GlobalTableData table = getTableInColumnIndex(index);
                boolean success = globalTableQueries.deleteSelectColumnFromTable(table, col);
                if (success){
                    columnListModel.remove(index);
                    if (columnListModel.size()==1){
                        //remove the table name from the list (if only one table was present and all its columns was deleted)
                        columnListModel.remove(0);
                    }
                    else if (index < columnListModel.size() && !columnListModel.get(index).toString().contains("    ")){//all columns of a table have been deleted. If there are multiple tables
                        //index will now be a next table. in that case remove the element index-1 is the table with no columns in the list that must be removed
                        columnListModel.remove(index-1);
                    }

                    columnsList.revalidate();
                }
            }
        };
    }

    private ActionListener getRemoveFilterNodActionListener(FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    int childCount = parent.getChildCount();
                    if (index > 0){
                        //need to
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
        FilterNode root = (FilterNode) this.filterTreeModel.getRoot();
        int nChilds = root.getChildCount();
        if (nChilds <= 0){
            return "";
        }
        String query = " WHERE ";
        for (int i = 0 ; i < nChilds; i++){
            FilterNode filterNode = (FilterNode) root.getChildAt(i);
            query += filterNode.getUserObject().toString() +" ";
        }
        return query;
    }

    public void executeQuery(){
        defaultTableModel.setColumnCount(0);
        defaultTableModel.setRowCount(0);//clear any previous results


        String localQuery = globalTableQueries.getLocalTableQuery();//create query with inner query to get local table data
        //get all filters as a string
        String filterQuery = getFilterQuery();
        if (filterQuery.length() > 0){
            localQuery += filterQuery;//add filters to the query (if there are filters). Filters will be applied to the outer query
        }
        System.out.println(localQuery);
        ///mover pro fim da funçlão:
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
        DateTime time = new DateTime();
        String log = formatter.print(time)+" - "+localQuery;
        queryLogModel.addElement(log);
        ////////////
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

    private MouseListener getMouseListenerForColumnList() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    int index = columnsList.locationToIndex(arg0.getPoint());

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

    private MouseListener getMouseListenerForFilterTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    TreePath pathForLocation = filterTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    filterTree.setSelectionPath(pathForLocation);
                    FilterNode selectedNode = null;
                    if(pathForLocation != null) {
                        selectedNode = (FilterNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == FilterNodeType.CONDITION){
                            //menu for a condition
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem item1 = new JMenuItem("Delete");
                            item1.addActionListener(getRemoveFilterNodActionListener(selectedNode));
                            //item1.addActionListener(getRemoveActionListener());
                            menu.add(item1);
                            filterTree.setComponentPopupMenu(menu);
                        }
                        else if (selectedNode.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            //menu for a boolean op
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem item1 = new JMenuItem("AND");
                            JMenuItem item2 = new JMenuItem("OR");
                            item1.addActionListener(changeBooleanOperation("AND", selectedNode));
                            item2.addActionListener(changeBooleanOperation("OR", selectedNode));
                            menu.add(item1);
                            menu.add(item2);
                            filterTree.setComponentPopupMenu(menu);
                        }
                    }
                }
                super.mousePressed(arg0);
            }
        };
    }

    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];
        private int[] indices = null;
        private int addIndex = -1; //Location where items were added
        private int addCount = 0;  //Number of items added.

        public TreeTransferHandler() {
            nodesFlavor = new DataFlavor(CustomTreeNode.class, "custom node");//TODO!! CustomTreeNode
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
                return false;
            }

            if (data.getNodeType() != NodeType.GLOBAL_COLUMN){
                JOptionPane.showMessageDialog(null, "You can only drag and drop columns.",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            GlobalColumnData column = (GlobalColumnData) data.getObj();
            String lisElem = data.getUserObject().toString();

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
                    addColumnsToColumnsList(listModel, (GlobalColumnData)data.getObj(), (GlobalTableData) parentNode.getObj()) ;
                }
                else if (list.equals(aggregationList)){
                    lisElem+= " ("+ aggregationOpComboBox.getSelectedItem().toString() +")";
                    listModel.add(index++, lisElem);
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
                        filterTreeModel = new DefaultTreeModel(root);
                        filterTree.setModel(filterTreeModel);
                        filterTree.setRootVisible(false);
                        filterTree.revalidate();
                        filterTree.updateUI();
                        return true;
                    }
                    FilterNode root = (FilterNode) filterTreeModel.getRoot();
                    if (root.getChildCount() == 0){//filters added and removed such that jtree is empty
                        while (s == null){
                            s = createFilterStringOperation(column, true);
                        }
                        if (s.length == 0)
                            return false;
                        //no filters added
                        root.add(new FilterNode(s[1], column, FilterNodeType.CONDITION));
                        filterTreeModel = new DefaultTreeModel(root);
                        filterTree.setModel(filterTreeModel);
                        filterTree.setRootVisible(false);
                        filterTree.revalidate();
                        filterTree.updateUI();
                        return true;
                    }
                    /*JTree.DropLocation dl =
                            (JTree.DropLocation)info.getDropLocation();
                    int childIndex = dl.getChildIndex();
                    TreePath dest = dl.getPath();
                    FilterNode parent =
                            (FilterNode)dest.getLastPathComponent();
                    int index = childIndex;    // DropMode.INSERT
                    if(index == -1)
                    if(childIndex == -1) {     // DropMode.ON
                        index = parent.getChildCount();
                    }*/
                    while (s == null){
                        s = createFilterStringOperation(column, false);
                    }
                    if (s.length == 0)
                        return false;
                    filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), root, root.getChildCount());
                    filterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), root, root.getChildCount());
                    return true;
                }
            }
            return false;
        }

        private String[] createFilterStringOperation(GlobalColumnData droppedCol, boolean isFirst){
            String s[] = new String [2];
            String elem = droppedCol.getName();
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
                if (filterValue.length() == 0){
                    JOptionPane.showMessageDialog(null, "Please insert a filter value with same data type",
                            "Operation Failed", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                elem+= filter.getSelectedItem().toString() + filterValue;
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

        private void addColumnsToColumnsList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
            String[] s = null;
            //check if table name of this column exists. If true then inserted here
            ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);
            if (columnListModel.contains(elemtTosearch)){
                int index = columnListModel.indexOf(elemtTosearch);
                index++;
                //iterate the columns of this tables. insert a new one
                for (int i = index; i < columnListModel.getSize(); i++) {
                    if (!String.valueOf(columnListModel.getElementAt(i)).contains("    ")){
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
