import helper_classes.ColumnData;
import helper_classes.GlobalColumnData;
import helper_classes.GlobalTableData;
import helper_classes.TableData;
import prestoComm.DBModel;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;
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
        /*setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();*/
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
        GlobalTableData g = new GlobalTableData("Global Table 1");
        TableData table1 = new TableData("local table 1", "schema", null, 1);
        TableData table2 = new TableData("local table 2", "schema", null, 2);
        Set<ColumnData> colsA = new HashSet<>();
        Set<ColumnData> colsB = new HashSet<>();
        Set<ColumnData> colsC = new HashSet<>();
        colsA.add(new ColumnData("local A1", "integer", true));
        colsA.add(new ColumnData("local A2", "integer", true));
        colsB.add(new ColumnData("local B1", "double", false));
        colsB.add(new ColumnData("local B2", "varchar", false));
        colsC.add(new ColumnData("local C", "integer", false));
        GlobalColumnData globalColA = new GlobalColumnData("A", "integer", true, colsA);
        GlobalColumnData globalColB = new GlobalColumnData("B", "varchar", true, colsB);
        GlobalColumnData globalColC = new GlobalColumnData("C", "integer", true, colsC);
        List<GlobalColumnData> globalCols = new ArrayList<>();
        globalCols.add(globalColA);
        globalCols.add(globalColB);
        globalCols.add(globalColC);
        g.setGlobalColumnData(globalCols);
        DefaultMutableTreeNode data = new DefaultMutableTreeNode("Global Tables");
        //tables
        DefaultMutableTreeNode tables = new DefaultMutableTreeNode(g.getTableName());
        //cols
        DefaultMutableTreeNode cols = new DefaultMutableTreeNode("Columns");
        for (GlobalColumnData col : globalCols){
            DefaultMutableTreeNode column = new DefaultMutableTreeNode(col.getName());
            column.add(new DefaultMutableTreeNode(col.getDataType()));
            column.add(new DefaultMutableTreeNode("primary key: "+col.isPrimaryKey()));
            //corrs
            DefaultMutableTreeNode corrs = new DefaultMutableTreeNode("local columns");
            for (ColumnData localCol : col.getLocalColumns()){
                corrs.add(new DefaultMutableTreeNode(localCol.getName()));
            }
            column.add(corrs);
            cols.add(column);
        }
        tables.add(cols);
        data.add(tables);
        root = new DefaultTreeModel(data);
        return root;
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
