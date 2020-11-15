package main_app.query_ui;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Frame to show text in a text area
 */
public class InfoViewer extends JFrame{
    private JTextArea textArea;
    private JPanel mainPanel;
    private JButton closeButton;
    private JLabel labelT;

    public InfoViewer(String labelText, String text){
        this.setPreferredSize(new Dimension(450, 600));
        setContentPane(mainPanel);
        labelT.setText(labelText);
        textArea.setText(text);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Query Load selection");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        closeButton.addActionListener(e -> InfoViewer.this.dispose());
    }
}
