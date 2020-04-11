import helper_classes.*;
import prestoComm.Constants;
import prestoComm.DBModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class GlobalSchemaConfiguration extends JFrame {
    private JTree globalSchemaTree;
    private JTree localSchemaTree;
    private JButton addColumnToSelectedButton;
    private JTextField globalTableName;
    private JButton addGlobalTableButton;
    private JTextField globalColumnName;
    private JComboBox dataTypeBox;
    private JPanel mainPanel;
    private JLabel globalSchemaLabel;
    private JLabel globalTableLable;
    private JLabel globalTableColumn;
    private JLabel column;
    private JLabel localSchemaTipLabel;
    private JScrollPane qPane;
    private static final String[] DATATYPES = {"varchar", "char", "integer", "tiny int", "big int", "small int", "double", "decimal"};
    private DefaultMutableTreeNode selectedNode;
    private DefaultTreeModel root;


    public GlobalSchemaConfiguration(){
        dataTypeBox.setModel(new DefaultComboBoxModel<String>(DATATYPES));
        //global schema tree set up
        globalSchemaTree.setEditable(true);
        globalSchemaTree.setComponentPopupMenu(getPopUpMenu());
        globalSchemaTree.addMouseListener(getMouseListener());
        globalSchemaTree.setModel(setExampleData());
        globalSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());

        //local schema tree set up
        localSchemaTree.setEditable(false);
        //globalSchemaTree.setComponentPopupMenu(getPopUpMenu());
        //globalSchemaTree.addMouseListener(getMouseListener());
        localSchemaTree.setModel(setExampleDataForLocalSchema());
        localSchemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        localSchemaTree.setRootVisible(false);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        //add(mainPanel); g-wizard
        setVisible(true);
    }

    /* for g-wizard
    @Override
    protected AbstractWizardPage getNextPage() {
        return null;
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
        return false;
    }

    @Override
    protected boolean isFinishAllowed() {
        return true;
    }*/


    public static void main(String[] args){
        GlobalSchemaConfiguration window = new GlobalSchemaConfiguration();
    }

    private MouseListener getMouseListener() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if(arg0.getButton() == MouseEvent.BUTTON3){
                    TreePath pathForLocation = globalSchemaTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                    if(pathForLocation != null){
                        selectedNode = (DefaultMutableTreeNode) pathForLocation.getLastPathComponent();
                    } else{
                        selectedNode = null;
                    }

                }
                super.mousePressed(arg0);
            }
        };
    }

    //pop up meno that shows up when right clicking
    private JPopupMenu getPopUpMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("edit");
        item.addActionListener(getEditActionListener());
        menu.add(item);

        JMenuItem item2 = new JMenuItem("add");
        item2.addActionListener(getAddActionListener());
        menu.add(item2);

        return menu;
    }

    private DefaultTreeModel setExampleData(){
        List<GlobalTableData> globalTableDataList = new ArrayList<>();
        GlobalTableData g = new GlobalTableData("employees");
        TableData table1 = new TableData("employees_lisbon", "schema", null, 1);
        TableData table2 = new TableData("employees_paris", "schema", null, 2);
        TableData table3 = new TableData("employees_contacts_paris", "schema", null, 2);
        Set<ColumnData> colsA = new HashSet<>();
        Set<ColumnData> colsB = new HashSet<>();
        Set<ColumnData> colsC = new HashSet<>();
        Set<ColumnData> colsD = new HashSet<>();
        colsA.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table1).build());
        colsB.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build());
        colsC.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build());
        colsD.add(new ColumnData.Builder("email", "varchar", false).withTable(table1).build());
        colsA.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table2).build());
        colsB.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table2).build());
        colsA.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table3).build());
        colsC.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table3).build());
        colsD.add(new ColumnData.Builder("email", "varchar", false).withTable(table3).build());

        GlobalColumnData globalColA = new GlobalColumnData("id", "integer", true, colsA);
        GlobalColumnData globalColB = new GlobalColumnData("name", "varchar", true, colsB);
        GlobalColumnData globalColC = new GlobalColumnData("phone_number", "varchar", false, colsC);
        GlobalColumnData globalColD = new GlobalColumnData("email", "varchar", false, colsD);
        List<GlobalColumnData> globalCols = new ArrayList<>();
        globalCols.add(globalColA);
        globalCols.add(globalColB);
        globalCols.add(globalColC);
        globalCols.add(globalColD);

        List<TableData> tableLocals = new ArrayList<>();
        tableLocals.add(table1);
        tableLocals.add(table2);
        tableLocals.add(table3);
        g.setGlobalColumnData(globalCols);
        CustomTreeNode data = new CustomTreeNode(loadImageForGlobalSchemaTree(0),"Global Tables");
        //tables
        CustomTreeNode tables = new CustomTreeNode(loadImageForGlobalSchemaTree(1), g.getTableName());
        //local tables
        CustomTreeNode localTable = new CustomTreeNode(loadImageForGlobalSchemaTree(4),"Matched Colums");
        for (GlobalColumnData col : globalCols){
            CustomTreeNode column = new CustomTreeNode(loadImageForGlobalSchemaTree(2), col.getName());
            column.add(new CustomTreeNode(loadImageForGlobalSchemaTree(-1), col.getDataType()));
            column.add(new CustomTreeNode(loadImageForGlobalSchemaTree(3), "primary key: "+col.isPrimaryKey()));
            //corrs
            CustomTreeNode corrs = new CustomTreeNode(loadImageForGlobalSchemaTree(4),"Matches");
            //THIS PART IS NOT RIGHT!!
            for (TableData t : tableLocals){
                CustomTreeNode localTableTree = new CustomTreeNode(loadImageForGlobalSchemaTree(1),t.getTableName());
                boolean hasMatches = false;
                for (ColumnData localCol : col.getLocalColumns()){
                    if (localCol.getTable().equals(t) && col.getLocalColumns().contains(localCol)) {
                        localTableTree.add(new CustomTreeNode(loadImageForGlobalSchemaTree(2), localCol.getName()));
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
        root = new DefaultTreeModel(data);
        return root;
    }

    private DefaultTreeModel setExampleDataForLocalSchema(){
        List<DBData> dbs = new ArrayList<>();
        List<TableData> tables = new ArrayList<>();
        DBData dbData1 = new DBData("\\http://example.com", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("\\http://example.com", DBModel.MYSQL, "parisDB");
        TableData table1 = new TableData("employees_lisbon", "schema", dbData1, 1);
        TableData table2 = new TableData("employees_paris", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts_paris", "schema", dbData2, 3);
        List<ColumnData> colsForTable1 = new ArrayList<>();
        List<ColumnData> colsForTable2 = new ArrayList<>();
        List<ColumnData> colsForTable3 = new ArrayList<>();
        colsForTable1.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("email", "varchar", false).withTable(table1).build());
        colsForTable2.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table2).build());
        colsForTable2.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table2).build());
        colsForTable3.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table3)
                .withForeignKey("employees_paris.employee_id").build());
        colsForTable3.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table3).build());
        colsForTable3.add(new ColumnData.Builder("email", "varchar", false).withTable(table3).build());
        table1.setColumnsList(colsForTable1);
        table2.setColumnsList(colsForTable2);
        table3.setColumnsList(colsForTable3);
        dbs.add(dbData1);
        dbs.add(dbData2);

        tables.add(table1);
        tables.add(table2);
        tables.add(table3);
        CustomTreeNode data = new CustomTreeNode(loadImageForLocalSchemaTree(0), "root");
        for (DBData db : dbs){
            CustomTreeNode dbTree = new CustomTreeNode(loadImageForLocalSchemaTree(0), db.getDbName());
            dbTree.add(new CustomTreeNode(loadImageForLocalSchemaTree(-1), db.getUrl()));
            dbTree.add(new CustomTreeNode(loadImageForLocalSchemaTree(-1), db.getDbModel()));
            for (TableData t : tables){
                if (!t.getDB().equals(db))
                    continue;
                CustomTreeNode tableTree = new CustomTreeNode(loadImageForLocalSchemaTree(1), t.getTableName());
                for (ColumnData col : t.getColumnsList()){
                    CustomTreeNode colTree = new CustomTreeNode(loadImageForLocalSchemaTree(2), col.getName());
                    colTree.add(new CustomTreeNode(loadImageForLocalSchemaTree(-1), col.getDataType()));
                    if (col.isPrimaryKey())
                        colTree.add(new CustomTreeNode(loadImageForLocalSchemaTree(3), "primary key"));
                    if (col.getForeignKey()!=null && !col.getForeignKey().isEmpty()){
                        colTree.add(new CustomTreeNode(loadImageForLocalSchemaTree(3), "foreign key: "+col.getForeignKey()));
                    }
                    tableTree.add(colTree);
                }
                dbTree.add(tableTree);
            }
            data.add(dbTree);
        }
        root = new DefaultTreeModel(data);
        return root;
    }

    public ImageIcon loadImageForGlobalSchemaTree(int imageNumber){
        String fileName = "";
        switch (imageNumber){
            case -1:
                fileName = "info_icon.png";
                break;
            case 0: //global schema (root)
                fileName = "schema_icon.png";
                break;
            case 1: //global table
                fileName = "table_icon.jpg";
                break;
            case 2: //global column
                fileName = "column_icon.png";
                break;
            case 3: //primary key
                fileName = "primary_key_icon.png";
                break;
            case 4: //matches
                fileName = "match_icon.png";
                break;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(Constants.IMAGES_DIR+fileName));
        } catch (IOException e) {
        }
        return new ImageIcon(img.getScaledInstance(20,20, 5));
    }

    public ImageIcon loadImageForLocalSchemaTree(int imageNumber){
        String fileName = "";
        switch (imageNumber){
            case -1:
                fileName = "info_icon.png";
                break;
            case 0: //database
                fileName = "database_icon.png";
                break;
            case 1: //global table
                fileName = "table_icon.jpg";
                break;
            case 2: //global column
                fileName = "column_icon.png";
                break;
            case 3: //primary key
                fileName = "primary_key_icon.png";
                break;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(Constants.IMAGES_DIR+fileName));
        } catch (IOException e) {
        }
        return new ImageIcon(img.getScaledInstance(20,20, 5));
    }

    private ActionListener getAddActionListener() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                DefaultMutableTreeNode selNode = (DefaultMutableTreeNode) globalSchemaTree
                        .getLastSelectedPathComponent();
                if(selNode != null){
                    /*System.out.println("pressed " + selectedNode);
                    DefaultMutableTreeNode n = new DefaultMutableTreeNode("added");
                    selectedNode.add(n);
                    globalSchemaTree.repaint();
                    globalSchemaTree.updateUI();*/
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("New Node");
                    root.insertNodeInto(newNode, selNode, selNode.getChildCount());
                    TreeNode[] nodes = root.getPathToRoot(newNode);
                    TreePath path = new TreePath(nodes);
                    globalSchemaTree.scrollPathToVisible(path);
                    globalSchemaTree.setSelectionPath(path);
                    globalSchemaTree.startEditingAtPath(path);
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
                    System.out.println("pressed" + selectedNode);
                }
            }
        };
    }
}
