import se.gustavkarlsson.gwiz.AbstractWizardPage;
import se.gustavkarlsson.gwiz.WizardController;
import se.gustavkarlsson.gwiz.wizards.JFrameWizard;
import wizards.global_schema_config.GlobalSchemaConfigurationV2;;import java.awt.*;

public class WizardTest {

    public static void main (String[] args){

        /**example:
         * // Create a new wizard (this one is based on a JFrame)
         * 		JFrameWizard wizard = new JFrameWizard("g-wiz demo");
         *
         * 		// Create the first page of the wizard
         * 		AbstractWizardPage demoStartPage = new StartPage();
         *
         * 		// Create the controller for wizard
         * 		WizardController wizardController = new WizardController(wizard);
         *
         * 		// Start the wizard and show it
         * 		wizard.setVisible(true);
         * 		wizardController.startWizard(demoStartPage);
         */
        // Create a new wizard (this one is based on a JFrame)
        JFrameWizard wizard = new JFrameWizard("Configuration Wizard");

        // Create the first page of the wizard
       /*AbstractWizardPage demoStartPage = new GlobalSchemaConfigurationV2();

        // Create the controller for wizard
        WizardController wizardController = new WizardController(wizard);

        // Start the wizard and show it
        wizard.setResizable(true);
        wizard.setVisible(true);
        wizardController.startWizard(demoStartPage);*/
    }
}
