package exhaustiveTrees;

import genericStructures.*;
import main.Main;
import utilities.Utility;

import java.util.*;

import static exhaustiveTrees.ExhaustiveTrees.*;

public class ExhaustiveTreesNode extends LatticeNode {
    private List<Interval> intervals;
    private double rootToLeaf = 0;
    private double weight = 0;

    public ExhaustiveTreesNode(String name) {
        super(name);
        intervals = new ArrayList<>();
    }

    void computeDS() {
        intervals = new ArrayList<>();
        if (getChildren().isEmpty()) {
            double val = Main.valMap.get(name);
            Interval interval = val > 0 ? new Interval(val - val * alpha, val + val * alpha) :
                    new Interval(val + val * alpha, val - val * alpha);
            intervals.add(interval);
        } else {
            Map<IntervalPoint, IntervalPoint> pointMap = new HashMap<>();
            List<IntervalPoint> points = new ArrayList<>();

            for (LatticeNode child : getChildren()) {
                ((ExhaustiveTreesNode) child).computeDS();
                for (Interval interval : ((ExhaustiveTreesNode) child).intervals) {
                    Utility.addPointIntervals(pointMap, points, interval);
                }
            }

            if (!points.isEmpty()) {
                List<double[]> mostOverlappedIntervals = Utility.getMostOverlappedIntervals(points, pointMap);
                for (double[] overlap : mostOverlappedIntervals) {
                    intervals.add(new Interval(overlap[0], overlap[1]));
                }
            }
        }
    }

    void weightTree() {
        Interval proposedInterval = intervals.get(0); // you can get any interval, so we just pick the first one

        weight = proposedInterval.getMidPoint();
        rootToLeaf = weight;

        for (LatticeNode child : getChildren()) {
            ((ExhaustiveTreesNode) child).weightTreeHelper(rootToLeaf);
        }
    }

    private void weightTreeHelper(double parentRootToLeaf) {

        boolean matchedInterval = false;
        for (Interval proposedInterval : intervals) {
            if (proposedInterval.containsPoint(parentRootToLeaf)) {
                weight = 0;
                rootToLeaf = parentRootToLeaf;
                matchedInterval = true;
                break;
            }
        }
        if (!matchedInterval) {
            Interval proposedInterval = intervals.get(0);
            weight = proposedInterval.getMidPoint() - parentRootToLeaf;
            rootToLeaf = proposedInterval.getMidPoint();
        }

        for (LatticeNode child : getChildren()) {
            ((ExhaustiveTreesNode) child).weightTreeHelper(rootToLeaf);
        }
    }

    int getCurrentSize(LatticeNode root) {
        int count = 0;
        if (weight != 0 || this.equals(root))
            count++;
        for (LatticeNode child : getChildren()) {
            count += ((ExhaustiveTreesNode) child).getCurrentSize(root);
        }
        return count;
    }

    List<LatticeNode> getChildren() {
        if (!isLeaf)
            return children.get(nodeDimsMap.get(name));
        else return children.get(0); // return empty list
    }


    List<LatticeNode> getPreviousNodes(LatticeNode root) {
        List<LatticeNode> previousNodes = new ArrayList<>();
        LatticeNode temp = this;
        while (!temp.equals(root)) {
            for (LatticeNode parent : temp.parents) {
                if (traversalStack.contains(parent)) {
                    int dim = nodeDimsMap.get(parent.name);
                    boolean afterThisNode = false;
                    for (LatticeNode node : parent.children.get(dim)) {
                        if (afterThisNode)
                            previousNodes.add(node);
                        if (node.equals(temp))
                            afterThisNode = true;
                    }
                    temp = parent;
                    break;
                }
            }
        }
        return previousNodes;
    }

    void initNodeDimensions() {
        for (int dim = 0; dim < ExhaustiveTrees.dims; dim++) {
            if (!children.get(dim).isEmpty()) {
                nodeDimsMap.put(name, dim);
                break;
            }
        }
        for (LatticeNode child : getChildren()) {
            ((ExhaustiveTreesNode) child).initNodeDimensions();
        }
    }

    void pushDescendantsToStack() {
        if (!isLeaf)
            traversalStack.push(this);
        for (LatticeNode child : getChildren()) {
            ((ExhaustiveTreesNode) child).pushDescendantsToStack();
        }
    }

    void printCurrentTree() {
        System.out.println(name);
        for (LatticeNode child : getChildren()) {
            ((ExhaustiveTreesNode) child).printSummaryTree(1);
        }
    }

    private void printSummaryTree(int spaces) {
        for (int i = 0; i < spaces; i++)
            System.out.print("  ");
        System.out.println(name);
        for (LatticeNode child : getChildren()) {
            if (!child.isLeaf)
                ((ExhaustiveTreesNode) child).printSummaryTree(spaces + 1);
        }
    }

    ReconstructionError getReconstructionError(ReconstructionError error) {
        if (getChildren().isEmpty()) {
            double origVal = Main.valMap.get(name);
            double e = Utility.calculateSMAPE(origVal, rootToLeaf);
            error.sum += e;
            if (e > error.worst)
                error.worst = e;
        } else {
            for (LatticeNode child : getChildren())
                ((ExhaustiveTreesNode) child).getReconstructionError(error);
        }
        return error;
    }
}
