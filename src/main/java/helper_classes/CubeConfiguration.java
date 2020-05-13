package helper_classes;

import se.gustavkarlsson.gwiz.AbstractWizardPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CubeConfiguration extends AbstractWizardPage{
        //extends JFrame{
    private List<GlobalTableData> globalTables;
    private int selectedDimTable;
    private int[] selectedFactsTables;
    private JComboBox dimensionTableComboBox;
    private JLabel stepLabel;
    private JLabel helpLabel;
    private JPanel measurementsPanel;
    private JPanel factsPanel;
    private JPanel mainPanel;

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
        selectedDimTable = 0;
        setFactsCheckBox();

        dimensionTableComboBox.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedDimTable = dimensionTableComboBox.getSelectedIndex();
                setFactsCheckBox();
            }
        });
        add(mainPanel);
        this.setVisible(true);
    }

    public void setFactsCheckBox(){
        factsPanel.setLayout(new BoxLayout(factsPanel, BoxLayout.PAGE_AXIS));
        for (int i = 0; i < globalTables.size(); i++){
            JCheckBox checkBox = new JCheckBox(globalTables.get(i).getTableName());
            factsPanel.add(checkBox);
        }
    }

    //for g-wizard
    @Override
    protected AbstractWizardPage getNextPage() {
        //return new CubeConfiguration(this.getGlobalSchemaFromTree());
        return null;
    }

    @Override
    protected boolean isCancelAllowed() {
        return true;
    }

    @Override
    protected boolean isPreviousAllowed() {
        return true;
    }

    @Override
    protected boolean isNextAllowed() {
        return true;
    }

    @Override
    protected boolean isFinishAllowed() {
        return false;
    }
}
