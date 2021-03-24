package main_app.wizards;

import helper_classes.*;
import helper_classes.ui_utils.LoadingScreenAnimator;
import main_app.database_integration.SchemaMatcher;
import main_app.*;
import main_app.metadata_storage.MetaDataManager;
import main_app.presto_com.PrestoMediator;
import main_app.wizards.DBConfig.DatabaseConnectionWizard;
import main_app.wizards.DBConfig.DatabaseSelection;
import main_app.wizards.global_schema_config.GlobalSchemaConfiguration;
import main_app.wizards.star_schema.CubeConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * Acts as a instal wizard. Users can move to next and last screen.
 * Changes the content depending on the configuration phase
 */
public class MainWizardPanel extends JPanel {
    private JPanel mainPanel;
    private JButton previousButton;
    private JButton cancelButton;
    private JButton nextBtn;
    private JPanel lowerPanel;
    private JPanel contentPanel;
    //prestoComm.wizards;
    private DatabaseConnectionWizard dbConnWizzard;
    private GlobalSchemaConfiguration globalSchemaConfigWizzard;
    private DatabaseSelection dbSelection;
    private CubeConfiguration cubeConfigWizzard;

    //steps
    private final String DB_CONN_CONFIG = "DBConfig";
    private final String DB_SELECTION = "DBSelection";
    private final String GLOBAL_SCHEMA_CONFIG = "globalSchemaConfig";
    private final String MULTI_DIM_CONFIG = "MultidimensionalSchemaConfig";

    private boolean isLast;
    private boolean localSchemaChange;
    private int currentStepNumber;
    private String[] steps = {DB_CONN_CONFIG, DB_SELECTION, GLOBAL_SCHEMA_CONFIG, MULTI_DIM_CONFIG}; //add DB_CONN...
    private PrestoMediator prestoMediator;
    private MetaDataManager metaDataManager;
    private SchemaMatcher schemaMatcher;

    //data
    private List<DBData> dbs;
    private List<GlobalTableData> globalSchema;
    private StarSchema starSchema;
    private EasyBDI mainMenu;

    private String projectName;
    private boolean isEdit;

    //
    volatile boolean isThreadRunning = true;

