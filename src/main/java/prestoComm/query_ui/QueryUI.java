package prestoComm.query_ui;

import helper_classes.*;
import helper_classes.Utils;
import io.github.qualtagh.swing.table.model.*;
import io.github.qualtagh.swing.table.view.JBroTable;
import io.github.qualtagh.swing.table.view.JBroTableModel;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import prestoComm.*;
import wizards.global_schema_config.CustomTreeNode;
import wizards.global_schema_config.CustomeTreeCellRenderer;
import wizards.global_schema_config.NodeType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static prestoComm.query_ui.GlobalTableQuery.MAX_SELECT_COLS;

public class QueryUI extends JPanel{
    private JBroTable queryResultsTableGroupable;
    private JTree schemaTree;
    private JTree rowFilterTree;
    private JTextArea editFilters;
    private JTextArea editColFilters;
    private JTextArea editFiltersAggr;
    private JList aggregationsList;
    private JTextArea manualAggregations;
    private JComboBox aggregationOpComboBox;
    private JList rowsList;
    private JPanel mainPanel;
    private JButton executeQueryButton;
    private JButton backButton;
    private JList columnsList;
    private JTabbedPane logPane;
    private JList queryLogList;
    private JButton saveSelectedQueryButton;
    private JButton saveAllQueriesButton;
    private JButton clearAllFieldsButton;
    private JScrollPane tablePane;
    private JTree aggrFiltersTree;
    private JLabel rowsLabel;
    private JLabel columnLabel;
    private JLabel measuresLabel;
    private JLabel measuresManualEditLabel;
    private JLabel filtersLabel;
    private JLabel aggrFiltersLabel;
    private JLabel arrowLabel;
    private JScrollPane filterPane;
    private JList globalQueryLogList;
    private JScrollPane aggregationsPane;
    private JPanel aggrAreaPanel;
    private JScrollPane aggrFilterPane;
    private JPanel globalSchemaLogPane;
    private JPanel localSchemaLogPane;
    private JList localQueryLogList;
    private JTree colFiltersTree;
    private boolean showLocalQueryLog;

    private StarSchema starSchema;
    private GlobalTableQuery globalTableQueries;//used to store all queries for each global table, and their columns

    private DefaultTreeModel schemaTreeModel;
    private DefaultTreeModel rowFilterTreeModel;
    private DefaultTreeModel colFilterTreeModel;
    private DefaultTreeModel aggrFilterTreeModel;
    private DefaultListModel measuresListModel;
    private DefaultListModel columnListModel;
    private DefaultListModel rowsListModel;
    private DefaultListModel queryStatusLogModel;
    private DefaultListModel globalSchemaLogModel;
    private DefaultListModel localSchemaLogModel;
    private JBroTableModel defaultTableModel;

    private MetaDataManager metaDataManager;
    private PrestoMediator prestoMediator;
    private static final String GROUP_BY_OP = "Group By";
    private boolean countAllAdded;
    private final String[] aggregationsMeasures = { GROUP_BY_OP, "COUNT", "SUM", "AVG"};
    private final String[] aggregationsRows = {"COUNT", "SUM", "AVERAGE", "MAX", "MIN"};
    private final String[] numberOperations = { "=", "!=", ">", ">=", "<", "<="};
    private final String[] stringOperations = { "=", "!=", "like"};

    private MainMenu mainMenu;
    private JMenu starSchemaMenu;
    private String currentStarSchema;

