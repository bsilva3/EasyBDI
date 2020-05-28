package prestoComm;

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
    private JPanel mainPanel;
    private JComboBox projectsComboBox;
    //button icons
    private Image queryBtnImage;
    private Image newProjectBtnImage;
    private Image editProjectBtnImage;

    //windows
    private MainWizardPanel wizard;

    public MainMenu(){
        try {
            queryBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"query_icon.jpg"));
            queryExecutionBtn.setIcon(new ImageIcon(queryBtnImage.getScaledInstance(80, 80, 0)));
            editProjectBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"edit_project_icon.png"));
            editProjectBtn.setIcon(new ImageIcon(editProjectBtnImage.getScaledInstance(80, 80, 0)));
            newProjectBtnImage = ImageIO.read(new File(Constants.IMAGES_DIR+"new_project_icon.png"));
            newProjectBtn.setIcon(new ImageIcon(newProjectBtnImage.getScaledInstance(80, 80, 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //fill combobox with projects
        String[] dbNames = MetaDataManager.listAllDBNames();
        projectsComboBox.setModel(new DefaultComboBoxModel(dbNames));
        setListeners();

        this.setPreferredSize(new Dimension(950, 800));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Data source configuration wizard");
        pack();
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
            }
        });

        editProjectBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open wizard and edit current project
            }
        });

        queryExecutionBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                //open query window
            }
        });
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
        if (projectName.isEmpty()){
            JOptionPane.showMessageDialog(this, "Empty project name", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (MetaDataManager.dbExists(projectName)){
            JOptionPane.showMessageDialog(this, "Project Name already exists. Please choose a diferent name.", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //open wizard and add new project
        placePanelInFrame(new MainWizardPanel(this, projectName));
    }

    private void placePanelInFrame(JPanel panel){
        setContentPane(panel);
        revalidate();
    }

    public void returnToMainMenu(){
        placePanelInFrame(mainPanel);
    }


}
