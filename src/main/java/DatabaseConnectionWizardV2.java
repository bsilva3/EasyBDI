import helper_classes.DBData;
import prestoComm.DBModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionWizardV2 extends JFrame {
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

    public DatabaseConnectionWizardV2(){
        //this.setTitle("Database Configuration");
        credentialsTxt.setFont(new Font("", Font.PLAIN, 12));

        databaseModelSelect.setModel(new DefaultComboBoxModel<DBModel>(DBModel.values()));
        listModel = new DefaultListModel<>();
        databaseList.setModel(listModel);
        dbList = new ArrayList<>();
        dbConnectionTested = new ArrayList<>();
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        //add(mainPanel); //g-wizard
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

            }
        });
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass){
        DBData db = new DBData(name, model, url, user, pass);
        String s = name+", "+model+", "+url;
        dbList.add(db);
        listModel.addElement(s);
        databaseList.updateUI();
    }

    private void removeDatabase(int index){
        dbList.remove(index);
        listModel.remove(index);
        databaseList.updateUI();
    }

    private boolean testConnection(int index){
        dbList.get(index);
        return false;//TODO: finish
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
        DatabaseConnectionWizardV2 window = new DatabaseConnectionWizardV2();
    }

}
