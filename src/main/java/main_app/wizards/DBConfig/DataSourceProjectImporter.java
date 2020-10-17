package main_app.wizards.DBConfig;

import helper_classes.SimpleDBData;
import main_app.metadata_storage.MetaDataManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DataSourceProjectImporter extends JFrame{
    private JList dbList;
    private JPanel mainPanel;
    private JButton cancelButton;
    private JLabel helpLabel;
    private JButton confirmButton;
    private DefaultListModel listModel;
    private DatabaseConnectionWizardV2 databaseConnectionWizardV2;

    public DataSourceProjectImporter(DatabaseConnectionWizardV2 databaseConnectionWizardV2){
        this.databaseConnectionWizardV2 = databaseConnectionWizardV2;
        helpLabel.setText("Check the Data Sources to be imported.");
        listModel = new DefaultListModel();
        dbList.setModel(listModel);
        //dbList.setCellRenderer(new CheckboxListCellRenderer());

        dbList.setCellRenderer(new CheckboxListRenderer());
        dbList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add a mouse listener to handle changing selection

        dbList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                JList<CheckboxListItem> list =
                        (JList<CheckboxListItem>) event.getSource();

                // Get index of item clicked

                int index = list.locationToIndex(event.getPoint());
                CheckboxListItem item = (CheckboxListItem) list.getModel()
                        .getElementAt(index);

                // Toggle selected state

                item.setSelected(!item.isSelected());

                // Repaint cell

                list.repaint(list.getCellBounds(index, index));
            }
        });

        Set<SimpleDBData> dbs = MetaDataManager.getAllDatabasesInProjects();
        for (SimpleDBData db : dbs){
            listModel.addElement(new CheckboxListItem(db));
        }
        dbList.revalidate();
        dbList.updateUI();

        this.setPreferredSize(new Dimension(450, 800));
        setVisible(true);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Data source import");
        pack();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmChoices();
            }
        });
    }

    public void confirmChoices(){
        List<SimpleDBData> dbs = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++){
            CheckboxListItem item = (CheckboxListItem) listModel.get(i);
            if (item.isSelected){
                dbs.add(item.getDB());
            }
        }

        databaseConnectionWizardV2.addImportedDatabases(dbs);
        closeWindow();
    }

    private void closeWindow(){
        this.setVisible(false);
        dispose();
    }

    class CheckboxListItem {
        private SimpleDBData dbLabel;
        private boolean isSelected = false;

        public CheckboxListItem(SimpleDBData dbLabel) {
            this.dbLabel = dbLabel;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public SimpleDBData getDB() {
            return dbLabel;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public String toString() {
            return dbLabel.toString();
        }
    }


    // Handles rendering cells in the list using a check box

    class CheckboxListRenderer extends JCheckBox implements
            ListCellRenderer<CheckboxListItem> {

        @Override
        public Component getListCellRendererComponent(
                JList<? extends CheckboxListItem> list, CheckboxListItem value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setEnabled(list.isEnabled());
            setSelected(value.isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }
}


