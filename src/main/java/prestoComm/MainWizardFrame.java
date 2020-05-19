package prestoComm;

import helper_classes.*;
import weka.core.converters.DatabaseConnection;
import wizards.DBConfig.DatabaseConnectionWizardV2;
import wizards.global_schema_config.GlobalSchemaConfigurationV2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainWizardFrame extends JFrame{
    private JPanel mainPanel;
    private JButton previousButton;
    private JButton cancelButton;
    private JButton nextBtn;
    private JPanel lowerPanel;
    //wizards;
    private DatabaseConnectionWizardV2 dbConnWizzard;
    private GlobalSchemaConfigurationV2 globalSchemaConfigWizzard;
    private CubeConfiguration cubeConfigWizzard;

    //steps
    private final String DB_CONN_CONFIG = "DBConfig";
    private final String GLOBAL_SCHEMA_CONFIG = "globalSchemaConfig";
    private final String MULTI_DIM_CONFIG = "MultidimensionalSchemaConfig";
    private final String REVIEW = "Review";//?

    private boolean isLast;
    private int currentStepNumber;
    private String[] steps = {DB_CONN_CONFIG, GLOBAL_SCHEMA_CONFIG, MULTI_DIM_CONFIG}; //add DB_CONN...
    private PrestoMediator prestoMediator;
    private MetaDataManager metaDataManager;

    //data
    private List<DBData> dbs;
    private List<GlobalTableData> globalSchema;
    private StarSchema starSchema;

    public MainWizardFrame (){
        prestoMediator = new PrestoMediator();
        metaDataManager = new MetaDataManager();
        currentStepNumber = 0;
        if (steps.length == 1)
            setIsLastWindow(true);
        else
            setIsLastWindow(false);
        currentStepNumber = 0;
        setWizardPanel();

        nextBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                nextWindow();
            }
        });

        previousButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                previousWindow();
            }
        });
        this.setPreferredSize(new Dimension(900, 800));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        this.setVisible(true);
    }

    public static void main(String[] args){
        MainWizardFrame m = new MainWizardFrame();
    }

    private void nextWindow(){
        previousButton.setEnabled(true);
        if (currentStepNumber == (steps.length - 1))
            finnish();
        else {
            ++currentStepNumber;
            setWizardPanel();
        }
    }

    private void previousWindow(){
        if (currentStepNumber < 0)
            return;
        --currentStepNumber;
        if (currentStepNumber == 0)
            previousButton.setEnabled(false);
        nextBtn.setText("Next");
        switch (steps[currentStepNumber]){
            case(DB_CONN_CONFIG):
                addToMainPanel(globalSchemaConfigWizzard, dbConnWizzard);//return to DB Connection config wizard interface
            case(GLOBAL_SCHEMA_CONFIG):
                addToMainPanel(cubeConfigWizzard, globalSchemaConfigWizzard);//return to to global schema wizard interface
                break;
            case(MULTI_DIM_CONFIG):
                //handleCubeConfig();//transition to multidimensional wizard interface
                break;
        }
    }


    private void setWizardPanel(){
        if (currentStepNumber == 0)
            previousButton.setEnabled(false);
        switch (steps[currentStepNumber]){
            case(GLOBAL_SCHEMA_CONFIG):
                handleGlobalSchemaConfig();//transition to global schema wizard interface
                break;
            case(MULTI_DIM_CONFIG):
                handleCubeConfig();//transition to multidimensional wizard interface
                break;
        }
    }

    private void finnish(){
        if (this.globalSchema == null)
            return ; //TODO: handle error
        metaDataManager.insertGlobalSchemaData(globalSchema);
        StarSchema starSchema = cubeConfigWizzard.getMultiDimSchema();
        metaDataManager.insertStarSchema(starSchema);
    }

    public void setLastWindow(){
        isLast = true;
        nextBtn.setText("Finnish");
    }

    private void setIsLastWindow(boolean b){
        isLast = b;
        if (isLast)
            setLastWindow();

    }

    private void handleDBConnConfig(){
        dbConnWizzard = new DatabaseConnectionWizardV2(prestoMediator);
        addToMainPanel(null, dbConnWizzard);
    }

    private void handleGlobalSchemaConfig(){
        //receive db data from DBConfig window
        List<DBData> dbs = dbConnWizzard.getDbList();
        buildLocalSchema(dbs);
        globalSchemaConfigWizzard = new GlobalSchemaConfigurationV2();
        addToMainPanel(null, globalSchemaConfigWizzard);
    }

    private void handleCubeConfig(){
        // receive global schema from global schema config window
        this.globalSchema = globalSchemaConfigWizzard.getGlobalSchemaFromTree();
        cubeConfigWizzard = new CubeConfiguration(globalSchema);
        addToMainPanel(globalSchemaConfigWizzard, cubeConfigWizzard);
    }

    /**
     * Add a panel to the main center of this jframe. The JPanel must be a wizard panel
     * @param previousWizardPanel - panel to be removed (null if there was none)
     * @param newWizardPanel - new panel to be transitioned
     */
    private void addToMainPanel(JPanel previousWizardPanel, JPanel newWizardPanel){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        if (previousWizardPanel != null)
            mainPanel.remove(previousWizardPanel);
        mainPanel.add(newWizardPanel, gbc);
        mainPanel.revalidate();
        mainPanel.updateUI();
    }

    public void createDatabaseAndConnectToPresto(){
        //create local table models
        metaDataManager.createTablesAndFillDBModelData();
        //start presto connector
        boolean isConnected = prestoMediator.createConnection();
        if (isConnected){
            System.out.println("Connection Succesfull to presto");
        }
        else{
            System.out.println("Could not connect to presto.");
            System.exit(0);
        }
    }

    /**using presto, retrieve information about tables in each db, and columns in each table
     * and store in sqlite.
     * It is assumed that DB connection info for presto has already been set up.
     * Will fetch DB Data and store it on SQLITE
     **/
    /*public void buildLocalSchemas(){
        //get databases registered
        prestoMediator.
        prestoMediator.getTablesInDatabase();
    }*/

    /**using presto, retrieve information about tables in each db, and columns in each table
     * and store in sqlite.
     * It is assumed that DB connection info for presto has already been set up
     * AND that database info has already been retrieved
     * return - List of table data, with info regarding its columns and database.
     **/
    public List<DBData> buildLocalSchema(List<DBData> dbs){
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

        return dbs;
    }

    public void buildGlobalSchemaFromLocalSchema(List<DBData> dbs){
        //tables in SQLITE for global schema already created
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        //Generate the global schema from the local schemas
        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(dbs);
        GlobalSchemaConfigurationV2 schemaConfigurationV2 = new GlobalSchemaConfigurationV2(dbs, globalTables);
        //insert the global tables, global columns in the database and correspondences between local and global columns
        //metaDataManager.insertGlobalSchemaData(globalTables);
    }

    public void buildStarSchema(GlobalTableData factTable, List<GlobalTableData> dimTables, List<GlobalColumnData> measures){

    }


    /**
     * Given a database information (url, auth parameters) provided by the user, create presto config files to connect to those databases
     * return - false if presto was not restarted in order to use the new catalogs. True otherwise
     */
    public boolean generatePrestoDBConfigFiles(List<DBData> dbDataList){
        for (DBData db : dbDataList){
            prestoMediator.createDBFileProperties(db);
        }
        return prestoMediator.showRestartPrompt();
    }

    public List<DBData> getAllRegisteredDatabases(){
        return metaDataManager.getDatabases();
    }

    public void printQuery(String query){
        metaDataManager.makeQueryAndPrint(query);
    }
    public void dropTables(){
        metaDataManager.deleteTables();
    }
}
