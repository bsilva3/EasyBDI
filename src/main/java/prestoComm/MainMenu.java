package prestoComm;

import prestoComm.query_ui.QueryUI;
import wizards.CubeConfiguration;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class MainMenu extends JFrame{
    private JButton queryExecutionBtn;
    private JButton newProjectBtn;
    private JButton editProjectBtn;
    private JButton createStarSchemaBtn;
    private JPanel mainPanel;
    private JComboBox projectsComboBox;
    private JButton deleteSelectedProjectButton;
    private JLabel warningsLabel;
    //button icons
    private Image queryBtnImage;
    private Image newProjectBtnImage;
    private Image editProjectBtnImage;
    private Image newStarSchemaBtnImage;

    private String currentProjectSelected;

    private Dimension windowSize;

    private final String mainMenuTitle = "Data source configuration wizard";

    public MainMenu(){
        try {
            queryBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"query_icon.jpg"));
            queryExecutionBtn.setIcon(new ImageIcon(queryBtnImage.getScaledInstance(80, 80, 0)));
            editProjectBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"edit_project_icon.png"));
            editProjectBtn.setIcon(new ImageIcon(editProjectBtnImage.getScaledInstance(80, 80, 0)));
            newProjectBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"new_project_icon.png"));
            newProjectBtn.setIcon(new ImageIcon(newProjectBtnImage.getScaledInstance(80, 80, 0)));
            newStarSchemaBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"new_star_schema_icon.png"));
            createStarSchemaBtn.setIcon(new ImageIcon(newStarSchemaBtnImage.getScaledInstance(80, 80, 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //fill combobox with projects
        refreshProjectsInComboBox();
        projectsComboBox.setSelectedIndex(0);
        currentProjectSelected = projectsComboBox.getSelectedItem().toString();
        checkProject();
        setListeners();

        windowSize = new Dimension(1050, 850);
        this.setPreferredSize(windowSize);
        this.setLayout(new BorderLayout());
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle(mainMenuTitle);
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args){
        MainMenu m = new MainMenu();
    }

    private void setListeners(){

        newProjectBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createNewProjectWizard();
            }
        });

        editProjectBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open wizard and edit current project
                editCurrentProject();
            }
        });

        createStarSchemaBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open query window with selected project in combo box
                createNewStarSchema();
            }
        });

        queryExecutionBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open query window with selected project in combo box
                openQueryUI();
            }
        });

        deleteSelectedProjectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open query window with selected project in combo box
                deleteProject();
            }
        });

        projectsComboBox.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                currentProjectSelected = projectsComboBox.getSelectedItem().toString();
                checkProject();
            }
        });
    }

    private void checkProject(){
        MetaDataManager m = new MetaDataManager(currentProjectSelected);
        if (m.getDatabaseCount() <= 0){
            warningsLabel.setText("<html>This project does not have a Local Schema. Please create one by using the 'Edit Project' button and adding data sources to the project.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        }
        if (m.getGlobalTablesCount() <= 0){
            warningsLabel.setText("<html>This project does not have a Global Schema. Please create one by using the 'Edit Project' button and moving on to the 'Global Schema Configuration Window'.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        }
        else if(m.getStarSchemaNames().size() <=0){
            warningsLabel.setText("<html>This project does not have any Star Schemas. At least one is required to execute analytical queries. Used the 'Create Star Schema' button to create a new one.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        }
        else{
            warningsLabel.setText("<html>This project is ready to be used for analytical queries (Local schema, Global schema and at least one Star schema are configured).</html>");
            createStarSchemaBtn.setEnabled(true);
            queryExecutionBtn.setEnabled(true);
        }
        m.close();
    }

    private void deleteProject(){
        String projectName = currentProjectSelected;
        int dialogResult = JOptionPane.showConfirmDialog (null, "Are you sure? The project will be permanently deleted.","Warning",JOptionPane.YES_NO_OPTION);
        if(dialogResult == JOptionPane.YES_OPTION){
            boolean success = MetaDataManager.deleteProject(projectName);
            if (success){
                refreshProjectsInComboBox();
                JOptionPane.showMessageDialog(null, "Project deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(null, "Failed to delete project.\nProject may no longer exist or does not have permition to be deleted.", "Failed to delete", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public void changeTitle(String title){
        this.setTitle(title);
    }

    private void openQueryUI(){
        placePanelInFrame(new QueryUI(currentProjectSelected, this));
    }

    private void createNewStarSchema(){
        placePanelInFrame(new CubeConfiguration(currentProjectSelected, this));
    }


    private void createNewProjectWizard(){
        //dialog for user to enter project name
        String projectName = (String)JOptionPane.showInputDialog(
                this,
                "Please, type project name",
                "Project Name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "My Project");
        if (projectName == null || projectName.isEmpty()){
            JOptionPane.showMessageDialog(this, "Empty project name", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (MetaDataManager.dbExists(projectName)){
            JOptionPane.showMessageDialog(this, "Project Name already exists. Please choose a diferent name.", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //open wizard and add new project
        placePanelInFrame(new MainWizardPanel(this, projectName, false));
    }

    private void editCurrentProject(){
        placePanelInFrame(new MainWizardPanel(this, currentProjectSelected, true));
    }


    private void placePanelInFrame(JPanel panel){
        setContentPane(panel);
        this.pack();
        revalidate();
    }

    public void returnToMainMenu(){
        changeTitle(mainMenuTitle);
        placePanelInFrame(mainPanel);
        this.pack();
        refreshProjectsInComboBox();
        checkProject();
    }

    private void refreshProjectsInComboBox(){
        String[] dbNames = MetaDataManager.listAllProjectNames();
        projectsComboBox.setModel(new DefaultComboBoxModel(dbNames));
        for (String s : dbNames){
            if (s.equals(currentProjectSelected)){
                projectsComboBox.setSelectedItem(currentProjectSelected);//project name selected is still in list, use it as selected
            }
        }

    }


}
