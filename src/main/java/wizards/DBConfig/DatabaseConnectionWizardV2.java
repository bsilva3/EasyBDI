package wizards.DBConfig;

import helper_classes.DBData;
import helper_classes.SimpleDBData;
import helper_classes.TableData;
import org.omg.PortableInterceptor.SUCCESSFUL;
import prestoComm.DBModel;
import prestoComm.MetaDataManager;
import prestoComm.PrestoMediator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static prestoComm.Constants.SUCCESS_STR;

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
    private JLabel noteLabel;
    private JButton importDataSourceFromButton;
    private List<DBData> dbList;
    private List<Boolean> dbConnectionTested;
    private DefaultListModel<String> listModel;
    private DefaultListModel<String> connListModel;
    private PrestoMediator prestoMediator;
    private MetaDataManager metaDataManager;
    private boolean isEdit;

    public DatabaseConnectionWizardV2(PrestoMediator prestoMediator, MetaDataManager metaDataManager){
        this.metaDataManager = metaDataManager;
        helpLabel.setText("<html>Please, enter database url, database model and the name of the database you want to connect."
                +"<br/> Do not specify a database in the url (x.x.x./dbname). Always specify the database in the 'database name' field."
                +"<br/> You must test if it is possible to connect to the database you inserted. To do this, select a database in the list and click 'test connection' button."
                +"<br/> You can't continue the configuration process while there are databases with failed connections or no connection attempts.</html>");
        stepLabel.setText("Step 1/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));
        noteLabel.setText("<html>Note: databases such as MySQL and MongoDB only need a server to be specified, therefore a database name is not necessary and will not be used." +
                "<br/>However it is still required to insert a database name in order to identify each database/server, therefore you must type a database name.</html>");
        noteLabel.setFont(new Font("", Font.PLAIN, 11));
        credentialsTxt.setFont(new Font("", Font.PLAIN, 11));

        databaseModelSelect.setModel(new DefaultComboBoxModel<DBModel>(DBModel.values()));
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
        add(mainPanel); //g-wizard
        setVisible(true);

        addDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDatabase(nameText.getText(), (DBModel) databaseModelSelect.getSelectedItem(), urlText.getText(), userText.getText(), String.valueOf(passText.getPassword()));
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
    }

    public DatabaseConnectionWizardV2(PrestoMediator prestoMediator, MetaDataManager metaDataManager, boolean isEdit){
        this(prestoMediator, metaDataManager);
        this.isEdit = isEdit;
        if (isEdit) {
            List<DBData> dbs = metaDataManager.getDatabases();
            for (DBData db : dbs) {
                addDatabase(db.getDbName(), db.getDbModel(), db.getUrl(), db.getUser(), db.getPass());
            }
            databaseList.revalidate();
            databaseList.updateUI();
        }
    }

    public void addImportedDatabases(List<SimpleDBData> importedDBs){
        List<DBData> dbs = metaDataManager.getDatabases();
        for (DBData db : dbs) {
            addDatabase(db.getDbName(), db.getDbModel(), db.getUrl(), db.getUser(), db.getPass());
        }
        databaseList.revalidate();
        databaseList.updateUI();
    }

    private void openDBImporter(){
        new DataSourceProjectImporter(this);
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass){
        DBData db = new DBData(url, model, name, user, pass);
        String s = name+" in "+url+" ("+model+")";
        dbList.add(db);
        listModel.addElement(s);
        dbConnectionTested.add(null);
        connListModel.addElement(" ---- ");
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
        String result = prestoMediator.testDBConnection(dbList.get(index));
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
        dbList.add(new DBData("localhost:5432", DBModel.PostgreSQL, "employees_horizontal", "postgres", "brunosilva"));//for test
        dbList.add(new DBData("localhost:5432", DBModel.PostgreSQL, "employees_vertical", "postgres", "brunosilva"));//for test
        dbList.add(new DBData("http://localhost:27017", DBModel.MongoDB,"inventory"));
        //dbList.add(new DBData("http://localhost:3306", DBModel.MYSQL,"employeesMYSQL", "bruno", "brunosilva"));
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
        prestoMediator.showRestartPrompt();
        for (int i = 0; i < dbList.size(); i++){
            DBData db = getTablesInDBFromPresto(dbList.get(i));
            //TODO: what to do if presto cant get tables.. fix
            dbList.set(i, db);
        }
        return dbList;
    }

    public DBData getTablesInDBFromPresto(DBData db){
        List<TableData> tables = prestoMediator.getTablesInDatabase(db);
        for (int i = 0; i < tables.size(); i++){
            TableData table = prestoMediator.getColumnsInTable(tables.get(i));
            tables.set(i, table); //update the table in this index with its columns
        }
        db.setTableList(tables);
        return db;
    }

}
