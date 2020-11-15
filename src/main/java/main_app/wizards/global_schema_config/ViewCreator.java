package main_app.wizards.global_schema_config;

import main_app.presto_com.PrestoMediator;

import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ViewCreator extends JFrame{
    private JPanel mainPanel;
    private JTextArea sqlCodeText;
    private JLabel helpLabel;
    private JButton createViewButton;
    private JButton cancelButton;
    private PrestoMediator prestoMediator;
    private String tableName;
    private GlobalSchemaConfiguration globalSchemaConfiguration;

    public ViewCreator(PrestoMediator prestoMediator, GlobalSchemaConfiguration globalSchemaConfiguration, String tableName){
        this.prestoMediator = prestoMediator;
        this.tableName = tableName;
        this.globalSchemaConfiguration = globalSchemaConfiguration;
        helpLabel.setText("<html><p>This feature is for experienced users.</p>" +
                "<p>Use SQL code to modify the structure of the table and create a view. " +
                "This code will be executed each time data is fetched from this table.</p>" +
                "<p>An example of a view is to write code that unpivots a column(s).</p>" +
                "<p>(Do not use any ';' at the end)</p></html>");

        sqlCodeText.setWrapStyleWord(true);
        sqlCodeText.setLineWrap(true);

        createViewButton.addActionListener(actionEvent -> testAndCreateView());

        cancelButton.addActionListener(actionEvent -> closeWindow());

        this.setPreferredSize(new Dimension(700, 650));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("View creation");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

    }

    public void testAndCreateView(){
        String sqlCode = sqlCodeText.getText();
        if (sqlCode.isEmpty())
            return;
        //get sql code and execute it once on Presto.
        List<String[]> result = prestoMediator.getQueryColsName(sqlCode+"limit 1");
        if (result == null || result.isEmpty()){
            JOptionPane.showMessageDialog(null, "Error Executing inserted query.", "", JOptionPane.ERROR_MESSAGE);
            globalSchemaConfiguration.setViewInfo(null, "");
            return;
        }
        globalSchemaConfiguration.setViewInfo(result, sqlCode);
        dispose();
    }

    private void closeWindow(){
        this.setVisible(false);
        dispose();
    }
}
