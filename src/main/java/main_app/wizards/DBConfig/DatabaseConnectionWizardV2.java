package main_app.wizards.DBConfig;

import helper_classes.elements.DBData;
import helper_classes.ui_utils.LoadingScreenAnimator;
import helper_classes.elements.SimpleDBData;
import helper_classes.elements.TableData;
import helper_classes.elements.DBModel;
import main_app.metadata_storage.MetaDataManager;
import main_app.presto_com.PrestoMediator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static helper_classes.utils_other.Constants.*;

public class DatabaseConnectionWizardV2 extends JPanel {
    //extends AbstractWizardPage
    private JPanel mainPanel;
    private JTextField nameText;
    private JTextField urlText;
    private JComboBox databaseModelSelect;
    private JTextField userText;
    private JPasswordField passText;
    private JButton addDatabaseButton;
    private JButton removeDatabaseBtn;
    private JPanel formPanel;
    private JLabel credentialsTxt;
    private JButton testSelectedDBConnectionButton;
    private JList databaseList;
    private JList connectionTestList;
    private JLabel helpLabel;
    private JLabel stepLabel;
    private JButton importDataSourceFromButton;
    private JLabel nameFieldLabel;
    private List<DBData> dbList;
    private List<Boolean> dbConnectionTested;
    private DefaultListModel<String> listModel;
    private DefaultListModel<String> connListModel;
    private PrestoMediator prestoMediator;
    private MetaDataManager metaDataManager;
    private boolean isEdit;

