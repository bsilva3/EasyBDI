package main_app.query_ui;

import helper_classes.utils_other.Constants;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class FilterNode extends DefaultMutableTreeNode implements Serializable {

    /**
     * The icon which is displayed on the JTree object. open, close, leaf icon.
     */
    private String escappedFilterStringObj;
    private ImageIcon icon;
    private FilterNodeType nodeType;
    private Serializable obj;

    public FilterNode(String userObject, Serializable obj, String escappedFilterStringObj, FilterNodeType nodeType) {
        super(userObject);
        this.obj = obj;
        this.nodeType = nodeType;
        this.escappedFilterStringObj = escappedFilterStringObj;
        this.icon = loadImageForLocalSchemaTree(nodeType);
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public FilterNodeType getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(FilterNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Serializable getObj() {
        return this.obj;
    }

    public void setObj(Serializable obj) {
        this.obj = obj;
    }

    public String getEscappedFilterStringObj() {
        return escappedFilterStringObj;
    }

    public void setEscappedFilterStringObj(String escappedFilterStringObj) {
        this.escappedFilterStringObj = escappedFilterStringObj;
    }

    public ImageIcon loadImageForLocalSchemaTree(FilterNodeType type){
        if (type == null)
            return null;//no icon
        String fileName = "";
        switch (type) {
            case BOOLEAN_OPERATION: //database
                if (userObject.toString().equalsIgnoreCase("AND")){
                    fileName = "and_icon.png";
                }
                else if (userObject.toString().equalsIgnoreCase("OR")){
                    fileName = "or_icon.png";
                }
                else if (userObject.toString().equalsIgnoreCase("NOT")){
                    fileName = "not_icon.jpg";
                }
                break;
            case CONDITION: //database
                fileName = "condition_icon.png";
                break;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(Constants.IMAGES_DIR+fileName));
        } catch (IOException e) {
        }
        return new ImageIcon(img.getScaledInstance(20,20, 5));
    }


}