    public QueryUI(String projectName, final MainMenu mainMenu){
        this.mainMenu = mainMenu;
        this.metaDataManager = new MetaDataManager(projectName);
        this.prestoMediator = new PrestoMediator();
        countAllAdded = false;
        showLocalQueryLog = false;

        mainMenu.setTitle("Analytical Query Environment");
        //mainPanel.setSize(mainMenu.getSize());
        MouseAdapter adapterTooltip = new MouseAdapter(){
            final int defaultTimeout = ToolTipManager.sharedInstance().getInitialDelay();

                @Override
                public void mouseEntered(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(0);
                ToolTipManager.sharedInstance().setDismissDelay(30000);//tooltip visible for 30 seconds
            }

                @Override
                public void mouseExited(MouseEvent e) {
                ToolTipManager.sharedInstance().setInitialDelay(defaultTimeout);
            }
        };

        //set labels icons
        Image img = null;
        try {
            img = ImageIO.read(new File(Constants.IMAGES_DIR+"help_icon.png"));
            rowsLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            rowsLabel.addMouseListener(adapterTooltip);
            rowsLabel.setToolTipText("<html><p>Attributes from dimension tables dropped here will be used to view records as rows.</p> " +
                    "<p>This is the equivalent as the SQL Select statement.</p>" +
                    " <p>Order by is available by right clicking a value.</p></html>");
            columnLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            columnLabel.addMouseListener(adapterTooltip);
            columnLabel.setToolTipText("<html><p>Columns from dimension tables dropped here will be used to view records as columns and measures will be aggregated across the values of the columns.</p> " +
                    "<p>This is the equivalent as the SQL Pivot statement.</p>" +
                    " <p>At least one measure and one value in 'rows' is required to perform queries containing columns. An attribute cannot be in 'columns' and 'rows' at the same time</p>" +
                    " <p>A maximum of 3 attributes can be dropped here.</p></html>");
            measuresLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            measuresLabel.addMouseListener(adapterTooltip);
            measuresLabel.setToolTipText("<html><p>Attributes from dimension tables or measures dropped here will be used to view records as rows and aggregated accordingly with the selected function.</p> " +
                    "<p>Measures can only be dropped on this area.</p>" +
                    "<p>Distinct can only be added to attributes with aggregate functions. For attributes with no aggregate functions, the group by will produce a similar" +
                    "effect as distinct.</p>" +
                    "<p>Order by is available by right clicking on an attribute.</p></html>");
            measuresManualEditLabel = new JLabel("<html><p>To manually edit this area, identify attributes in the current star schema in the form " +
                    "'table.attribute', along with an aggregate function.</p> <p>Any attribute identified here must be used within an aggregate function. Measures and Dimension attributes can be specified here.</p>" +
                    "<p>Order By attributes cannot be here specified.</p>");
            measuresManualEditLabel.setFont(new Font("", Font.PLAIN, 12));
            measuresLabel.setText("Aggregations - Normal Edit Mode");
            aggrFiltersLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            aggrFiltersLabel.addMouseListener(adapterTooltip);
            aggrFiltersLabel.setToolTipText("<html><p>Drag attributes from the aggregations area to create conditions with those attributes.</p> " +
                    "<p>You cannot drag attributes from the star schema to this area, as only conditions with aggregations are allowed here.</p>" +
                    "<p>This is equivalent as to adding a condition on a SQL Having statement.</p></html>");
            aggrFiltersLabel.setText("Aggregation Filters - Normal Mode");
            filtersLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
            filtersLabel.addMouseListener(adapterTooltip);
            filtersLabel.setToolTipText("<html><p>Drag attributes from the star schema to create conditions.</p> " +
                    "<p>Attributes used here are assumed to be part of the same tables as the attributes selected in the 'rows' and aggregations' areas.</p>" +
                    "<p>Any attributes that belongs to a table not listed on those 2 areas will result in an invalid SQL.</p> " +
                    "<p>It is however possible to use attributes even if that attribute is not selected in any of those areas if the table from the attribute is listed on at least one of the areas.</p>" +
                    "<p>This is equivalent as to adding a condition on a SQL Where statement.</p>" +
                    "<p>It is possible to write the filters instead of a dragging and dropping then by selecting the 'change to manual edit' option on the pop up menu.</p></html>");
            filtersLabel.setText(filtersLabel.getText() + " - Normal Edit Mode");

            img = ImageIO.read(new File(Constants.IMAGES_DIR+"arrow_right.png"));
            arrowLabel.setIcon(new ImageIcon(img.getScaledInstance(20, 20, 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //set ops on combobox
        aggregationOpComboBox.setModel(new DefaultComboBoxModel(aggregationsMeasures));

        List<String> starSchemas =  metaDataManager.getStarSchemaNames();
        if (starSchemas.isEmpty()){
            JOptionPane.showMessageDialog(null, "There are no star schemas in this project.", "No Star schemas found", JOptionPane.ERROR_MESSAGE);
            //close db
            metaDataManager.close();
            mainMenu.returnToMainMenu();
        }
        else {
            currentStarSchema = starSchemas.get(0);//when opening this window, select first star schema by default
            //Set JMENU BAR
            JMenuBar menuBar = new JMenuBar();
            //star schema selection
            starSchemaMenu = new JMenu("Choose Star Schema");
            BufferedImage image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"cube_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //starSchemaMenu.setHorizontalTextPosition(SwingConstants.CENTER);
            //starSchemaMenu.setVerticalTextPosition(SwingConstants.BOTTOM);
            populateStarSchemaMenu(starSchemas);
            starSchemaMenu.setIcon(new ImageIcon(image.getScaledInstance(25, 25, 0)));
            menuBar.add(starSchemaMenu);

            // advanced option
            JMenu advancedOptions = new JMenu("Advanced Options");
            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"advanced_options_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //advancedOptions.setHorizontalTextPosition(SwingConstants.CENTER);
            //advancedOptions.setVerticalTextPosition(SwingConstants.BOTTOM);
            advancedOptions.setIcon(new ImageIcon(image.getScaledInstance(25, 25, 0)));

            JCheckBoxMenuItem item1 = new JCheckBoxMenuItem("Show Local Schema Query Log");
            item1.setState(false);
            item1.addActionListener(e -> showHideLocalSchemaQueryPane(item1.getState()));
            showHideLocalSchemaQueryPane(item1.getState());
            advancedOptions.add(item1);
            menuBar.add(advancedOptions);

            JMenu queryMenu = new JMenu("Query");
            queryMenu.addActionListener(e -> saveQueryState());
            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"query_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            queryMenu.setIcon(new ImageIcon(image.getScaledInstance(20, 20, 0)));

                //save query state
            JMenuItem saveQueryState = new JMenuItem("Save Query");
            saveQueryState.addActionListener(e -> saveQueryState());
            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"save_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            saveQueryState.setIcon(new ImageIcon(image.getScaledInstance(20, 20, 0)));
            queryMenu.add(saveQueryState);

            //load query state
            JMenuItem loadQueryState = new JMenuItem("Load Query");
            loadQueryState.addActionListener(e -> selectQueryToLoad());
            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"load_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadQueryState.setIcon(new ImageIcon(image.getScaledInstance(20, 20, 0)));
            queryMenu.add(loadQueryState);

            menuBar.add(queryMenu);

            JMenu exportMenu = new JMenu("Export Results");

            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"export_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //advancedOptions.setHorizontalTextPosition(SwingConstants.CENTER);
            //advancedOptions.setVerticalTextPosition(SwingConstants.BOTTOM);
            exportMenu.setIcon(new ImageIcon(image.getScaledInstance(20, 20, 0)));

            JMenuItem exportToCsv = new JMenuItem("Export To CSV");

            image = null;
            try {
                image = ImageIO.read(new File(Constants.IMAGES_DIR+"export_as_csv_icon.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            exportToCsv.setIcon(new ImageIcon(image.getScaledInstance(20, 20, 0)));
            exportMenu.addActionListener(e -> exportResultsToCSV());
            exportMenu.add(exportToCsv);

            menuBar.add(exportMenu);

            //menuBar.setBackground(new Color(154, 154, 154));
            mainMenu.setJMenuBar(menuBar);


            this.starSchema = metaDataManager.getStarSchema(currentStarSchema);
            schemaTreeModel = setStarSchemaTree();
            schemaTree.setModel(schemaTreeModel);
            CustomTreeNode root = (CustomTreeNode) schemaTreeModel.getRoot();
            expandAllStarSchema(new TreePath(root), true);
            schemaTree.setCellRenderer(new CustomeTreeCellRenderer());
            schemaTree.setTransferHandler(new TreeTransferHandler());
            schemaTree.setDragEnabled(true);
            schemaTree.setRootVisible(false);

            rowFilterTreeModel = null;
            colFilterTreeModel = null;
            aggrFilterTreeModel = null;
            measuresListModel = new DefaultListModel();
            columnListModel = new DefaultListModel();
            rowsListModel = new DefaultListModel();
            queryStatusLogModel = new DefaultListModel();
            globalSchemaLogModel = new DefaultListModel();
            localSchemaLogModel = new DefaultListModel();
            rowFilterTree.setModel(rowFilterTreeModel);
            rowFilterTree.setCellRenderer(new FilterNodeCellRenderer());
            colFiltersTree.setModel(colFilterTreeModel);
            colFiltersTree.setCellRenderer(new FilterNodeCellRenderer());
            rowFilterTree.addMouseListener(getMouseListenerForFilterTree());
            colFiltersTree.addMouseListener(getMouseListenerForColsFilterTree());
            aggrFiltersTree.setModel(aggrFilterTreeModel);
            aggrFiltersTree.setCellRenderer(new FilterNodeCellRenderer());
            aggrFiltersTree.addMouseListener(getMouseListenerForAggFilterTree());
            aggregationsList.setModel(measuresListModel);
            aggregationsList.setCellRenderer(new CustomGroupCellRendererList());
            rowsList.setModel(rowsListModel);
            rowsList.setCellRenderer(new CustomGroupCellRendererList());
            columnsList.setModel(columnListModel);
            columnsList.setCellRenderer(new CustomGroupCellRendererList());
            queryLogList.setModel(queryStatusLogModel);
            globalQueryLogList.setModel(globalSchemaLogModel);
            localQueryLogList.setModel(localSchemaLogModel);

            rowFilterTree.setTransferHandler(new TreeTransferHandler());
            colFiltersTree.setTransferHandler(new TreeTransferHandler());
            aggregationsList.setTransferHandler(new TreeTransferHandler());
            rowsList.setTransferHandler(new TreeTransferHandler());
            columnsList.setTransferHandler(new TreeTransferHandler());
            aggrFiltersTree.setTransferHandler(new TreeTransferHandler());

            editFilters = new JTextArea(80, 100);
            editFilters.setVisible(false);
            editFilters.setWrapStyleWord(true);
            editFilters.setLineWrap(true);
            editFilters.addMouseListener(getMouseListenerEditFilterTextArea());

            editColFilters = new JTextArea(80, 100);
            editColFilters.setVisible(false);
            editColFilters.setWrapStyleWord(true);
            editColFilters.setLineWrap(true);
            editColFilters.addMouseListener(getMouseListenerEditFilterTextArea());

            editFiltersAggr = new JTextArea(80, 100);
            editFiltersAggr.setVisible(false);
            editFiltersAggr.setWrapStyleWord(true);
            editFiltersAggr.setLineWrap(true);
            editFiltersAggr.addMouseListener(getMouseListenerEditFilterAggrTextArea());

            manualAggregations = new JTextArea(80, 100);
            manualAggregations.setVisible(false);
            manualAggregations.setWrapStyleWord(true);
            manualAggregations.setLineWrap(true);
            manualAggregations.addMouseListener(getMouseListenerEditAggrTextArea());

            queryLogList.setCellRenderer(new StripeRenderer());
            globalQueryLogList.setCellRenderer(new StripeRenderer());



            //jtable
            //this.defaultTableModel = new JBroTableModel(new ModelData());
            //queryResultsTableGroupable = new JBroTable();
            //tablePane.add(queryResultsTableGroupable);
            //this.queryResultsTable.setModel(defaultTableModel);
            //queryResultsTableGroupable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//maintain column width

            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //open wizard and edit current project
                    //remove manubar
                    mainMenu.setJMenuBar(null);
                    metaDataManager.close();
                    mainMenu.returnToMainMenu();
                }
            });

            executeQueryButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //open wizard and edit current project
                    executeQueryAndShowResults();
                }
            });

            //listeners for lists left click to open menus
            columnsList.addMouseListener(getMouseListenerForColumnList());
            rowsList.addMouseListener(getMouseListenerForRowsList());
            aggregationsList.addMouseListener(getMouseListenerForMeasuresList());

            queryLogList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 2) {
                        // Double-click on list log item: show full message
                        int index = list.locationToIndex(evt.getPoint());
                        if (index < 0){
                            return;
                        }
                        QueryLog queryLog = (QueryLog) queryStatusLogModel.get(index);
                        JOptionPane optionPane = new NarrowOptionPane();
                        optionPane.setMessage(queryLog.toString());
                        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        JDialog dialog = optionPane.createDialog(null, "Query Log");
                        dialog.setVisible(true);
                    }
                }
            });

            globalQueryLogList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 2) {
                        // Double-click on list log item: show full message
                        int index = list.locationToIndex(evt.getPoint());
                        if (index < 0){
                            return;
                        }
                        QueryLog queryLog = (QueryLog) globalSchemaLogModel.get(index);
                        JOptionPane optionPane = new NarrowOptionPane();
                        optionPane.setMessage(queryLog.toString());
                        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        JDialog dialog = optionPane.createDialog(null, "Global Schema Query");
                        dialog.setVisible(true);
                    }
                }
            });

            localQueryLogList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    JList list = (JList) evt.getSource();
                    if (evt.getClickCount() == 2) {
                        // Double-click on list log item: show full message
                        int index = list.locationToIndex(evt.getPoint());
                        if (index < 0){
                            return;
                        }
                        QueryLog queryLog = (QueryLog) localSchemaLogModel.get(index);
                        JOptionPane optionPane = new NarrowOptionPane();
                        optionPane.setMessage(queryLog.toString());
                        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
                        JDialog dialog = optionPane.createDialog(null, "Local Schema Query");
                        dialog.setVisible(true);
                    }
                }
            });

            this.setLayout(new BorderLayout());
            this.globalTableQueries = new GlobalTableQuery(prestoMediator, starSchema.getFactsTable(), starSchema.getDimsTables());
            //this.setSize(mainMenu.getSize());
            mainPanel.setSize(mainMenu.getSize());
            this.revalidate();
            add(mainPanel);
            this.setVisible(true);
        }
        saveAllQueriesButton.addActionListener(e -> {
            saveToFile(true);
        });
        saveSelectedQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile(false);
            }
        });

        clearAllFieldsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFieldsAndQueryElements();
            }
        });
    }

    private void populateStarSchemaMenu(List<String> starSchemas){
        starSchemaMenu.removeAll();
        for (String starSchema : starSchemas){
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(starSchema);
            if (currentStarSchema.equals(starSchema))
                menuItem.setState(true);
            menuItem.addActionListener(e -> changeStarSchema(menuItem.getText(), menuItem));
            starSchemaMenu.add(menuItem);
        }
    }

    private void changeStarSchema(String starSchemaName, JCheckBoxMenuItem menuItem){
        if (starSchemaName.equals(currentStarSchema)) {
            menuItem.setState(true);
            return;
        }
        currentStarSchema = starSchemaName;
        clearAllFieldsAndQueryElements();//new project selected, clear all ui and data structures
        this.starSchema = metaDataManager.getStarSchema(currentStarSchema);
        globalTableQueries.setFactsTable(starSchema.getFactsTable());
        schemaTreeModel = setStarSchemaTree();
        schemaTree.setModel(schemaTreeModel);
        expandAllStarSchema(new TreePath(schemaTreeModel.getRoot()), true);
        schemaTree.revalidate();
        schemaTree.updateUI();
        for (int i = 0; i < starSchemaMenu.getItemCount(); i++){
            if (!starSchemaMenu.getItem(i).equals(menuItem))
                starSchemaMenu.getItem(i).setSelected(false);
        }
    }

    private void showHideLocalSchemaQueryPane(boolean show){
        this.showLocalQueryLog = show;
        if (showLocalQueryLog)
            logPane.addTab("Local Schema Query", localSchemaLogPane);
        else{
            logPane.remove(localSchemaLogPane);
        }
    }

    private void clearAllFieldsAndQueryElements(){
        //clear all fields in ui
        rowsListModel.clear();
        columnListModel.clear();
        measuresListModel.clear();
        editFilters.setText("");
        editFiltersAggr.setText("");
        manualAggregations.setText("");
        aggrFilterTreeModel = null;
        rowFilterTreeModel = null;
        colFilterTreeModel = null;

        rowsList.revalidate();
        rowsList.updateUI();
        columnsList.revalidate();
        columnsList.updateUI();
        aggregationsList.revalidate();
        aggregationsList.updateUI();
        aggrFiltersTree.setModel(null);
        aggrFiltersTree.revalidate();
        aggrFiltersTree.updateUI();
        rowFilterTree.setModel(null);
        rowFilterTree.revalidate();
        rowFilterTree.updateUI();

        //clear all query elements in structures
        globalTableQueries.clearAllElements();
    }

    private void clearRowFilters(){
        rowFilterTree.setModel(null);
        rowFilterTree.revalidate();
        rowFilterTree.updateUI();
    }

    private void clearColFilters(){
        colFiltersTree.setModel(null);
        colFiltersTree.revalidate();
        colFiltersTree.updateUI();
    }

    private void clearAggrFilters(){
        aggrFiltersTree.setModel(null);
        aggrFiltersTree.revalidate();
        aggrFiltersTree.updateUI();
    }

    private void selectQueryToLoad(){
        List<String> queryNames = metaDataManager.getListOfQueriesByCube(currentStarSchema);
        new QuerySelector(queryNames, this);
    }

    public void loadSelectedQuery(String queryName){
        int cubeID = metaDataManager.getOrcreateCube(currentStarSchema);
        int queryID = metaDataManager.getQueryID(queryName, cubeID);
        if (queryID == -1){
            JOptionPane.showMessageDialog(mainMenu, "Could not load query: does not exist", "Error loading query", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Map<GlobalTableData, List<GlobalColumnData>> rows = metaDataManager.getQueryRows(queryID);
        Map<GlobalTableData, List<GlobalColumnData>> cols = metaDataManager.getQueryColumns(queryID);
        List<GlobalColumnData> measures = metaDataManager.getQueryMeasures(queryID);
        //clear all fields in ui
        clearAllFieldsAndQueryElements();

        //place columns in UI (if any) and in data structures
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> colSelect : cols.entrySet()){
            for (GlobalColumnData c : colSelect.getValue()){
                addColumnsToList(columnListModel, c, colSelect.getKey());
            }
        }

        //place measures in UI (if any) and in data structures -> (measures before rows because its easier to get index of measures, as they go on top on aggr area
        String editAggr = metaDataManager.getManualAggr(queryID);
        if ( (measures == null && editAggr.length()>0)  || (measures.isEmpty() && editAggr.length()>0)){ //aggregations were in manual mode
            changeAggrManualMode();
            manualAggregations.setText(editAggr);
            manualAggregations.revalidate();
            manualAggregations.updateUI();
        }
        else if (measures != null && measures.size() > 0){//aggregation added on normal mode
            changeAggrNormalMode();
            int index = 0;
            for (GlobalColumnData measure : measures) {
                addAggrRow(measuresListModel, starSchema.getFactsTable().getGlobalTable(), measure, true);
                index = measuresListModel.getSize()-1;
                if (measure.getOrderBy() != null && measure.getOrderBy().equalsIgnoreCase("ASC")){
                    this.addOrderBy(index, true, true);
                }
                else if (measure.getOrderBy() != null && measure.getOrderBy().equalsIgnoreCase("DESC")){
                    this.addOrderBy(index, false, true);
                }
            }
        }

        //place rows in UI and in data structures and add group by's (if any)
        for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rowSelect : rows.entrySet()){
            for (GlobalColumnData c : rowSelect.getValue()){
                int index = 0;
                boolean isAggrRow = false;
                if (c.getAggrOp()!= null && !c.getAggrOp().isEmpty()) {
                    addAggrRow(measuresListModel, rowSelect.getKey(), c, false);
                    index = measuresListModel.getSize()-1;
                    isAggrRow = true;
                }
                else {
                    addRowsToList(rowsListModel, c, rowSelect.getKey());
                    index = rowsListModel.getSize()-1;
                }
                if (c.getOrderBy() != null && c.getOrderBy().equalsIgnoreCase("ASC")){
                    this.addOrderBy(index, true, isAggrRow);
                }
                else if (c.getOrderBy() != null && c.getOrderBy().equalsIgnoreCase("DESC")){
                    this.addOrderBy(index, false, isAggrRow);
                }
            }
        }

        //get regular filters
        String editFiltersStr = metaDataManager.getManualFilter(queryID);
        if ( editFiltersStr.length()>0){
            changeFilterManualMode();
            editFilters.setText(editFiltersStr);
            editFilters.revalidate();
            editFilters.updateUI();
        }
        else{
            Object[] filters = metaDataManager.getQueryFilters(queryID);
            if (filters != null && filters.length > 0 && filters[1] != null) {
                changeFilterNormalMode();
                this.rowFilterTreeModel = new DefaultTreeModel((FilterNode) filters[1]);
                this.rowFilterTree.setModel(rowFilterTreeModel);
                rowFilterTree.setRootVisible(false);
                FilterNode root = (FilterNode) rowFilterTreeModel.getRoot();
                expandAllFilterNodes(rowFilterTree, new TreePath(root), true);
                String filtersStr = (String) filters[0];
                Set<String> filtersList = new HashSet<>();
                String[] filtersSplit = filtersStr.split(";");
                for (String s : filtersSplit) {
                    filtersList.add(s);
                }
                globalTableQueries.setFilters(filtersList);
            }
        }

        //get aggr filters
        String editAggrFilters = metaDataManager.getManualFilterAggr(queryID);
        if ( (measures == null && editAggrFilters.length()>0) ||  (measures.isEmpty() && editAggrFilters.length()>0)){
            changeAggrFilterManualMode();
            editFiltersAggr.setText(editAggrFilters);
            editFiltersAggr.revalidate();
            editFiltersAggr.updateUI();
        }
        else {
            FilterNode filtersNodeRoot = metaDataManager.getQueryAggrFilters(queryID);
            if (filtersNodeRoot != null) {
                changeAggrFilterNormalMode();
                this.aggrFilterTreeModel = new DefaultTreeModel(filtersNodeRoot);
                this.aggrFiltersTree.setModel(aggrFilterTreeModel);
                aggrFiltersTree.setRootVisible(false);
                FilterNode root = (FilterNode) aggrFilterTreeModel.getRoot();
                expandAllFilterNodes(aggrFiltersTree, new TreePath(root), true);
            }
        }

    }

    private void saveQueryState(){
        //user must name the query
        JTextField nameTxt = new JTextField();
        JComponent[] inputs = new JComponent[]{
                new JLabel("Please, insert a name for this query."),
                nameTxt};
        int result = JOptionPane.showConfirmDialog(
            null,
            inputs,
            "Save Query",
            JOptionPane.PLAIN_MESSAGE);
        if (nameTxt.getText().length() == 0) {
            JOptionPane.showConfirmDialog(
                    null,
                    "You must name your query to save it.",
                    "Save Query Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        //get all select rows:
        Map<GlobalTableData, List<GlobalColumnData>> rows = globalTableQueries.getSelectRows();
        //get all select cols:
        Map<GlobalTableData, List<GlobalColumnData>> columns = globalTableQueries.getSelectColumns();
        List<GlobalColumnData> measures = globalTableQueries.getMeasures();
        List<String> orderBy = globalTableQueries.getOrderBy(); //each order by is in the form 'tableName.column (ASC/DESC)'
        /*for (Map.Entry<GlobalTableData, List<GlobalColumnData>> r : rows.entrySet()){
            String tName = r.getKey().getTableName();
            for (GlobalColumnData c : r.getValue()){
                String cName = c.getName();
                for (String s : orderBy){
                    String [] splitted = s.split(" ");
                    String name = splitted[0];
                    String type = splitted[1];
                    if (name.equals(tName+"."+cName)){
                        c.setOrderBy(type);
                        orderBy.remove(s);
                        break;
                    }
                }
            }
        }*/

        FilterNode filterRoot = null;
        if (rowFilterTreeModel != null){
            filterRoot = (FilterNode) rowFilterTreeModel.getRoot();
        }

        FilterNode aggrFilterRoot = null;
        if (aggrFilterTreeModel != null){
            aggrFilterRoot = (FilterNode) aggrFilterTreeModel.getRoot();
        }

        boolean success = metaDataManager.insertNewQuerySave(nameTxt.getText(), currentStarSchema, rows, columns,
                measures, filterRoot, globalTableQueries.getFilters(), aggrFilterRoot, manualAggregations.getText(), editFilters.getText(), editFiltersAggr.getText() );
        if (success){
            JOptionPane.showMessageDialog(mainMenu, "Query "+nameTxt.getText()+" save successfully!", "Query saved", JOptionPane.PLAIN_MESSAGE);
        }
        else{
            JOptionPane.showMessageDialog(mainMenu, "Query "+nameTxt.getText()+" could not be saved.", "Query not saved", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteQuery(String queryName){
        metaDataManager.deleteQuery(queryName, currentStarSchema);
    }

    private void saveToFile(boolean multiline){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {

            public String getDescription() {
                return "txt file (*.txt)";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    String filename = f.getName().toLowerCase();
                    return filename.endsWith(".txt");
                }
            }
        });
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(mainMenu);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            //validate
            String[] fileSplit = fileToSave.toString().split(".");
            if (fileSplit.length == 2 && fileSplit[1].equals("txt")) {
                // filename is OK as-is
            } else {
                fileToSave = new File(fileToSave.toString() + ".txt");  // append .xml if "foo.jpg.xml" is OK
            }
            System.out.println("Save as file: " + fileToSave.getAbsolutePath());
            boolean success = false;
            if (multiline)
                success = saveAllQueriesLog(fileToSave);
            else
                success = saveLogToFile(queryStatusLogModel.get(queryLogList.getSelectedIndex()).toString(), fileToSave);
            if (success){
                JOptionPane.showMessageDialog(mainMenu, "Query Log Saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(mainMenu, "Failed to save query log. Select an apropriate folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean saveAllQueriesLog(File file){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < queryStatusLogModel.getSize(); i++) {
                //saveLogToFile(queryLogModel.get(i).toString(), folder);
                List<String> limitLine = textLimiter(queryStatusLogModel.get(i).toString(), 80);
                for (String s : limitLine)
                    writer.write(s+"\n");
                writer.write("\n ------------------------------------------------------------------------------- \n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean saveLogToFile(String log, File folder){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(folder));
            List<String> limitLine = textLimiter(log, 80);
            for (String s : limitLine)
                writer.write(s+"\n");

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Limit the ammount of chars by line when saving to file
     * @param input
     * @param limit
     * @return
     */
    private List<String> textLimiter(String input, int limit) {
        List<String> returnList = new ArrayList<>();
        String[] parts = input.split("[ ,\n]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() + part.length() > limit) {
                returnList.add(sb.toString().substring(0, sb.toString().length() - 1));
                sb = new StringBuilder();
            }
            sb.append(part + " ");
        }
        if (sb.length() > 0) {
            returnList.add(sb.toString());
        }
        return returnList;
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
        CustomTreeNode factsNode = new CustomTreeNode("Facts Table: "+facts.getGlobalTable().getTableName(),facts.getGlobalTable(), NodeType.FACTS_TABLE);
        CustomTreeNode measuresNode = new CustomTreeNode("Measures", facts.getGlobalTable(), NodeType.MEASURES);
        CustomTreeNode nonFKNode = new CustomTreeNode("Non-FK attributes",facts.getGlobalTable(), NodeType.FACTS_ATTR);

        //set columns that are measures ONLY
        Map<GlobalColumnData, Boolean> cols = facts.getColumns();

        for (Map.Entry<GlobalColumnData, Boolean> col : cols.entrySet()){
            if (col.getValue() == true){
                //is measure, add
                GlobalColumnData measure = col.getKey();
                CustomTreeNode measureNode = new CustomTreeNode(measure.getName(), measure, NodeType.MEASURE);
                measureNode.add(new CustomTreeNode(col.getKey().getDataType(), "", NodeType.COLUMN_INFO));
                measuresNode.add(measureNode);
            }
            else{
                if (!col.getKey().isForeignKey()){
                    //not a fk or measure
                    CustomTreeNode node = new CustomTreeNode(col.getKey().getName(), col.getKey(), NodeType.GLOBAL_COLUMN);
                    node.add(new CustomTreeNode(col.getKey().getDataType(), "", NodeType.COLUMN_INFO));
                    nonFKNode.add(node);
                }
            }
        }
        factsNode.add(measuresNode);
        //if (nonFKNode.getChildCount() > 0)
          //  factsNode.add(nonFKNode);
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
                if (elem.getType() == ListElementType.GLOBAL_TABLE){
                    return;
                }
                GlobalColumnData col = (GlobalColumnData) elem.getObj();
                GlobalTableData table = getTableInRowIndex(columnListModel, index);
                columnListModel.removeElementAt(index);

                //remove table group if last element or no other elements in that group
                ListElementWrapper el = (ListElementWrapper) columnListModel.getElementAt(columnListModel.size()-1);
                if (columnListModel.size() == 1)
                    columnListModel.removeElementAt(0);
                    //check if last element is a table
                else if (el.getType() == ListElementType.GLOBAL_TABLE ){
                    columnListModel.removeElementAt(columnListModel.size()-1);
                }
                else{
                    //iterate all tables and check if any tabe has no groups and remove it. Empty tables are followed together.
                    for (int i = 1; i < columnListModel.size(); i++){
                        ListElementWrapper e1 = (ListElementWrapper) columnListModel.getElementAt(i-1);
                        ListElementWrapper e2 = (ListElementWrapper) columnListModel.getElementAt(i);
                        if (e1.getType()==ListElementType.GLOBAL_TABLE && e2.getType()==ListElementType.GLOBAL_TABLE){
                            columnListModel.removeElementAt(i-1);
                            break; //end here, because only one emoty table can exist
                        }

                    }
                }

                globalTableQueries.deleteSelectColumnFromTable(table, col);
                columnsList.updateUI();
                columnsList.revalidate();
            }
        };
    }

    private ActionListener getRemoveActionListenerForRowsList(JList list, DefaultListModel listModel, int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                ListElementWrapper elem = (ListElementWrapper)listModel.get(index);
                if (elem.getType() == ListElementType.GLOBAL_TABLE){
                    return;
                }
                GlobalColumnData col = (GlobalColumnData) elem.getObj();
                GlobalTableData table = getTableInRowIndex(listModel, index);
                listModel.removeElementAt(index);

                //remove table group if last element or no other elements in that group
                ListElementWrapper el = (ListElementWrapper) listModel.getElementAt(listModel.size()-1);
                if (listModel.size() == 1)
                    listModel.removeElementAt(0);
                    //check if last element is a table
                else if (el.getType() == ListElementType.GLOBAL_TABLE ){
                    listModel.removeElementAt(listModel.size()-1);
                }
                else{
                    //iterate all tables and check if any tabe has no groups and remove it. Empty tables are followed together.
                    for (int i = 1; i < listModel.size(); i++){
                        ListElementWrapper e1 = (ListElementWrapper) listModel.getElementAt(i-1);
                        ListElementWrapper e2 = (ListElementWrapper) listModel.getElementAt(i);
                        if (e1.getType()==ListElementType.GLOBAL_TABLE && e2.getType()==ListElementType.GLOBAL_TABLE){
                            listModel.removeElementAt(i-1);
                            break; //end here, because only one emoty table can exist
                        }

                    }
                }

                globalTableQueries.deleteSelectRowFromTable(table, col);
                list.updateUI();
                list.revalidate();
            }
        };
    }

    private ActionListener getAddOrderByListenerRows(int index, boolean isAsc, boolean isAggrRow) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                addOrderBy(index, isAsc, isAggrRow);
            }
        };
    }

    public void addOrderBy(int index, boolean isAsc, boolean isAggrRow){
            if (index < 0)
                return;
            ListElementWrapper elem = null;
            if (isAggrRow) {
                elem = (ListElementWrapper) measuresListModel.get(index);
            }
            else {
                elem = (ListElementWrapper) rowsListModel.get(index);
            }

            String tableName = "";
            GlobalColumnData c = (GlobalColumnData) elem.getObj();
            if (isAggrRow) {
                measuresListModel.getElementAt(index).toString();
                tableName = getTableNameOfColumnInList(measuresListModel, index);
            }
            else {
                rowsListModel.getElementAt(index).toString();
                tableName = getTableNameOfColumnInList(rowsListModel, index);
            }
            if (tableName == null)
                return;
            //add to the list (ASC or DESC)
            String order = "";
            if (isAsc)
                order = "ASC";
            else
                order = "DESC";
            c.setOrderBy(order);
            elem.setObj(c);
            if (isAggrRow) {
                elem.setName(elem.getName()+" ("+order+")");
                globalTableQueries.addOrderByRow(c.getAggrOpFullName()+" "+order);//add Aggr(table.col) sortOrder to queries
                measuresListModel.setElementAt(elem, index);//update element with ASC or DESC to signal it will be ordered
                aggregationsList.revalidate();
                aggregationsList.updateUI();
            }
            else {
                elem.setName(elem.getName()+ " ("+order+")");
                rowsListModel.setElementAt(elem, index);//update element with ASC or DESC to signal it will be ordered
                rowsList.revalidate();
                rowsList.updateUI();
                globalTableQueries.addOrderByRow(tableName+"."+c.getName()+" "+order);
            }
    }

    private String getTableNameOfColumnInList(DefaultListModel listModel, int colIndex) {//must be either column or row list
        for (int i = colIndex-1; i >= 0; i--){
            String name = listModel.getElementAt(i).toString();
            if (!name.contains("    ")){
                return name;
            }
        }
        return null;
    }

    private ActionListener getRemoveOrderByListenerRows(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;

                ListElementWrapper elem = (ListElementWrapper) rowsListModel.getElementAt(index);
                String colName = elem.getName().split(" \\(")[0]; //keep element in list, but remove (ASC) or (DESC)

                String colNameOnly = colName;
                colNameOnly.replaceAll("\\s+", "");
                String tableName = getTableNameOfColumnInList(rowsListModel, index);
                if (tableName == null)
                    return;
                elem.setName("   "+colNameOnly);
                GlobalColumnData c = (GlobalColumnData) elem.getObj();
                c.setOrderBy("");
                elem.setObj(c);
                rowsListModel.setElementAt(elem, index);//update element without the asc or desc
                globalTableQueries.removeOrderByIfPresent(tableName+"."+colNameOnly);
                rowsList.revalidate();
            }
        };
    }

    private ActionListener getRemoveActionListenerForMeasuresList(int index) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                ListElementWrapper elem = (ListElementWrapper)measuresListModel.get(index);
                if (elem.getType() == ListElementType.GLOBAL_TABLE){
                    return;
                }
                GlobalColumnData col = (GlobalColumnData) elem.getObj();
                GlobalTableData table = getTableInRowIndex(measuresListModel, index);
                measuresListModel.removeElementAt(index);

                //remove table group if last element or no other elements in that group
                ListElementWrapper el = (ListElementWrapper) measuresListModel.getElementAt(measuresListModel.size()-1);
                if (measuresListModel.size() == 1 && !countAllAdded)
                    measuresListModel.removeElementAt(0);
                else if (measuresListModel.size() == 2 && countAllAdded)
                    measuresListModel.removeElementAt(1);
                //check if last element is a table
                else if (el.getType() == ListElementType.GLOBAL_TABLE ){
                    measuresListModel.removeElementAt(measuresListModel.size()-1);
                }
                else{
                    //iterate all tables and check if any tabe has no groups and remove it. Empty tables are followed together.
                    for (int i = 1; i < measuresListModel.size(); i++){
                        ListElementWrapper e1 = (ListElementWrapper) measuresListModel.getElementAt(i-1);
                        ListElementWrapper e2 = (ListElementWrapper) measuresListModel.getElementAt(i);
                        if (e1.getType()==ListElementType.GLOBAL_TABLE && e2.getType()==ListElementType.GLOBAL_TABLE){
                            measuresListModel.removeElementAt(i-1);
                            break; //end here, because only one emoty table can exist
                        }

                    }

                }

                if (elem.getType() == ListElementType.MEASURE){
                    globalTableQueries.removeMeasure(col);
                    //no more measures in facts tale group, delete it
                    /*ListElementWrapper e = (ListElementWrapper) measuresListModel.getElementAt(1);
                    if (e.getType()==ListElementType.GLOBAL_TABLE)
                        measuresListModel.removeElementAt(0);*/
                }
                else if (elem.getType() == ListElementType.GLOBAL_COLUMN){
                    globalTableQueries.deleteSelectRowFromTable(table, col);
                }
                aggregationsList.updateUI();
                aggregationsList.revalidate();
            }
        };
    }

    private ActionListener getChangeAggregateActionListener(int index, String aggregate, DefaultListModel model, JList list) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (index < 0)
                    return;
                ListElementWrapper element = (ListElementWrapper) model.getElementAt(index);
                if (element.getType() == ListElementType.GLOBAL_TABLE){
                    return;//a table was selected, do nothing
                }
                GlobalColumnData c = (GlobalColumnData) element.getObj();
                GlobalTableData t = getTableInRowIndex(measuresListModel, index);
                String oldAggr = c.getAggrOp();
                //update query
                if (element.getType() == ListElementType.GLOBAL_COLUMN){
                    if(globalTableQueries.rowExists(t,c, aggregate)){
                        JOptionPane.showMessageDialog(mainMenu, "The same attribute already exists with the selected aggregate. Please select another aggregate function, or remove the other attribute", "Cannot add measure", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    globalTableQueries.updateSelectRowFromTable(t, c, oldAggr, aggregate);
                }
                else if (element.getType() == ListElementType.MEASURE){
                    if(globalTableQueries.measureExists(c, aggregate)){
                        JOptionPane.showMessageDialog(mainMenu, "Measure already present. Cannot add repeated Measure with same Aggregate Function.", "Cannot add measure", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    globalTableQueries.addAndReplaceMeasureAggrOP(c, oldAggr, aggregate);
                }
                c.setAggrOp(aggregate);
                element.setObj(c);
                element.setName("    " + c.getAggrOpName());

                model.setElementAt(element, index);
                list.revalidate();
                list.updateUI();
            }
        };
    }

    private ActionListener getChangeAggregateDistinctActionListener(int index, DefaultListModel model, JList list) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                //adds/removes the distinct clause
                if (index < 0)
                    return;
                ListElementWrapper element = (ListElementWrapper) model.getElementAt(index);
                if (!element.getName().contains("    ")){
                    return;//a table was selected, do nothing
                }
                GlobalColumnData c = (GlobalColumnData) element.getObj();
                GlobalTableData t = getTableInRowIndex(measuresListModel, index);
                String oldAggr = c.getAggrOp();
                //update query
                if (element.getType() == ListElementType.GLOBAL_COLUMN){
                    c.changeDistinct();
                    globalTableQueries.updateSelectRowFromTable(t, c, oldAggr, c.getAggrOp());
                }
                else if (element.getType() == ListElementType.MEASURE){
                    globalTableQueries.removeMeasure(c);
                    c.changeDistinct();
                    globalTableQueries.addMeasure(c);
                }

                element.setObj(c);
                element.setName("    " + c.getAggrOpName());
                model.setElementAt(element, index);
                list.revalidate();
                list.updateUI();
            }
        };
    }
    private ActionListener addRemoveCountAll(DefaultListModel model, JList list) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                //adds/removes count all
                if(countAllAdded){
                    //remove it
                    model.removeElementAt(0);
                    globalTableQueries.setCountAll(false);
                    countAllAdded = false;
                }
                else{
                    //add it
                    if (model.size() == 0)
                        model.addElement(new ListElementWrapper("Count(*)", null, ListElementType.COUNTALL));
                    else
                        model.insertElementAt(new ListElementWrapper("Count(*)", null, ListElementType.COUNTALL), 0);
                    globalTableQueries.setCountAll(true);
                    countAllAdded = true;
                }
                list.revalidate();
                list.updateUI();
            }
        };
    }

    private ActionListener getAddNOTActionListenerOnNestedExprssion(JTree tree, FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    FilterNode notNode = new FilterNode("NOT", "NOT", FilterNodeType.BOOLEAN_OPERATION);
                    List <FilterNode> childNodes = getAllChildNodes(node);
                    //remove all childs, make a new child called not and have all childs be child of not node
                    for (FilterNode n : childNodes)
                        node.remove(n);
                    node.add(notNode);
                    for (FilterNode n : childNodes)
                        notNode.add(n);
                    tree.expandPath(new TreePath(notNode.getPath()));
                    tree.repaint();
                    tree.updateUI();
                }
            }
        };
    }

    private ActionListener getAddNOTActionListener(JTree tree, FilterNode node) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    parent.remove(node);
                    FilterNode notNode = new FilterNode("NOT", "NOT", FilterNodeType.BOOLEAN_OPERATION);
                    notNode.add(node);
                    parent.insert(notNode, index);
                    tree.expandPath(new TreePath(notNode.getPath()));
                    tree.repaint();
                    tree.updateUI();
                }
            }
        };
    }


    private ActionListener getRemoveFilterNodActionListener(JTree tree, DefaultTreeModel treeModel, FilterNode node, boolean isAggrFilter) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {//TODO: bug when removing first filter, boolean op must be deleted and is not (what if a sub condition exists..)
                if(node != null){
                    //remove node
                    FilterNode parent = (FilterNode) node.getParent();
                    int index = parent.getIndex(node);
                    int childCount = parent.getChildCount();
                    if (index > 0){
                        //
                        FilterNode nodeAbove = (FilterNode) parent.getChildAt(index-1);
                        if (nodeAbove.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            treeModel.removeNodeFromParent(nodeAbove);
                        }
                    }
                    else if (index == 0 && childCount > 1){
                        //if there is a boolean operation below, remove it
                        FilterNode nodeBellow = (FilterNode) parent.getChildAt(index+1);
                        if (nodeBellow.getNodeType() == FilterNodeType.BOOLEAN_OPERATION){
                            treeModel.removeNodeFromParent(nodeBellow);
                        }
                    }
                    treeModel.removeNodeFromParent(node);
                    GlobalColumnData c = (GlobalColumnData) node.getObj();
                    if (!isAggrFilter)
                        if (tree.equals(rowFilterTree))
                            globalTableQueries.removeFilter(c.getFullName());
                        else if (tree.equals(colFiltersTree))
                            globalTableQueries.removeColFilter(c.getFullName());
                    tree.repaint();
                    tree.updateUI();
                }
            }
        };
    }

    private ActionListener changeBooleanOperation(JTree tree, String booleanOp, FilterNode selectedNode) {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //FilterNode node = (FilterNode) parentNode.getChildAt(index);
                selectedNode.setUserObject(booleanOp);
                tree.repaint();
                tree.updateUI();
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

    private List<FilterNode> getAllChildNodes(FilterNode node){
        List<FilterNode> nodes = new ArrayList<>();
        int nChilds = node.getChildCount();
        for (int i = 0; i < nChilds; i++){
            FilterNode child = (FilterNode) node.getChildAt(i);
            nodes.add(child);
        }

        return nodes;
    }

                                              /**
     * In the list of rows, returns the table name that contains the column with the index specified
     * @param index
     * @return
     */
    private GlobalTableData getTableInRowIndex(DefaultListModel model, int index){
        for (int i = index; i >=0; i--){
            ListElementWrapper elem = (ListElementWrapper) model.get(i);
            if (elem.getType() == ListElementType.GLOBAL_TABLE){//first table to appear belongs to this
                return (GlobalTableData) elem.getObj();
            }
        }
        return null;
    }

    private void expandAllStarSchema(TreePath path, boolean expand) {
        CustomTreeNode node = (CustomTreeNode) path.getLastPathComponent();
        if (node.getNodeType() == NodeType.GLOBAL_COLUMN || node.getNodeType() == NodeType.MEASURE)
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

    private void expandAllFilterNodes(JTree tree, TreePath path, boolean expand) {
        FilterNode node = (FilterNode) path.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                FilterNode n = (FilterNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);

                expandAllFilterNodes(tree, p, expand);
            }
        }

        if (expand) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }

    public String getFilterQuery(boolean isAggrFilter){
        DefaultTreeModel treeModel = null;
        JTextArea textArea = null;
        if (isAggrFilter) {
            treeModel = aggrFilterTreeModel;
            textArea = editFiltersAggr;
        }
        else {
            treeModel = rowFilterTreeModel;
            textArea = editFilters;
        }
        if (textArea != null && textArea.isVisible()) { //manual edit mode
            String filterText = textArea.getText().replaceAll(";", "");
            filterText = filterText.replaceAll("(?i)having", "");//case insensitive
            filterText = filterText.replaceAll("(?i)where", "");
            if (!isAggrFilter){
                TableColumnNameExtractor ext = new TableColumnNameExtractor();
                globalTableQueries.setFilters(ext.getColumnsFromStringSet(textArea.getText()));
            }
            return filterText;
        }
        //normal mode
        if (treeModel == null) //easy fix
            return "";
        FilterNode root = (FilterNode) treeModel.getRoot();
        int nChilds = root.getChildCount();
        if (nChilds <= 0){
            return "";
        }
        String query = "";
        for (int i = 0 ; i < nChilds; i++){
            FilterNode filterNode = (FilterNode) root.getChildAt(i);
            query += filterNode.getUserObject().toString() +" ";
            if (!isAggrFilter && filterNode.getNodeType() == FilterNodeType.CONDITION){
                GlobalColumnData c = (GlobalColumnData) filterNode.getObj();
                globalTableQueries.addFilter(c.getFullName());
            }
            query += processInnerExpressions(filterNode, isAggrFilter);
        }
        System.out.println("Filter query: "+query);
        return query;
    }

    public String getColFilterQuery(){
        DefaultTreeModel treeModel = colFilterTreeModel;
        JTextArea textArea = editColFilters;


        if (textArea != null && textArea.isVisible()) { //manual edit mode
            String filterText = textArea.getText().replaceAll(";", "");
            filterText = filterText.replaceAll("(?i)where", "");
            TableColumnNameExtractor ext = new TableColumnNameExtractor();
            globalTableQueries.setColFilters(ext.getColumnsFromStringSet(textArea.getText()));
            return filterText;
        }
        //normal mode
        if (treeModel == null) //easy fix
            return "";
        FilterNode root = (FilterNode) treeModel.getRoot();
        int nChilds = root.getChildCount();
        if (nChilds <= 0){
            return "";
        }
        String query = "";
        for (int i = 0 ; i < nChilds; i++){
            FilterNode filterNode = (FilterNode) root.getChildAt(i);
            query += filterNode.getUserObject().toString() +" ";
            if (filterNode.getNodeType() == FilterNodeType.CONDITION){
                GlobalColumnData c = (GlobalColumnData) filterNode.getObj();
                globalTableQueries.addColFilter(c.getFullName());
            }
            query += processInnerExpressions(filterNode, false);
        }
        System.out.println("Col Filter query: "+query);
        return query;
    }

    private String processInnerExpressions(FilterNode filterNode, boolean isAggrFilters){
        String query = "";
        if (filterNode.getChildCount() > 0){
            query +="(";
            for (int j = 0 ; j < filterNode.getChildCount(); j++){
                FilterNode innerFilterNode = (FilterNode) filterNode.getChildAt(j);
                if (!isAggrFilters && innerFilterNode.getNodeType() == FilterNodeType.CONDITION){
                    GlobalColumnData c = (GlobalColumnData) innerFilterNode.getObj();
                    globalTableQueries.addFilter(c.getFullName());
                }

                query += innerFilterNode.getUserObject().toString()+" ";
                query += processInnerExpressions(innerFilterNode, isAggrFilters);
            }
            query +=")";
        }
        return query;
    }

    private boolean filterTableExistsInRows(){
        if (globalTableQueries.getFilters().size() == 0)
            return true;
        for (String s : globalTableQueries.getFilters()){
            String tableName = s.split("\\.")[0];
            //String columnName = s.split("\\.")[1];
            boolean isSelected = false;
            //check the tables on rows selected
            for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rows : globalTableQueries.getSelectRows().entrySet()){
                GlobalTableData gt = rows.getKey();
                if (gt.getTableName().equals(tableName)){
                    isSelected = true;
                    break;
                }
            }
            if (globalTableQueries.getMeasures().size()>0 || globalTableQueries.getManualMeasures().size()>0){ //if measure table has filters, check if it is selected
                if (tableName.equals(starSchema.getFactsTable().getGlobalTable().getTableName()))
                    isSelected = true;
            }
            //check the tables on columns selected
            /*for (Map.Entry<GlobalTableData, List<GlobalColumnData>> rows : globalTableQueries.getSelectColumns().entrySet()){
                GlobalTableData gt = rows.getKey();
                if (gt.getTableName().equals(tableName)){
                    isSelected = true;
                    break;
                }
            }*/
            if (!isSelected){//this filter tables is not selected in the rows
                return false;
            }
        }
        return true;//all filter tables are seleced in the rows or measure
    }

    public void executeQueryAndShowResults(){
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws InterruptedException {
                DateTime beginTime = new DateTime();
                System.out.println("Building query... ");
                long startTime = System.currentTimeMillis();

                String localQuery = buildQuery(true);//create query with inner query to get local table data

                long endTime = System.currentTimeMillis();
                System.out.println("Query Build took: " + (endTime - startTime) + " milliseconds");
                System.out.println(localQuery);
                if (localQuery.contains("Error")){
                    JOptionPane.showMessageDialog(null, "Could not execute query:\n"+localQuery, "Query Error", JOptionPane.ERROR_MESSAGE);
                    queryStatusLogModel.addElement(new QueryLog(localQuery, beginTime, null, 0));
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                    backButton.setEnabled(true);
                    return null;
                }
                //execute query by presto
                firePropertyChange("querying", null, null);
                System.out.println("Querying and Retrieving results... ");
                startTime = System.currentTimeMillis();
                ResultSet results = prestoMediator.getLocalTablesQueries(localQuery);

                if (results == null){
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                    backButton.setEnabled(true);
                    //JOptionPane.showMessageDialog(mainMenu, "Query returned with no results. Check if Presto is running and\nthat the data source is also available.",
                     //       "Query empty", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                //process query results
                //firePropertyChange("results_processing", null, null);
                setResultsAndCreateLog(results, localQuery, beginTime);
                endTime = System.currentTimeMillis();
                System.out.println("Query and Results Processing took: " + (endTime - startTime) + " milliseconds");

                LoadingScreenAnimator.closeGeneralLoadingAnimation();
                backButton.setEnabled(true);
                return null;
            }

            protected void done() {
                try {
                    //System.out.println("Done");
                    get();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    String msg = String.format("Unexpected problem: %s",
                            e.getCause().toString());
                    //JOptionPane.showMessageDialog(mainMenu,
                     //       msg, "Error", JOptionPane.ERROR_MESSAGE);
                    LoadingScreenAnimator.closeGeneralLoadingAnimation();
                } catch (InterruptedException e) {
                    // Process e here
                }
            }
        };
        LoadingScreenAnimator.openGeneralLoadingAnimation(mainMenu, "Constructing Query...");
        worker.execute();
        worker.addPropertyChangeListener( new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("build_query".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Constructing Query...");
                }
                else if ("querying".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Executing Query and Processing Results...");
                }
                else if ("results_processing".equals(evt.getPropertyName())) {
                    LoadingScreenAnimator.setText("Processing Query Results...");
                }
            }
        });
        //backButton.setEnabled(false);
    }

    private String buildQuery(boolean includeInnerQueries){
        //validate query string
        if (manualAggregations.isVisible()){
            globalTableQueries.setManualAggregationsStr(manualAggregations.getText());
            TableColumnNameExtractor tExtractor = new TableColumnNameExtractor();
            globalTableQueries.setManualRowsAndMeasuresAggr(tExtractor.getSchemaObjectsFromSQLText(manualAggregations.getText(), starSchema));
        }

        String filterQuery = getFilterQuery(false);
        if (!filterQuery.equals("ERROR"))
            globalTableQueries.setFilterQuery(filterQuery);

        String filterQueryAggr = getFilterQuery(true);

        String colFilterQuery = getColFilterQuery();
        if (!filterQuery.equals("ERROR"))
            globalTableQueries.setColFilterQuery(colFilterQuery);

        if (!filterQueryAggr.equals("ERROR"))
            globalTableQueries.setFilterAggrQuery(filterQueryAggr);
        if (!filterTableExistsInRows()){
            LoadingScreenAnimator.closeGeneralLoadingAnimation();
            backButton.setEnabled(true);
            //JOptionPane.showMessageDialog(mainMenu, "There is one or more columns in filters \n not selected in the rows area.", "Invalid Query", JOptionPane.ERROR_MESSAGE);
            globalTableQueries.clearAllFilters();
            return "Error: There is one or more columns in filters \n not selected in the rows area.";
        }
        return globalTableQueries.buildQuery(includeInnerQueries);//create query with inner query to get local table data
    }

    private void setResultsAndCreateLog(ResultSet results, String localQuery, DateTime beginTime){
        if (results == null)
            return;
        //defaultTableModel.setColumnCount(0);
        //defaultTableModel.setRowCount(0);//clear any previous results

        IModelFieldGroup[] cols = null;
        ModelData data = null;

        //insert column results in JTable
        int nRows = 0;
        try {
            ResultSetMetaData rsmd = results.getMetaData();
            int columnCount = rsmd.getColumnCount() + 1;
            ;//+1 for line col

            List<List<String>> pivots = globalTableQueries.getPivotValues();
            //pivots.clear();
            List<ModelRow> rows = new ArrayList<>();
            if (pivots.size() > 0 && pivots.get(0).size() > 1) {//if there are pivoted columns and the number of columns that were pivot is biggger than one, then it is necessary to group multiple column headers
                data = createMultiHeaders(pivots, rsmd, columnCount, results);
            }
            else { //no pivoted columns or only one pivoted column, there is only one level of column headers
                cols = new IModelFieldGroup[columnCount];
                cols[0] = new ModelField(" ", " ");
                for (int i = 1; i < columnCount; i++) {
                    String name = rsmd.getColumnName(i);
                    cols[i] = new ModelField(name+i, name);
                }
                data = new ModelData(cols);
                //place rows
                //results.beforeFirst(); //return to begining
                while (results.next()) {
                    //Fetch each row from the ResultSet, and add to ArrayList of rows
                    rows.add(new ModelRow(columnCount));
                    rows.get(nRows).setValue(0, (nRows + 1) + "");
                    for (int i = 0; i < columnCount - 1; i++) {
                        //Again, note that ResultSet column indices start at 1
                        //currentRow[i+1] = results.getString(i+1);//first column (index 0) is the line number)
                        rows.get(nRows).setValue(i + 1, results.getString(i + 1));//first column (index 0) is the line number)
                    }
                    nRows++;
                    //defaultTableModel.addRow(rows);
                }
                ModelRow[] rowsArray = new ModelRow[rows.size()];
                rowsArray = rows.toArray(rowsArray);
                data.setRows(rowsArray);
            }
            if (data == null){
                JOptionPane.showMessageDialog(mainMenu, "Error processing query results.", "Error", JOptionPane.ERROR_MESSAGE);
                LoadingScreenAnimator.closeGeneralLoadingAnimation();
                backButton.setEnabled(true);
                return;
            }
            //add elements to table and add table to scrollpane
            queryResultsTableGroupable = new JBroTable(data);
            queryResultsTableGroupable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//maintain column width
            tablePane.setViewportView(queryResultsTableGroupable);
            DefaultTableCellRenderer rendar1 = new DefaultTableCellRenderer();
            rendar1.setBackground(new Color(238, 238, 238));//same color on line rows as header
            queryResultsTableGroupable.getColumnModel().getColumn(0).setCellRenderer(rendar1);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            LoadingScreenAnimator.closeGeneralLoadingAnimation();
            backButton.setEnabled(true);
            return;
        }
        DateTime endTime = new DateTime();
        queryStatusLogModel.addElement(new QueryLog(localQuery, beginTime, endTime, nRows));
        queryResultsTableGroupable.revalidate();
    }

    private ModelData createMultiHeaders(List<List<String>> pivotValues, ResultSetMetaData rsmd, int columnCount, ResultSet results) throws SQLException {
        List<IModelFieldGroup> cols = new ArrayList<>();
        cols.add(new ModelField( " ", " " ));
        int nNonPivotTables = Math.abs(columnCount - (pivotValues.size() +1));//get number of columns from elements that are not pivoted columns
        int nNonPivotTablesAndLineCOl = nNonPivotTables+1;
        //nNonPivotTables++;//add the column with line numbers
        //add column names of non pivoted columns
        for (int i = 1; i <= nNonPivotTables; i++) {//start iterating on first pivot column
            String name = rsmd.getColumnName(i);
            cols.add(new ModelField( name, name ));
        }

        int nLevels =  pivotValues.get(0).size();

        //for all list of values group similar values at header 0 add only different values
        cols.add(new ModelFieldGroup(pivotValues.get(0).get(0), pivotValues.get(0).get(0)));
        for (int i = 1; i < pivotValues.size(); i++){//start on the second values list, fisrt already inserted
            String value = pivotValues.get(i).get(0);
            if (!cols.get(cols.size()-1).getCaption().equals(value)){//check of previous value has same value. if it does not, add new value
                cols.add(new ModelFieldGroup(value, value));
            }
        }

        //2nd level of values if there are 3 levels if it exists
        if (nLevels == 3) {
            for (int i = 0; i < pivotValues.size(); i++) {//start on the second values list, fisrt already inserted
                String value = pivotValues.get(i).get(1);//2nd value of each list
                String parentValue = pivotValues.get(i).get(0);//value that appears on same list, one level up
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking for the parent after the 'one level columns'
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    //fieldParent = (ModelFieldGroup) fieldParent.getChild(parentValue);
                    if (fieldParent == null){
                        continue;
                    }
                    if (fieldParent.getCaption().equals(parentValue)) {
                        ModelFieldGroup childField = (ModelFieldGroup) fieldParent.getChild(value);
                        if (childField == null){
                            ModelFieldGroup secondLevelField = new ModelFieldGroup(value+i, value);
                            fieldParent.withChild(secondLevelField);//add this value as child
                            break;
                        }
                        String child = childField.getCaption();
                        if (child != null && child.equals(value)){//this value is already child, move to next value
                            break;
                        }
                    }
                }
            }
        }

        int lastLevelIndex = nLevels-1;
        //do the same but for last elements, (leaf headers) and put them in respective parents. These are either the 3rd or 2nd level of headers
        for (int i = 0; i < pivotValues.size(); i++) {
            String value = pivotValues.get(i).get(lastLevelIndex);
            ModelField leafField = new ModelField(value + i, value);
            if (nLevels == 3) {//3 header levels
                String parentSecondHeaderValue = pivotValues.get(i).get(1);
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking after the 'one level columns'
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    String valueTopHeader = pivotValues.get(i).get(0);
                    if (fieldParent.getCaption().equals(valueTopHeader)){
                        fieldParent = (ModelFieldGroup) fieldParent.getChild(parentSecondHeaderValue+i);//get child requires IDENTIFIER AND NOT CAPTION!!
                        if (fieldParent != null) {
                            fieldParent.withChild(leafField);
                            break;
                        }
                    }
                }
            } else if (nLevels == 2) {
                String parentValue = pivotValues.get(i).get(lastLevelIndex - 1);
                for (int j = nNonPivotTablesAndLineCOl ; j < cols.size(); j++) {//start looking after the 'one level columns'
                    //only 2 column headers
                    ModelFieldGroup fieldParent = (ModelFieldGroup) cols.get(j);
                    if (fieldParent.getCaption().equals(parentValue)) {
                        fieldParent.withChild(leafField);
                    }
                }
            }
        }

        IModelFieldGroup[] colsArray = new IModelFieldGroup[cols.size()];
        colsArray = cols.toArray(colsArray);

        List<ModelRow> rows = new ArrayList<>();
        ModelData data = null;
        try{ data = new ModelData(colsArray);}
        catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }

        int nRows = 0;

        while (results.next()) {
            //Fetch each row from the ResultSet, and add to ArrayList of rows
            rows.add(new ModelRow(columnCount));
            rows.get(nRows).setValue(0, (nRows + 1) + "");
            for (int i = 0; i < columnCount - 1; i++) {
                //Again, note that ResultSet column indices start at 1
                //currentRow[i+1] = results.getString(i+1);//first column (index 0) is the line number)
                rows.get(nRows).setValue(i + 1, results.getString(i + 1));//first column (index 0) is the line number)
            }
            nRows++;
            //defaultTableModel.addRow(rows);
            ModelRow[] rowsArray = new ModelRow[rows.size()];
            rowsArray = rows.toArray(rowsArray);
            data.setRows(rowsArray);
        }

        return data;
    }


    public boolean exportResultsToCSV() {
        ModelData data = queryResultsTableGroupable.getData();
        if (data == null){
            JOptionPane.showMessageDialog(mainMenu, "No results in table, nothing to save.", "Nothing to save", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        File pathToExportTo = null;
        JFileChooser f = new JFileChooser();
        //f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        f.setFileFilter(new FileNameExtensionFilter("CSV", ".csv"));
        int returnVal = f.showSaveDialog(mainMenu);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            pathToExportTo = f.getSelectedFile();
            if (pathToExportTo == null){
                return false;
            }
            if (!pathToExportTo.toString().endsWith(".csv")){
                pathToExportTo = new File(pathToExportTo.toString()+".csv");
            }
            try {
                FileWriter csv = new FileWriter(pathToExportTo);


                //column names
                ModelField[] fields = data.getFields();
                for (int i = 0; i < fields.length; i++) {
                    ModelField leafField = fields[i];
                    String fieldStr = leafField.getCaption();
                    if (leafField.getParent() != null){//check if theres a parent, a higher level header
                        ModelFieldGroup groupField = leafField.getParent();
                        fieldStr = groupField.getCaption() +" - "+fieldStr;
                        if (groupField.getParent() != null) {//check if theres a parent, a higher level header (considering no more than 3 levels of headers!)
                            fieldStr = groupField.getParent().getCaption() + " - " + fieldStr;
                        }
                    }
                    csv.write(fieldStr+",");
                }

                csv.write("\n");
                ModelRow[] rows = data.getRows();
                for (int i = 0; i < rows.length; i++) {
                    Object[] values = rows[i].getValues();
                    for (int j = 0; j < values.length; j++) {
                        csv.write(values[j].toString() + ",");
                    }
                    csv.write("\n");
                }

                csv.close();
                JOptionPane.showMessageDialog(mainMenu, "Results exported to "+pathToExportTo+"!", "Success", JOptionPane.PLAIN_MESSAGE);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JOptionPane.showMessageDialog(mainMenu, "Could not export results", "Failed export", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private MouseListener getMouseListenerForColumnList() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                if (SwingUtilities.isRightMouseButton(arg0)){
                    int index = rowsList.locationToIndex(arg0.getPoint());
                    if (index < 0)
                        return;
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

    private MouseListener getMouseListenerForRowsList() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                //if (SwingUtilities.isRightMouseButton(arg0)){
                int index = rowsList.locationToIndex(arg0.getPoint());
                if (index < 0)
                    return;
                if (!rowsListModel.getElementAt(index).toString().contains("    ")){
                    rowsList.setComponentPopupMenu(null);
                    return;
                }
                rowsList.setSelectedIndex(index);
                JPopupMenu menu = new JPopupMenu();
                JMenuItem item1 = new JMenuItem("Delete");
                JMenu subMenu = new JMenu("Order by");
                JMenuItem subItem = new JMenuItem("Ascending");
                JMenuItem subItem2 = new JMenuItem("Descending");
                JMenuItem subItem3 = new JMenuItem("No order");
                subMenu.add(subItem);
                subMenu.add(subItem2);
                subMenu.add(subItem3);
                item1.addActionListener(getRemoveActionListenerForRowsList(rowsList, rowsListModel, index));
                subItem.addActionListener(getAddOrderByListenerRows(index, true, false));
                subItem2.addActionListener(getAddOrderByListenerRows(index, false, false));
                subItem3.addActionListener(getRemoveOrderByListenerRows(index));
                //item1.addActionListener(getRemoveActionListener());
                menu.add(item1);
                menu.add(subMenu);
                rowsList.setComponentPopupMenu(menu);
                //}
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForMeasuresList() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                int index = aggregationsList.locationToIndex(arg0.getPoint());

                //add (add count(*) ) option even if no element selected
                //count *
                String menuCountTitle = "";
                if (countAllAdded){
                    menuCountTitle = "Remove Count(*)";
                }
                else
                    menuCountTitle = "Add Count(*)";
                JPopupMenu menu = new JPopupMenu();
                JMenuItem countAllItem = new JMenuItem(menuCountTitle);
                countAllItem.addActionListener(addRemoveCountAll(measuresListModel, aggregationsList));

                JMenuItem manualEditItem = new JMenuItem("Change to Manual Edit");
                manualEditItem.addActionListener(changeToManualEditAggr());
                if (index < 0) {
                    menu.add(countAllItem);
                    menu.add(manualEditItem);
                    aggregationsList.setComponentPopupMenu(menu);
                    return;
                }
                aggregationsList.setSelectedIndex(index);
                ListElementWrapper elem = (ListElementWrapper)measuresListModel.get(index);
                if (elem.getType() == ListElementType.GLOBAL_TABLE){
                    return;
                }
                JMenuItem item1 = new JMenuItem("Delete");
                item1.addActionListener(getRemoveActionListenerForMeasuresList(index));
                menu.add(item1);

                JMenu subMenu = new JMenu("Order by");
                JMenuItem subItem = new JMenuItem("Ascending");
                JMenuItem subItem2 = new JMenuItem("Descending");
                JMenuItem subItem3 = new JMenuItem("No order");
                subMenu.add(subItem);
                subMenu.add(subItem2);
                subMenu.add(subItem3);
                JMenu subMenu2 = new JMenu("Change aggregate");
                JMenuItem subItem20 = new JMenuItem(GROUP_BY_OP);
                JMenuItem subItem21 = new JMenuItem("Count");
                JMenuItem subItem23 = new JMenuItem("Sum");
                JMenuItem subItem24 = new JMenuItem("Average");
                JMenuItem subItem25 = new JMenuItem("Max");
                JMenuItem subItem26 = new JMenuItem("Min");
                subMenu2.add(subItem20);
                subMenu2.add(subItem21);
                subMenu2.add(subItem23);
                subMenu2.add(subItem24);
                //subMenu2.add(subItem25);
                //subMenu2.add(subItem26);
                subItem.addActionListener(getAddOrderByListenerRows(index, true, true));
                subItem2.addActionListener(getAddOrderByListenerRows(index, false, true));
                subItem3.addActionListener(getRemoveOrderByListenerRows(index));
                subItem20.addActionListener(getChangeAggregateActionListener(index, GROUP_BY_OP, measuresListModel, aggregationsList));
                subItem21.addActionListener(getChangeAggregateActionListener(index, "COUNT", measuresListModel, aggregationsList));
                subItem23.addActionListener(getChangeAggregateActionListener(index, "SUM", measuresListModel, aggregationsList));
                subItem24.addActionListener(getChangeAggregateActionListener(index, "AVG", measuresListModel, aggregationsList));
                subItem25.addActionListener(getChangeAggregateActionListener(index, "MAX", measuresListModel, aggregationsList));//not added
                subItem26.addActionListener(getChangeAggregateActionListener(index, "MIN", measuresListModel, aggregationsList));//not added
                //add/remove distinct
                String menuDistinctTitle = "";
                String selectedElem = measuresListModel.getElementAt(index).toString();
                if (selectedElem.contains("DISTINCT")){
                    menuDistinctTitle = "Remove DISTINCT";
                }
                else
                    menuDistinctTitle = "Add DISTINCT";
                JMenuItem item3 = new JMenuItem(menuDistinctTitle);
                item3.addActionListener(getChangeAggregateDistinctActionListener(index, measuresListModel, aggregationsList));

                //item1.addActionListener(getRemoveActionListener());
                menu.add(item1);
                menu.add(subMenu);
                menu.add(subMenu2);
                if (selectedElem.contains("SUM") ||selectedElem.contains("AVG") || selectedElem.contains("COUNT"))//cannot add distinct if current attribute does not have aggregate function
                    menu.add(item3);
                menu.add(countAllItem);
                menu.add(manualEditItem);
                aggregationsList.setComponentPopupMenu(menu);
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForFilterTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                TreePath pathForLocation = rowFilterTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                handleFilterClickMenus(rowFilterTree, rowFilterTreeModel, pathForLocation, false);
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerForColsFilterTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                TreePath pathForLocation = colFiltersTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                handleFilterClickMenus(colFiltersTree, colFilterTreeModel, pathForLocation, false);
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerEditFilterTextArea() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Change to Normal Edit");
                editItem.addActionListener(changeToNormalEditFilter());
                menu.add(editItem);
                editFilters.setComponentPopupMenu(menu);
                super.mousePressed(arg0);
            }
        };
    }


    private MouseListener getMouseListenerEditFilterAggrTextArea() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Change to Normal Edit");
                editItem.addActionListener(changeToNormalEditFilterAggr());
                menu.add(editItem);
                editFiltersAggr.setComponentPopupMenu(menu);
                super.mousePressed(arg0);
            }
        };
    }

    private MouseListener getMouseListenerEditAggrTextArea() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem editItem = new JMenuItem("Change to Normal Edit");
                editItem.addActionListener(changeToNormalEditAggr());
                menu.add(editItem);
                manualAggregations.setComponentPopupMenu(menu);
                super.mousePressed(arg0);
            }
        };
    }

    private void handleFilterClickMenus(JTree tree, DefaultTreeModel model, TreePath pathForLocation, boolean isAggrFilter){
        tree.setSelectionPath(pathForLocation);
        FilterNode selectedNode = null;
        JPopupMenu menu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Manual Edit");
        if (isAggrFilter)
            editItem.addActionListener(changeToManualEditFilterAggr());
        else
            editItem.addActionListener(changeToManualEditFilter());
        if(pathForLocation != null) {
            selectedNode = (FilterNode) pathForLocation.getLastPathComponent();
            if (selectedNode.getNodeType() == FilterNodeType.CONDITION) {
                //menu for a condition
                JMenuItem item1 = new JMenuItem("Delete");
                JMenuItem item2 = new JMenuItem("NOT");
                item1.addActionListener(getRemoveFilterNodActionListener(tree, model, selectedNode, isAggrFilter));
                item2.addActionListener(getAddNOTActionListener(tree, selectedNode));
                menu.add(item1);
                menu.add(item2);
            } else if (selectedNode.getNodeType() == FilterNodeType.BOOLEAN_OPERATION) {
                //menu for a boolean op
                JMenuItem item1 = new JMenuItem("AND");
                JMenuItem item2 = new JMenuItem("OR");
                JMenuItem item3 = new JMenuItem("NOT on nested expression");
                item1.addActionListener(changeBooleanOperation(tree, "AND", selectedNode));
                item2.addActionListener(changeBooleanOperation(tree, "OR", selectedNode));
                item3.addActionListener(getAddNOTActionListenerOnNestedExprssion(tree, selectedNode));
                menu.add(item1);
                menu.add(item2);
                if (selectedNode.getChildCount() > 0)
                    menu.add(item3);
            }
        }
        menu.add(editItem);
        tree.setComponentPopupMenu(menu);
    }

    private MouseListener getMouseListenerForAggFilterTree() {
        return new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent arg0) {
                //if (SwingUtilities.isRightMouseButton(arg0)){
                TreePath pathForLocation = aggrFiltersTree.getPathForLocation(arg0.getPoint().x, arg0.getPoint().y);
                handleFilterClickMenus(aggrFiltersTree, aggrFilterTreeModel, pathForLocation, true);
                //}
                super.mousePressed(arg0);
            }
        };
    }

    private boolean addColumnsToList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
        //first iterate to check if maximum value of columns is achieved:
        int nCols = 0;
        for (int i = 0; i < listModel.getSize(); i++) {
            if (String.valueOf(listModel.getElementAt(i)).contains("    ")){
                nCols++;
            }
        }
        if (nCols >= MAX_SELECT_COLS){
            JOptionPane.showMessageDialog(mainMenu, "Maximum number of columns Reached", "Maximum number of columns Reached.\nDelete columns to add new ones.", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String[] s = null;
        //check if table name of this column exists. If true then inserted here
        ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);
        if (listModel.contains(elemtTosearch)){
            int index = listModel.indexOf(elemtTosearch);
            index++;
            //iterate the columns of this tables. insert a new one
            for (int i = index; i < listModel.getSize(); i++) {
                if (!String.valueOf(listModel.getElementAt(i)).contains("    ")){
                    listModel.add(i, new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
                    globalTableQueries.addSelectColumn(globalTable, globalCol);
                    return true;
                }
            }
            //maybe this table is the last one, insert at last position
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
            globalTableQueries.addSelectColumn(globalTable, globalCol);
        }
        else{
            listModel.addElement(new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE)); //add table name
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add column
            globalTableQueries.addSelectColumn(globalTable, globalCol);
        }
        return true;
    }

    private boolean addRowsToList(DefaultListModel listModel, GlobalColumnData globalCol, GlobalTableData globalTable){
        //check if table name of this column exists. If true then inserted here
        ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);
        if (listModel.contains(elemtTosearch)){
            int index = listModel.indexOf(elemtTosearch);
            index++;
            //iterate the columns of this tables. insert a new one
            for (int i = index; i < listModel.getSize(); i++) {
                if (!String.valueOf(listModel.getElementAt(i)).contains("    ")){
                    listModel.add(i, new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
                    globalTableQueries.addSelectRow(globalTable, globalCol);
                    return true;
                }
            }
            //maybe this table is the last one, insert at last position
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
            globalTableQueries.addSelectRow(globalTable, globalCol);
        }
        else{
            listModel.addElement(new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE)); //add table name
            listModel.addElement(new ListElementWrapper("    "+globalCol.getName(), globalCol, ListElementType.GLOBAL_COLUMN));//add row
            globalTableQueries.addSelectRow(globalTable, globalCol);
        }
        return true;
    }


    private boolean addAggrRow(DefaultListModel listModel, GlobalTableData globalTable, GlobalColumnData attribute, boolean isMeasure){

        if ((attribute.getAggrOp() == null || attribute.getAggrOp().isEmpty()) )//if attribute
            attribute.setAggrOp(aggregationOpComboBox.getSelectedItem().toString(), false);

        if (isMeasure) {
            if(globalTableQueries.measureExists(attribute)){
                JOptionPane.showMessageDialog(mainMenu, "Measure already present. Cannot add repeated measure with same Aggregate Function.", "Cannot add measure", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            //ADD measures group if not present: check if first item is measures title
            int indexForFactsTable = 0;
            if (countAllAdded)
                indexForFactsTable = 1;
            if (listModel.isEmpty())
                listModel.add(indexForFactsTable, new ListElementWrapper("Measures of "+globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE));
            else {
                //add measures table at first or second position (second if count(*) is added)
                ListElementWrapper firstElem = (ListElementWrapper) listModel.getElementAt(indexForFactsTable);
                GlobalTableData t = (GlobalTableData)firstElem.getObj();
                if (!t.equals(globalTable)) {
                    listModel.add(indexForFactsTable, new ListElementWrapper("Measures of "+globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE));
                }
            }
            //add on last element below measures group
            for (int i = (indexForFactsTable+1); i < listModel.size(); i++){
                ListElementWrapper currElem = (ListElementWrapper) listModel.getElementAt(i);
                if (currElem.getType() == ListElementType.GLOBAL_TABLE){
                    listModel.add(i, new ListElementWrapper("    "+attribute.getAggrOpName(), attribute, ListElementType.MEASURE));
                    globalTableQueries.addMeasure(attribute);
                    return true;
                }
            }
            listModel.add(listModel.getSize(), new ListElementWrapper("    "+attribute.getAggrOpName(), attribute, ListElementType.MEASURE));
            globalTableQueries.addMeasure(attribute);
        }
        else{
            //check if table name of this column exists. If true then inserted here
            ListElementWrapper elemtTosearch = new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE);

            if (listModel.contains(elemtTosearch)){
                int index = listModel.indexOf(elemtTosearch);
                ++index;
                //iterate the columns of this tables. insert a new one at the end
                for (int i = index; i < listModel.getSize(); i++) {
                    ListElementWrapper currElem = (ListElementWrapper) listModel.getElementAt(i);
                    if (currElem.getType() == ListElementType.GLOBAL_TABLE){
                        listModel.add(i, new ListElementWrapper("    "+attribute.getAggrOpName(), attribute, ListElementType.GLOBAL_COLUMN));//add row
                        globalTableQueries.addSelectRow(globalTable, attribute);
                        return true;
                    }
                }
                //maybe this table is the last one, insert at last position
                listModel.addElement(new ListElementWrapper("    "+attribute.getAggrOpName(), attribute, ListElementType.GLOBAL_COLUMN));//add row
                globalTableQueries.addSelectRow(globalTable, attribute);

            }
            else{
                listModel.addElement(new ListElementWrapper(globalTable.getTableName(), globalTable, ListElementType.GLOBAL_TABLE)); //add table name
                listModel.addElement(new ListElementWrapper("    "+attribute.getAggrOpName(), attribute, ListElementType.GLOBAL_COLUMN));//add row
                globalTableQueries.addSelectRow(globalTable, attribute);
            }
        }
        return true;
    }

    private ActionListener changeToManualEditAggr(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //if (manualAggregations.isVisible() && !aggregationsList.isVisible())
                    //return;
                    int dialogResult = JOptionPane.showConfirmDialog(mainMenu, "If you switch to manual mode, all current items in this area will be deleted.\n" +
                            "You can type all aggregations needed, including more complex operations not possibloe to create with normal mode.\n" +
                            "However, you cannot drop items from the star schema to this area. To do this you must change back to normal mode on the pop up menu \n" +
                            "Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    changeAggrManualMode();
                }
            }
        };
    }
    private void changeAggrManualMode(){
        //change to edit mode
        String filtersStr = getFilterQuery(false);
        aggregationsList.setVisible(false);
        manualAggregations.setVisible(true);
        measuresListModel.clear();
        //delete all attributes with aggregations on rows select and measures
        globalTableQueries.clearMeasures();
        globalTableQueries.deleteAllRowsWithAggregations();
        aggregationsPane.getViewport().remove(aggregationsList);
        aggregationsPane.getViewport().add(manualAggregations);
        editFilters.setText(filtersStr);
        measuresLabel.setText("Aggregations - Manual Edit Mode");

        aggrAreaPanel.remove(aggregationOpComboBox);
        measuresManualEditLabel.setVisible(true);
        aggrAreaPanel.add(measuresManualEditLabel);
    }

    private ActionListener changeToNormalEditAggr(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //if (!manualAggregations.isVisible() && aggregationsList.isVisible())
                    //return;
                int dialogResult = JOptionPane.showConfirmDialog (mainMenu, "Changing back to normal mode will delete all content on this area.\n Would you like to continue?","Warning",JOptionPane.YES_NO_OPTION);
                boolean goOn = dialogResult == JOptionPane.YES_OPTION;
                if(goOn){
                    //change to normal mode
                    changeAggrNormalMode();
                }
            }
        };
    }

    private void changeAggrNormalMode(){
        //change to normal mode
        manualAggregations.setText("");
        manualAggregations.setVisible(false);
        aggregationsList.setVisible(true);
        aggregationsPane.getViewport().remove(manualAggregations);
        aggregationsPane.getViewport().add(aggregationsList);
        measuresLabel.setText("Aggregations - Normal Edit Mode");
        aggrAreaPanel.remove(measuresManualEditLabel);
        aggrAreaPanel.add(aggregationOpComboBox);
    }

    private ActionListener changeToManualEditFilter(){
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    //if (!rowFilterTree.isVisible() && editFilters.isVisible())
                        //return;
                    int dialogResult = JOptionPane.showConfirmDialog(mainMenu, "If you switch to manual mode, all current filters will be converted to SQL and you can manually edit them.\n" +
                            "However, you cannot drop items from the star schema to these filters.\n To do this you must change back to normal mode on the pop up menu \n" +
                            "Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                    if(dialogResult == JOptionPane.YES_OPTION){
                        changeFilterManualMode();
                    }
                }
            };
    }

    private void changeFilterManualMode(){
        String filtersStr = getFilterQuery(false);
        rowFilterTree.setVisible(false);
        editFilters.setVisible(true);
        filterPane.getViewport().remove(rowFilterTree);
        filterPane.getViewport().add(editFilters);
        editFilters.setText(filtersStr);
        filtersLabel.setText("Filters - Manual Edit Mode");
        globalTableQueries.clearNormalFilters();
    }

    private ActionListener changeToNormalEditFilter(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
               //if (rowFilterTree.isVisible() && !editFilters.isVisible())
                    //return;
                int dialogResult = JOptionPane.showConfirmDialog (mainMenu, "Changing back to normal mode will reset all filters created.\n Would you like to continue?","Warning",JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    //change to normal mode
                    changeFilterNormalMode();
                }

            }
        };
    }

    private void changeFilterNormalMode(){
        editFilters.setVisible(false);
        editFilters.setText("");
        rowFilterTree.setVisible(true);
        clearRowFilters();
        globalTableQueries.clearNormalFilters();
        filterPane.getViewport().remove(editFilters);
        filterPane.getViewport().add(rowFilterTree);
        filtersLabel.setText("Filters - Normal Edit Mode");
    }

    private ActionListener changeToManualEditFilterAggr(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //if (!aggrFiltersTree.isVisible() && editFiltersAggr.isVisible())
                    //return;
                    int dialogResult = JOptionPane.showConfirmDialog(mainMenu, "If you switch to manual mode, all current filters will be converted to SQL and you can manually edit them.\n" +
                            "However, you cannot drop items from the aggregations area to these filters.\n To do this you must change back to normal mode on the pop up menu \n" +
                            "Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    //change to edit mode
                    changeAggrFilterManualMode();
                }

            }
        };
    }

    private void changeAggrFilterManualMode(){
        String filtersStr = getFilterQuery(false);
        aggrFiltersTree.setVisible(false);
        editFiltersAggr.setVisible(true);
        aggrFilterPane.getViewport().remove(aggrFiltersTree);
        aggrFilterPane.getViewport().add(editFiltersAggr);
        editFiltersAggr.setText(filtersStr);
        aggrFiltersLabel.setText("Aggregation Filters - Manual Mode");
    }

    private ActionListener changeToNormalEditFilterAggr(){
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //if (aggrFiltersTree.isVisible() && !editFiltersAggr.isVisible())
                    //return;
                    int dialogResult = JOptionPane.showConfirmDialog(mainMenu, "Changing back to normal mode will reset all filters created.\n Would you like to continue?", "Warning", JOptionPane.YES_NO_OPTION);
                if(dialogResult == JOptionPane.YES_OPTION){
                    changeAggrFilterNormalMode();
                }

            }
        };
    }

    private void changeAggrFilterNormalMode(){
        //change to normal mode
        editFiltersAggr.setVisible(false);
        editFiltersAggr.setText("");
        aggrFiltersTree.setVisible(true);
        clearAggrFilters();
        globalTableQueries.setFilterQuery("");
        aggrFilterPane.getViewport().remove(editFilters);
        aggrFilterPane.getViewport().add(aggrFiltersTree);
        aggrFiltersLabel.setText("Aggregation Filters - Normal Mode");
    }


    public void addGlobalQueryLog(){
        String query = buildQuery(false);
        DateTime currentTime = new DateTime();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
        globalSchemaLogModel.addElement(formatter.print(currentTime)+ " - "+query);
    }

    public void addLocalQueryLog(){
        if (showLocalQueryLog) {
            String query = buildQuery(true);
            DateTime currentTime = new DateTime();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm:ss");
            localSchemaLogModel.addElement(formatter.print(currentTime) + " - " + query);
        }
    }

    class TreeTransferHandler extends TransferHandler {
        DataFlavor[] flavors = new DataFlavor[2];

        public TreeTransferHandler() {
            flavors[0] = new DataFlavor(CustomTreeNode.class, "custom node");
            flavors[1] = new DataFlavor(ListElementWrapper.class, "ListElementWrapper");
        }

        public boolean canImport(TransferHandler.TransferSupport support) {
            if(!support.isDrop()) {
                return false;
            }

            support.setShowDropLocation(true);
            for (DataFlavor f : flavors){
                if (!support.isDataFlavorSupported(f)){
                    return false;
                }
            }
            return true;
        }

        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JTree) {
                JTree tree = (JTree) c;
                TreePath[] paths = tree.getSelectionPaths();
                if (paths != null) {
                    // exportDone after a successful drop.
                    CustomTreeNode node =
                            (CustomTreeNode) paths[0].getLastPathComponent();
                    return new TreeTransferHandler.NodesTransferable(node);
                }
                return null;
            }
            else if (c instanceof JList){
                JList list = (JList) c;
                int index = list.getSelectedIndex();
                if (index > -1){
                    ListElementWrapper elem = (ListElementWrapper) list.getModel().getElementAt(index);
                    return new ListElemTransferable(elem);
                }
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

            try {
                CustomTreeNode data = (CustomTreeNode) t.getTransferData(flavors[0]);
                boolean added = handleNodeTransfer(data, info);
                if (added) {
                    //addGlobalQueryLog();
                    //addLocalQueryLog();
                }
                return added;
            } catch (Exception e) {
                if (e.getLocalizedMessage().contains("custom node")) {
                    //it may be a transfer from list to filter aggregate area
                    try {
                        ListElementWrapper listElem = (ListElementWrapper) t.getTransferData(flavors[1]);
                        boolean added = handleListTransfer(listElem, info);
                        if (added) {
                            //addGlobalQueryLog();
                            //addLocalQueryLog();
                        }
                        return added;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                else{
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        private boolean handleListTransfer(ListElementWrapper listElem, TransferHandler.TransferSupport info){
            if (!listElem.getName().contains("    ")){
                JOptionPane.showMessageDialog(null, "You can only drag and drop columns to create filters.",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            GlobalColumnData column = (GlobalColumnData) listElem.getObj();
            if (info.getComponent() instanceof JTree){
                JTree tree = (JTree) info.getComponent();
                JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
                TreePath dest = dl.getPath();
                if (tree.equals(aggrFiltersTree)){
                    //user drops in the filter aggr tree
                    if (tree.equals(aggrFiltersTree)){
                        return insertAggrFilterNode(dest, column);
                    }

                }
            }
            return false;
        }

        private boolean handleNodeTransfer(CustomTreeNode data, TransferHandler.TransferSupport info){
            if (data.getNodeType() != NodeType.GLOBAL_COLUMN && data.getNodeType() != NodeType.MEASURE){
                JOptionPane.showMessageDialog(null, "You can only drag and drop columns.",
                        "Operation Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            GlobalColumnData column = (GlobalColumnData) data.getObj();
            CustomTreeNode globalTable = (CustomTreeNode) data.getParent();
            GlobalTableData gt = (GlobalTableData) globalTable.getObj();
            column.setFullName(gt.getTableName()+"."+column.getName());
            boolean added = false;

            if (info.getComponent() instanceof JList){
                JList list = (JList) info.getComponent();
                DefaultListModel listModel = (DefaultListModel) list.getModel();
                JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
                int index = dl.getIndex();
                if (index == -1)
                    index = 0;

                CustomTreeNode parentNode = (CustomTreeNode) data.getParent();//global table
                GlobalTableData tab = (GlobalTableData) parentNode.getObj();
                GlobalColumnData col = (GlobalColumnData)data.getObj();

                if (list.equals(columnsList)){
                    //select columns list
                    added = addColumnsToList(listModel, col, tab) ;
                }
                else if (list.equals(rowsList)){
                    //select rows list
                    added = addRowsToList(listModel, col, tab) ;
                }
                else if (list.equals(aggregationsList)){
                    if (data.getNodeType() != NodeType.MEASURE && (globalTableQueries.getMeasures().size() > 1 && globalTableQueries.getSelectColumns().size() > 0)){
                        JOptionPane.showMessageDialog(mainMenu, "You can only use 1 measure when creating a query with pivot attributes.", "1 measure max", JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                    added = addAggrRow(listModel, tab, col, data.getNodeType() == NodeType.MEASURE);
                }
                return added;
            }
            //user drops on jtree
            else if (info.getComponent() instanceof JTree){
                JTree tree = (JTree) info.getComponent();
                JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
                TreePath dest = dl.getPath();
                if (tree.equals(rowFilterTree)){
                    added = insertFilterNode(dest, column);
                }
                else if (tree.equals(colFiltersTree)){
                    added = insertColFilterNode( dest, column);
                }
                return added;
            }
            return false;
        }

        private boolean insertFilterNode(TreePath dest, GlobalColumnData column){
            String s[] = null;
            //user drops in the filter tree
            if (rowFilterTreeModel == null){//filters dropped for the first time (bug if root added on jtree creation, thats why there's two ifs..)
                while (s == null){
                    s = createFilterStringOperation(column, true, false);
                }
                if (s.length == 0)
                    return false;
                //no filters added yet
                FilterNode root = new FilterNode("", null, null);
                root.add(new FilterNode(s[1], column, FilterNodeType.CONDITION));
                rowFilterTreeModel = new DefaultTreeModel(root);
                rowFilterTree.setModel(rowFilterTreeModel);
                rowFilterTree.setRootVisible(false);
                rowFilterTree.revalidate();
                rowFilterTree.updateUI();
                return true;
            }
            FilterNode root = (FilterNode) rowFilterTreeModel.getRoot();
            //decide where to drop
            FilterNode parent;
            if (dest == null)
                parent = root;
            else
                parent = (FilterNode)dest.getLastPathComponent();

            TreePath path = null;
            if (parent.getNodeType() == null && parent.getChildCount() == 0){
                while (s == null){
                    s = createFilterStringOperation(column, true, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                rowFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == null && parent.getChildCount() > 0){
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                rowFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                rowFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                //globalTableQueries.addFilter(column.getFullName());//for validation purposes
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == FilterNodeType.CONDITION ){
                //user wants to create an inner expression OR to add content to inner expression and dragg it to a condition
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                FilterNode boleanNodeParent = (FilterNode) parent.getNextNode();

                if (boleanNodeParent == null || boleanNodeParent.getChildCount() == 0){ //no boolean operator, create it and add child
                    //if creating a new expression nested in a codition, add a boolean operator between the condition and a boolean operator. Make the inner expression child of the boolean operator
                    FilterNode parentOfParent = (FilterNode) parent.getParent();
                    int indexOfParent = parentOfParent.getIndex(parent);
                    FilterNode booleanNode = new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION);
                    rowFilterTreeModel.insertNodeInto(booleanNode, parentOfParent, indexOfParent+1);// create this condition between the condition and the inner expression
                    rowFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), booleanNode, booleanNode.getChildCount());//Must be child of the bolean operator
                    path = new TreePath(booleanNode.getPath());
                }
                else {
                    //inserting on an already existent boolean node with inner expr. IF it has an inner expr, add the operator and cond, else only the cond as childs
                    rowFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), boleanNodeParent, boleanNodeParent.getChildCount());
                    rowFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), boleanNodeParent, boleanNodeParent.getChildCount());
                    path = new TreePath(boleanNodeParent.getPath());
                }
            }
            else if (parent.getNodeType() == FilterNodeType.BOOLEAN_OPERATION && parent.getChildCount()>0){
                //user wants to add content to inner expression and dragg it to the outer boolean operator
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                rowFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                rowFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            rowFilterTree.expandPath(path);
            rowFilterTree.revalidate();
            rowFilterTree.updateUI();
            return true;
        }

        private boolean insertColFilterNode(TreePath dest, GlobalColumnData column){
            String s[] = null;
            //user drops in the filter tree
            if (colFilterTreeModel == null){//filters dropped for the first time (bug if root added on jtree creation, thats why there's two ifs..)
                while (s == null){
                    s = createFilterStringOperation(column, true, false);
                }
                if (s.length == 0)
                    return false;
                //no filters added yet
                FilterNode root = new FilterNode("", null, null);
                root.add(new FilterNode(s[1], column, FilterNodeType.CONDITION));
                colFilterTreeModel = new DefaultTreeModel(root);
                colFiltersTree.setModel(colFilterTreeModel);
                colFiltersTree.setRootVisible(false);
                colFiltersTree.revalidate();
                colFiltersTree.updateUI();
                return true;
            }
            FilterNode root = (FilterNode) colFilterTreeModel.getRoot();
            //decide where to drop
            FilterNode parent;
            if (dest == null)
                parent = root;
            else
                parent = (FilterNode)dest.getLastPathComponent();

            TreePath path = null;
            if (parent.getNodeType() == null && parent.getChildCount() == 0){
                while (s == null){
                    s = createFilterStringOperation(column, true, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //filterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                colFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == null && parent.getChildCount() > 0){
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                colFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                colFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                //globalTableQueries.addFilter(column.getFullName());//for validation purposes
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == FilterNodeType.CONDITION ){
                //user wants to create an inner expression OR to add content to inner expression and dragg it to a condition
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                FilterNode boleanNodeParent = (FilterNode) parent.getNextNode();

                if (boleanNodeParent == null || boleanNodeParent.getChildCount() == 0){ //no boolean operator, create it and add child
                    //if creating a new expression nested in a codition, add a boolean operator between the condition and a boolean operator. Make the inner expression child of the boolean operator
                    FilterNode parentOfParent = (FilterNode) parent.getParent();
                    int indexOfParent = parentOfParent.getIndex(parent);
                    FilterNode booleanNode = new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION);
                    colFilterTreeModel.insertNodeInto(booleanNode, parentOfParent, indexOfParent+1);// create this condition between the condition and the inner expression
                    colFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), booleanNode, booleanNode.getChildCount());//Must be child of the bolean operator
                    path = new TreePath(booleanNode.getPath());
                }
                else {
                    //inserting on an already existent boolean node with inner expr. IF it has an inner expr, add the operator and cond, else only the cond as childs
                    colFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), boleanNodeParent, boleanNodeParent.getChildCount());
                    colFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), boleanNodeParent, boleanNodeParent.getChildCount());
                    path = new TreePath(boleanNodeParent.getPath());
                }
            }
            else if (parent.getNodeType() == FilterNodeType.BOOLEAN_OPERATION && parent.getChildCount()>0){
                //user wants to add content to inner expression and dragg it to the outer boolean operator
                while (s == null){
                    s = createFilterStringOperation(column, false, false);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                colFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                colFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            colFiltersTree.expandPath(path);
            colFiltersTree.revalidate();
            colFiltersTree.updateUI();
            return true;
        }


        private boolean insertAggrFilterNode(TreePath dest, GlobalColumnData column){
            String s[] = null;
            //user drops in the filter tree
            if (aggrFilterTreeModel == null){//filters dropped for the first time (bug if root added on jtree creation, thats why there's two ifs..)
                while (s == null){
                    s = createFilterStringOperation(column, true, true);
                }
                if (s.length == 0)
                    return false;
                //no filters added yet
                FilterNode root = new FilterNode("", null, null);
                root.add(new FilterNode(s[1], column, FilterNodeType.CONDITION));
                aggrFilterTreeModel = new DefaultTreeModel(root);
                aggrFiltersTree.setModel(aggrFilterTreeModel);
                aggrFiltersTree.setRootVisible(false);
                aggrFiltersTree.revalidate();
                aggrFiltersTree.updateUI();
                return true;
            }
            FilterNode root = (FilterNode) aggrFilterTreeModel.getRoot();
            //decide where to drop
            FilterNode parent;
            if (dest == null)
                parent = root;
            else
                parent = (FilterNode)dest.getLastPathComponent();

            TreePath path = null;
            if (parent.getNodeType() == null && parent.getChildCount() == 0){
                while (s == null){
                    s = createFilterStringOperation(column, true, true);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //aggrFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                aggrFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == null && parent.getChildCount() > 0){
                while (s == null){
                    s = createFilterStringOperation(column, false, true);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                aggrFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                aggrFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }
            else if (parent.getNodeType() == FilterNodeType.CONDITION ){
                //user wants to create an inner expression OR to add content to inner expression and dragg it to a condition
                while (s == null){
                    s = createFilterStringOperation(column, false, true);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                FilterNode boleanNodeParent = (FilterNode) parent.getNextNode();

                if (boleanNodeParent == null || boleanNodeParent.getChildCount() == 0){ //no boolean operator, create it and add child
                    //if creating a new expression nested in a codition, add a boolean operator between the condition and a boolean operator. Make the inner expression child of the boolean operator
                    FilterNode parentOfParent = (FilterNode) parent.getParent();
                    int indexOfParent = parentOfParent.getIndex(parent);
                    FilterNode booleanNode = new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION);
                    aggrFilterTreeModel.insertNodeInto(booleanNode, parentOfParent, indexOfParent+1);// create this condition between the condition and the inner expression
                    aggrFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), booleanNode, booleanNode.getChildCount());//Must be child of the bolean operator
                    path = new TreePath(booleanNode.getPath());
                }
                else {
                    //inserting on an already existent boolean node with inner expr. IF it has an inner expr, add the operator and cond, else only the cond as childs
                    aggrFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), boleanNodeParent, boleanNodeParent.getChildCount());
                    aggrFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), boleanNodeParent, boleanNodeParent.getChildCount());
                    path = new TreePath(boleanNodeParent.getPath());
                }
            }
            else if (parent.getNodeType() == FilterNodeType.BOOLEAN_OPERATION && parent.getChildCount()>0){
                //user wants to add content to inner expression and dragg it to the outer boolean operator
                while (s == null){
                    s = createFilterStringOperation(column, false, true);//0 - boolean operation if any, 1 - condition
                }
                if (s.length == 0)
                    return false;
                //get the boolean operator next to it and check if it has childs
                aggrFilterTreeModel.insertNodeInto(new FilterNode(s[0], s[0], FilterNodeType.BOOLEAN_OPERATION), parent, parent.getChildCount());
                aggrFilterTreeModel.insertNodeInto(new FilterNode(s[1], column, FilterNodeType.CONDITION), parent, parent.getChildCount());
                path = new TreePath(parent.getPath());
            }

            aggrFiltersTree.expandPath(path);
            aggrFiltersTree.revalidate();
            aggrFiltersTree.updateUI();
            return true;
        }

        private String[] createFilterStringOperation(GlobalColumnData droppedCol, boolean isFirst, boolean isFilterAggr){
            String s[] = new String [2];
            String elem = "";
            if (isFilterAggr)
                elem = droppedCol.getAggrOpFullName();//aggrOP(table.column)
            else
                elem = droppedCol.getFullName();
            //filter operations depende on data type (<, >, <= only for numeric OR date)
            String[] filterOps;
            //select filter operations that can be performed depending on datatype
            if (isFilterAggr)
                filterOps = numberOperations;//if aggregate operations, then only numeric filter operations are allowed
            else {
                if (droppedCol.isNumeric()) //TODO: also accept date time datatypes
                    filterOps = numberOperations;
                else
                    filterOps = stringOperations;
            }
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
                if (!Utils.stringIsNumericOrBoolean(filterValue)){
                    filterValue = "'"+filterValue+"'";
                }
                if (filterValue.length() == 0){
                    JOptionPane.showMessageDialog(null, "Please insert a filter value with same data type",
                            "Operation Failed", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                elem+= " "+filter.getSelectedItem().toString() +" "+ filterValue;
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
                return flavors[0].equals(flavor);
            }
        }
        public class ListElemTransferable implements Transferable {
            ListElementWrapper listElem;

            public ListElemTransferable(ListElementWrapper elem) {
                this.listElem = elem;
            }

            public ListElementWrapper getTransferData(DataFlavor flavor)
                    throws UnsupportedFlavorException {
                if(!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);
                return listElem;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return flavors;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavors[1].equals(flavor);
            }
        }
    }


    //maximum characters per line when saving files
    class NarrowOptionPane extends JOptionPane {

        NarrowOptionPane() {
        }

        public int getMaxCharactersPerLineCount() {
            return 100;
        }
    }

    class CustomGroupCellRendererList extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ListElementWrapper elem = (ListElementWrapper) value;

            if (elem.getType() == ListElementType.GLOBAL_TABLE) {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            }
            return c;
        }
    }
}