    public DatabaseConnectionWizardV2(PrestoMediator prestoMediator, MetaDataManager metaDataManager){
        this.metaDataManager = metaDataManager;
        helpLabel.setText("<html>Please, enter database url, database model and a name to be assigned to the database you want to connect."
                +"<br/> Note that the 'database name' field does not appear for data sources where a specific database must be selected within the server."
                +"<br/> In this case, you must add the database you intend to connect in the url: x.x.x.x/database"
                +"<br/> You must test if it is possible to connect to the datasource using the inserted details. To do this, select a datasource in the list and click 'Test Selected DS Connection' button."
                +"<br/> You cannot continue the configuration process while there are datasources with failed connections or no connection attempts.</html>");
        stepLabel.setText("Step 1/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));
        credentialsTxt.setFont(new Font("", Font.PLAIN, 11));

        databaseModelSelect.setModel(new DefaultComboBoxModel<DBModel>(DBModel.values()));
        handleDbFieldsInfo();//to show/hide the name field depending on the selected db model
        dbList = new ArrayList<>();
        dbConnectionTested = new ArrayList<>();
        listModel = new DefaultListModel<>();
        databaseList.setModel(listModel);
        connListModel = new DefaultListModel<>();
        connectionTestList.setModel(connListModel);
        this.prestoMediator = prestoMediator;
        //setContentPane(mainPanel);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        this.setLayout(new BorderLayout());
        add(mainPanel);
        setVisible(true);

        addDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDatabase(nameText.getText(), (DBModel) databaseModelSelect.getSelectedItem(), urlText.getText(), userText.getText(), String.valueOf(passText.getPassword()), false);
            }
        });

        removeDatabaseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeDatabase(databaseList.getSelectedIndex());
            }
        });

        testSelectedDBConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = databaseList.getSelectedIndex();
                boolean connected = testConnection(index);
                if (connected)
                    dbConnectionTested.set(index, true);
                else
                    dbConnectionTested.set(index, false);

            }
        });
        importDataSourceFromButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDBImporter();
            }
        });
        databaseModelSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDbFieldsInfo();
            }
        });
    }

    private void handleDbFieldsInfo(){
        DBModel dbModel = DBModel.valueOf(databaseModelSelect.getSelectedItem().toString());
        if (dbModel.isSingleServerOneDatabase()){
            nameText.setVisible(true);
            nameFieldLabel.setVisible(true);
        }
        else{
            nameText.setVisible(false);
            nameFieldLabel.setVisible(false);
        }
    }

    public DatabaseConnectionWizardV2(PrestoMediator prestoMediator, MetaDataManager metaDataManager, boolean isEdit){
        this(prestoMediator, metaDataManager);
        this.isEdit = isEdit;
        if (isEdit) {
            List<DBData> dbs = metaDataManager.getDatabases();
            for (DBData db : dbs) {
                addDatabase(db.getDbName(), db.getDbModel(), db.getUrl(), db.getUser(), db.getPass(), true);
            }
            databaseList.revalidate();
            databaseList.updateUI();
        }
    }

    public void addImportedDatabases(List<SimpleDBData> importedDBs){
        for (SimpleDBData db : importedDBs) {
            addDatabase(db.getDbName(), db.getDbModel(), db.getUrl(), db.getUser(), db.getPass(), false);
        }
        databaseList.revalidate();
        databaseList.updateUI();
    }

    private void openDBImporter(){
        new DataSourceProjectImporter(this);
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass, boolean validate){
        DBData db = new DBData(url, model, name, user, pass);
        String s = db.getDbName()+" in "+url+" ("+model+")";
        dbList.add(db);
        listModel.addElement(s);
        if (validate){
            dbConnectionTested.add(true);
            connListModel.addElement("Success");
        }
        else {
            dbConnectionTested.add(null);
            connListModel.addElement(" ---- ");
        }
        databaseList.updateUI();
        connectionTestList.updateUI();
    }

    private void removeDatabase(int index){
        dbList.remove(index);
        listModel.remove(index);
        connListModel.remove(index);
        dbConnectionTested.remove(index);
        databaseList.updateUI();
        connectionTestList.updateUI();
    }

    private boolean testConnection(int index){
        if (index < 0){
            JOptionPane.showMessageDialog(null,
                    "Please, select first a data source from the list by cliking on it. Then press 'Test connection'",
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
        LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html>Restarting Presto to temporarly add the new data source.</p>" +
                "<p> A query will be made to test connectivity.</p><html>This may take no more than 30 seconds.</p></html>");
        String result = prestoMediator.testDBConnection(dbList.get(index));
        LoadingScreenAnimator.closeGeneralLoadingAnimation();
        if (result.equals(SUCCESS_STR)){
            //connection success
            JOptionPane.showMessageDialog(null,
                    "Connection to database was succesfull!",
                    "Connection Test Success",
                    JOptionPane.INFORMATION_MESSAGE);
            connListModel.set(index, "Success");
            return true;
        }
        else{
            //connection error
            JOptionPane.showMessageDialog(null,
                    "Could not connect to database. Error: \n"+result,
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE);
            connListModel.set(index, "Error: "+ result);
            return false;
        }
    }

    public List<DBData> getDbList(){
        /*dbList.add(new DBData("localhost:5432", DBModel.PostgreSQL, "employees_horizontal", "postgres", "brunosilva"));//for test
        dbList.add(new DBData("localhost:5432", DBModel.PostgreSQL, "employees_vertical", "postgres", "brunosilva"));//for test
        dbList.add(new DBData("http://localhost:27017", DBModel.MongoDB,"inventory"));
        //dbList.add(new DBData("http://localhost:3306", DBModel.MYSQL,"employeesMYSQL", "bruno", "brunosilva"));*/
        if (dbConnectionTested.contains(false) || dbConnectionTested.contains(null)){
            JOptionPane.showMessageDialog(null,
                    "There are databases that could not be connected or databases in which a connection test was not made.\n"
                    +"Please, make sure the url, database name and other credentials to access the database are correct, then test its connection.\n"
                    +"You can only move on when all databases in the list have been successfully connected.",
                    "Invalid Database connections",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (dbList.size() == 0){
            JOptionPane.showConfirmDialog(this, "Please, add databases and test their connection before moving on.",
                    "Insuficient validated databases", JOptionPane.WARNING_MESSAGE);
            return dbList;
        }
        for (int i = 0; i < dbList.size(); i++){
            prestoMediator.createDBFileProperties(dbList.get(i));
        }
        LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html><p>Restarting Presto to add the new data sources.</p>" +
                "<p>This may take no more than 30 seconds</p></html>.");
        int success = prestoMediator.restartPresto();
        LoadingScreenAnimator.closeGeneralLoadingAnimation();
        if (success == FAILED){
            JOptionPane.showMessageDialog(mainPanel, "Presto could not be restarted.", "Could not restart", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        else if (success == CANCELED){
            //JOptionPane.showMessageDialog(mainPanel, "Presto could not be restarted.", "Could not restart", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        //prestoMediator.showRestartPrompt();
        for (int i = 0; i < dbList.size(); i++){
            DBData db = getTablesInDBFromPresto(dbList.get(i));
            if (db == null){
                return null;
            }
            dbList.set(i, db);
        }
        return dbList;
    }

    public DBData getTablesInDBFromPresto(DBData db){
        List<TableData> tables = prestoMediator.getTablesInDatabase(db);
        for (int i = 0; i < tables.size(); i++){
            TableData table = prestoMediator.getColumnsInTable(tables.get(i));
            if (table == null){
                return null;
            }
            tables.set(i, table); //update the table in this index with its columns
        }
        db.setTableList(tables);
        return db;
    }

}
