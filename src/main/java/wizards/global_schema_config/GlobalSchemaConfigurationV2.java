package wizards.global_schema_config;

import helper_classes.*;
import prestoComm.Constants;
import prestoComm.DBModel;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

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
import java.util.*;
import java.util.List;

public class GlobalSchemaConfigurationV2 extends JPanel {
    private JTree globalSchemaTree;
    private JTree localSchemaTree;
    private JTextField searchGlobalField;
    private JButton searchGlobalButton;
    private JButton searchLocalButton;
    private JButton addGlobalTableButton;
    private JButton addColumnToSelectedButton;
    private JPanel mainPanel;
    private JLabel globalSchemaLabel;
    private JLabel helpLabel;
    private JLabel stepLabel;
    private JTextField localTableSearchField;
    private JButton resetFilterLocalSchemaBtn;
    private JButton resetFilterGlobalSchemaBtn;
    private static final String[] DATATYPES = {"varchar", "char", "integer", "tiny int", "big int", "small int", "double", "decimal"};
    private CustomTreeNode selectedNode;
    private DefaultTreeModel globalSchemaModel;
    private DefaultTreeModel localSchemaModel;


    public GlobalSchemaConfigurationV2(List<DBData> dbs, List<GlobalTableData> globalTables){
        helpLabel.setText("<html>Verify the proposed schema matching and Global Schema and make the necessary adjustments. "
                +"<br/> You can drag and drop columns or tables from the local schema to the global schema to add new items or to create correlations. " +
                "<br/> You can also add elements to the global schema by right clicking or by using the form on the bottom.</html>");
        stepLabel.setText("Step 2/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));

        //global schema tree set up
        globalSchemaTree.setEditable(true);
        globalSchemaTree.addMouseListener(getMouseListener());
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
            Image img = ImageIO.read(new File(Constants.IMAGES_DIR+"search_icon.png"));
            searchGlobalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            searchLocalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        resetFilterLocalSchemaBtn.setVisible(false);
        resetFilterGlobalSchemaBtn.setVisible(false);
        //search button listeners
        searchLocalButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //get text written and search
                String searchStr = localTableSearchField.getText().trim();
                searchAndSetFilter(searchStr, localSchemaModel, true);
            }
        });

        searchGlobalButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //get text written and search
                String searchStr = searchGlobalField.getText().trim();
                searchAndSetFilter(searchStr, globalSchemaModel, false);
            }
        });

        resetFilterLocalSchemaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //reset filters
                localSchemaTree.setModel(localSchemaModel);
                localSchemaTree.repaint();
                resetFilterLocalSchemaBtn.setVisible(false);
            }
        });

        resetFilterGlobalSchemaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //reset filters
                globalSchemaTree.setModel(globalSchemaModel);
                globalSchemaTree.repaint();
                resetFilterGlobalSchemaBtn.setVisible(false);
            }
        });

        /*this.setContentPane(mainPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();*/
        add(mainPanel); //g-wizard
        this.setVisible(true);
    }

    public GlobalSchemaConfigurationV2(){
        //generate local schema and global schema (for testing only)
        this(GlobalSchemaConfigurationV2.generateLocalSchema(), GlobalSchemaConfigurationV2.generateGlobalSchema());
    }


     //for g-wizard
    /*@Override
    protected AbstractWizardPage getNextPage() {
        return new CubeConfiguration(this.getGlobalSchemaFromTree());
        //return null;
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

    public static List<DBData> generateLocalSchema(){
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
        colsForTable4.add(new ColumnData.Builder("unitsInStock", "integer", false).withTable(table4).build());
        table1.setColumnsList(colsForTable1);
        table2.setColumnsList(colsForTable2);
        table3.setColumnsList(colsForTable3);
        table4.setColumnsList(colsForTable4);
        dbData1.addTable(table1);
        dbData2.addTable(table3);
        dbData2.addTable(table3);
        dbData3.addTable(table4);
        dbs.add(dbData1);
        dbs.add(dbData2);
        dbs.add(dbData3);
        return dbs;
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
    /*public static void main(String[] args){
        wizards.global_schema_config.GlobalSchemaConfigurationV2 window = new GlobalSchemaConfigurationV2(GlobalSchemaConfigurationV2.generateLocalSchema(),
                GlobalSchemaConfigurationV2.generateGlobalSchema());
    }*/

    //search filter
    public void searchAndSetFilter(String searchStr, DefaultTreeModel model, boolean isLocalSchema){
        if (searchStr == null || searchStr.length() < 2)
            JOptionPane.showMessageDialog(null,
                    "Your search query must have at least 2 characters",
                    "Inane warning",
                    JOptionPane.WARNING_MESSAGE);
        else{
            JTree tree = null;
            if (isLocalSchema)
                tree = localSchemaTree;
            else
                tree = globalSchemaTree;
            CustomTreeNode currentRoot = (CustomTreeNode)tree.getModel().getRoot();
            Enumeration<TreePath> en = currentRoot != null ?
                    tree.getExpandedDescendants(new TreePath(currentRoot.getPath())) : null;
            List<TreePath> pl = en != null ? Collections.list(en) : null;
            CustomTreeNode resultNodes = createFilteredTree((CustomTreeNode)model.getRoot(), searchStr);
            tree.setModel(new DefaultTreeModel(resultNodes));
                    /*else {
                        localSchemaTree.setModel(localSchemaModel);
                    }*/
            if (en != null) {
                CustomTreeNode r = (CustomTreeNode)tree.getModel().getRoot();
                if (r != null)
                    restoreExpandedState(r, pl, tree);
                if (isLocalSchema)
                    resetFilterLocalSchemaBtn.setVisible(true);
                else
                    resetFilterGlobalSchemaBtn.setVisible(true);
            }
            tree.repaint();
        }
    }

    //adapted from: https://gist.github.com/steos/1334152/032b3af14a8f25f46c3cca959d84330574594574
    public CustomTreeNode createFilteredTree(CustomTreeNode parent, String filter) {
        int c = parent.getChildCount();
        CustomTreeNode fparent = new CustomTreeNode(parent.getUserObject(), parent.getObj(), parent.getNodeType());
        boolean matches = (parent.getUserObject().toString()).contains(filter);
        for (int i = 0; i < c; ++i) {
            CustomTreeNode childNode = (CustomTreeNode)parent.getChildAt(i);
            //only search tables, columns and databases
            if (childNode.getNodeType() == NodeType.COLUMN || childNode.getNodeType() == NodeType.TABLE || childNode.getNodeType() == NodeType.DATABASE
                    || childNode.getNodeType() == NodeType.GLOBAL_COLUMN || childNode.getNodeType() == NodeType.GLOBAL_TABLE
                    || childNode.getNodeType() == NodeType.COLUMN_MATCHES){
                CustomTreeNode f = createFilteredTree(childNode, filter);
                if (f != null) {
                    fparent.add(f);
                    matches = true;
                }
            }
            else{
                fparent.add(new CustomTreeNode(childNode.getUserObject(), childNode.getObj(), childNode.getNodeType()));
            }
        }
        return matches ? fparent : null;
    }


    public void restoreExpandedState(CustomTreeNode base, List<TreePath> exps, JTree tree) {
        if (base == null) {
            throw new NullPointerException();
        }
        if (wasExpanded(base, exps)) {
            tree.expandPath(new TreePath(base.getPath()));
        }
        int c = base.getChildCount();
        for (int i = 0; i < c; ++i) {
            CustomTreeNode n = (CustomTreeNode)base.getChildAt(i);
            restoreExpandedState(n, exps, tree);
        }
    }

    public boolean wasExpanded(CustomTreeNode n, List<TreePath> en) {
        if (n == null) {
            throw new NullPointerException();
        }
        for (TreePath path : en) {
            for (Object o : path.getPath()) {
                if (((CustomTreeNode)o).getUserObject() == n.getUserObject()) {
                    return true;
                }
            }
        }
        return false;
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

    //set local schema in jtree
    public DefaultTreeModel setLocalSchemaTree(List<DBData> dbs){
        CustomTreeNode data = new CustomTreeNode("root");
        for (DBData db : dbs){
            CustomTreeNode dbTree = new CustomTreeNode(db.getDbName(), db, NodeType.DATABASE);
            dbTree.add(new CustomTreeNode(db.getUrl(), NodeType.DATABASE_URL));
            dbTree.add(new CustomTreeNode(db.getDbModel(), db.getDbModel(), NodeType.DATABASE_MODEL));

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
                        colTree.add(new CustomTreeNode("foreign key: "+col.getForeignKey(), NodeType.FOREIGN_KEY));
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
                CustomTreeNode column = new CustomTreeNode(col.getName(), col, NodeType.GLOBAL_COLUMN);
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
                if (SwingUtilities.isRightMouseButton(arg0)){
                    TreePath pathForLocation = globalSchemaTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    globalSchemaTree.setSelectionPath(pathForLocation);
                    JPopupMenu menu = null;
                    if(pathForLocation != null){
                        selectedNode = (CustomTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE)
                            menu = getPopUpMenuForGlobalTable();
                        else if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLES){
                            menu = getPopUpMenuForGlobalTableRoot();
                        }
                        else if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN)
                            menu = getPopUpMenuForColumn();
                        else if (selectedNode.getNodeType() == NodeType.PRIMARY_KEY)
                            menu = getPopUpMenuForPrimaryKey();
                        else
                            menu = getPopUpMenuGeneral();
                        if (menu!= null)
                            menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
                    } else{
                        //selectedNode = null;
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

        /*JMenuItem item2 = new JMenuItem("Add Global Column");
        item2.addActionListener(getAddGlobalColumnActionListener());
        menu.add(item2);*/

        JMenuItem item2 = new JMenuItem("Delete Table");
        item2.addActionListener(getRemoveActionListener());
        menu.add(item2);

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

    //get global schema
    public List<GlobalTableData> getGlobalSchemaFromTree(){
        List<GlobalTableData> globalTables = new ArrayList<>();
        CustomTreeNode globalTablesRoot = (CustomTreeNode) globalSchemaModel.getRoot();
        int nChilds = globalTablesRoot.getChildCount();
        for (int i = 0; i < nChilds; i++){
            //for each global table, create a new object
            CustomTreeNode globalTableNode = (CustomTreeNode)globalTablesRoot.getChildAt(i);
            GlobalTableData globalTable = (GlobalTableData) globalTableNode.getObj();
            List<GlobalColumnData> cols = new ArrayList<>();
            //get its global columns
            for (int j = 0; j < globalTableNode.getChildCount(); j++){
                CustomTreeNode globalColumnNode = (CustomTreeNode)globalTableNode.getChildAt(j);
                GlobalColumnData globalCol = (GlobalColumnData) globalColumnNode.getObj();
                globalCol.setPrimaryKey(false);//if it is primary key, will be updated
                CustomTreeNode dataTypeNode = (CustomTreeNode)globalColumnNode.getChildAt(0);
                globalCol.setDataType(dataTypeNode.getUserObject().toString());
                //get primary key (if it is) info and matches list
                for (int k = 1; k < globalColumnNode.getChildCount(); k++){
                    CustomTreeNode node = (CustomTreeNode)globalColumnNode.getChildAt(k);
                    if (node.getNodeType() == NodeType.PRIMARY_KEY)
                        globalCol.setPrimaryKey(true);
                    else if (node.getNodeType() == NodeType.MATCHES){
                        //set matches list
                        Set<ColumnData> matches = new HashSet<>();
                        for (int c = 1; c < node.getChildCount(); c++){
                            //node with <db.table>
                            CustomTreeNode dbTableNode = (CustomTreeNode)node.getChildAt(c);
                            for (int z = 0; z < dbTableNode.getChildCount(); z++){
                                CustomTreeNode columnMatch = (CustomTreeNode)dbTableNode.getChildAt(z);

                                if (columnMatch.getNodeType() == NodeType.COLUMN_MATCHES_TYPE)
                                    continue;
                                //column
                                matches.add((ColumnData)columnMatch.getObj());
                            }

                        }
                        globalCol.setLocalColumns(matches);
                    }
                }
                cols.add(globalCol);
            }
            globalTable.setGlobalColumnData(cols);
            globalTables.add(globalTable);
        }
        return globalTables;
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
                //Drop a table in the list of global tables and make that table become a global table with the matches to that local table
                //users creates a global table from a local table. Add a node with correspondences and add this local table as match (matches in each column)
                int nChilds = node.getChildCount();
                TableData table = (TableData)node.getObj();
                GlobalTableData gloTab = new GlobalTableData(table.getTableName());
                CustomTreeNode newNode = new CustomTreeNode(table.getTableName(), gloTab, NodeType.GLOBAL_TABLE);
                for (int i = 0; i < nChilds; i++){
                    CustomTreeNode col = (CustomTreeNode)node.getChildAt(i);
                    CustomTreeNode matchNode = new CustomTreeNode("Matches", NodeType.MATCHES);
                    CustomTreeNode tableMatch = new CustomTreeNode(table.getDB().getDbName()+"."+table.getTableName(), null, NodeType.TABLE_MATCHES);
                    ColumnData column = (ColumnData) col.getObj();
                    GlobalColumnData globalCol = new GlobalColumnData(column.getName(), column.getDataType(), column.isPrimaryKey(), column);
                    //new global column node
                    CustomTreeNode globalColNode = new CustomTreeNode(column.getName(), globalCol, NodeType.GLOBAL_COLUMN);
                    CustomTreeNode colMatch = new CustomTreeNode(globalCol.getName(), globalCol, NodeType.COLUMN_MATCHES);
                    tableMatch.add(colMatch);
                    matchNode.add(tableMatch);
                    globalColNode.add(new CustomTreeNode(globalCol.getDataType(), null, NodeType.COLUMN_INFO));//datatype in global column
                    globalColNode.add(matchNode);
                    newNode.add(globalColNode);
                }
                node = newNode;
            }
            else if (node.getNodeType() == NodeType.COLUMN && (parent.getNodeType() == NodeType.MATCHES || parent.getNodeType() == NodeType.GLOBAL_COLUMN)){
                //drop a column in the matches of a global column
                ColumnData localCol = (ColumnData)node.getObj();
                CustomTreeNode dbTableLocalNode = new CustomTreeNode(localCol.getTable().getDB().getDbName()+"."+localCol.getTable().getTableName(), null, NodeType.TABLE_MATCHES);
                dbTableLocalNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                dbTableLocalNode.add(new CustomTreeNode("Mapping Type: "+MappingType.Simple, MappingType.Simple, NodeType.COLUMN_MATCHES_TYPE));

                CustomTreeNode matchesNode = null;
                if (parent.getNodeType() == NodeType.GLOBAL_COLUMN) {
                    //dropped on the global col. search for a matches node
                    int nChilds = parent.getChildCount();
                    for (int i = 0; i < nChilds; i++){
                        CustomTreeNode nodeChild = (CustomTreeNode)parent.getChildAt(i);
                        if (nodeChild.getUserObject().toString().equalsIgnoreCase("Matches")) {
                            matchesNode = nodeChild;
                            break;
                        }
                    }
                    if (matchesNode == null){
                        //create a Matches node
                        matchesNode = new CustomTreeNode("Matches", null, NodeType.MATCHES);
                    }
                    matchesNode.add(dbTableLocalNode);
                    node = matchesNode;
                }
                else{
                    //dropped on matches node
                    //simply add db.tableName node on matches
                    node = dbTableLocalNode;
                }

            }
            else if (node.getNodeType() == NodeType.COLUMN && parent.getNodeType() == NodeType.GLOBAL_TABLE){
                //drop a column in a global table and make it a global column with the matches
                ColumnData localCol = (ColumnData)node.getObj();
                GlobalColumnData globalCol = new GlobalColumnData(localCol);
                CustomTreeNode newNode = new CustomTreeNode(globalCol.getName(), globalCol, NodeType.GLOBAL_COLUMN);
                //add data type node
                newNode.add(new CustomTreeNode(globalCol.getDataType(), null, NodeType.COLUMN_INFO));
                if (globalCol.isPrimaryKey())
                    newNode.add(new CustomTreeNode("Primary Key", null, NodeType.PRIMARY_KEY));
                //add matches node and the local col
                CustomTreeNode matches = new CustomTreeNode("Matches", null, NodeType.MATCHES);
                CustomTreeNode dbTableLocalNode = new CustomTreeNode(localCol.getTable().getDB().getDbName()+"."+localCol.getTable().getTableName(), null, NodeType.TABLE_MATCHES);
                dbTableLocalNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                dbTableLocalNode.add(new CustomTreeNode("Mapping Type: "+MappingType.Simple, MappingType.Simple, NodeType.COLUMN_MATCHES_TYPE));
                matches.add(dbTableLocalNode);
                newNode.add(matches);
                node = newNode;
            }
            else
                return false;
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