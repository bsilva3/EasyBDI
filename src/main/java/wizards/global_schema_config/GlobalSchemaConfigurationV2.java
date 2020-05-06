package wizards.global_schema_config;

import helper_classes.*;
import prestoComm.Constants;
import prestoComm.DBModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalSchemaConfigurationV2 extends JFrame {
    // extends AbstractWizardPage
    private JTree globalSchemaTree;
    private JTree localSchemaTree;
    private JTextField searchGlobalField;
    private JButton searchGlobalButton;
    private JButton searchLocalButton;
    private JLabel globalTableLable;
    private JTextField globalTableName;
    private JButton addGlobalTableButton;
    private JLabel globalTableColumn;
    private JTextField globalColumnName;
    private JComboBox dataTypeBox;
    private JButton addColumnToSelectedButton;
    private JPanel mainPanel;
    private JLabel globalSchemaLabel;
    private JLabel helpLabel;
    private JLabel stepLabel;
    private static final String[] DATATYPES = {"varchar", "char", "integer", "tiny int", "big int", "small int", "double", "decimal"};
    private CustomTreeNode selectedNode;
    private DefaultTreeModel globalSchemaModel;
    private DefaultTreeModel localSchemaModel;


    public GlobalSchemaConfigurationV2(List<DBData> dbs, List<GlobalTableData> globalTables){
        dataTypeBox.setModel(new DefaultComboBoxModel<String>(DATATYPES));
        helpLabel.setText("<html>Verify the proposed schema matching and Global Schema and make the necessary adjustments. "
                +"<br/> You can drag and drop columns or tables from the local schema to the global schema to add new items or to create correlations. " +
                "<br/> You can also add elements to the global schema by right clicking or by using the form on the bottom.</html>");
        stepLabel.setText("Step 2/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));

        //global schema tree set up
        globalSchemaTree.setEditable(true);
        globalSchemaTree.addMouseListener(getMouseListener());
        //globalSchemaModel = setExampleData();
        globalSchemaModel = setGlobalSchemaTree(globalTables);
        globalSchemaTree.setModel(globalSchemaModel);
        globalSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        globalSchemaTree.setDropMode(DropMode.ON_OR_INSERT); //todo: review dropmode: https://docs.oracle.com/javase/7/docs/api/javax/swing/DropMode.html
        globalSchemaTree.setTransferHandler( this.new TreeTransferHandler() );

        //local schema tree set up
        localSchemaTree.setTransferHandler(this.new TreeTransferHandler());
        localSchemaTree.setEditable(false);
        //localSchemaModel = setExampleDataForLocalSchema();
        localSchemaModel = setLocalSchemaTree(dbs);
        localSchemaTree.setModel(localSchemaModel);
        localSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        localSchemaTree.setRootVisible(false);

        //set button icons
        try {
            Image img = ImageIO.read(new File(Constants.IMAGES_DIR+"add_table_icon.png"));
            addGlobalTableButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            img = ImageIO.read(new File(Constants.IMAGES_DIR+"add_column_icon.png"));
            addColumnToSelectedButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            img = ImageIO.read(new File(Constants.IMAGES_DIR+"search_icon.png"));
            searchGlobalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            searchLocalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        //add(mainPanel); //g-wizard
        setVisible(true);
    }

    /*public void createUIComponents(){
        globalSchemaTree = new DndTree();
        localSchemaTree = new DndTree();
    }*/

     //for g-wizard
    /*@Override
    protected AbstractWizardPage getNextPage() {
        return new DatabaseConnectionWizardV2();
    }

    @Override
    protected boolean isCancelAllowed() {
        return true;
    }

    @Override
    protected boolean isPreviousAllowed() {
        return true;
    }

    @Override
    protected boolean isNextAllowed() {
        return true;
    }

    @Override
    protected boolean isFinishAllowed() {
        return false;
    }*/


    public static void main(String[] args){
        //wizards.global_schema_config.GlobalSchemaConfigurationV2 window = new GlobalSchemaConfigurationV2();
    }

    private DefaultTreeModel setExampleData(){
        java.util.List<GlobalTableData> globalTableDataList = new ArrayList<>();
        GlobalTableData g1 = new GlobalTableData("employees");
        //GlobalTableData g2 = new GlobalTableData("inventory");
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.PostgreSQL, "parisDB");
        //DBData dbData3 = new DBData("http://192.168.23.5", DBModel.MongoDB, "inventory");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        //TableData table4 = new TableData("products", "schema", dbData3, 4);
        Set<ColumnData> colsA = new HashSet<>();
        Set<ColumnData> colsB = new HashSet<>();
        Set<ColumnData> colsC = new HashSet<>();
        Set<ColumnData> colsD = new HashSet<>();
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

        java.util.List<TableData> tableLocals = new ArrayList<>();
        tableLocals.add(table1);
        tableLocals.add(table2);
        tableLocals.add(table3);
        //tableLocals.add(table4);
        CustomTreeNode data = new CustomTreeNode("Global Tables", NodeType.GLOBAL_TABLES);
        //tables
        for (GlobalTableData gt:globalTableDataList) {
            CustomTreeNode tables = new CustomTreeNode(gt.getTableName(), gt, NodeType.GLOBAL_TABLE);
            //global cols
            for (GlobalColumnData col : gt.getGlobalColumnData()) {
                CustomTreeNode column = new CustomTreeNode(col.getName(), col, NodeType.COLUMN);
                column.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                if (col.isPrimaryKey())
                    column.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                //corrs
                CustomTreeNode corrs = new CustomTreeNode("Matches", NodeType.MATCHES);
                //THIS PART IS NOT RIGHT!!
                for (TableData t : tableLocals) {
                    CustomTreeNode localTableTree = new CustomTreeNode(t.getDB().getDbName()+"."+t.getTableName(), t, NodeType.TABLE_MATCHES);
                    boolean hasMatches = false;
                    for (ColumnData localCol : col.getLocalColumns()) {
                        if (localCol.getTable().equals(t) && col.getLocalColumns().contains(localCol)) {
                            localTableTree.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                            localTableTree.add(new CustomTreeNode("Mapping Type: "+localCol.getMapping(), null, NodeType.COLUMN_MATCHES_TYPE)); //node indicating mapping type
                            hasMatches = true;
                        }
                    }
                    if (hasMatches)
                        corrs.add(localTableTree);
                }
                column.add(corrs);
                tables.add(column);
            }
            data.add(tables);
        }
        globalSchemaModel = new DefaultTreeModel(data);
        return globalSchemaModel;
    }

    private DefaultTreeModel setExampleDataForLocalSchema(){
        java.util.List<DBData> dbs = new ArrayList<>();
        java.util.List<TableData> tables = new ArrayList<>();
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.PostgreSQL, "parisDB");
        DBData dbData3 = new DBData("http://192.168.23.5", DBModel.MongoDB, "inventory");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        TableData table4 = new TableData("products", "schema", dbData3, 4);
        java.util.List<ColumnData> colsForTable1 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable2 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable3 = new ArrayList<>();
        List<ColumnData> colsForTable4 = new ArrayList<>();
        colsForTable1.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("email", "varchar", false).withTable(table1).build());

        colsForTable2.add(new ColumnData.Builder("id", "integer", true).withTable(table2).build());
        colsForTable2.add(new ColumnData.Builder("name", "varchar", false).withTable(table2).build());

        colsForTable3.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table3)
                .withForeignKey("employees_paris.id").build());
        colsForTable3.add(new ColumnData.Builder("phone", "integer", false).withTable(table3).build());
        colsForTable3.add(new ColumnData.Builder("email", "varchar", false).withTable(table3).build());

        colsForTable4.add(new ColumnData.Builder("product_id", "integer", false).withTable(table4).build());
        colsForTable4.add(new ColumnData.Builder("product_name", "varchar", false).withTable(table4).build());
        colsForTable4.add(new ColumnData.Builder("price", "double", false).withTable(table4).build());
        colsForTable4.add(new ColumnData.Builder("UnitsInStock", "integer", false).withTable(table4).build());
        table1.setColumnsList(colsForTable1);
        table2.setColumnsList(colsForTable2);
        table3.setColumnsList(colsForTable3);
        table4.setColumnsList(colsForTable4);
        dbs.add(dbData1);
        dbs.add(dbData2);
        dbs.add(dbData3);

        tables.add(table1);
        tables.add(table2);
        tables.add(table3);
        tables.add(table4);
        CustomTreeNode data = new CustomTreeNode("root");
        for (DBData db : dbs){
            CustomTreeNode dbTree = new CustomTreeNode(db.getDbName(), db, NodeType.DATABASE);
            dbTree.add(new CustomTreeNode(db.getUrl(), NodeType.DATABASE_INFO));
            dbTree.add(new CustomTreeNode(db.getDbModel(), NodeType.DATABASE_INFO));
            for (TableData t : tables){
                if (!t.getDB().equals(db))
                    continue;
                CustomTreeNode tableTree = new CustomTreeNode(t.getTableName(), t, NodeType.TABLE);
                for (ColumnData col : t.getColumnsList()){
                    CustomTreeNode colTree = new CustomTreeNode(col.getName(), col, NodeType.COLUMN);
                    colTree.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                    if (col.isPrimaryKey())
                        colTree.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                    if (col.getForeignKey()!=null && !col.getForeignKey().isEmpty()){
                        colTree.add(new CustomTreeNode("foreign key: "+col.getForeignKey(), NodeType.COLUMN_INFO));
                    }
                    tableTree.add(colTree);
                }
                dbTree.add(tableTree);
            }
            data.add(dbTree);
        }
        return new DefaultTreeModel(data);
    }

    //set local schema in jtree
    public DefaultTreeModel setLocalSchemaTree(List<DBData> dbs){
        CustomTreeNode data = new CustomTreeNode("root");
        for (DBData db : dbs){
            CustomTreeNode dbTree = new CustomTreeNode(db.getDbName(), db, NodeType.DATABASE);
            dbTree.add(new CustomTreeNode(db.getUrl(), NodeType.DATABASE_INFO));
            dbTree.add(new CustomTreeNode(db.getDbModel(), NodeType.DATABASE_INFO));

            for (TableData t : db.getTableList()){
                if (!t.getDB().equals(db))
                    continue;
                CustomTreeNode tableTree = new CustomTreeNode(t.getTableName(), t, NodeType.TABLE);
                for (ColumnData col : t.getColumnsList()){
                    CustomTreeNode colTree = new CustomTreeNode(col.getName(), col, NodeType.COLUMN);
                    colTree.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                    if (col.isPrimaryKey())
                        colTree.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                    if (col.getForeignKey()!=null && !col.getForeignKey().isEmpty()){
                        colTree.add(new CustomTreeNode("foreign key: "+col.getForeignKey(), NodeType.COLUMN_INFO));
                    }
                    tableTree.add(colTree);
                }
                dbTree.add(tableTree);
            }
            data.add(dbTree);
        }
        return new DefaultTreeModel(data);
    }

    //set local schema in jtree
    public DefaultTreeModel setGlobalSchemaTree(List<GlobalTableData> globalTables){
        CustomTreeNode data = new CustomTreeNode("Global Tables", NodeType.GLOBAL_TABLES);
        //tables
        for (GlobalTableData gt:globalTables) {
            CustomTreeNode tables = new CustomTreeNode(gt.getTableName(), gt, NodeType.GLOBAL_TABLE);
            //global cols
            for (GlobalColumnData col : gt.getGlobalColumnData()) {
                CustomTreeNode column = new CustomTreeNode(col.getName(), col, NodeType.COLUMN);
                column.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                if (col.isPrimaryKey())
                    column.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                //corrs
                CustomTreeNode corrs = new CustomTreeNode("Matches", NodeType.MATCHES);
                for (TableData t : col.getLocalTables()) {
                    CustomTreeNode localTableTree = new CustomTreeNode(t.getDB().getDbName()+"."+t.getTableName(), t, NodeType.TABLE_MATCHES);
                    boolean hasMatches = false;
                    for (ColumnData localCol : col.getLocalColumns()) {
                        if (localCol.getTable().equals(t) && col.getLocalColumns().contains(localCol)) {
                            localTableTree.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                            localTableTree.add(new CustomTreeNode("Mapping Type: "+localCol.getMapping(), null, NodeType.COLUMN_MATCHES_TYPE));
                            hasMatches = true;
                        }
                    }
                    if (hasMatches)
                        corrs.add(localTableTree);
                }
                column.add(corrs);
                tables.add(column);
            }
            data.add(tables);
        }
        return new DefaultTreeModel(data);
    }

    /** -------pop up menu that appears on right click. It is different if user selects table, column or something else----------**/

    private MouseListener getMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){
                    TreePath pathForLocation = globalSchemaTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    if(pathForLocation != null){
                        selectedNode = (CustomTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE)
                            globalSchemaTree.setComponentPopupMenu(getPopUpMenuForGlobalTable());//show menu with option to create tables
                        else if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLES){
                            globalSchemaTree.setComponentPopupMenu(getPopUpMenuForGlobalTableRoot());
                        }
                        else if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN)
                            globalSchemaTree.setComponentPopupMenu(getPopUpMenuForColumn());
                        else if (selectedNode.getNodeType() == NodeType.PRIMARY_KEY)
                            globalSchemaTree.setComponentPopupMenu(getPopUpMenuForPrimaryKey());
                        else
                            globalSchemaTree.setComponentPopupMenu(getPopUpMenuGeneral());
                    } else{
                        selectedNode = null;
                        globalSchemaTree.setComponentPopupMenu(null);
                    }

                }
                super.mousePressed(arg0);
            }
        };
    }

    //pop up menu that shows up when right clicking on the root node of global tables
    private JPopupMenu getPopUpMenuForGlobalTableRoot() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem item1 = new JMenuItem("Add Global Table");
        item1.addActionListener(getAddGlobalTableActionListener());
        menu.add(item1);

        return menu;
    }

    //pop up menu that shows up when right clicking on a global table
    private JPopupMenu getPopUpMenuForGlobalTable() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Edit name");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        JMenuItem item2 = new JMenuItem("Add Global Column");
        item2.addActionListener(getAddGlobalColumnActionListener());
        menu.add(item2);

        JMenuItem item3 = new JMenuItem("Delete Table");
        item3.addActionListener(getRemoveActionListener());
        menu.add(item3);

        return menu;
    }

    //pop up menu that shows up when right clicking a column
    private JPopupMenu getPopUpMenuForColumn() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Edit");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        JMenuItem item2 = new JMenuItem("Add Primary Key");
        item2.addActionListener(getAddPrimaryKeyActionListener());
        menu.add(item2);

        JMenuItem item3 = new JMenuItem("Delete column");
        item3.addActionListener(getRemoveActionListener());
        menu.add(item3);
        if (selectedNode != null)
            System.out.println(selectedNode.getUserObject());

        return menu;
    }

    //pop up menu that shows up when right clicking a column
    private JPopupMenu getPopUpMenuForPrimaryKey() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Remove Primary Key constraint");
        item1.addActionListener(getRemoveActionListener());
        menu.add(item1);
        if (selectedNode != null)
            System.out.println(selectedNode.getUserObject());

        return menu;
    }

    //pop up menu that shows up when right clicking
    private JPopupMenu getPopUpMenuGeneral() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Edit name");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        /*JMenuItem item2 = new JMenuItem("Add");
        item2.addActionListener(getAddActionListener());
        menu.add(item2);*/

        JMenuItem item3 = new JMenuItem("Delete");
        item3.addActionListener(getRemoveActionListener());
        menu.add(item3);
        if (selectedNode != null)
            System.out.println(selectedNode.getUserObject());

        return menu;
    }


    private ActionListener getAddGlobalColumnActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CustomTreeNode selNode = (CustomTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();
                if(selNode != null){
                    /*System.out.println("pressed " + selectedNode);
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("added");
                    selectedNode.add(n);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();*/
                    CustomTreeNode newNode = null;
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE) {
                        newNode = new CustomTreeNode("New Column", null, NodeType.GLOBAL_COLUMN);
                        CustomTreeNode datatype = new CustomTreeNode("varchar", null, NodeType.COLUMN_INFO);
                        CustomTreeNode matches = new CustomTreeNode("Matches", null, NodeType.MATCHES);
                        newNode.add(datatype);
                        newNode.add(matches);
                    }
                    globalSchemaModel.insertNodeInto(newNode, selNode, selNode.getChildCount());
                    TreeNode[] nodes = globalSchemaModel.getPathToRoot(newNode);
                    TreePath path = new TreePath(nodes);
                    globalSchemaTree.scrollPathToVisible(path);
                    globalSchemaTree.setSelectionPath(path);
                    globalSchemaTree.startEditingAtPath(path);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();
                }
            }
        };
    }

    private ActionListener getAddGlobalTableActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                /*CustomTreeNode selNode = (CustomTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();*/
                if(selectedNode != null){
                    /*System.out.println("pressed " + selectedNode);
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("added");
                    selectedNode.add(n);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();*/
                    String defaultTextInNode = "New Node";
                    CustomTreeNode newNode = null;
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLES)
                        newNode = new CustomTreeNode("New Table", null, NodeType.GLOBAL_TABLE);

                    globalSchemaModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
                    TreeNode[] nodes = globalSchemaModel.getPathToRoot(newNode);
                    System.out.println(Arrays.asList(nodes));
                    TreePath path = new TreePath(nodes);
                    globalSchemaTree.scrollPathToVisible(path);
                    globalSchemaTree.setSelectionPath(path);
                    globalSchemaTree.startEditingAtPath(path);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();
                }
            }
        };
    }

    //when user click on column, he can add a primary key constraint
    private ActionListener getAddPrimaryKeyActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CustomTreeNode selNode = (CustomTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();
                if(selNode != null){
                    /*System.out.println("pressed " + selectedNode);
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("added");
                    selectedNode.add(n);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();*/
                    CustomTreeNode newNode = null;
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE) {
                        newNode = new CustomTreeNode("primary key", null, NodeType.PRIMARY_KEY);
                    }
                    globalSchemaModel.insertNodeInto(newNode, selNode, selNode.getChildCount());
                    TreeNode[] nodes = globalSchemaModel.getPathToRoot(newNode);
                    TreePath path = new TreePath(nodes);
                    globalSchemaTree.scrollPathToVisible(path);
                    globalSchemaTree.setSelectionPath(path);
                    globalSchemaTree.startEditingAtPath(path);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();
                }
            }
        };
    }

    private ActionListener getEditActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(selectedNode != null){
                    //edit here
                    TreePath path = new TreePath(selectedNode.getPath());
                    globalSchemaTree.scrollPathToVisible(path);
                    globalSchemaTree.setSelectionPath(path);
                    globalSchemaTree.startEditingAtPath(path);
                    //globalSchemaTree.repaint();
                    //globalSchemaTree.updateUI();
                }
            }
        };
    }

    private ActionListener getRemoveActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(selectedNode != null){
                    //remove node and its children
                    globalSchemaModel.removeNodeFromParent(selectedNode);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();
                }
            }
        };
    }

    // --------- custom transfer handler to move tree nodes
    //adapted from: https://coderanch.com/t/346509/java/JTree-drag-drop-tree-Java
    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];

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

        private boolean haveCompleteNode(JTree tree) {
            int[] selRows = tree.getSelectionRows();
            TreePath path = tree.getPathForRow(selRows[0]);
            CustomTreeNode first =
                    (CustomTreeNode)path.getLastPathComponent();
            int childCount = first.getChildCount();
            // first has children and no children are selected.
            if(childCount > 0 && selRows.length == 1)
                return false;
            // first may have children.
            for(int i = 1; i < selRows.length; i++) {
                path = tree.getPathForRow(selRows[i]);
                CustomTreeNode next =
                        (CustomTreeNode)path.getLastPathComponent();
                if(first.isNodeChild(next)) {
                    // Found a child of first.
                    if(childCount > selRows.length-1) {
                        // Not all children of first are selected.
                        return false;
                    }
                }
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

        public boolean importData(TransferHandler.TransferSupport support) {
            if(!canImport(support)) {
                return false;
            }
            // Extract transfer data.
            CustomTreeNode node = null;
            try {
                Transferable t = support.getTransferable();
                node = (CustomTreeNode)t.getTransferData(nodesFlavor);
            } catch(UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch(java.io.IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("node level: " +node.getLevel());
            System.out.println("child count: "+localSchemaModel.getChildCount(node));
            // Get drop location info.
            JTree.DropLocation dl =
                    (JTree.DropLocation)support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            CustomTreeNode parent =
                    (CustomTreeNode)dest.getLastPathComponent();
            System.out.println("droping in: "+parent.getNodeType());

            //determine if user can drop in this location. If possible, rearrange the node accordingly (if needed)
            if (node.getNodeType() == NodeType.TABLE && parent.getNodeType() == NodeType.GLOBAL_TABLES){
                //users creates a global table from a local table. Add a node with correspondences and add this local table as match (matches in each column)
                int nChilds = node.getChildCount();
                for (int i = 0; i < nChilds; i++){
                    CustomTreeNode col = (CustomTreeNode)node.getChildAt(i);
                    System.out.println(col.getUserObject().toString());
                    CustomTreeNode matchNode = new CustomTreeNode("Matches", NodeType.MATCHES);
                    TableData table = (TableData)node.getObj();
                    CustomTreeNode tableMatch = new CustomTreeNode(table.getDB().getDbName()+"."+table.getTableName(), table, NodeType.TABLE_MATCHES);
                    ColumnData column = (ColumnData) col.getObj();
                    CustomTreeNode colMatch = new CustomTreeNode(column.getName(), column, NodeType.COLUMN_MATCHES);
                    tableMatch.add(colMatch);
                    matchNode.add(tableMatch);
                    col.add(matchNode);
                }
            }
            else if (node.getNodeType() == NodeType.COLUMN && parent.getNodeType() == NodeType.MATCHES){

            }
            JTree tree = (JTree)support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            // Configure for drop mode.
            int index = childIndex;    // DropMode.INSERT
            if(childIndex == -1) {     // DropMode.ON
                index = parent.getChildCount();
            }
            model.insertNodeInto(node, parent, index);
            return true;
        }

        public void canDrop(NodeType draggedNodeType, NodeType dropNodeType){
            if (draggedNodeType == NodeType.TABLE && dropNodeType == NodeType.GLOBAL_TABLES){

            }
            else if (draggedNodeType == NodeType.COLUMN && dropNodeType == NodeType.MATCHES){

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