package main_app.wizards.global_schema_config;

import helper_classes.utils_other.Constants;
import helper_classes.DBModel;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class CustomTreeNode extends DefaultMutableTreeNode implements Serializable {

    /**
     * The icon which is displayed on the JTree object. open, close, leaf icon.
     */
    private ImageIcon icon;
    private NodeType nodeType;
    private Object obj;

    public CustomTreeNode(Object userObject) {
        super(userObject);
        //default
        this.nodeType = NodeType.COLUMN_INFO;
        this.icon = loadImageForLocalSchemaTree(nodeType);
    }

    public CustomTreeNode(Object userObject, Object obj, NodeType nodeType) {
        super(userObject);
        this.obj = obj;
        this.nodeType = nodeType;
        this.icon = loadImageForLocalSchemaTree(nodeType);
    }

    public CustomTreeNode(Object userObject, NodeType nodeType) {
        super(userObject);
        this.nodeType = nodeType;
        this.icon = loadImageForLocalSchemaTree(nodeType);
    }


    public CustomTreeNode(Object userObject, Object obj, NodeType nodeType, boolean allowsChildren) {
        super(userObject, allowsChildren);
        this.obj = obj;
        this.nodeType = nodeType;
        this.icon = loadImageForLocalSchemaTree(nodeType);
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Object getObj() {
        return this.obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public ImageIcon loadImageForLocalSchemaTree(NodeType type){
        String fileName = "";
        switch (type) {
            case DATABASE: //database
                fileName = "database_icon.png";
                break;
            case DATABASE_MODEL: //database
                fileName = getDatabaseModelFileName((DBModel)this.getObj());
                break;
            case DATABASE_URL: //database
                fileName = "url_icon.png";
                break;
            case TABLE:
            case GLOBAL_TABLE:
            case TABLE_MATCHES:{
                fileName = "table_icon.jpg";
                break;
             }
            case GLOBAL_TABLES:
                fileName = "schema_icon.png";
                break;
            case COLUMN:
            case GLOBAL_COLUMN:
            case COLUMN_MATCHES: {
                fileName = "column_icon.png";
                break;
            }
            case PRIMARY_KEY:
                fileName = "primary_key_icon.png";
                break;
            case FOREIGN_KEY:
                fileName = "foreign_key.png";
                break;
            case MATCHES:
                fileName = "match_icon.png";
                break;
            case MEASURE:
                fileName = "measures_icon.png";
                break;
            case FACTS_TABLE:
                fileName = "fact_table_icon.png";
                break;
            case MEASURES:
                fileName = "measures_list.png";
                break;
            case FACTS_ATTR:
                fileName = "column_icon.png";
                break;
            default:
                fileName = "info_icon.png";
                break;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(Constants.IMAGES_DIR+fileName));
        } catch (IOException e) {
        }
        return new ImageIcon(img.getScaledInstance(20,20, 5));
    }

    private String getDatabaseModelFileName(DBModel model){
        switch (model){
            case MYSQL:
                return "mysql_icon.png";
            case PostgreSQL:
                return "postgresqlIcon.png";
            case SQLServer:
                return "sqlServer_icon.jpg";
            case Redis:
                return "redis_icon.png";
            case Cassandra:
                return "cassandra_icon.png";
            case MongoDB:
                return "mongo_icon.png";
            case HIVE:
                return "hive_icon.png";
        }
        return "database_icon.png";
    }


}