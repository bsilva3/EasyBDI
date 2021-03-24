package main_app.wizards.DBConfig;

import helper_classes.DBData;
import helper_classes.TableData;
import helper_classes.DBModel;
import main_app.metadata_storage.MetaDataManager;
import main_app.wizards.MainWizardPanel;
import main_app.wizards.global_schema_config.NodeType;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseSelection extends JPanel {
    private JPanel mainPanel;
    private JCheckBoxTree checkBoxTree;
    private JScrollPane scrollPane;
    private JLabel helpLabel;
    private JLabel stepLabel;
    private DefaultTreeModel treeModel;
    private List<DBData> dbs;
    private boolean isEdit;
    private MetaDataManager dbManager;
    private MainWizardPanel mainWizardPanel;
    public static final String LOCAL_SCHEMA_ELEMENT = "-> In Local Schema";

    public DatabaseSelection(List<DBData> dbs, MainWizardPanel mainWizardPanel) {
        this.mainWizardPanel = mainWizardPanel;
        this.dbManager = mainWizardPanel.getMetaDataManager();
        this.isEdit = mainWizardPanel.isEdit();
        if (isEdit) {
            helpLabel.setText("<html>Select which elements in each database to use for the local schema construction by checking the boxes next to them.<br/>" +
                    "Elements that were previously selected to build the local schema cannot be unselected.<html>");
        } else {
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

    private void setDbsInJtree() {
        CheckBoxTreeNode root = new CheckBoxTreeNode("root", null, null);
        for (DBData db : dbs) {
            CheckBoxTreeNode dbNode = null;
            if (isEdit && dbManager.dbExists(db.getDbName(), db.getUrl())) {
                dbNode = new CheckBoxTreeNode(db.getDbName() + " in " + db.getUrl() + " (" + db.getDbModel() + ")" + LOCAL_SCHEMA_ELEMENT, db, NodeType.DATABASE);
            } else {
                dbNode = new CheckBoxTreeNode(db.getDbName() + " in " + db.getUrl() + " (" + db.getDbModel() + ")", db, NodeType.DATABASE);
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
                CheckBoxTreeNode schemaNode = new CheckBoxTreeNode(prefix + schema, schema, NodeType.SCHEMA);
                for (TableData t : tables) {
                    String prefixT = "";
                    if (db.getDbModel() == DBModel.MongoDB)
                        prefixT = "Collection: ";
                    else
                        prefixT = "Table: ";
                    CheckBoxTreeNode tableNodes = null;
                    if (isEdit && dbManager.tableExists(t.getTableName(), t.getSchemaName(), db.getDbName(), db.getUrl())) {
                        tableNodes = new CheckBoxTreeNode(prefixT + t.getTableName() + LOCAL_SCHEMA_ELEMENT, t, NodeType.TABLE);
                    } else {
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


    public List<DBData> getSelection() {
        List<DBData> filteredDbs = new ArrayList<>();
        CheckBoxTreeNode root = (CheckBoxTreeNode) treeModel.getRoot();

        Map<TreePath, JCheckBoxTree.CheckedNode> state = checkBoxTree.nodesCheckingState;
        int childs = root.getChildCount();
        for (int i = 0; i < childs; i++) {
            //database node
            CheckBoxTreeNode dbNode = (CheckBoxTreeNode) root.getChildAt(i);
            boolean isSelected = state.get(new TreePath(dbNode.getPath())).isSelected;//check in the list of paths of all nodes if this node is selected
            if (isSelected) {
                //database is selected
                DBData db = (DBData) dbNode.getObj();
                db.clearTables();
                int nDBChilds = dbNode.getChildCount();
                for (int j = 0; j < nDBChilds; j++) { //schemas
                    CheckBoxTreeNode schemaNode = (CheckBoxTreeNode) dbNode.getChildAt(j);
                    if (state.get(new TreePath(schemaNode.getPath())).isSelected) {//tables
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
        if (isEdit) {
            //TODO: revision; might not detect all cases in which the user adds new data sources
            //check if user added elements to global schema
            if (filteredDbs.size() == dbs.size()) {
                for (int i = 0; i < dbs.size(); i++) {
                    if (dbs.get(i).getTableList().size() != filteredDbs.get(i).getTableList().size()) {
                        mainWizardPanel.setLocalSchemaChange(true);
                        break;
                    }
                }
                mainWizardPanel.setLocalSchemaChange(false);
                return dbs;
            } else {
                //user change local schema
                mainWizardPanel.setLocalSchemaChange(true);
            }
        }
        return filteredDbs;
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
        mainPanel.setPreferredSize(new Dimension(822, 550));
        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(700, 450));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 5.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);
        checkBoxTree = new JCheckBoxTree();
        scrollPane.setViewportView(checkBoxTree);
        helpLabel = new JLabel();
        helpLabel.setHorizontalAlignment(0);
        helpLabel.setHorizontalTextPosition(0);
        helpLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(helpLabel, gbc);
        stepLabel = new JLabel();
        stepLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        mainPanel.add(stepLabel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
