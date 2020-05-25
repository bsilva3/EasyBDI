package prestoComm;

import helper_classes.*;
import weka.core.converters.DatabaseConnection;
import wizards.DBConfig.DatabaseConnectionWizardV2;
import wizards.global_schema_config.GlobalSchemaConfigurationV2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
    private SchemaMatcher schemaMatcher;

    //data
    private List<DBData> dbs;
    private List<GlobalTableData> globalSchema;
    private StarSchema starSchema;

    public MainWizardFrame (){
        prestoMediator = new PrestoMediator();
        metaDataManager = new MetaDataManager();
        metaDataManager.createTablesAndFillDBModelData();//create tables if they dont exist already
        schemaMatcher = new SchemaMatcher();
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
        this.setPreferredSize(new Dimension(950, 800));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Data source configuration wizard");
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
            case(DB_CONN_CONFIG):
                handleDBConnConfig();//transition to database config wizard interface
                break;
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
        List<DBData> dbs = new ArrayList<>();
        //receive db data from DBConfig window
        /*List<DBData> dbs = dbConnWizzard.getDbList();
        if (dbs == null || dbs.size() == 0){
            --currentStepNumber;
            return;
        }*/
        dbs.addAll(generateLocalSchema());
        dbs = buildLocalSchema(dbs);
        /*globalSchemaConfigWizzard = new GlobalSchemaConfigurationV2();
        addToMainPanel(null, globalSchemaConfigWizzard);*/
        List<GlobalTableData> globalSchema = schemaMatcher.schemaIntegration(dbs);
        globalSchemaConfigWizzard = new GlobalSchemaConfigurationV2(dbs, globalSchema);
        addToMainPanel(dbConnWizzard, globalSchemaConfigWizzard);
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
        gbc.gridwidth = 3;
        gbc.gridheight = 3;
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

        // check all elements to see if they have ids. if their id is 0, they were already inserted.
        return dbs;
    }

    public void buildGlobalSchemaFromLocalSchema(List<DBData> dbs){
        //tables in SQLITE for global schema already created
        SchemaMatcher schemaMatcher = new SchemaMatcher();
        //Generate the global schema from the local schemas
        List<GlobalTableData> globalTables = schemaMatcher.schemaIntegration(dbs);
        //GlobalSchemaConfigurationV2 schemaConfigurationV2 = new GlobalSchemaConfigurationV2(dbs, globalTables);
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

    /**
     * test purposes only
     * @return
     */
    public static List<DBData> generateLocalSchema(){
        java.util.List<DBData> dbs = new ArrayList<>();
        java.util.List<TableData> tables = new ArrayList<>();
        DBData dbData1 = new DBData("http://192.168.11.3", DBModel.MYSQL, "lisbonDB");
        DBData dbData2 = new DBData("http://192.168.23.2", DBModel.PostgreSQL, "parisDB");
        DBData dbData3 = new DBData("http://192.168.23.5", DBModel.MongoDB, "inventory");
        TableData table1 = new TableData("employees", "schema", dbData1, 1);
        TableData table2 = new TableData("employees_paris", "schema", dbData2, 2);
        TableData table3 = new TableData("employees_contacts", "schema", dbData2, 3);
        TableData table4 = new TableData("products", "schema", dbData3, 4);
        java.util.List<ColumnData> colsForTable1 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable2 = new ArrayList<>();
        java.util.List<ColumnData> colsForTable3 = new ArrayList<>();
        List<ColumnData> colsForTable4 = new ArrayList<>();
        colsForTable1.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("full_name", "varchar", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("phone_number", "integer", false).withTable(table1).build());
        colsForTable1.add(new ColumnData.Builder("email", "varchar", false).withTable(table1).build());

        colsForTable2.add(new ColumnData.Builder("id", "integer", true).withTable(table2).build());
        colsForTable2.add(new ColumnData.Builder("name", "varchar", false).withTable(table2).build());

        colsForTable3.add(new ColumnData.Builder("employee_id", "integer", true).withTable(table3)
                .withForeignKey("catalog.schema.employees_paris.id").build());
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
        dbData2.addTable(table2);
        dbData2.addTable(table3);
        dbData3.addTable(table4);
        dbs.add(dbData1);
        dbs.add(dbData2);
        dbs.add(dbData3);
        return dbs;
    }
}
