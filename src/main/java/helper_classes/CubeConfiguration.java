package helper_classes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeConfiguration extends JPanel{
        //extends JFrame{
    private List<GlobalTableData> globalTables;
    private int selectedFactsTable;
    private Map<Integer, JCheckBox> dimsCheckBoxes; //and index of the global table and its checkbox in the dims table selection
    private Map<GlobalColumnData, Boolean> factsColumns;
    private Map<GlobalColumnData, JCheckBox> measuresCheckBoxes;
    private JComboBox dimensionTableComboBox;
    private JLabel stepLabel;
    private JLabel helpLabel;
    private JPanel measurementsPanel;
    private JPanel factsPanel;
    private JPanel mainPanel;
    private JTextField cubeNameField;

    public CubeConfiguration (List<GlobalTableData> globalTables){
        this.globalTables = globalTables;
        helpLabel.setText("<html>Create a star schema from the previously created Global Schema. "
                +"<br/> Select dimensions, facts and measurements.</html>");
        stepLabel.setText("Step 3/4");
        stepLabel.setFont(new Font("", Font.PLAIN, 18));

        String[] tableNames = new String[globalTables.size()];
        for (int i = 0; i < globalTables.size(); i++)
            tableNames[i] = globalTables.get(i).getTableName();
        dimensionTableComboBox.setModel(new DefaultComboBoxModel(tableNames));
        dimensionTableComboBox.setSelectedIndex(0);
        selectedFactsTable = 0;
        measuresCheckBoxes = new HashMap<>();
        factsColumns = new HashMap<>();
        dimsCheckBoxes = new HashMap<>();
        setFactsCheckBox();
        setMeasuresCheckBox();

        dimensionTableComboBox.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedFactsTable = dimensionTableComboBox.getSelectedIndex();
                setFactsCheckBox();
                setMeasuresCheckBox();
            }
        });
        this.add(mainPanel);
        this.setVisible(true);
    }

    private void setFactsCheckBox(){
        factsPanel.removeAll();
        factsPanel.setLayout(new BoxLayout(factsPanel, BoxLayout.PAGE_AXIS));
        dimsCheckBoxes.clear();
        for (int i = 0; i < globalTables.size(); i++){
            if (i == selectedFactsTable)
                continue;
            JCheckBox checkBox = new JCheckBox(globalTables.get(i).getTableName());
            factsPanel.add(checkBox);
            dimsCheckBoxes.put(i, checkBox);
        }
        factsPanel.revalidate();
        factsPanel.repaint();
    }

    private void setMeasuresCheckBox(){
        measurementsPanel.removeAll();
        measurementsPanel.setLayout(new BoxLayout(measurementsPanel, BoxLayout.PAGE_AXIS));
        GlobalTableData gtFacts = globalTables.get(selectedFactsTable);
        List<GlobalColumnData> globalCols = gtFacts.getGlobalColumnDataList();
        measuresCheckBoxes.clear();
        for (int i = 0; i < globalCols.size(); i++){
            GlobalColumnData globalCol = globalCols.get(i);
            if (globalCol.isNumeric() && !globalCol.isForeignKey()) {
                //measures are usually numeric types and are not foreign keys
                JCheckBox checkBox = new JCheckBox(globalCols.get(i).getName() + " (" + globalCols.get(i).getDataType() + ")");
                measurementsPanel.add(checkBox);
                measuresCheckBoxes.put(globalCol, checkBox);
            }
        }
        measurementsPanel.revalidate();
        measurementsPanel.repaint();
    }

    private List<GlobalTableData> getDimensionTables(){
        List<GlobalTableData> dimsTablesList = new ArrayList<>();
        for (Map.Entry<Integer, JCheckBox> entry : dimsCheckBoxes.entrySet()) {
            Integer index = entry.getKey();
            JCheckBox checkBox = entry.getValue();
            if (checkBox.isSelected())
                dimsTablesList.add(globalTables.get(index));
        }
        return dimsTablesList;
    }

    /**
     * get columns of the facts table and mark the columns that are measurements. A column is a measurement if the user checked
     * its respective CheckBox.
     * @return
     */
    private Map<GlobalColumnData, Boolean> getFactsColumns(){
        Map<GlobalColumnData, Boolean> factsCols = new HashMap<>();
        List<GlobalColumnData> globalCols = globalTables.get(selectedFactsTable).getGlobalColumnDataList();
        //iterate all cols
        for (int i = 0; i < globalCols.size(); i++){
            GlobalColumnData globalCol = globalCols.get(i);
            if (measuresCheckBoxes.containsKey(globalCol) && measuresCheckBoxes.get(globalCol).isSelected()){
                factsCols.put(globalCol, true);
            }
            else
                factsCols.put(globalCol, false);
        }
        return factsCols;
    }

    public StarSchema getMultiDimSchema(){
        //TODO ensure cube  name does not exist already
        List<GlobalTableData> dimTables = getDimensionTables();
        if(!this.cubeNameField.getText().isEmpty() && this.selectedFactsTable > -1 && dimTables.size() > 0){
            FactsTable factsTable = new FactsTable(globalTables.get(selectedFactsTable), getFactsColumns());
            StarSchema starSchema = new StarSchema(cubeNameField.getText(), factsTable, dimTables);
            return starSchema;
        }
        JOptionPane.showMessageDialog(null,
                "Please, type a cube name and select the dimensions and the facts table",
                "Inane warning",
                JOptionPane.WARNING_MESSAGE);
        return null;
    }
}
