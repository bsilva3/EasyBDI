import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

// taken from: https://stackoverflow.com/questions/14096725/jtree-set-custom-open-closed-icons-for-individual-groups

class CustomeTreeCellRenderer extends DefaultTreeCellRenderer {

    public CustomeTreeCellRenderer() {
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

//            if (!leaf) {
        CustomTreeNode node = (CustomTreeNode) value;

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