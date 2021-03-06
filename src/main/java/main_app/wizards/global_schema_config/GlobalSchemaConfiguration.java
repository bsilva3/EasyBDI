package main_app.wizards.global_schema_config;

import helper_classes.*;
import helper_classes.utils_other.Constants;
import helper_classes.utils_other.Utils;
import main_app.metadata_storage.MetaDataManager;
import main_app.presto_com.PrestoMediator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static helper_classes.utils_other.Constants.*;

public class GlobalSchemaConfiguration extends JPanel {
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
    private CustomTreeNode selectedNode;
    private DefaultTreeModel globalSchemaModel;
    private DefaultTreeModel localSchemaModel;

    private MetaDataManager metaDataManager;
    private boolean isEdit;
    private Map<GlobalColumnData, String> referencedCols;
    private PrestoMediator prestoMediator;

    public GlobalSchemaConfiguration(MetaDataManager metaDataManager, List<DBData> dbs, List<GlobalTableData> globalTables, PrestoMediator prestoMediator) {
        this.prestoMediator = prestoMediator;
        this.metaDataManager = metaDataManager;
        referencedCols = new HashMap<>();

        helpLabel.setText("<html>Verify the proposed Global Schema and make the necessary adjustments."
                + "<br/> You can drag and drop columns or tables from the Local to the Global schema to add new correspondences or to create new items (global table or global columns). " +
                "<br/> You can edit key constraints by right clicking on a Global Column and select the appropriate option. You can also remove or edit elements in this menu." +
                "<br/> Please note that in order to validate the global schema, no global table must have an 'Undefined' mapping towards the local schema correspondences.<html>");
        stepLabel.setText("Step 3/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));

        //global schema tree set up
        globalSchemaTree.setEditable(true);
        globalSchemaTree.addMouseListener(getMouseListenerForGlobalSchemaTree());
        //globalSchemaTree.addMouseListener(getMouseListener());
        //globalSchemaModel = setExampleData();
        //define distribuituin types for each global table
        for (int i = 0; i < globalTables.size(); i++) {
            GlobalTableData gt = defineDistributionType(globalTables.get(i));//get for each column their distribuition type (all should be the same)
            globalTables.set(i, gt); //update
        }


        globalSchemaModel = setGlobalSchemaTree(globalTables);
        globalSchemaModel.addTreeModelListener(new MyTreeModelListener());
        globalSchemaTree.setModel(globalSchemaModel);
        globalSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        globalSchemaTree.setDropMode(DropMode.ON_OR_INSERT);
        globalSchemaTree.setTransferHandler(this.new TreeTransferHandler());

        //local schema tree set up
        localSchemaTree.setTransferHandler(this.new TreeTransferHandler());
        localSchemaTree.setEditable(false);
        localSchemaTree.addMouseListener(getMouseListenerForLocalSchemaTree());
        //localSchemaModel = setExampleDataForLocalSchema();
        localSchemaModel = setLocalSchemaTree(dbs);
        localSchemaTree.setModel(localSchemaModel);
        localSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        localSchemaTree.setRootVisible(false);

        //set button icons
        try {
            Image img = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(Constants.IMAGES_DIR + "search_icon.png"));
            searchGlobalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            searchLocalButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        resetFilterLocalSchemaBtn.setVisible(false);
        resetFilterGlobalSchemaBtn.setVisible(false);
        //search button listeners
        searchLocalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //get text written and search
                String searchStr = localTableSearchField.getText().trim();
                searchAndSetFilter(searchStr, localSchemaModel, true);
            }
        });

        searchGlobalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

        this.setLayout(new BorderLayout());
        add(mainPanel); //for use inside jpanel
        this.setVisible(true);
    }


    public static List<DBData> generateLocalSchema() {
        List<DBData> dbs = new ArrayList<>();
        List<TableData> tables = new ArrayList<>();
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.PostgreSQL, "parisDB");
        DBData dbData3 = new DBData("http://192.168.23.5", DBModel.MongoDB, "inventory");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        TableData table4 = new TableData("products", "schema", dbData3, 4);
        List<ColumnData> colsForTable1 = new ArrayList<>();
        List<ColumnData> colsForTable2 = new ArrayList<>();
        List<ColumnData> colsForTable3 = new ArrayList<>();
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

    public static List<GlobalTableData> generateGlobalSchema() {
        List<GlobalTableData> globalTableDataList = new ArrayList<>();
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
        List<GlobalColumnData> globalCols = new ArrayList<>();
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
        prestoComm.wizards.global_schema_config.GlobalSchemaConfigurationV2 window = new GlobalSchemaConfigurationV2(GlobalSchemaConfigurationV2.generateLocalSchema(),
                GlobalSchemaConfigurationV2.generateGlobalSchema());
    }*/

    //search filter
    public void searchAndSetFilter(String searchStr, DefaultTreeModel model, boolean isLocalSchema) {
        if (searchStr == null || searchStr.length() < 2)
            JOptionPane.showMessageDialog(null,
                    "Your search query must have at least 2 characters",
                    "Inane warning",
                    JOptionPane.WARNING_MESSAGE);
        else {
            JTree tree = null;
            if (isLocalSchema)
                tree = localSchemaTree;
            else
                tree = globalSchemaTree;
            CustomTreeNode currentRoot = (CustomTreeNode) tree.getModel().getRoot();
            Enumeration<TreePath> en = currentRoot != null ?
                    tree.getExpandedDescendants(new TreePath(currentRoot.getPath())) : null;
            List<TreePath> pl = en != null ? Collections.list(en) : null;
            CustomTreeNode resultNodes = createFilteredTree((CustomTreeNode) model.getRoot(), searchStr);
            tree.setModel(new DefaultTreeModel(resultNodes));
                    /*else {
                        localSchemaTree.setModel(localSchemaModel);
                    }*/
            if (en != null) {
                CustomTreeNode r = (CustomTreeNode) tree.getModel().getRoot();
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
            CustomTreeNode childNode = (CustomTreeNode) parent.getChildAt(i);
            //only search tables, columns and databases
            if (childNode.getNodeType() == NodeType.COLUMN || childNode.getNodeType() == NodeType.TABLE || childNode.getNodeType() == NodeType.DATABASE
                    || childNode.getNodeType() == NodeType.GLOBAL_COLUMN || childNode.getNodeType() == NodeType.GLOBAL_TABLE
                    || childNode.getNodeType() == NodeType.COLUMN_MATCHES) {
                CustomTreeNode f = createFilteredTree(childNode, filter);
                if (f != null) {
                    fparent.add(f);
                    matches = true;
                }
            } else {
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
            CustomTreeNode n = (CustomTreeNode) base.getChildAt(i);
            restoreExpandedState(n, exps, tree);
        }
    }

    public boolean wasExpanded(CustomTreeNode n, List<TreePath> en) {
        if (n == null) {
            throw new NullPointerException();
        }
        for (TreePath path : en) {
            for (Object o : path.getPath()) {
                if (((CustomTreeNode) o).getUserObject() == n.getUserObject()) {
                    return true;
                }
            }
        }
        return false;
    }


    //set local schema in jtree
    public DefaultTreeModel setLocalSchemaTree(List<DBData> dbs) {
        CustomTreeNode data = new CustomTreeNode("root");
        for (DBData db : dbs) {
            CustomTreeNode dbTree = new CustomTreeNode(db.getDbName(), db, NodeType.DATABASE);
            dbTree.add(new CustomTreeNode(db.getUrl(), NodeType.DATABASE_URL));
            dbTree.add(new CustomTreeNode(db.getDbModel(), db.getDbModel(), NodeType.DATABASE_MODEL));

            for (TableData t : db.getTableList()) {
                if (!t.getDB().equals(db))
                    continue;
                CustomTreeNode tableTree = new CustomTreeNode(t.getTableName(), t, NodeType.TABLE);
                for (ColumnData col : t.getColumnsList()) {
                    CustomTreeNode colTree = new CustomTreeNode(col.getName(), col, NodeType.COLUMN);
                    colTree.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                    if (col.isPrimaryKey())
                        colTree.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                    if (col.getForeignKey() != null && !col.getForeignKey().isEmpty()) {
                        colTree.add(new CustomTreeNode("foreign key: " + col.getForeignKey(), NodeType.FOREIGN_KEY));
                    }
                    tableTree.add(colTree);
                }
                dbTree.add(tableTree);
            }
            data.add(dbTree);
        }
        return new DefaultTreeModel(data);
    }

    //set glocal schema in jtree
    public DefaultTreeModel setGlobalSchemaTree(List<GlobalTableData> globalTables) {
        CustomTreeNode data = new CustomTreeNode("Global Tables", NodeType.GLOBAL_TABLES);
        //tables
        for (GlobalTableData gt : globalTables) {
            CustomTreeNode tables = new CustomTreeNode(gt.getTableName(), gt, NodeType.GLOBAL_TABLE);
            //global cols
            for (GlobalColumnData col : gt.getGlobalColumnDataList()) {
                CustomTreeNode column = new CustomTreeNode(col.getName(), col, NodeType.GLOBAL_COLUMN);
                column.add(new CustomTreeNode(col.getDataType(), NodeType.COLUMN_INFO));
                if (col.isPrimaryKey())
                    column.add(new CustomTreeNode("primary key", NodeType.PRIMARY_KEY));
                if (col.hasForeignKey())
                    column.add(new CustomTreeNode("Foreign Key: " + col.getForeignKey(), col.getForeignKey(), NodeType.FOREIGN_KEY));
                //corrs
                CustomTreeNode corrs = new CustomTreeNode("Matches", NodeType.MATCHES);
                for (TableData t : col.getLocalTables()) {
                    CustomTreeNode localTableTree = new CustomTreeNode(t.getDB().getDbName() + "." + t.getTableName(), t, NodeType.TABLE_MATCHES);
                    boolean hasMatches = false;
                    for (ColumnData localCol : col.getLocalColumns()) {
                        if (localCol.getTable().equals(t) && col.getLocalColumns().contains(localCol)) {
                            localTableTree.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                            //localTableTree.add(new CustomTreeNode("Mapping Type: "+localCol.getMapping(), null, NodeType.COLUMN_MATCHES_TYPE));
                            hasMatches = true;
                        }
                    }
                    if (hasMatches)
                        corrs.add(localTableTree);
                }
                column.add(corrs);
                tables.add(column);
            }
            tables.add(new CustomTreeNode("Mapping Type: " + gt.getMappingType(), gt.getMappingType(), NodeType.COLUMN_MATCHES_TYPE));
            data.add(tables);
        }
        return new DefaultTreeModel(data);
    }

    /**
     * -------pop up menu that appears on right click. It is different if user selects table, column or something else----------
     **/

    private MouseListener getMouseListenerForLocalSchemaTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)) {
                    TreePath pathForLocation = localSchemaTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    localSchemaTree.setSelectionPath(pathForLocation);
                    JPopupMenu menu = null;
                    if (pathForLocation != null) {
                        selectedNode = (CustomTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == NodeType.TABLE)
                            menu = getPopUpMenuForLocalTable();
                        if (menu != null)
                            menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
                    } else {
                        //selectedNode = null;
                        localSchemaTree.setComponentPopupMenu(null);
                    }
                }
                super.mousePressed(arg0);
            }
        };
    }

    //pop up menu that shows up when right clicking on the root node of global tables
    private JPopupMenu getPopUpMenuForLocalTable() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem item1 = new JMenuItem("Create View for table using SQL...");
        item1.addActionListener(getCreateSQLCodeValidateInsert());
        menu.add(item1);

        return menu;
    }

    private MouseListener getMouseListenerForGlobalSchemaTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)) {
                    TreePath pathForLocation = globalSchemaTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    globalSchemaTree.setSelectionPath(pathForLocation);
                    JPopupMenu menu = null;
                    if (pathForLocation != null) {
                        selectedNode = (CustomTreeNode) pathForLocation.getLastPathComponent();
                        if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE)
                            menu = getPopUpMenuForGlobalTable();
                        else if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLES) {
                            menu = getPopUpMenuForGlobalTableRoot();
                        } else if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN)
                            menu = getPopUpMenuForColumn();
                        else if (selectedNode.getNodeType() == NodeType.COLUMN_MATCHES_TYPE)
                            menu = getPopUpMenuForMapping();
                        else if (selectedNode.getNodeType() == NodeType.PRIMARY_KEY)
                            menu = getPopUpMenuForPrimaryKey();
                        else if (selectedNode.getNodeType() == NodeType.FOREIGN_KEY)
                            menu = getPopUpMenuForForeignKey();
                        else
                            menu = getPopUpMenuGeneral();
                        if (menu != null)
                            menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
                    } else {
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

    //pop up menu that shows up when right clicking a mapping type node
    private JPopupMenu getPopUpMenuForMapping() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Edit Name");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        JMenu item1 = new JMenu("Change datatype");
        //fill menus with types of data types, then fill in each datatype
        //numeric
        JMenu subnumeric = new JMenu(NUMERIC_DATATYPE);
        for (String datatype : Constants.NUMERIC_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(NUMERIC_DATATYPE, datatype));
            subnumeric.add(i);
        }
        item1.add(subnumeric);

        //string
        JMenu substring = new JMenu(STRING_DATATYPE);
        for (String datatype : Constants.STRING_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(STRING_DATATYPE, datatype));
            substring.add(i);
        }
        item1.add(substring);

        //time
        JMenu subtime = new JMenu(TIME_DATATYPE);
        for (String datatype : Constants.TIME_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(TIME_DATATYPE, datatype));
            subtime.add(i);
        }
        item1.add(subtime);

        //time
        JMenuItem subboolean = new JMenuItem(BOOLEAN_DATATYPE);
        subboolean.addActionListener(changeDataType(BOOLEAN_DATATYPE, BOOLEAN_DATATYPE));
        item1.add(subboolean);

        //item1.addActionListener(getEditActionListener());
        menu.add(item1);

        JMenuItem item2 = new JMenuItem("Add Primary Key");
        item2.addActionListener(getAddPrimaryKeyActionListener());
        menu.add(item2);

        JMenuItem item3 = new JMenuItem("Add Foreign Key");
        item3.addActionListener(addForeignKeyActionListener());
        menu.add(item3);

        JMenuItem item4 = new JMenuItem("Delete column");
        item4.addActionListener(getRemoveActionListener());
        menu.add(item4);

        return menu;
    }

    //pop up menu that shows up when right clicking a column
    private JPopupMenu getPopUpMenuForColumn() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("Edit Name");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        JMenu item1 = new JMenu("Change datatype");
        //fill menus with types of data types, then fill in each datatype
        //numeric
        JMenu subnumeric = new JMenu(NUMERIC_DATATYPE);
        for (String datatype : Constants.NUMERIC_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(NUMERIC_DATATYPE, datatype));
            subnumeric.add(i);
        }
        item1.add(subnumeric);

        //string
        JMenu substring = new JMenu(STRING_DATATYPE);
        for (String datatype : Constants.STRING_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(STRING_DATATYPE, datatype));
            substring.add(i);
        }
        item1.add(substring);

        //time
        JMenu subtime = new JMenu(TIME_DATATYPE);
        for (String datatype : Constants.TIME_DATATYPES) {
            JMenuItem i = new JMenuItem(datatype);
            i.addActionListener(changeDataType(TIME_DATATYPE, datatype));
            subtime.add(i);
        }
        item1.add(subtime);

        //time
        JMenuItem subboolean = new JMenuItem(BOOLEAN_DATATYPE);
        subboolean.addActionListener(changeDataType(BOOLEAN_DATATYPE, BOOLEAN_DATATYPE));
        item1.add(subboolean);

        //item1.addActionListener(getEditActionListener());
        menu.add(item1);

        JMenuItem item2 = new JMenuItem("Add Primary Key");
        item2.addActionListener(getAddPrimaryKeyActionListener());
        menu.add(item2);

        JMenuItem item3 = new JMenuItem("Add Foreign Key");
        item3.addActionListener(addForeignKeyActionListener());
        menu.add(item3);

        JMenuItem item4 = new JMenuItem("Delete column");
        item4.addActionListener(getRemoveActionListener());
        menu.add(item4);

        return menu;
    }

    private ActionListener changeDataType(String dataTypeCategoryToConvert, String datatype) {
        return arg0 -> {
            if (selectedNode != null) {
                //edit here
                GlobalColumnData col = (GlobalColumnData) selectedNode.getObj();
                String datatypeCategoryOG = col.getOGDataTypeCategory();
                if (datatypeCategoryOG == null)
                    return;
                Map<DatatypePair, String> convertibleDict = loadConvertibleDataTypeFile();
                String canBeConverted = convertibleDict.get(new DatatypePair(datatypeCategoryOG, dataTypeCategoryToConvert));
                if (canBeConverted.equalsIgnoreCase("true")) {
                    col.setDataType(datatype);
                    selectedNode.setObj(col);
                    CustomTreeNode datatypeNode = (CustomTreeNode) selectedNode.getChildAt(0);//datatypeNode
                    datatypeNode.setUserObject(datatype);
                    globalSchemaTree.revalidate();
                    globalSchemaTree.updateUI();
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "A " + datatypeCategoryOG + " datatype cannot be converted to " + dataTypeCategoryToConvert);
                    return;
                }
            }
        };
    }

    private Map<DatatypePair, String> loadConvertibleDataTypeFile() {
        String line = "";
        String csvSplitBy = ",";
        Map<DatatypePair, String> convertipleDataTypes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(DATATYPE_CONVERTIBLES_GROUP_DICT)))) {
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(csvSplitBy);
                convertipleDataTypes.put(new DatatypePair(elements[0], elements[1]), elements[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertipleDataTypes;
    }

    //pop up menu that shows up when right clicking a column
    private JPopupMenu getPopUpMenuForPrimaryKey() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Remove Primary Key constraint");
        item1.addActionListener(getRemoveActionListener());
        menu.add(item1);

        return menu;
    }

    //pop up menu that shows up when right clicking a column
    private JPopupMenu getPopUpMenuForForeignKey() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem("Remove Foreign Key constraint");
        item1.addActionListener(getRemoveActionListener());
        menu.add(item1);

        return menu;
    }

    private JPopupMenu getPopUpMenuForMappingType() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem item1 = new JMenuItem("Change Mapping");
        JMenuItem s = new JMenuItem("Simple");
        JMenuItem h = new JMenuItem("Horizontal");
        JMenuItem v = new JMenuItem("Vertical");
        s.addActionListener(setMappingActionListener(MappingType.Simple));
        h.addActionListener(setMappingActionListener(MappingType.Horizontal));
        v.addActionListener(setMappingActionListener(MappingType.Vertical));
        item1.add(s);
        item1.add(h);
        item1.add(v);
        menu.add(item1);

        return menu;
    }

    private ActionListener setMappingActionListener(MappingType type) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null && selectedNode.getNodeType() == NodeType.COLUMN_MATCHES_TYPE) {
                    selectedNode.setUserObject("Mapping Type: " + type);
                    selectedNode.setObj(type);
                }
            }
        };
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
        //if (selectedNode != null)
        //  System.out.println(selectedNode.getUserObject());

        return menu;
    }

    private ActionListener getCreateSQLCodeValidateInsert() {
        return arg0 -> {
            if (selectedNode != null) {
                CustomTreeNode newNode = null;
                new ViewCreator(prestoMediator, this, selectedNode.getUserObject().toString());
            }
        };
    }


    public void setViewInfo(List<String[]> colsInView, String SQLcode) {
        if (colsInView == null)
            return;
        JOptionPane.showMessageDialog(this, "View Created! The new table view can be seen on the local schema tree.", "View created", JOptionPane.PLAIN_MESSAGE);
        TableData t = (TableData) selectedNode.getObj();
        TableData view = new TableData(t.getTableName() + " (View)", t.getSchemaName(), t.getDB());
        view.setSqlCode(SQLcode);
        List<ColumnData> cols = new ArrayList<>();
        for (String[] attribute : colsInView) {
            ColumnData c = new ColumnData(attribute[0], attribute[1], false);
            c.setTable(view);
            cols.add(c);
        }
        view.setColumnsList(cols);
        //store on SQL
        view = metaDataManager.insertTableData(view);
        view = metaDataManager.insertColumnData(view);
        CustomTreeNode tableNode = new CustomTreeNode(Utils.getFileNameNoExtension(t.getTableName()) + " (view)", view, NodeType.TABLE);
        for (ColumnData c : view.getColumnsList()) {
            CustomTreeNode colTree = new CustomTreeNode(c.getName(), c, NodeType.COLUMN);
            colTree.add(new CustomTreeNode(c.getDataType(), NodeType.COLUMN_INFO));
            tableNode.add(colTree);
        }
        localSchemaModel.insertNodeInto(tableNode, (CustomTreeNode) selectedNode.getParent(), selectedNode.getParent().getChildCount());

    }


    private ActionListener getAddGlobalColumnActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CustomTreeNode selNode = (CustomTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();
                if (selNode != null) {
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
                if (selectedNode != null) {
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
                    //System.out.println(Arrays.asList(nodes));
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
                if (selNode != null) {
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN) {
                        CustomTreeNode newNode = new CustomTreeNode("primary key", null, NodeType.PRIMARY_KEY);
                        globalSchemaModel.insertNodeInto(newNode, selNode, 1);
                        TreeNode[] nodes = globalSchemaModel.getPathToRoot(newNode);
                        TreePath path = new TreePath(nodes);
                        globalSchemaTree.scrollPathToVisible(path);
                        globalSchemaTree.setSelectionPath(path);
                        globalSchemaTree.startEditingAtPath(path);
                        globalSchemaTree.repaint();
                        globalSchemaTree.updateUI();
                    }
                }
            }
        };
    }

    //when user click on column, he can add a foreign key constraint
    private ActionListener addForeignKeyActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CustomTreeNode selNode = (CustomTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();
                if (selNode != null && selNode.getNodeType() == NodeType.GLOBAL_COLUMN) {
                    //get global table
                    CustomTreeNode globalTableNode = (CustomTreeNode) selNode.getParent();
                    GlobalTableData t = (GlobalTableData) globalTableNode.getObj();
                    List<GlobalTableData> tablesWithKeys = getGlobalTablesWithPrimKeys(t);
                    if (tablesWithKeys.isEmpty()) {
                        JOptionPane.showMessageDialog(mainPanel, "No PKs", "There are no primary keys in the global schema\n Please, create primary keys before assigning foreign keys.", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    new ForeignKeySelector(GlobalSchemaConfiguration.this, tablesWithKeys);//Open window to select a primary key to be referenced
                }
            }
        };
    }

    public void addForeignKey(GlobalTableData t, GlobalColumnData referencedCol) {
        if (selectedNode != null) {
            CustomTreeNode newNode = new CustomTreeNode("Foreign Key: " + t.getTableName() + "." + referencedCol.getName(), t.getTableName() + "." + referencedCol.getName(), NodeType.FOREIGN_KEY);
            referencedCols.put((GlobalColumnData) selectedNode.getObj(), t.getTableName() + "." + referencedCol.getName());
            globalSchemaModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount() - 1);
            TreeNode[] nodes = globalSchemaModel.getPathToRoot(newNode);
            TreePath path = new TreePath(nodes);
            globalSchemaTree.scrollPathToVisible(path);
            globalSchemaTree.setSelectionPath(path);
            globalSchemaTree.startEditingAtPath(path);
            globalSchemaTree.repaint();
            globalSchemaTree.updateUI();
        }
    }

    /**
     * Iterate the current global schema tree and returns a list of tables with at least on primary key and their primary key columns. Does not include primary keys of table given as argument
     *
     * @return
     */
    private List<GlobalTableData> getGlobalTablesWithPrimKeys(GlobalTableData t) {
        List<GlobalTableData> tables = new ArrayList<>();
        CustomTreeNode root = (CustomTreeNode) globalSchemaModel.getRoot();
        int nChilds = root.getChildCount();
        for (int i = 0; i < nChilds; i++) {
            //iterate global tables
            CustomTreeNode globalTableNode = (CustomTreeNode) root.getChildAt(i);
            GlobalTableData globalTable = (GlobalTableData) globalTableNode.getObj();
            if (globalTable.equals(t))//do not include primary keys of table given as argument
                continue;
            GlobalTableData g = new GlobalTableData(globalTable.getTableName());
            g.setId(globalTable.getId());
            int nTableChilds = globalTableNode.getChildCount();
            for (int j = 0; j < nTableChilds; j++) {
                CustomTreeNode globalColumn = (CustomTreeNode) globalTableNode.getChildAt(j);
                int nColumnsChilds = globalColumn.getChildCount();
                for (int k = 0; k < nColumnsChilds; k++) {
                    CustomTreeNode colChild = (CustomTreeNode) globalColumn.getChildAt(k);
                    if (colChild.getNodeType() == NodeType.PRIMARY_KEY) {
                        GlobalColumnData c = (GlobalColumnData) globalColumn.getObj();
                        g.addGlobalColumn(c);
                    }
                }
            }
            if (g.getGlobalColumnDataList() != null && g.getGlobalColumnDataList().size() > 0) {//add a table only if there are primary keys
                tables.add(g);
            }
        }
        return tables;
    }

    private ActionListener getEditActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (selectedNode != null) {
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
                if (selectedNode != null) {
                    //check if columns are referenced
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_TABLE) {
                        boolean isReferenced = isColumnsReferenced(selectedNode);
                        if (isReferenced) {
                            JOptionPane.showMessageDialog(mainPanel, "A column is referencing this table as its Foreign Key.\nPlease remove that constraint first.");
                            return;
                        }
                        // check now if there is a global column that is a foreign key and remove it
                        int nChilds = selectedNode.getChildCount();
                        for (int i = 0; i < nChilds; i++) {
                            CustomTreeNode c = (CustomTreeNode) selectedNode.getChildAt(i);
                            deleteForeignKeyReferenceIfExist(c);

                        }
                    } else if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN) {
                        String colName = selectedNode.getUserObject().toString();
                        CustomTreeNode globTabNode = (CustomTreeNode) selectedNode.getParent();
                        String tabName = globTabNode.getUserObject().toString();
                        if (referencedCols.values().contains(tabName + "." + colName)) {
                            JOptionPane.showMessageDialog(mainPanel, "Another column is referencing this column as its Foreign Key.\nPlease remove that constraint first.");
                            return;
                        } else if (referencedCols.containsKey((GlobalColumnData) selectedNode.getObj())) {
                            referencedCols.remove((GlobalColumnData) selectedNode.getObj()); //remove this reference as it is being deleted
                        }
                    } else if (selectedNode.getNodeType() == NodeType.FOREIGN_KEY) {
                        CustomTreeNode columNodeParent = (CustomTreeNode) selectedNode.getParent();
                        try {
                            referencedCols.remove((GlobalColumnData) columNodeParent.getObj());//remove this reference as it is being deleted
                        } catch (Exception e) {
                        }
                    }
                    //remove node and its children
                    //update mapping type

                    globalSchemaModel.nodeChanged(selectedNode);
                    if (selectedNode.getNodeType() == NodeType.GLOBAL_COLUMN) { // if a column is removed, must update the distribuition type
                        //update only on global table edited
                        CustomTreeNode globalTableNode = (CustomTreeNode) selectedNode.getParent();
                        globalSchemaModel.removeNodeFromParent(selectedNode);
                        GlobalTableData globTable = getGlobalTableFromNode(globalTableNode);
                        globTable = defineDistributionType(globTable);
                        selectedNode = updateMappingsOnNodes(globalTableNode, globTable);//update all the global table
                        globalSchemaModel.nodeChanged(selectedNode);
                    } else {
                        globalSchemaModel.removeNodeFromParent(selectedNode); //TODO: This could be improved and updated only the edited table
                        //update all global table's distribuition type (naive approach)
                        updateAllGlobalTablesMapping();
                    }
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();
                }
            }
        };
    }

    private void updateAllGlobalTablesMapping() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) globalSchemaModel.getRoot();
        int nChilds = root.getChildCount();
        for (int i = 0; i < nChilds; i++) {
            CustomTreeNode globalTableNode = (CustomTreeNode) root.getChildAt(i);
            GlobalTableData globTable = getGlobalTableFromNode(globalTableNode);
            globTable = defineDistributionType(globTable);
            globalTableNode = updateMappingsOnNodes(globalTableNode, globTable);//update all the global table
            globalSchemaModel.nodeChanged(globalTableNode);
        }
        globalSchemaTree.revalidate();
        globalSchemaTree.updateUI();
    }

    private boolean isColumnsReferenced(CustomTreeNode globalTableNode) {
        int nChilds = globalTableNode.getChildCount();
        for (int i = 0; i < nChilds; i++) {
            CustomTreeNode childCol = (CustomTreeNode) globalTableNode.getChildAt(i);
            String colName = childCol.getUserObject().toString();
            String tableName = globalTableNode.getUserObject().toString();
            if (referencedCols.values().contains(tableName + "." + colName)) {
                return true;
            }
        }
        return false;
    }

    private void deleteForeignKeyReferenceIfExist(CustomTreeNode globalTableNode) {
        int nChilds = globalTableNode.getChildCount();
        for (int i = 0; i < nChilds; i++) {
            CustomTreeNode childCol = (CustomTreeNode) globalTableNode.getChildAt(i);
            if (childCol.getNodeType() != NodeType.GLOBAL_COLUMN)
                continue;
            GlobalColumnData c = (GlobalColumnData) childCol.getObj();
            if (referencedCols.containsKey(c)) {
                referencedCols.remove(c);
            }
        }
    }

    //get global schema
    public List<GlobalTableData> getGlobalSchemaFromTree() {
        List<GlobalTableData> globalTables = new ArrayList<>();
        CustomTreeNode globalTablesRoot = (CustomTreeNode) globalSchemaModel.getRoot();
        int nChilds = globalTablesRoot.getChildCount();
        for (int i = 0; i < nChilds; i++) {
            //for each global table, create a new object
            CustomTreeNode globalTableNode = (CustomTreeNode) globalTablesRoot.getChildAt(i);
            GlobalTableData globalTable = getGlobalTableFromNode(globalTableNode);
            if (isGlobalTableMappingTypeUndefined(globalTable)) {
                JOptionPane.showMessageDialog(null, "At least one global table contains undefined mappings.\nPlease create " +
                        "valid correspondences such that one mapping type \n(simple, horizontal or vertical) is created.", "Invalid mappings", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            globalTables.add(globalTable);
        }
        return globalTables;
    }

    private boolean isGlobalTableMappingTypeUndefined(GlobalTableData g) {
        MappingType m = g.getGlobalColumnDataList().get(0).getMappingType();//the same for all columns
        if (m == MappingType.Undefined)
            return true;
        return false;
    }

    /**
     * Given a node of global table returns an object with all global columns and their matches updated with the data on the nodes
     *
     * @param globalTableNode
     * @return
     */
    private GlobalTableData getGlobalTableFromNode(CustomTreeNode globalTableNode) {
        if (globalTableNode.getNodeType() != NodeType.GLOBAL_TABLE)
            return null;
        GlobalTableData globalTable = (GlobalTableData) globalTableNode.getObj();
        List<GlobalColumnData> cols = new ArrayList<>();
        try {
            //get its global columns
            for (int j = 0; j < globalTableNode.getChildCount() - 1; j++) {
                CustomTreeNode globalColumnNode = (CustomTreeNode) globalTableNode.getChildAt(j);//All tables have their mappings updated
                GlobalColumnData globalCol = (GlobalColumnData) globalColumnNode.getObj();
                globalCol.setPrimaryKey(false);//if it is primary key, will be updated
                CustomTreeNode dataTypeNode = (CustomTreeNode) globalColumnNode.getChildAt(0);
                globalCol.setDataType(dataTypeNode.getUserObject().toString());
                //get primary key (if it is) info and matches list
                for (int k = 1; k < globalColumnNode.getChildCount(); k++) {//k = 1 because first child is data type
                    CustomTreeNode node = (CustomTreeNode) globalColumnNode.getChildAt(k);
                    if (node.getNodeType() == NodeType.PRIMARY_KEY)
                        globalCol.setPrimaryKey(true);
                    if (node.getNodeType() == NodeType.FOREIGN_KEY)
                        globalCol.setForeignKey(node.getObj().toString());
                    else if (node.getNodeType() == NodeType.MATCHES) {
                        //set matches list
                        Set<ColumnData> matches = new HashSet<>();
                        for (int c = 0; c < node.getChildCount(); c++) {
                            //node with <db.table>
                            CustomTreeNode dbTableNode = (CustomTreeNode) node.getChildAt(c);
                            for (int z = 0; z < dbTableNode.getChildCount(); z++) {
                                //node with local column
                                CustomTreeNode columnMatch = (CustomTreeNode) dbTableNode.getChildAt(z);

                            /*if (columnMatch.getNodeType() == NodeType.COLUMN_MATCHES_TYPE) {
                                continue;
                            }*/
                                //column
                                matches.add((ColumnData) columnMatch.getObj());
                            }

                        }
                        globalCol.setLocalColumns(matches);
                    }
                }
                cols.add(globalCol);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Malformed trees, please make sure all trees are valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        globalTable.setGlobalColumnData(cols);
        CustomTreeNode mappingTypeNode = (CustomTreeNode) globalTableNode.getChildAt(globalTableNode.getChildCount() - 1);
        globalTable.setMappingType((MappingType) mappingTypeNode.getObj());
        return globalTable;
    }

    /**
     * Define if between the type of mapping between the global and local table(s). Local tables can be only one (0), be vertically partioned (1),
     * be horizontally partitioned (2) or...
     *
     * @param globalTable
     * @return
     */
    public GlobalTableData defineDistributionType(GlobalTableData globalTable) {
        //test if its a simple mapping 1 - 1 and only 1 local table
        MappingType mappingType = MappingType.Undefined;
        if (isSimpleMapping(globalTable)) {
            mappingType = MappingType.Simple;
        } else if (isHorizontalMapping(globalTable)) {
            mappingType = MappingType.Horizontal;
        } else if (isVerticalMapping(globalTable)) {
            mappingType = MappingType.Vertical;
        }
        for (GlobalColumnData gc : globalTable.getGlobalColumnDataList()) {
            Set<ColumnData> localCols = gc.getLocalColumns();
            Set<ColumnData> updatedLocalCols = new HashSet<>();
            //for each local table corresponding to this global column, set the mapping, simple mapping in this case
            for (ColumnData c : localCols) {
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
     *
     * @param globalTable
     * @return
     */
    private boolean isSimpleMapping(GlobalTableData globalTable) {
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
     *
     * @param globalTable
     * @return
     */
    private boolean isVerticalMapping(GlobalTableData globalTable) {
        Set<TableData> completeLocalTables = globalTable.getLocalTablesFromCols();
        if (completeLocalTables.size() > 1) {
            ColumnData primKeyOriginalTable = null;
            Set<ColumnData> primCols = new HashSet<>();
            //get the original prim key that other foreign keys prim keys reference
            for (GlobalColumnData c : globalTable.getPrimaryKeyColumns()) {
                primCols.addAll(c.getLocalColumns());
            }
            for (ColumnData c : primCols) {
                if (c.isPrimaryKey() && !c.hasForeignKey()) {
                    primKeyOriginalTable = c;
                    completeLocalTables.remove(c.getTable());
                    break;
                }
            }
            int nFK = 0;
            for (TableData localTable : completeLocalTables) {
                for (ColumnData localColumn : localTable.getColumnsList()) {
                    //check for columns that are both primary and foreign keys
                    if (localColumn.isPrimaryKey() && localColumn.hasForeignKey()) {
                        nFK++;
                        ColumnData referencedCol = localColumn.getForeignKeyColumn(metaDataManager);
                        if (referencedCol != null && !referencedCol.equals(primKeyOriginalTable))
                            return false;
                    }
                }
            }
            if (nFK == 0)
                return false; //no foreign keys between tables, cannot be vertical mapping
            return true;
        }
        return false;
    }

    /**
     * Given  a global table, and the local tables that have correspondences with the global table, check to see if there is a horizontal partioning
     * mapping between them. This means that the global table is constituted by several equal local tables,
     * whose attributes (columns) are the same in all local and global tables.
     *
     * @param globalTable
     * @return
     */
    private boolean isHorizontalMapping(GlobalTableData globalTable) {
        Set<TableData> completeLocalTables = globalTable.getLocalTablesFromCols();
        if (completeLocalTables.size() > 1) {//there are matches for more than one table
            for (TableData localTable : completeLocalTables) {
                //all local and global tables have same nº of columns if horizontal partioning
                if (localTable.getNCols() != globalTable.getNCols())
                    return false;
                for (ColumnData gc : localTable.getColumnsList()) {
                    //if all columns in all local tables are the same as the cols in global table, then there is horizontal partiotioning
                    if (!globalTable.columnExistsOriginalInfo(gc.getName(), gc.getDataTypeNoLimit(), gc.isPrimaryKey()))
                        return false;
                }
            }
            return true;
        }
        return false;
    }

    private CustomTreeNode updateMappingsOnNodes(CustomTreeNode globalTableNode, GlobalTableData globalTable) {
        MappingType m = globalTable.getMappingType();
        globalTableNode.remove(globalTableNode.getChildCount() - 1);//remove last node (mapping type)
        globalTableNode.add(new CustomTreeNode("Mapping Type: " + m, m, NodeType.COLUMN_MATCHES_TYPE));
        return globalTableNode;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setMinimumSize(new Dimension(850, 750));
        mainPanel.setPreferredSize(new Dimension(850, 710));
        final JScrollPane scrollPane1 = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.5;
        gbc.weighty = 1.4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 10);
        mainPanel.add(scrollPane1, gbc);
        globalSchemaTree = new JTree();
        globalSchemaTree.setAlignmentX(0.5f);
        globalSchemaTree.setAlignmentY(1.0f);
        globalSchemaTree.setInheritsPopupMenu(false);
        scrollPane1.setViewportView(globalSchemaTree);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setVerticalScrollBarPolicy(20);
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 2.0;
        gbc.weighty = 1.4;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane2, gbc);
        localSchemaTree = new JTree();
        localSchemaTree.setAlignmentX(0.5f);
        localSchemaTree.setAlignmentY(1.0f);
        localSchemaTree.setAutoscrolls(false);
        localSchemaTree.setDragEnabled(true);
        localSchemaTree.setEditable(false);
        localSchemaTree.setLargeModel(false);
        localSchemaTree.setRequestFocusEnabled(true);
        localSchemaTree.setVisible(true);
        localSchemaTree.setVisibleRowCount(20);
        scrollPane2.setViewportView(localSchemaTree);
        final JLabel label1 = new JLabel();
        label1.setHorizontalTextPosition(4);
        label1.setText("Search");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(label1, gbc);
        searchGlobalField = new JTextField();
        searchGlobalField.setAlignmentX(0.5f);
        searchGlobalField.setPreferredSize(new Dimension(100, 20));
        searchGlobalField.setText("");
        searchGlobalField.setToolTipText("Global Table Name");
        searchGlobalField.putClientProperty("caretWidth", new Integer(1));
        searchGlobalField.putClientProperty("html.disable", Boolean.FALSE);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.5;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(searchGlobalField, gbc);
        searchGlobalButton = new JButton();
        searchGlobalButton.setPreferredSize(new Dimension(78, 25));
        searchGlobalButton.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(searchGlobalButton, gbc);
        final JLabel label2 = new JLabel();
        label2.setHorizontalTextPosition(4);
        label2.setText("Search");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(label2, gbc);
        localTableSearchField = new JTextField();
        localTableSearchField.setHorizontalAlignment(10);
        localTableSearchField.setPreferredSize(new Dimension(100, 20));
        localTableSearchField.setText("");
        localTableSearchField.setToolTipText("Global Table Name");
        localTableSearchField.putClientProperty("caretWidth", new Integer(1));
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 3;
        gbc.weightx = 1.5;
        gbc.weighty = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(localTableSearchField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Local Schema");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        mainPanel.add(label3, gbc);
        helpLabel = new JLabel();
        helpLabel.setAlignmentX(0.5f);
        helpLabel.setHorizontalAlignment(0);
        helpLabel.setHorizontalTextPosition(0);
        helpLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(1, 0, 0, 0);
        mainPanel.add(helpLabel, gbc);
        stepLabel = new JLabel();
        stepLabel.setAlignmentX(0.5f);
        stepLabel.setHorizontalAlignment(0);
        stepLabel.setHorizontalTextPosition(0);
        stepLabel.setText("label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 9;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        mainPanel.add(stepLabel, gbc);
        globalSchemaLabel = new JLabel();
        globalSchemaLabel.setText("Global Schema Configuration");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        mainPanel.add(globalSchemaLabel, gbc);
        resetFilterLocalSchemaBtn = new JButton();
        resetFilterLocalSchemaBtn.setPreferredSize(new Dimension(78, 25));
        resetFilterLocalSchemaBtn.setRequestFocusEnabled(true);
        resetFilterLocalSchemaBtn.setRolloverEnabled(true);
        resetFilterLocalSchemaBtn.setText("Reset Search");
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(resetFilterLocalSchemaBtn, gbc);
        searchLocalButton = new JButton();
        searchLocalButton.setHorizontalAlignment(0);
        searchLocalButton.setHorizontalTextPosition(0);
        searchLocalButton.setPreferredSize(new Dimension(78, 25));
        searchLocalButton.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(searchLocalButton, gbc);
        resetFilterGlobalSchemaBtn = new JButton();
        resetFilterGlobalSchemaBtn.setPreferredSize(new Dimension(78, 25));
        resetFilterGlobalSchemaBtn.setText("Reset Search");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 0.1;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(resetFilterGlobalSchemaBtn, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    /*private CustomTreeNode updateMappingsOnNodes(CustomTreeNode globalTableNode, GlobalTableData globalTable){
        int nChilds = globalTableNode.getChildCount();
        for (int i = 0; i < nChilds; i++){
            CustomTreeNode globalColNode = (CustomTreeNode) globalTableNode.getChildAt(i);
            GlobalColumnData globalCol = (GlobalColumnData) globalColNode.getObj();
            int nColsChilds = globalColNode.getChildCount();
            globalCol = globalTable.getGlobalColumnData(globalCol.getName());
            CustomTreeNode matchesNode = (CustomTreeNode) globalColNode.getChildAt(nColsChilds-1);//get the Matches node
            int nMatchesChilds = matchesNode.getChildCount();
            for (int j = 0; j < nMatchesChilds; j++){
                CustomTreeNode matchDBTable = (CustomTreeNode) matchesNode.getChildAt(j);
                int nDBTableChilds = matchDBTable.getChildCount();
                CustomTreeNode matchDBTableChild = (CustomTreeNode) matchDBTable.getChildAt(nDBTableChilds-1);
                MappingType mapping = globalCol.getMappingType();
                //get the mapping type node (remove if exist) and add a new mapping node with updated mapping
                if (matchDBTableChild.getNodeType() == NodeType.COLUMN_MATCHES_TYPE){
                    matchDBTable.remove(nDBTableChilds-1);
                }
                matchDBTable.add(new CustomTreeNode( mapping, mapping, NodeType.COLUMN_MATCHES_TYPE));
            }
        }
        return globalTableNode;
    }*/

    // --------- custom transfer handler to move tree nodes
    //adapted from: https://coderanch.com/t/346509/java/JTree-drag-drop-tree-Java
    class TreeTransferHandler extends TransferHandler {
        DataFlavor nodesFlavor;
        DataFlavor[] flavors = new DataFlavor[1];

        public TreeTransferHandler() {
            nodesFlavor = new DataFlavor(CustomTreeNode.class, "custom node");
            flavors[0] = nodesFlavor;
        }

        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }

            support.setShowDropLocation(true);
            if (!support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            return true;
        }

        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath[] paths = tree.getSelectionPaths();
            if (paths != null) {
                // exportDone after a successful drop.
                CustomTreeNode node =
                        (CustomTreeNode) paths[0].getLastPathComponent();
                return new NodesTransferable(node);
            }
            return null;
        }

        /**
         * Defensive copy used in createTransferable.
         */
        private CustomTreeNode copy(TreeNode node) {
            return new CustomTreeNode(node);
        }

        protected void exportDone(JComponent source, Transferable data, int action) {
            if ((action & MOVE) == MOVE) {
                JTree tree = (JTree) source;
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            }
        }

        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            // Extract transfer data.
            CustomTreeNode node = null;
            try {
                Transferable t = support.getTransferable();
                node = (CustomTreeNode) t.getTransferData(nodesFlavor);
            } catch (UnsupportedFlavorException ufe) {
                System.out.println("UnsupportedFlavor: " + ufe.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            //System.out.println("node level: " +node.getLevel());
            //System.out.println("child count: "+localSchemaModel.getChildCount(node));
            // Get drop location info.
            JTree.DropLocation dl =
                    (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath dest = dl.getPath();
            CustomTreeNode parent =
                    (CustomTreeNode) dest.getLastPathComponent();

            //System.out.println("dropping in: "+parent.getNodeType());
            CustomTreeNode globalTableNode = null;
            boolean updateMappingsInTable = false;

            JTree tree = (JTree) support.getComponent();
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            boolean isGlobColInsert = false;
            //determine if user can drop in this location. If possible, rearrange the node accordingly (if needed)
            if (node.getNodeType() == NodeType.TABLE && parent.getNodeType() == NodeType.GLOBAL_TABLES) {
                //Drop a table in the list of global tables and make that table become a global table with the matches to that local table
                //users creates a global table from a local table. Add a node with correspondences and add this local table as match (matches in each column)
                int nChilds = node.getChildCount();
                TableData table = (TableData) node.getObj();
                GlobalTableData gloTab = new GlobalTableData(table.getTableName());
                CustomTreeNode newNode = new CustomTreeNode(table.getTableName(), gloTab, NodeType.GLOBAL_TABLE);

                for (int i = 0; i < nChilds; i++) {
                    CustomTreeNode col = (CustomTreeNode) node.getChildAt(i);
                    CustomTreeNode matchNode = new CustomTreeNode("Matches", NodeType.MATCHES);
                    CustomTreeNode tableMatch = new CustomTreeNode(table.getDB().getDbName() + "." + table.getTableName(), null, NodeType.TABLE_MATCHES);
                    ColumnData column = (ColumnData) col.getObj();
                    GlobalColumnData globalCol = new GlobalColumnData(column.getName(), column.getDataType(), column.isPrimaryKey(), column);
                    //new global column node
                    CustomTreeNode globalColNode = new CustomTreeNode(column.getName(), globalCol, NodeType.GLOBAL_COLUMN);
                    CustomTreeNode colMatch = new CustomTreeNode(globalCol.getName(), column, NodeType.COLUMN_MATCHES);
                    //CustomTreeNode mapType = new CustomTreeNode(MappingType.Simple, MappingType.Simple, NodeType.COLUMN_MATCHES_TYPE);//by default, when creating a new global table by draggin a local table, the mapping is simple
                    tableMatch.add(colMatch);
                    //tableMatch.add(mapType);
                    matchNode.add(tableMatch);
                    globalColNode.add(new CustomTreeNode(globalCol.getDataType(), null, NodeType.COLUMN_INFO));//datatype in global column
                    globalColNode.add(matchNode);
                    newNode.add(globalColNode);
                }
                newNode.add(new CustomTreeNode(MappingType.Simple, MappingType.Simple, NodeType.COLUMN_MATCHES_TYPE));
                node = newNode;
            } else if (node.getNodeType() == NodeType.COLUMN && (parent.getNodeType() == NodeType.MATCHES || parent.getNodeType() == NodeType.GLOBAL_COLUMN)) {
                //drop a local column in the matches of a global column OR drop a local column in a the Matches node
                ColumnData localCol = (ColumnData) node.getObj();
                //check matches node childs and look for dbtable name nodes
                CustomTreeNode dbTableLocalNode = new CustomTreeNode(localCol.getTable().getDB().getDbName() + "." + localCol.getTable().getTableName(), null, NodeType.TABLE_MATCHES);
                dbTableLocalNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                //dbTableLocalNode.add(new CustomTreeNode("Mapping Type: "+MappingType.Simple, MappingType.Simple, NodeType.COLUMN_MATCHES_TYPE));

                CustomTreeNode matchesNode = null;
                if (parent.getNodeType() == NodeType.GLOBAL_COLUMN) {
                    globalTableNode = (CustomTreeNode) parent.getParent();
                    //dropped on the global col. search for a matches node
                    int nChilds = parent.getChildCount();
                    boolean dbTableExists = false;
                    for (int i = 0; i < nChilds; i++) {
                        CustomTreeNode nodeChild = (CustomTreeNode) parent.getChildAt(i);
                        if (nodeChild.getUserObject().toString().equalsIgnoreCase("Matches")) {
                            matchesNode = nodeChild;
                            int nChildsmatches = nodeChild.getChildCount();
                            for (int j = 0; j < nChildsmatches; j++) {
                                CustomTreeNode dbTableNode = (CustomTreeNode) nodeChild.getChildAt(j);
                                if (dbTableNode.getUserObject().toString().equals(localCol.getTable().getDB().getDbName() + "." + localCol.getTable().getTableName())) {
                                    dbTableLocalNode = dbTableNode;
                                    CustomTreeNode mappType = (CustomTreeNode) dbTableNode.getChildAt(dbTableNode.getChildCount() - 1);
                                    dbTableLocalNode.remove(dbTableNode.getChildCount() - 1);
                                    dbTableLocalNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                                    dbTableLocalNode.add(mappType);//redundant.. its going to be updated..
                                    matchesNode.insert(dbTableLocalNode, j);
                                    dbTableExists = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (matchesNode == null) {
                        //create a Matches node
                        matchesNode = new CustomTreeNode("Matches", null, NodeType.MATCHES);
                    }
                    if (!dbTableExists)
                        matchesNode.add(dbTableLocalNode);
                    node = matchesNode;
                    CustomTreeNode n = (CustomTreeNode) parent.getChildAt(parent.getChildCount() - 1);
                    if (n.getNodeType() == NodeType.MATCHES)
                        parent.remove(parent.getChildCount() - 1);
                } else {
                    //dropped on matches node
                    int nchilds = parent.getChildCount();
                    boolean bdTableexists = false;
                    //see if db.table exists already
                    for (int i = 0; i < nchilds; i++) {
                        CustomTreeNode dbTableNode = (CustomTreeNode) parent.getChildAt(i);
                        //exists add there the  col match
                        if (dbTableNode.getUserObject().toString().equals(localCol.getTable().getDB().getDbName() + "." + localCol.getTable().getTableName())) {
                            //parent.remove(i);
                            //CustomTreeNode mappType = (CustomTreeNode) dbTableNode.getChildAt(dbTableNode.getChildCount()-1);
                            /*if (mappType.getNodeType() == NodeType.COLUMN_MATCHES_TYPE){
                                dbTableNode.remove(dbTableNode.getChildCount()-1);
                                dbTableNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                                dbTableNode.add(mappType);//redundant.. its going to be updated..
                            }
                            else*/
                            dbTableNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                            globalSchemaModel.nodeChanged(dbTableNode);
                            //parent.insert(dbTableNode, i);
                            bdTableexists = true;
                            break;
                        }
                    }
                    //simply add db.tableName node on matches
                    globalTableNode = (CustomTreeNode) parent.getParent().getParent();
                    if (!bdTableexists) {
                        parent.add(dbTableLocalNode);
                    }
                    node = parent;
                    parent = (CustomTreeNode) parent.getParent();
                    CustomTreeNode n = (CustomTreeNode) parent.getChildAt(parent.getChildCount() - 1);
                    if (n.getNodeType() == NodeType.MATCHES)
                        parent.remove(parent.getChildCount() - 1);//remove matches node because its going to be updated
                }
                updateMappingsInTable = true;
            } else if (node.getNodeType() == NodeType.COLUMN && parent.getNodeType() == NodeType.GLOBAL_TABLE) {
                //drop a local column in a global table and make it a global column with the matches
                ColumnData localCol = (ColumnData) node.getObj();
                if (localCol.hasForeignKey()) {
                    ColumnData referencedCol = localCol.getForeignKeyColumn(metaDataManager);
                    if (referencedCol != null) {
                        boolean referenceColExists = referencedColumnExists(referencedCol);
                        //if referenced column does not belong to match in any global tables, then do not allow
                        if (!referenceColExists) {
                            JOptionPane.showMessageDialog(null, "The column you tried to drag contains a foreign key that \nreferences a column not matched" +
                                    " in any global column. This operation is not possible.", "Cannot add column", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                }
                GlobalColumnData globalCol = new GlobalColumnData(localCol);
                CustomTreeNode newNode = new CustomTreeNode(globalCol.getName(), globalCol, NodeType.GLOBAL_COLUMN);
                //add data type node
                newNode.add(new CustomTreeNode(globalCol.getDataType(), null, NodeType.COLUMN_INFO));
                if (globalCol.isPrimaryKey())
                    newNode.add(new CustomTreeNode("Primary Key", null, NodeType.PRIMARY_KEY));
                if (globalCol.hasForeignKey()) {
                    //TODO: check if table exists first!?
                    newNode.add(new CustomTreeNode("foreign key: " + globalCol.getForeignKey(), null, NodeType.FOREIGN_KEY));
                }
                //add matches node and the local col
                CustomTreeNode matches = new CustomTreeNode("Matches", null, NodeType.MATCHES);
                CustomTreeNode dbTableLocalNode = new CustomTreeNode(localCol.getTable().getDB().getDbName() + "." + localCol.getTable().getTableName(), null, NodeType.TABLE_MATCHES);
                dbTableLocalNode.add(new CustomTreeNode(localCol.getName(), localCol, NodeType.COLUMN_MATCHES));
                //dbTableLocalNode.add(new CustomTreeNode("Mapping Type: "+MappingType.Undefined, MappingType.Undefined, NodeType.COLUMN_MATCHES_TYPE));
                matches.add(dbTableLocalNode);
                newNode.add(matches);
                node = newNode;
                updateMappingsInTable = true;
                isGlobColInsert = true;
            } else
                return false;
            // Configure for drop mode.
            int index = childIndex;    // DropMode.INSERT
            if (childIndex == -1) {     // DropMode.ON
                index = parent.getChildCount();
            }
            if (isGlobColInsert)
                index = index - 1; //if inserting column in global table, insert before last position. Last position is for the mapping type
            model.insertNodeInto(node, parent, index);
            if (updateMappingsInTable) {
                //update mapping type
                /*GlobalTableData globTable = getGlobalTableFromNode(globalTableNode);
                globTable = defineDistributionType(globTable);
                node = updateMappingsOnNodes(globalTableNode, globTable);//update all the global table
                CustomTreeNode root = (CustomTreeNode) globalSchemaModel.getRoot();
                //int ind = root.getIndex(globalTableNode);
                //root.remove(ind);
                //model.insertNodeInto(node, root, ind);
                model.nodeChanged(node);*/
                updateAllGlobalTablesMapping();
            }

            globalSchemaTree.repaint();
            globalSchemaTree.updateUI();
            return true;
        }

        private boolean referencedColumnExists(ColumnData col) {
            CustomTreeNode root = (CustomTreeNode) globalSchemaModel.getRoot();
            int nChilds = root.getChildCount();
            for (int i = 0; i < nChilds; i++) {
                //iterate global tables
                CustomTreeNode globalTable = (CustomTreeNode) root.getChildAt(i);
                int nTableChilds = globalTable.getChildCount();
                for (int j = 0; j < nTableChilds; j++) {
                    CustomTreeNode globalColumn = (CustomTreeNode) globalTable.getChildAt(i);
                    int nColumnsChilds = globalColumn.getChildCount();
                    for (int k = 0; k < nColumnsChilds; k++) {
                        CustomTreeNode columnChild = (CustomTreeNode) globalColumn.getChildAt(i);
                        if (columnChild.getNodeType() == NodeType.MATCHES) {
                            int childrenNumber = columnChild.getChildCount();
                            for (int z = 0; z < childrenNumber; z++) {
                                CustomTreeNode tableMatch = (CustomTreeNode) columnChild.getChildAt(i);
                                int nTableMatchesChild = tableMatch.getChildCount();
                                for (int c = 0; c < nTableMatchesChild; c++) {
                                    CustomTreeNode columnMatch = (CustomTreeNode) tableMatch.getChildAt(i);
                                    if (columnMatch.getObj().equals(col)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        /*private CustomTreeNode updateMappingsOnNodes(CustomTreeNode globalTableNode, GlobalTableData globalTable){
            CustomTreeNode newGlobalTableNode = new CustomTreeNode(globalTableNode.getUserObject(), globalTableNode.getObj(), globalTableNode.getNodeType());
            int nChilds = globalTableNode.getChildCount();
            for (int i = 0; i < nChilds; i++){
                CustomTreeNode globalColNode = (CustomTreeNode) globalTableNode.getChildAt(i);
                GlobalColumnData globalCol = (GlobalColumnData) globalColNode.getObj();
                int nColsChilds = globalColNode.getChildCount();
                CustomTreeNode newColumnNode = (CustomTreeNode) globalColNode;
                globalCol = globalTable.getGlobalColumnData(globalCol.getName());
                CustomTreeNode matchesNode = (CustomTreeNode) globalColNode.getChildAt(nColsChilds-1);//get the Matches node
                newColumnNode.remove(nColsChilds-1);//remove matches node because an updated version will be added
                CustomTreeNode newMatchesNode = new CustomTreeNode(matchesNode.getUserObject(), matchesNode.getObj(), matchesNode.getNodeType());
                int nMatchesChilds = matchesNode.getChildCount();
                for (int j = 0; j < nMatchesChilds; j++){
                    CustomTreeNode matchDBTable = (CustomTreeNode) matchesNode.getChildAt(j);
                    int nDBTableChilds = matchDBTable.getChildCount();
                    CustomTreeNode matchDBTableChild = (CustomTreeNode) matchDBTable.getChildAt(nDBTableChilds-1);
                    MappingType mapping = globalCol.getMappingType();
                    //get the mapping type node (remove if exist) and add a new mapping node with updated mapping
                    if (matchDBTableChild.getNodeType() == NodeType.COLUMN_MATCHES_TYPE){
                        matchDBTable.remove(nDBTableChilds-1);
                    }
                    matchDBTable.add(new CustomTreeNode( mapping, mapping, NodeType.COLUMN_MATCHES_TYPE));
                    newMatchesNode.add(matchDBTable);
                    newColumnNode.add(newMatchesNode);
                }
                newGlobalTableNode.add(newColumnNode);
            }
            return newGlobalTableNode;
        }*/

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
                if (!isDataFlavorSupported(flavor))
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

    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            CustomTreeNode node;
            node = (CustomTreeNode)
                    (e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we have already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */
            try {
                int index = e.getChildIndices()[0];
                node = (CustomTreeNode)
                        (node.getChildAt(index));
            } catch (NullPointerException exc) {
            }
            if (node.getNodeType() == NodeType.GLOBAL_TABLE) {
                GlobalTableData t = (GlobalTableData) node.getObj();
                t.setTableName(node.getUserObject().toString());
                node.setObj(t);
            } else if (node.getNodeType() == NodeType.GLOBAL_COLUMN) {
                GlobalColumnData c = (GlobalColumnData) node.getObj();
                c.setName(node.getUserObject().toString());
                node.setObj(c);
            }

        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }
    }
}