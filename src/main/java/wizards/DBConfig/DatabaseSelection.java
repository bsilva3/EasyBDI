package wizards.DBConfig;

import helper_classes.DBData;
import helper_classes.TableData;
import prestoComm.DBModel;
import wizards.global_schema_config.NodeType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.xml.soap.Node;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSelection extends JPanel{
    private JPanel mainPanel;
    private JCheckBoxTree checkBoxTree;
    private JScrollPane scrollPane;
    private DefaultTreeModel treeModel;
    private List<DBData> dbs;

    public DatabaseSelection (List<DBData> dbs){
        this.dbs = dbs;
        setDbsInJtree();
        add(mainPanel);
        setVisible(true);
    }

    private void setDbsInJtree(){
        CheckBoxTreeNode root = new CheckBoxTreeNode("root", null, null);
        for (DBData db : dbs){
            CheckBoxTreeNode dbNode = new CheckBoxTreeNode(db.getDbName()+" in "+db.getUrl()+" ("+db.getDbModel()+")", db, NodeType.DATABASE);
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
                    CheckBoxTreeNode tableNodes = new CheckBoxTreeNode(prefixT + t.getTableName(), t, NodeType.TABLE);
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
    }
    /*private void createUIComponents() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1.0;
        gc.weighty = 5.0;
        checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setPreferredSize(new Dimension(700, 450));
        scrollPane.add(checkBoxTree, gc);
    }*/

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
