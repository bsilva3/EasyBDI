package wizards.global_schema_config;

import helper_classes.GlobalColumnData;
import helper_classes.GlobalTableData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ForeignKeySelector extends JFrame{
    private JPanel mainPanel;
    private JComboBox tableComboBox;
    private JComboBox columnComboBox;
    private DefaultComboBoxModel tableModel;
    private DefaultComboBoxModel colModel;
    private JButton confirmButton;
    private List<GlobalTableData> globalTabs;
    private GlobalSchemaConfiguration schemaConfig;

    public ForeignKeySelector(GlobalSchemaConfiguration schemaConfig, List<GlobalTableData> globalTabs){
        this.schemaConfig = schemaConfig;
        this.globalTabs = globalTabs;
        fillTablesComboBox();
        tableComboBox.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setColumnsComboBox(globalTabs.get(tableComboBox.getSelectedIndex()));
            }
        });
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GlobalTableData g = globalTabs.get(tableComboBox.getSelectedIndex());
                GlobalColumnData c = g.getGlobalColumnData(columnComboBox.getSelectedItem().toString());
                schemaConfig.addForeignKey(g, c);
                dispose();
            }
        });
        this.setPreferredSize(new Dimension(600, 600));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Add Foreign Key");
        pack();
        this.setVisible(true);
    }

    private void fillTablesComboBox(){
        String[] s = new String[globalTabs.size()];
        for (int i = 0; i < globalTabs.size(); i++){
            s[i] = globalTabs.get(i).getTableName();
        }
        tableModel = new DefaultComboBoxModel(s);
        tableComboBox.setModel(tableModel);
        tableComboBox.setSelectedIndex(0);
        setColumnsComboBox(globalTabs.get(0));
    }

    private void setColumnsComboBox(GlobalTableData t){
        List<GlobalColumnData> primKeyCols = t.getGlobalColumnDataList();
        String[] s = new String[primKeyCols.size()];
        for (int i = 0; i < primKeyCols.size(); i++){
            s[i] = primKeyCols.get(i).getName();
        }
        colModel = new DefaultComboBoxModel(s);
        columnComboBox.setModel(colModel);
        columnComboBox.setSelectedIndex(0);
    }
}
