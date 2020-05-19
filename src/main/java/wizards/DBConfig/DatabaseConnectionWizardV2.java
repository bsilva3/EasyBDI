package wizards.DBConfig;

import helper_classes.DBData;
import org.omg.PortableInterceptor.SUCCESSFUL;
import prestoComm.DBModel;
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
    private JList databaseList;
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
    private List<DBData> dbList;
    private List<Boolean> dbConnectionTested;
    private DefaultListModel<String> listModel;
    private PrestoMediator prestoMediator;

    public DatabaseConnectionWizardV2(PrestoMediator prestoMediator){
        //this.setTitle("Database Configuration");
        credentialsTxt.setFont(new Font("", Font.PLAIN, 12));

        databaseModelSelect.setModel(new DefaultComboBoxModel<DBModel>(DBModel.values()));
        listModel = new DefaultListModel<>();
        databaseList.setModel(listModel);
        dbList = new ArrayList<>();
        dbConnectionTested = new ArrayList<>();
        this.prestoMediator = prestoMediator;
        //setContentPane(mainPanel);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //pack();
        add(mainPanel); //g-wizard
        setVisible(true);

        addDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDatabase(nameText.getText(), (DBModel) databaseModelSelect.getSelectedItem(), urlText.getText(), userText.getText(), passText.getPassword().toString());
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
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass){
        DBData db = new DBData(name, model, url, user, pass);
        String s = name+", "+model+", "+url;
        dbList.add(db);
        listModel.addElement(s);
        dbConnectionTested.add(null);
        databaseList.updateUI();
    }

    private void removeDatabase(int index){
        dbList.remove(index);
        listModel.remove(index);
        dbConnectionTested.remove(index);
        databaseList.updateUI();
    }

    private boolean testConnection(int index){
        String result = prestoMediator.testDBConnection(dbList.get(index));
        if (result.equals(SUCCESS_STR)){
            //connection success
            JOptionPane.showMessageDialog(null,
                    "Connection to database was succesfull!",
                    "Connection Test Success",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        else{
            //connection error
            JOptionPane.showMessageDialog(null,
                    "Could not connect to database. Error: \n"+result,
                    "Connection Test Failed",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<DBData> getDbList(){
        return dbList;
    }


    //for g-wizard
    /*@Override
    protected AbstractWizardPage getNextPage() {
        return new GlobalSchemaConfigurationV2();//TODO: passar lista bases de dados
    }

    @Override
    protected boolean isCancelAllowed() {
        return true;
    }

    @Override
    protected boolean isPreviousAllowed() {
        return false;
    }

    @Override
    protected boolean isNextAllowed() {
        return true;
    }

    @Override
    protected boolean isFinishAllowed() {
        return false;
    }*/


    public static void main(String[] args){
        DatabaseConnectionWizardV2 window = new DatabaseConnectionWizardV2(new PrestoMediator());
    }

}
