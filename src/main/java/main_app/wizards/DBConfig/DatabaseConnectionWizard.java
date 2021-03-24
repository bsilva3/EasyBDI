package main_app.wizards.DBConfig;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import helper_classes.DBData;
import helper_classes.ui_utils.LoadingScreenAnimator;
import helper_classes.SimpleDBData;
import helper_classes.TableData;
import helper_classes.DBModel;
import helper_classes.utils_other.Utils;
import main_app.metadata_storage.MetaDataManager;
import main_app.presto_com.PrestoMediator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static helper_classes.utils_other.Constants.*;
import static helper_classes.utils_other.Utils.getExtension;

public class DatabaseConnectionWizard extends JPanel {
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
    private JLabel fileLabel;
    private JLabel urlLabel;
    private JLabel userLabel;
    private JLabel passLabel;
    private List<DBData> dbList;
    private List<Boolean> dbConnectionTested;
    private DefaultListModel<String> listModel;
    private DefaultListModel<String> connListModel;
    private PrestoMediator prestoMediator;
    private MetaDataManager metaDataManager;
    private boolean isEdit;

    public DatabaseConnectionWizard(PrestoMediator prestoMediator, MetaDataManager metaDataManager) {
        this.metaDataManager = metaDataManager;
        helpLabel.setText("<html>Please, enter database url, database model and a name to be assigned to the database you want to connect."
                + "<br/> Note that the 'database name' field does not appear for data sources where a specific database must be selected within the server."
                + "<br/> In this case, you must add the database you intend to connect in the url: x.x.x.x/database"
                + "<br/> You can test if it is possible to connect to the datasource using the inserted details. To do this, select a datasource in the list and click 'Test Selected DS Connection' button.</html>");
                //+ "<br/> You cannot continue the configuration process while there are datasources with failed connections or no connection attempts.</html>");
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
                DBModel dbModel = (DBModel) databaseModelSelect.getSelectedItem();
                if (dbModel == DBModel.File && Utils.hasExtension(urlText.getText())) {
                    //if file and not inserted extension, refuse
                    JOptionPane.showMessageDialog(mainPanel, "Please, add the file's extension at the end", "Error: no file extension", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                addDatabase(nameText.getText(), dbModel, urlText.getText(), userText.getText(), String.valueOf(passText.getPassword()), false);
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

    private void handleDbFieldsInfo() {
        DBModel dbModel = DBModel.valueOf(databaseModelSelect.getSelectedItem().toString());
        if (dbModel.getBDDataModel().equalsIgnoreCase("files")) {
            nameText.setVisible(true);
            nameFieldLabel.setVisible(true);
            urlLabel.setText("File Location:");
            fileLabel.setVisible(true);
            //do not show login prompts
            credentialsTxt.setVisible(false);
            userText.setVisible(false);
            passText.setVisible(false);
            userLabel.setVisible(false);
            passLabel.setVisible(false);
            return;

        } else {
            urlLabel.setText("URL:");
            fileLabel.setVisible(false);
            //show login prompts
            credentialsTxt.setVisible(true);
            userText.setVisible(true);
            passText.setVisible(true);
            userLabel.setVisible(true);
            passLabel.setVisible(true);
        }
        if (dbModel.isSingleServerOneDatabase()) {
            nameText.setVisible(true);
            nameFieldLabel.setVisible(true);
        } else {
            nameText.setVisible(false);
            nameFieldLabel.setVisible(false);
        }
    }

    public DatabaseConnectionWizard(PrestoMediator prestoMediator, MetaDataManager metaDataManager, boolean isEdit) {
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

    public void addImportedDatabases(List<SimpleDBData> importedDBs) {
        for (SimpleDBData db : importedDBs) {
            addDatabase(db.getDbName(), db.getDbModel(), db.getUrl(), db.getUser(), db.getPass(), false);
        }
        databaseList.revalidate();
        databaseList.updateUI();
    }

    private void openDBImporter() {
        new DataSourceProjectImporter(this);
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass, boolean validate) {
        DBData db = new DBData(url, model, name, user, pass);
        String s = db.getDbName() + " in " + url + " (" + model + ")";
        dbList.add(db);
        listModel.addElement(s);
        if (validate) {
            dbConnectionTested.add(true);
            connListModel.addElement("Success");
        } else {
            dbConnectionTested.add(null);
            connListModel.addElement(" ---- ");
        }
        databaseList.updateUI();
        connectionTestList.updateUI();
    }

    private void removeDatabase(int index) {
        dbList.remove(index);
        listModel.remove(index);
        connListModel.remove(index);
        dbConnectionTested.remove(index);
        databaseList.updateUI();
        connectionTestList.updateUI();
    }

    private boolean testConnection(int index) {
        if (index < 0) {
            JOptionPane.showMessageDialog(null,
                    "Please, select first a data source from the list by cliking on it. Then press 'Test connection'",
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
        LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html>Restarting Trino to temporarily add the new data source.</p>" +
                "<p> A query will be made to test connectivity.</p><html>This may take no more than 30 seconds.</p></html>");
        String result = prestoMediator.testDBConnection(dbList.get(index));
        LoadingScreenAnimator.closeGeneralLoadingAnimation();
        if (result.equals(SUCCESS_STR)) {
            //connection success
            JOptionPane.showMessageDialog(null,
                    "Connection to data source was succesfull!",
                    "Connection Test Success",
                    JOptionPane.INFORMATION_MESSAGE);
            connListModel.set(index, "Success");
            return true;
        } else {
            //connection error
            JOptionPane.showMessageDialog(null,
                    "Could not connect to data source. The following error occurred: \n" + result,
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE);
            connListModel.set(index, "Error: " + result);
            return false;
        }
    }

    public List<DBData> getDbList() {
        /*if (dbConnectionTested.contains(false) || dbConnectionTested.contains(null)){
            JOptionPane.showMessageDialog(null,
                    "There are data sources that could not be connected or data source in which a connection test was not made.\n"
                    +"Please, make sure the url, data source name and other credentials to access the data source are correct, then test its connection.\n"
                    +"You can only move on when all data source in the list have been successfully connected.",
                    "Invalid data source connections",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }*/
        if (dbList.size() == 0) {
            JOptionPane.showConfirmDialog(this, "Please, add data sources and test their connection before moving on.",
                    "Insuficient validated data sources", JOptionPane.WARNING_MESSAGE);
            return dbList;
        }
        for (int i = 0; i < dbList.size(); i++) {
            prestoMediator.createDBFileProperties(dbList.get(i));
        }
        LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html><p>Restarting Trino to add the new data sources.</p>" +
                "<p>This may take no more than 30 seconds</p></html>.");
        int success = prestoMediator.restartPresto();
        LoadingScreenAnimator.closeGeneralLoadingAnimation();
        if (success == FAILED) {
            JOptionPane.showMessageDialog(mainPanel, "Presto could not be restarted.", "Could not restart", JOptionPane.WARNING_MESSAGE);
            return null;
        } else if (success == CANCELED) {
            //JOptionPane.showMessageDialog(mainPanel, "Presto could not be restarted.", "Could not restart", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        //LoadingScreenAnimator.openGeneralLoadingOnlyText(mainPanel, "<html>Gathering schema information from data sources.</p>" +
          //      "<p>Please Wait</p></html>");
        for (int i = 0; i < dbList.size(); i++) {
            DBData db = getTablesInDBFromPresto(dbList.get(i));
            if (db == null) {
                return null;
            }
            dbList.set(i, db);
        }
        //LoadingScreenAnimator.closeGeneralLoadingAnimation();
        return dbList;
    }

    public DBData getTablesInDBFromPresto(DBData db) {
        if (db.getDbModel() == DBModel.File) {
            TableData table = new TableData(db.getUrl(), getExtension(db.getUrl()), db);//if the 'db' is a file, the schema is its extension and the table name is its file name
            table = prestoMediator.getColumnsInTable(table);

            List<TableData> tables = new ArrayList<>(1);
            tables.add(table);
            db.setTableList(tables);
            return db;
        }

        List<TableData> tables = prestoMediator.getTablesInDatabase(db);
        for (int i = 0; i < tables.size(); i++) {
            TableData table = prestoMediator.getColumnsInTable(tables.get(i));
            if (table == null) {
                return null;
            }
            tables.set(i, table); //update the table in this index with its columns
        }
        db.setTableList(tables);
        return db;
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
        mainPanel.setPreferredSize(new Dimension(1000, 700));
        formPanel = new JPanel();
        formPanel.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(100, 0, 0, 0);
        mainPanel.add(formPanel, gbc);
        nameText = new JTextField();
        nameText.setText("");
        CellConstraints cc = new CellConstraints();
        formPanel.add(nameText, new CellConstraints(3, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        urlText = new JTextField();
        urlText.setMaximumSize(new Dimension(500, 20));
        formPanel.add(urlText, new CellConstraints(3, 7, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        urlLabel = new JLabel();
        urlLabel.setText("URL:");
        formPanel.add(urlLabel, cc.xy(1, 7));
        credentialsTxt = new JLabel();
        credentialsTxt.setText("<html>If the database requires user and password, please provide them here, otherwise leave these fields blank.<br/> Make sure the user has enough permissions to execute queries.</html>");
        formPanel.add(credentialsTxt, new CellConstraints(1, 9, 3, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 10, 0, 10)));
        userText = new JTextField();
        userText.setText("");
        formPanel.add(userText, new CellConstraints(3, 11, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        userLabel = new JLabel();
        userLabel.setText("User:");
        formPanel.add(userLabel, cc.xy(1, 11));
        passText = new JPasswordField();
        formPanel.add(passText, new CellConstraints(3, 13, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        passLabel = new JLabel();
        passLabel.setText("Password");
        formPanel.add(passLabel, cc.xy(1, 13));
        nameFieldLabel = new JLabel();
        nameFieldLabel.setText("Data Source Name:");
        formPanel.add(nameFieldLabel, cc.xy(1, 3));
        final JLabel label1 = new JLabel();
        label1.setText("Model:");
        formPanel.add(label1, cc.xy(1, 1));
        databaseModelSelect = new JComboBox();
        formPanel.add(databaseModelSelect, new CellConstraints(3, 1, 1, 1, CellConstraints.DEFAULT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 5)));
        fileLabel = new JLabel();
        fileLabel.setText("<html><p>If the file is local, add 'file://' followed by the path to the file.</p><p> You can also add remote files by specifying the link.</p></html>");
        fileLabel.setVisible(true);
        formPanel.add(fileLabel, cc.xyw(1, 5, 3));
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.3;
        gbc.weighty = 2.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane1, gbc);
        databaseList = new JList();
        scrollPane1.setViewportView(databaseList);
        addDatabaseButton = new JButton();
        addDatabaseButton.setText("Add Data Source");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(addDatabaseButton, gbc);
        removeDatabaseBtn = new JButton();
        removeDatabaseBtn.setText("Remove Selected Data Source");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(removeDatabaseBtn, gbc);
        testSelectedDBConnectionButton = new JButton();
        testSelectedDBConnectionButton.setText("Test Selected DS Connection");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(testSelectedDBConnectionButton, gbc);
        final JScrollPane scrollPane2 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.weightx = 2.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainPanel.add(scrollPane2, gbc);
        connectionTestList = new JList();
        scrollPane2.setViewportView(connectionTestList);
        helpLabel = new JLabel();
        helpLabel.setHorizontalAlignment(0);
        helpLabel.setHorizontalTextPosition(0);
        helpLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        mainPanel.add(helpLabel, gbc);
        stepLabel = new JLabel();
        stepLabel.setHorizontalAlignment(0);
        stepLabel.setHorizontalTextPosition(0);
        stepLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(stepLabel, gbc);
        importDataSourceFromButton = new JButton();
        importDataSourceFromButton.setText("Import Data Source From Project...");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.6;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 0, 0, 0);
        mainPanel.add(importDataSourceFromButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
