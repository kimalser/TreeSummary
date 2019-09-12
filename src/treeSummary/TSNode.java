package treeSummary;

import genericStructures.ReconstructionError;
import genericStructures.LatticeNode;
import genericStructures.TreeNode;
import utilities.Utility;

import java.io.BufferedWriter;
import java.util.Map;

public class TSNode extends TreeNode {
    private double weight; // if the weight is NaN, use the closestAncestorWeight as approximation
    private double closestAncestorWeight;

    TSNode(TSNode parent, String name) {
        super(parent, name);
    }

    void setWeight(double weight) {
        this.weight = weight;
    }

    double getClosestAncestorWeight() {
        return closestAncestorWeight;
    }

    void setClosestAncestorWeight(double closestAncestorWeight) {
        this.closestAncestorWeight = closestAncestorWeight;
    }

    public int getTsSize() {
        int size = 0;
        if (!Double.isNaN(Math.abs(weight)))
            size++;
        for (TreeNode n : getChildren()) {
            size += ((TSNode) n).getTsSize();
        }
        return size;
    }

    public void printSizeByLevel() {
        int i = 0;
        while (true) {
            int nonOnes = tsSizeByLevel(0, i);
            int all = nodeNumByLevel(0, i);
            if (all == 0) break;
            System.out.println(nonOnes + " / " + all);
            i++;
        }
        System.out.println("------------------");
        System.out.println(getTsSize() + " / " + getSubtreeSize() + "\n");

    }

    private int nodeNumByLevel(int curr, int desired) {
        if (curr == desired)
            return 1;
        int count = 0;
        for (TreeNode n : getChildren()) {
            count += ((TSNode) n).nodeNumByLevel(curr + 1, desired);
        }
        return count;
    }

    private int tsSizeByLevel(int curr, int desired) {
        if (curr == desired && !Double.isNaN(weight))
            return 1;
        int count = 0;
        for (TreeNode n : getChildren()) {
            count += ((TSNode) n).tsSizeByLevel(curr + 1, desired);
        }
        return count;
    }

    public ReconstructionError getReconstructionError(Map<String, LatticeNode> map, BufferedWriter bw) throws Exception {
        ReconstructionError error = new ReconstructionError();
        return getReconstructionErrorHelper(error, map, bw);
    }

    private ReconstructionError getReconstructionErrorHelper(ReconstructionError error, Map<String, LatticeNode> map, BufferedWriter bw) throws Exception {
        if (getChildren().isEmpty()) {
            double origVal = map.get(getName()).getValue();
            double e = Utility.calculateSMAPE(origVal, closestAncestorWeight);
            error.sum += e;
            if (e > error.worst)
                error.worst = e;
            if (bw != null)
                bw.write(e + ",");
        } else {
            for (TreeNode child : getChildren())
                ((TSNode) child).getReconstructionErrorHelper(error, map, bw);
        }
        return error;
    }

    public void printSummaryTree(boolean withWeights) {
        if (withWeights)
            System.out.println(getName() + ": " + weight);
        else System.out.println(getName());
        for (TreeNode child : getChildren()) {
            ((TSNode) child).printSummaryTree(1, withWeights);
        }
    }

    private void printSummaryTree(int spaces, boolean withWeights) {
        for (int i = 0; i < spaces; i++)
            System.out.print("  ");
        if (withWeights)
            System.out.println(getName() + ": " + weight);
        else System.out.println(getName());
        for (TreeNode child : getChildren()) {
            ((TSNode) child).printSummaryTree(spaces + 1, withWeights);
        }
    }

    public void printThisNode() {
        System.out.println(getName() + ": " + weight);
    }

}
