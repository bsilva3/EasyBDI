package main_app.wizards.DBConfig;

import helper_classes.DBData;
import helper_classes.TableData;
import helper_classes.DBModel;
import main_app.metadata_storage.MetaDataManager;
import main_app.wizards.global_schema_config.NodeType;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseSelection extends JPanel{
    private JPanel mainPanel;
    private JCheckBoxTree checkBoxTree;
    private JScrollPane scrollPane;
    private JLabel helpLabel;
    private JLabel stepLabel;
    private DefaultTreeModel treeModel;
    private List<DBData> dbs;
    private boolean isEdit;
    private MetaDataManager dbManager;
    public static final String LOCAL_SCHEMA_ELEMENT = "-> In Local Schema";

    public DatabaseSelection (List<DBData> dbs, boolean isEdit, MetaDataManager dbManager){
        this.dbManager = dbManager;
        this.isEdit = isEdit;
        if (isEdit){
            helpLabel.setText("<html>Select which elements in each database to use for the local schema construction by checking the boxes next to them.<br/>" +
                    "Elements that were previously selected to build the local schema cannot be unselected.<html>");
        }
        else {
            //new project
            helpLabel.setText("<html>Select which elements in each database to use for the local schema construction by checking the boxes next to them.<html>");
        }
        this.setLayout(new BorderLayout());
        stepLabel.setText("Step 2/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));
        this.dbs = dbs;
        setDbsInJtree();
        add(mainPanel);
        setVisible(true);
    }

    private void setDbsInJtree(){
        CheckBoxTreeNode root = new CheckBoxTreeNode("root", null, null);
        for (DBData db : dbs){
            CheckBoxTreeNode dbNode = null;
            if (isEdit && dbManager.dbExists(db.getDbName(), db.getUrl())){
                dbNode = new CheckBoxTreeNode(db.getDbName()+" in "+db.getUrl()+" ("+db.getDbModel()+")"+LOCAL_SCHEMA_ELEMENT, db, NodeType.DATABASE);
            }
            else{
                dbNode = new CheckBoxTreeNode(db.getDbName()+" in "+db.getUrl()+" ("+db.getDbModel()+")", db, NodeType.DATABASE);
            }
            Map<String, List<TableData>> tablesInSchemas = db.getTableBySchemaMap();
            for (Map.Entry<String, List<TableData>> entry : tablesInSchemas.entrySet()) {
                String schema = entry.getKey();
                List<TableData> tables = entry.getValue();
                String prefix = "";
                if (db.getDbModel() == DBModel.MongoDB)
                    prefix = "Mongo Database: ";
                else
                    prefix = "Schema: ";
                CheckBoxTreeNode schemaNode = new CheckBoxTreeNode(prefix+schema, schema, NodeType.SCHEMA);
                for (TableData t : tables){
                    String prefixT = "";
                    if (db.getDbModel() == DBModel.MongoDB)
                        prefixT = "Collection: ";
                    else
                        prefixT = "Table: ";
                    CheckBoxTreeNode tableNodes = null;
                    if (isEdit && dbManager.tableExists(t.getTableName(), t.getSchemaName(), db.getDbName(), db.getUrl())){
                        tableNodes = new CheckBoxTreeNode(prefixT + t.getTableName()+LOCAL_SCHEMA_ELEMENT, t, NodeType.TABLE);
                    }
                    else{
                        tableNodes = new CheckBoxTreeNode(prefixT + t.getTableName(), t, NodeType.TABLE);
                    }
                    schemaNode.add(tableNodes);
                }
                dbNode.add(schemaNode);
            }
            root.add(dbNode);
        }
        treeModel = new DefaultTreeModel(root);
        checkBoxTree.setModel(treeModel);
        checkBoxTree.setRootVisible(false);
        //expand all nodes
        for (int i = 0; i < checkBoxTree.getRowCount(); i++) {
            checkBoxTree.expandRow(i);
        }
        checkBoxTree.checkSubTree(new TreePath(root.getPath()), true);//begin with all nodes checked
        checkBoxTree.revalidate();
        checkBoxTree.updateUI();
    }


    public List<DBData> getSelection(){
        List<DBData> filteredDbs = new ArrayList<>();
        CheckBoxTreeNode root = (CheckBoxTreeNode) treeModel.getRoot();

        Map<TreePath, JCheckBoxTree.CheckedNode> state = checkBoxTree.nodesCheckingState;
        int childs = root.getChildCount();
        for (int i = 0; i < childs; i++){
            //database node
            CheckBoxTreeNode dbNode = (CheckBoxTreeNode) root.getChildAt(i);
            boolean isSelected = state.get(new TreePath(dbNode.getPath())).isSelected;//check in the list of paths of all nodes if this node is selected
            if (isSelected){
                //database is selected
                DBData db = (DBData) dbNode.getObj();
                db.clearTables();
                int nDBChilds = dbNode.getChildCount();
                for (int j = 0; j < nDBChilds; j++){ //schemas
                    CheckBoxTreeNode schemaNode = (CheckBoxTreeNode) dbNode.getChildAt(j);
                   if(state.get(new TreePath(schemaNode.getPath())).isSelected){//tables
                       int nSchemasChilds = schemaNode.getChildCount();
                       for (int k = 0; k < nSchemasChilds; k++) {
                           CheckBoxTreeNode tableNode = (CheckBoxTreeNode) schemaNode.getChildAt(k);
                           if (state.get(new TreePath(tableNode.getPath())).isSelected) {
                                db.addTable((TableData) tableNode.getObj());
                           }
                       }
                   }
                }
                filteredDbs.add(db);
            }
        }
        return filteredDbs;
    }
}
