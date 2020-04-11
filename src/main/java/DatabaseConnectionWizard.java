import helper_classes.DBData;
import prestoComm.DBModel;
import se.gustavkarlsson.gwiz.AbstractWizardPage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnectionWizard extends JFrame {
    private JButton addDatabaseButton;
    private JTextField nameText;
    private JTextField urlText;
    private JComboBox databaseModelSelect;
    private JPanel mainPanel;
    private JButton removeDatabase;
    private JTextField userText;
    private JPasswordField passText;
    private List<DBData> dbList;
    private JList databaseList;
    private DefaultListModel<String> listModel;

    public DatabaseConnectionWizard(){
        //this.setTitle("Database Configuration");
        databaseModelSelect.setModel(new DefaultComboBoxModel<DBModel>(DBModel.values()));
        listModel = new DefaultListModel<>();
        databaseList.setModel(listModel);
        dbList = new ArrayList<>();
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
    }

    private void addDatabase(String name, DBModel model, String url, String user, String pass){
        DBData db = new DBData(name, model, url, user, pass);
        String s = name+", "+model+", "+url;
        dbList.add(db);
        listModel.addElement(s);
        databaseList.updateUI();
    }

    public static void main(String[] args){
        DatabaseConnectionWizard window = new DatabaseConnectionWizard();
    }


    /*for g-wizard
    @Override
    protected AbstractWizardPage getNextPage() {
        return new GlobalSchemaConfiguration();//TODO: passar lista bases de dados
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

}
