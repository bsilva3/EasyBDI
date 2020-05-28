package prestoComm;

import helper_classes.*;
import wizards.global_schema_config.CustomTreeNode;
import wizards.global_schema_config.CustomeTreeCellRenderer;
import wizards.global_schema_config.GlobalSchemaConfigurationV2;
import wizards.global_schema_config.NodeType;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
import java.sql.ResultSet;
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
    private String project;

    private DefaultTreeModel schemaTreeModel;
    private DefaultListModel fliterListModel;
    private DefaultListModel aggreListModel;
    private DefaultListModel columnListModel;

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;
    private String projectName;


    public QueryUI(String projectName){
        this.projectName = projectName;
        this.metaDataManager = new MetaDataManager(projectName);
        this.prestoMediator = new PrestoMediator();

        String[] aggregations = { "sum", "average"};
        aggregationOpComboBox.setModel(new DefaultComboBoxModel(aggregations));

        schemaTreeModel = setGlobalSchemaTree(generateGlobalSchema());
        schemaTree.setModel(schemaTreeModel);
        schemaTree.setCellRenderer(new CustomeTreeCellRenderer());
        schemaTree.setTransferHandler(new TreeTransferHandler());
        schemaTree.setDragEnabled(true);

        fliterListModel = new DefaultListModel();
        aggreListModel = new DefaultListModel();
        columnListModel = new DefaultListModel();
        filterList.setModel(fliterListModel);
        aggregationList.setModel(aggreListModel);
        columnsList.setModel(columnListModel);
        filterList.setTransferHandler(new TreeTransferHandler());
        aggregationList.setTransferHandler(new TreeTransferHandler());
        columnsList.setTransferHandler(new TreeTransferHandler());

        add(mainPanel);
        this.setVisible(true);
    }

    public static void main(String[] args){
        QueryUI m = new QueryUI("My Project");
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

    public DefaultTreeModel setGlobalSchemaTree(List<GlobalTableData> globalTables){
        CustomTreeNode data = new CustomTreeNode("Global Tables", NodeType.GLOBAL_TABLES);
        //tables
        for (GlobalTableData gt:globalTables) {
            CustomTreeNode tables = new CustomTreeNode(gt.getTableName(), gt, NodeType.GLOBAL_TABLE);
            //global cols
            for (GlobalColumnData col : gt.getGlobalColumnDataList()) {
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

    public void globalToLocalQueries(GlobalTableData globalTableData){
        //List<TableData> localTables = metaDataManager.getLocalTablesOfGlobalTable(globalTableData);
        //ResultSet results = prestoMediator.getLocalTablesQueries(localTables);
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
            boolean insert = dl.isInsert();

            // Get the string that is being dropped.
            Transferable t = info.getTransferable();
            CustomTreeNode data;
            try {
                data = (CustomTreeNode) t.getTransferData(nodesFlavor);
            } catch (Exception e) {
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
            String lisElem = data.getUserObject().toString();
            if (list.equals(filterList)){
                String filterValue = (String)JOptionPane.showInputDialog(
                        null,
                        "Insert the value",
                        "Filter value selection",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "");
                lisElem+= " = " + filterValue;
            }
            else if (list.equals(aggregationList)){
                lisElem+= " ("+ aggregationOpComboBox.getSelectedItem().toString() +")";
            }
            listModel.add(index++, lisElem);
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