    public MainWizardPanel(EasyBDI mainMenu, String projectName, boolean isEdit) {
        this.setLayout(new BorderLayout());
        this.isEdit = isEdit;
        this.mainMenu = mainMenu;
        localSchemaChange = false;
        this.projectName = projectName;
        prestoMediator = new PrestoMediator();
        metaDataManager = new MetaDataManager(projectName);
        metaDataManager.createTablesAndFillDBModelData();//create tables if they dont exist already
        schemaMatcher = new SchemaMatcher(projectName);
        currentStepNumber = 0;
        if (steps.length == 1)
            setIsLastWindow(true);
        else
            setIsLastWindow(false);
        currentStepNumber = 0;
        setWizardPanel();

        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextWindow();
            }
        });

        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                previousWindow();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isEdit) {
                    String configs = "";
                    if (metaDataManager.getDatabaseCount() == 0)
                        configs += "\n -Local Schema";
                    if (globalSchema == null)
                        configs += "\n -Global Schema";
                    if (starSchema == null)
                        configs += "\n -Star Schema";
                    int dialogResult = JOptionPane.showConfirmDialog(null, "Would You Like to Save all configurations currently made to this project?\n" +
                            "Note that until this point the following was not configured and saved for this project: " + configs, "Warning", JOptionPane.YES_NO_OPTION);
                    if (dialogResult == JOptionPane.YES_OPTION) {

                    } else {
                        MetaDataManager.deleteProject(projectName); //remove database file (dont save progress)
                    }
                }
                else
                    metaDataManager.close();
                mainMenu.returnToMainMenu();
            }
        });
        /*this.setPreferredSize(new Dimension(950, 800));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Data source configuration wizard");
        pack();*/
        this.add(mainPanel);
        this.setVisible(true);
    }

    private void nextWindow() {
        previousButton.setEnabled(true);
        if (currentStepNumber == (steps.length - 1)) {
            finnish();
        } else {
            ++currentStepNumber;
            if (currentStepNumber == (steps.length - 1)) {
                nextBtn.setText("Finnish");
            }
            setWizardPanel();
        }
    }

    private void previousWindow() {
        if (currentStepNumber < 0)
            return;
        --currentStepNumber;
        if (currentStepNumber == 0)
            previousButton.setEnabled(false);
        nextBtn.setText("Next");
        switch (steps[currentStepNumber]) {
            case (DB_CONN_CONFIG):
                addToMainPanel(dbSelection, dbConnWizzard);//return to DB Connection config wizard interface
                break;
            case (DB_SELECTION):
                addToMainPanel(globalSchemaConfigWizzard, dbSelection);//return to DB filter config wizard interface
                break;
            case (GLOBAL_SCHEMA_CONFIG):
                addToMainPanel(cubeConfigWizzard, globalSchemaConfigWizzard);//return to to global schema wizard interface
                break;
            case (MULTI_DIM_CONFIG):
                //handleCubeConfig();//transition to multidimensional wizard interface
                break;
        }
    }


    private void setWizardPanel() {
        if (currentStepNumber == 0)
            previousButton.setEnabled(false);
        switch (steps[currentStepNumber]) {
            case (DB_CONN_CONFIG):
                handleDBConnConfig();//transition to database config wizard interface
                break;
            case (DB_SELECTION):
                handleDBSelect();//transition to database selection wizard interface
                break;
            case (GLOBAL_SCHEMA_CONFIG):
                handleGlobalSchemaConfig();//transition to global schema wizard interface
                break;
            case (MULTI_DIM_CONFIG):
                handleCubeConfig();//transition to multidimensional wizard interface
                break;
        }
    }

    private void finnish() {
        if (this.globalSchema == null || globalSchema.size() == 0) {
            JOptionPane.showMessageDialog(mainMenu, "Global Schema is invalid and could not be saved. Please verify the Global Schema.", "Error Saving Schema", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isEdit && metaDataManager.getGlobalSchema().size() > 0) {
            //delete all global schema tables (and therefore, star schemas (cubes) to add new edited global schema
            metaDataManager.deleteGlobalSchemaAndCubes();
        }

        metaDataManager.insertGlobalSchemaData(globalSchema);

        StarSchema starSchema = cubeConfigWizzard.getMultiDimSchema();

        if (starSchema == null) {
            JOptionPane.showMessageDialog(mainMenu, "Star Schema is invalid and could not be saved. Please verify the star schema.", "Error Saving Schema", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (metaDataManager.cubeExists(starSchema.getSchemaName())) {
            JOptionPane.showMessageDialog(mainPanel, "Cube already exists", "A cube with the same name already exists!\n Please select another name",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        metaDataManager.insertStarSchema(starSchema);
        metaDataManager.close();
        this.mainMenu.returnToMainMenu();
    }

    public void setLastWindow() {
        isLast = true;
        nextBtn.setText("Finnish");
    }

    private void setIsLastWindow(boolean b) {
        isLast = b;
        if (isLast)
            setLastWindow();

    }

    private void handleDBConnConfig() {
        if (isEdit)
            dbConnWizzard = new DatabaseConnectionWizard(prestoMediator, metaDataManager, true);
        else
            dbConnWizzard = new DatabaseConnectionWizard(prestoMediator, metaDataManager, false);
        addToMainPanel(null, dbConnWizzard);
    }

    private void handleDBSelect() {
        List<DBData> dbs = new ArrayList<>();
        //receive db data from DBConfig window
        if (!isEdit)
            dbs = dbConnWizzard.getDbList();
        else {
            dbs = metaDataManager.getDatabases();
            for (DBData db : dbs) {
                db.setTableList(metaDataManager.getTablesInDB(db));
            }
        }
        if (dbs == null || dbs.size() == 0) {//nothing to continue
            --currentStepNumber;
            return;
        }
        //dbs.addAll(generateLocalSchema());
        dbSelection = new DatabaseSelection(dbs, this);
        addToMainPanel(dbConnWizzard, dbSelection);
    }

    private void handleGlobalSchemaConfig() {
        List<DBData> dbs;
        dbs = dbSelection.getSelection();
        if (!isEdit || (isEdit && localSchemaChange))
            dbs = buildLocalSchema(dbs);

        //print local schema
        //metaDataManager.printLocalSchema();

        List<GlobalTableData> globalSchema;
        //if user is editing project with an existing global schema created previously, do not perform schema match and use the current global schema to populate global schema tree

        //LoadingScreenAnimator.openGeneralLoadingOnlyText(null, "<html><p>Creating a Global Schema.</p>" +
        //      "<p>Please Wait</p></html>");

        if (isEdit && metaDataManager.getGlobalTablesCount() > 0) {
            globalSchema = metaDataManager.getGlobalSchema();
        } else {
            //new project, or edit of project that does not contain a saved global schema, perform schema match and present a global schema sugestion.
            globalSchema = schemaMatcher.schemaIntegration(dbs);
        }
        //LoadingScreenAnimator.closeGeneralLoadingAnimation();
        //LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html><p>Preparing Schema Elements for display.</p>" +
        //              "<p>Please Wait</p></html>");
        globalSchemaConfigWizzard = new GlobalSchemaConfiguration(metaDataManager, dbs, globalSchema, prestoMediator);
        addToMainPanel(dbSelection, globalSchemaConfigWizzard);
        LoadingScreenAnimator.closeGeneralLoadingAnimation();
    }

    private void handleCubeConfig() {
        // receive global schema from global schema config window
        this.globalSchema = globalSchemaConfigWizzard.getGlobalSchemaFromTree();
        if (globalSchema == null || globalSchema.size() == 0) {
            --currentStepNumber;//undo step increment
            return;
        }
        cubeConfigWizzard = new CubeConfiguration(globalSchema, metaDataManager);
        addToMainPanel(globalSchemaConfigWizzard, cubeConfigWizzard);
    }

    /**
     * Add a panel to the main center of this jframe. The JPanel must be a wizard panel
     *
     * @param previousWizardPanel - panel to be removed (null if there was none)
     * @param newWizardPanel      - new panel to be transitioned
     */
    private void addToMainPanel(JPanel previousWizardPanel, JPanel newWizardPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridwidth = 3;
        gbc.gridheight = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        if (previousWizardPanel != null)
            contentPanel.remove(previousWizardPanel);
        contentPanel.add(newWizardPanel, gbc);
        contentPanel.revalidate();
        contentPanel.updateUI();
    }

    public void createDatabaseAndConnectToPresto() {
        //create local table models
        metaDataManager.createTablesAndFillDBModelData();
        //start presto connector
        boolean isConnected = prestoMediator.createConnection();
        if (isConnected) {
            System.out.println("Connection Succesfull to presto");
        } else {
            System.out.println("Could not connect to presto.");
            System.exit(0);
        }
    }

    /**
     * using presto, retrieve information about tables in each db, and columns in each table
     * and store in sqlite.
     * It is assumed that DB connection info for presto has already been set up
     * AND that database info has already been retrieved
     * return - List of table data, with info regarding its columns and database.
     **/
    public List<DBData> buildLocalSchema(List<DBData> dbs) {
        //insert DB Data
        dbs = metaDataManager.insertDBData(dbs);
        //get information about tables
        for (DBData db : dbs) {
            //List<TableData> dbTables = prestoMediator.getTablesInDatabase(db);
            List<TableData> dbTables = db.getTableList();
            dbTables = metaDataManager.insertTableData(dbTables); //tables updated with their id
            //get information about columns (for each table check information about their columns)
            dbTables = metaDataManager.insertColumnData(dbTables);//columns in tables updates with their id
            db.setTableList(dbTables);
        }
        // check all elements to see if they have ids. if their id is 0, they were already inserted.
        return dbs;
    }

    public void setLocalSchemaChange(boolean localSchemaChange) {
        this.localSchemaChange = localSchemaChange;
    }

    public boolean isLocalSchemaChange() {
        return localSchemaChange;
    }

    public MetaDataManager getMetaDataManager() {
        return metaDataManager;
    }

    public void setMetaDataManager(MetaDataManager metaDataManager) {
        this.metaDataManager = metaDataManager;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    /**
     * Given a database information (url, auth parameters) provided by the user, create presto config files to connect to those databases
     * return - false if presto was not restarted in order to use the new catalogs. True otherwise
     */
    public boolean generatePrestoDBConfigFiles(List<DBData> dbDataList) {
        for (DBData db : dbDataList) {
            prestoMediator.createDBFileProperties(db);
        }
        return prestoMediator.showRestartPrompt();
    }

    public List<DBData> getAllRegisteredDatabases() {
        return metaDataManager.getDatabases();
    }

    public void printQuery(String query) {
        metaDataManager.makeQueryAndPrint(query);
    }

    public void dropTables() {
        metaDataManager.deleteTables();
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
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new GridBagLayout());
        lowerPanel.setAlignmentY(0.5f);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(30, 0, 0, 0);
        mainPanel.add(lowerPanel, gbc);
        nextBtn = new JButton();
        nextBtn.setPreferredSize(new Dimension(120, 30));
        nextBtn.setText("Next >");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 15, 10);
        lowerPanel.add(nextBtn, gbc);
        final JSeparator separator1 = new JSeparator();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.fill = GridBagConstraints.BOTH;
        lowerPanel.add(separator1, gbc);
        cancelButton = new JButton();
        cancelButton.setPreferredSize(new Dimension(120, 30));
        cancelButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);
        lowerPanel.add(cancelButton, gbc);
        previousButton = new JButton();
        previousButton.setPreferredSize(new Dimension(120, 30));
        previousButton.setText("< Previous");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 15, 0);
        lowerPanel.add(previousButton, gbc);
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 2.0;
        gbc.weighty = 2.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(contentPanel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
