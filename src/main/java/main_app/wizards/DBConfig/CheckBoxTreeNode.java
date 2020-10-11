package main_app.wizards.DBConfig;

import main_app.wizards.global_schema_config.NodeType;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.Serializable;

public class CheckBoxTreeNode extends DefaultMutableTreeNode implements Serializable    {

        /**
         * The icon which is displayed on the JTree object. open, close, leaf icon.
         */
        private NodeType nodeType;
        private Object obj;

    public CheckBoxTreeNode(Object userObject, Object obj, NodeType nodeType) {
        super(userObject);
        //default
        this.nodeType = nodeType;
        this.obj = obj;
    }

    public NodeType getNodeType () {
        return this.nodeType;
    }

    public void setNodeType (NodeType nodeType){
        this.nodeType = nodeType;
    }

    public Object getObj () {
        return this.obj;
    }

    public void setObj (Object obj){
        this.obj = obj;
    }

}


