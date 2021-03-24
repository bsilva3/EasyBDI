package main_app;

import helper_classes.utils_other.Constants;
import main_app.metadata_storage.MetaDataManager;
import main_app.query_ui.QueryUI;
import main_app.wizards.star_schema.CubeConfiguration;
import main_app.wizards.MainWizardPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static helper_classes.utils_other.Constants.*;

public class EasyBDI extends JFrame {
    private JButton queryExecutionBtn;
    private JButton newProjectBtn;
    private JButton editProjectBtn;
    private JButton createStarSchemaBtn;
    private JPanel mainPanel;
    private JComboBox projectsComboBox;
    private JButton deleteSelectedProjectButton;
    private JLabel warningsLabel;
    private JLabel easybdiText;
    //button icons
    private Image queryBtnImage;
    private Image newProjectBtnImage;
    private Image editProjectBtnImage;
    private Image newStarSchemaBtnImage;

    private String currentProjectSelected;

    private Dimension windowSize;

    private final String mainMenuTitle = "EasyBDI - Main Menu";

    private static final String HELP_MESSAGE = "java -jar EasyBDI.jar <path to Presto/Trino> <path to projects directory>\n -h to see this message";

    public EasyBDI(String prestoDir, String projectDir) {

        PRESTO_DIR = prestoDir;
        PROJECT_DIR = projectDir;
        PRESTO_BIN = PRESTO_DIR + File.separator + "bin";
        PRESTO_PROPERTIES_FOLDER = PRESTO_DIR + File.separator + "etc" + File.separator + "catalog" + File.separator;
        if (PRESTO_DIR.isBlank()) {
            System.out.println("Presto/Trino directory is blank. Please provide a valid Presto directory");
        }
        if (!PRESTO_DIR.endsWith(File.separator)) {
            PRESTO_DIR += File.separator;
        }
        if (PROJECT_DIR.isBlank()) {
            System.out.println("A directory to save projects is blank. Please provide a valid project directory");
        }
        if (!PROJECT_DIR.endsWith(File.separator)) {
            PROJECT_DIR += File.separator;
        }

        try {
            queryBtnImage = ImageIO.read(this.getClass().getClassLoader().getResource(Constants.IMAGES_DIR + "query_icon.jpg"));
            queryExecutionBtn.setIcon(new ImageIcon(queryBtnImage.getScaledInstance(80, 80, 0)));
            editProjectBtnImage = ImageIO.read(this.getClass().getClassLoader().getResource(Constants.IMAGES_DIR + "edit_project_icon.png"));
            editProjectBtn.setIcon(new ImageIcon(editProjectBtnImage.getScaledInstance(80, 80, 0)));
            newProjectBtnImage = ImageIO.read(this.getClass().getClassLoader().getResource(Constants.IMAGES_DIR + "new_project_icon.png"));
            newProjectBtn.setIcon(new ImageIcon(newProjectBtnImage.getScaledInstance(80, 80, 0)));
            newStarSchemaBtnImage = ImageIO.read(this.getClass().getClassLoader().getResource(Constants.IMAGES_DIR + "new_star_schema_icon.png"));
            createStarSchemaBtn.setIcon(new ImageIcon(newStarSchemaBtnImage.getScaledInstance(80, 80, 0)));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //fill combobox with projects
        refreshProjectsInComboBox();
        //this.prestoDirLabel.setText(Constants.PRESTO_DIR);
        projectsComboBox.setSelectedIndex(0);
        currentProjectSelected = projectsComboBox.getSelectedItem().toString();
        checkProject();
        setListeners();
        easybdiText.setFont(new Font("Serif", Font.PLAIN, 28));

        windowSize = new Dimension(1050, 850);
        this.setPreferredSize(windowSize);
        this.setLayout(new BorderLayout());
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle(mainMenuTitle);
        this.pack();
        this.setVisible(true);

    }

    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equals("-h"))) {
            System.out.println(HELP_MESSAGE);
            System.exit(0);
        }

        EasyBDI m = new EasyBDI(args[0], args[1]);
    }

    private void setListeners() {

        newProjectBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createNewProjectWizard();
            }
        });

        editProjectBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open wizard and edit current project
                editCurrentProject();
            }
        });

        createStarSchemaBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open query window with selected project in combo box
                createNewStarSchema();
            }
        });

        queryExecutionBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open query window with selected project in combo box
                openQueryUI();
            }
        });

        deleteSelectedProjectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //open query window with selected project in combo box
                deleteProject();
            }
        });

        projectsComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentProjectSelected = projectsComboBox.getSelectedItem().toString();
                checkProject();
            }
        });
    }

    private void checkProject() {
        MetaDataManager m = new MetaDataManager(currentProjectSelected);
        if (m.getDatabaseCount() <= 0) {
            warningsLabel.setText("<html>This project does not have a Local Schema. Please create one by using the 'Edit Project' button and adding data sources to the project.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        }
        if (m.getGlobalTablesCount() <= 0) {
            warningsLabel.setText("<html>This project does not have a Global Schema. Please create one by using the 'Edit Project' button and moving on to the 'Global Schema Configuration Window'.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        } else if (m.getStarSchemaNames().size() <= 0) {
            warningsLabel.setText("<html>This project does not have any Star Schemas. At least one is required to execute analytical queries. Used the 'Create Star Schema' button to create a new one.</html>");
            createStarSchemaBtn.setEnabled(false);
            queryExecutionBtn.setEnabled(false);
        } else {
            warningsLabel.setText("<html>This project is ready to be used for analytical queries (Local schema, Global schema and at least one Star schema are configured).</html>");
            createStarSchemaBtn.setEnabled(true);
            queryExecutionBtn.setEnabled(true);
        }
        m.close();
    }

    private void deleteProject() {
        String projectName = currentProjectSelected;
        int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure? The project will be permanently deleted.", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            boolean success = MetaDataManager.deleteProject(projectName);
            if (success) {
                refreshProjectsInComboBox();
                JOptionPane.showMessageDialog(null, "Project deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to delete project.\nProject may no longer exist or does not have permition to be deleted.", "Failed to delete", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public void changeTitle(String title) {
        this.setTitle(title);
    }

    private void openQueryUI() {
        placePanelInFrame(new QueryUI(currentProjectSelected, this));
    }

    private void createNewStarSchema() {
        placePanelInFrame(new CubeConfiguration(currentProjectSelected, this));
    }


    private void createNewProjectWizard() {
        //dialog for user to enter project name
        String projectName = (String) JOptionPane.showInputDialog(
                this,
                "Please, type project name",
                "Project Name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "My Project");
        if (projectName == null || projectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Empty project name", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (MetaDataManager.dbExists(projectName)) {
            JOptionPane.showMessageDialog(this, "Project Name already exists. Please choose a diferent name.", "Failed to create project", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //open wizard and add new project
        placePanelInFrame(new MainWizardPanel(this, projectName, false));
    }

    private void editCurrentProject() {
        placePanelInFrame(new MainWizardPanel(this, currentProjectSelected, true));
    }


    private void placePanelInFrame(JPanel panel) {
        setContentPane(panel);
        this.pack();
        revalidate();
    }

    public void returnToMainMenu() {
        changeTitle(mainMenuTitle);
        placePanelInFrame(mainPanel);
        this.pack();
        refreshProjectsInComboBox();
        checkProject();
    }

    private void refreshProjectsInComboBox() {
        String[] dbNames = MetaDataManager.listAllProjectNames();
        projectsComboBox.setModel(new DefaultComboBoxModel(dbNames));
        for (String s : dbNames) {
            if (s.equals(currentProjectSelected)) {
                projectsComboBox.setSelectedItem(currentProjectSelected);//project name selected is still in list, use it as selected
            }
        }
    }

    private void handleFileConfiguration(String configFile) throws IOException {
        Map<String, String> properties = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.strip().startsWith("#") || line.isBlank() || line.isEmpty())
                    continue;
                String[] lineSplit = line.split("=");
                properties.put(lineSplit[0].strip(), lineSplit[1]);
            }
        }
        if (!properties.containsKey(PRESTO_DIR_PROPERTY))
            JOptionPane.showMessageDialog(null, "Invalid Configuration File", "Property " + PRESTO_DIR_PROPERTY + " not present.\n Exiting", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
        if (!properties.containsKey(PROJECT_DIR_PROPERTY))
            JOptionPane.showMessageDialog(null, "Invalid Configuration File", "Property " + PROJECT_DIR_PROPERTY + " not present.\n Exiting", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
        Constants.PRESTO_DIR = properties.get(PRESTO_DIR_PROPERTY);
        Constants.PROJECT_DIR = properties.get(PROJECT_DIR_PROPERTY);
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
        mainPanel.setVisible(true);
        newProjectBtn = new JButton();
        newProjectBtn.setText("Create new Project");
        newProjectBtn.setToolTipText("Create new project");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(newProjectBtn, gbc);
        queryExecutionBtn = new JButton();
        queryExecutionBtn.setText("Perform Queries");
        queryExecutionBtn.setToolTipText("Start Analytical Queries");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.gridheight = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        mainPanel.add(queryExecutionBtn, gbc);
        createStarSchemaBtn = new JButton();
        createStarSchemaBtn.setText("Create Star Schema");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.gridheight = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(createStarSchemaBtn, gbc);
        editProjectBtn = new JButton();
        editProjectBtn.setText("Edit Current Project");
        editProjectBtn.setToolTipText("Edit currently selected Project");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(editProjectBtn, gbc);
        easybdiText = new JLabel();
        easybdiText.setHorizontalAlignment(0);
        easybdiText.setHorizontalTextPosition(0);
        easybdiText.setText("Easy BDI");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        mainPanel.add(easybdiText, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 5);
        mainPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Select Project");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(label1, gbc);
        projectsComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(projectsComboBox, gbc);
        deleteSelectedProjectButton = new JButton();
        deleteSelectedProjectButton.setText("Delete Selected Project");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.NORTH;
        panel1.add(deleteSelectedProjectButton, gbc);
        warningsLabel = new JLabel();
        warningsLabel.setHorizontalAlignment(0);
        warningsLabel.setHorizontalTextPosition(0);
        warningsLabel.setMaximumSize(new Dimension(60, 40));
        warningsLabel.setPreferredSize(new Dimension(40, 40));
        warningsLabel.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(warningsLabel, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
