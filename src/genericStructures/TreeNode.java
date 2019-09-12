package genericStructures;

import java.util.ArrayList;
import java.util.List;

/* The most basic notion of a tree node. The basis of more complex tree nodes and
 * is also used for creating a lattice (i.e. a cross-product of trees).
 * Particularly, it is used to store tree hierarchies (e.g. time, geography) to be
 * later used in lattice creation.
 */

public class TreeNode {
    private List<TreeNode> children;
    private TreeNode parent;
    private String name;

    public TreeNode(TreeNode parent, String name) {
        this.name = name;
        this.parent = parent;
        children = new ArrayList<>();
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeNode getParent() {
        return parent;
    }

    public TreeNode findNode(String name) {
        if (this.name.equals(name)) {
            return this;
        } else {
            for (TreeNode n : children)
                if (n.findNode(name) != null)
                    return n.findNode(name);
        }
        return null;
    }

    public int totalLeaves() {
        if (getChildren().isEmpty()) {
            return 1;
        } else {
            int count = 0;
            for (TreeNode n : getChildren())
                count += n.totalLeaves();
            return count;
        }
    }

    public int getSubtreeSize() {
        int count = 1;
        for (TreeNode n : getChildren()) {
            count += n.getSubtreeSize();
        }
        return count;
    }
}
