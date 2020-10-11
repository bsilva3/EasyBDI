package main_app.query_ui;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

// taken from: https://stackoverflow.com/questions/14096725/jtree-set-custom-open-closed-icons-for-individual-groups

public class FilterNodeCellRenderer extends DefaultTreeCellRenderer {

    public FilterNodeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

//            if (!leaf) {
        FilterNode node = (FilterNode) value;

        if (node.getIcon() != null) {
            //System.out.println(node + " - " + node.getIcon());
            setClosedIcon(node.getIcon());
            setOpenIcon(node.getIcon());
            setLeafIcon(node.getIcon());
        } else {
            //System.out.println(node + " - default");
            setClosedIcon(getDefaultClosedIcon());
            setLeafIcon(getDefaultLeafIcon());
            setOpenIcon(getDefaultOpenIcon());
        }
//            }

        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        return this;
    }
}