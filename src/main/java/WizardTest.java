import se.gustavkarlsson.gwiz.AbstractWizardPage;
import se.gustavkarlsson.gwiz.WizardController;
import se.gustavkarlsson.gwiz.wizards.JFrameWizard;;

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
        /*JFrameWizard wizard = new JFrameWizard("g-wiz demo");

        // Create the first page of the wizard
        AbstractWizardPage demoStartPage = new DatabaseConnectionWizard();

        // Create the controller for wizard
        WizardController wizardController = new WizardController(wizard);

        // Start the wizard and show it
        wizard.setVisible(true);
        wizardController.startWizard(demoStartPage);*/
    }
}
