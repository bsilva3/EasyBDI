package helper_classes.ui_utils;

import helper_classes.utils_other.Constants;
import main_app.query_ui.QueryLog;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;

/**
 * Utility to create a loading screen
 */
public class LoadingScreenAnimator{

    private static JFrame frameGeneralLoading;
    private static JLabel messageLabel;

    public static void openGeneralLoadingAnimation(JFrame frame, String text) {
        frameGeneralLoading = new JFrame(" ");
        frameGeneralLoading.setLayout(new BorderLayout());
        frameGeneralLoading.setUndecorated(true);
        frameGeneralLoading.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        frameGeneralLoading.setLocationRelativeTo(frame);

        ImageIcon loading = new ImageIcon(LoadingScreenAnimator.class.getClassLoader().getResource(Constants.LOADING_GIF));
        //frameGeneralLoading.add(new JLabel(loading, JLabel.CENTER));
        frameGeneralLoading.add(new JLabel(loading),BorderLayout.CENTER);
        messageLabel = new JLabel(text);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frameGeneralLoading.add(messageLabel, BorderLayout.PAGE_END);

        frameGeneralLoading.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frameGeneralLoading.setSize(400, 200);
        frameGeneralLoading.setVisible(true);
        return;
    }

    public static void openGeneralLoadingOnlyText(JComponent comp, String text) {
        frameGeneralLoading = new JFrame(" ");
        frameGeneralLoading.setLayout(new BorderLayout());
        frameGeneralLoading.setUndecorated(true);
        frameGeneralLoading.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        frameGeneralLoading.setLocationRelativeTo(comp);

        messageLabel = new JLabel(text);
        messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.PLAIN, 15));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frameGeneralLoading.add(messageLabel, BorderLayout.CENTER);

        frameGeneralLoading.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frameGeneralLoading.setSize(400, 200);
        frameGeneralLoading.setVisible(true);
        return;
    }

    public static void setText(String text) {
        messageLabel.setText(text);
    }


    public static void closeGeneralLoadingAnimation() {
        if (frameGeneralLoading != null)
            frameGeneralLoading.dispose();

    }

}
