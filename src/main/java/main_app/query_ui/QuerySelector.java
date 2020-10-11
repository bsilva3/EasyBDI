package main_app.query_ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class QuerySelector extends JFrame {
    private JList queryList;
    private DefaultListModel listModel;
    private JPanel mainPanel;
    private JButton loadSelectedButton;
    private JButton backButton;
    private JButton deleteSelectedButton;
    private List<String> queryNames;
    private QueryUI queryUI;

    public QuerySelector(List<String> queryNames, QueryUI queryUI) {
        this.queryNames = queryNames;
        this.queryUI = queryUI;
        loadQueryNames();
        this.setPreferredSize(new Dimension(450, 600));
        setVisible(true);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle("Query Load selection");
        pack();
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuerySelector.this.dispose();
            }
        });
        loadSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectQuery();
                QuerySelector.this.dispose();
            }
        });
        deleteSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteQuery();
            }
        });
    }

    private void loadQueryNames(){
        listModel = new DefaultListModel();
        for (String q : queryNames)
            listModel.addElement(q);
        queryList.setModel(listModel);
    }

    private void selectQuery(){
        queryUI.loadSelectedQuery(queryList.getSelectedValue().toString());
    }

    private void deleteQuery(){
        String queryName = queryList.getSelectedValue().toString();
        listModel.remove(queryList.getSelectedIndex());
        queryUI.deleteQuery(queryName);
        queryList.revalidate();
        queryList.updateUI();
    }
}
