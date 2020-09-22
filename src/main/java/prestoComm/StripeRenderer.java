package prestoComm;

import javax.swing.*;
import java.awt.*;
//http://www.captaindebug.com/2011/07/creating-striped-renderer-for-swing.html#.X2iqhGhKiUk
public class StripeRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);

        if (index % 2 == 0) {
            label.setBackground(new Color(230, 230, 255));
        }
        return label;
    }
}