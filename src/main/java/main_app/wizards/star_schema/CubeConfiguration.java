package main_app.wizards.star_schema;

import com.intellij.uiDesigner.core.GridLayoutManager;
import helper_classes.FactsTable;
import helper_classes.GlobalColumnData;
import helper_classes.GlobalTableData;
import helper_classes.StarSchema;
import main_app.EasyBDI;
import main_app.metadata_storage.MetaDataManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeConfiguration extends JPanel {
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
    private JButton cancelAndReturnButton;
    private JButton saveButton;

    private MetaDataManager metaDataManager;
    private boolean isWizard;
    private EasyBDI mainMenu;

    public CubeConfiguration(String projectName, EasyBDI mainMenu) {
        this.mainMenu = mainMenu;
        this.isWizard = false;
        this.metaDataManager = new MetaDataManager(projectName);
        this.globalTables = metaDataManager.getGlobalSchema();
        initUI();
    }

    public CubeConfiguration(List<GlobalTableData> globalTables, MetaDataManager metaDataManager) {
        this.isWizard = true;
        this.metaDataManager = metaDataManager;
        this.globalTables = globalTables;
        initUI();
    }

    private void initUI() {
        helpLabel.setText("<html>Create a star schema from the previously created Global Schema. "
                + "<br/> Select dimensions, facts and measurements.</html>");
        if (isWizard) {
            stepLabel.setText("Step 4/4");
            stepLabel.setFont(new Font("", Font.PLAIN, 18));
        }
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

        dimensionTableComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedFactsTable = dimensionTableComboBox.getSelectedIndex();
                setFactsCheckBox();
                setMeasuresCheckBox();
            }
        });
        if (!isWizard) {
            cancelAndReturnButton.setVisible(true);
            saveButton.setVisible(true);
            cancelAndReturnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!isWizard)
                        metaDataManager.close();
                    mainMenu.returnToMainMenu();
                }
            });
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    StarSchema starSchema = getMultiDimSchema();
                    boolean success = metaDataManager.insertStarSchema(starSchema);
                    if (success) {
                        JOptionPane.showMessageDialog(null, "Star Schema created!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to create star Schema.\nThere could be a problem with the global schema.", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                    if (!isWizard)
                        metaDataManager.close();
                    mainMenu.returnToMainMenu();
                }
            });
        }
        this.add(mainPanel);
        this.setVisible(true);
    }

    private void setFactsCheckBox() {
        factsPanel.removeAll();
        factsPanel.setLayout(new BoxLayout(factsPanel, BoxLayout.PAGE_AXIS));
        dimsCheckBoxes.clear();
        for (int i = 0; i < globalTables.size(); i++) {
            if (i == selectedFactsTable)
                continue;
            JCheckBox checkBox = new JCheckBox(globalTables.get(i).getTableName());
            factsPanel.add(checkBox);
            dimsCheckBoxes.put(i, checkBox);
        }
        factsPanel.revalidate();
        factsPanel.repaint();
    }

    private void setMeasuresCheckBox() {
        measurementsPanel.removeAll();
        measurementsPanel.setLayout(new BoxLayout(measurementsPanel, BoxLayout.PAGE_AXIS));
        GlobalTableData gtFacts = globalTables.get(selectedFactsTable);
        List<GlobalColumnData> globalCols = gtFacts.getGlobalColumnDataList();
        measuresCheckBoxes.clear();
        for (int i = 0; i < globalCols.size(); i++) {
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

    private List<GlobalTableData> getDimensionTables() {
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
     *
     * @return
     */
    private Map<GlobalColumnData, Boolean> getFactsColumns() {
        Map<GlobalColumnData, Boolean> factsCols = new HashMap<>();
        List<GlobalColumnData> globalCols = globalTables.get(selectedFactsTable).getGlobalColumnDataList();
        //iterate all cols
        for (int i = 0; i < globalCols.size(); i++) {
            GlobalColumnData globalCol = globalCols.get(i);
            if (measureCheckBoxContainsGlobalColIDAnsIsSelected(globalCol)) {
                factsCols.put(globalCol, true);
            } else
                factsCols.put(globalCol, false);
        }
        return factsCols;
    }

    private boolean measureCheckBoxContainsGlobalColIDAnsIsSelected(GlobalColumnData g) {
        for (Map.Entry<GlobalColumnData, JCheckBox> map : measuresCheckBoxes.entrySet()) {
            GlobalColumnData t = map.getKey();
            boolean isChecked = map.getValue().isSelected();
            if (t.getName().equals(g.getName()) && isChecked) {
                return true;
            }
        }
        return false;
    }

    public StarSchema getMultiDimSchema() {
        //TODO ensure cube  name does not exist already
        List<GlobalTableData> dimTables = getDimensionTables();
        if (!this.cubeNameField.getText().isEmpty() && this.selectedFactsTable > -1 && dimTables.size() > 0) {
            //check that cube does not exist already
            if (metaDataManager.getCubeID(this.cubeNameField.getText()) != -1) {
                JOptionPane.showMessageDialog(null,
                        "This cube name was previously used for another cube in the same project. Please, use another name.",
                        "warning",
                        JOptionPane.WARNING_MESSAGE);
            }
            FactsTable factsTable = new FactsTable(globalTables.get(selectedFactsTable), getFactsColumns());
            StarSchema starSchema = new StarSchema(cubeNameField.getText(), factsTable, dimTables);
            return starSchema;
        }
        JOptionPane.showMessageDialog(null,
                "Please, type a cube name and select the dimensions and the facts table",
                "warning",
                JOptionPane.WARNING_MESSAGE);
        return null;
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
        final JLabel label1 = new JLabel();
        label1.setText("Select the facts table");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Select dimensions");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(label2, gbc);
        factsPanel = new JPanel();
        factsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(factsPanel, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Select Measurements");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(label3, gbc);
        stepLabel = new JLabel();
        stepLabel.setHorizontalAlignment(0);
        stepLabel.setHorizontalTextPosition(0);
        stepLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(stepLabel, gbc);
        helpLabel = new JLabel();
        helpLabel.setHorizontalAlignment(0);
        helpLabel.setHorizontalTextPosition(0);
        helpLabel.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(helpLabel, gbc);
        dimensionTableComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        mainPanel.add(dimensionTableComboBox, gbc);
        measurementsPanel = new JPanel();
        measurementsPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 0, 0, 0);
        mainPanel.add(measurementsPanel, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.VERTICAL;
        mainPanel.add(spacer1, gbc);
        final JLabel label4 = new JLabel();
        label4.setText("Cube Name:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(label4, gbc);
        cubeNameField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(cubeNameField, gbc);
        cancelAndReturnButton = new JButton();
        cancelAndReturnButton.setText("Cancel and Return");
        cancelAndReturnButton.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(cancelAndReturnButton, gbc);
        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.setVisible(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(saveButton, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
