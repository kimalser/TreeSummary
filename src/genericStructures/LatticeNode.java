package genericStructures;

import java.util.ArrayList;
import java.util.List;

public class LatticeNode {
    public boolean isLeaf;
    public double value;
    public List<LatticeNode> parents; // list of parents in all dimensions
    public List<List<LatticeNode>> children; // lists of children in all dimensions
    public String name;

    public LatticeNode(String name) {
        this.isLeaf = false;
        this.name = name;
        parents = new ArrayList<>();
        children = new ArrayList<>();
        for (int i = 0; i < Lattice.dims; i++) {
            parents.add(null);
            children.add(new ArrayList<>());
        }
    }

    void addChild(LatticeNode child, int dim) {
        children.get(dim).add(child);
    }

    public void setValue(double value) {
        this.value = value;
        this.isLeaf = !Double.isNaN(value);
    }

    public double getValue() {
        return value;
    }

    public void setParent(LatticeNode parent, int dim) {
        parents.add(dim, parent);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LatticeNode))
            return false;
        else {
            LatticeNode n = (LatticeNode) o;
            return name.equals(n.name);
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void print() {
        System.out.println(name);
        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                child.print(1);
            }
        }
    }

    private void print(int spaces) {
        for (int i = 0; i < spaces; i++)
            System.out.print("  ");
        if (!isLeaf)
            System.out.print(name);
        else System.out.println(name + ": " + value);
        System.out.println();
        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                child.print(spaces + 1);
            }
        }
    }

    public void printNodeInfo() {
        System.out.println("Node: " + name);
        for (int dim = 0; dim < Lattice.dims; dim++) {
            if (parents.get(dim) == null)
                System.out.println("Parent in dim#" + (dim + 1) + ": null");
            else System.out.println("Parent in dim#" + (dim + 1) + ": " + parents.get(dim).name);
        }

        for (int dim = 0; dim < Lattice.dims; dim++) {
            if (!children.get(dim).isEmpty()) {
                System.out.print("Children in dim#" + (dim + 1) + ": ");
                for (LatticeNode n : children.get(dim)) {
                    System.out.print(n.name + " ");
                }
                System.out.println();
            }
        }

        if (isLeaf) {
            System.out.println("Leaf value: " + value);
        }

        System.out.println();
    }

    public int getLeafDescendantNum() {
        if (isLeaf)
            return 1;
        else {
            int num = 0;
            for (int d = 0; d < Lattice.dims; d++) {
                if (!children.get(d).isEmpty()) {
                    for (LatticeNode child : children.get(d)) {
                        num += child.getLeafDescendantNum();
                    }
                    break;
                }
            }
            return num;
        }
    }

    public boolean hasChildren() {
        for (List list : children) {
            if (!list.isEmpty())
                return true;
        }
        return false;
    }
}
